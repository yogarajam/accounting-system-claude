-- Initial Data Setup for Accounting System (H2 Compatible)

-- Roles
MERGE INTO roles (id, name, description) KEY(name) VALUES
    (1, 'ADMIN', 'System administrator with full access'),
    (2, 'ACCOUNTANT', 'Can manage accounts, journal entries, and reports'),
    (3, 'VIEWER', 'Read-only access to view reports');

-- Default Admin User (password: admin123)
-- BCrypt hash for 'admin123'
MERGE INTO users (id, username, password, email, full_name, role_id, enabled, created_at) KEY(username) VALUES
    (1, 'admin', '$2a$10$PYn5KLlYP1n4P4PGcIdqnOg1fd4oLnJiixzIO9A56LUpts493lswa', 'admin@accounting.local', 'System Administrator', 1, true, CURRENT_TIMESTAMP);

-- Currencies
MERGE INTO currencies (id, code, name, symbol, exchange_rate, is_base) KEY(code) VALUES
    (1, 'USD', 'US Dollar', '$', 1.000000, true),
    (2, 'EUR', 'Euro', '€', 1.100000, false),
    (3, 'GBP', 'British Pound', '£', 1.270000, false),
    (4, 'NGN', 'Nigerian Naira', '₦', 0.000650, false);

-- Standard Chart of Accounts

-- Assets (1000-1999)
MERGE INTO accounts (id, code, name, account_type, description, is_active) KEY(code) VALUES
    (1, '1000', 'Cash', 'ASSET', 'Cash on hand and in bank', true),
    (2, '1010', 'Petty Cash', 'ASSET', 'Petty cash fund', true),
    (3, '1100', 'Bank Account - Checking', 'ASSET', 'Main checking account', true),
    (4, '1110', 'Bank Account - Savings', 'ASSET', 'Business savings account', true),
    (5, '1200', 'Accounts Receivable', 'ASSET', 'Amounts owed by customers', true),
    (6, '1300', 'Inventory', 'ASSET', 'Inventory of goods for sale', true),
    (7, '1400', 'Prepaid Expenses', 'ASSET', 'Expenses paid in advance', true),
    (8, '1500', 'Fixed Assets', 'ASSET', 'Property, plant, and equipment', true),
    (9, '1510', 'Furniture & Equipment', 'ASSET', 'Office furniture and equipment', true),
    (10, '1520', 'Vehicles', 'ASSET', 'Company vehicles', true),
    (11, '1590', 'Accumulated Depreciation', 'ASSET', 'Accumulated depreciation on fixed assets', true);

-- Liabilities (2000-2999)
MERGE INTO accounts (id, code, name, account_type, description, is_active) KEY(code) VALUES
    (12, '2000', 'Accounts Payable', 'LIABILITY', 'Amounts owed to suppliers', true),
    (13, '2100', 'Accrued Expenses', 'LIABILITY', 'Expenses incurred but not yet paid', true),
    (14, '2200', 'Wages Payable', 'LIABILITY', 'Salaries and wages owed to employees', true),
    (15, '2300', 'Taxes Payable', 'LIABILITY', 'Taxes owed to government', true),
    (16, '2400', 'Short-term Loans', 'LIABILITY', 'Loans due within one year', true),
    (17, '2500', 'Long-term Loans', 'LIABILITY', 'Loans due after one year', true),
    (18, '2600', 'Unearned Revenue', 'LIABILITY', 'Revenue received but not yet earned', true);

-- Equity (3000-3999)
MERGE INTO accounts (id, code, name, account_type, description, is_active) KEY(code) VALUES
    (19, '3000', 'Owner''s Capital', 'EQUITY', 'Owner''s investment in the business', true),
    (20, '3100', 'Owner''s Drawings', 'EQUITY', 'Withdrawals by owner', true),
    (21, '3200', 'Retained Earnings', 'EQUITY', 'Accumulated profits retained in business', true);

-- Revenue (4000-4999)
MERGE INTO accounts (id, code, name, account_type, description, is_active) KEY(code) VALUES
    (22, '4000', 'Sales Revenue', 'REVENUE', 'Income from sales of products or services', true),
    (23, '4100', 'Service Revenue', 'REVENUE', 'Income from providing services', true),
    (24, '4200', 'Interest Income', 'REVENUE', 'Interest earned on bank accounts', true),
    (25, '4300', 'Other Income', 'REVENUE', 'Miscellaneous income', true),
    (26, '4900', 'Sales Returns & Allowances', 'REVENUE', 'Contra revenue for returns', true);

-- Expenses (5000-5999)
MERGE INTO accounts (id, code, name, account_type, description, is_active) KEY(code) VALUES
    (27, '5000', 'Cost of Goods Sold', 'EXPENSE', 'Direct cost of products sold', true),
    (28, '5100', 'Salaries & Wages', 'EXPENSE', 'Employee compensation', true),
    (29, '5200', 'Rent Expense', 'EXPENSE', 'Office or facility rent', true),
    (30, '5300', 'Utilities Expense', 'EXPENSE', 'Electricity, water, internet', true),
    (31, '5400', 'Office Supplies', 'EXPENSE', 'Office supplies and materials', true),
    (32, '5500', 'Insurance Expense', 'EXPENSE', 'Business insurance premiums', true),
    (33, '5600', 'Depreciation Expense', 'EXPENSE', 'Depreciation of fixed assets', true),
    (34, '5700', 'Interest Expense', 'EXPENSE', 'Interest on loans', true),
    (35, '5800', 'Bank Charges', 'EXPENSE', 'Bank fees and charges', true),
    (36, '5900', 'Advertising & Marketing', 'EXPENSE', 'Marketing and promotional expenses', true),
    (37, '5910', 'Travel & Entertainment', 'EXPENSE', 'Business travel expenses', true),
    (38, '5920', 'Professional Fees', 'EXPENSE', 'Legal, accounting, consulting fees', true),
    (39, '5930', 'Repairs & Maintenance', 'EXPENSE', 'Equipment repairs and maintenance', true),
    (40, '5990', 'Miscellaneous Expense', 'EXPENSE', 'Other business expenses', true);

-- Fiscal Years
MERGE INTO fiscal_years (id, name, start_date, end_date, is_closed) KEY(name) VALUES
    (1, 'FY 2024', '2024-01-01', '2024-12-31', false),
    (2, 'FY 2025', '2025-01-01', '2025-12-31', false),
    (3, 'FY 2026', '2026-01-01', '2026-12-31', false);