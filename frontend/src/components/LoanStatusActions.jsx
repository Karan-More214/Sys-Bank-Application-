import "./LoanStatusActions.css";

const BADGE_CLASS = {
  PENDING: "loan-status-badge-pending",
  APPROVED: "loan-status-badge-approved",
  REJECTED: "loan-status-badge-rejected",
  REPAID: "loan-status-badge-repaid",
};

const Spinner = () => <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>;

export function loanEffectiveStatus(status, paid) {
  return status === "APPROVED" && paid ? "REPAID" : status;
}

/**
 * The single place a loan status badge is ever rendered. Shares its CSS class
 * (.loan-status-badge) with the buttons in LoanStatusActions below, so a badge
 * used standalone (e.g. top-right of a card) is still pixel-identical to one
 * rendered inline with buttons. There is deliberately no second badge-drawing
 * code path anywhere - that duplication is what caused Approved+paid loans to
 * show two stacked badges before.
 */
export function LoanStatusBadge({ status, paid }) {
  const effectiveStatus = loanEffectiveStatus(status, paid);
  return (
    <span className={`loan-status-badge ${BADGE_CLASS[effectiveStatus] || BADGE_CLASS.PENDING}`}>
      {effectiveStatus === "REPAID" ? "Repaid" : effectiveStatus}
    </span>
  );
}

/**
 * Renders a loan's action buttons for its current status - Pending (Approve +
 * Reject), Approved-unpaid (Mark Paid), or nothing for terminal states (Repaid,
 * Rejected). Includes the badge inline by default (list/row usage); pass
 * hideBadge to omit it when the caller is positioning LoanStatusBadge itself
 * elsewhere (e.g. a card header) - the badge logic itself is never duplicated.
 *
 * busyAction: null | "APPROVED" | "REJECTED" | "REPAID" - which action (if
 * any) is in flight for *this* loan; any non-null value disables every button
 * here, and the matching button shows a spinner instead of its label.
 */
export default function LoanStatusActions({
  status,
  paid,
  busyAction,
  confirmingReject,
  rejectReason,
  onRejectReasonChange,
  onApprove,
  onRequestReject,
  onConfirmReject,
  onCancelReject,
  onMarkPaid,
  hideBadge,
}) {
  const effectiveStatus = loanEffectiveStatus(status, paid);
  const rowBusy = busyAction !== null;

  if (confirmingReject) {
    return (
      <div className="loan-status-actions loan-status-actions-confirming">
        {!hideBadge && <LoanStatusBadge status="PENDING" paid={false} />}
        <span className="loan-status-note">Reject this loan?</span>
        <input
          type="text"
          className="loan-status-reason-input"
          placeholder="Reason (optional)"
          value={rejectReason || ""}
          onChange={(e) => onRejectReasonChange && onRejectReasonChange(e.target.value)}
          disabled={busyAction === "REJECTED"}
        />
        <button
          type="button"
          className="loan-status-btn loan-status-btn-confirm"
          disabled={busyAction === "REJECTED"}
          onClick={onConfirmReject}
        >
          {busyAction === "REJECTED" ? <Spinner /> : "Yes, Reject"}
        </button>
        <button
          type="button"
          className="loan-status-btn loan-status-btn-cancel"
          disabled={busyAction === "REJECTED"}
          onClick={onCancelReject}
        >
          Cancel
        </button>
      </div>
    );
  }

  return (
    <div className="loan-status-actions">
      {!hideBadge && <LoanStatusBadge status={status} paid={paid} />}

      {effectiveStatus === "PENDING" && (
        <>
          <button
            type="button"
            className="loan-status-btn loan-status-btn-approve"
            disabled={rowBusy}
            onClick={onApprove}
          >
            {busyAction === "APPROVED" ? <Spinner /> : "Approve"}
          </button>
          <button
            type="button"
            className="loan-status-btn loan-status-btn-reject"
            disabled={rowBusy}
            onClick={onRequestReject}
          >
            Reject
          </button>
        </>
      )}

      {effectiveStatus === "APPROVED" && (
        <button
          type="button"
          className="loan-status-btn loan-status-btn-markpaid"
          disabled={rowBusy}
          onClick={onMarkPaid}
        >
          {busyAction === "REPAID" ? <Spinner /> : "Mark Paid"}
        </button>
      )}
    </div>
  );
}
