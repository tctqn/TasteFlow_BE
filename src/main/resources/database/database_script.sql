-- 1. Independent Tables (No Foreign Keys)
CREATE TABLE users (
   user_id SERIAL PRIMARY KEY,
   username VARCHAR(50) UNIQUE NOT NULL,
   email VARCHAR(100) UNIQUE NOT NULL,
   password_hash VARCHAR(255) NOT NULL,
   role VARCHAR(20) NOT NULL DEFAULT 'user',
   first_name VARCHAR(50),
   last_name VARCHAR(50),
   phone VARCHAR(20),
   address TEXT,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   points INT DEFAULT 0
);

CREATE TABLE suppliers (
   supplier_id SERIAL PRIMARY KEY,
   name VARCHAR(255) NOT NULL,
   contact_info VARCHAR(100),
   address TEXT,
   email VARCHAR(100) UNIQUE NOT NULL,
   phone VARCHAR(20)
);

CREATE TABLE categories (
   category_id SERIAL PRIMARY KEY,
   name VARCHAR(100) NOT NULL,
   description TEXT
);

CREATE TABLE stores (
   store_id SERIAL PRIMARY KEY,
   name VARCHAR(255) NOT NULL,
   address TEXT NOT NULL,
   contact_info VARCHAR(100),
   business_hours VARCHAR(100)
);

CREATE TABLE warehouses (
   warehouse_id SERIAL PRIMARY KEY,
   name VARCHAR(255) NOT NULL,
   location TEXT NOT NULL,
   manager_name VARCHAR(100),
   phone VARCHAR(20),
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE promotions (
   promotion_id SERIAL PRIMARY KEY,
   name VARCHAR(255) NOT NULL,
   description TEXT,
   discount_percentage DECIMAL(5,2),
   start_date TIMESTAMP NOT NULL,
   end_date TIMESTAMP NOT NULL
);

CREATE TABLE vouchers (
   voucher_id SERIAL PRIMARY KEY,
   code VARCHAR(50) UNIQUE NOT NULL,
   discount_amount DECIMAL(10,2) NOT NULL,
   discount_type VARCHAR(20) CHECK (discount_type IN ('Percentage', 'Fixed')) NOT NULL,
   start_date TIMESTAMP NOT NULL,
   end_date TIMESTAMP NOT NULL
);

-- 2. Tables with Foreign Key Dependencies
CREATE TABLE products (
   product_id SERIAL PRIMARY KEY,
   name VARCHAR(255) NOT NULL,
   description TEXT,
   price DECIMAL(10,2) NOT NULL,
   sku VARCHAR(50) UNIQUE NOT NULL,
   category_id INT,
   stock_quantity INT NOT NULL DEFAULT 0,
   image_url TEXT,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

CREATE TABLE shipping_addresses (
   address_id SERIAL PRIMARY KEY,
   user_id INT NOT NULL,
   recipient_name VARCHAR(100),
   phone VARCHAR(20),
   address_line TEXT NOT NULL,
   is_default BOOLEAN DEFAULT FALSE,
   FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE orders (
   order_id SERIAL PRIMARY KEY,
   user_id INT NOT NULL,
   status VARCHAR(50) NOT NULL CHECK (status IN ('Pending', 'Processing', 'Completed', 'Cancelled')),
   total_price DECIMAL(10,2) NOT NULL,
   voucher_id INT NULL,
   voucher_discount DECIMAL(10,2) DEFAULT 0,
   order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   shipping_address_id INT NULL,
   store_id INT NULL,
   FOREIGN KEY (user_id) REFERENCES users(user_id),
   FOREIGN KEY (voucher_id) REFERENCES vouchers(voucher_id),
   FOREIGN KEY (shipping_address_id) REFERENCES shipping_addresses(address_id),
   FOREIGN KEY (store_id) REFERENCES stores(store_id)
);

-- 3. Product Batches (Updated)
CREATE TABLE product_batches (
   batch_id SERIAL PRIMARY KEY,
   product_id INT NOT NULL,
   warehouse_id INT NOT NULL,
   supplier_id INT NOT NULL,
   initial_quantity INT NOT NULL,
   available_quantity INT NOT NULL,
   damaged_quantity INT NOT NULL DEFAULT 0,
   manufacture_date DATE,
   expiration_date DATE,
   received_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   note TEXT,
   import_price NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
   FOREIGN KEY (product_id) REFERENCES products(product_id),
   FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id),
   FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
);


-- 4. Inventories (Updated)
CREATE TABLE inventories (
   inventory_id SERIAL PRIMARY KEY,
   warehouse_id INT NULL,
   store_id INT NULL,
   product_id INT NOT NULL,
   batch_id INT NOT NULL,
   quantity INT NOT NULL DEFAULT 0,
   reorder_level INT NOT NULL DEFAULT 10,
   FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id),
   FOREIGN KEY (store_id) REFERENCES stores(store_id),
   FOREIGN KEY (product_id) REFERENCES products(product_id),
   FOREIGN KEY (batch_id) REFERENCES product_batches(batch_id)
);

-- Nếu muốn chặt chẽ hơn, bạn có thể thêm UNIQUE constraint:
-- UNIQUE (warehouse_id, store_id, product_id, batch_id)

-- 5. Promotion Products
CREATE TABLE promotion_products (
   promotion_id INT NOT NULL,
   product_id INT NOT NULL,
   PRIMARY KEY (promotion_id, product_id),
   FOREIGN KEY (promotion_id) REFERENCES promotions(promotion_id),
   FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- 6. Order Items (Updated to keep batch_id)
CREATE TABLE order_items (
   order_item_id SERIAL PRIMARY KEY,
   order_id INT NOT NULL,
   product_id INT NOT NULL,
   batch_id INT NOT NULL,
   quantity INT NOT NULL,
   price DECIMAL(10,2) NOT NULL,
   discount DECIMAL(10,2) DEFAULT 0,
   FOREIGN KEY (order_id) REFERENCES orders(order_id),
   FOREIGN KEY (product_id) REFERENCES products(product_id),
   FOREIGN KEY (batch_id) REFERENCES product_batches(batch_id)
);

-- 7. Payments
CREATE TABLE payments (
   payment_id SERIAL PRIMARY KEY,
   order_id INT NOT NULL,
   amount DECIMAL(10,2) NOT NULL,
   payment_method VARCHAR(50) NOT NULL,
   status VARCHAR(50) NOT NULL CHECK (status IN ('Pending', 'Paid', 'Failed', 'Refunded')),
   payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- 8. Refunds
CREATE TABLE refunds (
   refund_id SERIAL PRIMARY KEY,
   order_id INT NOT NULL,
   payment_id INT NOT NULL,
   amount DECIMAL(10,2) NOT NULL,
   reason TEXT,
   status VARCHAR(50) NOT NULL CHECK (status IN ('Requested', 'Approved', 'Rejected', 'Completed')),
   refund_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (order_id) REFERENCES orders(order_id),
   FOREIGN KEY (payment_id) REFERENCES payments(payment_id)
);

-- 9. Cart Items
CREATE TABLE cart_items (
   cart_item_id SERIAL PRIMARY KEY,
   user_id INT NOT NULL,
   product_id INT NOT NULL,
   quantity INT NOT NULL DEFAULT 1,
   added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (user_id) REFERENCES users(user_id),
   FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- 10. Invoices
CREATE TABLE invoices (
   invoice_id SERIAL PRIMARY KEY,
   order_id INT NOT NULL,
   issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   total_amount DECIMAL(10,2) NOT NULL,
   FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- 11. Delivery Trackings
CREATE TABLE delivery_trackings (
   tracking_id SERIAL PRIMARY KEY,
   order_id INT NOT NULL,
   status VARCHAR(50) NOT NULL CHECK (status IN ('Pending', 'In Transit', 'Delivered', 'Cancelled')),
   tracking_number VARCHAR(100),
   carrier VARCHAR(100),
   estimated_delivery_date DATE,
   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- 12. Stock Movements (Updated)
CREATE TABLE stock_movements (
   movement_id SERIAL PRIMARY KEY,
   warehouse_id INT NULL,
   store_id INT NULL,
   product_id INT NOT NULL,
   batch_id INT NOT NULL,
   movement_type VARCHAR(30) NOT NULL CHECK (
      movement_type IN ('IMPORT_BATCH', 'TRANSFER_TO_STORE', 'SALE')
   ),
   quantity INT NOT NULL,
   movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   note TEXT,
   related_order_id INT NULL,
   FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id),
   FOREIGN KEY (store_id) REFERENCES stores(store_id),
   FOREIGN KEY (product_id) REFERENCES products(product_id),
   FOREIGN KEY (batch_id) REFERENCES product_batches(batch_id),
   FOREIGN KEY (related_order_id) REFERENCES orders(order_id)
);

