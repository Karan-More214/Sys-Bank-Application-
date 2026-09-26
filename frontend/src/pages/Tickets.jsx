import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getMyTickets, raiseTicket } from "../api/tickets";
import { useAuth } from "../context/AuthContext";
import "./Dashboard.css";

const CATEGORIES = ["Account", "Loan", "Transaction", "Card", "Other"];
const PRIORITIES = ["Low", "Medium", "High"];

const STATUS_BADGE = {
  OPEN: "bg-warning text-dark",
  IN_PROGRESS: "bg-info text-dark",
  RESOLVED: "bg-success",
  CLOSED: "bg-secondary",
};

const emptyForm = { subject: "", description: "", category: "Account", priority: "Medium" };

export default function Tickets() {
  const { user, logout } = useAuth();
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [expandedId, setExpandedId] = useState(null);

  const [form, setForm] = useState(emptyForm);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const [submitSuccess, setSubmitSuccess] = useState("");

  const loadTickets = () => {
    setLoading(true);
    setLoadError("");
    getMyTickets()
      .then((data) => setTickets(data))
      .catch((err) => setLoadError(err.response?.data?.message || "Unable to load your tickets."))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadTickets();
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
      const created = await raiseTicket(form);
      setSubmitSuccess(`Ticket #${created.id} submitted successfully! Our team will respond soon.`);
      setForm(emptyForm);
      loadTickets();
    } catch (err) {
      const data = err.response?.data;
      if (data && typeof data === "object" && !data.message) {
        setSubmitError(Object.values(data).join(" "));
      } else {
        setSubmitError(data?.message || "Unable to submit ticket.");
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
              <li className="nav-item"><Link className="nav-link" to="/loans"><i className="fas fa-hand-holding-usd me-1"></i>Loans</Link></li>
              <li className="nav-item"><Link className="nav-link active" to="/tickets"><i className="fas fa-life-ring me-1"></i>Support</Link></li>
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
          <h2 className="mb-2"><i className="fas fa-life-ring me-2"></i>Support</h2>
          <p className="mb-0">Raise a ticket and track its status</p>
        </div>

        <div className="row g-4">
          <div className="col-lg-5">
            <div className="card">
              <div className="card-header bg-white">
                <h5 className="mb-0"><i className="fas fa-plus-circle me-2"></i>Raise a Ticket</h5>
              </div>
              <div className="card-body">
                {submitError && <div className="alert alert-danger">{submitError}</div>}
                {submitSuccess && <div className="alert alert-success">{submitSuccess}</div>}

                <form onSubmit={handleSubmit}>
                  <div className="mb-2">
                    <label className="form-label">Subject</label>
                    <input className="form-control" name="subject" value={form.subject} onChange={handleChange} required />
                  </div>

                  <div className="row g-2 mb-2">
                    <div className="col-6">
                      <label className="form-label">Category</label>
                      <select className="form-select" name="category" value={form.category} onChange={handleChange}>
                        {CATEGORIES.map((c) => <option key={c}>{c}</option>)}
                      </select>
                    </div>
                    <div className="col-6">
                      <label className="form-label">Priority</label>
                      <select className="form-select" name="priority" value={form.priority} onChange={handleChange}>
                        {PRIORITIES.map((p) => <option key={p}>{p}</option>)}
                      </select>
                    </div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Description</label>
                    <textarea className="form-control" name="description" rows="5" value={form.description} onChange={handleChange} required />
                  </div>

                  <button type="submit" className="btn btn-primary-custom w-100" disabled={submitting}>
                    {submitting ? "Submitting..." : "Submit Ticket"}
                  </button>
                </form>
              </div>
            </div>
          </div>

          <div className="col-lg-7">
            <div className="card">
              <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <h5 className="mb-0"><i className="fas fa-list me-2"></i>My Tickets</h5>
                <button className="btn btn-sm btn-outline-secondary" onClick={loadTickets}>
                  <i className="fas fa-sync-alt"></i>
                </button>
              </div>
              <div className="card-body">
                {loading && <p className="text-center">Loading tickets...</p>}
                {loadError && <div className="alert alert-danger">{loadError}</div>}

                {!loading && !loadError && tickets.length === 0 && (
                  <div className="text-center text-muted py-3">No tickets raised yet</div>
                )}

                {!loading && !loadError && tickets.length > 0 && (
                  <div className="list-group list-group-flush">
                    {tickets.map((t) => (
                      <div className="list-group-item" key={t.id}>
                        <div
                          className="d-flex justify-content-between align-items-start"
                          style={{ cursor: "pointer" }}
                          onClick={() => setExpandedId(expandedId === t.id ? null : t.id)}
                        >
                          <div>
                            <h6 className="mb-1">#{t.id} &mdash; {t.subject}</h6>
                            <small className="text-muted">
                              {t.category} &bull; {t.priority} priority &bull; {t.createdAt ? new Date(t.createdAt).toLocaleDateString("en-IN") : ""}
                            </small>
                          </div>
                          <span className={`badge ${STATUS_BADGE[t.status] || "bg-secondary"}`}>{t.status.replace("_", " ")}</span>
                        </div>

                        {expandedId === t.id && (
                          <div className="mt-3 ps-2 border-start">
                            <p className="mb-2">{t.description}</p>
                            {t.adminResponse ? (
                              <div className="alert alert-info mb-0">
                                <strong>Support response{t.resolvedByName ? ` from ${t.resolvedByName}` : ""}:</strong>
                                <div>{t.adminResponse}</div>
                              </div>
                            ) : (
                              <p className="text-muted mb-0 fst-italic">No response yet.</p>
                            )}
                          </div>
                        )}
                      </div>
                    ))}
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
