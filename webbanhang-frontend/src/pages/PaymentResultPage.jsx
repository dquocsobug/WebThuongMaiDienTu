import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

export default function PaymentResultPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  useEffect(() => {
    const resultCode = searchParams.get("resultCode");

    // Thành công
    if (resultCode === "0") {
      sessionStorage.removeItem("checkoutState");

      navigate("/orders", {
        replace: true,
      });
    }

    // Hủy / thất bại
    else {
      navigate("/checkout", {
        replace: true,
      });
    }
  }, [navigate, searchParams]);

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
        fontSize: 18,
        fontWeight: 700,
      }}
    >
      Đang xử lý kết quả thanh toán...
    </div>
  );
}