import apiClient from "./client";

export async function initiateTransfer(payload) {
  const response = await apiClient.post("/api/transfers", payload);
  return response.data;
}

export async function getMyTransfers() {
  const response = await apiClient.get("/api/transfers/mine");
  return response.data;
}

export async function listAllTransfers(status) {
  const params = {};
  if (status) params.status = status;
  const response = await apiClient.get("/api/transfers", { params });
  return response.data;
}

export async function updateTransferStatus(id, newStatus, reason) {
  const response = await apiClient.patch(`/api/transfers/${id}/status`, { newStatus, reason });
  return response.data;
}
