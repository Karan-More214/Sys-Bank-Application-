import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getDashboard } from "../api/user";
import { getMyTransfers, initiateTransfer } from "../api/transfers";
import { useAuth } from "../context/AuthContext";
import { TransferStatusBadge } from "../components/TransferStatusActions";
import "./Dashboard.css";

const money = (amount) =>
  new Intl.NumberFormat("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(amount ?? 0);

const FLAG_THRESHOLD = 100000;

const emptyForm = { recipient: "", amount: "", note: "" };

export default function Transfer() {
  const { user, logout } = useAuth();
  const [balance, setBalance] = useState(null);
  const [balanceLoading, setBalanceLoading] = useState(true);

  const [transfers, setTransfers] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [historyError, setHistoryError] = useState("");

  const [form, setForm] = useState(emptyForm);
  const [step, setStep] = useState("form"); // "form" | "confirm"
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const loadBalance = () => {
    setBalanceLoading(true);
    getDashboard()
      .then((res) => setBalance(res.bankAccount?.balance ?? null))
      .catch(() => {})
      .finally(() => setBalanceLoading(false));
  };

  const loadHistory = () => {
    setHistoryLoading(true);
    setHistoryError("");
    getMyTransfers()
      .then((data) => setTransfers(data))
      .catch((err) => setHistoryError(err.response?.data?.message || "Unable to load transfer history."))
      .finally(() => setHistoryLoading(false));
  };

  useEffect(() => {
    loadBalance();
    loadHistory();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user.email]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const handleReview = (e) => {
    e.preventDefault();
    setFormError("");
    const amount = Number(form.amount);
    if (!form.recipient.trim()) {
      setFormError("Enter a recipient email or account number.");
      return;
    }
    if (!amount || amount <= 0) {
      setFormError("Enter a valid amount.");
      return;
    }
    if (balance != null && amount > balance) {
      setFormError("This amount exceeds your current balance.");
      return;
    }
    setStep("confirm");
  };

  const handleConfirm = async () => {
    setSubmitting(true);
    setFormError("");
    try {
      const amount = Number(form.amount);
      const result = await initiateTransfer({
        recipient: form.recipient.trim(),
        amount,
        note: form.note.trim() || undefined,
      });
      if (result.status === "FLAGGED") {
        setSuccessMessage(`Transfer of ₹${money(amount)} was submitted for admin review since it's ₹${money(FLAG_THRESHOLD)} or more. Funds have not moved yet.`);
      } else {
        setSuccessMessage(`Transfer of ₹${money(amount)} to ${form.recipient.trim()} completed.`);
      }
      setForm(emptyForm);
      setStep("form");
      loadBalance();
      loadHistory();
    } catch (err) {
      const data = err.response?.data;
      setFormError(data?.message || "Unable to complete this transfer.");
      setStep("form");
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
              <li className="nav-item"><Link className="nav-link active" to="/transfer"><i className="fas fa-paper-plane me-1"></i>Transfer</Link></li>
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
          <h2 className="mb-2"><i className="fas fa-paper-plane me-2"></i>Transfer Money</h2>
          <p className="mb-0">Send money to another SysBank account instantly</p>
        </div>

        <div className="row g-4">
          <div className="col-lg-5">
            <div className="card mb-3">
              <div className="card-body">
                <h5 className="text-muted mb-2"><i className="fas fa-wallet me-1"></i> Available Balance</h5>
                <p className="balance-amount mb-0">{balanceLoading ? "..." : `₹ ${money(balance ?? 0)}`}</p>
              </div>
            </div>

            <div className="card">
              <div className="card-header bg-white">
                <h5 className="mb-0">
                  <i className="fas fa-file-signature me-2"></i>
                  {step === "confirm" ? "Confirm Transfer" : "New Transfer"}
                </h5>
              </div>
              <div className="card-body">
                {formError && <div className="alert alert-danger">{formError}</div>}
                {successMessage && <div className="alert alert-success">{successMessage}</div>}

                {step === "form" && (
                  <form onSubmit={handleReview}>
                    <div className="mb-2">
                      <label className="form-label">Recipient (email or account number)</label>
                      <input
                        className="form-control"
                        name="recipient"
                        value={form.recipient}
                        onChange={handleChange}
                        placeholder="name@example.com or SYB123"
                        required
                      />
                    </div>
                    <div className="mb-2">
                      <label className="form-label">Amount (₹)</label>
                      <input
                        type="number"
                        min="1"
                        step="0.01"
                        className="form-control"
                        name="amount"
                        value={form.amount}
                        onChange={handleChange}
                        required
                      />
                      {Number(form.amount) >= FLAG_THRESHOLD && (
                        <small className="text-muted">
                          Transfers of ₹{money(FLAG_THRESHOLD)} or more are held for admin review before funds move.
                        </small>
                      )}
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Note (optional)</label>
                      <input className="form-control" name="note" value={form.note} onChange={handleChange} maxLength={200} />
                    </div>
                    <button type="submit" className="btn btn-primary-custom w-100">Review Transfer</button>
                  </form>
                )}

                {step === "confirm" && (
                  <div>
                    <p className="mb-2">You're about to send:</p>
                    <h3 className="mb-3">₹{money(Number(form.amount))}</h3>
                    <p className="mb-1"><strong>To:</strong> {form.recipient}</p>
                    {form.note && <p className="mb-1"><strong>Note:</strong> {form.note}</p>}
                    {Number(form.amount) >= FLAG_THRESHOLD && (
                      <div className="alert alert-warning mt-3 mb-3">
                        This is above the ₹{money(FLAG_THRESHOLD)} threshold, so it will be held for admin review instead of moving instantly.
                      </div>
                    )}
                    <div className="d-flex gap-2 mt-3">
                      <button type="button" className="btn btn-outline-secondary flex-fill" disabled={submitting} onClick={() => setStep("form")}>
                        Edit
                      </button>
                      <button type="button" className="btn btn-primary-custom flex-fill" disabled={submitting} onClick={handleConfirm}>
                        {submitting ? "Sending..." : "Confirm & Send"}
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="col-lg-7">
            <div className="card">
              <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <h5 className="mb-0"><i className="fas fa-history me-2"></i>Transfer History</h5>
                <button className="btn btn-sm btn-outline-secondary" onClick={loadHistory}>
                  <i className="fas fa-sync-alt"></i>
                </button>
              </div>
              <div className="card-body">
                {historyLoading && <p className="text-center">Loading transfer history...</p>}
                {historyError && <div className="alert alert-danger">{historyError}</div>}

                {!historyLoading && !historyError && transfers.length === 0 && (
                  <div className="text-center text-muted py-3">No transfers yet</div>
                )}

                {!historyLoading && !historyError && transfers.length > 0 && (
                  <div className="list-group list-group-flush">
                    {transfers.map((t) => {
                      const sent = t.fromEmail === user.email;
                      return (
                        <div className="list-group-item" key={t.id}>
                          <div className="d-flex justify-content-between align-items-start flex-wrap gap-2">
                            <div>
                              <h6 className="mb-1">
                                {sent ? `To ${t.toEmail}` : `From ${t.fromEmail}`}
                              </h6>
                              <small className="text-muted">
                                {t.createdAt ? new Date(t.createdAt).toLocaleString("en-IN", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" }) : ""}
                                {t.note && <> &bull; &ldquo;{t.note}&rdquo;</>}
                              </small>
                            </div>
                            <div className="text-end">
                              <div className={sent ? "text-danger fw-bold" : "text-success fw-bold"}>
                                {sent ? "-" : "+"}₹{money(t.amount)}
                              </div>
                              <TransferStatusBadge status={t.status} />
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
