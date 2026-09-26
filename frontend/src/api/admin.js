import apiClient from "./client";

export async function listCustomers() {
  const response = await apiClient.get("/api/admin/customers");
  return response.data;
}

export async function listLoans() {
  const response = await apiClient.get("/api/admin/loans");
  return response.data;
}

export async function deleteCustomer(id) {
  const response = await apiClient.delete(`/api/admin/customers/${id}`);
  return response.data;
}

export async function listPendingKyc() {
  const response = await apiClient.get("/api/admin/kyc/pending");
  return response.data;
}

export async function updateKycStatus(customerId, newStatus, reason) {
  const response = await apiClient.patch(`/api/admin/kyc/${customerId}/status`, { newStatus, reason });
  return response.data;
}
