import { useEffect, useMemo, useState } from "react";
import { listCustomers, listLoans, listPendingKyc, updateKycStatus } from "../api/admin";
import { updateLoanStatus } from "../api/loans";
import { listAllTickets, respondToTicket } from "../api/tickets";
import { useAuth } from "../context/AuthContext";
import StatusTabBar from "../components/StatusTabBar";
import { useToasts, ToastStack } from "../components/Toast";
import LoanCard from "../components/LoanCard";
import LoanStatusActions from "../components/LoanStatusActions";
import TicketCard from "../components/TicketCard";
import Pagination from "../components/Pagination";
import DocumentViewButton from "../components/DocumentViewButton";
import KycStatusActions from "../components/KycStatusActions";
import "./Admin.css";

const KYC_BADGE = {
  PENDING: "bg-warning text-dark",
  VERIFIED: "bg-success",
  REJECTED: "bg-danger",
};

const LOAN_TAB_CONFIG = [
  { key: "ALL", label: "All", variant: "neutral" },
  { key: "PENDING", label: "Pending", variant: "warning" },
  { key: "APPROVED", label: "Approved", variant: "success" },
  { key: "REPAID", label: "Repaid", variant: "info" },
  { key: "REJECTED", label: "Rejected", variant: "danger" },
];

const LOANS_PAGE_SIZE = 12;
const TICKETS_PAGE_SIZE = 12;

const money = (amount) =>
  new Intl.NumberFormat("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(amount ?? 0);

const TICKET_STATUSES = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"];
const TICKET_PRIORITIES = ["Low", "Medium", "High"];

export default function Admin() {
  const { user, logout } = useAuth();
  const [view, setView] = useState("dashboard");
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const [customers, setCustomers] = useState([]);
  const [customersLoading, setCustomersLoading] = useState(true);
  const [customersError, setCustomersError] = useState("");

  const [loans, setLoans] = useState([]);
  const [loansLoading, setLoansLoading] = useState(true);
  const [loansError, setLoansError] = useState("");
  const [loanActionBusyKey, setLoanActionBusyKey] = useState(null);
  const [loanActionBusyRowId, setLoanActionBusyRowId] = useState(null);
  const [confirmingRejectId, setConfirmingRejectId] = useState(null);
  const [rejectReasonDraft, setRejectReasonDraft] = useState("");
  const [loanTab, setLoanTab] = useState("ALL");
  const [loanPage, setLoanPage] = useState(1);

  const [pendingKyc, setPendingKyc] = useState([]);
  const [kycLoading, setKycLoading] = useState(true);
  const [kycError, setKycError] = useState("");
  const [kycActionBusyKey, setKycActionBusyKey] = useState(null);
  const [kycActionBusyRowId, setKycActionBusyRowId] = useState(null);
  const [confirmingKycRejectId, setConfirmingKycRejectId] = useState(null);
  const [kycRejectReasonDraft, setKycRejectReasonDraft] = useState("");

  const { toasts, push: pushToast, dismiss: dismissToast } = useToasts();

  const [tickets, setTickets] = useState([]);
  const [ticketsLoading, setTicketsLoading] = useState(true);
  const [ticketsError, setTicketsError] = useState("");
  const [ticketStatusFilter, setTicketStatusFilter] = useState("");
  const [ticketPriorityFilter, setTicketPriorityFilter] = useState("");
  const [ticketPage, setTicketPage] = useState(1);
  const [selectedTicketId, setSelectedTicketId] = useState(null);
  const [respondForm, setRespondForm] = useState({ status: "IN_PROGRESS", adminResponse: "" });
  const [respondBusy, setRespondBusy] = useState(false);
  const [respondError, setRespondError] = useState("");

  const loadCustomers = () => {
    setCustomersLoading(true);
    setCustomersError("");
    listCustomers()
      .then((data) => setCustomers(data))
      .catch((err) => setCustomersError(err.response?.data?.message || "Unable to load customers."))
      .finally(() => setCustomersLoading(false));
  };

  const loadLoans = () => {
    setLoansLoading(true);
    setLoansError("");
    listLoans()
      .then((data) => setLoans(data))
      .catch((err) => setLoansError(err.response?.data?.message || "Unable to load loans."))
      .finally(() => setLoansLoading(false));
  };

  const loadPendingKyc = () => {
    setKycLoading(true);
    setKycError("");
    listPendingKyc()
      .then((data) => setPendingKyc(data))
      .catch((err) => setKycError(err.response?.data?.message || "Unable to load pending KYC reviews."))
      .finally(() => setKycLoading(false));
  };

  const loadTickets = () => {
    setTicketsLoading(true);
    setTicketsError("");
    const filters = {};
    if (ticketStatusFilter) filters.status = ticketStatusFilter;
    if (ticketPriorityFilter) filters.priority = ticketPriorityFilter;
    listAllTickets(filters)
      .then((data) => setTickets(data))
      .catch((err) => setTicketsError(err.response?.data?.message || "Unable to load tickets."))
      .finally(() => setTicketsLoading(false));
  };

  useEffect(() => {
    loadCustomers();
    loadLoans();
    loadPendingKyc();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user.email]);

  useEffect(() => {
    loadTickets();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user.email, ticketStatusFilter, ticketPriorityFilter]);

  const stats = useMemo(() => {
    const activeAccounts = customers.filter((c) => c.accountStatus === "ACTIVE").length;
    const totalBalance = customers.reduce((sum, c) => sum + (c.balance || 0), 0);
    const pendingLoans = loans.filter((l) => l.status === "PENDING").length;
    const openTickets = tickets.filter((t) => t.status === "OPEN" || t.status === "IN_PROGRESS").length;
    return {
      totalCustomers: customers.length,
      activeAccounts,
      totalBalance,
      pendingLoans,
      openTickets,
      pendingKyc: pendingKyc.length,
    };
  }, [customers, loans, tickets, pendingKyc]);

  const loanTabs = useMemo(() => {
    const pending = loans.filter((l) => l.status === "PENDING");
    const approved = loans.filter((l) => l.status === "APPROVED" && !l.paid);
    const repaid = loans.filter((l) => l.status === "APPROVED" && l.paid);
    const rejected = loans.filter((l) => l.status === "REJECTED");
    return {
      ALL: loans,
      PENDING: pending,
      APPROVED: approved,
      REPAID: repaid,
      REJECTED: rejected,
    };
  }, [loans]);

  const filteredLoans = loanTabs[loanTab] || loans;
  const loanTotalPages = Math.max(1, Math.ceil(filteredLoans.length / LOANS_PAGE_SIZE));
  const pagedLoans = filteredLoans.slice((loanPage - 1) * LOANS_PAGE_SIZE, loanPage * LOANS_PAGE_SIZE);

  useEffect(() => {
    if (loanPage > loanTotalPages) setLoanPage(loanTotalPages);
  }, [loanTotalPages, loanPage]);

  const selectTab = (key) => {
    setLoanTab(key);
    setLoanPage(1);
  };

  const ticketTotalPages = Math.max(1, Math.ceil(tickets.length / TICKETS_PAGE_SIZE));
  const pagedTickets = tickets.slice((ticketPage - 1) * TICKETS_PAGE_SIZE, ticketPage * TICKETS_PAGE_SIZE);

  useEffect(() => {
    if (ticketPage > ticketTotalPages) setTicketPage(ticketTotalPages);
  }, [ticketTotalPages, ticketPage]);

  const selectedTicket = tickets.find((t) => t.id === selectedTicketId) || null;

  const openTicket = (ticket) => {
    setSelectedTicketId(ticket.id);
    setRespondForm({
      status: ticket.status === "OPEN" ? "IN_PROGRESS" : ticket.status,
      adminResponse: ticket.adminResponse || "",
    });
    setRespondError("");
  };

  const submitResponse = async (e) => {
    e.preventDefault();
    if (!selectedTicket) return;
    setRespondError("");
    setRespondBusy(true);
    try {
      await respondToTicket(selectedTicket.id, {
        status: respondForm.status,
        adminResponse: respondForm.adminResponse,
      });
      loadTickets();
    } catch (err) {
      setRespondError(err.response?.data?.message || "Unable to update ticket.");
    } finally {
      setRespondBusy(false);
    }
  };

  const STATUS_LABEL = { APPROVED: "approved", REJECTED: "rejected", REPAID: "marked as repaid" };

  const changeLoanStatus = async (loan, newStatus, reason) => {
    const busyKey = `${loan.id}:${newStatus}`;
    setLoanActionBusyKey(busyKey);
    setLoanActionBusyRowId(loan.id);
    try {
      const updated = await updateLoanStatus(loan.id, newStatus, reason);
      setLoans((prev) => prev.map((l) => (l.id === updated.id ? updated : l)));
      pushToast({ type: "success", message: `Loan #${loan.id} (${loan.firstname} ${loan.lastname}) ${STATUS_LABEL[newStatus] || "updated"}.` });
      if (newStatus === "REJECTED") {
        setConfirmingRejectId(null);
        setRejectReasonDraft("");
      }
    } catch (err) {
      pushToast({ type: "error", message: err.response?.data?.message || `Unable to update loan #${loan.id}.` });
    } finally {
      setLoanActionBusyKey(null);
      setLoanActionBusyRowId(null);
    }
  };

  const requestReject = (loanId) => {
    setConfirmingRejectId(loanId);
    setRejectReasonDraft("");
  };

  const cancelReject = () => {
    setConfirmingRejectId(null);
    setRejectReasonDraft("");
  };

  const KYC_STATUS_LABEL = { VERIFIED: "verified", REJECTED: "rejected" };

  const changeKycStatus = async (customer, newStatus, reason) => {
    const busyKey = `${customer.id}:${newStatus}`;
    setKycActionBusyKey(busyKey);
    setKycActionBusyRowId(customer.id);
    try {
      await updateKycStatus(customer.id, newStatus, reason);
      setPendingKyc((prev) => prev.filter((c) => c.id !== customer.id));
      setCustomers((prev) => prev.map((c) => (c.id === customer.id ? { ...c, kycStatus: newStatus, kycRejectionReason: newStatus === "REJECTED" ? reason : null } : c)));
      pushToast({ type: "success", message: `${customer.firstname} ${customer.lastname}'s KYC ${KYC_STATUS_LABEL[newStatus] || "updated"}.` });
      if (newStatus === "REJECTED") {
        setConfirmingKycRejectId(null);
        setKycRejectReasonDraft("");
      }
    } catch (err) {
      pushToast({ type: "error", message: err.response?.data?.message || `Unable to update KYC for ${customer.firstname} ${customer.lastname}.` });
    } finally {
      setKycActionBusyKey(null);
      setKycActionBusyRowId(null);
    }
  };

  const requestKycReject = (customerId) => {
    setConfirmingKycRejectId(customerId);
    setKycRejectReasonDraft("");
  };

  const cancelKycReject = () => {
    setConfirmingKycRejectId(null);
    setKycRejectReasonDraft("");
  };

  const goTo = (target) => {
    setView(target);
    setSidebarOpen(false);
  };

  return (
    <div className="admin-page">
      <nav className="top-navbar">
        <div className="d-flex align-items-center gap-3">
          <button
            className="btn btn-sm btn-outline-light d-lg-none"
            onClick={() => setSidebarOpen((v) => !v)}
          >
            <i className="fas fa-bars"></i>
          </button>
          <a className="brand" href="#" onClick={(e) => { e.preventDefault(); goTo("dashboard"); }}>
            <i className="fas fa-university"></i> SysBank <span className="badge bg-warning text-dark">Admin</span>
          </a>
        </div>
        <div className="d-flex align-items-center gap-3">
          <span className="text-white-50 d-none d-md-inline">{user.fullName}</span>
          <button className="logout-btn" onClick={logout}>
            <i className="fas fa-sign-out-alt"></i> Logout
          </button>
        </div>
      </nav>

      <aside className={`sidebar ${sidebarOpen ? "show" : ""}`}>
        <div className="sidebar-header">
          <h3 className="h6 mb-0"><i className="fas fa-university me-2"></i>Sys Bank</h3>
          <p>Admin Panel</p>
        </div>
        <ul className="sidebar-menu">
          <li>
            <button className={view === "dashboard" ? "active" : ""} onClick={() => goTo("dashboard")}>
              <i className="fas fa-tachometer-alt"></i> Dashboard
            </button>
          </li>
          <li>
            <button className={view === "customers" ? "active" : ""} onClick={() => goTo("customers")}>
              <i className="fas fa-users"></i> Customers
            </button>
          </li>
          <li>
            <button className={view === "kyc" ? "active" : ""} onClick={() => goTo("kyc")}>
              <i className="fas fa-id-card"></i> KYC Verification
              {stats.pendingKyc > 0 && <span className="badge bg-warning text-dark ms-auto">{stats.pendingKyc}</span>}
            </button>
          </li>
          <li>
            <button className={view === "loans" ? "active" : ""} onClick={() => goTo("loans")}>
              <i className="fas fa-hand-holding-usd"></i> Loan Applications
              {stats.pendingLoans > 0 && <span className="badge bg-warning text-dark ms-auto">{stats.pendingLoans}</span>}
            </button>
          </li>
          <li>
            <button className={view === "tickets" ? "active" : ""} onClick={() => goTo("tickets")}>
              <i className="fas fa-life-ring"></i> Ticket Resolver
              {stats.openTickets > 0 && <span className="badge bg-danger ms-auto">{stats.openTickets}</span>}
            </button>
          </li>
          <li className="logout-item">
            <button onClick={logout}>
              <i className="fas fa-sign-out-alt"></i> Logout
            </button>
          </li>
        </ul>
      </aside>

      <main className="main-content">
        {view === "dashboard" && (
          <>
            <h4 className="mb-4">Welcome back, {user.fullName}</h4>
            <div className="row g-3 mb-4">
              <div className="col-md-6 col-lg-3">
                <div className="stat-card">
                  <div className="stat-icon"><i className="fas fa-users"></i></div>
                  <div>
                    <h2>{stats.totalCustomers}</h2>
                    <p>Total Customers</p>
                  </div>
                </div>
              </div>
              <div className="col-md-6 col-lg-3">
                <div className="stat-card">
                  <div className="stat-icon"><i className="fas fa-wallet"></i></div>
                  <div>
                    <h2>{stats.activeAccounts}</h2>
                    <p>Active Accounts</p>
                  </div>
                </div>
              </div>
              <div className="col-md-6 col-lg-3">
                <div className="stat-card">
                  <div className="stat-icon"><i className="fas fa-hand-holding-usd"></i></div>
                  <div>
                    <h2>{stats.pendingLoans}</h2>
                    <p>Pending Loan Approvals</p>
                  </div>
                </div>
              </div>
              <div className="col-md-6 col-lg-3">
                <div className="stat-card">
                  <div className="stat-icon"><i className="fas fa-piggy-bank"></i></div>
                  <div>
                    <h2>₹{money(stats.totalBalance)}</h2>
                    <p>Total Balance Managed</p>
                  </div>
                </div>
              </div>
              <div className="col-md-6 col-lg-3">
                <div className="stat-card">
                  <div className="stat-icon"><i className="fas fa-life-ring"></i></div>
                  <div>
                    <h2>{stats.openTickets}</h2>
                    <p>Open Support Tickets</p>
                  </div>
                </div>
              </div>
              <div className="col-md-6 col-lg-3">
                <div className="stat-card">
                  <div className="stat-icon"><i className="fas fa-id-card"></i></div>
                  <div>
                    <h2>{stats.pendingKyc}</h2>
                    <p>Pending KYC Reviews</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="panel mb-4">
              <div className="panel-header">
                <h5><i className="fas fa-id-card me-2"></i>KYC Reviews Pending</h5>
                <button className="btn btn-sm btn-outline-primary" onClick={() => goTo("kyc")}>View All</button>
              </div>
              <div className="p-3">
                {kycLoading && <p className="text-center text-muted mb-0 py-2">Loading...</p>}
                {!kycLoading && pendingKyc.length === 0 && (
                  <p className="text-center text-muted mb-0 py-2">No KYC reviews pending</p>
                )}
                {!kycLoading && pendingKyc.slice(0, 5).map((customer) => (
                  <div className="d-flex justify-content-between align-items-center py-2 border-bottom flex-wrap gap-2" key={customer.id}>
                    <div>
                      <strong>{customer.firstname} {customer.lastname}</strong>
                      <div className="text-muted small">{customer.email} &bull; {customer.accountType}</div>
                    </div>
                    <KycStatusActions
                      status={customer.kycStatus}
                      busyAction={kycActionBusyRowId === customer.id ? kycActionBusyKey.split(":")[1] : null}
                      confirmingReject={confirmingKycRejectId === customer.id}
                      rejectReason={confirmingKycRejectId === customer.id ? kycRejectReasonDraft : ""}
                      onRejectReasonChange={setKycRejectReasonDraft}
                      onApprove={() => changeKycStatus(customer, "VERIFIED")}
                      onRequestReject={() => requestKycReject(customer.id)}
                      onConfirmReject={() => changeKycStatus(customer, "REJECTED", kycRejectReasonDraft)}
                      onCancelReject={cancelKycReject}
                    />
                  </div>
                ))}
              </div>
            </div>

            <div className="panel">
              <div className="panel-header">
                <h5><i className="fas fa-hand-holding-usd me-2"></i>Loans Awaiting Review</h5>
                <button className="btn btn-sm btn-outline-primary" onClick={() => goTo("loans")}>View All</button>
              </div>
              <div className="p-3">
                {loansLoading && <p className="text-center text-muted mb-0 py-2">Loading...</p>}
                {!loansLoading && loans.filter((l) => l.status === "PENDING").length === 0 && (
                  <p className="text-center text-muted mb-0 py-2">No loans awaiting review</p>
                )}
                {!loansLoading && loans.filter((l) => l.status === "PENDING").slice(0, 5).map((loan) => (
                  <div className="d-flex justify-content-between align-items-center py-2 border-bottom flex-wrap gap-2" key={loan.id}>
                    <div>
                      <strong>{loan.firstname} {loan.lastname}</strong>
                      <div className="text-muted small">{loan.loanType} &bull; ₹{money(loan.loanAmount)}</div>
                    </div>
                    <LoanStatusActions
                      status={loan.status}
                      paid={loan.paid}
                      busyAction={loanActionBusyRowId === loan.id ? loanActionBusyKey.split(":")[1] : null}
                      confirmingReject={confirmingRejectId === loan.id}
                      rejectReason={confirmingRejectId === loan.id ? rejectReasonDraft : ""}
                      onRejectReasonChange={setRejectReasonDraft}
                      onApprove={() => changeLoanStatus(loan, "APPROVED")}
                      onRequestReject={() => requestReject(loan.id)}
                      onConfirmReject={() => changeLoanStatus(loan, "REJECTED", rejectReasonDraft)}
                      onCancelReject={cancelReject}
                    />
                  </div>
                ))}
              </div>
            </div>
          </>
        )}

        {view === "customers" && (
          <div className="panel">
            <div className="panel-header">
              <h5>All Customers ({customers.length})</h5>
              <button className="btn btn-sm btn-outline-secondary" onClick={loadCustomers}>
                <i className="fas fa-sync-alt"></i>
              </button>
            </div>
            {customersLoading && <p className="text-center py-4">Loading customers...</p>}
            {customersError && <div className="alert alert-danger m-3">{customersError}</div>}

            {!customersLoading && !customersError && (
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Phone</th>
                      <th>Type</th>
                      <th>Status</th>
                      <th>KYC</th>
                      <th className="text-end">Balance</th>
                    </tr>
                  </thead>
                  <tbody>
                    {customers.length === 0 ? (
                      <tr><td colSpan={7} className="text-center text-muted py-4">No customers found</td></tr>
                    ) : (
                      customers.map((c) => (
                        <tr key={c.id}>
                          <td>{c.firstname} {c.lastname}</td>
                          <td>{c.email}</td>
                          <td>{c.phoneNo || "-"}</td>
                          <td className="text-capitalize">{c.accountType}</td>
                          <td>
                            <span className={`badge ${c.accountStatus === "ACTIVE" ? "bg-success" : "bg-secondary"}`}>
                              {c.accountStatus}
                            </span>
                          </td>
                          <td>
                            <span className={`badge ${KYC_BADGE[c.kycStatus] || KYC_BADGE.PENDING}`}>{c.kycStatus}</span>
                          </td>
                          <td className="text-end">₹{money(c.balance)}</td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {view === "kyc" && (
          <div className="panel">
            <div className="panel-header">
              <h5>KYC Verification ({pendingKyc.length} pending)</h5>
              <button className="btn btn-sm btn-outline-secondary" onClick={loadPendingKyc}>
                <i className="fas fa-sync-alt"></i>
              </button>
            </div>
            <div className="p-3">
              {kycLoading && <p className="text-center py-4">Loading pending KYC reviews...</p>}
              {kycError && <div className="alert alert-danger">{kycError}</div>}

              {!kycLoading && !kycError && (
                pendingKyc.length === 0 ? (
                  <div className="text-center text-muted py-4">No KYC reviews pending - all caught up.</div>
                ) : (
                  <div className="row g-3">
                    {pendingKyc.map((customer) => (
                      <div className="col-md-6" key={customer.id}>
                        <div className="card h-100">
                          <div className="card-body">
                            <div className="d-flex justify-content-between align-items-start mb-2">
                              <div>
                                <h6 className="mb-1">{customer.firstname} {customer.lastname}</h6>
                                <div className="text-muted small">{customer.email}</div>
                              </div>
                              <span className={`badge ${KYC_BADGE[customer.kycStatus] || KYC_BADGE.PENDING}`}>{customer.kycStatus}</span>
                            </div>
                            <div className="text-muted small mb-3">
                              {customer.accountType} account &bull; Applied {customer.createdAt ? new Date(customer.createdAt).toLocaleDateString("en-IN") : "-"}
                            </div>

                            <div className="d-flex gap-2 mb-3 flex-wrap">
                              <DocumentViewButton url={customer.aadhaarUrl} label="View Aadhaar" />
                              <DocumentViewButton url={customer.panUrl} label="View PAN" />
                              {customer.jointAadhaarUrl && <DocumentViewButton url={customer.jointAadhaarUrl} label="Joint Aadhaar" />}
                              {customer.jointPanUrl && <DocumentViewButton url={customer.jointPanUrl} label="Joint PAN" />}
                            </div>

                            <KycStatusActions
                              status={customer.kycStatus}
                              busyAction={kycActionBusyRowId === customer.id ? kycActionBusyKey.split(":")[1] : null}
                              confirmingReject={confirmingKycRejectId === customer.id}
                              rejectReason={confirmingKycRejectId === customer.id ? kycRejectReasonDraft : ""}
                              onRejectReasonChange={setKycRejectReasonDraft}
                              onApprove={() => changeKycStatus(customer, "VERIFIED")}
                              onRequestReject={() => requestKycReject(customer.id)}
                              onConfirmReject={() => changeKycStatus(customer, "REJECTED", kycRejectReasonDraft)}
                              onCancelReject={cancelKycReject}
                              hideBadge
                            />
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )
              )}
            </div>
          </div>
        )}

        {view === "loans" && (
          <div className="panel">
            <div className="panel-header">
              <h5>Loan Applications ({loans.length})</h5>
              <button className="btn btn-sm btn-outline-secondary" onClick={loadLoans}>
                <i className="fas fa-sync-alt"></i>
              </button>
            </div>
            <StatusTabBar
              tabs={LOAN_TAB_CONFIG.map((tab) => ({ ...tab, count: loanTabs[tab.key].length }))}
              active={loanTab}
              onChange={selectTab}
            />
            <div className="p-3">
              {loansLoading && <p className="text-center py-4">Loading loans...</p>}
              {loansError && <div className="alert alert-danger">{loansError}</div>}

              {!loansLoading && !loansError && (
                <>
                  {filteredLoans.length === 0 ? (
                    <div className="text-center text-muted py-4">No loan applications in this view</div>
                  ) : (
                    <div className="loan-grid">
                      {pagedLoans.map((loan) => (
                        <LoanCard
                          key={loan.id}
                          loan={loan}
                          busyAction={loanActionBusyRowId === loan.id ? loanActionBusyKey.split(":")[1] : null}
                          confirmingReject={confirmingRejectId === loan.id}
                          rejectReason={confirmingRejectId === loan.id ? rejectReasonDraft : ""}
                          onRejectReasonChange={setRejectReasonDraft}
                          onApprove={() => changeLoanStatus(loan, "APPROVED")}
                          onRequestReject={() => requestReject(loan.id)}
                          onConfirmReject={() => changeLoanStatus(loan, "REJECTED", rejectReasonDraft)}
                          onCancelReject={cancelReject}
                          onMarkPaid={() => changeLoanStatus(loan, "REPAID")}
                        />
                      ))}
                    </div>
                  )}

                  <Pagination
                    page={loanPage}
                    totalPages={loanTotalPages}
                    totalItems={filteredLoans.length}
                    pageSize={LOANS_PAGE_SIZE}
                    onPageChange={setLoanPage}
                  />
                </>
              )}
            </div>
          </div>
        )}

        {view === "tickets" && (
          <div className="row g-3">
            <div className={selectedTicket ? "col-lg-7" : "col-12"}>
              <div className="panel">
                <div className="panel-header flex-wrap gap-2">
                  <h5>Ticket Queue ({tickets.length})</h5>
                  <div className="d-flex gap-2">
                    <select
                      className="form-select form-select-sm"
                      value={ticketStatusFilter}
                      onChange={(e) => { setTicketStatusFilter(e.target.value); setTicketPage(1); }}
                    >
                      <option value="">All statuses</option>
                      {TICKET_STATUSES.map((s) => <option key={s} value={s}>{s.replace("_", " ")}</option>)}
                    </select>
                    <select
                      className="form-select form-select-sm"
                      value={ticketPriorityFilter}
                      onChange={(e) => { setTicketPriorityFilter(e.target.value); setTicketPage(1); }}
                    >
                      <option value="">All priorities</option>
                      {TICKET_PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
                    </select>
                    <button className="btn btn-sm btn-outline-secondary" onClick={loadTickets}>
                      <i className="fas fa-sync-alt"></i>
                    </button>
                  </div>
                </div>
                {ticketsLoading && <p className="text-center py-4">Loading tickets...</p>}
                {ticketsError && <div className="alert alert-danger m-3">{ticketsError}</div>}

                {!ticketsLoading && !ticketsError && (
                  <div className="p-3">
                    {tickets.length === 0 ? (
                      <div className="text-center text-muted py-4">No tickets match this filter</div>
                    ) : (
                      <div className="ticket-grid">
                        {pagedTickets.map((t) => (
                          <TicketCard
                            key={t.id}
                            ticket={t}
                            isOpen={selectedTicketId === t.id}
                            onView={() => openTicket(t)}
                          />
                        ))}
                      </div>
                    )}

                    <Pagination
                      page={ticketPage}
                      totalPages={ticketTotalPages}
                      totalItems={tickets.length}
                      pageSize={TICKETS_PAGE_SIZE}
                      onPageChange={setTicketPage}
                    />
                  </div>
                )}
              </div>
            </div>

            {selectedTicket && (
              <div className="col-lg-5">
                <div className="panel">
                  <div className="panel-header">
                    <h5>Ticket #{selectedTicket.id}</h5>
                    <button className="btn btn-sm btn-outline-secondary" onClick={() => setSelectedTicketId(null)}>
                      <i className="fas fa-times"></i>
                    </button>
                  </div>
                  <div className="p-3">
                    <h6>{selectedTicket.subject}</h6>
                    <p className="text-muted small mb-2">
                      From {selectedTicket.userFullName} ({selectedTicket.userEmail}) &bull; {selectedTicket.category} &bull; {selectedTicket.priority} priority
                    </p>
                    <p>{selectedTicket.description}</p>
                    <hr />

                    {respondError && <div className="alert alert-danger">{respondError}</div>}

                    <form onSubmit={submitResponse}>
                      <div className="mb-2">
                        <label className="form-label">Status</label>
                        <select
                          className="form-select"
                          value={respondForm.status}
                          onChange={(e) => setRespondForm((f) => ({ ...f, status: e.target.value }))}
                        >
                          {TICKET_STATUSES.map((s) => <option key={s} value={s}>{s.replace("_", " ")}</option>)}
                        </select>
                      </div>
                      <div className="mb-3">
                        <label className="form-label">Response</label>
                        <textarea
                          className="form-control"
                          rows="4"
                          value={respondForm.adminResponse}
                          onChange={(e) => setRespondForm((f) => ({ ...f, adminResponse: e.target.value }))}
                          placeholder="Write a reply to the customer..."
                        />
                      </div>
                      <button type="submit" className="btn btn-primary w-100" disabled={respondBusy}>
                        {respondBusy ? "Saving..." : "Save & Notify"}
                      </button>
                    </form>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </main>

      <ToastStack toasts={toasts} onDismiss={dismissToast} />
    </div>
  );
}
