-- 修复预置产品名称的编码问题
SET NAMES utf8mb4;
UPDATE tb_product SET product_name = '智能电表' WHERE product_model = 'SMART_METER_ELEC';
UPDATE tb_product SET product_name = '智能水表' WHERE product_model = 'SMART_METER_WATER';
UPDATE tb_product SET product_name = '智能气表' WHERE product_model = 'SMART_METER_GAS';
