import { createContext, useContext, useState, useCallback } from "react";
import { authApi } from "../api";
import toast from "react-hot-toast";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem("user");
      return stored ? JSON.parse(stored) : null;
    } catch {
      localStorage.removeItem("user");
      return null;
    }
  });

  const [loading, setLoading] = useState(false);

  const isAuthenticated = Boolean(localStorage.getItem("token"));

  const hasRole = useCallback(
    (role) => {
      if (!user) return false;

      if (Array.isArray(user.roles)) {
        return user.roles.includes(role);
      }

      if (user.role) {
        return user.role === role;
      }

      return false;
    },
    [user]
  );

  const login = async (credentials) => {
    setLoading(true);

    try {
      const res = await authApi.login(credentials);

const data = res?.data || res;

const token = data?.token || data?.accessToken || data?.jwt;
const loginUser = data?.user || {
  userId: data?.userId,
  email: data?.email,
  fullName: data?.fullName,
  role: data?.role,
};

if (!token) {
  console.log("LOGIN RESPONSE =", res);
  throw new Error("Backend không trả về token.");
}

      if (!token) {
        throw new Error("Backend không trả về token.");
      }

      localStorage.setItem("token", token);
      localStorage.setItem("user", JSON.stringify(loginUser));
      setUser(loginUser);

      toast.success("Đăng nhập thành công!");
      return { success: true };
    } catch (error) {
      const message = error?.message || "Đăng nhập thất bại";
      toast.error(String(message));
      return { success: false, message: String(message) };
    } finally {
      setLoading(false);
    }
  };

  const logout = useCallback(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
    toast.success("Đã đăng xuất.");
  }, []);

  const updateUser = useCallback((updatedUser) => {
    localStorage.setItem("user", JSON.stringify(updatedUser));
    setUser(updatedUser);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAuthenticated,
        hasRole,
        login,
        logout,
        updateUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);

  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }

  return ctx;
};