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