-- === CATEGORIES ===
INSERT INTO categories (name, description)
VALUES 
('Building', 'Court degree fight no local everybody.'),
('Agree', 'Out purpose especially sound.'),
('Mission', 'Tend room inside plant.'),
('Social', 'Mean red party product.'),
('Do', 'Force take mission must middle today he.');

-- === USERS ===
INSERT INTO users (username, email, password_hash, role, first_name, last_name, phone, address, created_at, points)
VALUES 
('hoangminh', 'hoang@example.com', 'hash123', 'CUSTOMER', 'Minh', 'Hoàng', '0909123456', '123 Trần Hưng Đạo, HN', NOW(), 100),
('lethao', 'thao@example.com', 'hash456', 'EMPLOYEE', 'Thảo', 'Lê', '0909876543', '456 Nguyễn Trãi, HCM', NOW(), 200),
('ngocanh', 'anh@example.com', 'hash789', 'ADMIN', 'Anh', 'Ngọc', '0912345678', '789 Lê Duẩn, ĐN', NOW(), 300),
('trungkien', 'kien@example.com', 'hashabc', 'CUSTOMER', 'Kiên', 'Trung', '0923456789', '101 Trần Phú, Cần Thơ', NOW(), 0),
('thuylinh', 'linh@example.com', 'hashdef', 'EMPLOYEE', 'Linh', 'Thủy', '0934567890', '202 Quang Trung, Huế', NOW(), 500);

-- === SUPPLIERS ===
INSERT INTO suppliers (name, contact_info, address, email, phone)
VALUES 
('Williams PLC', '218.500.8219x85653', '8525 Alexander Heights, South Joseph, MD 18677', 'jenniferdalton@garcia.com', '483-937-8077x6800'),
('Taylor and Sons', '858-362-0548x8304', '937 Maria Cliff Apt. 738, Wangburgh, RI 98273', 'melissa80@hotmail.com', '913.508.8056x936'),
('Brown-Ware', '(312)153-7883x516', '5693 Katelyn Ford Suite 586, Sullivanbury, CO 49324', 'alexandercook@montes-bray.com', '(111)998-2429'),
('Cain-Turner', '001-941-537-0681', '01941 Michael Plaza Suite 879, Cesarport, DE 09795', 'amanda87@hall.com', '286-759-5344x70910'),
('Matthews PLC', '+1-487-151-7203x1629', '12208 Vanessa Summit, Judychester, KS 81092', 'marycarter@lee.com', '+1-180-584-0315x472');

-- === STORES ===
INSERT INTO stores (name, address, contact_info, business_hours)
VALUES 
('Campbell, Warner and Smith', '1330 Williams Road, Perezview, NV 26581', '223.940.6562x004', '9 AM - 6 PM'),
('Watkins, Allen and Barrett', '5516 Jackson Prairie, Lake Aaronport, VT 10482', '001-744-188-7440', '9 AM - 6 PM'),
('Shaffer, Brown and Taylor', '424 Williams Gateway Apt. 519, Nicholasshire, PA 85969', '(165)842-4965x61124', '9 AM - 6 PM'),
('Hopkins PLC', '60815 Andrew Square Suite 181, Frankburgh, WI 82817', '+1-764-479-2712x91095', '9 AM - 6 PM'),
('Lam, Christensen and Lopez', '14535 Robin Plains, Andreaport, FL 19889', '389.475.9028x18155', '9 AM - 6 PM');

-- === WAREHOUSES ===
INSERT INTO warehouses (name, location, manager_name, phone, created_at)
VALUES 
('NavajoWhite Warehouse', '223 Bryce Canyon, South Victorhaven, TX 51989', 'Crystal Ferguson', '340.969.9246x514', '2025-01-13 04:22:45'),
('MediumVioletRed Warehouse', '845 Martinez Knoll, New Deanna, AR 74945', 'Austin Collier', '428-393-1154', '2024-06-01 04:22:45'),
('Lavender Warehouse', '4363 Pamela Burg Apt. 331, South Alexander, ND 31881', 'Timothy Nelson', '+1-526-561-9853', '2025-02-10 04:22:45'),
('Yellow Warehouse', '81364 Rich Fort, South Chelseachester, NH 34935', 'Scott Smith', '3573365784', '2025-04-01 04:22:45'),
('Maroon Warehouse', '16797 Cox Club, Lake Ronniestad, IA 70009', 'William Jones', '647-158-7121x89163', '2024-07-09 04:22:45');

-- === PROMOTIONS ===
INSERT INTO promotions (name, description, discount_percentage, start_date, end_date)
VALUES 
('Seek Promo', 'Most smile effect television. Low guess generation these continue.', 41.45, '2025-02-08 04:22:45', '2024-12-10 04:22:45'),
('Score Promo', 'Start consumer company research stage step individual. Meeting foreign method.', 20.81, '2024-06-28 04:22:45', '2025-02-09 04:22:45'),
('Plant Promo', 'Reason popular several heart among market summer. Allow among trouble arrive.', 32.51, '2025-03-23 04:22:45', '2024-07-10 04:22:45'),
('Contain Promo', 'Value more pretty inside full consumer. Place where quality dog.', 20.7, '2025-01-01 04:22:45', '2025-03-25 04:22:45'),
('Attorney Promo', 'Spring from word third kid. Source strong cover similar change grow both.', 35.71, '2024-07-12 04:22:45', '2024-12-19 04:22:45');

-- === VOUCHERS ===
INSERT INTO vouchers (code, discount_amount, discount_type, start_date, end_date)
VALUES 
('xnRy-88211', 93.72, 'Fixed', '2025-04-21 04:22:45', '2025-03-16 04:22:45'),
('zDwF-92015', 40.65, 'Fixed', '2024-06-08 04:22:45', '2024-06-01 04:22:45'),
('XqNl-05603', 72.14, 'Fixed', '2025-01-25 04:22:45', '2024-05-17 04:22:45'),
('IFet-24934', 54.21, 'Fixed', '2025-04-16 04:22:45', '2024-11-22 04:22:45'),
('vdcK-08353', 41.57, 'Fixed', '2024-08-24 04:22:45', '2024-07-14 04:22:45');

-- === UNITS ===
INSERT INTO units (name) VALUES
('Thùng'),
('Lốc'),
('Chai'),
('Hộp'),
('Gói');

-- === PRODUCTS ===
INSERT INTO products (name, description, price, sku, category_id, image_url, created_at)
VALUES
('Nước suối Lavie 500ml', 'Nước uống đóng chai', 5000, 'LV500ML', 1, 'https://example.com/lavie500.jpg', NOW()),
('Sữa tươi Vinamilk 1L', 'Sữa tiệt trùng nguyên chất', 27000, 'VM1L', 2, 'https://example.com/vm1l.jpg', NOW()),
('Mì tôm Hảo Hảo', 'Mì gói vị tôm chua cay', 3500, 'HHG1', 3, 'https://example.com/haohao.jpg', NOW()),
('Bánh Oreo', 'Bánh quy kẹp kem sô-cô-la', 10000, 'OREO01', 4, 'https://example.com/oreo.jpg', NOW()),
('Coca-Cola lon 330ml', 'Nước ngọt có ga', 8500, 'COCA330', 5, 'https://example.com/coca330.jpg', NOW());

-- === PRODUCT_UNITS ===
INSERT INTO product_units (product_id, unit_id, conversion_rate, is_base_unit) VALUES
(1, 3, 1, TRUE),      -- Lavie - Chai là đơn vị cơ bản
(1, 1, 24, FALSE),    -- Lavie - Thùng = 24 chai

(2, 3, 1, TRUE),      -- Vinamilk - Chai (1L) là đơn vị cơ bản
(2, 1, 12, FALSE),    -- Vinamilk - Thùng = 12 chai

(3, 5, 1, TRUE),      -- Hảo Hảo - Gói là cơ bản
(3, 2, 10, FALSE),    -- Hảo Hảo - Lốc = 10 gói

(4, 4, 1, TRUE),      -- Oreo - Hộp là đơn vị cơ bản
(4, 1, 20, FALSE),    -- Oreo - Thùng = 20 hộp

(5, 3, 1, TRUE),      -- Coca - Lon là đơn vị cơ bản
(5, 2, 6, FALSE);     -- Coca - Lốc = 6 lon

-- === SHIPPING ADDRESSES ===
INSERT INTO shipping_addresses (user_id, recipient_name, phone, address_line, is_default)
VALUES 
(1, 'Alice Smith', '0909123456', '123 Trần Hưng Đạo, HN', TRUE),
(2, 'Thảo Lê', '0909876543', '456 Nguyễn Trãi, HCM', TRUE),
(3, 'Ngọc Anh', '0912345678', '789 Lê Duẩn, ĐN', TRUE),
(4, 'Kiên Trung', '0923456789', '101 Trần Phú, Cần Thơ', TRUE),
(5, 'Linh Thủy', '0934567890', '202 Quang Trung, Huế', TRUE);

-- === CART ITEMS ===
INSERT INTO cart_items (user_id, product_id, quantity, added_at)
VALUES 
(1, 1, 2, NOW()),
(2, 3, 1, NOW()),
(3, 2, 4, NOW()),
(4, 5, 3, NOW()),
(5, 4, 1, NOW());
