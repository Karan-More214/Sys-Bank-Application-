import { useState } from "react";
import "./TransferStatusActions.css";

const BADGE_CLASS = {
  COMPLETED: "transfer-status-badge-completed",
  FLAGGED: "transfer-status-badge-flagged",
  REVERSED: "transfer-status-badge-reversed",
};

const Spinner = () => <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>;

export function TransferStatusBadge({ status }) {
  return (
    <span className={`transfer-status-badge ${BADGE_CLASS[status] || "transfer-status-badge-reversed"}`}>
      {status}
    </span>
  );
}

/**
 * Renders a transfer's action buttons for its current status - Flagged
 * (Approve + Reverse), Completed (Reverse only), or nothing for Reversed
 * (terminal). Reverse requires a typed reason before it can be confirmed,
 * matching the reject-confirmation pattern from LoanStatusActions but with a
 * text reason instead of a plain yes/no.
 */
export default function TransferStatusActions({ status, busyAction, onApprove, onReverse, hideBadge }) {
  const [reversing, setReversing] = useState(false);
  const [reason, setReason] = useState("");
  const rowBusy = busyAction !== null;

  const submitReverse = () => {
    if (!reason.trim()) return;
    onReverse(reason.trim());
  };

  if (reversing) {
    return (
      <div className="transfer-reverse-form">
        <textarea
          rows={2}
          placeholder="Reason for reversing this transfer..."
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          disabled={busyAction === "REVERSED"}
        />
        <div className="transfer-reverse-actions">
          <button
            type="button"
            className="transfer-status-btn transfer-status-btn-confirm"
            disabled={busyAction === "REVERSED" || !reason.trim()}
            onClick={submitReverse}
          >
            {busyAction === "REVERSED" ? <Spinner /> : "Confirm Reverse"}
          </button>
          <button
            type="button"
            className="transfer-status-btn transfer-status-btn-cancel"
            disabled={busyAction === "REVERSED"}
            onClick={() => { setReversing(false); setReason(""); }}
          >
            Cancel
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="transfer-status-actions">
      {!hideBadge && <TransferStatusBadge status={status} />}

      {status === "FLAGGED" && (
        <button
          type="button"
          className="transfer-status-btn transfer-status-btn-approve"
          disabled={rowBusy}
          onClick={onApprove}
        >
          {busyAction === "COMPLETED" ? <Spinner /> : "Approve"}
        </button>
      )}

      {(status === "FLAGGED" || status === "COMPLETED") && (
        <button
          type="button"
          className="transfer-status-btn transfer-status-btn-reverse"
          disabled={rowBusy}
          onClick={() => setReversing(true)}
        >
          Reverse
        </button>
      )}
    </div>
  );
}
