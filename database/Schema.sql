-- Ad Allocation Engine Database Schema

CREATE DATABASE IF NOT EXISTS ad_allocation_engine;
USE ad_allocation_engine;

-- Users table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Advertisers table
CREATE TABLE advertisers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    total_budget DECIMAL(10,2) NOT NULL,
    remaining_budget DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System Officers (Admin) table
CREATE TABLE system_officers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ads table
CREATE TABLE ads (
    id INT PRIMARY KEY AUTO_INCREMENT,
    advertiser_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    bid_amount DECIMAL(10,2) NOT NULL,
    keywords TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (advertiser_id) REFERENCES advertisers(id)
);

-- Advertiser conflicts (which advertisers cannot appear together)
CREATE TABLE advertiser_conflicts (
    advertiser1_id INT NOT NULL,
    advertiser2_id INT NOT NULL,
    PRIMARY KEY (advertiser1_id, advertiser2_id),
    FOREIGN KEY (advertiser1_id) REFERENCES advertisers(id),
    FOREIGN KEY (advertiser2_id) REFERENCES advertisers(id)
);

-- User sessions
CREATE TABLE user_sessions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Search history
CREATE TABLE search_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    session_id INT,
    search_query VARCHAR(500) NOT NULL,
    page_content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (session_id) REFERENCES user_sessions(id)
);

-- Ad allocation events
CREATE TABLE allocation_events (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    session_id INT,
    ad_id INT NOT NULL,
    slot_type ENUM('TOP', 'SIDEBAR', 'FOOTER') NOT NULL,
    score DECIMAL(10,4) NOT NULL,
    final_price DECIMAL(10,2) NOT NULL,
    was_clicked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (session_id) REFERENCES user_sessions(id),
    FOREIGN KEY (ad_id) REFERENCES ads(id)
);

-- Ad performance tracking
CREATE TABLE ad_performance (
    id INT PRIMARY KEY AUTO_INCREMENT,
    ad_id INT NOT NULL,
    total_shown INT DEFAULT 0,
    total_clicked INT DEFAULT 0,
    last_shown TIMESTAMP NULL,
    last_clicked TIMESTAMP NULL,
    FOREIGN KEY (ad_id) REFERENCES ads(id)
);

-- Insert sample data
INSERT INTO users (username, email, password) VALUES 
('john_doe', 'john@example.com', SHA2('password123', 256)),
('jane_smith', 'jane@example.com', SHA2('password123', 256)),
('mike_wilson', 'mike@example.com', SHA2('password123', 256));

INSERT INTO advertisers (name, email, password, total_budget, remaining_budget) VALUES 
('Apple Inc', 'apple@example.com', SHA2('password123', 256), 10000.00, 10000.00),
('Samsung Electronics', 'samsung@example.com', SHA2('password123', 256), 8000.00, 8000.00),
('Canon Inc', 'canon@example.com', SHA2('password123', 256), 5000.00, 5000.00),
('Nike Inc', 'nike@example.com', SHA2('password123', 256), 6000.00, 6000.00),
('Adidas AG', 'adidas@example.com', SHA2('password123', 256), 5500.00, 5500.00);

INSERT INTO system_officers (username, email, password) VALUES 
('admin', 'admin@example.com', SHA2('admin123', 256));

INSERT INTO ads (advertiser_id, title, content, bid_amount, keywords) VALUES 
(1, 'iPhone 15 Pro with AI Camera', 'Experience the future with our revolutionary AI-powered camera system', 5.50, 'iPhone,camera,AI,smartphone'),
(2, 'Galaxy S24 Ultra', 'Unleash your creativity with Galaxy''s advanced photography', 4.80, 'Samsung,phone,camera,Android'),
(3, 'EOS R5 Mirrorless Camera', 'Professional photography meets cutting-edge technology', 3.20, 'camera,photography,lens,professional'),
(4, 'Air Max 2024', 'Run faster with our innovative cushioning technology', 2.80, 'shoes,running,athletic,sports'),
(5, 'Ultraboost 22', 'Energy return for unlimited running comfort', 2.60, 'shoes,running,comfort,athletic');

INSERT INTO advertiser_conflicts (advertiser1_id, advertiser2_id) VALUES 
(1, 2), -- Apple vs Samsung
(4, 5); -- Nike vs Adidas

INSERT INTO ad_performance (ad_id, total_shown, total_clicked) VALUES 
(1, 0, 0), (2, 0, 0), (3, 0, 0), (4, 0, 0), (5, 0, 0);
