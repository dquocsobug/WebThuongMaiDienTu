import axios from "axios";

const axiosClient = axios.create({
  baseURL: "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor - attach JWT token
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - unwrap data.data, handle 401
axiosClient.interceptors.response.use(
  (response) => {
    // Unwrap: response.data is { success, message, data, timestamp }
    return response.data;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    const message =
      error.response?.data?.message || "Đã xảy ra lỗi. Vui lòng thử lại.";
    return Promise.reject(new Error(message));
  }
);

export default axiosClient;