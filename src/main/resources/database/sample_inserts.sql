-- === USERS ===
INSERT INTO users (username, email, password_hash, role, first_name, last_name, phone, address, created_at, points)
VALUES 
('imarshall', 'yvonnestephens@salas.com', '29859f9c4a478d913f7922fbb084304dfccf2fcc3ff556ab72c352771c5f0a7d', 'admin', 'Carla', 'Gardner', '(428)234-7573x45966', '8363 Steven Port Apt. 605, North Sheri, ND 19406', '2024-06-15 04:22:45', 252),
('yolandagrimes', 'fmorris@hall.info', 'e182740d40c7f75c1342235b3fbcd06862ffbc0a16cfb11b97c0bd75ab0d78be', 'admin', 'Elizabeth', 'Russell', '(474)298-0762', 'USCGC Barnes, FPO AA 91856', '2024-11-10 04:22:45', 107),
('troycollins', 'allenalbert@yahoo.com', '736129c0892e5ad8c8d768b1a4be308ba6dd1fc129ccee10eecf87a7b5e4e52b', 'admin', 'Stephanie', 'Meadows', '399-958-0835x98600', '470 Hicks Grove Apt. 222, Comptonborough, ME 51166', '2024-10-08 04:22:45', 187),
('hnorman', 'urhodes@thomas-gordon.net', '8d07e7ccc41b457a77d93349dc69eb42adb3b6cdf8d9f68249d3758e0be2ffd3', 'user', 'Derek', 'Palmer', '+1-241-726-5324', '317 Lopez Loop, Nicoleborough, UT 83171', '2024-11-13 04:22:45', 154),
('christinejames', 'catherine22@nash.com', '87a93cd8aeb4449733a06f809732df031c75ea8d49ec8c61fa6bfaf062d8243a', 'admin', 'Christina', 'Stone', '100.970.6117', 'Unit 8126 Box 8270, DPO AP 56619', '2024-09-04 04:22:45', 271);

-- === SUPPLIERS ===
INSERT INTO suppliers (name, contact_info, address, email, phone)
VALUES 
('Williams PLC', '218.500.8219x85653', '8525 Alexander Heights, South Joseph, MD 18677', 'jenniferdalton@garcia.com', '483-937-8077x6800'),
('Taylor and Sons', '858-362-0548x8304', '937 Maria Cliff Apt. 738, Wangburgh, RI 98273', 'melissa80@hotmail.com', '913.508.8056x936'),
('Brown-Ware', '(312)153-7883x516', '5693 Katelyn Ford Suite 586, Sullivanbury, CO 49324', 'alexandercook@montes-bray.com', '(111)998-2429'),
('Cain-Turner', '001-941-537-0681', '01941 Michael Plaza Suite 879, Cesarport, DE 09795', 'amanda87@hall.com', '286-759-5344x70910'),
('Matthews PLC', '+1-487-151-7203x1629', '12208 Vanessa Summit, Judychester, KS 81092', 'marycarter@lee.com', '+1-180-584-0315x472');

-- === CATEGORIES ===
INSERT INTO categories (name, description)
VALUES 
('Building', 'Court degree fight no local everybody.'),
('Agree', 'Out purpose especially sound.'),
('Mission', 'Tend room inside plant.'),
('Social', 'Mean red party product.'),
('Do', 'Force take mission must middle today he.');

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

-- === PRODUCTS ===
INSERT INTO products (name, description, price, sku, category_id, stock_quantity, image_url, created_at)
VALUES 
('Organic Honey', '100% natural honey from highland bees', 120.00, 'SKU10001', 1, 200, 'https://example.com/images/honey.jpg', NOW()),
('Almond Milk', 'Dairy-free milk made from almonds', 55.00, 'SKU10002', 2, 300, 'https://example.com/images/almond_milk.jpg', NOW()),
('Brown Rice', 'Unpolished brown rice, healthy option', 30.00, 'SKU10003', 3, 400, 'https://example.com/images/brown_rice.jpg', NOW()),
('Olive Oil', 'Cold-pressed virgin olive oil', 90.00, 'SKU10004', 1, 250, 'https://example.com/images/olive_oil.jpg', NOW()),
('Chia Seeds', 'High in fiber and omega-3', 45.00, 'SKU10005', 2, 150, 'https://example.com/images/chia_seeds.jpg', NOW());

-- === SHIPPING ADDRESSES ===
INSERT INTO shipping_addresses (user_id, recipient_name, phone, address_line, is_default)
VALUES 
(1, 'Alice Smith', '0988000111', '101 Main St, District 1, HCM', TRUE),
(2, 'Bob Johnson', '0977555222', '202 Second St, District 3, HCM', FALSE),
(3, 'Charlie Brown', '0966444333', '303 Third St, District 5, HCM', TRUE),
(4, 'Diana Prince', '0955333444', '404 Fourth St, Binh Thanh, HCM', FALSE),
(5, 'Evan Davis', '0944222555', '505 Fifth St, District 7, HCM', TRUE);

-- === PROMOTION PRODUCTS ===
INSERT INTO promotion_products (promotion_id, product_id)
VALUES 
(1, 1),
(1, 2),
(2, 3),
(3, 4),
(4, 5);

-- === CART ITEMS ===
INSERT INTO cart_items (user_id, product_id, quantity, added_at)
VALUES 
(1, 1, 2, NOW()),
(2, 3, 1, NOW()),
(3, 2, 4, NOW()),
(4, 5, 3, NOW()),
(5, 4, 1, NOW());
