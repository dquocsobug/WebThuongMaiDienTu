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
(N'Phụ kiện', N'Phụ kiện công nghệ');
GO

-- =========================================
-- 3. PRODUCTS
-- =========================================
INSERT INTO Products (ProductName, Description, Price, Stock, CategoryID)
VALUES 
(N'AirPods Pro 2', N'Tai nghe chống ồn cao cấp của Apple', 5500000, 50, 1),
(N'Sony WH-1000XM5', N'Tai nghe chống ồn cao cấp của Sony', 8000000, 30, 1),
(N'iPhone 15', N'Điện thoại Apple hiệu năng cao', 25000000, 20, 2),
(N'Sạc nhanh 20W', N'Củ sạc nhanh dành cho điện thoại', 300000, 100, 3);
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
(4, 'charger1.jpg', 1, 1);
GO

-- =========================================
-- 5. CARTS
-- =========================================
INSERT INTO Carts (UserID)
VALUES (2), (3);
GO

-- =========================================
-- 6. CART_ITEMS
-- =========================================
INSERT INTO CartItems (CartID, ProductID, Quantity)
VALUES 
(1, 1, 1),
(1, 4, 2),
(2, 2, 1);
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
    N'Bài viết tổng hợp những mẫu tai nghe đáng mua dành cho sinh viên và dân văn phòng.',
    N'Danh sách tai nghe nổi bật năm 2026',
    1,
    'APPROVED',
    1,
    GETDATE(),
    NULL
),
(
    N'Review AirPods Pro 2 sau 1 tháng sử dụng',
    N'Bài viết chia sẻ trải nghiệm thực tế sau khi sử dụng AirPods Pro 2 trong học tập và giải trí.',
    N'Review thực tế AirPods Pro 2',
    3,
    'PENDING',
    NULL,
    NULL,
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
(2, 'post2.jpg', 1, 1);
GO

-- =========================================
-- 12. COMMENTS
-- Bình luận bài viết
-- =========================================
INSERT INTO Comments (UserID, PostID, Content)
VALUES 
(2, 1, N'Bài viết rất hữu ích cho người mới mua tai nghe'),
(3, 1, N'Mình đã dùng Sony và thấy rất ổn'),
(2, 2, N'Mong bài này sớm được duyệt');
GO

-- =========================================
-- 13. PROMOTIONS
-- =========================================
INSERT INTO Promotions (PromotionName, DiscountPercent, StartDate, EndDate)
VALUES 
(N'Giảm giá hè', 10, GETDATE(), DATEADD(DAY, 30, GETDATE())),
(N'Sale cuối năm', 20, GETDATE(), DATEADD(DAY, 45, GETDATE()));
GO

-- =========================================
-- 14. PRODUCT_PROMOTIONS
-- =========================================
INSERT INTO ProductPromotions (ProductID, PromotionID)
VALUES 
(1, 1),
(2, 2);
GO

-- =========================================
-- 15. POST_PRODUCTS
-- =========================================
INSERT INTO PostProducts (PostID, ProductID, DisplayOrder, Note)
VALUES 
(1, 1, 1, N'Phù hợp người thích hệ sinh thái Apple'),
(1, 2, 2, N'Phù hợp người cần chống ồn mạnh'),
(2, 1, 1, N'Sản phẩm được trải nghiệm thực tế');
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
('VIP20', N'Ưu đãi khách hàng thân thiết 20%', 20, 'LOYAL_CUSTOMER');
GO

-- =========================================
-- 17. USER_VOUCHERS
-- =========================================
INSERT INTO UserVouchers (UserID, VoucherID)
VALUES 
(3, 1),
(3, 2);
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
SET Password = '$2a$10$aPfg88DgwZls4fDehjtfz.WOCvYKQ8iOrLh9wADB.WK3AnBsK60Mi'
WHERE Email = 'writer@gmail.com';