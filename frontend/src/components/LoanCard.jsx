import LoanStatusActions, { LoanStatusBadge } from "./LoanStatusActions";
import "./LoanCard.css";

const money = (amount) =>
  new Intl.NumberFormat("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(amount ?? 0);

export default function LoanCard({
  loan,
  busyAction,
  confirmingReject,
  rejectReason,
  onRejectReasonChange,
  onApprove,
  onRequestReject,
  onConfirmReject,
  onCancelReject,
  onMarkPaid,
}) {
  return (
    <div className="loan-card">
      <div className="loan-card-header">
        <span className="loan-type-tag">{loan.loanType} Loan</span>
        <LoanStatusBadge status={loan.status} paid={loan.paid} />
      </div>

      <h6 className="loan-card-name">{loan.firstname} {loan.lastname}</h6>
      <div className="loan-card-meta">#{loan.id} &bull; {loan.email}</div>

      <div className="loan-card-amount-row">
        <span className="loan-card-amount">₹{money(loan.loanAmount)}</span>
        <span className="loan-card-tenure">{loan.loanYears} yr(s)</span>
      </div>

      <div className="loan-card-footer">
        <LoanStatusActions
          status={loan.status}
          paid={loan.paid}
          busyAction={busyAction}
          confirmingReject={confirmingReject}
          rejectReason={rejectReason}
          onRejectReasonChange={onRejectReasonChange}
          onApprove={onApprove}
          onRequestReject={onRequestReject}
          onConfirmReject={onConfirmReject}
          onCancelReject={onCancelReject}
          onMarkPaid={onMarkPaid}
          hideBadge
        />
      </div>
    </div>
  );
}
