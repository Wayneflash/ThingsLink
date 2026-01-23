-- Add raw_payload column to tb_device_data table
DELIMITER $$

DROP PROCEDURE IF EXISTS add_raw_payload_column$$

CREATE PROCEDURE add_raw_payload_column()
BEGIN
    DECLARE column_exists INT DEFAULT 0;
    
    SELECT COUNT(*) INTO column_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'tb_device_data'
    AND COLUMN_NAME = 'raw_payload';
    
    IF column_exists = 0 THEN
        ALTER TABLE tb_device_data ADD COLUMN raw_payload TEXT DEFAULT NULL COMMENT 'Raw MQTT payload JSON' AFTER receive_time;
    END IF;
END$$

DELIMITER ;

CALL add_raw_payload_column();

DROP PROCEDURE IF EXISTS add_raw_payload_column;

-- Record migration history
INSERT INTO tb_migration_history (migration_file, status, executed_at) 
VALUES ('006_add_raw_payload_to_device_data.sql', 'success', NOW())
ON DUPLICATE KEY UPDATE status = 'success', executed_at = NOW();
