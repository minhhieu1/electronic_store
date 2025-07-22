-- Insert Roles
INSERT INTO role (name) VALUES ('ADMIN');
INSERT INTO role (name) VALUES ('CUSTOMER');

-- Insert Permissions
-- Product permissions
INSERT INTO permission (name, description, resource, action) VALUES
('PRODUCT_READ', 'View products', 'PRODUCT', 'READ'),
('PRODUCT_CREATE', 'Create new products', 'PRODUCT', 'CREATE'),
('PRODUCT_UPDATE', 'Update existing products', 'PRODUCT', 'UPDATE'),
('PRODUCT_DELETE', 'Delete products', 'PRODUCT', 'DELETE'),
('PRODUCT_MANAGE', 'Full product management access', 'PRODUCT', 'MANAGE'),

-- User permissions
('USER_READ', 'View user information', 'USER', 'READ'),
('USER_CREATE', 'Create new users', 'USER', 'CREATE'),
('USER_UPDATE', 'Update user information', 'USER', 'UPDATE'),
('USER_DELETE', 'Delete users', 'USER', 'DELETE'),
('USER_MANAGE', 'Full user management access', 'USER', 'MANAGE'),

-- Order permissions
('ORDER_READ', 'View orders', 'ORDER', 'READ'),
('ORDER_CREATE', 'Create new orders', 'ORDER', 'CREATE'),
('ORDER_UPDATE', 'Update order status', 'ORDER', 'UPDATE'),
('ORDER_DELETE', 'Cancel/delete orders', 'ORDER', 'DELETE'),
('ORDER_MANAGE', 'Full order management access', 'ORDER', 'MANAGE'),

-- Deal permissions
('DEAL_READ', 'View deals', 'DEAL', 'READ'),
('DEAL_CREATE', 'Create new deals', 'DEAL', 'CREATE'),
('DEAL_UPDATE', 'Update existing deals', 'DEAL', 'UPDATE'),
('DEAL_DELETE', 'Delete deals', 'DEAL', 'DELETE'),
('DEAL_MANAGE', 'Full deal management access', 'DEAL', 'MANAGE'),

-- Role permissions
('ROLE_READ', 'View roles', 'ROLE', 'READ'),
('ROLE_CREATE', 'Create new roles', 'ROLE', 'CREATE'),
('ROLE_UPDATE', 'Update role permissions', 'ROLE', 'UPDATE'),
('ROLE_DELETE', 'Delete roles', 'ROLE', 'DELETE'),
('ROLE_MANAGE', 'Full role management access', 'ROLE', 'MANAGE'),

-- Permission permissions
('PERMISSION_READ', 'View permissions', 'PERMISSION', 'READ'),
('PERMISSION_CREATE', 'Create new permissions', 'PERMISSION', 'CREATE'),
('PERMISSION_UPDATE', 'Update permissions', 'PERMISSION', 'UPDATE'),
('PERMISSION_DELETE', 'Delete permissions', 'PERMISSION', 'DELETE'),
('PERMISSION_MANAGE', 'Full permission management access', 'PERMISSION', 'MANAGE'),

-- Basket permissions
('BASKET_READ', 'View shopping baskets', 'BASKET', 'READ'),
('BASKET_CREATE', 'Create shopping baskets', 'BASKET', 'CREATE'),
('BASKET_UPDATE', 'Update basket contents', 'BASKET', 'UPDATE'),
('BASKET_DELETE', 'Delete baskets', 'BASKET', 'DELETE');

-- Assign permissions to ADMIN role (full access)
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- Product permissions
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
-- User permissions  
(1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
-- Order permissions
(1, 11), (1, 12), (1, 13), (1, 14), (1, 15),
-- Deal permissions
(1, 16), (1, 17), (1, 18), (1, 19), (1, 20),
-- Role permissions
(1, 21), (1, 22), (1, 23), (1, 24), (1, 25),
-- Permission permissions
(1, 26), (1, 27), (1, 28), (1, 29), (1, 30);

-- Assign permissions to CUSTOMER role (limited access)
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- Product permissions (read only)
(2, 1),
-- Order permissions (read and create own orders)
(2, 11), (2, 12),
-- Deal permissions (read only)
(2, 16),
-- Basket permissions (full access to own baskets)
(2, 31), (2, 32), (2, 33), (2, 34);

-- Users will be created programmatically by Java code

-- Insert Products
INSERT INTO product (name, description, price, stock, category, availability) VALUES 
('iPhone 15 Pro', 'Latest Apple smartphone with A17 Pro chip', 999.99, 50, 'Smartphones', true),
('Samsung Galaxy S24', 'Premium Android smartphone', 849.99, 30, 'Smartphones', true),
('MacBook Pro 16', 'Powerful laptop for professionals', 2499.99, 15, 'Laptops', true),
('Dell XPS 13', 'Ultrabook with excellent display', 1299.99, 25, 'Laptops', true),
('Sony WH-1000XM5', 'Noise-canceling wireless headphones', 399.99, 100, 'Audio', true),
('iPad Air', 'Versatile tablet for work and play', 599.99, 40, 'Tablets', true),
('Nintendo Switch', 'Popular gaming console', 299.99, 60, 'Gaming', true),
('Apple Watch Series 9', 'Advanced smartwatch', 429.99, 35, 'Wearables', true),
('Samsung 4K TV 55"', 'Smart TV with excellent picture quality', 799.99, 20, 'TVs', true),
('PlayStation 5', 'Next-gen gaming console', 499.99, 10, 'Gaming', true);

-- Insert Deal Types
INSERT INTO deal_type (name, description, strategy_class) VALUES 
('Percentage Discount', 'Simple percentage off the product price', 'com.altech.electronicstore.util.discount.PercentageDiscountStrategy'),
('Buy One Get 50% Off Second', 'Buy one item, get 50% off the second identical item', 'com.altech.electronicstore.util.discount.BuyOneGetFiftyPercentOffStrategy'),
('Fixed Amount Discount', 'Fixed dollar amount off the product price', 'com.altech.electronicstore.util.discount.FixedAmountDiscountStrategy');

-- Insert Active Deals (expiring in the future)
-- Format: (product_id, deal_type_id, discount_percent, discount_amount, minimum_quantity, expiration_date)

-- Percentage Discount Deals
INSERT INTO deal (product_id, deal_type_id, discount_percent, discount_amount, minimum_quantity, expiration_date) VALUES 
(1, 1, 10.00, NULL, 1, '2025-08-31 23:59:59'), -- iPhone 15 Pro - 10% off
(3, 1, 15.00, NULL, 1, '2025-08-15 23:59:59'), -- MacBook Pro - 15% off
(7, 1, 20.00, NULL, 1, '2025-08-01 23:59:59'), -- Nintendo Switch - 20% off
(9, 1, 5.00, NULL, 1, '2025-09-30 23:59:59'),  -- Samsung TV - 5% off

-- Buy One Get 50% Off Deals (requires minimum quantity of 2)
(5, 2, 50.00, NULL, 2, '2025-07-31 23:59:59'), -- Sony Headphones - Buy one get 50% off second
(6, 2, 50.00, NULL, 2, '2025-08-15 23:59:59'), -- iPad Air - Buy one get 50% off second

-- Fixed Amount Discount Deals
(2, 3, NULL, 100.00, 1, '2025-08-20 23:59:59'), -- Samsung Galaxy S24 - $100 off
(4, 3, NULL, 200.00, 1, '2025-09-15 23:59:59'), -- Dell XPS 13 - $200 off
(8, 3, NULL, 50.00, 1, '2025-08-10 23:59:59'),  -- Apple Watch - $50 off
(10, 3, NULL, 75.00, 1, '2025-09-01 23:59:59'); -- PlayStation 5 - $75 off
