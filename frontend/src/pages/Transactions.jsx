import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getTransactionHistory } from "../api/transactions";
import { useAuth } from "../context/AuthContext";
import "./Dashboard.css";

const money = (amount) =>
  new Intl.NumberFormat("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(amount ?? 0);

export default function Transactions() {
  const { user, logout } = useAuth();
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  const loadHistory = () => {
    setLoading(true);
    setLoadError("");
    getTransactionHistory()
      .then((data) => setTransactions(data))
      .catch((err) => setLoadError(err.response?.data?.message || "Unable to load transaction history."))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadHistory();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user.email]);

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
              <li className="nav-item"><Link className="nav-link active" to="/transactions"><i className="fas fa-exchange-alt me-1"></i>Transactions</Link></li>
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
        <div className="dashboard-header text-center mb-4">
          <h2 className="mb-2"><i className="fas fa-exchange-alt me-2"></i>Transactions</h2>
          <p className="mb-0">Deposit, withdraw, and review your transaction history</p>
        </div>

        <div className="card">
          <div className="card-header bg-white d-flex justify-content-between align-items-center">
            <h5 className="mb-0"><i className="fas fa-history me-2"></i>Transaction History</h5>
            <button className="btn btn-sm btn-outline-secondary" onClick={loadHistory}>
              <i className="fas fa-sync-alt"></i>
            </button>
          </div>
          <div className="card-body">
            {loading && <p className="text-center">Loading transactions...</p>}
            {loadError && <div className="alert alert-danger">{loadError}</div>}

            {!loading && !loadError && (
              <div className="list-group list-group-flush">
                {transactions.length === 0 ? (
                  <div className="text-center text-muted py-3">No transactions yet</div>
                ) : (
                  transactions.map((txn) => (
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
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
