import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { getDashboard } from "../api/user";
import { getMyTickets } from "../api/tickets";
import { useAuth } from "../context/AuthContext";
import "./Dashboard.css";

const API_BASE = import.meta.env.VITE_API_BASE_URL;

const money = (amount) =>
  new Intl.NumberFormat("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(amount ?? 0);

const maskAccountNumber = (id) => {
  if (!id) return "•••• •••• ----";
  const last4 = String(id).padStart(4, "0").slice(-4);
  return `•••• •••• ${last4}`;
};

const memberSince = (isoDate) => {
  if (!isoDate) return "N/A";
  const date = new Date(isoDate);
  if (Number.isNaN(date.getTime())) return "N/A";
  return date.toLocaleDateString("en-IN", { month: "short", year: "numeric" });
};

const titleCase = (value) =>
  value ? value.charAt(0).toUpperCase() + value.slice(1).toLowerCase() : "Savings";

export default function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [openTicketCount, setOpenTicketCount] = useState(0);
  const isAdmin = user.role && user.role.toLowerCase() === "admin";

  useEffect(() => {
    if (isAdmin) {
      navigate("/admin", { replace: true });
      return;
    }

    let cancelled = false;

    getDashboard()
      .then((res) => { if (!cancelled) setData(res); })
      .catch((err) => { if (!cancelled) setError(err.response?.data?.message || "Unable to load dashboard."); })
      .finally(() => { if (!cancelled) setLoading(false); });

    getMyTickets()
      .then((res) => {
        if (cancelled) return;
        const open = res.filter((t) => t.status === "OPEN" || t.status === "IN_PROGRESS").length;
        setOpenTicketCount(open);
      })
      .catch(() => { /* non-critical for the dashboard */ });

    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user.email, isAdmin]);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const bankAccount = data?.bankAccount ?? null;
  const hasAccount = data?.hasAccount ?? false;

  if (isAdmin) {
    return null;
  }

  return (
    <div className="dashboard-page">
      {/* Navigation */}
      <nav className="navbar navbar-expand-lg navbar-dark mb-4">
        <div className="container">
          <a className="navbar-brand" href="#">
            <i className="fas fa-university me-2"></i>SysBank
          </a>
          <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span className="navbar-toggler-icon"></span>
          </button>
          <div className="collapse navbar-collapse" id="navbarNav">
            <ul className="navbar-nav ms-auto">
              <li className="nav-item">
                <a className="nav-link active" href="#"><i className="fas fa-home me-1"></i>Home</a>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/profile"><i className="fas fa-user me-1"></i> Profile</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/transactions"><i className="fas fa-exchange-alt me-1"></i> Transactions</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/loans"><i className="fas fa-hand-holding-usd me-1"></i> Loans</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/tickets">
                  <i className="fas fa-life-ring me-1"></i> Support
                  {openTicketCount > 0 && <span className="badge bg-danger ms-1">{openTicketCount}</span>}
                </Link>
              </li>
              {!hasAccount && (
                <li className="nav-item">
                  <a className="nav-link btn-create-account text-white px-3 mx-2" href={`${API_BASE}/main/create`}>
                    <i className="fas fa-user-plus me-1"></i> Create Account
                  </a>
                </li>
              )}
              <li className="nav-item">
                <a className="nav-link logout-btn" href="#" onClick={(e) => { e.preventDefault(); handleLogout(); }}>
                  <i className="fas fa-sign-out-alt me-1"></i> Logout
                </a>
              </li>
            </ul>
          </div>
        </div>
      </nav>

      <div className="container">
        <div className="dashboard-header text-center mb-4">
          <h2 className="mb-3">Welcome back, <strong>{bankAccount?.firstname || user.fullName || "User"}</strong>!</h2>
          <p><i className="fas fa-envelope me-1"></i> Account Email: <span>{bankAccount?.email || user.email}</span></p>
        </div>

        {loading && <p className="text-center">Loading your account...</p>}
        {error && <div className="alert alert-danger">{error}</div>}

        {!loading && !error && (
          <>
            <div className="row mb-4 g-4">
              <div className="col-md-7">
                <div className="balance-hero h-100">
                  <h5 className="mb-3"><i className="fas fa-wallet me-1"></i> CURRENT BALANCE</h5>
                  <div className="d-flex justify-content-between align-items-end flex-wrap gap-3">
                    <div>
                      <p className="balance-amount">₹ {money(bankAccount?.balance ?? 0)}</p>
                      <p className="mb-0"><i className="fas fa-shield-alt me-1"></i> {hasAccount ? "Account Active" : "No Account Yet"}</p>
                    </div>
                    <span className={`badge ${hasAccount ? "bg-success" : "bg-secondary"}`}>{hasAccount ? "Active" : "None"}</span>
                  </div>
                </div>
              </div>
              <div className="col-md-5">
                <div className="card h-100">
                  <div className="card-header bg-white">
                    <h5><i className="fas fa-id-card me-2"></i> Account Summary</h5>
                  </div>
                  <div className="card-body">
                    <div className="mb-3">
                      <strong><i className="fas fa-layer-group me-1"></i> Account Type:</strong>
                      <p>{hasAccount ? `${titleCase(bankAccount?.accountType)} Account` : "N/A"}</p>
                    </div>
                    <div className="mb-3">
                      <strong><i className="fas fa-hashtag me-1"></i> Account Number:</strong>
                      <p>{hasAccount ? maskAccountNumber(bankAccount?.id) : "N/A"}</p>
                    </div>
                    <div className="mb-0">
                      <strong><i className="fas fa-calendar-alt me-1"></i> Member Since:</strong>
                      <p className="mb-0">{hasAccount ? memberSince(bankAccount?.createdAt) : "N/A"}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <h4 className="mb-4"><i className="fas fa-concierge-bell me-2"></i> Banking Services</h4>
            <div className="row g-4 mb-4">
              <div className="col-md-6 col-lg-3">
                <Link className="card card-feature text-center p-4" to="/deposit">
                  <i className="fas fa-money-bill-wave"></i>
                  <h5>Deposit</h5>
                  <p>Add funds to your account</p>
                </Link>
              </div>
              <div className="col-md-6 col-lg-3">
                <Link className="card card-feature text-center p-4" to="/withdraw">
                  <i className="fas fa-hand-holding-usd"></i>
                  <h5>Withdraw</h5>
                  <p>Withdraw funds from your account</p>
                </Link>
              </div>
              <div className="col-md-6 col-lg-3">
                <Link className="card card-feature text-center p-4" to="/transactions">
                  <i className="fas fa-history"></i>
                  <h5>Transactions</h5>
                  <p>View your transaction history</p>
                </Link>
              </div>
              <div className="col-md-6 col-lg-3">
                <Link
                  className="card card-feature text-center p-4"
                  to={hasAccount ? "/loans" : "#"}
                  onClick={(e) => { if (!hasAccount) { e.preventDefault(); window.location.href = `${API_BASE}/main/create`; } }}
                >
                  <i className="fas fa-hand-holding-heart"></i>
                  <h5>Loan</h5>
                  <p>Apply for a personal loan</p>
                </Link>
              </div>
            </div>

            <div className="row mb-4">
              <div className="col-md-6">
                <div className="card h-100">
                  <div className="card-header bg-white">
                    <h5><i className="fas fa-exchange-alt me-2"></i> Recent Transactions</h5>
                  </div>
                  <div className="card-body">
                    <div className="list-group list-group-flush">
                      {(!data?.transactions || data.transactions.length === 0) ? (
                        <div className="text-center text-muted py-3">No transactions yet</div>
                      ) : (
                        data.transactions.map((txn) => (
                          <div className="list-group-item d-flex justify-content-between align-items-center" key={txn.id}>
                            <div>
                              <h6 className="mb-1">{txn.type}</h6>
                              <small className="text-muted">
                                {new Date(txn.timestamp).toLocaleString("en-IN", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" })}
                              </small>
                            </div>
                            <span className={txn.type === "Deposit" ? "text-success fw-bold" : "text-danger fw-bold"}>
                              {txn.type === "Deposit" ? "+" : "-"}₹{money(txn.amount)}
                            </span>
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                </div>
              </div>

              <div className="col-md-6">
                <div className="card h-100">
                  <div className="card-header bg-white">
                    <h5><i className="fas fa-info-circle me-2"></i> Account Information</h5>
                  </div>
                  <div className="card-body">
                    <div className="row">
                      <div className="col-6 mb-3">
                        <strong><i className="fas fa-user me-1"></i> Name:</strong>
                        <p>{bankAccount ? `${bankAccount.firstname} ${bankAccount.lastname}` : "N/A"}</p>
                      </div>
                      <div className="col-6 mb-3">
                        <strong><i className="fas fa-envelope me-1"></i> Email:</strong>
                        <p>{bankAccount?.email || "N/A"}</p>
                      </div>
                      <div className="col-6 mb-3">
                        <strong><i className="fas fa-phone me-1"></i> Phone:</strong>
                        <p>{bankAccount?.phoneNo || "N/A"}</p>
                      </div>
                      <div className="col-6 mb-3">
                        <strong><i className="fas fa-map-marker-alt me-1"></i> Address:</strong>
                        <p>{bankAccount?.address || "N/A"}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
