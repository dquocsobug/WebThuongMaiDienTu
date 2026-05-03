-- =========================================
-- DATABASE WEB BÁN HÀNG NỘI DUNG CHI PHỐI
-- SQL Server
-- Mô hình hỗ trợ:
-- 1) Khách hàng thường
-- 2) Khách hàng thân thiết
-- 3) Admin
-- 4) Cộng tác viên viết bài
-- Ghi chú: Khách vãng lai không có tài khoản nên không lưu trong DB.
-- =========================================

IF EXISTS (SELECT * FROM sys.databases WHERE name = 'WebBanHang')
BEGIN
    ALTER DATABASE WebBanHang SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE WebBanHang;
END
GO

CREATE DATABASE WebBanHang;
GO

USE WebBanHang;
GO

-- =========================================
-- 1. USERS
-- Role:
-- CUSTOMER        : khách hàng đã đăng nhập
-- LOYAL_CUSTOMER  : khách hàng thân thiết
-- ADMIN           : quản trị viên
-- WRITER          : cộng tác viên viết bài
-- =========================================
CREATE TABLE Users (
    UserID            INT IDENTITY(1,1) PRIMARY KEY,
    FullName          NVARCHAR(100) NOT NULL,
    Email             NVARCHAR(100) NOT NULL UNIQUE,
    Password          NVARCHAR(255) NOT NULL,
    Phone             NVARCHAR(20) NULL,
    Address           NVARCHAR(255) NULL,
    Role              NVARCHAR(30) NOT NULL DEFAULT 'CUSTOMER'
                      CHECK (Role IN ('CUSTOMER', 'LOYAL_CUSTOMER', 'ADMIN', 'WRITER')),
    IsActive          BIT NOT NULL DEFAULT 1,
    LoyalSince        DATETIME NULL,
    CreatedAt         DATETIME NOT NULL DEFAULT GETDATE()
);
GO

-- =========================================
-- 2. CATEGORIES
-- =========================================
CREATE TABLE Categories (
    CategoryID        INT IDENTITY(1,1) PRIMARY KEY,
    CategoryName      NVARCHAR(100) NOT NULL,
    Description       NVARCHAR(255) NULL,
    CreatedAt         DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT UQ_Categories_CategoryName UNIQUE (CategoryName)
);
GO

-- =========================================
-- 3. PRODUCTS
-- Admin quản lý sản phẩm
-- =========================================
CREATE TABLE Products (
    ProductID         INT IDENTITY(1,1) PRIMARY KEY,
    ProductName       NVARCHAR(255) NOT NULL,
    Description       NVARCHAR(MAX) NULL,
    Price             DECIMAL(18,2) NOT NULL CHECK (Price >= 0),
    Stock             INT NOT NULL DEFAULT 0 CHECK (Stock >= 0),
    CategoryID        INT NOT NULL,
    CreatedBy         INT NULL,
    IsActive          BIT NOT NULL DEFAULT 1,
    CreatedAt         DATETIME NOT NULL DEFAULT GETDATE(),
    UpdatedAt         DATETIME NULL,

    CONSTRAINT FK_Products_Categories
        FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID),
    CONSTRAINT FK_Products_Users
        FOREIGN KEY (CreatedBy) REFERENCES Users(UserID)
);
GO

-- =========================================
-- 4. PRODUCT_IMAGES
-- 1 sản phẩm có nhiều ảnh
-- =========================================
CREATE TABLE ProductImages (
    ImageID           INT IDENTITY(1,1) PRIMARY KEY,
    ProductID         INT NOT NULL,
    ImageURL          NVARCHAR(255) NOT NULL,
    IsMain            BIT NOT NULL DEFAULT 0,
    DisplayOrder      INT NOT NULL DEFAULT 1 CHECK (DisplayOrder > 0),
    CreatedAt         DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_ProductImages_Products
        FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);
GO

-- =========================================
-- 5. CARTS
-- 1 user có 1 cart
-- Chỉ khách hàng / khách hàng thân thiết mới dùng giỏ hàng
-- =========================================
CREATE TABLE Carts (
    CartID            INT IDENTITY(1,1) PRIMARY KEY,
    UserID            INT NOT NULL UNIQUE,
    CreatedAt         DATETIME NOT NULL DEFAULT GETDATE(),
    UpdatedAt         DATETIME NULL,

    CONSTRAINT FK_Carts_Users
        FOREIGN KEY (UserID) REFERENCES Users(UserID)
);
GO

-- =========================================
-- 6. CART_ITEMS
-- =========================================
CREATE TABLE CartItems (
    CartItemID        INT IDENTITY(1,1) PRIMARY KEY,
    CartID            INT NOT NULL,
    ProductID         INT NOT NULL,
    Quantity          INT NOT NULL DEFAULT 1 CHECK (Quantity > 0),

    CONSTRAINT FK_CartItems_Carts
        FOREIGN KEY (CartID) REFERENCES Carts(CartID),
    CONSTRAINT FK_CartItems_Products
        FOREIGN KEY (ProductID) REFERENCES Products(ProductID),
    CONSTRAINT UQ_CartItems UNIQUE (CartID, ProductID)
);
GO

-- =========================================
-- 7. ORDERS
-- =========================================
CREATE TABLE Orders (
    OrderID            INT IDENTITY(1,1) PRIMARY KEY,
    UserID             INT NOT NULL,
    TotalAmount        DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (TotalAmount >= 0),
    Status             NVARCHAR(50) NOT NULL DEFAULT 'PENDING'
                       CHECK (Status IN ('PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'CANCELLED')),
    ReceiverName       NVARCHAR(100) NOT NULL,
    ReceiverPhone      NVARCHAR(20) NOT NULL,
    ShippingAddress    NVARCHAR(255) NOT NULL,
    PaymentMethod      NVARCHAR(50) NOT NULL
                       CHECK (PaymentMethod IN ('COD', 'MOMO', 'BANK_TRANSFER')),
    PaymentStatus      NVARCHAR(50) NOT NULL DEFAULT 'UNPAID'
                       CHECK (PaymentStatus IN ('UNPAID', 'PAID', 'REFUNDED')),
    Note               NVARCHAR(255) NULL,
    DiscountAmount     DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (DiscountAmount >= 0),
    FinalAmount        DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (FinalAmount >= 0),
    CreatedAt          DATETIME NOT NULL DEFAULT GETDATE(),
    UpdatedAt          DATETIME NULL,

    CONSTRAINT FK_Orders_Users
        FOREIGN KEY (UserID) REFERENCES Users(UserID)
);
GO

-- =========================================
-- 8. ORDER_DETAILS
-- Lưu snapshot giá tại thời điểm mua hàng
-- =========================================
CREATE TABLE OrderDetails (
    OrderDetailID      INT IDENTITY(1,1) PRIMARY KEY,
    OrderID            INT NOT NULL,
    ProductID          INT NOT NULL,
    Quantity           INT NOT NULL CHECK (Quantity > 0),
    UnitPrice          DECIMAL(18,2) NOT NULL CHECK (UnitPrice >= 0),
    Subtotal           DECIMAL(18,2) NOT NULL CHECK (Subtotal >= 0),

    CONSTRAINT FK_OrderDetails_Orders
        FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
    CONSTRAINT FK_OrderDetails_Products
        FOREIGN KEY (ProductID) REFERENCES Products(ProductID),
    CONSTRAINT UQ_OrderDetails UNIQUE (OrderID, ProductID)
);
GO

-- =========================================
-- 9. REVIEWS
-- Đánh giá sản phẩm
-- Chỉ nên cho người đã mua hàng đánh giá ở tầng ứng dụng
-- =========================================
CREATE TABLE Reviews (
    ReviewID           INT IDENTITY(1,1) PRIMARY KEY,
    UserID             INT NOT NULL,
    ProductID          INT NOT NULL,
    Rating             INT NOT NULL CHECK (Rating BETWEEN 1 AND 5),
    Comment            NVARCHAR(MAX) NULL,
    CreatedAt          DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Reviews_Users
        FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT FK_Reviews_Products
        FOREIGN KEY (ProductID) REFERENCES Products(ProductID),
    CONSTRAINT UQ_Reviews UNIQUE (UserID, ProductID)
);
GO

-- =========================================
-- 10. POSTS
-- Bài viết nội dung chi phối
-- Admin, WRITER, LOYAL_CUSTOMER có thể tạo bài
-- PENDING  : chờ duyệt
-- APPROVED : đã duyệt
-- REJECTED : bị từ chối
-- =========================================
CREATE TABLE Posts (
    PostID              INT IDENTITY(1,1) PRIMARY KEY,
    Title               NVARCHAR(255) NOT NULL,
    Summary             NVARCHAR(500) NULL,
    Content             NVARCHAR(MAX) NOT NULL,
    CreatedBy           INT NOT NULL,
    Status              NVARCHAR(30) NOT NULL DEFAULT 'PENDING'
                        CHECK (Status IN ('PENDING', 'APPROVED', 'REJECTED')),
    ApprovedBy          INT NULL,
    ApprovedAt          DATETIME NULL,
    RejectReason        NVARCHAR(255) NULL,
    ViewCount           INT NOT NULL DEFAULT 0 CHECK (ViewCount >= 0),
    IsFeatured          BIT NOT NULL DEFAULT 0,
    CreatedAt           DATETIME NOT NULL DEFAULT GETDATE(),
    UpdatedAt           DATETIME NULL,

    CONSTRAINT FK_Posts_CreatedBy
        FOREIGN KEY (CreatedBy) REFERENCES Users(UserID),
    CONSTRAINT FK_Posts_ApprovedBy
        FOREIGN KEY (ApprovedBy) REFERENCES Users(UserID),
    CONSTRAINT CK_Posts_Approval CHECK (
        (Status = 'PENDING' AND ApprovedBy IS NULL AND ApprovedAt IS NULL)
        OR (Status = 'APPROVED' AND ApprovedBy IS NOT NULL AND ApprovedAt IS NOT NULL)
        OR (Status = 'REJECTED')
    )
);
GO

-- =========================================
-- 11. POST_IMAGES
-- 1 bài viết có nhiều ảnh
-- =========================================
CREATE TABLE PostImages (
    ImageID            INT IDENTITY(1,1) PRIMARY KEY,
    PostID             INT NOT NULL,
    ImageURL           NVARCHAR(255) NOT NULL,
    IsMain             BIT NOT NULL DEFAULT 0,
    DisplayOrder       INT NOT NULL DEFAULT 1 CHECK (DisplayOrder > 0),
    CreatedAt          DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_PostImages_Posts
        FOREIGN KEY (PostID) REFERENCES Posts(PostID)
);
GO

-- =========================================
-- 12. COMMENTS
-- Bình luận bài viết, không phải bình luận sản phẩm
-- Có hỗ trợ trả lời bình luận bằng ParentCommentID
-- =========================================
CREATE TABLE Comments (
    CommentID          INT IDENTITY(1,1) PRIMARY KEY,
    UserID             INT NOT NULL,
    PostID             INT NOT NULL,
    ParentCommentID    INT NULL,
    Content            NVARCHAR(MAX) NOT NULL,
    IsApproved         BIT NOT NULL DEFAULT 1,
    CreatedAt          DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Comments_Users
        FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT FK_Comments_Posts
        FOREIGN KEY (PostID) REFERENCES Posts(PostID),
    CONSTRAINT FK_Comments_Parent
        FOREIGN KEY (ParentCommentID) REFERENCES Comments(CommentID)
);
GO

-- =========================================
-- 13. PROMOTIONS
-- Khuyến mãi chung hoặc riêng theo nhóm role
-- =========================================
CREATE TABLE Promotions (
    PromotionID         INT IDENTITY(1,1) PRIMARY KEY,
    PromotionName       NVARCHAR(255) NOT NULL,
    DiscountPercent     INT NULL CHECK (DiscountPercent BETWEEN 0 AND 100),
    DiscountAmount      DECIMAL(18,2) NULL CHECK (DiscountAmount >= 0),
    TargetRole          NVARCHAR(30) NULL
                        CHECK (TargetRole IN ('ALL', 'CUSTOMER', 'LOYAL_CUSTOMER')),
    StartDate           DATETIME NULL,
    EndDate             DATETIME NULL,
    IsActive            BIT NOT NULL DEFAULT 1,
    CreatedAt           DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT CK_Promotions_Value CHECK (
        (DiscountPercent IS NOT NULL AND DiscountAmount IS NULL)
        OR (DiscountPercent IS NULL AND DiscountAmount IS NOT NULL)
    ),
    CONSTRAINT CK_Promotions_Date CHECK (
        EndDate IS NULL OR StartDate IS NULL OR EndDate >= StartDate
    )
);
GO

-- =========================================
-- 14. PRODUCT_PROMOTIONS
-- Áp khuyến mãi cho từng sản phẩm
-- =========================================
CREATE TABLE ProductPromotions (
    ID                  INT IDENTITY(1,1) PRIMARY KEY,
    ProductID           INT NOT NULL,
    PromotionID         INT NOT NULL,

    CONSTRAINT FK_ProductPromotions_Products
        FOREIGN KEY (ProductID) REFERENCES Products(ProductID),
    CONSTRAINT FK_ProductPromotions_Promotions
        FOREIGN KEY (PromotionID) REFERENCES Promotions(PromotionID),
    CONSTRAINT UQ_ProductPromotions UNIQUE (ProductID, PromotionID)
);
GO

-- =========================================
-- 15. POST_PRODUCTS
-- Sản phẩm xuất hiện trong bài viết
-- =========================================
CREATE TABLE PostProducts (
    ID                  INT IDENTITY(1,1) PRIMARY KEY,
    PostID              INT NOT NULL,
    ProductID           INT NOT NULL,
    DisplayOrder        INT NOT NULL DEFAULT 1 CHECK (DisplayOrder > 0),
    Note                NVARCHAR(255) NULL,

    CONSTRAINT FK_PostProducts_Posts
        FOREIGN KEY (PostID) REFERENCES Posts(PostID),
    CONSTRAINT FK_PostProducts_Products
        FOREIGN KEY (ProductID) REFERENCES Products(ProductID),
    CONSTRAINT UQ_PostProducts UNIQUE (PostID, ProductID)
);
GO

-- =========================================
-- 16. VOUCHERS
-- Có thể dùng cho khách hàng thân thiết
-- =========================================
CREATE TABLE Vouchers (
    VoucherID           INT IDENTITY(1,1) PRIMARY KEY,
    VoucherCode         NVARCHAR(50) NOT NULL UNIQUE,
    VoucherName         NVARCHAR(255) NOT NULL,
    DiscountPercent     INT NULL CHECK (DiscountPercent BETWEEN 0 AND 100),
    DiscountAmount      DECIMAL(18,2) NULL CHECK (DiscountAmount >= 0),
    MinOrderValue       DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (MinOrderValue >= 0),
    TargetRole          NVARCHAR(30) NOT NULL DEFAULT 'LOYAL_CUSTOMER'
                        CHECK (TargetRole IN ('CUSTOMER', 'LOYAL_CUSTOMER')),
    Quantity            INT NOT NULL DEFAULT 0 CHECK (Quantity >= 0),
    StartDate           DATETIME NULL,
    EndDate             DATETIME NULL,
    IsActive            BIT NOT NULL DEFAULT 1,
    CreatedAt           DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT CK_Vouchers_Value CHECK (
        (DiscountPercent IS NOT NULL AND DiscountAmount IS NULL)
        OR (DiscountPercent IS NULL AND DiscountAmount IS NOT NULL)
    ),
    CONSTRAINT CK_Vouchers_Date CHECK (
        EndDate IS NULL OR StartDate IS NULL OR EndDate >= StartDate
    )
);
GO

-- =========================================
-- 17. USER_VOUCHERS
-- Gán voucher cho từng user nếu cần
-- =========================================
CREATE TABLE UserVouchers (
    UserVoucherID       INT IDENTITY(1,1) PRIMARY KEY,
    UserID              INT NOT NULL,
    VoucherID           INT NOT NULL,
    IsUsed              BIT NOT NULL DEFAULT 0,
    UsedAt              DATETIME NULL,
    AssignedAt          DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_UserVouchers_Users
        FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT FK_UserVouchers_Vouchers
        FOREIGN KEY (VoucherID) REFERENCES Vouchers(VoucherID),
    CONSTRAINT UQ_UserVouchers UNIQUE (UserID, VoucherID)
);
GO

-- =========================================
-- INDEXES
-- =========================================
CREATE INDEX IX_Products_CategoryID ON Products(CategoryID);
CREATE INDEX IX_ProductImages_ProductID ON ProductImages(ProductID);
CREATE INDEX IX_CartItems_CartID ON CartItems(CartID);
CREATE INDEX IX_CartItems_ProductID ON CartItems(ProductID);
CREATE INDEX IX_Orders_UserID ON Orders(UserID);
CREATE INDEX IX_OrderDetails_OrderID ON OrderDetails(OrderID);
CREATE INDEX IX_OrderDetails_ProductID ON OrderDetails(ProductID);
CREATE INDEX IX_Reviews_ProductID ON Reviews(ProductID);
CREATE INDEX IX_Posts_CreatedBy ON Posts(CreatedBy);
CREATE INDEX IX_Posts_Status ON Posts(Status);
CREATE INDEX IX_PostImages_PostID ON PostImages(PostID);
CREATE INDEX IX_Comments_PostID ON Comments(PostID);
CREATE INDEX IX_PostProducts_PostID ON PostProducts(PostID);
CREATE INDEX IX_PostProducts_ProductID ON PostProducts(ProductID);
CREATE INDEX IX_UserVouchers_UserID ON UserVouchers(UserID);
GO

-- =========================================
-- GỢI Ý NGHIỆP VỤ
-- =========================================
-- 1. Khách vãng lai chỉ xem nên không lưu trong DB.
-- 2. Khi user mua hàng thành công đủ điều kiện 1 tháng:
--    cập nhật Users.Role = 'LOYAL_CUSTOMER'
--    và có thể set LoyalSince = GETDATE().
-- 3. Chỉ ADMIN mới nên được duyệt bài ở tầng ứng dụng.
-- 4. WRITER và LOYAL_CUSTOMER có thể tạo bài nhưng mặc định nên là PENDING.
-- 5. Reviews chỉ nên cho user đã mua hàng đánh giá ở tầng backend.

-- ================================================Chèn dữ liệu======================================================--
USE WebBanHang;
GO

-- =========================================
-- 1. USERS
-- =========================================
INSERT INTO Users (FullName, Email, Password, Phone, Address, Role)
VALUES 
(N'Quản trị viên', 'admin@gmail.com', '123456', '0900000001', N'Đà Nẵng', 'ADMIN'),
(N'Nguyễn Văn A', 'user1@gmail.com', '123456', '0900000002', N'Quy Nhơn', 'CUSTOMER'),
(N'Lê Văn B', 'user2@gmail.com', '123456', '0900000003', N'Hà Nội', 'LOYAL_CUSTOMER'),
(N'Cộng tác viên A', 'writer@gmail.com', '123456', '0900000004', N'Hồ Chí Minh', 'WRITER');
GO

-- =========================================
-- 2. CATEGORIES
-- =========================================
INSERT INTO Categories (CategoryName, Description)
VALUES 
(N'Tai nghe', N'Tai nghe có dây và không dây'),
(N'Điện thoại', N'Các dòng điện thoại thông minh'),
(N'Phụ kiện', N'Phụ kiện công nghệ'),
(N'Laptop', N'Laptop học tập, văn phòng và gaming'),
(N'Đồng hồ thông minh', N'Smartwatch theo dõi sức khỏe'),
(N'Bàn phím', N'Bàn phím cơ và bàn phím văn phòng'),
(N'Chuột', N'Chuột gaming và chuột không dây');
GO

-- =========================================
-- 3. PRODUCTS
-- =========================================
INSERT INTO Products (ProductName, Description, Price, Stock, CategoryID)
VALUES 
(N'AirPods Pro 2', N'Tai nghe chống ồn cao cấp của Apple', 5500000, 50, 1),
(N'Sony WH-1000XM5', N'Tai nghe chống ồn cao cấp của Sony', 8000000, 30, 1),
(N'iPhone 15', N'Điện thoại Apple hiệu năng cao', 25000000, 20, 2),
(N'Sạc nhanh 20W', N'Củ sạc nhanh dành cho điện thoại', 300000, 100, 3),
(N'MacBook Air M2', N'Laptop mỏng nhẹ dùng chip Apple M2', 24500000, 15, 4),
(N'Dell XPS 13', N'Laptop cao cấp cho học tập và văn phòng', 28000000, 10, 4),
(N'Apple Watch Series 9', N'Đồng hồ thông minh theo dõi sức khỏe', 10500000, 25, 5),
(N'Samsung Galaxy Watch 6', N'Smartwatch Android pin tốt', 6500000, 30, 5),
(N'Keychron K2', N'Bàn phím cơ không dây layout gọn', 2200000, 40, 6),
(N'Logitech MX Keys', N'Bàn phím văn phòng cao cấp', 2600000, 35, 6),
(N'Logitech MX Master 3S', N'Chuột không dây cao cấp cho làm việc', 2500000, 45, 7),
(N'Razer DeathAdder V3', N'Chuột gaming siêu nhẹ', 1800000, 50, 7),
(N'Galaxy S24 Ultra', N'Điện thoại Samsung cao cấp', 30000000, 18, 2),
(N'Tai nghe JBL Tune 770NC', N'Tai nghe chống ồn giá tốt', 3200000, 28, 1);
GO

-- =========================================
-- 4. PRODUCT_IMAGES
-- =========================================
INSERT INTO ProductImages (ProductID, ImageURL, IsMain, DisplayOrder)
VALUES 
(1, 'airpods1.jpg', 1, 1),
(1, 'airpods2.jpg', 0, 2),
(2, 'sony1.jpg', 1, 1),
(3, 'iphone1.jpg', 1, 1),
(4, 'charger1.jpg', 1, 1),
(5, 'macbook-air-m2-1.jpg', 1, 1),
(5, 'macbook-air-m2-2.jpg', 0, 2),
(6, 'dell-xps-13-1.jpg', 1, 1),
(7, 'apple-watch-s9-1.jpg', 1, 1),
(8, 'galaxy-watch-6-1.jpg', 1, 1),
(9, 'keychron-k2-1.jpg', 1, 1),
(10, 'mx-keys-1.jpg', 1, 1),
(11, 'mx-master-3s-1.jpg', 1, 1),
(12, 'razer-deathadder-v3-1.jpg', 1, 1),
(13, 'galaxy-s24-ultra-1.jpg', 1, 1),
(14, 'jbl-tune-770nc-1.jpg', 1, 1);
GO

-- =========================================
-- 5. CARTS
-- =========================================
INSERT INTO Carts (UserID)
VALUES (2), (3) ;
GO

-- =========================================
-- 6. CART_ITEMS
-- =========================================
INSERT INTO CartItems (CartID, ProductID, Quantity)
VALUES 
(1, 1, 1),
(1, 4, 2),
(2, 2, 1),
(1, 5, 1),
(1, 9, 1),
(2, 13, 1),
(2, 11, 1),
(2, 7, 1),
(2, 14, 2);
GO

-- =========================================
-- 7. ORDERS
-- =========================================
INSERT INTO Orders
(
    UserID,
    TotalAmount,
    Status,
    ReceiverName,
    ReceiverPhone,
    ShippingAddress,
    PaymentMethod,
    PaymentStatus,
    Note,
    DiscountAmount,
    FinalAmount
)
VALUES 
(
    2,
    5800000,
    'DELIVERED',
    N'Nguyễn Văn A',
    '0900000002',
    N'Đà Nẵng',
    'COD',
    'PAID',
    N'Giao giờ hành chính',
    0,
    5800000
),
(
    3,
    8000000,
    'DELIVERED',
    N'Lê Văn B',
    '0900000003',
    N'Hà Nội',
    'BANK_TRANSFER',
    'PAID',
    N'Khách hàng thân thiết',
    0,
    8000000
);
GO

-- =========================================
-- 8. ORDER_DETAILS
-- =========================================
INSERT INTO OrderDetails
(
    OrderID,
    ProductID,
    Quantity,
    UnitPrice,
    Subtotal
)
VALUES 
(1, 1, 1, 5500000, 5500000),
(1, 4, 1, 300000, 300000),
(2, 2, 1, 8000000, 8000000);
GO

-- =========================================
-- 9. REVIEWS
-- =========================================
INSERT INTO Reviews (UserID, ProductID, Rating, Comment)
VALUES 
(2, 1, 5, N'Âm thanh tốt, chống ồn ổn'),
(3, 2, 4, N'Đeo thoải mái, pin tốt');
GO

-- =========================================
-- 10. POSTS
-- Lưu ý:
-- APPROVED thì phải có ApprovedBy, ApprovedAt
-- PENDING thì ApprovedBy, ApprovedAt phải NULL
-- =========================================
INSERT INTO Posts
(
    Title,
    Content,
    Summary,
    CreatedBy,
    Status,
    ApprovedBy,
    ApprovedAt,
    RejectReason
)
VALUES 
(
    N'Top tai nghe tốt nhất 2026',
    N'Năm 2026 chứng kiến sự phát triển mạnh mẽ của công nghệ âm thanh, đặc biệt là phân khúc tai nghe không dây và chống ồn chủ động (ANC). 
Các hãng lớn như Apple, Sony, Bose và JBL đều liên tục cải tiến chất lượng âm thanh, thời lượng pin và trải nghiệm người dùng.

Đối với sinh viên và dân văn phòng, tiêu chí lựa chọn tai nghe thường bao gồm:
- Khả năng chống ồn để tập trung học tập/làm việc
- Thời lượng pin dài (ít nhất 20-30 giờ)
- Độ thoải mái khi đeo lâu
- Kết nối ổn định (Bluetooth 5.2 trở lên)

Một số lựa chọn nổi bật:
1. AirPods Pro 2:
   - Âm thanh cân bằng, phù hợp người dùng iPhone
   - ANC tốt trong tầm giá
   - Tích hợp sâu với hệ sinh thái Apple

2. Sony WH-1000XM5:
   - Chống ồn hàng đầu thị trường
   - Âm bass mạnh, phù hợp giải trí
   - Pin lên tới 30 giờ

3. JBL Tune 770NC:
   - Giá rẻ hơn nhưng vẫn có ANC
   - Phù hợp sinh viên ngân sách thấp

Kết luận: Nếu bạn cần trải nghiệm cao cấp → Sony/Apple.
Nếu bạn cần tiết kiệm → JBL là lựa chọn hợp lý.',
    N'Danh sách tai nghe nổi bật năm 2026',
    1,
    'APPROVED',
    1,
    GETDATE(),
    NULL
),

(
    N'Review AirPods Pro 2 sau 1 tháng sử dụng',
    N'Sau 1 tháng sử dụng AirPods Pro 2 trong môi trường học tập và làm việc, mình có một số đánh giá chi tiết như sau:

1. Chất lượng âm thanh:
   - Âm thanh cân bằng, không quá thiên bass
   - Nghe nhạc, podcast và học online đều rất tốt

2. Chống ồn (ANC):
   - Hoạt động cực kỳ hiệu quả trong quán cà phê
   - Có thể loại bỏ ~80-90% tiếng ồn môi trường

3. Transparency Mode:
   - Rất hữu ích khi cần nghe môi trường xung quanh
   - Tự nhiên hơn thế hệ trước

4. Pin:
   - ~6 giờ nghe liên tục
   - Hộp sạc nâng tổng thời gian lên ~30 giờ

5. Trải nghiệm hệ sinh thái:
   - Kết nối nhanh với iPhone, iPad
   - Chuyển đổi thiết bị mượt

Nhược điểm:
- Giá cao
- Không tối ưu cho Android

Kết luận: Đây là lựa chọn tốt nhất nếu bạn đang dùng hệ sinh thái Apple.',
    N'Review thực tế AirPods Pro 2',
    3,
    'APPROVED',
    1,
    GETDATE(),
    NULL
),

(
    N'Laptop sinh viên nên mua năm 2026',
    N'Việc chọn laptop cho sinh viên năm 2026 không chỉ phụ thuộc vào giá mà còn phụ thuộc vào ngành học và nhu cầu sử dụng.

1. Sinh viên CNTT:
   - Cần CPU mạnh (Intel i5/i7 hoặc Apple M2 trở lên)
   - RAM tối thiểu 16GB
   - SSD 512GB

2. Sinh viên kinh tế/văn phòng:
   - Ưu tiên nhẹ, pin lâu
   - RAM 8-16GB là đủ

3. Gợi ý:
   - MacBook Air M2: nhẹ, pin lâu, phù hợp đa số
   - Dell XPS 13: thiết kế đẹp, màn hình chất lượng
   - ASUS Vivobook: giá tốt

4. Lưu ý khi mua:
   - Không nên mua laptop dưới 8GB RAM
   - Nên chọn SSD thay vì HDD
   - Ưu tiên máy có pin > 8 giờ

Kết luận: MacBook Air M2 là lựa chọn cân bằng nhất giữa hiệu năng và pin.',
    N'Gợi ý laptop đáng mua cho sinh viên',
    4,
    'APPROVED',
    1,
    GETDATE(),
    NULL
),

(
    N'So sánh Apple Watch Series 9 và Galaxy Watch 6',
    N'Apple Watch Series 9 và Galaxy Watch 6 là hai mẫu smartwatch nổi bật nhất năm 2026.

1. Thiết kế:
   - Apple Watch: vuông, hiện đại
   - Galaxy Watch: tròn, giống đồng hồ truyền thống

2. Hệ điều hành:
   - Apple Watch: watchOS (chỉ dùng tốt với iPhone)
   - Galaxy Watch: WearOS (tương thích Android)

3. Tính năng:
   - Cả hai đều có đo nhịp tim, SpO2, theo dõi giấc ngủ
   - Apple Watch mạnh hơn về app ecosystem
   - Galaxy Watch có pin tốt hơn

4. Pin:
   - Apple Watch: ~1-2 ngày
   - Galaxy Watch: ~2-3 ngày

5. Giá:
   - Apple Watch cao hơn

Kết luận:
- Dùng iPhone → Apple Watch
- Dùng Android → Galaxy Watch',
    N'So sánh hai mẫu đồng hồ thông minh phổ biến',
    4,
    'APPROVED',
    1,
    GETDATE(),
    NULL
),

(
    N'Trải nghiệm Logitech MX Master 3S',
    N'Logitech MX Master 3S là một trong những con chuột tốt nhất cho dân văn phòng và designer.

1. Thiết kế:
   - Công thái học, ôm tay
   - Phù hợp sử dụng lâu

2. Hiệu năng:
   - DPI cao, tracking tốt trên nhiều bề mặt
   - Silent click (bấm êm)

3. Tính năng:
   - Scroll wheel thông minh
   - Kết nối nhiều thiết bị

4. Nhược điểm:
   - Giá cao
   - Không phù hợp gaming

Kết luận:
Nếu bạn làm việc nhiều trên máy tính → rất đáng đầu tư.',
    N'Đánh giá chuột Logitech MX Master 3S',
    3,
    'APPROVED',
    1,
    GETDATE(),
    NULL
);
GO

-- =========================================
-- 11. POST_IMAGES
-- =========================================
INSERT INTO PostImages (PostID, ImageURL, IsMain, DisplayOrder)
VALUES 
(1, 'post1.jpg', 1, 1),
(1, 'post1_2.jpg', 0, 2),
(2, 'post2.jpg', 1, 1),
(3, 'post-laptop-2026-1.jpg', 1, 1),
(3, 'post-laptop-2026-2.jpg', 0, 2),
(4, 'post-smartwatch-1.jpg', 1, 1),
(4, 'post-smartwatch-2.jpg', 0, 2),
(5, 'post-mx-master-3s-1.jpg', 1, 1);
GO

-- =========================================
-- 12. COMMENTS
-- Bình luận bài viết
-- =========================================
INSERT INTO Comments (UserID, PostID, Content)
VALUES 
(2, 1, N'Bài viết rất hữu ích cho người mới mua tai nghe'),
(3, 1, N'Mình đã dùng Sony và thấy rất ổn'),
(2, 2, N'Mong bài này sớm được duyệt'),
(2, 3, N'Mình đang cần mua laptop đi học, bài viết rất hữu ích'),
(3, 3, N'MacBook Air M2 khá phù hợp sinh viên CNTT'),
(2, 4, N'Mình dùng Android nên quan tâm Galaxy Watch 6 hơn'),
(2, 4, N'Apple Watch mạnh khi dùng với iPhone'),
(3, 5, N'Mong bài đánh giá chuột này sớm được duyệt');
GO

-- =========================================
-- 13. PROMOTIONS
-- =========================================
INSERT INTO Promotions (PromotionName, DiscountPercent, StartDate, EndDate)
VALUES 
(N'Giảm giá hè', 10, GETDATE(), DATEADD(DAY, 30, GETDATE())),
(N'Sale cuối năm', 20, GETDATE(), DATEADD(DAY, 45, GETDATE())),
(N'Back to School', 15, GETDATE(), DATEADD(DAY, 60, GETDATE())),
(N'Ưu đãi phụ kiện', 12, GETDATE(), DATEADD(DAY, 40, GETDATE())),
(N'Sale laptop cao cấp', 8, GETDATE(), DATEADD(DAY, 35, GETDATE()));
GO

-- =========================================
-- 14. PRODUCT_PROMOTIONS
-- =========================================
INSERT INTO ProductPromotions (ProductID, PromotionID)
VALUES 
(1, 1),
(2, 2),
(5, 3),
(6, 5),
(9, 4),
(10, 4),
(11, 4),
(12, 4),
(14, 3);
GO

-- =========================================
-- 15. POST_PRODUCTS
-- =========================================
INSERT INTO PostProducts (PostID, ProductID, DisplayOrder, Note)
VALUES 
(1, 1, 1, N'Phù hợp người thích hệ sinh thái Apple'),
(1, 2, 2, N'Phù hợp người cần chống ồn mạnh'),
(2, 1, 1, N'Sản phẩm được trải nghiệm thực tế'),
(3, 5, 1, N'Phù hợp sinh viên cần máy nhẹ, pin lâu'),
(3, 6, 2, N'Phù hợp người cần laptop Windows cao cấp'),
(4, 7, 1, N'Phù hợp người dùng iPhone'),
(4, 8, 2, N'Phù hợp người dùng Android'),
(5, 11, 1, N'Chuột phù hợp làm việc văn phòng và thiết kế');
GO

-- =========================================
-- 16. VOUCHERS
-- Chú ý bảng này của bạn đang cần VoucherName
-- =========================================
INSERT INTO Vouchers
(
    VoucherCode,
    VoucherName,
    DiscountPercent,
    TargetRole
)
VALUES 
('VIP10', N'Ưu đãi khách hàng thân thiết 10%', 10, 'LOYAL_CUSTOMER'),
('VIP20', N'Ưu đãi khách hàng thân thiết 20%', 20, 'LOYAL_CUSTOMER'),
('STUDENT15', N'Ưu đãi sinh viên 15%', 15, 'LOYAL_CUSTOMER'),
('WRITER10', N'Ưu đãi cộng tác viên 10%', 10, 'LOYAL_CUSTOMER'),
('LOYAL30', N'Ưu đãi khách hàng thân thiết 30%', 30, 'LOYAL_CUSTOMER');
GO

-- =========================================
-- 17. USER_VOUCHERS
-- =========================================
INSERT INTO UserVouchers (UserID, VoucherID)
VALUES 
(3, 1),
(3, 2);
GO




INSERT INTO PostProducts (PostID, ProductID, DisplayOrder, Note)
SELECT 1, 1, 1, N'Sản phẩm được nhắc đến chính trong bài viết'
WHERE NOT EXISTS (SELECT 1 FROM PostProducts WHERE PostID = 1 AND ProductID = 1);

INSERT INTO PostProducts (PostID, ProductID, DisplayOrder, Note)
SELECT 1, 2, 2, N'Sản phẩm so sánh thêm'
WHERE NOT EXISTS (SELECT 1 FROM PostProducts WHERE PostID = 1 AND ProductID = 2);

INSERT INTO PostProducts (PostID, ProductID, DisplayOrder, Note)
SELECT 2, 2, 1, N'Sản phẩm được review chính'
WHERE NOT EXISTS (SELECT 1 FROM PostProducts WHERE PostID = 2 AND ProductID = 2);

INSERT INTO PostProducts (PostID, ProductID, DisplayOrder, Note)
SELECT 3, 1, 1, N'Sản phẩm gợi ý tham khảo'
WHERE NOT EXISTS (SELECT 1 FROM PostProducts WHERE PostID = 3 AND ProductID = 1);

INSERT INTO PostProducts (PostID, ProductID, DisplayOrder, Note)
SELECT 3, 2, 2, N'Sản phẩm phù hợp để so sánh'
WHERE NOT EXISTS (SELECT 1 FROM PostProducts WHERE PostID = 3 AND ProductID = 2);
GO

UPDATE Users
SET Password = '$2a$10$M5P6L2lCNNuD8ZczDVdJSeAyx/jzbWVCrqfnK9QRhNOk9ZK5UOMB6'
WHERE Email = 'admin@gmail.com';


VALUES 
(N'Quản trị viên', 'admin@gmail.com', '123456', '0900000001', N'Đà Nẵng', 'ADMIN'),
(N'Nguyễn Văn A', 'user1@gmail.com', '123456', '0900000002', N'Quy Nhơn', 'CUSTOMER'),
(N'Lê Văn B', 'user2@gmail.com', '123456', '0900000003', N'Hà Nội', 'LOYAL_CUSTOMER'),
(N'Cộng tác viên A', 'writer@gmail.com', '123456', '0900000004', N'Hồ Chí Minh', 'WRITER');
GO

SELECT UserID, FullName, Email, Password, Role, IsActive
FROM Users
WHERE Email = 'admin@gmail.com';

UPDATE Users
SET Password = '$2a$10$f7HTUFmzbvX0rIIFz/YYxO0OoFgeusSzBfHyqjf3SeTQSU/mHPGfa'
WHERE Email = 'user2@gmail.com';

UPDATE Posts
SET 
    Status = 'APPROVED',
    ApprovedBy = 1,
    ApprovedAt = GETDATE(),
    RejectReason = NULL
WHERE Status = 'PENDING';
GO

USE WebBanHang;
GO

-- 1. Tạo voucher cho các bài APPROVED của CUSTOMER / LOYAL_CUSTOMER
INSERT INTO Vouchers (
    VoucherCode,
    VoucherName,
    DiscountPercent,
    DiscountAmount,
    MinOrderValue,
    TargetRole,
    Quantity,
    StartDate,
    EndDate,
    IsActive
)
SELECT
    CONCAT('POST10_', p.PostID, '_', p.CreatedBy),
    N'Voucher thưởng bài viết được duyệt',
    10,
    NULL,
    0,
    u.Role,
    1,
    GETDATE(),
    DATEADD(DAY, 30, GETDATE()),
    1
FROM Posts p
JOIN Users u ON u.UserID = p.CreatedBy
WHERE p.Status = 'APPROVED'
  AND u.Role IN ('CUSTOMER', 'LOYAL_CUSTOMER')
  AND NOT EXISTS (
      SELECT 1
      FROM Vouchers v
      WHERE v.VoucherCode = CONCAT('POST10_', p.PostID, '_', p.CreatedBy)
  );
GO

-- 2. Gán voucher cho đúng user viết bài
INSERT INTO UserVouchers (
    UserID,
    VoucherID,
    IsUsed,
    AssignedAt
)
SELECT
    p.CreatedBy,
    v.VoucherID,
    0,
    GETDATE()
FROM Posts p
JOIN Users u ON u.UserID = p.CreatedBy
JOIN Vouchers v 
    ON v.VoucherCode = CONCAT('POST10_', p.PostID, '_', p.CreatedBy)
WHERE p.Status = 'APPROVED'
  AND u.Role IN ('CUSTOMER', 'LOYAL_CUSTOMER')
  AND NOT EXISTS (
      SELECT 1
      FROM UserVouchers uv
      WHERE uv.UserID = p.CreatedBy
        AND uv.VoucherID = v.VoucherID
  );
GO

USE WebBanHang;
GO

-- Xem constraint PaymentMethod hiện tại
SELECT name, definition
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('Orders')
  AND definition LIKE '%PaymentMethod%';
GO

ALTER TABLE Orders
DROP CONSTRAINT CK__Orders__PaymentM__73BA3083;
GO

ALTER TABLE Orders
ADD CONSTRAINT CK_Orders_PaymentMethod
CHECK (PaymentMethod IN ('COD', 'MOMO', 'BANK_TRANSFER'));
GO

USE WebBanHang;
GO

SELECT name, definition
FROM sys.check_constraints
WHERE name = 'CK_Orders_PaymentMethod';
GO