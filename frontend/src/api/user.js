import apiClient from "./client";

export async function getDashboard() {
  const response = await apiClient.get("/api/user/dashboard");
  return response.data;
}

export async function updateProfile(payload) {
  const response = await apiClient.put("/api/user/profile", payload);
  return response.data;
}
