import apiClient from "./client";

export async function raiseTicket(payload) {
  const response = await apiClient.post("/api/tickets", payload);
  return response.data;
}

export async function getMyTickets() {
  const response = await apiClient.get("/api/tickets/mine");
  return response.data;
}

export async function getTicket(id) {
  const response = await apiClient.get(`/api/tickets/${id}`);
  return response.data;
}

export async function listAllTickets(filters = {}) {
  const response = await apiClient.get("/api/tickets", { params: filters });
  return response.data;
}

export async function respondToTicket(id, payload) {
  const response = await apiClient.patch(`/api/tickets/${id}`, payload);
  return response.data;
}
