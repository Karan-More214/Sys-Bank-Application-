import apiClient from "./client";

export async function getTransactionHistory() {
  const response = await apiClient.get("/api/transactions/history");
  return response.data;
}

export async function initiateDeposit(amount) {
  const response = await apiClient.post("/api/transactions/deposit/initiate", { amount });
  return response.data;
}

export async function verifyDeposit(payload) {
  const response = await apiClient.post("/api/transactions/deposit/verify", payload);
  return response.data;
}

export async function withdraw(amount) {
  const response = await apiClient.post("/api/transactions/withdraw", { amount });
  return response.data;
}
