import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getDashboard, updateProfile } from "../api/user";
import { useAuth } from "../context/AuthContext";
import DocumentViewButton from "../components/DocumentViewButton";
import "./Dashboard.css";

const API_BASE = import.meta.env.VITE_API_BASE_URL;

const money = (amount) =>
  new Intl.NumberFormat("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(amount ?? 0);

const KYC_BADGE = {
  PENDING: { className: "bg-warning text-dark", label: "Verification Pending" },
  VERIFIED: { className: "bg-success", label: "KYC Verified" },
  REJECTED: { className: "bg-danger", label: "Verification Rejected" },
};

export default function Profile() {
  const { user, setUser, logout } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ fullName: "", email: "", username: "", password: "" });
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");
  const [saveSuccess, setSaveSuccess] = useState("");

  const loadProfile = () => {
    setLoading(true);
    setLoadError("");
    getDashboard()
      .then((res) => {
        setData(res);
        setForm({
          fullName: res.user?.fullName || "",
          email: res.user?.email || "",
          username: res.user?.username || "",
          password: "",
        });
      })
      .catch((err) => setLoadError(err.response?.data?.message || "Unable to load profile."))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user.email]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaveError("");
    setSaveSuccess("");
    setSaving(true);

    try {
      const updated = await updateProfile({
        fullName: form.fullName,
        email: form.email,
        username: form.username,
        password: form.password || undefined,
      });
      setUser(updated);
      setSaveSuccess("Profile updated successfully!");
      setEditing(false);
      loadProfile();
    } catch (err) {
      const body = err.response?.data;
      if (body && typeof body === "object" && !body.message) {
        setSaveError(Object.values(body).join(" "));
      } else {
        setSaveError(body?.message || "Something went wrong while updating profile.");
      }
    } finally {
      setSaving(false);
    }
  };

  const bankAccount = data?.bankAccount ?? null;
  const isJoint = bankAccount?.accountType?.toLowerCase() === "joint";

  return (
    <div className="dashboard-page">
      <nav className="navbar navbar-expand-lg navbar-dark mb-4">
        <div className="container">
          <Link className="navbar-brand" to="/dashboard">
            <i className="fas fa-university me-2"></i>SysBank
          </Link>
          <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span className="navbar-toggler-icon"></span>
          </button>
          <div className="collapse navbar-collapse" id="navbarNav">
            <ul className="navbar-nav ms-auto">
              <li className="nav-item"><Link className="nav-link" to="/dashboard"><i className="fas fa-home me-1"></i>Home</Link></li>
              <li className="nav-item"><Link className="nav-link active" to="/profile"><i className="fas fa-user me-1"></i>Profile</Link></li>
              <li className="nav-item"><Link className="nav-link" to="/transactions"><i className="fas fa-exchange-alt me-1"></i>Transactions</Link></li>
              <li className="nav-item"><Link className="nav-link" to="/loans"><i className="fas fa-hand-holding-usd me-1"></i>Loans</Link></li>
              <li className="nav-item"><Link className="nav-link" to="/tickets"><i className="fas fa-life-ring me-1"></i>Support</Link></li>
              <li className="nav-item">
                <a className="nav-link logout-btn" href="#" onClick={(e) => { e.preventDefault(); logout(); }}>
                  <i className="fas fa-sign-out-alt me-1"></i> Logout
                </a>
              </li>
            </ul>
          </div>
        </div>
      </nav>

      <div className="container">
        {loading && <p className="text-center">Loading your profile...</p>}
        {loadError && <div className="alert alert-danger">{loadError}</div>}

        {!loading && !loadError && (
          <>
            <div className="card mb-4">
              <div className="dashboard-header d-flex align-items-center gap-4 mb-0">
                <img
                  src={bankAccount?.photoUrl ? `${API_BASE}${bankAccount.photoUrl}` : "https://via.placeholder.com/150"}
                  onError={(e) => { e.target.src = "https://via.placeholder.com/150"; }}
                  alt="Profile"
                  style={{ width: 96, height: 96, borderRadius: "50%", objectFit: "cover", border: "3px solid white" }}
                />
                <div>
                  <h3 className="mb-1">{data.user?.fullName || "Account Holder"}</h3>
                  <small className="opacity-75">Customer ID: SYB{bankAccount?.id ?? data.user?.id}</small>
                  <div className="d-flex flex-wrap gap-2 mt-2">
                    <span className="badge bg-white text-dark">Primary Account Holder</span>
                    {bankAccount && (
                      <span className={`badge ${(KYC_BADGE[bankAccount.kycStatus] || KYC_BADGE.PENDING).className}`}>
                        {(KYC_BADGE[bankAccount.kycStatus] || KYC_BADGE.PENDING).label}
                      </span>
                    )}
                  </div>
                </div>
              </div>

              <div className="card-body">
                {saveSuccess && <div className="alert alert-success">{saveSuccess}</div>}
                {bankAccount?.kycStatus === "REJECTED" && (
                  <div className="alert alert-danger">
                    <strong>Your account verification was rejected.</strong>
                    {bankAccount.kycRejectionReason && <> {bankAccount.kycRejectionReason}</>}
                    {" "}Please contact support or resubmit your documents.
                  </div>
                )}
                {bankAccount?.kycStatus === "PENDING" && (
                  <div className="alert alert-warning">
                    Your account verification is pending review. Some actions (deposits, withdrawals, loan applications) are restricted until an admin verifies your documents.
                  </div>
                )}

                <h5 className="mb-3"><i className="fas fa-user me-2"></i>Primary Account Holder Information</h5>
                <div className="row mb-3">
                  <div className="col-md-6 mb-3">
                    <p className="mb-1 text-muted">Full Name</p>
                    <p className="fw-bold">{data.user?.fullName}</p>
                  </div>
                  <div className="col-md-6 mb-3">
                    <p className="mb-1 text-muted">Date of Birth</p>
                    <p className="fw-bold">{bankAccount?.dob || "Not Available"}</p>
                  </div>
                  <div className="col-md-6 mb-3">
                    <p className="mb-1 text-muted">Email Address</p>
                    <p className="fw-bold">{data.user?.email}</p>
                  </div>
                  <div className="col-md-6 mb-3">
                    <p className="mb-1 text-muted">Phone Number</p>
                    <p className="fw-bold">{bankAccount?.phoneNo || "Not Available"}</p>
                  </div>
                  <div className="col-12">
                    <p className="mb-1 text-muted">Residential Address</p>
                    <p className="fw-bold">{bankAccount?.address || "Not Available"}</p>
                  </div>
                </div>

                {bankAccount && (
                  <>
                    <hr />
                    <h5 className="mb-3"><i className="fas fa-piggy-bank me-2"></i>Account Information</h5>
                    <div className="row mb-3">
                      <div className="col-md-6 mb-3">
                        <p className="mb-1 text-muted">Account Type</p>
                        <p className="fw-bold text-capitalize">
                          {bankAccount.accountType || "savings"}
                          {isJoint && <span className="badge bg-primary ms-2">Joint Account</span>}
                        </p>
                      </div>
                      <div className="col-md-6 mb-3">
                        <p className="mb-1 text-muted">Account Balance</p>
                        <p className="balance-amount mb-0" style={{ fontSize: "1.75rem" }}>₹ {money(bankAccount.balance)}</p>
                      </div>
                      <div className="col-md-6 mb-3">
                        <p className="mb-1 text-muted">Gender</p>
                        <p className="fw-bold">{bankAccount.gender || "Not Available"}</p>
                      </div>
                      <div className="col-md-6 mb-3">
                        <p className="mb-1 text-muted">Account Number</p>
                        <p className="fw-bold">SYB{bankAccount.id}</p>
                      </div>
                    </div>

                    <div className="mb-3">
                      <p className="mb-2 text-muted">Primary Account Holder Documents</p>
                      <div className="d-flex flex-wrap gap-2">
                        {bankAccount.aadhaarUrl && (
                          <DocumentViewButton url={bankAccount.aadhaarUrl} label="View Aadhaar" className="btn btn-outline-primary btn-sm" />
                        )}
                        {bankAccount.panUrl && (
                          <DocumentViewButton url={bankAccount.panUrl} label="View PAN Card" className="btn btn-outline-primary btn-sm" />
                        )}
                        {!bankAccount.aadhaarUrl && !bankAccount.panUrl && (
                          <span className="text-muted">No documents on file</span>
                        )}
                      </div>
                    </div>
                  </>
                )}

                {isJoint && (
                  <>
                    <hr />
                    <h5 className="mb-3"><i className="fas fa-users me-2"></i>Joint Account Holder Information</h5>
                    <div className="d-flex align-items-center gap-3 mb-3">
                      <img
                        src={bankAccount.jointPhotoUrl ? `${API_BASE}${bankAccount.jointPhotoUrl}` : "https://via.placeholder.com/80"}
                        onError={(e) => { e.target.src = "https://via.placeholder.com/80"; }}
                        alt="Joint holder"
                        style={{ width: 64, height: 64, borderRadius: "50%", objectFit: "cover" }}
                      />
                      <div>
                        <h6 className="mb-0">{bankAccount.jointFullName || "Joint Holder Name"}</h6>
                        <small className="text-muted">Joint Holder ID: {bankAccount.jointHolderId || "JNT000"}</small>
                      </div>
                    </div>
                    <div className="row mb-3">
                      <div className="col-md-6 mb-3">
                        <p className="mb-1 text-muted">Date of Birth</p>
                        <p className="fw-bold">{bankAccount.jointDob || "Not Available"}</p>
                      </div>
                      <div className="col-md-6 mb-3">
                        <p className="mb-1 text-muted">Gender</p>
                        <p className="fw-bold">{bankAccount.jointGender || "Not Available"}</p>
                      </div>
                      <div className="col-md-6 mb-3">
                        <p className="mb-1 text-muted">Email Address</p>
                        <p className="fw-bold">{bankAccount.jointEmail || "Not Available"}</p>
                      </div>
                      <div className="col-md-6 mb-3">
                        <p className="mb-1 text-muted">Phone Number</p>
                        <p className="fw-bold">{bankAccount.jointPhoneNo || "Not Available"}</p>
                      </div>
                      <div className="col-12">
                        <p className="mb-1 text-muted">Residential Address</p>
                        <p className="fw-bold">{bankAccount.jointAddress || "Not Available"}</p>
                      </div>
                    </div>
                    <div className="mb-3">
                      <p className="mb-2 text-muted">Joint Account Holder Documents</p>
                      <div className="d-flex flex-wrap gap-2">
                        {bankAccount.jointAadhaarUrl && (
                          <DocumentViewButton url={bankAccount.jointAadhaarUrl} label="View Joint Aadhaar" className="btn btn-outline-success btn-sm" />
                        )}
                        {bankAccount.jointPanUrl && (
                          <DocumentViewButton url={bankAccount.jointPanUrl} label="View Joint PAN" className="btn btn-outline-success btn-sm" />
                        )}
                        {!bankAccount.jointAadhaarUrl && !bankAccount.jointPanUrl && (
                          <span className="text-muted">No documents on file</span>
                        )}
                      </div>
                    </div>
                  </>
                )}
              </div>

              <div className="card-footer bg-white text-end">
                <button className="btn btn-primary-custom" onClick={() => setEditing((v) => !v)}>
                  <i className="fas fa-edit me-1"></i> {editing ? "Cancel" : "Update Details"}
                </button>
              </div>
            </div>

            {editing && (
              <div className="card mb-4">
                <div className="card-header bg-white">
                  <h5 className="mb-0"><i className="fas fa-user-edit me-2"></i>Update Profile Details</h5>
                </div>
                <div className="card-body">
                  {saveError && <div className="alert alert-danger">{saveError}</div>}
                  <form onSubmit={handleSave}>
                    <div className="row g-3">
                      <div className="col-md-6">
                        <label className="form-label">Full Name *</label>
                        <input className="form-control" name="fullName" value={form.fullName} onChange={handleChange} required />
                      </div>
                      <div className="col-md-6">
                        <label className="form-label">Email Address *</label>
                        <input type="email" className="form-control" name="email" value={form.email} onChange={handleChange} required />
                      </div>
                      <div className="col-md-6">
                        <label className="form-label">Username *</label>
                        <input className="form-control" name="username" value={form.username} onChange={handleChange} required />
                      </div>
                      <div className="col-md-6">
                        <label className="form-label">New Password</label>
                        <input type="password" className="form-control" name="password" value={form.password} onChange={handleChange} placeholder="Leave blank to keep current password" />
                      </div>
                    </div>
                    <div className="d-flex justify-content-end gap-2 mt-4">
                      <button type="button" className="btn btn-outline-secondary" onClick={() => setEditing(false)}>Cancel</button>
                      <button type="submit" className="btn btn-primary-custom" disabled={saving}>
                        {saving ? "Saving..." : "Save Changes"}
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
