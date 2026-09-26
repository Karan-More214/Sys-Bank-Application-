import "./Pagination.css";

/**
 * Minimal numbered-page pagination (prev / "Page X of Y" / next + an item
 * range label) rather than a numbered button list or "Load more" - scales to
 * any item count with no truncation/ellipsis logic needed, and resetting to
 * page 1 on a filter change is a single setState the caller already owns.
 * Deliberately generic (page/totalPages/totalItems/pageSize/onPageChange) so
 * Customers or Ticket Resolver can reuse it as-is later.
 */
export default function Pagination({ page, totalPages, totalItems, pageSize, onPageChange }) {
  if (totalPages <= 1) return null;

  const startItem = (page - 1) * pageSize + 1;
  const endItem = Math.min(page * pageSize, totalItems);

  return (
    <div className="pagination-bar">
      <span className="pagination-info">
        Showing {startItem}-{endItem} of {totalItems}
      </span>
      <div className="pagination-controls">
        <button
          type="button"
          className="pagination-btn"
          disabled={page <= 1}
          onClick={() => onPageChange(page - 1)}
          aria-label="Previous page"
        >
          <i className="fas fa-chevron-left"></i>
        </button>
        <span className="pagination-page">Page {page} of {totalPages}</span>
        <button
          type="button"
          className="pagination-btn"
          disabled={page >= totalPages}
          onClick={() => onPageChange(page + 1)}
          aria-label="Next page"
        >
          <i className="fas fa-chevron-right"></i>
        </button>
      </div>
    </div>
  );
}
