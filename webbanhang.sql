-- =========================================
-- DATABASE WEB BÁN HÀNG NỘI DUNG CHI PHỐI
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
-- =========================================
CREATE TABLE Users (
    UserID      INT IDENTITY(1,1) PRIMARY KEY,
    FullName    NVARCHAR(100),
    Email       NVARCHAR(100) NOT NULL UNIQUE,
    Password    NVARCHAR(255) NOT NULL,
    Phone       NVARCHAR(20),
    Address     NVARCHAR(255),
    Role        NVARCHAR(20) NOT NULL DEFAULT 'USER'
                CHECK (Role IN ('USER', 'ADMIN')),
    CreatedAt   DATETIME NOT NULL DEFAULT GETDATE()
);
GO

-- =========================================
-- 2. CATEGORIES
-- =========================================
CREATE TABLE Categories (
    CategoryID    INT IDENTITY(1,1) PRIMARY KEY,
    CategoryName  NVARCHAR(100) NOT NULL,
    Description   NVARCHAR(255)
);
GO

-- =========================================
-- 3. PRODUCTS
-- =========================================
CREATE TABLE Products (
    ProductID     INT IDENTITY(1,1) PRIMARY KEY,
    ProductName   NVARCHAR(255) NOT NULL,
    Description   NVARCHAR(MAX),
    Price         DECIMAL(18,2) NOT NULL CHECK (Price >= 0),
    Stock         INT NOT NULL DEFAULT 0 CHECK (Stock >= 0),
    CategoryID    INT NOT NULL,
    CreatedAt     DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Products_Categories
        FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID)
);
GO

-- =========================================
-- 4. PRODUCT_IMAGES
-- 1 sản phẩm có nhiều ảnh
-- =========================================
CREATE TABLE ProductImages (
    ImageID       INT IDENTITY(1,1) PRIMARY KEY,
    ProductID     INT NOT NULL,
    ImageURL      NVARCHAR(255) NOT NULL,
    IsMain        BIT NOT NULL DEFAULT 0,
    DisplayOrder  INT NOT NULL DEFAULT 1,

    CONSTRAINT FK_ProductImages_Products
        FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);
GO

-- =========================================
-- 5. CARTS
-- 1 user có 1 cart
-- =========================================
CREATE TABLE Carts (
    CartID        INT IDENTITY(1,1) PRIMARY KEY,
    UserID        INT NOT NULL UNIQUE,
    CreatedAt     DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Carts_Users
        FOREIGN KEY (UserID) REFERENCES Users(UserID)
);
GO

-- =========================================
-- 6. CART_ITEMS
-- =========================================
CREATE TABLE CartItems (
    CartItemID    INT IDENTITY(1,1) PRIMARY KEY,
    CartID        INT NOT NULL,
    ProductID     INT NOT NULL,
    Quantity      INT NOT NULL DEFAULT 1 CHECK (Quantity > 0),

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
    OrderID          INT IDENTITY(1,1) PRIMARY KEY,
    UserID           INT NOT NULL,
    TotalAmount      DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (TotalAmount >= 0),
    Status           NVARCHAR(50) NOT NULL DEFAULT 'Pending',
    ReceiverName     NVARCHAR(100),
    ReceiverPhone    NVARCHAR(20),
    ShippingAddress  NVARCHAR(255),
    PaymentMethod    NVARCHAR(50),
    PaymentStatus    NVARCHAR(50) NOT NULL DEFAULT 'Unpaid',
    Note             NVARCHAR(255),
    CreatedAt        DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Orders_Users
        FOREIGN KEY (UserID) REFERENCES Users(UserID)
);
GO

-- =========================================
-- 8. ORDER_DETAILS
-- =========================================
CREATE TABLE OrderDetails (
    OrderDetailID   INT IDENTITY(1,1) PRIMARY KEY,
    OrderID         INT NOT NULL,
    ProductID       INT NOT NULL,
    Quantity        INT NOT NULL CHECK (Quantity > 0),
    Price           DECIMAL(18,2) NOT NULL CHECK (Price >= 0),

    CONSTRAINT FK_OrderDetails_Orders
        FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),

    CONSTRAINT FK_OrderDetails_Products
        FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);
GO

-- =========================================
-- 9. REVIEWS
-- Đánh giá sản phẩm
-- =========================================
CREATE TABLE Reviews (
    ReviewID       INT IDENTITY(1,1) PRIMARY KEY,
    UserID         INT NOT NULL,
    ProductID      INT NOT NULL,
    Rating         INT NOT NULL CHECK (Rating BETWEEN 1 AND 5),
    Comment        NVARCHAR(MAX),
    CreatedAt      DATETIME NOT NULL DEFAULT GETDATE(),

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
-- =========================================
CREATE TABLE Posts (
    PostID         INT IDENTITY(1,1) PRIMARY KEY,
    Title          NVARCHAR(255) NOT NULL,
    Content        NVARCHAR(MAX) NOT NULL,
    Summary        NVARCHAR(500),
    CreatedBy      INT NOT NULL,
    CreatedAt      DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Posts_Users
        FOREIGN KEY (CreatedBy) REFERENCES Users(UserID)
);
GO

-- =========================================
-- 11. POST_IMAGES
-- 1 bài viết có nhiều ảnh
-- =========================================
CREATE TABLE PostImages (
    ImageID        INT IDENTITY(1,1) PRIMARY KEY,
    PostID         INT NOT NULL,
    ImageURL       NVARCHAR(255) NOT NULL,
    IsMain         BIT NOT NULL DEFAULT 0,
    DisplayOrder   INT NOT NULL DEFAULT 1,

    CONSTRAINT FK_PostImages_Posts
        FOREIGN KEY (PostID) REFERENCES Posts(PostID)
);
GO

-- =========================================
-- 12. COMMENTS
-- Bình luận bài viết, không phải bình luận sản phẩm
-- =========================================
CREATE TABLE Comments (
    CommentID      INT IDENTITY(1,1) PRIMARY KEY,
    UserID         INT NOT NULL,
    PostID         INT NOT NULL,
    Content        NVARCHAR(MAX) NOT NULL,
    CreatedAt      DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Comments_Users
        FOREIGN KEY (UserID) REFERENCES Users(UserID),

    CONSTRAINT FK_Comments_Posts
        FOREIGN KEY (PostID) REFERENCES Posts(PostID)
);
GO

-- =========================================
-- 13. PROMOTIONS
-- =========================================
CREATE TABLE Promotions (
    PromotionID       INT IDENTITY(1,1) PRIMARY KEY,
    PromotionName     NVARCHAR(255) NOT NULL,
    DiscountPercent   INT NOT NULL CHECK (DiscountPercent BETWEEN 0 AND 100),
    StartDate         DATETIME,
    EndDate           DATETIME
);
GO

-- =========================================
-- 14. PRODUCT_PROMOTIONS
-- =========================================
CREATE TABLE ProductPromotions (
    ID             INT IDENTITY(1,1) PRIMARY KEY,
    ProductID      INT NOT NULL,
    PromotionID    INT NOT NULL,

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
    ID             INT IDENTITY(1,1) PRIMARY KEY,
    PostID         INT NOT NULL,
    ProductID      INT NOT NULL,
    DisplayOrder   INT NOT NULL DEFAULT 1,
    Note           NVARCHAR(255),

    CONSTRAINT FK_PostProducts_Posts
        FOREIGN KEY (PostID) REFERENCES Posts(PostID),

    CONSTRAINT FK_PostProducts_Products
        FOREIGN KEY (ProductID) REFERENCES Products(ProductID),

    CONSTRAINT UQ_PostProducts UNIQUE (PostID, ProductID)
);
GO
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
-- =========================================
INSERT INTO Posts
(
    Title,
    Summary,
    Content,
    CreatedBy,
    Status,
    ApprovedBy,
    ApprovedAt,
    RejectReason
)
VALUES 
(
    N'Top tai nghe tốt nhất 2026',
    N'Danh sách tai nghe nổi bật năm 2026',
    N'Bài viết tổng hợp những mẫu tai nghe đáng mua dành cho sinh viên và dân văn phòng.',
    1,
    'APPROVED',
    1,
    GETDATE(),
    NULL
),
(
    N'Review AirPods Pro 2 sau 1 tháng sử dụng',
    N'Review thực tế AirPods Pro 2',
    N'Bài viết chia sẻ trải nghiệm thực tế sau khi sử dụng AirPods Pro 2 trong học tập và giải trí.',
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
-- =========================================
INSERT INTO Comments (UserID, PostID, ParentCommentID, Content)
VALUES 
(2, 1, NULL, N'Bài viết rất hữu ích cho người mới mua tai nghe'),
(3, 1, NULL, N'Mình đã dùng Sony và thấy rất ổn'),
(2, 2, NULL, N'Mong bài này sớm được duyệt');
GO

-- =========================================
-- 13. PROMOTIONS
-- =========================================
INSERT INTO Promotions
(
    PromotionName,
    DiscountPercent,
    DiscountAmount,
    TargetRole,
    StartDate,
    EndDate,
    IsActive
)
VALUES 
(
    N'Giảm giá hè',
    10,
    NULL,
    'ALL',
    GETDATE(),
    DATEADD(DAY, 30, GETDATE()),
    1
),
(
    N'Ưu đãi khách hàng thân thiết',
    15,
    NULL,
    'LOYAL_CUSTOMER',
    GETDATE(),
    DATEADD(DAY, 45, GETDATE()),
    1
);
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
-- =========================================
INSERT INTO Vouchers
(
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
VALUES 
(
    'VIP10',
    N'Ưu đãi khách hàng thân thiết 10%',
    10,
    NULL,
    1000000,
    'LOYAL_CUSTOMER',
    100,
    GETDATE(),
    DATEADD(DAY, 30, GETDATE()),
    1
),
(
    'VIP20',
    N'Ưu đãi khách hàng thân thiết 20%',
    20,
    NULL,
    3000000,
    'LOYAL_CUSTOMER',
    50,
    GETDATE(),
    DATEADD(DAY, 30, GETDATE()),
    1
);
GO

-- =========================================
-- 17. USER_VOUCHERS
-- =========================================
INSERT INTO UserVouchers (UserID, VoucherID, IsUsed)
VALUES 
(3, 1, 0),
(3, 2, 0);
GO