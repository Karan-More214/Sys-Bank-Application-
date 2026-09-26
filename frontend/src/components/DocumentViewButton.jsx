import { useState } from "react";
import { fetchDocumentBlobUrl } from "../api/documents";

export default function DocumentViewButton({ url, label, className }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  if (!url) {
    return <span className="text-muted small">{label} not uploaded</span>;
  }

  const handleView = async () => {
    setError("");
    setLoading(true);
    try {
      const blobUrl = await fetchDocumentBlobUrl(url);
      window.open(blobUrl, "_blank", "noopener,noreferrer");
    } catch (err) {
      setError(err.response?.status === 403 ? "Not authorized to view this document." : "Unable to load document.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <button type="button" className={className || "btn btn-sm btn-outline-secondary"} onClick={handleView} disabled={loading}>
        <i className="fas fa-file-alt me-1"></i>{loading ? "Loading..." : label}
      </button>
      {error && <div className="text-danger small mt-1">{error}</div>}
    </>
  );
}
