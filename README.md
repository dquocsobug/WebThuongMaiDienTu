# 🛒 TechStore E-Commerce Platform

A modern full-stack e-commerce platform built with **Spring Boot**, **React**, and **SQL Server**, featuring AI-powered customer support, role-based authorization, article-driven content management, and a modern admin dashboard.

---

# 🚀 Features

## 👤 Authentication & Authorization

* JWT Authentication
* Role-based authorization
* Customer / Loyal Customer / Admin / Writer roles
* Protected routes and secure APIs

---

## 🛍️ E-Commerce Features

* Product listing and detail pages
* Shopping cart system
* Direct buy & checkout flow
* Order management
* Product reviews and ratings
* Voucher & promotion system
* Online payment integration

  * COD
  * MoMo
  * Bank Transfer (VietQR)

---

## 📰 Content-Driven System

* Article management system
* Writer & Admin moderation workflow
* Featured posts
* Product-related articles
* Comment system with nested replies

---

## 🤖 AI Assistant Integration

Integrated Gemini API-powered AI Assistant capable of:

* Product recommendations
* Voucher guidance
* Payment instructions
* Order status explanation
* Shipping policy support
* Article/content suggestions

The AI Assistant dynamically retrieves real-time data from the database to provide contextual responses based on products, promotions, vouchers, and user orders.

---

# 🧩 Tech Stack

## Frontend

* React
* Vite
* React Router
* Axios
* CSS Modules

## Backend

* Java Spring Boot
* Spring Security
* JWT Authentication
* Hibernate / JPA
* RESTful APIs

## Database

* SQL Server

## AI Integration

* Gemini API

---

# 🏗️ System Architecture

Frontend (React + Vite)
↓
REST API (Spring Boot)
↓
SQL Server Database
↓
Gemini API (AI Assistant)

---

# 📦 Main Modules

* Authentication Module
* Product Module
* Cart Module
* Checkout Module
* Order Module
* Review Module
* Voucher & Promotion Module
* Article & Content Module
* Admin Dashboard
* AI Chatbox Assistant

---

# ⚡ Performance Optimizations

* Lazy Loading Routes
* JWT Request Interceptor
* AI Response Caching
* Optimized Database Queries
* Responsive UI Design

---

# 📸 Screenshots

> Add your screenshots here

* Home Page
* Product Detail
* Checkout
* Admin Dashboard
* AI Assistant
* Article System

---

# 🛠️ Installation

## Backend

```bash
cd backend
mvn spring-boot:run
```

## Frontend

```bash
cd frontend
npm install
npm run dev
```

---

# 🔐 Environment Variables

```properties
# Gemini API
gemini.api.key=YOUR_API_KEY
gemini.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
```

---

# 📚 Learning Outcomes

Through this project, I improved my skills in:

* Full-stack web development
* RESTful API design
* JWT Authentication
* Database design
* Role-based systems
* AI integration with Gemini API
* Responsive frontend development
* System architecture and project organization

---

# 👨‍💻 Author

**Le Duy Quoc**

* Full Stack Developer
* React & Spring Boot Enthusiast
