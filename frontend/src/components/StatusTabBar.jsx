import "./StatusTabBar.css";

const VARIANT_BADGE_CLASS = {
  neutral: "status-tab-badge-neutral",
  warning: "status-tab-badge-warning",
  success: "status-tab-badge-success",
  info: "status-tab-badge-info",
  danger: "status-tab-badge-danger",
};

/**
 * A segmented pill filter control (Stripe/Razorpay dashboard style) for switching
 * between a small set of statuses, each with a live count. Purely presentational -
 * the caller owns the active tab state and the filtering logic.
 *
 * tabs: [{ key, label, count, variant }], variant one of
 * "neutral" | "warning" | "success" | "info" | "danger" (falls back to neutral).
 */
export default function StatusTabBar({ tabs, active, onChange }) {
  return (
    <div className="status-tab-bar">
      <div className="status-tab-bar-track">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            type="button"
            className={`status-tab ${active === tab.key ? "active" : ""}`}
            onClick={() => onChange(tab.key)}
          >
            <span className="status-tab-label">{tab.label}</span>
            <span className={`status-tab-badge ${VARIANT_BADGE_CLASS[tab.variant] || VARIANT_BADGE_CLASS.neutral}`}>
              {tab.count}
            </span>
          </button>
        ))}
      </div>
    </div>
  );
}
