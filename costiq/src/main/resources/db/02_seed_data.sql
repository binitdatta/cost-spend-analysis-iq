-- =============================================================
-- CostIQ Seed Data  — GlobalBite Foods Inc.
-- =============================================================

USE costiq_db;

-- REGIONS
INSERT INTO regions (code, name, currency) VALUES
('NA',  'North America',  'USD'),
('EU',  'Europe',         'EUR'),
('APAC','Asia Pacific',   'USD'),
('AUS', 'Australia',      'AUD'),
('AF',  'Africa',         'USD');

-- COUNTRIES
INSERT INTO countries (region_id, code, name) VALUES
(1, 'US',  'United States'),
(1, 'CA',  'Canada'),
(1, 'MX',  'Mexico'),
(2, 'GB',  'United Kingdom'),
(2, 'DE',  'Germany'),
(2, 'FR',  'France'),
(2, 'IT',  'Italy'),
(2, 'ES',  'Spain'),
(3, 'CN',  'China'),
(3, 'JP',  'Japan'),
(3, 'IN',  'India'),
(3, 'SG',  'Singapore'),
(4, 'AU',  'Australia'),
(4, 'NZ',  'New Zealand'),
(5, 'ZA',  'South Africa'),
(5, 'NG',  'Nigeria'),
(5, 'KE',  'Kenya'),
(5, 'EG',  'Egypt');

-- FISCAL PERIODS  (FY2023–FY2025)
INSERT INTO fiscal_periods (fiscal_year, quarter, period_name, start_date, end_date, is_closed) VALUES
(2023, 1, 'FY2023-Q1', '2023-01-01', '2023-03-31', 1),
(2023, 2, 'FY2023-Q2', '2023-04-01', '2023-06-30', 1),
(2023, 3, 'FY2023-Q3', '2023-07-01', '2023-09-30', 1),
(2023, 4, 'FY2023-Q4', '2023-10-01', '2023-12-31', 1),
(2024, 1, 'FY2024-Q1', '2024-01-01', '2024-03-31', 1),
(2024, 2, 'FY2024-Q2', '2024-04-01', '2024-06-30', 1),
(2024, 3, 'FY2024-Q3', '2024-07-01', '2024-09-30', 1),
(2024, 4, 'FY2024-Q4', '2024-10-01', '2024-12-31', 1),
(2025, 1, 'FY2025-Q1', '2025-01-01', '2025-03-31', 1),
(2025, 2, 'FY2025-Q2', '2025-04-01', '2025-06-30', 0),
(2025, 3, 'FY2025-Q3', '2025-07-01', '2025-09-30', 0),
(2025, 4, 'FY2025-Q4', '2025-10-01', '2025-12-31', 0);

-- COST CENTERS
INSERT INTO cost_centers (code, name, department, manager_name, budget_usd) VALUES
('CC-FOOD-NA',   'Food Procurement - North America',   'Procurement',  'Sandra Kim',      15000000.00),
('CC-FOOD-EU',   'Food Procurement - Europe',           'Procurement',  'Hans Weber',      12000000.00),
('CC-FOOD-APAC', 'Food Procurement - Asia Pacific',     'Procurement',  'Mei Tanaka',      10000000.00),
('CC-FOOD-AUS',  'Food Procurement - Australia',        'Procurement',  'James Callahan',   4500000.00),
('CC-FOOD-AF',   'Food Procurement - Africa',           'Procurement',  'Amara Diallo',     3000000.00),
('CC-PKG-GLOBAL','Packaging - Global Operations',       'Supply Chain', 'Rachel Torres',    8000000.00),
('CC-PKG-EU',    'Packaging - Europe',                  'Supply Chain', 'Pierre Leblanc',   5000000.00),
('CC-PROMO-NA',  'Promotions & Campaigns - NA',         'Marketing',    'Tyler Brooks',     6000000.00),
('CC-PROMO-EU',  'Promotions & Campaigns - EU',         'Marketing',    'Sophie Müller',    5500000.00),
('CC-PROMO-APAC','Promotions & Campaigns - APAC',       'Marketing',    'Li Wei',           4000000.00),
('CC-PROMO-GLOBAL','Global Campaign Management',        'Marketing',    'Victor Huang',    20000000.00),
('CC-OPS-GLOBAL','Global Operations',                   'Operations',   'Diana Santos',    25000000.00);

-- SUPPLIERS
INSERT INTO suppliers (supplier_code, name, category, country_id, contact_email, contract_tier) VALUES
('SUP-FOOD-001', 'AgriPrime USA',           'FOOD',       1,  'orders@agriprime.com',      'PREFERRED'),
('SUP-FOOD-002', 'EuroGrain GmbH',          'FOOD',       5,  'supply@eurograin.de',       'PREFERRED'),
('SUP-FOOD-003', 'AsiaCrop Co. Ltd',        'FOOD',       9,  'procurement@asiacrop.cn',   'APPROVED'),
('SUP-FOOD-004', 'OceanFresh Australia',    'FOOD',       13, 'orders@oceanfresh.au',      'APPROVED'),
('SUP-FOOD-005', 'Sahara Grains Ltd',       'FOOD',       15, 'sales@saharagrains.co.za',  'PROVISIONAL'),
('SUP-PKG-001',  'PackSmart Inc.',          'PACKAGING',  1,  'sales@packsmart.com',       'PREFERRED'),
('SUP-PKG-002',  'EcoPack Europe',          'PACKAGING',  4,  'info@ecopack.co.uk',        'PREFERRED'),
('SUP-PKG-003',  'GreenWrap Singapore',     'PACKAGING',  12, 'orders@greenwrap.sg',       'APPROVED'),
('SUP-PKG-004',  'RecyclePack GmbH',        'PACKAGING',  5,  'sales@recyclepack.de',      'APPROVED'),
('SUP-TOY-001',  'FunFactory China',        'TOYS',       9,  'export@funfactory.cn',      'PREFERRED'),
('SUP-TOY-002',  'PlayCraft Inc.',          'TOYS',       1,  'orders@playcraft.com',      'PREFERRED'),
('SUP-TOY-003',  'ToyZone India',           'TOYS',       11, 'sales@toyzone.in',          'APPROVED'),
('SUP-MKT-001',  'GlobalMedia Group',       'MARKETING',  1,  'campaigns@globalmedia.com', 'PREFERRED'),
('SUP-MKT-002',  'EuroAds Agency',          'MARKETING',  6,  'contact@euroads.fr',        'APPROVED'),
('SUP-LOG-001',  'SwiftLogistics Global',   'LOGISTICS',  1,  'ops@swiftlog.com',          'PREFERRED');

-- FOOD CATEGORIES
INSERT INTO food_categories (code, name, description) VALUES
('BURGER',   'Burgers & Sandwiches',  'Beef, chicken, fish, and plant-based patties and sandwiches'),
('CHICKEN',  'Chicken Products',      'Nuggets, strips, tenders, and whole pieces'),
('SIDES',    'Sides & Snacks',        'Fries, hash browns, salads, apple slices'),
('BEVERAGES','Beverages',             'Soft drinks, juices, water, milkshakes'),
('BREAKFAST','Breakfast Items',       'Muffins, pancakes, egg items, breakfast burritos'),
('DESSERTS', 'Desserts & Sweets',     'Ice cream, pies, cookies, sundaes'),
('SAUCES',   'Sauces & Condiments',   'Ketchup, mayo, dips, dressings'),
('HEALTHY',  'Healthy Options',       'Wraps, grilled items, fruit cups, yogurt');

-- FOOD ITEMS
INSERT INTO food_items (sku, name, category_id, unit_of_measure, base_cost_usd, calories_per_unit, is_allergen_free) VALUES
('FOOD-B001', 'Classic Beef Patty 4oz',          1, 'KG',    8.50,  285, 0),
('FOOD-B002', 'Double Beef Patty 8oz',            1, 'KG',   10.20,  540, 0),
('FOOD-B003', 'Crispy Chicken Fillet',            1, 'UNIT',  1.80,  320, 0),
('FOOD-B004', 'Plant-Based Burger Patty',         1, 'KG',   14.75,  250, 1),
('FOOD-C001', 'Chicken Nuggets (10pc)',           2, 'UNIT',  2.40,  470, 0),
('FOOD-C002', 'Spicy Chicken Tenders 3pc',        2, 'UNIT',  1.95,  340, 0),
('FOOD-C003', 'Grilled Chicken Breast',           2, 'UNIT',  2.10,  210, 1),
('FOOD-S001', 'French Fries Large 170g',          3, 'UNIT',  0.65,  490, 1),
('FOOD-S002', 'Apple Slices Pack',                3, 'UNIT',  0.55,   35, 1),
('FOOD-S003', 'Side Salad Mix',                   3, 'KG',    3.20,   20, 1),
('FOOD-V001', 'Cola Syrup Concentrate 1L',        4, 'LITER', 4.80,  390, 1),
('FOOD-V002', 'Orange Juice 100% 200ml',          4, 'UNIT',  0.75,   90, 1),
('FOOD-V003', 'Chocolate Milkshake Mix',          4, 'KG',    6.50,  450, 0),
('FOOD-BK001','Buttermilk Pancake Mix 1kg',       5, 'KG',    3.10,  160, 0),
('FOOD-BK002','Egg McMuffin Assembly Pack',       5, 'UNIT',  1.25,  300, 0),
('FOOD-D001', 'Soft Serve Ice Cream Mix 1kg',     6, 'KG',    5.20,  230, 0),
('FOOD-D002', 'Apple Pie Filling 1kg',            6, 'KG',    4.10,  240, 0),
('FOOD-SC001','Tomato Ketchup Portion 10g',       7, 'UNIT',  0.08,   11, 1),
('FOOD-SC002','Honey Mustard Dip 25ml',           7, 'UNIT',  0.12,   60, 0),
('FOOD-H001', 'Quinoa Veggie Wrap',               8, 'UNIT',  2.85,  380, 1);

-- PACKAGING TYPES
INSERT INTO packaging_types (code, name, material, is_recyclable) VALUES
('PT-PAPER',  'Printed Paper Wrap',          'PAPER',         1),
('PT-BOX',    'Cardboard Happy Meal Box',    'CARDBOARD',     1),
('PT-CLAM',   'Clamshell Container',         'CARDBOARD',     1),
('PT-CUP-SM', 'Small Drink Cup',             'PAPER',         1),
('PT-CUP-LG', 'Large Drink Cup',             'PAPER',         1),
('PT-BAG',    'Carry Bag',                   'PAPER',         1),
('PT-TRAY',   'Serving Tray Liner',          'PAPER',         1),
('PT-FOIL',   'Foil Wrapper',                'FOIL',          0),
('PT-BIO',    'Biodegradable Container',     'BIODEGRADABLE', 1),
('PT-TOYBOX', 'Toy Inner Box',               'CARDBOARD',     1);

-- PACKAGING ITEMS
INSERT INTO packaging_items (sku, name, packaging_type_id, dimensions_cm, weight_grams, base_cost_usd, min_order_qty) VALUES
('PKG-001', 'Standard Burger Wrap Red',       1, '30x20',      8.0,  0.0180, 50000),
('PKG-002', 'Happy Meal Box Classic',         2, '18x12x10',  45.0,  0.1250, 20000),
('PKG-003', 'Happy Meal Box Premium',         2, '20x14x12',  60.0,  0.1750, 15000),
('PKG-004', 'Medium Clamshell Fry Box',       3, '12x10x8',   30.0,  0.0950, 30000),
('PKG-005', 'Small Drink Cup 12oz',           4, 'D8xH14',    15.0,  0.0620, 40000),
('PKG-006', 'Large Drink Cup 32oz',           5, 'D12xH22',   22.0,  0.0980, 30000),
('PKG-007', 'Takeout Carry Bag Large',        6, '35x28x12', 120.0,  0.2200, 10000),
('PKG-008', 'Tray Liner 35x45cm',             7, '35x45',      8.5,  0.0150, 100000),
('PKG-009', 'Burger Foil Wrap Gold',          8, '32x22',     10.0,  0.0220, 40000),
('PKG-010', 'Eco Bowl Biodegradable',         9, 'D16xH6',    35.0,  0.1450, 15000),
('PKG-011', 'Toy Inner Display Box',         10, '10x8x6',    25.0,  0.0850, 25000),
('PKG-012', 'Kids Meal Bag Branded',          6, '28x22x10', 100.0,  0.1950, 12000),
('PKG-013', 'Sauce Packet Foil 10ml',         8, '6x4',        2.5,  0.0050, 200000),
('PKG-014', 'Paper Straw Pack 10ct',          1, '22x2',       4.0,  0.0320, 50000),
('PKG-015', 'Premium Carry Bag Gift',         6, '40x32x15', 160.0,  0.3500, 5000);

-- TOY CATEGORIES
INSERT INTO toy_categories (code, name, age_range) VALUES
('TOY-ACT',   'Action Figures',         '5-12'),
('TOY-PUZ',   'Puzzles & Brain Games',  '4-10'),
('TOY-STUFF', 'Stuffed Animals',        '2-8'),
('TOY-DINO',  'Dinosaur Collection',    '3-10'),
('TOY-SPACE', 'Space Explorer Series',  '5-12'),
('TOY-CAR',   'Mini Vehicle Series',    '3-10'),
('TOY-BUILD', 'Build & Create Sets',    '4-12'),
('TOY-CARD',  'Collectible Cards',      '5-14');

-- TOY ITEMS
INSERT INTO toy_items (sku, name, toy_category_id, licensed_ip, material, safety_certified, unit_cost_usd) VALUES
('TOY-001', 'SuperHero Rocket Figure',        1, 'Original',       'ABS Plastic',     1, 0.85),
('TOY-002', 'Galaxy Explorer Action Pack',    5, 'Original',       'ABS Plastic',     1, 1.20),
('TOY-003', 'Mini T-Rex Dino',               4, 'Original',       'Soft Rubber',     1, 0.65),
('TOY-004', 'Plush Baby Penguin',             3, 'Original',       'Polyester Plush', 1, 1.10),
('TOY-005', 'Race Car Classic Red',           6, 'Original',       'Die-cast Metal',  1, 1.45),
('TOY-006', 'Race Car Sports Yellow',         6, 'Original',       'Die-cast Metal',  1, 1.45),
('TOY-007', 'Puzzle Cube 4x4',               2, 'Original',       'ABS Plastic',     1, 0.95),
('TOY-008', 'Mini Build Spaceship',           7, 'Original',       'ABS Plastic',     1, 1.65),
('TOY-009', 'Dino Egg Surprise',              4, 'Original',       'Soft Rubber',     1, 0.75),
('TOY-010', 'Animal Kingdom Cards 5ct',       8, 'Original',       'Cardstock',       1, 0.35),
('TOY-011', 'Plush Lion Cub',                 3, 'Original',       'Polyester Plush', 1, 1.20),
('TOY-012', 'Mini Helicopter Blue',           6, 'Original',       'ABS Plastic',     1, 1.30),
('TOY-013', 'Sticker Activity Book',          2, 'Original',       'Paper',           1, 0.55),
('TOY-014', 'Glow-in-Dark Star Set',          5, 'Original',       'Plastic/Paint',   1, 0.90),
('TOY-015', 'Slime Volcano Kit',              7, 'Original',       'Non-toxic Polymer',1,1.85);

-- CAMPAIGNS
INSERT INTO campaigns (campaign_code, name, description, campaign_type, status, start_date, end_date, target_region, budget_usd, created_by) VALUES
('CAMP-2024-DINO',   'Dinosaur Discovery 2024',      'Global dino toy collection across all kids meals',         'GLOBAL',   'COMPLETED', '2024-01-15', '2024-03-31', 'ALL',  5500000.00, 'victor.huang'),
('CAMP-2024-SPACE',  'Space Explorer Summer 2024',   'Space-themed toy series for summer campaign',              'GLOBAL',   'COMPLETED', '2024-06-01', '2024-08-31', 'ALL',  7200000.00, 'victor.huang'),
('CAMP-2024-RACE',   'Rev It Up! Race Cars 2024',    'Die-cast race car collection, limited edition',            'REGIONAL', 'COMPLETED', '2024-09-01', '2024-11-30', 'NA',   3800000.00, 'tyler.brooks'),
('CAMP-2025-PLUSH',  'Plush Pals Winter 2025',       'Soft plush animal collection for winter season',           'GLOBAL',   'ACTIVE',    '2025-01-01', '2025-03-31', 'ALL',  6000000.00, 'victor.huang'),
('CAMP-2025-BUILD',  'Build It Big Spring 2025',     'Building set toys to encourage STEM learning',             'GLOBAL',   'ACTIVE',    '2025-04-01', '2025-06-30', 'ALL',  8500000.00, 'victor.huang'),
('CAMP-2025-GLAM',   'Glow & Grow EU Summer 2025',   'Glow-in-dark and activity sets for EU summer',             'REGIONAL', 'PLANNED',   '2025-07-01', '2025-09-30', 'EU',   4200000.00, 'sophie.muller'),
('CAMP-2025-OCEAN',  'Ocean Adventure APAC 2025',    'Ocean animals plush series for APAC markets',              'REGIONAL', 'PLANNED',   '2025-07-01', '2025-09-30', 'APAC', 3600000.00, 'li.wei'),
('CAMP-2025-CARDS',  'Collector Cards Global 2025',  'Animal Kingdom collectible cards — 40 card set',           'GLOBAL',   'ACTIVE',    '2025-02-01', '2025-12-31', 'ALL',  9000000.00, 'victor.huang');

-- =============================================================
-- FOOD COST ENTRIES  (substantial seed data)
-- =============================================================

INSERT INTO food_cost_entries (food_item_id, supplier_id, cost_center_id, fiscal_period_id, country_id, quantity, unit_cost_usd, invoice_ref, po_number, notes, entry_date, created_by) VALUES
-- FY2024-Q1  North America
(1,  1, 1, 5,  1,  250000, 8.60, 'INV-2024-0101', 'PO-2024-0101', 'Q1 bulk beef delivery',   '2024-01-15', 'system.seed'),
(5,  1, 1, 5,  1,  180000, 2.45, 'INV-2024-0102', 'PO-2024-0102', 'Nuggets Q1 NA',            '2024-01-20', 'system.seed'),
(8,  1, 1, 5,  1,  500000, 0.68, 'INV-2024-0103', 'PO-2024-0103', 'Fries Q1 USA',             '2024-02-01', 'system.seed'),
(11, 1, 1, 5,  1,   80000, 4.90, 'INV-2024-0104', 'PO-2024-0104', 'Cola syrup Q1',            '2024-02-10', 'system.seed'),
(4,  1, 1, 5,  1,   30000,15.10, 'INV-2024-0105', 'PO-2024-0105', 'Plant-based patties',      '2024-02-15', 'system.seed'),
-- FY2024-Q1  Europe
(1,  2, 2, 5,  4,  120000, 9.20, 'INV-2024-0201', 'PO-2024-0201', 'Beef UK Q1',               '2024-01-18', 'system.seed'),
(1,  2, 2, 5,  5,   90000, 8.95, 'INV-2024-0202', 'PO-2024-0202', 'Beef Germany Q1',          '2024-01-22', 'system.seed'),
(5,  2, 2, 5,  6,   75000, 2.52, 'INV-2024-0203', 'PO-2024-0203', 'Nuggets France Q1',        '2024-02-05', 'system.seed'),
(8,  2, 2, 5,  7,  200000, 0.72, 'INV-2024-0204', 'PO-2024-0204', 'Fries Italy Q1',           '2024-02-12', 'system.seed'),
-- FY2024-Q1  APAC
(1,  3, 3, 5,  9,  200000, 8.75, 'INV-2024-0301', 'PO-2024-0301', 'Beef China Q1',            '2024-01-25', 'system.seed'),
(3,  3, 3, 5,  10,  90000, 1.88, 'INV-2024-0302', 'PO-2024-0302', 'Chicken Japan Q1',         '2024-02-08', 'system.seed'),
(8,  3, 3, 5,  11, 300000, 0.66, 'INV-2024-0303', 'PO-2024-0303', 'Fries India Q1',           '2024-02-18', 'system.seed'),
-- FY2024-Q2  North America
(1,  1, 1, 6,  1,  270000, 8.65, 'INV-2024-0401', 'PO-2024-0401', 'Q2 beef NA',               '2024-04-10', 'system.seed'),
(5,  1, 1, 6,  1,  190000, 2.48, 'INV-2024-0402', 'PO-2024-0402', 'Nuggets Q2 NA',            '2024-04-15', 'system.seed'),
(7,  1, 1, 6,  1,   60000, 2.15, 'INV-2024-0403', 'PO-2024-0403', 'Grilled chicken Q2',       '2024-05-01', 'system.seed'),
(16, 1, 1, 6,  1,   40000, 5.30, 'INV-2024-0404', 'PO-2024-0404', 'Ice cream mix Q2',         '2024-05-10', 'system.seed'),
(20, 1, 1, 6,  1,   25000, 2.90, 'INV-2024-0405', 'PO-2024-0405', 'Quinoa wraps Q2',          '2024-05-20', 'system.seed'),
-- FY2024-Q2  Europe
(4,  2, 2, 6,  5,   45000,15.20, 'INV-2024-0501', 'PO-2024-0501', 'Plant-based Germany Q2',   '2024-04-08', 'system.seed'),
(10, 2, 2, 6,  4,   55000, 3.30, 'INV-2024-0502', 'PO-2024-0502', 'Salad mix UK Q2',          '2024-04-22', 'system.seed'),
(12, 2, 2, 6,  6,  100000, 0.78, 'INV-2024-0503', 'PO-2024-0503', 'OJ France Q2',             '2024-05-15', 'system.seed'),
-- FY2024-Q3  APAC
(1,  3, 3, 7,  9,  280000, 8.80, 'INV-2024-0601', 'PO-2024-0601', 'Beef China Q3',            '2024-07-05', 'system.seed'),
(5,  3, 3, 7,  10,  85000, 2.50, 'INV-2024-0602', 'PO-2024-0602', 'Nuggets Japan Q3',         '2024-07-20', 'system.seed'),
(6,  3, 3, 7,  11, 110000, 2.00, 'INV-2024-0603', 'PO-2024-0603', 'Spicy tenders India Q3',   '2024-08-02', 'system.seed'),
-- FY2024-Q3  Australia
(1,  4, 4, 7,  13,  70000, 9.10, 'INV-2024-0701', 'PO-2024-0701', 'Beef Australia Q3',        '2024-07-10', 'system.seed'),
(8,  4, 4, 7,  13, 150000, 0.70, 'INV-2024-0702', 'PO-2024-0702', 'Fries AU Q3',              '2024-07-25', 'system.seed'),
-- FY2024-Q4  North America
(1,  1, 1, 8,  1,  290000, 8.70, 'INV-2024-0801', 'PO-2024-0801', 'Q4 beef NA holiday',       '2024-10-05', 'system.seed'),
(5,  1, 1, 8,  1,  210000, 2.50, 'INV-2024-0802', 'PO-2024-0802', 'Nuggets holiday season',   '2024-10-18', 'system.seed'),
(14, 1, 1, 8,  1,   35000, 3.15, 'INV-2024-0803', 'PO-2024-0803', 'Pancake mix Q4',           '2024-11-01', 'system.seed'),
(17, 1, 1, 8,  1,   28000, 4.20, 'INV-2024-0804', 'PO-2024-0804', 'Apple pie filling Q4',     '2024-11-15', 'system.seed'),
-- FY2025-Q1  North America
(1,  1, 1, 9,  1,  260000, 8.85, 'INV-2025-0101', 'PO-2025-0101', 'Q1 2025 beef NA',          '2025-01-10', 'system.seed'),
(5,  1, 1, 9,  1,  195000, 2.52, 'INV-2025-0102', 'PO-2025-0102', 'Nuggets Q1 2025 NA',       '2025-01-22', 'system.seed'),
(4,  1, 1, 9,  1,   35000,15.25, 'INV-2025-0103', 'PO-2025-0103', 'Plant-based Q1 2025',      '2025-02-05', 'system.seed'),
-- FY2025-Q1  Africa
(8,  5, 5, 9,  15,  80000, 0.62, 'INV-2025-0201', 'PO-2025-0201', 'Fries South Africa Q1',    '2025-01-18', 'system.seed'),
(3,  5, 5, 9,  16,  40000, 1.75, 'INV-2025-0202', 'PO-2025-0202', 'Chicken Nigeria Q1',       '2025-02-10', 'system.seed'),
-- FY2025-Q2  North America (recent)
(1,  1, 1, 10, 1,  265000, 8.90, 'INV-2025-0301', 'PO-2025-0301', 'Q2 2025 beef NA',          '2025-04-08', 'system.seed'),
(7,  1, 1, 10, 1,   65000, 2.18, 'INV-2025-0302', 'PO-2025-0302', 'Grilled chicken Q2 2025',  '2025-04-20', 'system.seed'),
(20, 1, 1, 10, 1,   30000, 2.95, 'INV-2025-0303', 'PO-2025-0303', 'Quinoa wraps Q2 2025',     '2025-05-05', 'system.seed'),
(11, 1, 1, 10, 1,   85000, 4.95, 'INV-2025-0304', 'PO-2025-0304', 'Cola syrup Q2 2025',       '2025-05-15', 'system.seed');

-- =============================================================
-- PACKAGING COST ENTRIES
-- =============================================================

INSERT INTO packaging_cost_entries (packaging_item_id, supplier_id, cost_center_id, fiscal_period_id, country_id, quantity, unit_cost_usd, invoice_ref, po_number, notes, entry_date, created_by) VALUES
-- FY2024-Q1
(2,  6, 6, 5,  1, 1200000, 0.1280, 'PKG-INV-2024-0101', 'PKG-PO-2024-0101', 'HM Box Q1 NA',          '2024-01-12', 'system.seed'),
(1,  6, 6, 5,  1, 3000000, 0.0185, 'PKG-INV-2024-0102', 'PKG-PO-2024-0102', 'Burger wrap Q1 NA',     '2024-01-15', 'system.seed'),
(5,  6, 6, 5,  1, 2500000, 0.0635, 'PKG-INV-2024-0103', 'PKG-PO-2024-0103', 'Sm cup Q1 NA',          '2024-01-20', 'system.seed'),
(6,  6, 6, 5,  1,  800000, 0.1010, 'PKG-INV-2024-0104', 'PKG-PO-2024-0104', 'Lg cup Q1 NA',          '2024-01-25', 'system.seed'),
(3,  7, 7, 5,  4,  600000, 0.1790, 'PKG-INV-2024-0201', 'PKG-PO-2024-0201', 'Premium HM box UK Q1',  '2024-01-18', 'system.seed'),
(2,  7, 7, 5,  5,  500000, 0.1300, 'PKG-INV-2024-0202', 'PKG-PO-2024-0202', 'HM Box Germany Q1',     '2024-01-22', 'system.seed'),
(10, 7, 7, 5,  6,  300000, 0.1480, 'PKG-INV-2024-0203', 'PKG-PO-2024-0203', 'Eco bowl France Q1',    '2024-02-05', 'system.seed'),
(8,  8, 6, 5,  9,  200000, 0.0230, 'PKG-INV-2024-0301', 'PKG-PO-2024-0301', 'Foil wrap China Q1',    '2024-01-28', 'system.seed'),
-- FY2024-Q2
(2,  6, 6, 6,  1, 1300000, 0.1285, 'PKG-INV-2024-0401', 'PKG-PO-2024-0401', 'HM Box Q2 NA',          '2024-04-08', 'system.seed'),
(11, 6, 6, 6,  1,  900000, 0.0870, 'PKG-INV-2024-0402', 'PKG-PO-2024-0402', 'Toy inner box Q2',      '2024-04-15', 'system.seed'),
(7,  6, 6, 6,  1,  250000, 0.2250, 'PKG-INV-2024-0403', 'PKG-PO-2024-0403', 'Carry bag Q2 NA',       '2024-05-01', 'system.seed'),
(13, 6, 6, 6,  1, 5000000, 0.0052, 'PKG-INV-2024-0404', 'PKG-PO-2024-0404', 'Sauce packets Q2',      '2024-05-10', 'system.seed'),
(3,  7, 7, 6,  7,  550000, 0.1800, 'PKG-INV-2024-0501', 'PKG-PO-2024-0501', 'Premium HM box Italy',  '2024-04-12', 'system.seed'),
-- FY2024-Q3
(2,  6, 6, 7,  1, 1400000, 0.1290, 'PKG-INV-2024-0601', 'PKG-PO-2024-0601', 'HM Box Q3 NA summer',   '2024-07-08', 'system.seed'),
(3,  8, 6, 7,  12, 400000, 0.1790, 'PKG-INV-2024-0602', 'PKG-PO-2024-0602', 'Premium HM box APAC Q3','2024-07-15', 'system.seed'),
(2,  9, 7, 7,  5,  480000, 0.1320, 'PKG-INV-2024-0603', 'PKG-PO-2024-0603', 'HM Box Germany Q3',     '2024-07-20', 'system.seed'),
-- FY2024-Q4
(2,  6, 6, 8,  1, 1500000, 0.1295, 'PKG-INV-2024-0701', 'PKG-PO-2024-0701', 'HM Box Q4 holiday NA',  '2024-10-05', 'system.seed'),
(15, 6, 6, 8,  1,   80000, 0.3600, 'PKG-INV-2024-0702', 'PKG-PO-2024-0702', 'Premium gift bag Q4',   '2024-10-20', 'system.seed'),
-- FY2025-Q1
(2,  6, 6, 9,  1, 1250000, 0.1300, 'PKG-INV-2025-0101', 'PKG-PO-2025-0101', 'HM Box Q1 2025 NA',     '2025-01-10', 'system.seed'),
(3,  7, 7, 9,  4,  580000, 0.1820, 'PKG-INV-2025-0201', 'PKG-PO-2025-0201', 'Premium box UK Q1 2025','2025-01-15', 'system.seed'),
(10, 7, 7, 9,  8,  350000, 0.1500, 'PKG-INV-2025-0202', 'PKG-PO-2025-0202', 'Eco bowl Spain Q1 2025','2025-02-08', 'system.seed'),
-- FY2025-Q2
(2,  6, 6, 10, 1, 1320000, 0.1310, 'PKG-INV-2025-0301', 'PKG-PO-2025-0301', 'HM Box Q2 2025 NA',     '2025-04-05', 'system.seed'),
(11, 6, 6, 10, 1, 1050000, 0.0880, 'PKG-INV-2025-0302', 'PKG-PO-2025-0302', 'Toy inner box Q2 2025', '2025-04-18', 'system.seed');

-- =============================================================
-- CAMPAIGN TOY ALLOCATIONS
-- =============================================================

INSERT INTO campaign_toy_allocations (campaign_id, toy_item_id, country_id, supplier_id, fiscal_period_id, quantity, unit_cost_usd, distribution_channel, invoice_ref, po_number, notes, entry_date, created_by) VALUES
-- CAMP-2024-DINO (id=1)
(1, 3, 1,  10, 5,  2000000, 0.68, 'RETAIL', 'TOY-INV-2024-001', 'TOY-PO-2024-001', 'Mini T-Rex NA Q1 2024',        '2024-01-20', 'system.seed'),
(1, 3, 4,  10, 5,  1200000, 0.70, 'RETAIL', 'TOY-INV-2024-002', 'TOY-PO-2024-002', 'Mini T-Rex UK Q1 2024',        '2024-01-22', 'system.seed'),
(1, 9, 5,  10, 5,   900000, 0.78, 'RETAIL', 'TOY-INV-2024-003', 'TOY-PO-2024-003', 'Dino Egg Germany Q1 2024',     '2024-01-25', 'system.seed'),
(1, 3, 9,  10, 5,  1500000, 0.66, 'RETAIL', 'TOY-INV-2024-004', 'TOY-PO-2024-004', 'Mini T-Rex China Q1 2024',     '2024-02-01', 'system.seed'),
(1, 9, 13, 10, 5,   450000, 0.72, 'RETAIL', 'TOY-INV-2024-005', 'TOY-PO-2024-005', 'Dino Egg AU Q1 2024',          '2024-02-05', 'system.seed'),
-- CAMP-2024-SPACE (id=2)
(2, 2, 1,  11, 7,  2500000, 1.22, 'RETAIL', 'TOY-INV-2024-101', 'TOY-PO-2024-101', 'Galaxy Explorer NA Q3 2024',   '2024-06-15', 'system.seed'),
(2, 2, 4,  11, 7,  1100000, 1.25, 'RETAIL', 'TOY-INV-2024-102', 'TOY-PO-2024-102', 'Galaxy Explorer UK Q3 2024',   '2024-06-20', 'system.seed'),
(2, 14,9,  10, 7,   800000, 0.92, 'RETAIL', 'TOY-INV-2024-103', 'TOY-PO-2024-103', 'Glow stars China Q3 2024',     '2024-06-25', 'system.seed'),
(2, 2, 13, 10, 7,   420000, 1.20, 'RETAIL', 'TOY-INV-2024-104', 'TOY-PO-2024-104', 'Galaxy Explorer AU Q3 2024',   '2024-07-01', 'system.seed'),
-- CAMP-2024-RACE (id=3)
(3, 5, 1,  11, 8,  1800000, 1.48, 'RETAIL', 'TOY-INV-2024-201', 'TOY-PO-2024-201', 'Race Car Red NA Q4 2024',      '2024-09-10', 'system.seed'),
(3, 6, 1,  11, 8,  1600000, 1.48, 'RETAIL', 'TOY-INV-2024-202', 'TOY-PO-2024-202', 'Race Car Yellow NA Q4 2024',   '2024-09-15', 'system.seed'),
(3, 5, 2,  11, 8,   600000, 1.50, 'RETAIL', 'TOY-INV-2024-203', 'TOY-PO-2024-203', 'Race Car Canada Q4 2024',      '2024-09-20', 'system.seed'),
-- CAMP-2025-PLUSH (id=4)
(4, 4, 1,  11, 9,  2200000, 1.12, 'RETAIL', 'TOY-INV-2025-001', 'TOY-PO-2025-001', 'Plush Penguin NA Q1 2025',     '2025-01-10', 'system.seed'),
(4, 11,4,  10, 9,  1000000, 1.25, 'RETAIL', 'TOY-INV-2025-002', 'TOY-PO-2025-002', 'Plush Lion UK Q1 2025',        '2025-01-15', 'system.seed'),
(4, 4, 9,  10, 9,  1800000, 1.08, 'RETAIL', 'TOY-INV-2025-003', 'TOY-PO-2025-003', 'Plush Penguin China Q1 2025',  '2025-01-20', 'system.seed'),
(4, 11,13, 10, 9,   400000, 1.22, 'RETAIL', 'TOY-INV-2025-004', 'TOY-PO-2025-004', 'Plush Lion AU Q1 2025',        '2025-01-25', 'system.seed'),
(4, 4, 15, 12, 9,   350000, 1.05, 'RETAIL', 'TOY-INV-2025-005', 'TOY-PO-2025-005', 'Plush Penguin ZA Q1 2025',     '2025-02-01', 'system.seed'),
-- CAMP-2025-BUILD (id=5)
(5, 8, 1,  11, 10, 2100000, 1.68, 'RETAIL', 'TOY-INV-2025-101', 'TOY-PO-2025-101', 'Spaceship build NA Q2 2025',   '2025-04-08', 'system.seed'),
(5, 8, 5,  10, 10,  900000, 1.70, 'RETAIL', 'TOY-INV-2025-102', 'TOY-PO-2025-102', 'Spaceship build DE Q2 2025',   '2025-04-12', 'system.seed'),
(5, 15,9,  10, 10, 1600000, 1.90, 'RETAIL', 'TOY-INV-2025-103', 'TOY-PO-2025-103', 'Slime volcano CN Q2 2025',     '2025-04-18', 'system.seed'),
-- CAMP-2025-CARDS (id=8)
(8, 10,1,  11, 9,  3000000, 0.36, 'RETAIL', 'TOY-INV-2025-201', 'TOY-PO-2025-201', 'Animal cards NA Q1 2025',      '2025-02-01', 'system.seed'),
(8, 10,4,  10, 9,  1500000, 0.38, 'RETAIL', 'TOY-INV-2025-202', 'TOY-PO-2025-202', 'Animal cards UK Q1 2025',      '2025-02-05', 'system.seed'),
(8, 10,9,  10, 9,  2500000, 0.35, 'RETAIL', 'TOY-INV-2025-203', 'TOY-PO-2025-203', 'Animal cards CN Q1 2025',      '2025-02-10', 'system.seed'),
(8, 10,13, 10, 9,   600000, 0.37, 'RETAIL', 'TOY-INV-2025-204', 'TOY-PO-2025-204', 'Animal cards AU Q1 2025',      '2025-02-15', 'system.seed'),
(8, 10,15, 12, 9,   500000, 0.34, 'RETAIL', 'TOY-INV-2025-205', 'TOY-PO-2025-205', 'Animal cards ZA Q1 2025',      '2025-02-20', 'system.seed'),
(8, 10,1,  11, 10, 3200000, 0.36, 'RETAIL', 'TOY-INV-2025-206', 'TOY-PO-2025-206', 'Animal cards NA Q2 2025',      '2025-04-05', 'system.seed');

-- =============================================================
-- CAMPAIGN MARKETING COSTS
-- =============================================================

INSERT INTO campaign_marketing_costs (campaign_id, cost_center_id, fiscal_period_id, cost_type, amount_usd, vendor_name, invoice_ref, description, entry_date, created_by) VALUES
(1, 8, 5,  'TV',           850000.00, 'NBCUniversal',        'MKT-INV-2024-001', 'Dino TV spots NA Q1',           '2024-01-15', 'system.seed'),
(1, 8, 5,  'DIGITAL',      420000.00, 'Google Ads',          'MKT-INV-2024-002', 'Dino digital banner NA Q1',     '2024-01-18', 'system.seed'),
(1, 9, 5,  'TV',           650000.00, 'ITV UK',              'MKT-INV-2024-003', 'Dino TV spots EU Q1',           '2024-01-20', 'system.seed'),
(1, 9, 5,  'SOCIAL_MEDIA', 180000.00, 'Meta Platforms',      'MKT-INV-2024-004', 'Dino social EU Q1',             '2024-01-22', 'system.seed'),
(2, 8, 7,  'TV',          1100000.00, 'Disney Ad Sales',     'MKT-INV-2024-101', 'Space summer TV NA Q3',         '2024-06-20', 'system.seed'),
(2, 8, 7,  'DIGITAL',      580000.00, 'YouTube',             'MKT-INV-2024-102', 'Space YouTube pre-roll NA',     '2024-07-01', 'system.seed'),
(2, 9, 7,  'AGENCY',       320000.00, 'EuroAds Agency',      'MKT-INV-2024-103', 'Space creative EU Q3',          '2024-06-25', 'system.seed'),
(2, 10,7,  'SOCIAL_MEDIA', 250000.00, 'WeChat Ads',          'MKT-INV-2024-104', 'Space WeChat APAC Q3',          '2024-06-28', 'system.seed'),
(3, 8, 8,  'TV',           720000.00, 'ESPN',                'MKT-INV-2024-201', 'Race Cars TV NA Q4',            '2024-09-12', 'system.seed'),
(3, 8, 8,  'DIGITAL',      310000.00, 'TikTok Ads',          'MKT-INV-2024-202', 'Race Cars TikTok NA Q4',        '2024-09-15', 'system.seed'),
(3, 8, 8,  'PRINT',        150000.00, 'USA Today',           'MKT-INV-2024-203', 'Race Cars print inserts Q4',    '2024-09-18', 'system.seed'),
(4, 8, 9,  'TV',           900000.00, 'NBCUniversal',        'MKT-INV-2025-001', 'Plush Pals TV NA Q1 2025',      '2025-01-12', 'system.seed'),
(4, 9, 9,  'TV',           750000.00, 'Channel 4 UK',        'MKT-INV-2025-002', 'Plush Pals TV EU Q1 2025',      '2025-01-15', 'system.seed'),
(4, 10,9,  'SOCIAL_MEDIA', 320000.00, 'Instagram',           'MKT-INV-2025-003', 'Plush social APAC Q1 2025',     '2025-01-20', 'system.seed'),
(4, 11,9,  'AGENCY',       480000.00, 'GlobalMedia Group',   'MKT-INV-2025-004', 'Plush global agency Q1 2025',   '2025-01-25', 'system.seed'),
(5, 8, 10, 'TV',           950000.00, 'Cartoon Network',     'MKT-INV-2025-101', 'Build It TV NA Q2 2025',        '2025-04-08', 'system.seed'),
(5, 9, 10, 'TV',           820000.00, 'France TV',           'MKT-INV-2025-102', 'Build It TV EU Q2 2025',        '2025-04-10', 'system.seed'),
(5, 10,10, 'DIGITAL',      450000.00, 'Google APAC',         'MKT-INV-2025-103', 'Build It digital APAC Q2 2025', '2025-04-15', 'system.seed'),
(5, 11,10, 'EVENTS',       680000.00, 'Event Horizon LLC',   'MKT-INV-2025-104', 'Build It in-store events Q2',   '2025-04-20', 'system.seed'),
(8, 11,9,  'DIGITAL',      600000.00, 'YouTube Global',      'MKT-INV-2025-201', 'Animal cards digital global',   '2025-02-01', 'system.seed'),
(8, 11,9,  'SOCIAL_MEDIA', 380000.00, 'TikTok Global',       'MKT-INV-2025-202', 'Animal cards TikTok global',    '2025-02-05', 'system.seed');
