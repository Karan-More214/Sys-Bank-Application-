import "../components/LoanStatusActions.css";

const BADGE_CLASS = {
  PENDING: "loan-status-badge-pending",
  VERIFIED: "loan-status-badge-approved",
  REJECTED: "loan-status-badge-rejected",
};

const Spinner = () => <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>;

export function KycStatusBadge({ status }) {
  return (
    <span className={`loan-status-badge ${BADGE_CLASS[status] || BADGE_CLASS.PENDING}`}>
      {status}
    </span>
  );
}

/**
 * Same interaction shape as LoanStatusActions (Approve / Reject with an optional-reason
 * confirm step), applied to KYC review instead of loan review - kept as a separate
 * component since the two badge vocabularies (PENDING/VERIFIED/REJECTED vs
 * PENDING/APPROVED/REJECTED/REPAID) and action sets genuinely differ, not just naming.
 */
export default function KycStatusActions({
  status,
  busyAction,
  confirmingReject,
  rejectReason,
  onRejectReasonChange,
  onApprove,
  onRequestReject,
  onConfirmReject,
  onCancelReject,
  hideBadge,
}) {
  const rowBusy = busyAction !== null;

  if (confirmingReject) {
    return (
      <div className="loan-status-actions loan-status-actions-confirming">
        {!hideBadge && <KycStatusBadge status="PENDING" />}
        <span className="loan-status-note">Reject this KYC submission?</span>
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
      {!hideBadge && <KycStatusBadge status={status} />}

      {status === "PENDING" && (
        <>
          <button
            type="button"
            className="loan-status-btn loan-status-btn-approve"
            disabled={rowBusy}
            onClick={onApprove}
          >
            {busyAction === "VERIFIED" ? <Spinner /> : "Approve"}
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
    </div>
  );
}
