-- Email Master Tables Creation and Sample Data
-- メール宛先マスター管理システム (NAIS) - メール関連マスタテーブル

-- ==========================================
-- Table 1: メール宛先子マスタ (mail_to_child_mst)
-- ==========================================

-- Create mail_to_child_mst table
CREATE TABLE IF NOT EXISTS mail_to_child_mst (
    mailing_list_id VARCHAR(320) NOT NULL,
    recipient_seq SMALLINT NOT NULL,
    recipient_address VARCHAR(320) NOT NULL,
    recipent_note VARCHAR(40),
    status_div CHAR(1) NOT NULL DEFAULT '0',
    input_user_cd CHAR(5),
    created_by VARCHAR(40),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(40),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT mail_to_child_mst_pkey PRIMARY KEY (mailing_list_id, recipient_seq)
);

-- Insert sample data for mail_to_child_mst
INSERT INTO mail_to_child_mst (
    mailing_list_id, recipient_seq, recipient_address, recipent_note, 
    status_div, input_user_cd, created_by
) VALUES
('ML001', 1, 'manager@hipe.asia', '管理者', '0', 'U001', 'system'),
('ML001', 2, 'admin@hipe.asia', '管理者副', '0', 'U001', 'system'),
('ML001', 3, 'test@hipe.asia', 'テストアカウント', '1', 'U002', 'system'),
('ML002', 1, 'dev@gleamorb.co.jp', '開発チーム', '0', 'U003', 'system'),
('ML002', 2, 'support@gleamorb.co.jp', 'サポート', '0', 'U003', 'system'),
('ML003', 1, 'sales@nais-mail.com', '営業部', '0', 'U004', 'system'),
('ML003', 2, 'marketing@nais-mail.com', 'マーケティング', '0', 'U004', 'system'),
('ML003', 3, 'old@nais-mail.com', '旧アカウント', '2', 'U005', 'system')
ON CONFLICT (mailing_list_id, recipient_seq) DO UPDATE SET
    recipient_address = EXCLUDED.recipient_address,
    recipent_note = EXCLUDED.recipent_note,
    status_div = EXCLUDED.status_div,
    updated_by = EXCLUDED.updated_by,
    updated_at = CURRENT_TIMESTAMP;

-- ==========================================
-- Table 2: メールAPI連携設定マスタ (mail_api_config_mst)
-- ==========================================

-- Create mail_api_config_mst table
CREATE TABLE IF NOT EXISTS mail_api_config_mst (
    job_id VARCHAR(20) NOT NULL,
    job_name VARCHAR(320) NOT NULL,
    send_mode CHAR(4) NOT NULL,
    search_directory VARCHAR(512) NOT NULL,
    send_directory VARCHAR(512) NOT NULL,
    subject VARCHAR(512) NOT NULL,
    body_file_path VARCHAR(512) NOT NULL,
    update_sys_div CHAR(1) NOT NULL,
    created_by VARCHAR(40),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(40),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT mail_api_config_mst_pkey PRIMARY KEY (job_id)
);

-- Insert sample data for mail_api_config_mst
INSERT INTO mail_api_config_mst (
    job_id, job_name, send_mode, search_directory, send_directory,
    subject, body_file_path, update_sys_div, created_by
) VALUES
('JOB001', '日次売上レポート配信', 'MPDF', '/data/reports/daily', '/data/send/daily', 
 '【NAIS】日次売上レポート', '/templates/daily_sales_report.html', '0', 'system'),
('JOB002', '顧客問い合わせ自動返信', 'AUTO', '/data/inquiries/inbox', '/data/inquiries/sent',
 'お問い合わせありがとうございます', '/templates/auto_reply.html', '1', 'system'),
('JOB003', 'システムメンテナンス通知', 'CMP', '/data/maintenance/alerts', '/data/maintenance/sent',
 '【重要】システムメンテナンスのお知らせ', '/templates/maintenance_notice.html', '2', 'system'),
('JOB004', 'PDF請求書送信', 'PDF', '/data/invoices/ready', '/data/invoices/sent',
 '請求書を送付いたします', '/templates/invoice_cover.html', '0', 'system'),
('JOB005', '製品カタログ配信', 'CTLG', '/data/catalogs/new', '/data/catalogs/sent',
 '【新商品】製品カタログをお送りします', '/templates/catalog_intro.html', '1', 'system')
ON CONFLICT (job_id) DO UPDATE SET
    job_name = EXCLUDED.job_name,
    send_mode = EXCLUDED.send_mode,
    search_directory = EXCLUDED.search_directory,
    send_directory = EXCLUDED.send_directory,
    subject = EXCLUDED.subject,
    body_file_path = EXCLUDED.body_file_path,
    update_sys_div = EXCLUDED.update_sys_div,
    updated_by = EXCLUDED.updated_by,
    updated_at = CURRENT_TIMESTAMP;

-- Verify data insertion
SELECT 'mail_to_child_mst' as table_name, COUNT(*) as record_count FROM mail_to_child_mst
UNION ALL
SELECT 'mail_api_config_mst' as table_name, COUNT(*) as record_count FROM mail_api_config_mst;

-- Display sample data
SELECT '=== mail_to_child_mst サンプルデータ ===' as info;
SELECT * FROM mail_to_child_mst ORDER BY mailing_list_id, recipient_seq;

SELECT '=== mail_api_config_mst サンプルデータ ===' as info;
SELECT * FROM mail_api_config_mst ORDER BY job_id;