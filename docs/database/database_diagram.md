# Electronic Store Database Diagram

This document contains the database schema diagram for the Electronic Store application, based on the `schema.sql` file.

## Entity Relationship Diagram

```mermaid
erDiagram
    %% User Management
    USER {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR(50) username UK "NOT NULL, UNIQUE"
        VARCHAR(255) password "NOT NULL"
        VARCHAR(100) email UK "NOT NULL, UNIQUE"
        TIMESTAMP created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    ROLE {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR(50) name UK "NOT NULL, UNIQUE"
    }

    PERMISSION {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR(100) name UK "NOT NULL, UNIQUE"
        VARCHAR(255) description "NOT NULL"
        VARCHAR(50) resource "NOT NULL"
        VARCHAR(50) action "NOT NULL"
    }

    USER_ROLES {
        BIGINT user_id PK,FK
        BIGINT role_id PK,FK
    }

    ROLE_PERMISSIONS {
        BIGINT role_id PK,FK
        BIGINT permission_id PK,FK
    }

    %% Product Management
    PRODUCT {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR(255) name "NOT NULL"
        TEXT description
        DECIMAL price
        INTEGER stock
        VARCHAR category
        BOOLEAN availability
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% Deal Management
    DEAL_TYPE {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR(255) name "NOT NULL"
        TEXT description
        VARCHAR(100) strategy_class
    }

    DEAL {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT product_id FK "NOT NULL"
        BIGINT deal_type_id FK "NOT NULL"
        DECIMAL discount_percent
        DECIMAL discount_amount
        INTEGER minimum_quantity
        TIMESTAMP expiration_date
        TIMESTAMP created_at
    }

    %% Shopping Basket
    BASKET {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT user_id FK "NOT NULL"
        VARCHAR(20) status "NOT NULL, DEFAULT 'ACTIVE'"
        TIMESTAMP created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    BASKET_ITEM {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT basket_id FK "NOT NULL"
        BIGINT product_id FK "NOT NULL"
        INTEGER quantity "NOT NULL, DEFAULT 1"
    }

    %% Order Management
    ORDER_TABLE {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT user_id FK "NOT NULL"
        DECIMAL total_amount
        DECIMAL total_discount
        DECIMAL final_amount
        TIMESTAMP order_date
        TEXT note
    }

    ORDER_ITEM {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT order_id FK "NOT NULL"
        BIGINT product_id FK "NOT NULL"
        INTEGER quantity "NOT NULL"
        DECIMAL unit_price "NOT NULL"
        DECIMAL total_price "NOT NULL"
        DECIMAL discount_applied "DEFAULT 0"
    }

    %% Relationships
    USER ||--o{ USER_ROLES : "has"
    ROLE ||--o{ USER_ROLES : "assigned to"
    ROLE ||--o{ ROLE_PERMISSIONS : "has"
    PERMISSION ||--o{ ROLE_PERMISSIONS : "granted to"

    PRODUCT ||--o{ DEAL : "has deals"
    DEAL_TYPE ||--o{ DEAL : "defines"

    USER ||--o{ BASKET : "owns"
    BASKET ||--o{ BASKET_ITEM : "contains"
    PRODUCT ||--o{ BASKET_ITEM : "added to"

    USER ||--o{ ORDER_TABLE : "places"
    ORDER_TABLE ||--o{ ORDER_ITEM : "contains"
    PRODUCT ||--o{ ORDER_ITEM : "ordered as"
```

## Table Descriptions

### User Management Tables

#### USER
- **Purpose**: Stores user account information
- **Key Features**: 
  - Unique username and email constraints
  - Password storage (encrypted)
  - Account creation timestamp

#### ROLE
- **Purpose**: Defines user roles (e.g., ADMIN, CUSTOMER)
- **Key Features**: 
  - Unique role names
  - Simple role-based access control

#### PERMISSION
- **Purpose**: Defines granular permissions for actions
- **Key Features**: 
  - Resource-action based permissions
  - Descriptive permission names
  - Flexible permission system

#### USER_ROLES (Junction Table)
- **Purpose**: Many-to-many relationship between users and roles
- **Key Features**: 
  - Composite primary key
  - Cascade delete on user/role removal

#### ROLE_PERMISSIONS (Junction Table)
- **Purpose**: Many-to-many relationship between roles and permissions
- **Key Features**: 
  - Composite primary key
  - Cascade delete on role/permission removal

### Product Management Tables

#### PRODUCT
- **Purpose**: Stores product catalog information
- **Key Features**: 
  - Product details (name, description, price)
  - Inventory tracking (stock, availability)
  - Category-based organization
  - Audit timestamps

### Deal Management Tables

#### DEAL_TYPE
- **Purpose**: Defines types of deals/promotions
- **Key Features**: 
  - Strategy pattern implementation
  - Flexible deal type system

#### DEAL
- **Purpose**: Stores active deals and promotions
- **Key Features**: 
  - Product-specific deals
  - Multiple discount types (percentage/fixed amount)
  - Minimum quantity requirements
  - Expiration date tracking

### Shopping System Tables

#### BASKET
- **Purpose**: User shopping basket/cart
- **Key Features**: 
  - User-specific baskets
  - Status tracking (ACTIVE, etc.)
  - Session management

#### BASKET_ITEM
- **Purpose**: Items in user's shopping basket
- **Key Features**: 
  - Product-basket relationship
  - Quantity tracking
  - Unique constraint per basket-product pair

### Order Management Tables

#### ORDER_TABLE
- **Purpose**: Completed customer orders
- **Key Features**: 
  - Financial totals (amount, discount, final)
  - Order date tracking
  - Optional notes

#### ORDER_ITEM
- **Purpose**: Individual items within an order
- **Key Features**: 
  - Product details at time of order
  - Pricing and discount tracking
  - Quantity and totals

## Key Relationships

1. **User Management**: Users can have multiple roles, roles can have multiple permissions
2. **Product Deals**: Products can have multiple deals, deals are of specific types
3. **Shopping Flow**: Users have baskets → baskets contain items → items reference products
4. **Order Flow**: Users place orders → orders contain items → items reference products
5. **Business Logic**: Deals apply to products and affect order pricing

## Database Features

- **Primary Keys**: All tables use auto-incrementing BIGINT primary keys
- **Foreign Key Constraints**: Proper referential integrity with cascade deletes where appropriate
- **Unique Constraints**: Prevent duplicate usernames, emails, and role names
- **Default Values**: Sensible defaults for timestamps, quantities, and status fields
- **Data Types**: Appropriate types for financial data (DECIMAL), text content, and timestamps

## Notes

- The `USER` table is quoted because "user" is a reserved keyword in some databases
- Financial amounts use DECIMAL type to avoid floating-point precision issues
- Timestamps are used for audit trails and business logic (deal expiration)
- The design supports both percentage and fixed-amount discounts
- Basket items have unique constraints to prevent duplicate products in the same basket
