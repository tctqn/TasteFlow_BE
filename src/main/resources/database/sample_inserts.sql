-- === CATEGORIES ===
INSERT INTO categories (name, description)
VALUES ('Building', 'Court degree fight no local everybody.'),
       ('Agree', 'Out purpose especially sound.'),
       ('Mission', 'Tend room inside plant.'),
       ('Social', 'Mean red party product.'),
       ('Do', 'Force take mission must middle today he.');

INSERT INTO users (username, email, password_hash, role, first_name, last_name, phone, address, created_at, points,
                   enabled)
VALUES ('hoangminh', 'hoang@example.com', 'hash123', 'WAREHOUSE_MANAGER', 'Minh', 'Hoàng', '0909123456',
        '123 Trần Hưng Đạo, HN', NOW(), 100, TRUE),
       ('lethao', 'thao@example.com', 'hash456', 'WAREHOUSE_MANAGER', 'Thảo', 'Lê', '0909876543',
        '456 Nguyễn Trãi, HCM', NOW(), 200, TRUE),
       ('ngocanh', 'anh@example.com', 'hash789', 'WAREHOUSE_MANAGER', 'Anh', 'Ngọc', '0912345678', '789 Lê Duẩn, ĐN',
        NOW(), 300, TRUE),
       ('trungkien', 'kien@example.com', 'hashabc', 'WAREHOUSE_MANAGER', 'Kiên', 'Trung', '0923456789',
        '101 Trần Phú, Cần Thơ', NOW(), 0, TRUE),
       ('thuylinh', 'linh@example.com', 'hashdef', 'WAREHOUSE_MANAGER', 'Linh', 'Thủy', '0934567890',
        '202 Quang Trung, Huế', NOW(), 500, TRUE);

-- === USERS (WAREHOUSE_MANAGER) ===
INSERT INTO users (username, email, password_hash, role,
                   first_name, last_name, phone, address,
                   created_at, points, enabled)
VALUES ('tuananh', 'tuananh@example.com', 'hashghi', 'WAREHOUSE_MANAGER', 'Tuấn', 'Anh', '0945678912',
        '303 Điện Biên Phủ, HN', NOW(), 450, TRUE),
       ('hoailinh', 'linhhoai@example.com', 'hashjkl', 'WAREHOUSE_MANAGER', 'Linh', 'Hoài', '0956789123',
        '404 Hai Bà Trưng, HCM', NOW(), 150, TRUE),
       ('duyphong', 'duyphong@example.com', 'hashmno', 'WAREHOUSE_MANAGER', 'Phong', 'Duy', '0967891234',
        '505 Nguyễn Huệ, ĐN', NOW(), 250, TRUE),
       ('maithao', 'thao.mai@example.com', 'hashpqr', 'WAREHOUSE_MANAGER', 'Thảo', 'Mai', '0978912345',
        '606 Hùng Vương, Huế', NOW(), 180, TRUE),
       ('khanhlinh', 'khanhlinh@example.com', 'hashstu', 'WAREHOUSE_MANAGER', 'Linh', 'Khánh', '0989123456',
        '707 Lý Thường Kiệt, Cần Thơ', NOW(), 320, TRUE);

-- === USERS (SHOP_MANAGER) ===
INSERT INTO users (username, email, password_hash, role,
                   first_name, last_name, phone, address,
                   created_at, points, enabled)
VALUES ('nhatminh', 'nhatminh@example.com', 'hashabc1', 'STORE_MANAGER', 'Nhật', 'Minh', '0911111111',
        '101 Lê Lợi, Hà Nội', NOW(), 300, TRUE),
       ('thuytien', 'thuylinh@example.com', 'hashabc2', 'STORE_MANAGER', 'Thúy', 'Linh', '0922222222',
        '202 Nguyễn Trãi, TP.HCM', NOW(), 220, TRUE),
       ('quocbao', 'quocbao@example.com', 'hashabc3', 'STORE_MANAGER', 'Quốc', 'Bảo', '0933333333',
        '303 Trần Hưng Đạo, Đà Nẵng', NOW(), 275, TRUE),
       ('minhthu', 'minhthu@example.com', 'hashabc4', 'STORE_MANAGER', 'Minh', 'Thư', '0944444444', '404 Pasteur, Huế',
        NOW(), 310, TRUE),
       ('baoduy', 'baoduy@example.com', 'hashabc5', 'STORE_MANAGER', 'Bảo', 'Duy', '0955555555',
        '505 Phan Đình Phùng, Cần Thơ', NOW(), 195, TRUE),
       ('camtu', 'camtu@example.com', 'hashabc6', 'STORE_MANAGER', 'Cẩm', 'Tú', '0966666666', '606 Lý Thái Tổ, Hà Nội',
        NOW(), 330, TRUE),
       ('thanhson', 'thanhson@example.com', 'hashabc7', 'STORE_MANAGER', 'Thanh', 'Sơn', '0977777777',
        '707 Nguyễn Văn Cừ, TP.HCM', NOW(), 280, TRUE),
       ('hoanganh', 'hoanganh@example.com', 'hashabc8', 'STORE_MANAGER', 'Hoàng', 'Anh', '0988888888',
        '808 Võ Thị Sáu, Hải Phòng', NOW(), 210, TRUE),
       ('thienan', 'thienan@example.com', 'hashabc9', 'STORE_MANAGER', 'Thiên', 'An', '0999999999',
        '909 Trần Phú, Nha Trang', NOW(), 240, TRUE),
       ('kimngan', 'kimngan@example.com', 'hashabc10', 'STORE_MANAGER', 'Kim', 'Ngân', '0900000000',
        '1001 Quang Trung, Buôn Ma Thuột', NOW(), 265, TRUE);


-- === SUPPLIERS ===
INSERT INTO suppliers (name, contact_info, address, email, phone)
VALUES ('Williams PLC', '218.500.8219x85653', '8525 Alexander Heights, South Joseph, MD 18677',
        'jenniferdalton@garcia.com', '483-937-8077x6800'),
       ('Taylor and Sons', '858-362-0548x8304', '937 Maria Cliff Apt. 738, Wangburgh, RI 98273',
        'melissa80@hotmail.com', '913.508.8056x936'),
       ('Brown-Ware', '(312)153-7883x516', '5693 Katelyn Ford Suite 586, Sullivanbury, CO 49324',
        'alexandercook@montes-bray.com', '(111)998-2429'),
       ('Cain-Turner', '001-941-537-0681', '01941 Michael Plaza Suite 879, Cesarport, DE 09795', 'amanda87@hall.com',
        '286-759-5344x70910'),
       ('Matthews PLC', '+1-487-151-7203x1629', '12208 Vanessa Summit, Judychester, KS 81092', 'marycarter@lee.com',
        '+1-180-584-0315x472');

-- === STORES (FULL DATA) ===
INSERT INTO stores (name, address, contact_info, business_hours, region, status, manager_id)
VALUES ('Campbell, Warner and Smith', '1330 Williams Road, Perezview, NV 26581', '223.940.6562x004', '9 AM - 6 PM',
        'NORTH', 'OPEN', NULL),
       ('Watkins, Allen and Barrett', '5516 Jackson Prairie, Lake Aaronport, VT 10482', '001-744-188-7440',
        '9 AM - 6 PM', 'SOUTH', 'OPEN', NULL),
       ('Shaffer, Brown and Taylor', '424 Williams Gateway Apt. 519, Nicholasshire, PA 85969', '(165)842-4965x61124',
        '9 AM - 6 PM', 'CENTRAL', 'OPEN', NULL),
       ('Hopkins PLC', '60815 Andrew Square Suite 181, Frankburgh, WI 82817', '+1-764-479-2712x91095', '9 AM - 6 PM',
        'NORTH', 'MAINTENANCE', NULL),
       ('Lam, Christensen and Lopez', '14535 Robin Plains, Andreaport, FL 19889', '389.475.9028x18155', '9 AM - 6 PM',
        'SOUTH', 'CLOSED', NULL);


-- === WAREHOUSES ===
INSERT INTO warehouses (name, location, manager_id, phone, created_at, capacity, region, status)
VALUES ('NavajoWhite Warehouse', '223 Bryce Canyon, South Victorhaven, TX 51989', 1, '340.969.9246x514',
        '2025-01-13 04:22:45', 1000, 'SOUTH', 'ACTIVE'),
       ('MediumVioletRed Warehouse', '845 Martinez Knoll, New Deanna, AR 74945', 2, '428-393-1154',
        '2024-06-01 04:22:45', 800, 'CENTRAL', 'ACTIVE'),
       ('Lavender Warehouse', '4363 Pamela Burg Apt. 331, South Alexander, ND 31881', 3, '+1-526-561-9853',
        '2025-02-10 04:22:45', 1200, 'NORTH', 'INACTIVE'),
       ('Yellow Warehouse', '81364 Rich Fort, South Chelseachester, NH 34935', 4, '3573365784', '2025-04-01 04:22:45',
        900, 'NORTH', 'ACTIVE'),
       ('Maroon Warehouse', '16797 Cox Club, Lake Ronniestad, IA 70009', 5, '647-158-7121x89163', '2024-07-09 04:22:45',
        750, 'NORTH', 'CLOSED');


-- === PROMOTIONS ===
INSERT INTO promotions (name, description, discount_percentage, start_date, end_date)
VALUES ('Seek Promo', 'Most smile effect television. Low guess generation these continue.', 41.45,
        '2025-02-08 04:22:45', '2024-12-10 04:22:45'),
       ('Score Promo', 'Start consumer company research stage step individual. Meeting foreign method.', 20.81,
        '2024-06-28 04:22:45', '2025-02-09 04:22:45'),
       ('Plant Promo', 'Reason popular several heart among market summer. Allow among trouble arrive.', 32.51,
        '2025-03-23 04:22:45', '2024-07-10 04:22:45'),
       ('Contain Promo', 'Value more pretty inside full consumer. Place where quality dog.', 20.7,
        '2025-01-01 04:22:45', '2025-03-25 04:22:45'),
       ('Attorney Promo', 'Spring from word third kid. Source strong cover similar change grow both.', 35.71,
        '2024-07-12 04:22:45', '2024-12-19 04:22:45');

-- === UNITS ===
INSERT INTO units (name)
VALUES ('Thùng'),
       ('Lốc'),
       ('Chai'),
       ('Hộp'),
       ('Gói');

-- === PRODUCTS (rút gọn theo class hiện tại) ===
INSERT INTO products (name, category_id, created_at)
VALUES ('Nước suối Lavie 500ml', 1, NOW()),
       ('Sữa tươi Vinamilk 1L', 2, NOW()),
       ('Mì tôm Hảo Hảo', 3, NOW()),
       ('Bánh Oreo', 4, NOW()),
       ('Coca-Cola lon 330ml', 5, NOW());


-- === PRODUCT_UNITS (đầy đủ theo class) ===
INSERT INTO product_units (product_id, unit_id, sku, image_url, description, conversion_rate, price, is_base_unit)
VALUES (1, 3, 'LV500ML', 'https://example.com/lavie500.jpg', 'Nước suối Lavie 500ml - Chai', 1, 5000.00, TRUE),
       (1, 1, 'LVTHUNG', 'https://example.com/lavie-thung.jpg', 'Lavie 500ml - Thùng 24 chai', 24, 120000.00, FALSE),

       (2, 3, 'VM1L', 'https://example.com/vm1l.jpg', 'Sữa tươi Vinamilk 1L - Chai', 1, 27000.00, TRUE),
       (2, 1, 'VMTHUNG', 'https://example.com/vm-thung.jpg', 'Vinamilk 1L - Thùng 12 chai', 12, 320000.00, FALSE),

       (3, 5, 'HHGOI', 'https://example.com/haohao.jpg', 'Mì tôm Hảo Hảo - Gói', 1, 3500.00, TRUE),
       (3, 2, 'HHLOC', 'https://example.com/haohao-loc.jpg', 'Hảo Hảo - Lốc 10 gói', 10, 34000.00, FALSE),

       (4, 4, 'OREOBOX', 'https://example.com/oreo.jpg', 'Bánh Oreo - Hộp', 1, 10000.00, TRUE),
       (4, 1, 'OREOTHUNG', 'https://example.com/oreo-thung.jpg', 'Oreo - Thùng 20 hộp', 20, 185000.00, FALSE),

       (5, 3, 'COCALON', 'https://example.com/coca330.jpg', 'Coca-Cola lon 330ml', 1, 8500.00, TRUE),
       (5, 2, 'COCALOC', 'https://example.com/coca-loc.jpg', 'Coca - Lốc 6 lon', 6, 48000.00, FALSE);


-- === SHIPPING ADDRESSES ===
INSERT INTO shipping_addresses (user_id, recipient_name, phone, address_line, is_default)
VALUES (1, 'Alice Smith', '0909123456', '123 Trần Hưng Đạo, HN', TRUE),
       (2, 'Thảo Lê', '0909876543', '456 Nguyễn Trãi, HCM', TRUE),
       (3, 'Ngọc Anh', '0912345678', '789 Lê Duẩn, ĐN', TRUE),
       (4, 'Kiên Trung', '0923456789', '101 Trần Phú, Cần Thơ', TRUE),
       (5, 'Linh Thủy', '0934567890', '202 Quang Trung, Huế', TRUE);

-- === CART ITEMS ===
INSERT INTO cart_items (user_id, product_id, quantity, added_at)
VALUES (1, 1, 2, NOW()),
       (2, 3, 1, NOW()),
       (3, 2, 4, NOW()),
       (4, 5, 3, NOW()),
       (5, 4, 1, NOW());

-- === PRODUCT_BATCHES ===
INSERT INTO product_batches (product_id, supplier_id, warehouse_id, unit_id, quantity, manufacture_date,
                             expiration_date, received_date, note, status, import_price)
VALUES (1, 1, 1, 3, 100, '2025-01-01', '2026-01-01', '2025-05-31 10:00:00', 'Lô hàng sữa tươi nhập từ nhà cung cấp A',
        'ACTIVE', 15000.00),
       (2, 2, 2, 3, 200, '2024-12-15', '2025-12-15', '2025-05-31 10:05:00', 'Lô hàng nước giải khát', 'ACTIVE',
        10000.00),
       (3, 3, 3, 5, 150, '2025-02-01', '2026-02-01', '2025-05-31 10:10:00', 'Lô bánh snack các loại', 'ACTIVE',
        8000.00),
       (4, 4, 4, 4, 120, '2025-03-10', '2026-03-10', '2025-05-31 10:15:00', 'Lô mì ăn liền từ nhà cung cấp D', 'ACTIVE',
        12000.00),
       (5, 5, 5, 3, 300, '2025-04-01', '2026-04-01', '2025-05-31 10:20:00', 'Lô hàng gia vị và nước chấm', 'ACTIVE',
        9000.00);

-- === INVENTORIES ===
INSERT INTO inventories (warehouse_id, store_id, product_id, batch_id, quantity, reorder_level)
VALUES (1, 1, 1, 1, 80, 10),
       (2, 2, 2, 2, 150, 15),
       (3, 3, 3, 3, 100, 20),
       (4, 4, 4, 4, 70, 10),
       (5, 5, 5, 5, 250, 30);





