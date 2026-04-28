import axiosClient from "./axiosClient";

// ─── AUTH ────────────────────────────────────────────────────────────────────
export const authApi = {
  login: (credentials) =>
    axiosClient.post("/auth/login", credentials).then((r) => r.data),

  register: (payload) =>
    axiosClient.post("/auth/register", payload).then((r) => r.data),

  logout: () => axiosClient.post("/auth/logout"),
};

// ─── PRODUCTS ─────────────────────────────────────────────────────────────────
export const productApi = {
  getAll: (params) =>
    axiosClient.get("/products", { params }).then((r) => r.data),

  getById: (id) =>
    axiosClient.get(`/products/${id}`).then((r) => r.data),

  search: (params) =>
    axiosClient.get("/products/search", { params }).then((r) => r.data),

  getByCategory: (categoryId, params) =>
    axiosClient
      .get(`/products/category/${categoryId}`, { params })
      .then((r) => r.data),
};

// ─── CART ─────────────────────────────────────────────────────────────────────
export const cartApi = {
  getCart: () => axiosClient.get("/cart").then((r) => r.data),

  addItem: (productId, quantity) =>
    axiosClient
      .post("/cart/items", { productId, quantity })
      .then((r) => r.data),

  updateItem: (cartItemId, quantity) =>
    axiosClient
      .put(`/cart/items/${cartItemId}`, { quantity })
      .then((r) => r.data),

  removeItem: (cartItemId) =>
    axiosClient.delete(`/cart/items/${cartItemId}`).then((r) => r.data),

  clearCart: () => axiosClient.delete("/cart").then((r) => r.data),
};

// ─── ORDERS ───────────────────────────────────────────────────────────────────
export const orderApi = {
  getMyOrders: (params) =>
    axiosClient.get("/orders/my", { params }).then((r) => r.data),

  getOrderById: (id) =>
    axiosClient.get(`/orders/my/${id}`).then((r) => r.data),

  createOrder: (payload) =>
    axiosClient.post("/orders", payload).then((r) => r.data),

  cancelOrder: (id) =>
    axiosClient.put(`/orders/${id}/cancel`).then((r) => r.data),
};

// ─── REVIEWS ──────────────────────────────────────────────────────────────────
export const reviewApi = {
  getByProduct: (productId, params) =>
    axiosClient
      .get(`/reviews/product/${productId}`, { params })
      .then((r) => r.data),

  create: (payload) =>
    axiosClient.post("/reviews", payload).then((r) => r.data),
};

// ─── POSTS ────────────────────────────────────────────────────────────────────
export const postApi = {
  getAll: (params) =>
    axiosClient.get("/posts", { params }).then((r) => r.data),

  getById: (id) => axiosClient.get(`/posts/${id}`).then((r) => r.data),
};

// ─── COMMENTS ─────────────────────────────────────────────────────────────────
export const commentApi = {
  getByPost: (postId, params) =>
    axiosClient
      .get(`/comments/post/${postId}`, { params })
      .then((r) => r.data),

  create: (payload) =>
    axiosClient.post("/comments", payload).then((r) => r.data),
};

// ─── PROMOTIONS ───────────────────────────────────────────────────────────────
export const promotionApi = {
  getActive: () => axiosClient.get("/promotions/active").then((r) => r.data),
};

// ─── CATEGORIES ───────────────────────────────────────────────────────────────
export const categoryApi = {
  getAll: () => axiosClient.get("/categories").then((r) => r.data),
};

// ─── USER ─────────────────────────────────────────────────────────────────────
export const userApi = {
  getProfile: () => axiosClient.get("/users/profile").then((r) => r.data),

  updateProfile: (payload) =>
    axiosClient.put("/users/profile", payload).then((r) => r.data),

  changePassword: (payload) =>
    axiosClient.put("/users/change-password", payload).then((r) => r.data),
};