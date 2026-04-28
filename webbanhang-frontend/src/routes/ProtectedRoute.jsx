import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/**
 * Requires user to be authenticated.
 * Redirects to /login with return path saved.
 */
export const ProtectedRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
};

/**
 * Requires user to have specific role(s).
 * @param {string|string[]} roles - required role(s)
 * @param {string} redirectTo - where to redirect if unauthorized (default: "/")
 */
export const RoleRoute = ({ children, roles, redirectTo = "/" }) => {
  const { isAuthenticated, hasRole } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  const requiredRoles = Array.isArray(roles) ? roles : [roles];
  const authorized = requiredRoles.some((role) => hasRole(role));

  if (!authorized) {
    return <Navigate to={redirectTo} replace />;
  }

  return children;
};