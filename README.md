# Electronics Store Backend

A comprehensive enterprise-grade REST API for an electronics store backend built with Spring Boot 3.3.5, featuring JWT authentication, role-based authorization, product management, shopping cart functionality, and sophisticated deal management with multiple discount strategies.

## 📖 Documentation

Comprehensive business logic and technical documentation is available in the `docs/` folder:

- **[Admin Business Logic](docs/business/admin.md)** - Complete admin functionality documentation including product management, deal creation, and discount strategies
- **[Customer Business Logic](docs/business/customer.md)** - Customer experience documentation covering shopping, basket management, and checkout process
- **[Database Schema](docs/database/database_diagram.md)** - Database ERD with detailed table relationships and business logic
- **[Getting Started with Gradle](docs/guideline/start_by_gradlew.md)** - Complete Gradle startup guide with troubleshooting
- **[Getting Started with Docker](docs/guideline/start_by_docker.md)** - Docker deployment guide using Spring Boot buildpacks
- **[API Testing Guide](docs/test/how_to_test.md)** - Comprehensive testing documentation with Swagger UI and Postman examples

## 🏗️ Technical Architecture

### Core Technologies
- **Java 17** - LTS runtime environment
- **Spring Boot 3.3.5** - Enterprise application framework
- **Spring Security 6** - JWT-based authentication and role-based authorization
- **Spring Data JPA** - ORM with Hibernate implementation
- **H2 Database** - In-memory database for development and testing
- **Gradle 8.5** - Build automation and dependency management

### Key Design Patterns
- **Strategy Pattern** - Flexible discount calculation system with three distinct strategies
- **Repository Pattern** - Data access layer abstraction
- **DTO Pattern** - Clean API boundaries with dedicated data transfer objects
- **Builder Pattern** - Complex object construction for entities and DTOs

## 🚀 Features

### 📦 **Database & Persistence**
- H2 in-memory database for development and testing
- Schema and sample data initialization via `schema.sql` and `data.sql`
- JPA/Hibernate for object-relational mapping

### 🛍️ **Product Management (Admin)**
- Create, update, delete, and list products with pagination
- Product filtering by category, price range, and availability
- Product fields: id, name, price, stock, category, availability, description
- Stock management with concurrency safety

### 💰 **Deal Management (Admin)**
- Configurable deal types stored in database
- Support for multiple discount types:
  - Percentage discounts
  - Buy-one-get-50%-off-second deals
  - Fixed amount discounts
- Deal expiration handling
- Link products to deals with expiration dates

### 🛒 **Customer Operations**
- Shopping basket management (add, remove, update quantities)
- Product browsing with filtering and pagination
- Checkout process with automatic deal application
- Stock validation and decrement during checkout
- Order history with detailed receipts

### 🔒 **Security & Authorization**
- JWT-based authentication
- Role-based access control (ADMIN, CUSTOMER)
- Password encryption with BCrypt
- Secure endpoints with method-level security

### 📑 **API Documentation**
- Swagger UI integration for interactive API documentation
- Comprehensive endpoint documentation with examples
- Available at `/swagger-ui.html` when running

### ♻️ **Transactional Integrity**
- Atomic checkout operations with rollback on failure
- Database constraints and foreign key relationships
- Exception handling with custom error messages

## 📋 Prerequisites

- **Java 17 or higher** - Required runtime environment
- **Gradle 8.0+** - Build tool (included via wrapper)
- **Git** - Version control for cloning repository
- **curl or Postman** - Optional for API testing
- **Docker** - Optional for containerized deployment

### Application Access
- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (empty)

## 👥 Default Users

The application comes with pre-configured users:

| Username  | Password | Role     | Email |
|---------- |----------|------    |-------|
| admin     | password | ADMIN    | admin@electronics-store.com |
| customer1 | password | CUSTOMER | customer1@example.com |
| customer2 | password | CUSTOMER | customer2@example.com |

## 📚 API Endpoints

### 🔓 Public Endpoints

#### Products (Browse)
- `GET /api/products` - Get all available products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/categories` - Get all categories

**Example:**
```bash
GET /api/products?page=0&size=10&category=Smartphones&minPrice=500&maxPrice=1000
```

### 🔒 Admin Endpoints (Require ADMIN role)

#### Product Management
- `POST /api/admin/products` - Create new product
- `PUT /api/admin/products/{id}` - Update product
- `DELETE /api/admin/products/{id}` - Delete product
- `GET /api/admin/products` - Get all products (including unavailable)

#### Deal Management
- `GET /api/admin/deals` - Get all active deals
- `GET /api/admin/deals/types` - Get all deal types
- `GET /api/admin/deals/product/{productId}` - Get deals for specific product
- `POST /api/admin/deals` - Create new deal
- `PUT /api/admin/deals/{id}` - Update deal
- `DELETE /api/admin/deals/{id}` - Delete deal

### 🛒 Customer Endpoints (Require CUSTOMER role)

#### Shopping Basket
- `GET /api/customer/basket` - Get current basket
- `POST /api/customer/basket/items?productId={id}&quantity={qty}` - Add item to basket
- `PUT /api/customer/basket/items?productId={id}&quantity={qty}` - Update item quantity
- `DELETE /api/customer/basket/items/{productId}` - Remove item from basket
- `DELETE /api/customer/basket` - Clear entire basket

#### Orders
- `POST /api/customer/orders/checkout` - Process checkout
- `GET /api/customer/orders` - Get order history (paginated)

### Environment-Specific Configuration
- **Development**: H2 in-memory database with console access
- **Testing**: Separate test database with @DataJpaTest support

## 🧪 Testing & API Usage

### Comprehensive Testing Guide
See detailed documentation: **[API Testing Guide](docs/test/how_to_test.md)**

### Run Unit & Integration Tests
```bash
./gradlew test
```

### Swagger UI Testing
Access interactive API documentation at http://localhost:8080/swagger-ui.html with built-in testing capabilities.

## 🏗️ Architecture & Business Logic

### Database Schema & Relationships
See comprehensive documentation: **[Database Schema](docs/database/database_diagram.md)**

**Core Tables:**
- **Users & Roles**: `user`, `role`, `user_roles`, `role_permissions`, `permission`
- **Products**: `product` (with stock management and availability)
- **Deals**: `deal_type`, `deal` (with expiration and strategy pattern)
- **Shopping**: `basket`, `basket_item` (with status management)
- **Orders**: `order_table`, `order_item` (with discount tracking)

### Security Architecture
- **JWT-based Stateless Authentication** - Scalable token-based security
- **Role-based Method-level Security** - Granular permission control
- **BCrypt Password Encryption** - Industry-standard password security
- **CORS Support** - Cross-origin resource sharing for frontend integration

### Discount Engine Architecture
**Strategy Pattern Implementation** with three distinct strategies:

1. **Percentage Discount Strategy**
   - Configurable percentage (0.01% to 100%)
   - Minimum quantity requirements
   - HALF_UP rounding for precise calculations

2. **Fixed Amount Discount Strategy**
   - Dollar amount discounts with safety caps
   - Cannot exceed total item price
   - Immediate savings visibility

3. **Buy One Get 50% Off Strategy**
   - Sophisticated bulk purchase incentives
   - Configurable discount percentages and minimum quantities
   - Integer division logic: `discounted_items = total_quantity / minimum_quantity`

**Multi-Deal Support:**
- Customers benefit from multiple deals simultaneously
- Discounts are additive (sum of all applicable discounts)
- Automatic expiration handling
- Real-time calculation during basket operations

### Business Logic Documentation
- **[Admin Business Logic](docs/business/admin.md)** - Product management, deal creation, discount strategies
- **[Customer Business Logic](docs/business/customer.md)** - Shopping experience, basket management, checkout process

## 🐛 Error Handling & Validation

### Comprehensive Exception Management
- **`ProductNotFoundException`** - 404 Not Found with product ID details
- **`InsufficientStockException`** - 400 Bad Request with available quantity info
- **`BasketNotFoundException`** - 404 Not Found for invalid basket access
- **`DuplicateDealException`** - 400 Bad Request for duplicate deal creation
- **Validation Errors** - 400 Bad Request with detailed field validation messages
- **Authentication Errors** - 401 Unauthorized for invalid credentials
- **Authorization Errors** - 403 Forbidden for insufficient permissions

### Business Rule Enforcement
- **Product Validation**: Name uniqueness, positive pricing, stock management
- **Deal Validation**: Future expiration dates, valid discount ranges, minimum quantities
- **Basket Validation**: Stock availability, quantity limits, user ownership
- **Order Validation**: Complete basket verification, payment processing
