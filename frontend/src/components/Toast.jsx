import { useCallback, useRef, useState } from "react";
import "./Toast.css";

let nextId = 1;

/**
 * Minimal toast notification system: a hook that owns the toast queue, and a
 * stack component that renders it. push({ type: "success" | "error", message })
 * from anywhere that has the hook's return value; toasts auto-dismiss after 4s.
 */
export function useToasts() {
  const [toasts, setToasts] = useState([]);
  const timers = useRef({});

  const dismiss = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
    clearTimeout(timers.current[id]);
    delete timers.current[id];
  }, []);

  const push = useCallback((toast) => {
    const id = nextId++;
    setToasts((prev) => [...prev, { id, type: toast.type || "success", message: toast.message }]);
    timers.current[id] = setTimeout(() => dismiss(id), 4000);
  }, [dismiss]);

  return { toasts, push, dismiss };
}

export function ToastStack({ toasts, onDismiss }) {
  if (!toasts.length) return null;

  return (
    <div className="toast-stack">
      {toasts.map((t) => (
        <div key={t.id} className={`toast-item toast-${t.type}`} role="alert">
          <i className={`fas ${t.type === "error" ? "fa-exclamation-circle" : "fa-check-circle"} me-2`}></i>
          <span className="toast-message">{t.message}</span>
          <button className="toast-close" onClick={() => onDismiss(t.id)} aria-label="Dismiss">
            <i className="fas fa-times"></i>
          </button>
        </div>
      ))}
    </div>
  );
}
