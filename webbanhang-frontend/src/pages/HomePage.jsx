import { useState, useEffect, useRef } from "react";
import { Link } from "react-router-dom";
import { postApi, productApi, promotionApi } from "../api";
import { formatVND } from "../utils/format";
import { useCart } from "../context/CartContext";

// ─── Helpers ──────────────────────────────────────────────────────────────────

const imgFallback = (e) =>
  (e.target.src = "https://placehold.co/800x500/f1f5f9/94a3b8?text=TechStore");
const productImgFallback = (e) =>
  (e.target.src = "https://placehold.co/400x400/f8fafc/94a3b8?text=Product");

function useReveal(options = {}) {
  const ref = useRef(null);
  const [visible, setVisible] = useState(false);
  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const obs = new IntersectionObserver(
      ([entry]) => { if (entry.isIntersecting) { setVisible(true); obs.disconnect(); } },
      { threshold: 0.1, ...options }
    );
    obs.observe(el);
    return () => obs.disconnect();
  }, []);
  return [ref, visible];
}

// ─── Mock data (shown while real API loads) ───────────────────────────────────

const MOCK_POSTS = [
  {
    postId: 1, category: "Review",
    title: "Top 5 điện thoại đáng mua nhất năm 2026",
    summary: "Thị trường smartphone 2026 bùng nổ với loạt flagship mới. Chúng tôi đã test kỹ và chọn ra 5 cái tên xứng đáng với từng đồng tiền bạn bỏ ra.",
    mainImageUrl: "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=900&q=80",
    commentCount: 48,
  },
  {
    postId: 2, category: "So sánh",
    title: "MacBook Air M4 vs Dell XPS 15: Ai dành cho ai?",
    summary: "Hai đối thủ đỉnh nhất phân khúc ultrabook đối đầu trực tiếp. Hiệu năng, pin, màn hình — không có câu trả lời dễ dàng.",
    mainImageUrl: "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=900&q=80",
    commentCount: 31,
  },
  {
    postId: 3, category: "Hướng dẫn",
    title: "Tai nghe không dây tốt nhất dưới 2 triệu 2026",
    summary: "Bạn không cần chi cả chục triệu để có âm thanh tốt. 8 mẫu tai nghe này sẽ thay đổi suy nghĩ của bạn.",
    mainImageUrl: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=900&q=80",
    commentCount: 22,
  },
  {
    postId: 4, category: "Hướng dẫn",
    title: "Cách chọn màn hình gaming: Những điều ai cũng bỏ qua",
    summary: "Hz cao không phải tất cả. Response time, panel type, HDR thực sự — những yếu tố quyết định trải nghiệm chơi game.",
    mainImageUrl: "https://images.unsplash.com/photo-1593640408182-31c228c71e7f?w=900&q=80",
    commentCount: 17,
  },
  {
    postId: 5, category: "Tin tức",
    title: "Samsung Galaxy S25 Ultra ra mắt: Những gì thay đổi",
    summary: "Màn hình mới, S Pen cải tiến, và con chip mạnh nhất từ trước đến nay. Samsung có làm hài lòng fan trung thành?",
    mainImageUrl: "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=900&q=80",
    commentCount: 63,
  },
];

const MOCK_PRODUCTS = [
  {
    productId: 1, categoryName: "Điện thoại",
    productName: "iPhone 16 Pro Max 256GB",
    price: 34990000, discountedPrice: 31990000, discountPercent: 9,
    mainImageUrl: "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=500&q=80",
    averageRating: 4.8, reviewCount: 124,
  },
  {
    productId: 2, categoryName: "Laptop",
    productName: 'MacBook Air 13" M4 8GB',
    price: 29990000, discountedPrice: 27990000, discountPercent: 7,
    mainImageUrl: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&q=80",
    averageRating: 4.9, reviewCount: 87,
  },
  {
    productId: 3, categoryName: "Tai nghe",
    productName: "Sony WH-1000XM6",
    price: 8990000, discountedPrice: 7490000, discountPercent: 17,
    mainImageUrl: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80",
    averageRating: 4.7, reviewCount: 56,
  },
  {
    productId: 4, categoryName: "Máy tính bảng",
    productName: "Samsung Galaxy Tab S10+",
    price: 22990000, discountedPrice: 19990000, discountPercent: 13,
    mainImageUrl: "https://images.unsplash.com/photo-1561154464-82e9adf32764?w=500&q=80",
    averageRating: 4.6, reviewCount: 43,
  },
];

// ─── Micro components ─────────────────────────────────────────────────────────

const Tag = ({ label }) => (
  <span className="inline-block text-[11px] font-bold tracking-widest uppercase px-2.5 py-0.5 rounded-md bg-blue-50 text-blue-600 border border-blue-100">
    {label}
  </span>
);

const Stars = ({ rating = 0, count }) => (
  <div className="flex items-center gap-1">
    <div className="flex gap-0.5">
      {[1,2,3,4,5].map(i => (
        <svg key={i} className={`w-3 h-3 ${i <= Math.round(rating) ? "text-amber-400" : "text-gray-200"}`} fill="currentColor" viewBox="0 0 20 20">
          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
        </svg>
      ))}
    </div>
    {count !== undefined && <span className="text-xs text-gray-400">({count})</span>}
  </div>
);

const Arrow = ({ className = "" }) => (
  <svg className={`w-4 h-4 ${className}`} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M17 8l4 4m0 0l-4 4m4-4H3" />
  </svg>
);

// ─── 1. HERO BANNER ───────────────────────────────────────────────────────────

const HeroBanner = ({ post }) => {
  const [ref, visible] = useReveal();
  if (!post) return null;
  return (
    <section ref={ref} className="relative overflow-hidden bg-[#0a0a0a]">
      {/* Background */}
      <div className="absolute inset-0">
        <img src={post.mainImageUrl} alt="" onError={imgFallback}
          className="w-full h-full object-cover opacity-35" />
        <div className="absolute inset-0 bg-gradient-to-r from-[#0a0a0a] via-[#0a0a0a]/75 to-transparent" />
        <div className="absolute inset-0 bg-gradient-to-t from-[#0a0a0a]/60 via-transparent to-transparent" />
      </div>

      {/* Grid lines decoration */}
      <div className="absolute inset-0 opacity-[0.04] pointer-events-none"
        style={{ backgroundImage: "linear-gradient(#fff 1px, transparent 1px), linear-gradient(90deg, #fff 1px, transparent 1px)", backgroundSize: "60px 60px" }} />

      <div className="relative max-w-7xl mx-auto px-6 sm:px-8 py-24 sm:py-32">
        <div
          className={`max-w-2xl transition-all duration-1000 ${visible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-10"}`}
        >
          <div className="flex items-center gap-3 mb-5">
            <Tag label={post.category || "Nổi bật"} />
            <span className="text-gray-500 text-xs tracking-wide">• Bài viết của tuần</span>
          </div>

          <h1
            className="text-4xl sm:text-5xl lg:text-6xl font-bold text-white mb-5 leading-[1.1]"
            style={{ fontFamily: "'Georgia', 'Times New Roman', serif", letterSpacing: "-0.025em" }}
          >
            {post.title}
          </h1>

          <p className="text-gray-300 text-lg leading-relaxed mb-8 max-w-lg">
            {post.summary}
          </p>

          <div className="flex items-center gap-4">
            <Link to={`/posts/${post.postId}`}
              className="inline-flex items-center gap-2 bg-white text-[#0a0a0a] px-7 py-3.5 rounded-full font-semibold text-sm hover:bg-gray-100 transition-all shadow-xl"
            >
              Đọc ngay <Arrow />
            </Link>
            <span className="text-gray-400 text-sm flex items-center gap-1.5">
              💬 {post.commentCount} bình luận
            </span>
          </div>
        </div>
      </div>

      {/* Bottom fade */}
      <div className="absolute bottom-0 left-0 right-0 h-16 bg-gradient-to-t from-white to-transparent" />
    </section>
  );
};

// ─── 2. FEATURED POSTS SECTION ────────────────────────────────────────────────

const CATEGORIES = [
  { label: "Tất cả", icon: "✦" },
  { label: "Review", icon: "⭐" },
  { label: "So sánh", icon: "⚖️" },
  { label: "Hướng dẫn", icon: "📖" },
  { label: "Tin tức", icon: "📡" },
];

const FeaturedPosts = ({ posts }) => {
  const [active, setActive] = useState("Tất cả");
  const filtered = active === "Tất cả" ? posts : posts.filter(p => p.category === active);
  const [hero, ...rest] = filtered;

  return (
    <section className="py-20 bg-white">
      <div className="max-w-7xl mx-auto px-6 sm:px-8">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4 mb-8">
          <div>
            <p className="text-xs font-bold tracking-widest uppercase text-blue-500 mb-1.5">Khám phá</p>
            <h2 className="text-3xl font-bold text-gray-900" style={{ fontFamily: "'Georgia', serif", letterSpacing: "-0.02em" }}>
              Bài viết công nghệ nổi bật
            </h2>
          </div>
          <Link to="/posts" className="group flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-900 font-medium transition-colors">
            Xem tất cả <Arrow className="group-hover:translate-x-1 transition-transform" />
          </Link>
        </div>

        {/* Filter pills */}
        <div className="flex gap-2 overflow-x-auto pb-3 mb-8 [-webkit-overflow-scrolling:touch]">
          {CATEGORIES.map(c => (
            <button key={c.label} onClick={() => setActive(c.label)}
              className={`flex items-center gap-1.5 px-4 py-2 rounded-full text-sm font-medium transition-all duration-200 whitespace-nowrap flex-shrink-0 ${
                active === c.label
                  ? "bg-gray-900 text-white shadow-sm"
                  : "bg-white text-gray-600 border border-gray-200 hover:border-gray-400 hover:text-gray-900"
              }`}
            >
              {c.icon} {c.label}
            </button>
          ))}
        </div>

        {/* Layout: big left + list right */}
        {hero && (
          <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
            {/* Big card */}
            <Link to={`/posts/${hero.postId}`}
              className="group lg:col-span-3 relative overflow-hidden rounded-2xl bg-gray-100 block"
            >
              <div className="aspect-[16/10] overflow-hidden">
                <img src={hero.mainImageUrl} alt={hero.title} onError={imgFallback}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700" />
              </div>
              <div className="absolute inset-0 bg-gradient-to-t from-black/75 via-black/20 to-transparent rounded-2xl" />
              <div className="absolute bottom-0 left-0 right-0 p-6">
                <Tag label={hero.category || "Bài viết"} />
                <h3 className="text-white font-bold text-xl sm:text-2xl mt-2 leading-snug line-clamp-2 group-hover:text-blue-200 transition-colors"
                  style={{ fontFamily: "'Georgia', serif" }}>
                  {hero.title}
                </h3>
                <p className="text-gray-300 text-sm mt-1.5 line-clamp-2">{hero.summary}</p>
                <div className="flex items-center gap-2 mt-3 text-gray-300 text-xs">
                  💬 {hero.commentCount} bình luận
                </div>
              </div>
            </Link>

            {/* Stacked list */}
            <div className="lg:col-span-2 divide-y divide-gray-100">
              {rest.slice(0, 4).map((post) => (
                <Link key={post.postId} to={`/posts/${post.postId}`}
                  className="group flex gap-4 items-start py-4 first:pt-0 hover:bg-gray-50 -mx-3 px-3 rounded-xl transition-colors"
                >
                  <div className="w-20 h-20 rounded-xl overflow-hidden flex-shrink-0 bg-gray-100">
                    <img src={post.mainImageUrl} alt={post.title} onError={imgFallback}
                      className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <Tag label={post.category || "Bài viết"} />
                    <h4 className="text-gray-900 font-semibold text-sm mt-1.5 line-clamp-2 group-hover:text-blue-600 transition-colors leading-snug"
                      style={{ fontFamily: "'Georgia', serif" }}>
                      {post.title}
                    </h4>
                    <span className="text-gray-400 text-xs mt-1 block">💬 {post.commentCount}</span>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        )}
      </div>
    </section>
  );
};

// ─── 3. PRODUCT CARD ─────────────────────────────────────────────────────────

const ProductCard = ({ product, delay = 0 }) => {
  const { addToCart } = useCart();
  const [ref, visible] = useReveal();
  const [adding, setAdding] = useState(false);

  const handleAdd = async (e) => {
    e.preventDefault();
    setAdding(true);
    await addToCart(product.productId, 1);
    setAdding(false);
  };

  return (
    <div
      ref={ref}
      className={`group bg-white rounded-2xl border border-gray-100 overflow-hidden hover:shadow-xl hover:border-gray-200 transition-all duration-500 ${
        visible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
      }`}
      style={{ transitionDelay: `${delay}ms` }}
    >
      <Link to={`/products/${product.productId}`}>
        <div className="relative bg-[#f8f9fa] aspect-square overflow-hidden">
          <img src={product.mainImageUrl} alt={product.productName} onError={productImgFallback}
            className="w-full h-full object-contain p-8 group-hover:scale-105 transition-transform duration-500" />
          {product.discountPercent > 0 && (
            <span className="absolute top-3 left-3 bg-red-500 text-white text-xs font-bold px-2 py-0.5 rounded-full">
              -{product.discountPercent}%
            </span>
          )}
          <span className="absolute top-3 right-3 text-[10px] font-semibold text-gray-500 bg-white/90 backdrop-blur-sm px-2 py-0.5 rounded-full border border-gray-100">
            {product.categoryName}
          </span>
        </div>
        <div className="p-4 pb-3">
          <h4 className="text-gray-900 font-semibold text-sm leading-snug line-clamp-2 mb-2 group-hover:text-blue-600 transition-colors">
            {product.productName}
          </h4>
          <Stars rating={product.averageRating} count={product.reviewCount} />
          <div className="mt-3 flex items-end justify-between">
            <div>
              <p className="text-gray-900 font-bold text-base">{formatVND(product.discountedPrice || product.price)}</p>
              {product.discountedPrice && product.discountedPrice < product.price && (
                <p className="text-gray-400 text-xs line-through">{formatVND(product.price)}</p>
              )}
            </div>
          </div>
        </div>
      </Link>
      <div className="px-4 pb-4">
        <button onClick={handleAdd} disabled={adding}
          className="w-full flex items-center justify-center gap-2 bg-gray-900 hover:bg-gray-700 text-white text-sm font-medium py-2.5 rounded-xl transition-all duration-200 disabled:opacity-60"
        >
          {adding
            ? <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
            : <><svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-1.5 6M17 13l1.5 6M9 19a1 1 0 100 2 1 1 0 000-2zm8 0a1 1 0 100 2 1 1 0 000-2z" /></svg> Thêm vào giỏ</>
          }
        </button>
      </div>
    </div>
  );
};

// ─── 4. REVIEW SECTION (products embedded in content) ─────────────────────────

const ReviewsSection = ({ products }) => (
  <section className="py-20 bg-[#f8f9fa]">
    <div className="max-w-7xl mx-auto px-6 sm:px-8">
      <div className="max-w-2xl mb-10">
        <p className="text-xs font-bold tracking-widest uppercase text-blue-500 mb-2">Đã kiểm chứng</p>
        <h2 className="text-3xl font-bold text-gray-900 mb-3" style={{ fontFamily: "'Georgia', serif", letterSpacing: "-0.02em" }}>
          Chúng tôi đã dùng thử cho bạn
        </h2>
        <p className="text-gray-500 text-base leading-relaxed">
          Mỗi sản phẩm được đội ngũ biên tập trải nghiệm ít nhất 2 tuần trước khi kết luận.
          Không quảng cáo, không thiên vị.
        </p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        {products.map((p, i) => <ProductCard key={p.productId} product={p} delay={i * 80} />)}
      </div>

      <div className="text-center mt-10">
        <Link to="/products"
          className="inline-flex items-center gap-2 border border-gray-300 text-gray-700 hover:border-gray-900 hover:text-gray-900 px-6 py-3 rounded-full text-sm font-medium transition-all duration-200"
        >
          Xem tất cả sản phẩm <Arrow />
        </Link>
      </div>
    </div>
  </section>
);

// ─── 5. EDITORIAL BRIDGE (content × product crossover) ────────────────────────

const EditorialBridge = () => {
  const [ref, visible] = useReveal();
  return (
    <section ref={ref} className={`py-20 bg-white transition-all duration-700 ${visible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}`}>
      <div className="max-w-7xl mx-auto px-6 sm:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-stretch">

          {/* Left: editorial card */}
          <Link to="/posts/1"
            className="group relative overflow-hidden rounded-2xl bg-gray-950 p-8 sm:p-10 flex flex-col justify-between min-h-[320px]"
          >
            <div className="absolute inset-0 opacity-20">
              <img src="https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&q=60"
                alt="" className="w-full h-full object-cover" />
            </div>
            <div className="relative">
              <Tag label="Hướng dẫn mua" />
              <h3 className="text-white text-2xl font-bold mt-4 mb-3 leading-snug group-hover:text-blue-300 transition-colors"
                style={{ fontFamily: "'Georgia', serif" }}>
                Chọn laptop đầu tiên: Đừng nhìn vào giá
              </h3>
              <p className="text-gray-400 text-sm leading-relaxed">
                RAM, CPU, màn hình — thứ tự ưu tiên thực sự khi mua laptop cho sinh viên và người đi làm năm 2026.
              </p>
            </div>
            <div className="relative flex items-center gap-2 mt-6 text-blue-400 text-sm font-medium">
              Đọc bài viết <Arrow className="group-hover:translate-x-1 transition-transform" />
            </div>
          </Link>

          {/* Right: stats + newsletter */}
          <div className="flex flex-col gap-5">
            {/* Trust stats */}
            <div className="grid grid-cols-3 gap-4">
              {[
                { num: "200+", label: "Bài review" },
                { num: "50k+", label: "Độc giả/tháng" },
                { num: "98%", label: "Hài lòng" },
              ].map(({ num, label }) => (
                <div key={label} className="text-center p-5 bg-gray-50 rounded-2xl border border-gray-100">
                  <p className="text-2xl font-black text-gray-900" style={{ fontFamily: "'Georgia', serif" }}>{num}</p>
                  <p className="text-gray-400 text-xs mt-0.5">{label}</p>
                </div>
              ))}
            </div>

            {/* Newsletter */}
            <div className="flex-1 bg-blue-600 rounded-2xl p-7 sm:p-8 flex flex-col justify-between">
              <div>
                <div className="text-2xl mb-3">📬</div>
                <h3 className="text-white font-bold text-xl mb-2" style={{ fontFamily: "'Georgia', serif" }}>
                  Bản tin công nghệ tuần
                </h3>
                <p className="text-blue-100 text-sm mb-5 leading-relaxed">
                  Review mới, so sánh sâu, gợi ý mua hàng — mỗi tuần một lần, không spam.
                </p>
              </div>
              <div className="flex gap-2">
                <input type="email" placeholder="email@của.bạn"
                  className="flex-1 bg-blue-500/50 text-white placeholder-blue-300 text-sm px-4 py-3 rounded-xl outline-none focus:ring-2 focus:ring-white/40 transition border border-blue-400/30" />
                <button className="bg-white text-blue-600 text-sm font-semibold px-5 py-3 rounded-xl hover:bg-blue-50 transition whitespace-nowrap">
                  Đăng ký
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

// ─── 6. SUGGESTION SECTION ────────────────────────────────────────────────────

const SuggestionsSection = ({ posts }) => (
  <section className="py-20 bg-[#f8f9fa] border-t border-gray-100">
    <div className="max-w-7xl mx-auto px-6 sm:px-8">
      <div className="flex items-end justify-between mb-8">
        <div>
          <p className="text-xs font-bold tracking-widest uppercase text-blue-500 mb-1.5">Dành riêng cho bạn</p>
          <h2 className="text-3xl font-bold text-gray-900" style={{ fontFamily: "'Georgia', serif", letterSpacing: "-0.02em" }}>
            Có thể bạn quan tâm
          </h2>
        </div>
        <Link to="/posts" className="group flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-900 font-medium transition-colors">
          Xem thêm <Arrow className="group-hover:translate-x-1 transition-transform" />
        </Link>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {posts.map((post, i) => {
          const [ref, visible] = useReveal();
          return (
            <Link
              key={post.postId}
              ref={ref}
              to={`/posts/${post.postId}`}
              className={`group bg-white rounded-2xl border border-gray-100 overflow-hidden hover:shadow-lg hover:border-gray-200 transition-all duration-500 ${
                visible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
              }`}
              style={{ transitionDelay: `${i * 80}ms` }}
            >
              <div className="aspect-video overflow-hidden bg-gray-100">
                <img src={post.mainImageUrl} alt={post.title} onError={imgFallback}
                  className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
              </div>
              <div className="p-4">
                <Tag label={post.category || "Công nghệ"} />
                <h4 className="text-gray-900 font-semibold text-sm mt-2 line-clamp-2 group-hover:text-blue-600 transition-colors leading-snug"
                  style={{ fontFamily: "'Georgia', serif" }}>
                  {post.title}
                </h4>
                <p className="text-gray-400 text-xs mt-1.5 line-clamp-2">{post.summary}</p>
                <div className="flex items-center justify-between mt-3 pt-3 border-t border-gray-50">
                  <span className="text-xs text-gray-400">💬 {post.commentCount}</span>
                  <span className="text-blue-500 text-xs font-medium group-hover:underline">Đọc →</span>
                </div>
              </div>
            </Link>
          );
        })}
      </div>
    </div>
  </section>
);

// ─── 7. PROMO BANNER ──────────────────────────────────────────────────────────

const PromoBanner = ({ promotions }) => {
  if (!promotions?.length) return null;
  return (
    <section className="bg-gray-900 border-t border-gray-800">
      <div className="max-w-7xl mx-auto px-6 sm:px-8 py-5">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0 text-xl">🎁</div>
            <div>
              <p className="text-white font-semibold text-sm">{promotions[0].promotionName}</p>
              <p className="text-gray-400 text-xs">Giảm đến {promotions[0].discountPercent}% · Số lượng có hạn</p>
            </div>
          </div>
          <Link to="/promotions"
            className="flex-shrink-0 bg-blue-600 hover:bg-blue-500 text-white text-sm font-semibold px-5 py-2.5 rounded-xl transition">
            Xem ưu đãi →
          </Link>
        </div>
      </div>
    </section>
  );
};

// ─── MAIN ─────────────────────────────────────────────────────────────────────

export default function HomePage() {
  const [posts, setPosts] = useState(MOCK_POSTS);
  const [products, setProducts] = useState(MOCK_PRODUCTS);
  const [promotions, setPromotions] = useState([]);

  useEffect(() => {
    const fetchAll = async () => {
      const [postsRes, productsRes, promoRes] = await Promise.allSettled([
        postApi.getAll({ page: 0, size: 8 }),
        productApi.getAll({ page: 0, size: 4, sort: "createdAt,desc" }),
        promotionApi.getActive(),
      ]);
      if (postsRes.status === "fulfilled" && postsRes.value?.content?.length) setPosts(postsRes.value.content);
      if (productsRes.status === "fulfilled" && productsRes.value?.content?.length) setProducts(productsRes.value.content);
      if (promoRes.status === "fulfilled" && Array.isArray(promoRes.value)) setPromotions(promoRes.value);
    };
    fetchAll();
  }, []);

  return (
    <div style={{ fontFamily: "'Helvetica Neue', Helvetica, sans-serif" }}>
      {promotions.length > 0 && <PromoBanner promotions={promotions} />}
      <HeroBanner post={posts[0]} />
      <FeaturedPosts posts={posts} />
      <ReviewsSection products={products.slice(0, 4)} />
      <EditorialBridge />
      <SuggestionsSection posts={posts.slice(1, 5)} />
    </div>
  );
}