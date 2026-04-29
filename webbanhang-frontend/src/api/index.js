import axiosClient from "./axiosClient";

// ─── AUTH ────────────────────────────────────────────────────────────────────
export const authApi = {
  login: (credentials) => axiosClient.post("/auth/login", credentials),

  register: (payload) => axiosClient.post("/auth/register", payload),

  logout: () => axiosClient.post("/auth/logout"),
};

// ─── PRODUCTS ─────────────────────────────────────────────────────────────────
export const productApi = {
  getAll: (params) => axiosClient.get("/products", { params }),

  getById: (id) => axiosClient.get(`/products/${id}`),

  search: (params) => axiosClient.get("/products/search", { params }),

  getByCategory: (categoryId, params) =>
    axiosClient.get(`/products/category/${categoryId}`, { params }),
};

// ─── CART ─────────────────────────────────────────────────────────────────────
export const cartApi = {
  getCart: () => axiosClient.get("/cart"),

  addItem: (productId, quantity) =>
    axiosClient.post("/cart/items", { productId, quantity }),

  updateItem: (cartItemId, quantity) =>
    axiosClient.put(`/cart/items/${cartItemId}`, { quantity }),

  removeItem: (cartItemId) => axiosClient.delete(`/cart/items/${cartItemId}`),

  clearCart: () => axiosClient.delete("/cart"),
};

// ─── ORDERS ───────────────────────────────────────────────────────────────────
export const orderApi = {
  getMyOrders: (params) => axiosClient.get("/orders/my", { params }),

  getOrderById: (id) => axiosClient.get(`/orders/my/${id}`),

  createOrder: (payload) => axiosClient.post("/orders", payload),

  cancelOrder: (id) => axiosClient.put(`/orders/${id}/cancel`),
};

// ─── REVIEWS ──────────────────────────────────────────────────────────────────
export const reviewApi = {
  getByProduct: (productId, params) =>
    axiosClient.get(`/reviews/product/${productId}`, { params }),

  create: (payload) => axiosClient.post("/reviews", payload),
};

// ─── POSTS ────────────────────────────────────────────────────────────────────
export const postApi = {
  getAll: (params) => axiosClient.get("/posts", { params }),

  getById: (id) => axiosClient.get(`/posts/${id}`),
};

// ─── COMMENTS ─────────────────────────────────────────────────────────────────
export const commentApi = {
  getByPost: (postId, params) =>
    axiosClient.get(`/comments/post/${postId}`, { params }),

  create: (payload) => axiosClient.post("/comments", payload),
};

// ─── PROMOTIONS ───────────────────────────────────────────────────────────────
export const promotionApi = {
  getActive: () => axiosClient.get("/promotions/active"),
};

// ─── CATEGORIES ───────────────────────────────────────────────────────────────
export const categoryApi = {
  getAll: () => axiosClient.get("/categories"),
};

// ─── USER ─────────────────────────────────────────────────────────────────────
export const userApi = {
  getProfile: () => axiosClient.get("/users/profile"),

  updateProfile: (payload) => axiosClient.put("/users/profile", payload),

  changePassword: (payload) =>
    axiosClient.put("/users/change-password", payload),
};