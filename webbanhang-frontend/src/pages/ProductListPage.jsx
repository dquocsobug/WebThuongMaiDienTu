import { useState, useEffect, useCallback, useRef } from "react";
import { Link, useSearchParams } from "react-router-dom";
import axiosClient from "../api/axiosClient";
import { useCart } from "../context/CartContext";
import styles from "./ProductsListPage.module.css";

// ─── Helpers ──────────────────────────────────────────────────────────────────
const formatPrice = (n) => n?.toLocaleString("vi-VN") + "₫";

const StarRating = ({ rating }) => (
  <div className={styles.stars}>
    {[1, 2, 3, 4, 5].map((i) => (
      <span key={i} className={i <= Math.round(rating) ? styles.starOn : styles.starOff}>★</span>
    ))}
  </div>
);

const getImageUrl = (url) => {
  if (!url) return "/images/placeholder.png";
  if (url.startsWith("http")) return url;
  return `/images/${url}`;
};

// ─── Product Card ─────────────────────────────────────────────────────────────
const ProductCard = ({ product }) => {
  const hasDiscount = product.discountedPrice && product.discountedPrice < product.price;
  const { addToCart } = useCart?.() || {};
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);

  const handleAddToCart = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (!addToCart) return;
    try {
      setAdding(true);
      await addToCart(product.productId, 1);
      setAdded(true);
      setTimeout(() => setAdded(false), 1800);
    } finally {
      setAdding(false);
    }
  };

  return (
    <Link to={`/products/${product.productId}`} className={styles.card}>
      <div className={styles.cardImageWrap}>
        {hasDiscount && (
          <span className={styles.cardBadge}>-{product.discountPercent}%</span>
        )}
        {product.stock === 0 && (
          <div className={styles.cardOutOfStock}>Hết hàng</div>
        )}
        <img
          src={getImageUrl(product.mainImageUrl)}
          alt={product.productName}
          className={styles.cardImage}
          loading="lazy"
        />

        {/* Hover overlay */}
        <div className={styles.cardOverlay}>
          <div className={styles.overlayActions}>
            <span className={styles.overlayBtn} title="Xem chi tiết">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </span>
            <button
              className={`${styles.overlayBtn} ${added ? styles.overlayBtnAdded : ""}`}
              title="Thêm vào giỏ hàng"
              onClick={handleAddToCart}
              disabled={adding || product.stock === 0}
            >
              {added ? (
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
              ) : (
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
                  <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
                </svg>
              )}
            </button>
          </div>
        </div>
      </div>

      <div className={styles.cardBody}>
        <span className={styles.cardCategory}>{product.categoryName}</span>
        <h3 className={styles.cardName}>{product.productName}</h3>
        <div className={styles.cardMeta}>
          <StarRating rating={product.averageRating} />
          <span className={styles.cardReviewCount}>({product.reviewCount})</span>
        </div>
        <div className={styles.cardPricing}>
          {hasDiscount ? (
            <>
              <span className={styles.priceNew}>{formatPrice(product.discountedPrice)}</span>
              <span className={styles.priceOld}>{formatPrice(product.price)}</span>
            </>
          ) : (
            <span className={styles.priceNew}>{formatPrice(product.price)}</span>
          )}
        </div>
        {product.stock > 0 && product.stock <= 5 && (
          <span className={styles.lowStock}>Chỉ còn {product.stock} sản phẩm</span>
        )}
      </div>
    </Link>
  );
};

// ─── Price Range Slider ───────────────────────────────────────────────────────
const PriceRangeInput = ({ min, max, value, onChange }) => {
  const [localMin, setLocalMin] = useState(value[0]);
  const [localMax, setLocalMax] = useState(value[1]);
  const trackRef = useRef(null);

  useEffect(() => { setLocalMin(value[0]); setLocalMax(value[1]); }, [value]);

  const toPercent = (v) => ((v - min) / (max - min)) * 100;
  const commitChange = () => onChange([localMin, localMax]);

  return (
    <div className={styles.priceRange}>
      <div className={styles.priceRangeTrack} ref={trackRef}>
        <div
          className={styles.priceRangeFill}
          style={{ left: `${toPercent(localMin)}%`, width: `${toPercent(localMax) - toPercent(localMin)}%` }}
        />
        <input
          type="range" min={min} max={max} step={100000}
          value={localMin}
          className={`${styles.rangeInput} ${styles.rangeMin}`}
          onChange={(e) => {
            const v = Math.min(Number(e.target.value), localMax - 100000);
            setLocalMin(v);
          }}
          onMouseUp={commitChange} onTouchEnd={commitChange}
        />
        <input
          type="range" min={min} max={max} step={100000}
          value={localMax}
          className={`${styles.rangeInput} ${styles.rangeMax}`}
          onChange={(e) => {
            const v = Math.max(Number(e.target.value), localMin + 100000);
            setLocalMax(v);
          }}
          onMouseUp={commitChange} onTouchEnd={commitChange}
        />
      </div>
      <div className={styles.priceRangeLabels}>
        <span>{formatPrice(localMin)}</span>
        <span>{formatPrice(localMax)}</span>
      </div>
    </div>
  );
};

// ─── Skeleton Card ────────────────────────────────────────────────────────────
const SkeletonCard = () => (
  <div className={styles.skeleton}>
    <div className={styles.skeletonImg} />
    <div className={styles.skeletonBody}>
      <div className={styles.skeletonLine} style={{ width: "40%" }} />
      <div className={styles.skeletonLine} style={{ width: "80%" }} />
      <div className={styles.skeletonLine} style={{ width: "60%" }} />
    </div>
  </div>
);

// ─── Sort options ─────────────────────────────────────────────────────────────
const SORT_OPTIONS = [
  { value: "", label: "Mặc định" },
  { value: "price,asc", label: "Giá: Thấp → Cao" },
  { value: "price,desc", label: "Giá: Cao → Thấp" },
  { value: "averageRating,desc", label: "Đánh giá cao nhất" },
  { value: "createdAt,desc", label: "Mới nhất" },
];

const PRICE_MIN = 0;
const PRICE_MAX = 50000000;

// ─── Main Page ────────────────────────────────────────────────────────────────
export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const [keyword, setKeyword] = useState(searchParams.get("keyword") || "");
  const [selectedCategories, setSelectedCategories] = useState(
    searchParams.get("categoryId") ? [Number(searchParams.get("categoryId"))] : []
  );
  const [priceRange, setPriceRange] = useState([
    Number(searchParams.get("minPrice")) || PRICE_MIN,
    Number(searchParams.get("maxPrice")) || PRICE_MAX,
  ]);
  const [sort, setSort] = useState(searchParams.get("sort") || "");
  const [page, setPage] = useState(0);
  const [viewMode, setViewMode] = useState("grid"); // grid | list

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [catLoading, setCatLoading] = useState(true);

  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [inputValue, setInputValue] = useState(keyword);
  const debounceRef = useRef(null);

  useEffect(() => {
    axiosClient.get("/categories")
      .then((res) => {
        const data = res?.data?.data || res?.data || res;
        setCategories(Array.isArray(data) ? data : []);
      })
      .catch(() => setCategories([]))
      .finally(() => setCatLoading(false));
  }, []);

  const fetchProducts = useCallback(async () => {
    try {
      setLoading(true);
      const params = { page, size: 12 };
      if (keyword) params.keyword = keyword;
      if (selectedCategories.length === 1) params.categoryId = selectedCategories[0];
      if (priceRange[0] > PRICE_MIN) params.minPrice = priceRange[0];
      if (priceRange[1] < PRICE_MAX) params.maxPrice = priceRange[1];
      if (sort) params.sort = sort;

      const res = await axiosClient.get("/products", { params });
      const data = res?.data?.data || res?.data || res;
      setProducts(data?.content || []);
      setTotalPages(data?.totalPages || 0);
      setTotalElements(data?.totalElements || 0);
    } catch {
      setProducts([]);
    } finally {
      setLoading(false);
    }
  }, [keyword, selectedCategories, priceRange, sort, page]);

  useEffect(() => { fetchProducts(); }, [fetchProducts]);

  useEffect(() => {
    const p = {};
    if (keyword) p.keyword = keyword;
    if (selectedCategories.length === 1) p.categoryId = selectedCategories[0];
    if (priceRange[0] > PRICE_MIN) p.minPrice = priceRange[0];
    if (priceRange[1] < PRICE_MAX) p.maxPrice = priceRange[1];
    if (sort) p.sort = sort;
    if (page > 0) p.page = page;
    setSearchParams(p, { replace: true });
  }, [keyword, selectedCategories, priceRange, sort, page]);

  const handleKeywordChange = (e) => {
    setInputValue(e.target.value);
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      setKeyword(e.target.value);
      setPage(0);
    }, 400);
  };

  const handleCategoryToggle = (id) => {
    setSelectedCategories((prev) =>
      prev.includes(id) ? prev.filter((c) => c !== id) : [id]
    );
    setPage(0);
  };

  const handleSortChange = (e) => { setSort(e.target.value); setPage(0); };
  const handlePriceChange = (range) => { setPriceRange(range); setPage(0); };

  const handleClearFilters = () => {
    setKeyword(""); setInputValue("");
    setSelectedCategories([]);
    setPriceRange([PRICE_MIN, PRICE_MAX]);
    setSort("");
    setPage(0);
  };

  const hasActiveFilters =
    keyword || selectedCategories.length > 0 ||
    priceRange[0] > PRICE_MIN || priceRange[1] < PRICE_MAX || sort;

  const activeFilterCount = [
    keyword ? 1 : 0,
    selectedCategories.length > 0 ? 1 : 0,
    priceRange[0] > PRICE_MIN || priceRange[1] < PRICE_MAX ? 1 : 0,
    sort ? 1 : 0,
  ].reduce((a, b) => a + b, 0);

  return (
    <div className={styles.page}>
      {sidebarOpen && (
        <div className={styles.overlay} onClick={() => setSidebarOpen(false)} />
      )}

      {/* ── Header bar ── */}
      <div className={styles.topBar}>
        <div className={styles.topBarInner}>
          <div className={styles.breadcrumb}>
            <Link to="/" className={styles.breadcrumbLink}>Trang chủ</Link>
            <span className={styles.breadcrumbSep}>/</span>
            <span>Sản phẩm</span>
          </div>

          <div className={styles.searchWrap}>
            <svg className={styles.searchIcon} width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8" /><path d="m21 21-4.35-4.35" />
            </svg>
            <input
              className={styles.searchInput}
              type="text"
              placeholder="Tìm sản phẩm..."
              value={inputValue}
              onChange={handleKeywordChange}
            />
            {inputValue && (
              <button className={styles.searchClear} onClick={() => { setInputValue(""); setKeyword(""); setPage(0); }}>✕</button>
            )}
          </div>

          <div className={styles.topBarRight}>
            <select className={styles.sortSelect} value={sort} onChange={handleSortChange}>
              {SORT_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>

            {/* View mode toggle */}
            <div className={styles.viewToggle}>
              <button
                className={`${styles.viewBtn} ${viewMode === "grid" ? styles.viewBtnActive : ""}`}
                onClick={() => setViewMode("grid")}
                title="Lưới"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                  <rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/>
                  <rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>
                </svg>
              </button>
              <button
                className={`${styles.viewBtn} ${viewMode === "list" ? styles.viewBtnActive : ""}`}
                onClick={() => setViewMode("list")}
                title="Danh sách"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                  <rect x="3" y="4" width="18" height="3" rx="1"/><rect x="3" y="10.5" width="18" height="3" rx="1"/><rect x="3" y="17" width="18" height="3" rx="1"/>
                </svg>
              </button>
            </div>

            <button
              className={`${styles.filterToggleBtn} ${sidebarOpen ? styles.filterToggleBtnActive : ""}`}
              onClick={() => setSidebarOpen((v) => !v)}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="4" y1="6" x2="20" y2="6" /><line x1="8" y1="12" x2="16" y2="12" /><line x1="12" y1="18" x2="12" y2="18" strokeLinecap="round" />
              </svg>
              Bộ lọc
              {activeFilterCount > 0 && (
                <span className={styles.filterBadge}>{activeFilterCount}</span>
              )}
            </button>
          </div>
        </div>
      </div>

      <div className={styles.layout}>
        {/* ── SIDEBAR ── */}
        <aside className={`${styles.sidebar} ${sidebarOpen ? styles.sidebarOpen : ""}`}>
          <div className={styles.sidebarHeader}>
            <h2 className={styles.sidebarTitle}>Bộ lọc</h2>
            <div className={styles.sidebarHeaderRight}>
              {hasActiveFilters && (
                <button className={styles.clearBtn} onClick={handleClearFilters}>Xoá tất cả</button>
              )}
              <button className={styles.sidebarClose} onClick={() => setSidebarOpen(false)}>✕</button>
            </div>
          </div>

          <div className={styles.filterSection}>
            <h3 className={styles.filterLabel}>Danh mục</h3>
            {catLoading ? (
              <div className={styles.catSkeleton}>
                {[1,2,3].map(i => <div key={i} className={styles.catSkeletonItem} />)}
              </div>
            ) : (
              <div className={styles.categoryList}>
                <button
                  className={`${styles.catItem} ${selectedCategories.length === 0 ? styles.catItemActive : ""}`}
                  onClick={() => { setSelectedCategories([]); setPage(0); }}
                >
                  <span className={styles.catName}>Tất cả</span>
                  <span className={styles.catCount}>{totalElements}</span>
                </button>
                {categories.map((cat) => (
                  <button
                    key={cat.categoryId}
                    className={`${styles.catItem} ${selectedCategories.includes(cat.categoryId) ? styles.catItemActive : ""}`}
                    onClick={() => handleCategoryToggle(cat.categoryId)}
                  >
                    <span className={styles.catName}>{cat.categoryName}</span>
                    <span className={styles.catCount}>{cat.productCount}</span>
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className={styles.filterSection}>
            <h3 className={styles.filterLabel}>Khoảng giá</h3>
            <PriceRangeInput
              min={PRICE_MIN}
              max={PRICE_MAX}
              value={priceRange}
              onChange={handlePriceChange}
            />
          </div>

          <div className={styles.sidebarApply}>
            <button className={styles.applyBtn} onClick={() => setSidebarOpen(false)}>
              Xem {totalElements} sản phẩm
            </button>
          </div>
        </aside>

        {/* ── MAIN CONTENT ── */}
        <main className={styles.main}>
          <div className={styles.resultBar}>
            {loading ? (
              <span className={styles.resultText}>Đang tải...</span>
            ) : (
              <span className={styles.resultText}>
                {totalElements > 0 ? `${totalElements} sản phẩm` : "Không có sản phẩm"}
                {keyword && <> cho "<strong>{keyword}</strong>"</>}
              </span>
            )}

            <div className={styles.filterChips}>
              {selectedCategories.length > 0 && categories
                .filter(c => selectedCategories.includes(c.categoryId))
                .map(c => (
                  <span key={c.categoryId} className={styles.chip}>
                    {c.categoryName}
                    <button onClick={() => handleCategoryToggle(c.categoryId)}>✕</button>
                  </span>
                ))}
              {(priceRange[0] > PRICE_MIN || priceRange[1] < PRICE_MAX) && (
                <span className={styles.chip}>
                  {formatPrice(priceRange[0])} – {formatPrice(priceRange[1])}
                  <button onClick={() => { setPriceRange([PRICE_MIN, PRICE_MAX]); setPage(0); }}>✕</button>
                </span>
              )}
              {sort && (
                <span className={styles.chip}>
                  {SORT_OPTIONS.find(o => o.value === sort)?.label}
                  <button onClick={() => { setSort(""); setPage(0); }}>✕</button>
                </span>
              )}
            </div>
          </div>

          {/* Product grid / list */}
          {loading ? (
            <div className={styles.grid}>
              {Array.from({ length: 12 }).map((_, i) => <SkeletonCard key={i} />)}
            </div>
          ) : products.length === 0 ? (
            <div className={styles.empty}>
              <div className={styles.emptyIcon}>🔍</div>
              <h3 className={styles.emptyTitle}>Không tìm thấy sản phẩm</h3>
              <p className={styles.emptyDesc}>Thử thay đổi bộ lọc hoặc từ khoá tìm kiếm.</p>
              <button className={styles.emptyBtn} onClick={handleClearFilters}>Xoá bộ lọc</button>
            </div>
          ) : (
            <div className={`${styles.grid} ${viewMode === "list" ? styles.gridList : ""}`}>
              {products.map((p) => <ProductCard key={p.productId} product={p} />)}
            </div>
          )}

          {totalPages > 1 && (
            <div className={styles.pagination}>
              <button className={styles.pageBtn} disabled={page === 0} onClick={() => setPage(0)}>«</button>
              <button className={styles.pageBtn} disabled={page === 0} onClick={() => setPage((p) => p - 1)}>‹</button>

              {Array.from({ length: totalPages }, (_, i) => i)
                .filter(i => Math.abs(i - page) <= 2)
                .map((i) => (
                  <button
                    key={i}
                    className={`${styles.pageBtn} ${i === page ? styles.pageBtnActive : ""}`}
                    onClick={() => setPage(i)}
                  >{i + 1}</button>
                ))}

              <button className={styles.pageBtn} disabled={page >= totalPages - 1} onClick={() => setPage((p) => p + 1)}>›</button>
              <button className={styles.pageBtn} disabled={page >= totalPages - 1} onClick={() => setPage(totalPages - 1)}>»</button>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}