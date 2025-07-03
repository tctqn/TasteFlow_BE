-- === 1. Categories ===
CREATE TABLE categories
(
    category_id SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT
);

-- === 2. Image Storage ===
CREATE TABLE image_storage
(
    id              SERIAL PRIMARY KEY,
    image_url       VARCHAR(255) NOT NULL,
    description     VARCHAR(255),
    reference_id    BIGINT       NOT NULL,
    reference_table VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP
);

-- === 3. Promotions ===
CREATE TABLE promotions
(
    promotion_id        SERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    discount_percentage NUMERIC(5, 2),
    start_date          TIMESTAMP    NOT NULL,
    end_date            TIMESTAMP    NOT NULL
);

-- === 4. Store Requests ===
CREATE TABLE store_requests
(
    request_id     SERIAL PRIMARY KEY,
    store_id       INT         NOT NULL,
    warehouse_id   INT         NOT NULL,
    status         VARCHAR(50) NOT NULL,
    request_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_date TIMESTAMP,
    notes          TEXT
);

-- === 5. Suppliers ===
CREATE TABLE suppliers
(
    supplier_id  SERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE,
    phone        VARCHAR(20),
    address      TEXT,
    contact_info VARCHAR(100)
);

-- === 6. Units ===
CREATE TABLE units
(
    unit_id SERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL
);

-- === 7. Users ===
CREATE TABLE users
(
    user_id            SERIAL PRIMARY KEY,
    username           VARCHAR(50)  NOT NULL UNIQUE,
    password_hash      VARCHAR(255) NOT NULL,
    email              VARCHAR(100) NOT NULL UNIQUE,
    role               VARCHAR(20)  NOT NULL CHECK (role IN ('CUSTOMER', 'STORE_STAFF', 'ADMIN', 'WAREHOUSE_MANAGER',
                                                             'STORE_MANAGER')),
    enabled            BOOLEAN      NOT NULL,
    created_at         TIMESTAMP    NOT NULL,
    first_name         VARCHAR(50),
    last_name          VARCHAR(50),
    phone              VARCHAR(20),
    address            TEXT,
    points             INT,
    verification_token VARCHAR(255)
);

-- === 8. Vouchers ===
CREATE TABLE vouchers
(
    voucher_id        SERIAL PRIMARY KEY,
    code              VARCHAR(50) NOT NULL UNIQUE,
    discount_amount   NUMERIC(10, 2),
    discount_percent  NUMERIC(5, 2),
    discount_type     VARCHAR(20) NOT NULL CHECK (discount_type IN ('AMOUNT', 'PERCENT')),
    distribution_type VARCHAR(20) NOT NULL CHECK (distribution_type IN ('PUBLIC', 'PRIVATE', 'CUSTOMER_ONLY')),
    start_date        TIMESTAMP   NOT NULL,
    end_date          TIMESTAMP   NOT NULL,
    is_stackable      BOOLEAN     NOT NULL,
    free_shipping     BOOLEAN,
    min_order_amount  NUMERIC(10, 2),
    max_per_user      INT         NOT NULL,
    claimed_count     INT         NOT NULL,
    quantity          INT         NOT NULL,
    title             VARCHAR(100),
    description       TEXT
);

-- === 9. Products ===
CREATE TABLE products
(
    product_id  SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    category_id INT,
    created_at  TIMESTAMP    NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories (category_id)
);

-- === 10. Product Units ===
CREATE TABLE product_units
(
    product_unit_id SERIAL PRIMARY KEY,
    product_id      INT            NOT NULL,
    unit_id         INT            NOT NULL,
    sku             VARCHAR(50)    NOT NULL,
    image_url       TEXT,
    description     TEXT,
    conversion_rate INT,
    price           NUMERIC(10, 2) NOT NULL,
    is_base_unit    BOOLEAN,
    FOREIGN KEY (product_id) REFERENCES products (product_id),
    FOREIGN KEY (unit_id) REFERENCES units (unit_id)
);

-- === 11. Promotion Products ===
CREATE TABLE promotion_products
(
    promotion_id INT NOT NULL,
    product_id   INT NOT NULL,
    FOREIGN KEY (promotion_id) REFERENCES promotions (promotion_id),
    FOREIGN KEY (product_id) REFERENCES products (product_id)
);

-- === 12. Shipping Addresses ===
CREATE TABLE shipping_addresses
(
    address_id     SERIAL PRIMARY KEY,
    user_id        INT     NOT NULL,
    address_line   TEXT    NOT NULL,
    phone          VARCHAR(20),
    recipient_name VARCHAR(100),
    is_default     BOOLEAN NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

-- === 13. Stores ===
CREATE TABLE stores
(
    store_id       SERIAL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    address        TEXT         NOT NULL,
    province       VARCHAR(100),
    district       VARCHAR(100),
    village        VARCHAR(100),
    business_hours VARCHAR(100),
    contact_info   VARCHAR(100),
    region         TEXT         NOT NULL CHECK (region IN ('NORTH', 'CENTRAL', 'SOUTH')),
    status         VARCHAR(50)  NOT NULL CHECK (status IN ('OPEN', 'CLOSED', 'MAINTENANCE')),
    manager_id     INT UNIQUE,
    FOREIGN KEY (manager_id) REFERENCES users (user_id)
);

-- === 14. Store Request Items ===
CREATE TABLE store_request_items
(
    request_item_id SERIAL PRIMARY KEY,
    request_id      INT NOT NULL,
    product_id      INT NOT NULL,
    unit_id         INT NOT NULL,
    quantity        INT NOT NULL,
    FOREIGN KEY (request_id) REFERENCES store_requests (request_id),
    FOREIGN KEY (product_id) REFERENCES products (product_id),
    FOREIGN KEY (unit_id) REFERENCES units (unit_id)
);

-- === 15. User Vouchers ===
CREATE TABLE user_vouchers
(
    user_voucher_id SERIAL PRIMARY KEY,
    user_id         INT       NOT NULL,
    voucher_id      INT       NOT NULL,
    claimed_at      TIMESTAMP NOT NULL,
    used            BOOLEAN   NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (voucher_id) REFERENCES vouchers (voucher_id)
);

-- === 16. Voucher Categories ===
CREATE TABLE voucher_categories
(
    voucher_id  INT NOT NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (voucher_id, category_id),
    FOREIGN KEY (voucher_id) REFERENCES vouchers (voucher_id),
    FOREIGN KEY (category_id) REFERENCES categories (category_id)
);

-- === 17. Voucher Products ===
CREATE TABLE voucher_products
(
    voucher_id INT NOT NULL,
    product_id INT NOT NULL,
    PRIMARY KEY (voucher_id, product_id),
    FOREIGN KEY (voucher_id) REFERENCES vouchers (voucher_id),
    FOREIGN KEY (product_id) REFERENCES products (product_id)
);

-- === 18. Warehouses ===
CREATE TABLE warehouses
(
    warehouse_id SERIAL PRIMARY KEY,
    name         VARCHAR(255)     NOT NULL,
    location     TEXT             NOT NULL,
    phone        VARCHAR(20),
    region       TEXT             NOT NULL CHECK (region IN ('NORTH', 'CENTRAL', 'SOUTH')),
    status       VARCHAR(50)      NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'CLOSED')),
    capacity     DOUBLE PRECISION NOT NULL,
    created_at   TIMESTAMP,
    manager_id   INT UNIQUE,
    FOREIGN KEY (manager_id) REFERENCES users (user_id)
);

-- === 19. Cart Items ===
CREATE TABLE cart_items
(
    cart_item_id SERIAL PRIMARY KEY,
    user_id      INT       NOT NULL,
    product_id   INT       NOT NULL,
    quantity     INT       NOT NULL,
    added_at     TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (product_id) REFERENCES products (product_id)
);

-- === 20. Orders ===
CREATE TABLE orders
(
    order_id            SERIAL PRIMARY KEY,
    user_id             INT,
    store_id            INT,
    shipping_address_id INT,
    order_code          VARCHAR(255),
    ref_code            VARCHAR(255),
    full_name           VARCHAR(255)   NOT NULL,
    phone               VARCHAR(255)   NOT NULL,
    address             VARCHAR(255)   NOT NULL,
    delivery_date       VARCHAR(255)   NOT NULL,
    delivery_slot       VARCHAR(255)   NOT NULL,
    order_date          TIMESTAMP      NOT NULL,
    payment_method      VARCHAR(255)   NOT NULL CHECK (payment_method IN ('COD', 'ONLINE')),
    status              VARCHAR(255)   NOT NULL CHECK (status IN
                                                       ('PENDING', 'PAID', 'CONFIRMED', 'PROCESSING', 'SHIPPING',
                                                        'DELIVERED', 'CANCELLED', 'FAILED')),
    need_invoice        BOOLEAN        NOT NULL,
    total_price         NUMERIC(10, 2) NOT NULL,
    voucher_discount    NUMERIC(10, 2),
    note                VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (store_id) REFERENCES stores (store_id),
    FOREIGN KEY (shipping_address_id) REFERENCES shipping_addresses (address_id),
    UNIQUE (order_code)
);

-- === 21. Order Vouchers ===
CREATE TABLE order_vouchers
(
    order_id   INT NOT NULL,
    voucher_id INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (order_id),
    FOREIGN KEY (voucher_id) REFERENCES vouchers (voucher_id)
);

-- === 22. Payments ===
CREATE TABLE payments
(
    payment_id     SERIAL PRIMARY KEY,
    order_id       INT            NOT NULL,
    amount         NUMERIC(10, 2) NOT NULL,
    payment_method VARCHAR(50)    NOT NULL,
    status         VARCHAR(50)    NOT NULL,
    payment_date   TIMESTAMP      NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (order_id)
);

-- === 23. Product Batches ===
CREATE TABLE product_batches
(
    batch_id         SERIAL PRIMARY KEY,
    product_id       INT            NOT NULL,
    unit_id          INT            NOT NULL,
    supplier_id      INT            NOT NULL,
    warehouse_id     INT            NOT NULL,
    quantity         INT            NOT NULL,
    import_price     NUMERIC(38, 2) NOT NULL,
    manufacture_date DATE,
    expiration_date  DATE,
    received_date    TIMESTAMP      NOT NULL,
    note             TEXT,
    status           VARCHAR(255)   NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products (product_id),
    FOREIGN KEY (unit_id) REFERENCES units (unit_id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers (supplier_id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses (warehouse_id)
);

-- === 24. Refunds ===
CREATE TABLE refunds
(
    refund_id   SERIAL PRIMARY KEY,
    order_id    INT            NOT NULL,
    payment_id  INT            NOT NULL,
    amount      NUMERIC(38, 2) NOT NULL,
    refund_date TIMESTAMP      NOT NULL,
    status      VARCHAR(255)   NOT NULL,
    reason      TEXT,
    FOREIGN KEY (order_id) REFERENCES orders (order_id),
    FOREIGN KEY (payment_id) REFERENCES payments (payment_id)
);

-- === 25. Stock Movements ===
CREATE TABLE stock_movements
(
    movement_id   SERIAL PRIMARY KEY,
    product_id    INT          NOT NULL,
    batch_id      INT,
    warehouse_id  INT,
    store_id      INT,
    request_id    INT,
    movement_type VARCHAR(255) NOT NULL CHECK (movement_type IN ('IMPORT_BATCH', 'TRANSFER_TO_STORE', 'SALE', 'DAMAGE')),
    movement_date TIMESTAMP    NOT NULL,
    quantity      INT          NOT NULL,
    note          VARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES products (product_id),
    FOREIGN KEY (batch_id) REFERENCES product_batches (batch_id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses (warehouse_id),
    FOREIGN KEY (store_id) REFERENCES stores (store_id),
    FOREIGN KEY (request_id) REFERENCES store_requests (request_id),
    UNIQUE (request_id)
);

-- === 26. Delivery Trackings ===
CREATE TABLE delivery_trackings
(
    tracking_id             SERIAL PRIMARY KEY,
    order_id                INT          NOT NULL,
    carrier                 VARCHAR(100),
    tracking_number         VARCHAR(100),
    status                  VARCHAR(255) NOT NULL,
    estimated_delivery_date DATE,
    updated_at              TIMESTAMP    NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (order_id)
);

-- === 27. Inventories ===
CREATE TABLE inventories
(
    inventory_id  SERIAL PRIMARY KEY,
    batch_id      INT NOT NULL,
    product_id    INT NOT NULL,
    warehouse_id  INT,
    store_id      INT,
    quantity      INT NOT NULL,
    reorder_level INT NOT NULL,
    FOREIGN KEY (batch_id) REFERENCES product_batches (batch_id),
    FOREIGN KEY (product_id) REFERENCES products (product_id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses (warehouse_id),
    FOREIGN KEY (store_id) REFERENCES stores (store_id)
);

-- === 28. Invoices ===
CREATE TABLE invoices
(
    invoice_id              SERIAL PRIMARY KEY,
    order_id                INT            NOT NULL UNIQUE,
    total_amount            NUMERIC(10, 2) NOT NULL,
    issued_at               TIMESTAMP      NOT NULL,
    invoice_company_name    VARCHAR(255),
    invoice_company_address VARCHAR(255),
    invoice_email           VARCHAR(255),
    invoice_tax_code        VARCHAR(255),
    invoice_url             VARCHAR(255),
    FOREIGN KEY (order_id) REFERENCES orders (order_id)
);

-- === 29. Order Items ===
CREATE TABLE order_items
(
    order_item_id    SERIAL PRIMARY KEY,
    order_id         INT            NOT NULL,
    product_unit_id  INT            NOT NULL,
    batch_id         INT,
    price            NUMERIC(10, 2) NOT NULL,
    discount         NUMERIC(10, 2),
    quantity         INT            NOT NULL,
    quantity_in_base INT            NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (order_id),
    FOREIGN KEY (product_unit_id) REFERENCES product_units (product_unit_id),
    FOREIGN KEY (batch_id) REFERENCES product_batches (batch_id)
);
