import { lazy, Suspense } from "react";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import Layout from "../components/layout/Layout";
import { ProtectedRoute, RoleRoute } from "./ProtectedRoute";
import { PageLoader } from "../components/common";
import { ROLES } from "../utils/format";

// Lazy-loaded pages
const HomePage        = lazy(() => import("../pages/HomePage"));
const ProductListPage = lazy(() => import("../pages/ProductListPage"));
const ProductDetailPage = lazy(() => import("../pages/ProductDetailPage"));
const CartPage        = lazy(() => import("../pages/CartPage"));
const CheckoutPage    = lazy(() => import("../pages/CheckoutPage"));
const OrderListPage   = lazy(() => import("../pages/OrderListPage"));
const OrderDetailPage = lazy(() => import("../pages/OrderDetailPage"));
const PostListPage    = lazy(() => import("../pages/PostListPage"));
const PostDetailPage  = lazy(() => import("../pages/PostDetailPage"));
const PromotionsPage  = lazy(() => import("../pages/PromotionsPage"));
const LoginPage       = lazy(() => import("../pages/LoginPage"));
const RegisterPage    = lazy(() => import("../pages/RegisterPage"));
const ProfilePage     = lazy(() => import("../pages/ProfilePage"));
const NotFoundPage    = lazy(() => import("../pages/NotFoundPage"));

const router = createBrowserRouter([
  {
    path: "/",
    element: <Layout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: "products", element: <ProductListPage /> },
      { path: "products/:id", element: <ProductDetailPage /> },
      { path: "posts", element: <PostListPage /> },
      { path: "posts/:id", element: <PostDetailPage /> },
      { path: "promotions", element: <PromotionsPage /> },
      { path: "login", element: <LoginPage /> },
      { path: "register", element: <RegisterPage /> },

      // Protected routes
      {
        path: "cart",
        element: <ProtectedRoute><CartPage /></ProtectedRoute>,
      },
      {
        path: "checkout",
        element: <ProtectedRoute><CheckoutPage /></ProtectedRoute>,
      },
      {
        path: "orders",
        element: <ProtectedRoute><OrderListPage /></ProtectedRoute>,
      },
      {
        path: "orders/:id",
        element: <ProtectedRoute><OrderDetailPage /></ProtectedRoute>,
      },
      {
        path: "profile",
        element: <ProtectedRoute><ProfilePage /></ProtectedRoute>,
      },

      // Role-based routes (placeholder - create pages as needed)
      {
        path: "admin/*",
        element: (
          <RoleRoute roles={[ROLES.ADMIN]}>
            <div className="p-8 text-center text-gray-500">Admin Dashboard (coming soon)</div>
          </RoleRoute>
        ),
      },
      {
        path: "writer/*",
        element: (
          <RoleRoute roles={[ROLES.WRITER, ROLES.ADMIN]}>
            <div className="p-8 text-center text-gray-500">Writer Dashboard (coming soon)</div>
          </RoleRoute>
        ),
      },

      { path: "*", element: <NotFoundPage /> },
    ],
  },
]);

const AppRouter = () => (
  <Suspense fallback={<PageLoader />}>
    <RouterProvider router={router} />
  </Suspense>
);

export default AppRouter;