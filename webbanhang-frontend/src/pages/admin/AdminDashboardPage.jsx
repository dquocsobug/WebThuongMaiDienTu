import { useEffect, useMemo, useState } from "react";
import {
  Users,
  Package,
  ShoppingCart,
  Newspaper,
} from "lucide-react";
import { userApi, productApi, orderApi, postApi } from "../../api";

const unwrapList = (res) => {
  const payload = res?.data?.data ?? res?.data ?? res;

  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.content)) return payload.content;
  if (Array.isArray(payload?.data)) return payload.data;
  if (Array.isArray(payload?.data?.content)) return payload.data.content;

  return [];
};

export default function AdminDashboardPage() {
  const [counts, setCounts] = useState({
    users: 0,
    products: 0,
    orders: 0,
    posts: 0,
  });

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboard = async () => {
      setLoading(true);

      try {
        const [userRes, productRes, orderRes, postRes] = await Promise.all([
          userApi.getAll({ page: 0, size: 100 }),
          productApi.getAll({ page: 0, size: 100 }),
          orderApi.getAll({ page: 0, size: 100 }),
          postApi.getAllAdmin({ page: 0, size: 100 }),
        ]);

        setCounts({
          users: unwrapList(userRes).length,
          products: unwrapList(productRes).length,
          orders: unwrapList(orderRes).length,
          posts: unwrapList(postRes).length,
        });
      } catch (error) {
        console.error("Lỗi tải dữ liệu dashboard:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboard();
  }, []);

  const stats = useMemo(
    () => [
      {
        label: "Người dùng",
        value: counts.users,
        desc: "Tài khoản trong hệ thống",
        icon: Users,
      },
      {
        label: "Sản phẩm",
        value: counts.products,
        desc: "Sản phẩm đang bán",
        icon: Package,
      },
      {
        label: "Đơn hàng",
        value: counts.orders,
        desc: "Đơn hàng gần đây",
        icon: ShoppingCart,
      },
      {
        label: "Bài viết",
        value: counts.posts,
        desc: "Nội dung sản phẩm",
        icon: Newspaper,
      },
    ],
    [counts]
  );

  const maxValue = Math.max(...stats.map((item) => Number(item.value)), 1);
  const totalValue = stats.reduce((sum, item) => sum + Number(item.value), 0);

  let currentDegree = 0;

  const pieGradient =
    totalValue === 0
      ? "#e5e7eb 0deg 360deg"
      : stats
          .map((item, index) => {
            const colors = ["#2563eb", "#16a34a", "#f59e0b", "#dc2626"];
            const degree = (Number(item.value) / totalValue) * 360;
            const start = currentDegree;
            const end = currentDegree + degree;
            currentDegree = end;

            return `${colors[index]} ${start}deg ${end}deg`;
          })
          .join(", ");

  return (
    <div>
      <div className="admin-page-title">
        <h2>Tổng quan</h2>
        <p>Theo dõi nhanh tình trạng website bán hàng.</p>
      </div>

      <div className="admin-grid">
        {stats.map((item) => {
          const Icon = item.icon;

          return (
            <div className="admin-stat-card" key={item.label}>
              <Icon size={26} />
              <span>{item.label}</span>
              <strong>{loading ? "..." : item.value}</strong>
              <p>{item.desc}</p>
            </div>
          );
        })}
      </div>

      <div className="admin-dashboard-charts">
        <div className="admin-card admin-chart-card">
          <div className="admin-chart-header">
            <h3>Biểu đồ cột</h3>
            <p>So sánh số lượng người dùng, sản phẩm, đơn hàng và bài viết.</p>
          </div>

          <div className="admin-column-chart">
            {stats.map((item) => {
              const height = loading
                ? 20
                : Math.max((Number(item.value) / maxValue) * 180, 20);

              return (
                <div className="admin-column-item" key={item.label}>
                  <div className="admin-column-value">
                    {loading ? "..." : item.value}
                  </div>

                  <div className="admin-column-track">
                    <div
                      className="admin-column-fill"
                      style={{ height: `${height}px` }}
                    />
                  </div>

                  <span>{item.label}</span>
                </div>
              );
            })}
          </div>
        </div>

        <div className="admin-card admin-chart-card">
          <div className="admin-chart-header">
            <h3>Biểu đồ tròn</h3>
            <p>Tỷ trọng dữ liệu chính trong hệ thống.</p>
          </div>

          <div className="admin-pie-wrap">
            <div
              className="admin-pie-chart"
              style={{
                background: `conic-gradient(${pieGradient})`,
              }}
            />

            <div className="admin-pie-legend">
              {stats.map((item, index) => (
                <div className="admin-pie-legend-item" key={item.label}>
                  <span className={`admin-pie-dot dot-${index}`} />
                  <p>{item.label}</p>
                  <strong>{loading ? "..." : item.value}</strong>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}