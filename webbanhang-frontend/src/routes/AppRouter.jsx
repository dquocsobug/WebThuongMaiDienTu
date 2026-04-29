import { lazy, Suspense } from "react";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import Layout from "../components/layout/Layout";
import { ProtectedRoute, RoleRoute } from "./ProtectedRoute";
import { PageLoader } from "../components/common";
import { ROLES } from "../utils/format";

const HomePage = lazy(() => import("../pages/HomePage"));
const ProductListPage = lazy(() => import("../pages/ProductListPage"));
const ProductDetailPage = lazy(() => import("../pages/ProductDetailPage"));
const CartPage = lazy(() => import("../pages/CartPage"));
const CheckoutPage = lazy(() => import("../pages/CheckoutPage"));
const OrderListPage = lazy(() => import("../pages/OrderListPage"));
const OrderDetailPage = lazy(() => import("../pages/OrderDetailPage"));
const PostListPage = lazy(() => import("../pages/PostListPage"));
const PostDetailPage = lazy(() => import("../pages/PostDetailPage"));
const PromotionsPage = lazy(() => import("../pages/PromotionsPage"));
const LoginPage = lazy(() => import("../pages/LoginPage"));
const RegisterPage = lazy(() => import("../pages/RegisterPage"));
const ProfilePage = lazy(() => import("../pages/ProfilePage"));
const NotFoundPage = lazy(() => import("../pages/NotFoundPage"));

const withSuspense = (Component) => (
  <Suspense fallback={<PageLoader />}>
    <Component />
  </Suspense>
);

const router = createBrowserRouter([
  // Trang auth không dùng Layout => không hiện Navbar/Footer
  {
    path: "/login",
    element: withSuspense(LoginPage),
  },
  {
    path: "/register",
    element: withSuspense(RegisterPage),
  },

  // Các trang chính có Layout => có Navbar/Footer
  {
    path: "/",
    element: <Layout />,
    children: [
      { index: true, element: withSuspense(HomePage) },
      { path: "products", element: withSuspense(ProductListPage) },
      { path: "products/:id", element: withSuspense(ProductDetailPage) },
      { path: "posts", element: withSuspense(PostListPage) },
      { path: "posts/:id", element: withSuspense(PostDetailPage) },
      { path: "promotions", element: withSuspense(PromotionsPage) },

      {
        path: "cart",
        element: (
          <ProtectedRoute>
            {withSuspense(CartPage)}
          </ProtectedRoute>
        ),
      },
      {
        path: "checkout",
        element: (
          <ProtectedRoute>
            {withSuspense(CheckoutPage)}
          </ProtectedRoute>
        ),
      },
      {
        path: "orders",
        element: (
          <ProtectedRoute>
            {withSuspense(OrderListPage)}
          </ProtectedRoute>
        ),
      },
      {
        path: "orders/:id",
        element: (
          <ProtectedRoute>
            {withSuspense(OrderDetailPage)}
          </ProtectedRoute>
        ),
      },
      {
        path: "profile",
        element: (
          <ProtectedRoute>
            {withSuspense(ProfilePage)}
          </ProtectedRoute>
        ),
      },
      {
        path: "admin/*",
        element: (
          <RoleRoute roles={[ROLES.ADMIN]}>
            <div className="p-8 text-center text-gray-500">
              Admin Dashboard (coming soon)
            </div>
          </RoleRoute>
        ),
      },
      {
        path: "writer/*",
        element: (
          <RoleRoute roles={[ROLES.WRITER, ROLES.ADMIN]}>
            <div className="p-8 text-center text-gray-500">
              Writer Dashboard (coming soon)
            </div>
          </RoleRoute>
        ),
      },

      { path: "*", element: withSuspense(NotFoundPage) },
    ],
  },
]);

export default function AppRouter() {
  return <RouterProvider router={router} />;
}