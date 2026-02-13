-- ========================================================================
-- NexGen Technologies Pvt. Ltd. — Realistic Demo Data
-- Run against: erp_database (MySQL)
-- ========================================================================
-- This script uses INSERT IGNORE so it can be re-run safely.
-- Foreign key IDs assume a clean database (auto-increment starts at 1).
-- If you already have data, adjust IDs accordingly.
-- ========================================================================

SET NAMES utf8mb4;
SET @now = NOW();

-- ========================================================================
-- 1. DEPARTMENTS
-- ========================================================================
INSERT IGNORE INTO departments (id, name, description, created_at, updated_at) VALUES
(1, 'Engineering',     'Software development and R&D',           @now, @now),
(2, 'Sales',           'Revenue generation and client relations', @now, @now),
(3, 'Finance',         'Accounting, billing and treasury',       @now, @now),
(4, 'Human Resources', 'Talent management and payroll',          @now, @now),
(5, 'Operations',      'Supply chain and logistics',             @now, @now),
(6, 'Marketing',       'Brand, digital marketing and PR',        @now, @now);

-- ========================================================================
-- 2. EMPLOYEES  (user_id can be NULL for non-login employees)
-- ========================================================================
INSERT IGNORE INTO employees (id, employee_code, first_name, last_name, email, phone, department_id, designation, date_of_joining, salary, status, created_at, updated_at) VALUES
(1, 'EMP-001', 'Karthikeyan', 'Murugan',   'karthikeyan@nexgen.in',  '9876543210', 1, 'CTO',                    '2023-01-15', 250000.00, 'ACTIVE', @now, @now),
(2, 'EMP-002', 'Priya',       'Sharma',     'priya.sharma@nexgen.in', '9876543211', 2, 'VP Sales',               '2023-03-01', 180000.00, 'ACTIVE', @now, @now),
(3, 'EMP-003', 'Ravi',        'Kumar',      'ravi.kumar@nexgen.in',   '9876543212', 3, 'Finance Manager',        '2023-04-10', 150000.00, 'ACTIVE', @now, @now),
(4, 'EMP-004', 'Anjali',      'Reddy',      'anjali.reddy@nexgen.in', '9876543213', 4, 'HR Manager',             '2023-05-05', 130000.00, 'ACTIVE', @now, @now),
(5, 'EMP-005', 'Vikram',      'Joshi',      'vikram.joshi@nexgen.in', '9876543214', 5, 'Operations Head',        '2023-06-20', 160000.00, 'ACTIVE', @now, @now),
(6, 'EMP-006', 'Sneha',       'Patel',      'sneha.patel@nexgen.in',  '9876543215', 1, 'Senior Developer',       '2023-07-01', 120000.00, 'ACTIVE', @now, @now),
(7, 'EMP-007', 'Arjun',       'Nair',       'arjun.nair@nexgen.in',   '9876543216', 2, 'Sales Executive',        '2023-08-15', 80000.00,  'ACTIVE', @now, @now),
(8, 'EMP-008', 'Divya',       'Iyer',       'divya.iyer@nexgen.in',   '9876543217', 6, 'Marketing Lead',         '2023-09-01', 110000.00, 'ACTIVE', @now, @now),
(9, 'EMP-009', 'Suresh',      'Menon',      'suresh.menon@nexgen.in', '9876543218', 5, 'Warehouse Supervisor',   '2024-01-10', 75000.00,  'ACTIVE', @now, @now),
(10,'EMP-010', 'Meera',       'Gupta',      'meera.gupta@nexgen.in',  '9876543219', 3, 'Junior Accountant',      '2024-03-01', 55000.00,  'ACTIVE', @now, @now);

-- ========================================================================
-- 3. CATEGORIES (product categories)
-- ========================================================================
INSERT IGNORE INTO categories (id, name, description) VALUES
(1, 'Electronics',       'Electronic devices and components'),
(2, 'Laptops',           'Business and consumer laptops'),
(3, 'Networking',        'Routers, switches, access points'),
(4, 'Office Supplies',   'Stationery and office consumables'),
(5, 'Peripherals',       'Keyboards, mice, monitors, printers'),
(6, 'Cloud Services',    'SaaS subscriptions and cloud infra'),
(7, 'Mobile Devices',    'Smartphones and tablets');

-- Set parent categories
UPDATE categories SET parent_category_id = 1 WHERE id IN (2, 3, 5, 7);

-- ========================================================================
-- 4. PRODUCTS
-- ========================================================================
INSERT IGNORE INTO products (id, product_code, name, description, category_id, unit, unit_price, reorder_level, is_active, created_at, updated_at) VALUES
(1,  'SKU-LAP-001', 'Dell Latitude 5540',          '14" i7, 16GB, 512GB SSD business laptop',     2, 'PIECE', 89500.00, 5,  1, @now, @now),
(2,  'SKU-LAP-002', 'HP EliteBook 840 G10',        '14" i5, 16GB, 256GB SSD ultrabook',           2, 'PIECE', 78000.00, 5,  1, @now, @now),
(3,  'SKU-LAP-003', 'Lenovo ThinkPad X1 Carbon',   '14" i7, 32GB, 1TB SSD premium ultrabook',     2, 'PIECE', 145000.00, 3, 1, @now, @now),
(4,  'SKU-NET-001', 'Cisco Catalyst 1000-24T',     '24-port managed gigabit switch',              3, 'PIECE', 42000.00, 3,  1, @now, @now),
(5,  'SKU-NET-002', 'Ubiquiti UniFi AP AC Pro',    'Enterprise indoor Wi-Fi access point',        3, 'PIECE', 12500.00, 10, 1, @now, @now),
(6,  'SKU-PER-001', 'Logitech MX Master 3S',       'Wireless ergonomic mouse',                    5, 'PIECE', 8500.00,  15, 1, @now, @now),
(7,  'SKU-PER-002', 'Dell UltraSharp U2723QE',     '27" 4K USB-C hub monitor',                   5, 'PIECE', 52000.00, 5,  1, @now, @now),
(8,  'SKU-PER-003', 'Keychron K2 Pro',             '75% wireless mechanical keyboard',            5, 'PIECE', 7500.00,  20, 1, @now, @now),
(9,  'SKU-OFF-001', 'A4 Copier Paper (5 reams)',   '80gsm A4 white paper bundle',                 4, 'BOX',   1200.00,  30, 1, @now, @now),
(10, 'SKU-OFF-002', 'HP 26A Toner Cartridge',      'Black LaserJet toner (CF226A)',               4, 'PIECE', 4800.00,  10, 1, @now, @now),
(11, 'SKU-MOB-001', 'Samsung Galaxy S24 Ultra',    '256GB, Titanium, enterprise edition',          7, 'PIECE', 124999.00,5, 1, @now, @now),
(12, 'SKU-MOB-002', 'Apple iPad Air M2',           '11" 256GB Wi-Fi, company tablet',              7, 'PIECE', 69900.00, 5, 1, @now, @now);

-- ========================================================================
-- 5. WAREHOUSES
-- ========================================================================
INSERT IGNORE INTO warehouses (id, name, location, manager_id, is_active, created_at, updated_at) VALUES
(1, 'Bengaluru Central',  'Whitefield, Bengaluru, Karnataka',     5, 1, @now, @now),
(2, 'Mumbai Warehouse',   'Andheri East, Mumbai, Maharashtra',    9, 1, @now, @now),
(3, 'Chennai Hub',        'Guindy, Chennai, Tamil Nadu',          NULL, 1, @now, @now);

-- ========================================================================
-- 6. STOCK (initial stock per warehouse)
-- ========================================================================
INSERT IGNORE INTO stock (id, product_id, warehouse_id, quantity, last_updated) VALUES
-- Bengaluru Central
(1,  1,  1, 25, @now),  -- Dell Latitude
(2,  2,  1, 18, @now),  -- HP EliteBook
(3,  3,  1, 8,  @now),  -- ThinkPad X1
(4,  4,  1, 12, @now),  -- Cisco Switch
(5,  5,  1, 30, @now),  -- Ubiquiti AP
(6,  6,  1, 40, @now),  -- Logitech Mouse
(7,  7,  1, 15, @now),  -- Dell Monitor
(8,  8,  1, 35, @now),  -- Keychron KB
(9,  9,  1, 50, @now),  -- A4 Paper
(10, 10, 1, 20, @now),  -- Toner
(11, 11, 1, 10, @now),  -- Samsung S24
(12, 12, 1, 12, @now),  -- iPad Air
-- Mumbai Warehouse
(13, 1,  2, 15, @now),
(14, 2,  2, 10, @now),
(15, 6,  2, 25, @now),
(16, 7,  2, 8,  @now),
(17, 9,  2, 40, @now),
(18, 10, 2, 15, @now),
-- Chennai Hub
(19, 1,  3, 10, @now),
(20, 5,  3, 20, @now),
(21, 8,  3, 15, @now),
(22, 11, 3, 5,  @now);

-- ========================================================================
-- 7. SUPPLIERS
-- ========================================================================
INSERT IGNORE INTO suppliers (id, supplier_code, name, contact_person, email, phone, address, city, country, is_active, created_at, updated_at) VALUES
(1, 'SUP-001', 'Tech Distributors India',    'Rajesh Verma',    'rajesh@techdist.in',     '9800012345', '45 MG Road, Electronic City',          'Bengaluru', 'India', 1, @now, @now),
(2, 'SUP-002', 'Digital Solutions Pvt Ltd',   'Amit Singh',      'amit@digsol.com',        '9800012346', '12 Sector 62, Noida',                  'Noida',     'India', 1, @now, @now),
(3, 'SUP-003', 'NetGear Supplies Co',        'Sanjay Tiwari',   'sanjay@netgears.co.in',  '9800012347', '78 Nehru Place, IT Park',              'New Delhi', 'India', 1, @now, @now),
(4, 'SUP-004', 'Pearl Office Essentials',    'Lakshmi Menon',   'lakshmi@pearloffice.in', '9800012348', '22 Anna Salai',                        'Chennai',   'India', 1, @now, @now),
(5, 'SUP-005', 'Samsung Enterprise India',   'Deepak Rao',      'deepak@samsung-ent.in',  '9800012349', 'Samsung Hub, Cyber City Phase 2',      'Gurugram',  'India', 1, @now, @now);

-- ========================================================================
-- 8. CUSTOMERS
-- ========================================================================
INSERT IGNORE INTO customers (id, customer_code, name, contact_person, email, phone, address, city, country, credit_limit, outstanding_balance, is_active, created_at, updated_at) VALUES
(1, 'CUST-001', 'Infosys Ltd',            'Arun Bansal',     'arun.bansal@infosys.com',    '9900011111', 'Electronic City Phase 1',        'Bengaluru',  'India', 5000000.00, 0.00, 1, @now, @now),
(2, 'CUST-002', 'Wipro Technologies',     'Neha Kapoor',     'neha.kapoor@wipro.com',      '9900022222', 'Sarjapur Road',                  'Bengaluru',  'India', 3000000.00, 0.00, 1, @now, @now),
(3, 'CUST-003', 'TCS Digital',            'Vivek Khanna',    'vivek.khanna@tcs.com',       '9900033333', 'Whitefield IT Park',             'Bengaluru',  'India', 8000000.00, 0.00, 1, @now, @now),
(4, 'CUST-004', 'Zoho Corporation',       'Ramya Krishnan',  'ramya.krishnan@zoho.com',    '9900044444', 'Thoraipakkam',                   'Chennai',    'India', 2000000.00, 0.00, 1, @now, @now),
(5, 'CUST-005', 'Freshworks Inc',         'Anil Reddy',      'anil.reddy@freshworks.com',  '9900055555', 'Global Infocity, OMR',           'Chennai',    'India', 4000000.00, 0.00, 1, @now, @now),
(6, 'CUST-006', 'Razorpay Software',      'Deepa Nair',      'deepa.nair@razorpay.com',    '9900066666', 'SJR I Park, Whitefield',         'Bengaluru',  'India', 1500000.00, 0.00, 1, @now, @now),
(7, 'CUST-007', 'Zerodha Broking Ltd',    'Prashanth Hegde', 'prashanth@zerodha.com',      '9900077777', 'JP Nagar 4th Phase',             'Bengaluru',  'India', 2500000.00, 0.00, 1, @now, @now),
(8, 'CUST-008', 'Reliance Jio Infocomm',  'Meghna Shah',     'meghna.shah@jio.com',        '9900088888', 'Ghansoli, Navi Mumbai',          'Mumbai',     'India', 10000000.00,0.00, 1, @now, @now);

-- ========================================================================
-- 9. ACCOUNTS (Chart of Accounts)
-- ========================================================================
INSERT IGNORE INTO accounts (id, account_code, account_name, account_type, parent_account_id, balance, is_active, created_at, updated_at) VALUES
-- Assets
(1,  'ACC-1000', 'Cash in Hand',                'ASSET',    NULL, 500000.00,   1, @now, @now),
(2,  'ACC-1001', 'HDFC Bank - Current',         'ASSET',    NULL, 12500000.00, 1, @now, @now),
(3,  'ACC-1002', 'ICICI Bank - Current',        'ASSET',    NULL, 3200000.00,  1, @now, @now),
(4,  'ACC-1010', 'Accounts Receivable',         'ASSET',    NULL, 0.00,        1, @now, @now),
(5,  'ACC-1020', 'Inventory Asset',             'ASSET',    NULL, 0.00,        1, @now, @now),
-- Liabilities
(6,  'ACC-2000', 'Accounts Payable',            'LIABILITY', NULL, 0.00,       1, @now, @now),
(7,  'ACC-2010', 'GST Payable',                 'LIABILITY', NULL, 0.00,       1, @now, @now),
(8,  'ACC-2020', 'Employee Salaries Payable',   'LIABILITY', NULL, 0.00,       1, @now, @now),
-- Revenue
(9,  'ACC-3000', 'Product Sales Revenue',       'REVENUE',  NULL, 0.00,        1, @now, @now),
(10, 'ACC-3010', 'Service Income',              'REVENUE',  NULL, 0.00,        1, @now, @now),
-- Expenses
(11, 'ACC-4000', 'Cost of Goods Sold',          'EXPENSE',  NULL, 0.00,        1, @now, @now),
(12, 'ACC-4010', 'Office Rent',                 'EXPENSE',  NULL, 0.00,        1, @now, @now),
(13, 'ACC-4020', 'Utilities',                   'EXPENSE',  NULL, 0.00,        1, @now, @now),
(14, 'ACC-4030', 'Employee Salaries',           'EXPENSE',  NULL, 0.00,        1, @now, @now),
(15, 'ACC-4040', 'Travel & Conveyance',         'EXPENSE',  NULL, 0.00,        1, @now, @now),
-- Equity
(16, 'ACC-5000', 'Owner Equity',                'EQUITY',   NULL, 20000000.00, 1, @now, @now),
(17, 'ACC-5010', 'Retained Earnings',           'EQUITY',   NULL, 0.00,        1, @now, @now);

-- ========================================================================
-- 10. EXPENSES
-- ========================================================================
INSERT IGNORE INTO expenses (id, expense_code, category, amount, expense_date, vendor_name, description, employee_id, status, created_at, updated_at) VALUES
(1, 'EXP-2026-001', 'TRAVEL',          15400.00,  '2026-01-15', 'MakeMyTrip',          'Client visit to TCS Whitefield — flight + cab',       2, 'APPROVED', @now, @now),
(2, 'EXP-2026-002', 'OFFICE_SUPPLIES', 8750.00,   '2026-01-20', 'Amazon Business',     'Stationery & desk organizers for new hires',           4, 'PAID',     @now, @now),
(3, 'EXP-2026-003', 'UTILITIES',       24000.00,  '2026-01-25', 'BESCOM',              'Electricity bill — Bengaluru HQ Jan 2026',             3, 'PAID',     @now, @now),
(4, 'EXP-2026-004', 'ENTERTAINMENT',   6200.00,   '2026-02-01', 'Taj West End',        'Client dinner — Zoho partnership discussion',          2, 'APPROVED', @now, @now),
(5, 'EXP-2026-005', 'TRAVEL',          42000.00,  '2026-02-05', 'IRCTC',               'Team offsite Goa — train tickets for 6 employees',     8, 'PENDING',  @now, @now),
(6, 'EXP-2026-006', 'OFFICE_SUPPLIES', 3200.00,   '2026-02-08', 'Flipkart',            'Printer paper & toner cartridge restock',              10,'APPROVED', @now, @now);

-- ========================================================================
-- Done! 
-- Summary of inserted data:
--   6 Departments
--  10 Employees
--   7 Categories (with parent-child hierarchy)
--  12 Products (laptops, networking, peripherals, office, mobile)
--   3 Warehouses (Bengaluru, Mumbai, Chennai)
--  22 Stock entries across warehouses
--   5 Suppliers
--   8 Customers (major Indian tech companies)
--  17 Accounts (Chart of Accounts)
--   6 Expenses
-- ========================================================================
