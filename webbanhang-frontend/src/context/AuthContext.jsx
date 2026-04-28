import { createContext, useContext, useState, useEffect, useCallback } from "react";
import { authApi } from "../api";
import toast from "react-hot-toast";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem("user");
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });
  const [loading, setLoading] = useState(false);

  const isAuthenticated = Boolean(user && localStorage.getItem("token"));

  const hasRole = useCallback(
    (role) => {
      if (!user) return false;
      const roles = user.roles || [];
      return roles.includes(role);
    },
    [user]
  );

  const login = async (credentials) => {
    setLoading(true);
    try {
      const data = await authApi.login(credentials);
      // data = { token, user }
      localStorage.setItem("token", data.token);
      localStorage.setItem("user", JSON.stringify(data.user));
      setUser(data.user);
      toast.success("Đăng nhập thành công!");
      return { success: true };
    } catch (error) {
      toast.error(error.message);
      return { success: false, message: error.message };
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
      value={{ user, loading, isAuthenticated, hasRole, login, logout, updateUser }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
};