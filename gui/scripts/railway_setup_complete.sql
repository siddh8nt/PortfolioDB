-- ===================================
-- PORTFOLIODB SCHEMA + DEMO DATA
-- ===================================

-- Create database (may already exist, so skip if present)
-- CREATE DATABASE IF NOT EXISTS portfoliodb;
USE portfoliodb;

-- =========================
-- INVESTOR TABLE
-- =========================
CREATE TABLE IF NOT EXISTS investors (
    investor_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE
);

-- =========================
-- ASSETS TABLE
-- =========================
CREATE TABLE IF NOT EXISTS assets (
    asset_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    type VARCHAR(20) CHECK (type IN ('Stock','Crypto','ETF','Bond','Mutual Fund'))
);

-- =========================
-- TRANSACTIONS TABLE
-- =========================
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    investor_id INT,
    asset_id INT,
    transaction_type VARCHAR(4) NOT NULL DEFAULT 'BUY' CHECK (transaction_type IN ('BUY','SELL')),
    quantity DECIMAL(10,2) CHECK (quantity > 0),
    price DECIMAL(10,2) CHECK (price > 0),
    transaction_date DATE,

    FOREIGN KEY (investor_id) REFERENCES investors(investor_id),
    FOREIGN KEY (asset_id) REFERENCES assets(asset_id)
);

-- =========================
-- PRICE TABLE
-- =========================
CREATE TABLE IF NOT EXISTS prices (
    asset_id INT PRIMARY KEY,
    current_price DECIMAL(10,2),

    FOREIGN KEY (asset_id) REFERENCES assets(asset_id)
);

-- =========================
-- TRIGGER (Constraint topic)
-- =========================
DELIMITER //

CREATE TRIGGER IF NOT EXISTS check_quantity
BEFORE INSERT ON transactions
FOR EACH ROW
BEGIN
    IF NEW.quantity <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Quantity must be positive';
    END IF;
END //

DELIMITER ;

-- =========================
-- FUNCTION (PL/SQL topic)
-- =========================
DELIMITER //

DROP FUNCTION IF EXISTS total_investment//

CREATE FUNCTION total_investment(inv_id INT)
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    DECLARE total DECIMAL(10,2);

    SELECT SUM(quantity * price)
    INTO total
    FROM transactions
    WHERE investor_id = inv_id;

    RETURN COALESCE(total, 0);
END //

DELIMITER ;

-- =========================
-- PROCEDURE (PL/SQL topic)
-- =========================
DELIMITER //

DROP PROCEDURE IF EXISTS get_portfolio//

CREATE PROCEDURE get_portfolio()
BEGIN
    SELECT 
        i.name,
        a.name AS asset,
        SUM(t.quantity) AS total_qty,
        SUM(t.quantity * t.price) AS investment,
        SUM(t.quantity * p.current_price) AS current_value,
        SUM(t.quantity * p.current_price) - SUM(t.quantity * t.price) AS profit_loss
    FROM transactions t
    JOIN investors i ON t.investor_id = i.investor_id
    JOIN assets a ON t.asset_id = a.asset_id
    JOIN prices p ON a.asset_id = p.asset_id
    GROUP BY i.name, a.name;
END //

DELIMITER ;

-- =========================
-- DEMO DATA
-- =========================

-- Insert sample investors
INSERT INTO investors (name, email) VALUES 
('Alice Johnson', 'alice@example.com'),
('Bob Smith', 'bob@example.com'),
('Carol Davis', 'carol@example.com');

-- Insert sample assets
INSERT INTO assets (name, type) VALUES 
('Apple Inc.', 'Stock'),
('Microsoft Corp.', 'Stock'),
('Bitcoin', 'Crypto'),
('Ethereum', 'Crypto');

-- Insert sample prices
INSERT INTO prices (asset_id, current_price) VALUES 
(1, 185.50),
(2, 378.90),
(3, 45000.00),
(4, 2500.00);

-- Insert sample transactions
INSERT INTO transactions (investor_id, asset_id, transaction_type, quantity, price, transaction_date) VALUES 
(1, 1, 'BUY', 10.00, 150.00, '2024-01-10'),
(1, 3, 'BUY', 0.5, 40000.00, '2024-01-15'),
(2, 2, 'BUY', 5.00, 350.00, '2024-01-20'),
(2, 4, 'BUY', 2.00, 2400.00, '2024-02-01'),
(3, 1, 'BUY', 15.00, 160.00, '2024-02-05'),
(3, 3, 'BUY', 1.00, 43000.00, '2024-02-10');

-- Verify data was inserted
SELECT 'Schema and demo data loaded successfully' AS status;
