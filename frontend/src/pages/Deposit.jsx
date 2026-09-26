import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getTransactionHistory, initiateDeposit, verifyDeposit } from "../api/transactions";
import { useAuth } from "../context/AuthContext";
import "./Dashboard.css";

const money = (amount) =>
  new Intl.NumberFormat("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(amount ?? 0);

export default function Deposit() {
  const { user, logout } = useAuth();
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  const [depositAmount, setDepositAmount] = useState("");
  const [depositBusy, setDepositBusy] = useState(false);
  const [depositMessage, setDepositMessage] = useState(null);

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

  const handleDeposit = async (e) => {
    e.preventDefault();
    setDepositMessage(null);
    const amount = Number(depositAmount);
    if (!amount || amount <= 0) {
      setDepositMessage({ type: "danger", text: "Enter a valid amount." });
      return;
    }
    if (typeof window.Razorpay !== "function") {
      setDepositMessage({ type: "danger", text: "Payment gateway failed to load. Please refresh and try again." });
      return;
    }

    setDepositBusy(true);
    try {
      const order = await initiateDeposit(amount);

      const options = {
        key: order.key,
        amount: order.amount,
        currency: order.currency,
        name: "SYS Bank",
        description: "Deposit to your account",
        order_id: order.orderId,
        prefill: { name: user.fullName, email: user.email },
        theme: { color: "#2c3e50" },
        handler: async (response) => {
          try {
            await verifyDeposit({
              amount,
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            });
            setDepositMessage({ type: "success", text: `Deposit of ₹${money(amount)} successful.` });
            setDepositAmount("");
            loadHistory();
          } catch (err) {
            setDepositMessage({ type: "danger", text: err.response?.data?.message || "Payment verification failed." });
          } finally {
            setDepositBusy(false);
          }
        },
        modal: {
          ondismiss: () => setDepositBusy(false),
        },
      };

      const rzp = new window.Razorpay(options);
      rzp.open();
    } catch (err) {
      setDepositMessage({ type: "danger", text: err.response?.data?.message || "Unable to start payment." });
      setDepositBusy(false);
    }
  };

  const recentDeposits = transactions.filter((txn) => txn.type === "Deposit").slice(0, 5);

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
          <h2 className="mb-2"><i className="fas fa-plus-circle me-2"></i>Deposit Funds</h2>
          <p className="mb-0">Add money to your account</p>
        </div>

        <div className="row g-4">
          <div className="col-lg-6">
            <div className="card h-100">
              <div className="card-header bg-white">
                <h5 className="mb-0"><i className="fas fa-money-bill-wave me-2"></i>Deposit</h5>
              </div>
              <div className="card-body">
                {depositMessage && <div className={`alert alert-${depositMessage.type}`}>{depositMessage.text}</div>}
                <form onSubmit={handleDeposit}>
                  <label className="form-label">Amount (₹)</label>
                  <div className="d-flex gap-2">
                    <input
                      type="number"
                      min="1"
                      step="0.01"
                      className="form-control"
                      placeholder="Amount (₹)"
                      value={depositAmount}
                      onChange={(e) => setDepositAmount(e.target.value)}
                      required
                    />
                    <button type="submit" className="btn btn-primary-custom text-nowrap" disabled={depositBusy}>
                      {depositBusy ? "Processing..." : "Pay with Razorpay"}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>

          <div className="col-lg-6">
            <div className="card h-100">
              <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <h5 className="mb-0"><i className="fas fa-history me-2"></i>Recent Deposits</h5>
                <button className="btn btn-sm btn-outline-secondary" onClick={loadHistory}>
                  <i className="fas fa-sync-alt"></i>
                </button>
              </div>
              <div className="card-body">
                {loading && <p className="text-center">Loading...</p>}
                {loadError && <div className="alert alert-danger">{loadError}</div>}

                {!loading && !loadError && (
                  <div className="list-group list-group-flush">
                    {recentDeposits.length === 0 ? (
                      <div className="text-center text-muted py-3">No deposits yet</div>
                    ) : (
                      recentDeposits.map((txn) => (
                        <div className="list-group-item d-flex justify-content-between align-items-center" key={txn.id}>
                          <small className="text-muted">
                            {new Date(txn.timestamp).toLocaleString("en-IN", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" })}
                          </small>
                          <span className="text-success fw-bold">+₹{money(txn.amount)}</span>
                        </div>
                      ))
                    )}
                  </div>
                )}
                <div className="text-end mt-2">
                  <Link to="/transactions" className="small">View full history &rarr;</Link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
