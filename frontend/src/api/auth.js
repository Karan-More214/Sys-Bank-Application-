import apiClient from "./client";

export async function login(email, password) {
  const response = await apiClient.post("/api/auth/login", { email, password });
  return response.data;
}

export async function register(fullName, email, username, password) {
  const response = await apiClient.post("/api/auth/register", {
    fullName,
    email,
    username,
    password,
  });
  return response.data;
}
