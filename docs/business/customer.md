# Customer Business Logic Documentation

## Overview

The Electronic Store application provides a comprehensive customer experience with product browsing, shopping basket management, and order processing capabilities. Customers have access to public product information and authenticated shopping functionality with automatic discount application and secure checkout processes.

## Customer Product Browsing

### Public Product Access

**Product Discovery (No Authentication Required):**
- **Browse All Products**: View paginated product catalog with filtering capabilities
- **Product Details**: Access detailed product information including pricing and availability
- **Category Browsing**: Filter products by categories (Electronics, Smartphones, Computers, etc.)
- **Advanced Filtering**: Search by price range, availability status, and product attributes
- **Sorting Options**: Sort by name, price, category (ascending/descending)

**Product Search & Filtering:**
- **Price Range Filtering**: `minPrice` and `maxPrice` parameters for budget-based shopping
- **Availability Filter**: Show only available products or include out-of-stock items
- **Category Navigation**: Browse products by specific categories
- **Pagination Support**: Configurable page size (default: 10, max: 100 items per page)

### Business Rules

1. **Public Access**: Product browsing requires no authentication - accessible to all users
2. **Availability Display**: Out-of-stock products are visible but clearly marked as unavailable
3. **Price Transparency**: All prices displayed include potential discounts for authenticated users
4. **Real-time Stock**: Product availability reflects current inventory levels

## Customer Authentication & Session Management

### Authentication Process

**Login & JWT Token Management:**
- **Username/Password Authentication**: Secure login with BCrypt password verification
- **JWT Token Generation**: Stateless authentication with role-based claims
- **Session Management**: Token-based sessions with configurable expiration
- **Role Assignment**: Automatic CUSTOMER role assignment for standard users

**Security Features:**
```java
// JWT Authentication Flow
POST /api/auth/login
{
  "username": "customer1", 
  "password": "password"
}
// Returns: JWT token with CUSTOMER role
```

**Token Management:**
- **Token Expiration**: Configurable JWT expiration times
- **Token Blacklisting**: Logout functionality invalidates tokens
- **Role Validation**: Each request validates customer permissions

### Customer Permissions

**Standard Customer Permissions:**
- `PRODUCT_READ`: Browse and view product information
- `BASKET_READ`: View shopping basket contents
- `BASKET_CREATE`: Create new shopping baskets
- `BASKET_UPDATE`: Add, remove, and modify basket items
- `BASKET_DELETE`: Clear entire shopping basket
- `ORDER_READ`: View personal order history
- `ORDER_CREATE`: Process checkout and create orders

## Shopping Basket Management

### Basket Lifecycle

**Basket Creation & Management:**
- **Automatic Creation**: New baskets created automatically on first item addition
- **Single Active Basket**: One active basket per customer at any time
- **Persistent Storage**: Baskets persist across login sessions
- **Status Management**: Baskets have ACTIVE, CHECKED_OUT, and EXPIRED statuses

### Core Basket Operations

#### 1. Add Items to Basket
```java
POST /api/customer/basket/items?productId=1&quantity=2
```
**Business Rules:**
- **Stock Validation**: Verify product availability before adding
- **Quantity Limits**: Support for minimum quantity of 1, no maximum limit
- **Duplicate Handling**: Adding existing products increases quantity
- **Price Calculation**: Real-time price calculation with current product pricing

#### 2. Update Item Quantities
```java
PUT /api/customer/basket/items?productId=1&quantity=5
```
**Business Rules:**
- **Quantity Zero**: Setting quantity to 0 removes item from basket
- **Stock Validation**: Ensure sufficient inventory for requested quantity
- **Price Recalculation**: Automatic total recalculation on quantity changes

#### 3. Remove Items from Basket
```java
DELETE /api/customer/basket/items/1
```
**Business Rules:**
- **Complete Removal**: Removes all quantities of specified product
- **Basket Updates**: Automatic basket total recalculation
- **Empty Basket Handling**: Basket remains active even when empty

#### 4. Clear Entire Basket
```java
DELETE /api/customer/basket
```
**Business Rules:**
- **Complete Cleanup**: Removes all items from active basket
- **Basket Preservation**: Basket entity remains for future use
- **Immediate Effect**: Changes are immediately visible

## Discount Application & Deal Processing

### Automatic Discount Calculation

**Real-time Discount Engine:**
- **Automatic Application**: Discounts calculated automatically during basket operations
- **Multi-Deal Support**: Customers benefit from multiple simultaneous deals
- **Additive Discounts**: All applicable discounts are summed together
- **Expiration Handling**: Expired deals automatically excluded from calculations

### Deal Types Available to Customers

#### 1. Percentage Discounts
**Customer Experience:**
- Automatic percentage reduction on qualifying products
- Minimum quantity requirements clearly displayed
- Real-time discount calculation in basket totals

**Example Scenario:**
- iPhone 15 Pro with 10% discount (minimum quantity: 1)
- Customer adds 2 iPhones at $1,200 each
- Discount Applied: $2,400 × 10% = $240 savings
- Final Price: $2,160

#### 2. Fixed Amount Discounts
**Customer Experience:**
- Direct dollar amount reduction from product price
- Cannot exceed total item price (customer protection)
- Immediate savings visibility in basket

**Example Scenario:**
- Samsung Galaxy S24 with $100 fixed discount
- Customer adds 1 phone at $800
- Discount Applied: $100 savings
- Final Price: $700

#### 3. Buy One Get 50% Off Strategy
**Customer Experience:**
- Encourages bulk purchases with progressive savings
- Clear indication of qualifying quantities
- Automatic calculation of discounted items

**Example Scenario:**
- Sony Headphones with "Buy 2, Get 50% off additional pairs"
- Customer adds 5 headphones at $200 each
- Calculation: 5 ÷ 2 = 2 discounted items
- Discount Applied: 2 × $200 × 50% = $200 savings
- Final Price: $1,000 - $200 = $800

### Discount Visibility

**Transparent Pricing:**
- **Real-time Updates**: Discounts calculated immediately upon basket changes
- **Detailed Breakdown**: Clear display of original price, discount amount, and final price
- **Deal Information**: Applied deals listed in order confirmation
- **Expiration Awareness**: Customers warned of expiring deals

## Order Processing & Checkout

### Checkout Workflow

**Secure Checkout Process:**
1. **Basket Validation**: Verify all items are available and in stock
2. **Stock Reservation**: Reserve inventory during checkout process
3. **Discount Application**: Apply all valid deals to final pricing
4. **Order Creation**: Generate detailed order with itemized pricing
5. **Stock Commitment**: Permanently reduce inventory levels
6. **Receipt Generation**: Provide detailed order confirmation

### Order Creation Process

**Transactional Integrity:**
```java
@Transactional
public Order checkout(Long userId) {
    // 1. Validate and reserve stock
    // 2. Calculate discounts
    // 3. Create order
    // 4. Commit stock reductions
    // 5. Update basket status
}
```

**Order Details Include:**
- **Order Items**: Product details, quantities, unit prices
- **Discount Information**: Applied deals and savings amounts
- **Total Calculations**: Subtotal, total discount, final amount
- **Applied Deals**: Detailed list of promotional offers used
- **Timestamp**: Order creation date and time

### Stock Management

**Inventory Control:**
- **Stock Validation**: Real-time availability checking before checkout
- **Concurrent Safety**: Thread-safe stock decrement operations
- **Insufficient Stock Handling**: Clear error messages for out-of-stock items
- **Stock Commitment**: Permanent inventory reduction only after successful order creation

**Business Rules:**
1. **Stock Availability**: Orders can only be placed for available quantities
2. **Concurrent Orders**: First-come-first-served for limited inventory
3. **Stock Reservation**: Temporary holds during checkout process
4. **Failure Recovery**: Stock reservations released on checkout failure

## Order History & Tracking

### Order Management

**Order History Access:**
```java
GET /api/customer/orders?page=0&size=10
```

**Order Information Provided:**
- **Order Summary**: Order ID, date, total amount, and status
- **Item Details**: Product names, quantities, and individual pricing
- **Discount Breakdown**: Applied deals and savings achieved
- **Payment Information**: Final amount paid after all discounts

**Pagination Support:**
- **Standard Parameters**: Page number (0-based), page size, sorting options
- **Performance Optimization**: Database-level pagination for large order histories
- **Sorting Options**: Sort by order date, total amount, or order status

### Order Status Tracking

**Order Lifecycle:**
- **Order Created**: Initial order placement with confirmed items
- **Payment Processed**: Payment validation and confirmation
- **Items Reserved**: Inventory allocation and preparation
- **Order Fulfilled**: Items ready for delivery/pickup
- **Order Completed**: Final status after successful delivery

## Customer Security & Privacy

### Data Protection

**Personal Information Security:**
- **Password Encryption**: BCrypt encryption for password storage
- **JWT Security**: Signed tokens with expiration for session management
- **Permission Validation**: Every request validates customer permissions
- **Data Isolation**: Customers can only access their own data

**Privacy Controls:**
- **Personal Baskets**: Basket data is user-specific and private
- **Order History**: Access limited to own order information
- **Secure Authentication**: Token-based authentication prevents session hijacking

### Permission-Based Access Control

**Customer-Specific Permissions:**
```java
@PreAuthorize("@permissionChecker.hasPermission('BASKET', 'READ')")
@PreAuthorize("@permissionChecker.hasPermission('ORDER', 'CREATE')")
```

**Access Restrictions:**
- **No Admin Access**: Customers cannot access admin functionality
- **Own Data Only**: Users can only view/modify their own baskets and orders
- **Product Read-Only**: Customers cannot modify product information

## Error Handling & User Experience

### Customer-Friendly Error Messages

**Common Error Scenarios:**
- **Product Not Found**: Clear messaging when products are unavailable
- **Insufficient Stock**: Helpful suggestions for alternative quantities
- **Authentication Required**: Guidance for login/registration process
- **Permission Denied**: Clear explanation of access limitations

**Validation Errors:**
- **Invalid Quantities**: Minimum quantity requirements clearly communicated
- **Price Changes**: Notification when product prices change during session
- **Expired Deals**: Clear indication when promotions expire

### Exception Handling

**Business Exception Types:**
- `ProductNotFoundException`: Product no longer available (404)
- `InsufficientStockException`: Requested quantity exceeds available stock (400)
- `BasketNotFoundException`: Basket access issues (404)
- `AuthenticationException`: Login required for protected operations (401)
- `PermissionDeniedException`: Insufficient permissions for operation (403)

## Performance & Scalability

### Customer Experience Optimization

**Response Time Optimization:**
- **Database Indexing**: Optimized queries for product search and filtering
- **Pagination**: Efficient data loading for large product catalogs
- **Caching**: Strategic caching for frequently accessed product information
- **Lazy Loading**: Basket items loaded on-demand for performance

**Scalability Features:**
- **Stateless Authentication**: JWT tokens enable horizontal scaling
- **Database Optimization**: Efficient queries for basket and order operations
- **Concurrent Safety**: Thread-safe operations for multi-user scenarios

## Integration Points

### Frontend Integration

**API Endpoints for Customer Operations:**
- **Public Product Browsing**: `/api/products/**` (no authentication required)
- **Authentication**: `/api/auth/**` for login/logout operations
- **Basket Management**: `/api/customer/basket/**` for shopping operations
- **Order Processing**: `/api/customer/orders/**` for checkout and history

**Response Formats:**
- **Standardized DTOs**: Consistent data transfer objects for all operations
- **Error Responses**: Uniform error handling with helpful messages
- **Pagination**: Standard pagination metadata for list operations

### Mobile & Web Support

**Cross-Platform Compatibility:**
- **RESTful APIs**: Standard HTTP methods for all operations
- **JSON Responses**: Universal data format for web and mobile clients
- **CORS Support**: Cross-origin resource sharing for web applications
- **Responsive Design**: API supports various client form factors

## Business Rules Summary

### Shopping Experience Rules

1. **Product Browsing**: Open access to product catalog without authentication
2. **Basket Persistence**: Shopping baskets persist across login sessions
3. **Automatic Discounts**: All applicable deals applied automatically
4. **Stock Validation**: Real-time inventory checking prevents overselling
5. **Secure Checkout**: Transactional integrity ensures order accuracy
6. **Order History**: Complete access to personal purchase history

### Discount Application Rules

1. **Automatic Calculation**: Discounts applied without customer intervention
2. **Multiple Deals**: Customers benefit from all applicable promotions
3. **Expiration Handling**: Expired deals automatically excluded
4. **Transparent Pricing**: Clear breakdown of all discounts and savings

### Security & Privacy Rules

1. **Authentication Required**: Basket and order operations require login
2. **Data Isolation**: Customers access only their own information
3. **Permission Validation**: Every operation validates appropriate permissions
4. **Secure Tokens**: JWT-based authentication with configurable expiration

## Future Enhancements

### Planned Customer Features

1. **Wishlist Management**: Save products for future purchase consideration
2. **Price Alerts**: Notification when product prices decrease
3. **Loyalty Program**: Points-based rewards for frequent customers
4. **Product Reviews**: Customer feedback and rating system
5. **Order Tracking**: Real-time delivery status updates
6. **Multiple Addresses**: Support for shipping to different locations
7. **Payment Methods**: Multiple payment options and saved payment methods

### Enhanced Shopping Features

1. **Recently Viewed**: Track and display recently browsed products
2. **Recommendations**: AI-powered product suggestions
3. **Bulk Ordering**: Enhanced support for large quantity purchases
4. **Gift Functionality**: Gift wrapping and delivery to recipients
5. **Subscription Orders**: Recurring purchases for regular items

## Summary

The customer business logic provides a comprehensive e-commerce experience with:

- **Seamless Product Discovery**: Public access to product catalog with advanced filtering
- **Intelligent Shopping Basket**: Persistent basket management with real-time calculations
- **Automatic Discount Application**: Sophisticated deal processing with multiple strategy support
- **Secure Checkout Process**: Transactional integrity with stock management
- **Comprehensive Order Management**: Complete order history with detailed breakdown
- **Robust Security**: JWT-based authentication with permission-level access control
- **Optimized Performance**: Efficient database operations with pagination support

This implementation delivers a modern, secure, and user-friendly shopping experience while maintaining high performance and scalability for growing customer bases.
