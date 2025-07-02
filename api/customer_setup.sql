-- Customer Master Table Creation and Sample Data
-- メール宛先マスター管理システム (NAIS) - 得意先マスタ

-- Create customer_mst table
CREATE TABLE IF NOT EXISTS customer_mst (
    customer_cd VARCHAR(4) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT customer_mst_pkey PRIMARY KEY (customer_cd)
);

-- Create unique index (already covered by primary key, but explicit for documentation)
CREATE UNIQUE INDEX IF NOT EXISTS customer_mst_pkey ON customer_mst (customer_cd);

-- Insert sample data
INSERT INTO customer_mst (customer_cd, customer_name) VALUES
('C001', '株式会社ヒップ'),
('C002', 'GleamOrb Corporation'),
('C003', 'NAIS Technologies'),
('C004', '東京システム開発'),
('C005', 'オーサカデータソリューション'),
('C006', 'Kyoto Software Labs'),
('C007', '名古屋エンジニアリング'),
('C008', 'Fukuoka Tech Solutions'),
('C009', 'Sapporo Information Systems'),
('C010', 'Hiroshima Digital Works')
ON CONFLICT (customer_cd) DO UPDATE SET
    customer_name = EXCLUDED.customer_name,
    updated_at = CURRENT_TIMESTAMP;

-- Verify data insertion
SELECT * FROM customer_mst ORDER BY customer_cd;