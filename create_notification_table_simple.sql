CREATE TABLE IF NOT EXISTS tb_notification (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  alarm_id bigint NOT NULL,
  device_id bigint NOT NULL,
  device_code varchar(50) NOT NULL,
  device_name varchar(100) NOT NULL,
  alarm_level varchar(20) NOT NULL,
  alarm_message text NOT NULL,
  is_read tinyint(1) NOT NULL DEFAULT 0,
  read_time datetime DEFAULT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_id (user_id),
  KEY idx_alarm_id (alarm_id),
  KEY idx_is_read (is_read),
  KEY idx_user_read_time (user_id, is_read, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
