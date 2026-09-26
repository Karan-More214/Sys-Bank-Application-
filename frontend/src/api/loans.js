import apiClient from "./client";

export async function getLoanHistory() {
  const response = await apiClient.get("/api/loans/history");
  return response.data;
}

export async function applyForLoan(payload) {
  const response = await apiClient.post("/api/loans/apply", payload);
  return response.data;
}

export async function updateLoanStatus(loanId, newStatus, reason) {
  const response = await apiClient.patch(`/api/loans/${loanId}/status`, { newStatus, reason });
  return response.data;
}
