import apiClient from "./client";

// Aadhaar/PAN endpoints require a valid JWT belonging to the account owner or an admin
// (see BankController.canAccessDocuments), so a plain <a href>/<img src> can't reach them -
// the browser never attaches the Authorization header to those. Fetch through apiClient
// instead and hand the caller a blob: URL it can open in a new tab. Used by both the
// customer's own Profile page and the admin KYC review page - ownership/role is enforced
// server-side, not by which page happens to call this.
export async function fetchDocumentBlobUrl(path) {
  const response = await apiClient.get(path, { responseType: "blob" });
  return URL.createObjectURL(response.data);
}
