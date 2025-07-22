# How to Test the Electronic Store API

This guide explains how to test the Electronic Store API using either Swagger UI or Postman collections.

## Overview

The Electronic Store API provides a complete e-commerce backend with user authentication, product management, shopping cart functionality, and order processing. You can test the API using two methods:

1. **Swagger UI** - Interactive web interface (Recommended for quick testing)
2. **Postman Collection** - Complete API collection for comprehensive testing

## Prerequisites

Before testing, ensure the application is running:

- **Application started**: Follow the [start_by_gradlew.md](../guideline/start_by_gradlew.md) or [start_by_docker.md](../guideline/start_by_docker.md) guide
- **Application URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## Test Users and Credentials

The application automatically creates test users on startup:

### 🔑 Admin User
- **Username**: `admin`
- **Password**: `password`
- **Email**: `admin@electronics-store.com`
- **Permissions**: Full access to all admin endpoints (product management, deal management, user management, etc.)

### 👤 Customer Users
- **Username**: `customer1` / **Password**: `password` / **Email**: `customer1@example.com`
- **Username**: `customer2` / **Password**: `password` / **Email**: `customer2@example.com`
- **Permissions**: Customer access (view products, manage basket, place orders, view own orders)

## Method 1: Testing with Swagger UI (Recommended)

### Access Swagger UI
1. Start the application
2. Open your browser and go to: **http://localhost:8080/swagger-ui.html**
3. You'll see the interactive API documentation


## Method 2: Testing with Postman

### Import Collection and Environment
1. **Import the collection**:
   - Click **"Import"** in Postman
   - Select the file: `docs/test/postman_collection/Electronics Store API.postman_collection.json`
2. **Import the environment**:
   - Import: `docs/test/postman_collection/Electronics Store Environment.postman_environment.json`
   - Select the **"Electronics Store Environment"** in the top-right dropdown
