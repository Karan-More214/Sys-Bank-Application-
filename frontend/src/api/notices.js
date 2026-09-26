import apiClient from "./client";

export async function getActiveNotices() {
  const response = await apiClient.get("/api/notices/active");
  return response.data;
}

export async function listAllNotices() {
  const response = await apiClient.get("/api/notices");
  return response.data;
}

export async function createNotice(title, message) {
  const response = await apiClient.post("/api/notices", { title, message });
  return response.data;
}

export async function updateNotice(id, payload) {
  const response = await apiClient.patch(`/api/notices/${id}`, payload);
  return response.data;
}

export async function deleteNotice(id) {
  const response = await apiClient.delete(`/api/notices/${id}`);
  return response.data;
}
