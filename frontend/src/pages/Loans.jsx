import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { applyForLoan, getLoanHistory } from "../api/loans";
import { useAuth } from "../context/AuthContext";
import "./Dashboard.css";

const money = (amount) =>
  new Intl.NumberFormat("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(amount ?? 0);

const STATUS_BADGE = {
  PENDING: "bg-warning text-dark",
  UNDER_REVIEW: "bg-info text-dark",
  APPROVED: "bg-success",
  REJECTED: "bg-danger",
};

const emptyForm = {
  firstname: "",
  lastname: "",
  dob: "",
  phoneNo: "",
  address: "",
  loanType: "Personal",
  loanAmount: "",
  loanYears: "",
  employmentType: "Salaried",
  monthlyIncome: "",
  purpose: "",
};

const LOAN_TYPES = ["Personal", "Home", "Auto", "Education", "Business"];

export default function Loans() {
  const { user, logout } = useAuth();
  const [searchParams] = useSearchParams();
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [showApplyForm, setShowApplyForm] = useState(() => LOAN_TYPES.includes(searchParams.get("type")));
  const [form, setForm] = useState(() => {
    const typeParam = searchParams.get("type");
    return LOAN_TYPES.includes(typeParam) ? { ...emptyForm, loanType: typeParam } : emptyForm;
  });
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const [submitSuccess, setSubmitSuccess] = useState("");

  const loadHistory = () => {
    setLoading(true);
    setLoadError("");
    getLoanHistory()
      .then((data) => setLoans(data))
      .catch((err) => setLoadError(err.response?.data?.message || "Unable to load loan history."))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadHistory();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user.email]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitError("");
    setSubmitSuccess("");
    setSubmitting(true);

    try {
      const payload = {
        firstname: form.firstname,
        lastname: form.lastname,
        dob: form.dob,
        phoneNo: form.phoneNo,
        address: form.address,
        loanType: form.loanType,
        loanAmount: Number(form.loanAmount),
        loanYears: Number(form.loanYears),
        employmentType: form.employmentType,
        monthlyIncome: Number(form.monthlyIncome),
        purpose: form.purpose,
      };
      const created = await applyForLoan(payload);
      setSubmitSuccess(`Loan application submitted successfully! Application ID: ${created.id}`);
      setForm(emptyForm);
      setShowApplyForm(false);
      loadHistory();
    } catch (err) {
      const data = err.response?.data;
      if (data && typeof data === "object" && !data.message) {
        setSubmitError(Object.values(data).join(" "));
      } else {
        setSubmitError(data?.message || "Unable to submit loan application.");
      }
    } finally {
      setSubmitting(false);
    }
  };

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
              <li className="nav-item"><Link className="nav-link" to="/profile"><i className="fas fa-user me-1"></i>Profile</Link></li>
              <li className="nav-item"><Link className="nav-link" to="/transactions"><i className="fas fa-exchange-alt me-1"></i>Transactions</Link></li>
              <li className="nav-item"><Link className="nav-link active" to="/loans"><i className="fas fa-hand-holding-usd me-1"></i>Loans</Link></li>
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
        <div className="dashboard-header text-center mb-4">
          <h2 className="mb-2"><i className="fas fa-hand-holding-usd me-2"></i>My Loans</h2>
          <p className="mb-2">Track and manage your loan applications</p>
          <Link to="/loan-products" className="text-white text-decoration-underline small">
            View loan products &amp; interest rates
          </Link>
        </div>

        <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
          <h4 className="mb-0"><i className="fas fa-list me-2"></i>My Loan Applications</h4>
          <button
            type="button"
            className="btn btn-primary-custom"
            onClick={() => setShowApplyForm((v) => !v)}
          >
            <i className={`fas ${showApplyForm ? "fa-minus" : "fa-plus"} me-1`}></i>
            {showApplyForm ? "Close Application Form" : "Apply for New Loan"}
          </button>
        </div>

        {showApplyForm && (
          <div className="card mb-4">
            <div className="card-header bg-white">
              <h5 className="mb-0"><i className="fas fa-file-signature me-2"></i>Apply for a Loan</h5>
            </div>
            <div className="card-body">
              {submitError && <div className="alert alert-danger">{submitError}</div>}
              {submitSuccess && <div className="alert alert-success">{submitSuccess}</div>}

              <form onSubmit={handleSubmit}>
                <div className="row g-2 mb-2">
                  <div className="col-md-6">
                    <label className="form-label">First Name</label>
                    <input className="form-control" name="firstname" value={form.firstname} onChange={handleChange} required />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Last Name</label>
                    <input className="form-control" name="lastname" value={form.lastname} onChange={handleChange} required />
                  </div>
                </div>

                <div className="row g-2 mb-2">
                  <div className="col-md-6">
                    <label className="form-label">Date of Birth</label>
                    <input type="date" className="form-control" name="dob" value={form.dob} onChange={handleChange} required />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Phone Number</label>
                    <input className="form-control" name="phoneNo" value={form.phoneNo} onChange={handleChange} required />
                  </div>
                </div>

                <div className="mb-2">
                  <label className="form-label">Address</label>
                  <textarea className="form-control" name="address" rows="2" value={form.address} onChange={handleChange} required />
                </div>

                <div className="row g-2 mb-2">
                  <div className="col-md-6">
                    <label className="form-label">Loan Type</label>
                    <select className="form-select" name="loanType" value={form.loanType} onChange={handleChange}>
                      <option>Personal</option>
                      <option>Home</option>
                      <option>Auto</option>
                      <option>Education</option>
                      <option>Business</option>
                    </select>
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Employment Type</label>
                    <select className="form-select" name="employmentType" value={form.employmentType} onChange={handleChange}>
                      <option>Salaried</option>
                      <option>Self-Employed</option>
                      <option>Business Owner</option>
                      <option>Unemployed</option>
                    </select>
                  </div>
                </div>

                <div className="row g-2 mb-2">
                  <div className="col-md-6">
                    <label className="form-label">Loan Amount (₹)</label>
                    <input type="number" min="1" step="0.01" className="form-control" name="loanAmount" value={form.loanAmount} onChange={handleChange} required />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Duration (years)</label>
                    <input type="number" min="1" step="1" className="form-control" name="loanYears" value={form.loanYears} onChange={handleChange} required />
                  </div>
                </div>

                <div className="mb-2">
                  <label className="form-label">Monthly Income (₹)</label>
                  <input type="number" min="0" step="0.01" className="form-control" name="monthlyIncome" value={form.monthlyIncome} onChange={handleChange} required />
                </div>

                <div className="mb-3">
                  <label className="form-label">Purpose</label>
                  <textarea className="form-control" name="purpose" rows="2" value={form.purpose} onChange={handleChange} required />
                </div>

                <button type="submit" className="btn btn-primary-custom w-100" disabled={submitting}>
                  {submitting ? "Submitting..." : "Submit Application"}
                </button>
              </form>
            </div>
          </div>
        )}

        {loading && <p className="text-center">Loading loan history...</p>}
        {loadError && <div className="alert alert-danger">{loadError}</div>}

        {!loading && !loadError && loans.length === 0 && (
          <div className="card">
            <div className="card-body text-center py-5">
              <i className="fas fa-file-invoice-dollar text-muted mb-3" style={{ fontSize: "2.5rem" }}></i>
              <h5>No Loan Applications Found</h5>
              <p className="text-muted mb-3">You haven&rsquo;t applied for a loan yet. Ready to get started?</p>
              <button type="button" className="btn btn-primary-custom" onClick={() => setShowApplyForm(true)}>
                <i className="fas fa-plus me-1"></i>Apply for New Loan
              </button>
            </div>
          </div>
        )}

        {!loading && !loadError && loans.length > 0 && (
          <div className="row g-4">
            {loans.map((loan) => (
              <div className="col-md-6" key={loan.id}>
                <div className="card h-100">
                  <div className="card-body">
                    <div className="d-flex justify-content-between align-items-start mb-2">
                      <h5 className="mb-0">{loan.loanType} Loan</h5>
                      <span className={`badge ${STATUS_BADGE[loan.status] || "bg-secondary"}`}>{loan.status}</span>
                    </div>
                    <p className="text-muted small mb-3">
                      Application #{loan.id} &bull; Applied {loan.applicationDate ? new Date(loan.applicationDate).toLocaleDateString("en-IN") : "-"}
                    </p>
                    <div className="row">
                      <div className="col-6 mb-2">
                        <strong className="small">Loan Amount:</strong>
                        <p className="mb-0">₹{money(loan.loanAmount)}</p>
                      </div>
                      <div className="col-6 mb-2">
                        <strong className="small">Duration:</strong>
                        <p className="mb-0">{loan.loanYears} year(s)</p>
                      </div>
                      <div className="col-6 mb-2">
                        <strong className="small">Monthly Income:</strong>
                        <p className="mb-0">₹{money(loan.monthlyIncome)}</p>
                      </div>
                      <div className="col-6 mb-2">
                        <strong className="small">Employment:</strong>
                        <p className="mb-0">{loan.employmentType}</p>
                      </div>
                      <div className="col-12 mb-0">
                        <strong className="small">Purpose:</strong>
                        <p className="mb-0">{loan.purpose}</p>
                      </div>
                    </div>
                    {loan.status === "APPROVED" && (
                      <div className="mt-2">
                        <span className={`badge ${loan.paid ? "bg-success" : "bg-secondary"}`}>
                          {loan.paid ? "Repaid" : "Repayment Pending"}
                        </span>
                      </div>
                    )}
                    {loan.status === "REJECTED" && loan.rejectionReason && (
                      <div className="mt-2 small text-muted">
                        <strong>Reason:</strong> {loan.rejectionReason}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
