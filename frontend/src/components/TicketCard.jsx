import "./TicketCard.css";

const STATUS_BADGE_CLASS = {
  OPEN: "ticket-status-badge-open",
  IN_PROGRESS: "ticket-status-badge-in-progress",
  RESOLVED: "ticket-status-badge-resolved",
  CLOSED: "ticket-status-badge-closed",
};

export default function TicketCard({ ticket, isOpen, onView }) {
  return (
    <div className="ticket-card">
      <div className="ticket-card-header">
        <span className={`ticket-status-badge ${STATUS_BADGE_CLASS[ticket.status] || "ticket-status-badge-closed"}`}>
          {ticket.status.replace("_", " ")}
        </span>
      </div>

      <h6 className="ticket-card-title">#{ticket.id} {ticket.subject}</h6>
      <div className="ticket-card-meta">{ticket.userFullName} &bull; {ticket.userEmail}</div>

      <div className="ticket-card-tags">
        <span className="meta-tag">{ticket.category}</span>
        <span className={`ticket-priority ${ticket.priority === "High" ? "ticket-priority-high" : ""}`}>
          {ticket.priority} priority
        </span>
      </div>

      <div className="ticket-card-footer">
        <button type="button" className={`ticket-view-btn ${isOpen ? "active" : ""}`} onClick={onView}>
          <i className="fas fa-comment-dots"></i> View / Respond
        </button>
      </div>
    </div>
  );
}
