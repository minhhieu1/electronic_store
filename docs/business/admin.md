# Admin Business Logic Documentation

## Overview

The Electronic Store application provides comprehensive admin functionality for managing products, deals, and analyzing business operations. Admin users have elevated permissions to perform CRUD operations on products and deals, with sophisticated discount strategy implementations.

## Admin Product Management

### Core Operations

**Product CRUD Operations:**
- **Create Product**: Add new products with validation for name, description, price, and availability
- **Update Product**: Modify existing product details including pricing and availability status
- **Delete Product**: Remove products from the system (soft delete recommended)
- **Read Product**: View individual product details or paginated product lists

**Product Search & Filtering:**
- Advanced pagination support with configurable page size
- Search by product name, category, and availability status
- Price range filtering for inventory analysis
- Sorting by various criteria (name, price, created date)

### Business Rules

1. **Price Validation**: All product prices must be positive values with 2 decimal precision
2. **Availability Management**: Products can be marked as available/unavailable without deletion
3. **Unique Product Names**: Product names must be unique within the system
4. **Inventory Control**: Admins can manage product visibility to customers

## Admin Deal Management

### Deal Types & Strategies

The system implements a sophisticated **Strategy Pattern** for discount calculations with three main deal types:

#### 1. Percentage Discount Strategy
```java
// Implementation: PercentageDiscountStrategy
```
- **Purpose**: Apply percentage-based discounts to products
- **Business Rules**:
  - Discount percentage: 0.01% to 100%
  - Minimum quantity requirements supported
  - Discount calculated on total item price (quantity × unit price)
  - Precision: 2 decimal places with HALF_UP rounding

**Example**: 20% off on iPhone 15 Pro with minimum quantity of 1
- Customer buys 2 iPhones at $1,000 each
- Total: $2,000 × 20% = $400 discount

#### 2. Fixed Amount Discount Strategy
```java
// Implementation: FixedAmountDiscountStrategy
```
- **Purpose**: Apply fixed dollar amount discounts
- **Business Rules**:
  - Fixed discount amount specified in dollars
  - Discount cannot exceed total item price (safety cap)
  - Minimum quantity requirements supported
  - Zero or negative amounts result in no discount

**Example**: $100 off Samsung Galaxy S24
- Customer buys 1 phone at $800
- Final price: $800 - $100 = $700

#### 3. Buy One Get 50% Off Strategy
```java
// Implementation: BuyOneGetFiftyPercentOffStrategy
```
- **Purpose**: Sophisticated bulk purchase incentive
- **Business Rules**:
  - Default: Buy 2, get 50% off on qualifying additional items
  - Customizable discount percentage (not limited to 50%)
  - Configurable minimum quantity requirements
  - Integer division logic: `discounted_items = total_quantity / minimum_quantity`
  - Default minimum quantity: 2 items

**Example**: Sony Headphones - Buy 2 get 50% off additional pairs
- Customer buys 5 headphones at $200 each
- Calculation: 5 ÷ 2 = 2 discounted items
- Discount: 2 × $200 × 50% = $200 total discount

### Deal Creation & Management

**Deal Creation Process:**
1. **Product Selection**: Choose target product for the deal
2. **Deal Type Selection**: Select appropriate discount strategy
3. **Discount Configuration**: Set percentage, amount, or minimum quantity
4. **Expiration Management**: Set future expiration date (past dates rejected)
5. **Validation**: Prevent duplicate deals (same product + deal type combination)

**Deal Business Rules:**
1. **Expiration Control**: All deals must have future expiration dates
2. **Duplicate Prevention**: Only one active deal per product-deal type combination
3. **Minimum Quantity**: Ranges from 1 to unlimited, defaults vary by strategy
4. **Automatic Expiration**: Expired deals are automatically excluded from calculations

## Discount Engine Architecture

### Core Components

**DiscountEngine (Central Processor):**
- Orchestrates discount calculations across multiple deals
- Handles deal retrieval for products
- Combines multiple discount strategies
- Ensures expired deals are excluded

**Strategy Pattern Implementation:**
```java
public interface DiscountStrategy {
    BigDecimal apply(BasketItem item, Deal deal);
}
```

### Discount Calculation Logic

**Multi-Deal Support:**
- Customers can benefit from multiple deals simultaneously
- Discounts are **additive** (sum of all applicable discounts)
- Each deal type calculates independently
- Final discount cannot exceed item total price

**Calculation Workflow:**
1. Retrieve all active deals for basket items
2. Group deals by product ID
3. For each basket item:
   - Filter expired deals
   - Apply each valid deal using appropriate strategy
   - Sum all discount amounts
4. Return discount map by product ID

## Permission & Security Model

### Role-Based Access Control

**Admin Permissions:**
- `PRODUCT_CREATE`: Create new products
- `PRODUCT_UPDATE`: Modify existing products  
- `PRODUCT_DELETE`: Remove products
- `DEAL_CREATE`: Create promotional deals
- `DEAL_UPDATE`: Modify existing deals
- `DEAL_DELETE`: Remove deals

**Security Implementation:**
```java
@PreAuthorize("hasAuthority('PRODUCT_CREATE')")
public ResponseEntity<ProductResponseDto> createProduct(...)
```

### Authentication Requirements

**JWT Token Authentication:**
- All admin endpoints require valid JWT tokens
- Role validation performed on each request
- Permission-based method security using `@PreAuthorize`

## Pagination & Performance

### Pagination Strategy

**Standard Pagination Parameters:**
- `page`: Zero-based page number (default: 0)
- `size`: Items per page (default: 10, max: 100)
- `sort`: Sorting criteria (e.g., "name,asc" or "price,desc")

**Performance Optimizations:**
- Database-level pagination using Spring Data JPA
- Lazy loading for product-deal relationships
- Efficient query patterns for deal lookups

## Error Handling & Validation

### Business Exception Types

**Product Management Exceptions:**
- `ProductNotFoundException`: Product ID not found
- `DuplicateProductException`: Product name already exists
- `InvalidPriceException`: Negative or invalid price values

**Deal Management Exceptions:**
- `DuplicateDealException`: Attempt to create duplicate active deal
- `ExpiredDealException`: Deal expiration date in the past
- `InvalidDiscountException`: Invalid discount values

### Validation Rules

**Product Validation:**
- Name: Required, 1-255 characters
- Price: Positive decimal with 2 decimal places
- Description: Optional, max 1000 characters

**Deal Validation:**
- Expiration Date: Must be in future (`@Future`)
- Discount Percent: 0.01-100% (`@DecimalMin`, `@DecimalMax`)
- Discount Amount: Positive values only (`@DecimalMin`)
- Minimum Quantity: At least 1 (`@Min`)

## Business Metrics & Analytics

### Key Performance Indicators

**Product Analytics:**
- Total active products
- Products with active deals
- Average discount percentage
- Most popular deal types

**Deal Performance:**
- Active vs expired deals
- Discount utilization rates
- Revenue impact of promotions
- Customer engagement with deals

### Reporting Capabilities

**Admin Dashboard Metrics:**
- Deal expiration monitoring
- Product performance tracking
- Discount effectiveness analysis
- Customer purchase pattern insights

## Integration Points

### Database Schema Integration

**Core Tables:**
- `product`: Product catalog management
- `deal`: Promotional deal configurations
- `deal_type`: Strategy class mappings
- `basket_item`: Customer purchase data

**Relationship Mappings:**
- Product ↔ Deal (One-to-Many)
- Deal ↔ DealType (Many-to-One)
- Deal ↔ BasketItem (calculation context)

### API Integration

**REST Endpoints:**
- `/api/admin/products/**`: Product management
- `/api/admin/deals/**`: Deal management
- Both support full CRUD operations with pagination

### Future Enhancements

**Planned Features:**
1. **Advanced Analytics**: Revenue impact analysis, customer segmentation
2. **Bulk Operations**: Multi-product deal creation, batch updates
3. **Deal Templates**: Reusable deal configurations
4. **A/B Testing**: Deal performance comparison tools
5. **Inventory Integration**: Stock-based deal activation/deactivation

## Summary

The admin business logic provides a comprehensive product and deal management system with:

- **Flexible Product Management**: Full CRUD operations with advanced search capabilities
- **Sophisticated Deal Engine**: Three distinct discount strategies with customizable parameters
- **Robust Security**: Role-based permissions and JWT authentication
- **Performance Optimization**: Efficient pagination and database queries
- **Business Rule Enforcement**: Comprehensive validation and error handling
- **Scalable Architecture**: Strategy pattern enables easy addition of new discount types

This implementation supports complex e-commerce scenarios while maintaining simplicity for administrators and providing excellent customer experiences through flexible promotional offerings.
