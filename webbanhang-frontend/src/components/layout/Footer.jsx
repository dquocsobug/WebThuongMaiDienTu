import { Link } from "react-router-dom";

const Footer = () => {
  return (
    <footer className="bg-gray-900 text-gray-300 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-12">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
          {/* Brand */}
          <div>
            <div className="flex items-center gap-2 mb-4">
              <div className="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-black text-sm">T</span>
              </div>
              <span className="font-black text-lg text-white tracking-tight">
                Tech<span className="text-indigo-400">Store</span>
              </span>
            </div>
            <p className="text-sm text-gray-400 leading-relaxed">
              Cửa hàng công nghệ uy tín hàng đầu. Sản phẩm chính hãng, bảo hành toàn quốc.
            </p>
            <div className="flex gap-3 mt-4">
              {["facebook", "instagram", "youtube"].map((s) => (
                <a
                  key={s}
                  href="#"
                  className="w-8 h-8 rounded-full bg-gray-800 hover:bg-indigo-600 flex items-center justify-center transition"
                >
                  <span className="text-xs capitalize text-gray-300">{s[0].toUpperCase()}</span>
                </a>
              ))}
            </div>
          </div>

          {/* Links */}
          <div>
            <h4 className="text-white font-semibold mb-4 text-sm uppercase tracking-wider">Sản phẩm</h4>
            <ul className="space-y-2 text-sm">
              {["Điện thoại", "Laptop", "Máy tính bảng", "Phụ kiện", "Đồng hồ thông minh"].map((item) => (
                <li key={item}>
                  <Link to="/products" className="hover:text-white hover:text-indigo-400 transition">{item}</Link>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h4 className="text-white font-semibold mb-4 text-sm uppercase tracking-wider">Hỗ trợ</h4>
            <ul className="space-y-2 text-sm">
              {["Hướng dẫn mua hàng", "Chính sách đổi trả", "Chính sách bảo hành", "Thanh toán", "Vận chuyển"].map((item) => (
                <li key={item}>
                  <a href="#" className="hover:text-white transition">{item}</a>
                </li>
              ))}
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h4 className="text-white font-semibold mb-4 text-sm uppercase tracking-wider">Liên hệ</h4>
            <ul className="space-y-3 text-sm">
              <li className="flex items-start gap-2">
                <span>📍</span>
                <span>123 Nguyễn Văn Cừ, Q5, TP.HCM</span>
              </li>
              <li className="flex items-center gap-2">
                <span>📞</span>
                <span>1800 xxxx (Miễn phí)</span>
              </li>
              <li className="flex items-center gap-2">
                <span>✉️</span>
                <span>support@techstore.vn</span>
              </li>
              <li className="flex items-center gap-2">
                <span>🕐</span>
                <span>8:00 – 22:00 mỗi ngày</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div className="border-t border-gray-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4 flex flex-col sm:flex-row items-center justify-between gap-2 text-xs text-gray-500">
          <p>© {new Date().getFullYear()} TechStore. Tất cả quyền được bảo lưu.</p>
          <div className="flex gap-4">
            <a href="#" className="hover:text-gray-300 transition">Điều khoản</a>
            <a href="#" className="hover:text-gray-300 transition">Bảo mật</a>
            <a href="#" className="hover:text-gray-300 transition">Cookies</a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;