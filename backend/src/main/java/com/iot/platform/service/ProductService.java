package com.iot.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.platform.entity.Attribute;
import com.iot.platform.entity.Command;
import com.iot.platform.mapper.CommandMapper;
import com.iot.platform.entity.Product;
import com.iot.platform.mapper.AttributeMapper;
import com.iot.platform.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品管理服务
 */
@Slf4j
@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {
    
    @Resource
    private AttributeMapper attributeMapper;
    
    @Resource
    private CommandMapper commandMapper;
    
    /**
     * 创建产品
     */
    @Transactional(rollbackFor = Exception.class)
    public Product createProduct(Product product) {
        // 检查产品型号是否已存在
        LambdaQueryWrapper<Product> query = new LambdaQueryWrapper<>();
        query.eq(Product::getProductModel, product.getProductModel());
        Product existProduct = this.getOne(query);
        if (existProduct != null) {
            throw new RuntimeException("产品型号已存在: " + product.getProductModel());
        }
        
        // 兼容处理：如果 protocol 是 "MQTT"，转换为 "MQTT1.0"
        if (product.getProtocol() != null && "MQTT".equalsIgnoreCase(product.getProtocol())) {
            product.setProtocol("MQTT1.0");
        }
        // 如果 protocol 为空，默认设置为 MQTT1.0
        if (product.getProtocol() == null || product.getProtocol().trim().isEmpty()) {
            product.setProtocol("MQTT1.0");
        }
        
        // 保存产品
        this.save(product);
        log.info("创建产品成功: {}，协议类型: {}", product.getProductName(), product.getProtocol());
        return product;
    }
    
    /**
     * 为产品添加属性
     */
    @Transactional(rollbackFor = Exception.class)
    public Attribute addAttribute(Attribute attribute) {
        // 检查产品是否存在
        Product product = this.getById(attribute.getProductId());
        if (product == null) {
            throw new RuntimeException("产品不存在: " + attribute.getProductId());
        }
        
        // 检查属性标识符是否重复
        LambdaQueryWrapper<Attribute> query = new LambdaQueryWrapper<>();
        query.eq(Attribute::getProductId, attribute.getProductId())
             .eq(Attribute::getAddr, attribute.getAddr());
        Attribute existAttr = attributeMapper.selectOne(query);
        if (existAttr != null) {
            throw new RuntimeException("属性标识符已存在: " + attribute.getAddr());
        }
        
        // 保存属性
        attributeMapper.insert(attribute);
        log.info("添加产品属性成功: {} - {}", product.getProductName(), attribute.getAttrName());
        return attribute;
    }
    
    /**
     * 获取产品的所有属性
     */
    public List<Attribute> getProductAttributes(Long productId) {
        LambdaQueryWrapper<Attribute> query = new LambdaQueryWrapper<>();
        query.eq(Attribute::getProductId, productId)
             .orderByAsc(Attribute::getId);
        return attributeMapper.selectList(query);
    }
    
    /**
     * 根据产品型号获取产品
     */
    public Product getByModel(String productModel) {
        LambdaQueryWrapper<Product> query = new LambdaQueryWrapper<>();
        query.eq(Product::getProductModel, productModel);
        return this.getOne(query);
    }
    
    /**
     * 为产品添加命令 ⭐️ 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public Command addCommand(Command command) {
        // 检查产品是否存在
        Product product = this.getById(command.getProductId());
        if (product == null) {
            throw new RuntimeException("产品不存在: " + command.getProductId());
        }
        
        // 保存命令
        commandMapper.insert(command);
        log.info("添加产品命令成功: {} - {}", product.getProductName(), command.getCommandName());
        return command;
    }
    
    /**
     * 获取产品的所有命令 ⭐️ 新增
     */
    public List<Command> getProductCommands(Long productId) {
        LambdaQueryWrapper<Command> query = new LambdaQueryWrapper<>();
        query.eq(Command::getProductId, productId)
             .orderByAsc(Command::getId);
        return commandMapper.selectList(query);
    }
    
    /**
     * 更新产品属性（只允许更新属性名称和单位，不允许更新标识符addr）
     */
    @Transactional(rollbackFor = Exception.class)
    public Attribute updateAttribute(Attribute attribute) {
        if (attribute.getId() == null) {
            throw new RuntimeException("属性ID不能为空");
        }
        
        // 获取现有属性
        Attribute existAttr = attributeMapper.selectById(attribute.getId());
        if (existAttr == null) {
            throw new RuntimeException("属性不存在: " + attribute.getId());
        }
        
        // 只更新属性名称和单位，不允许修改addr、productId、dataType
        existAttr.setAttrName(attribute.getAttrName());
        existAttr.setUnit(attribute.getUnit());
        existAttr.setDescription(attribute.getDescription()); // 也允许更新描述
        
        attributeMapper.updateById(existAttr);
        log.info("更新产品属性成功: {} - {}", existAttr.getAddr(), existAttr.getAttrName());
        return existAttr;
    }
    
    /**
     * 删除产品属性
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttribute(Long id) {
        attributeMapper.deleteById(id);
        log.info("删除产品属性成功: {}", id);
    }
    
    /**
     * 删除产品命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommand(Long id) {
        commandMapper.deleteById(id);
        log.info("删除产品命令成功: {}", id);
    }
    
    /**
     * 初始化预置产品（智能电表、智能水表、智能气表）
     * 幂等操作：如果产品已存在，则跳过创建
     */
    @Transactional(rollbackFor = Exception.class)
    public void initPresetProducts() {
        log.info("开始初始化预置产品...");
        
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 创建智能电表
        Product elecProduct = getByModel("ELEC");
        if (elecProduct == null) {
            elecProduct = new Product();
            elecProduct.setProductName("智能电表");
            elecProduct.setProductModel("ELEC");
            elecProduct.setProtocol("MQTT");
            elecProduct.setDescription("用于监测电力能耗");
            elecProduct.setStatus(1);
            elecProduct.setCreateTime(now);
            elecProduct.setUpdateTime(now);
            this.save(elecProduct);
            log.info("创建预置产品成功: 智能电表");
        } else {
            // 如果产品已存在，更新产品名称和描述（修复编码问题）
            elecProduct.setProductName("智能电表");
            elecProduct.setDescription("用于监测电力能耗");
            elecProduct.setUpdateTime(now);
            this.updateById(elecProduct);
            log.info("更新预置产品名称: 智能电表");
        }
        
        // 添加电表属性（无论产品是新创建还是已存在，都需要确保属性完整）
        addPresetAttributes(elecProduct.getId(), new String[][]{
            {"voltage", "电压", "float", "V", "实时电压"},
            {"current", "电流", "float", "A", "实时电流"},
            {"active_power", "有功功率", "float", "kW", "实时有功功率"},
            {"reactive_power", "无功功率", "float", "kvar", "实时无功功率"},
            {"power_factor", "功率因数", "float", "", "功率因数（0-1）"},
            {"total_energy", "总有功电能", "float", "kWh", "表底示数（累计值）"}
        }, now);
        
        // 1.2 创建三相智能电表
        Product elec3PhaseProduct = getByModel("ELEC_3PHASE");
        if (elec3PhaseProduct == null) {
            elec3PhaseProduct = new Product();
            elec3PhaseProduct.setProductName("三相智能电表");
            elec3PhaseProduct.setProductModel("ELEC_3PHASE");
            elec3PhaseProduct.setProtocol("MQTT");
            elec3PhaseProduct.setDescription("用于监测三相电力能耗");
            elec3PhaseProduct.setStatus(1);
            elec3PhaseProduct.setCreateTime(now);
            elec3PhaseProduct.setUpdateTime(now);
            this.save(elec3PhaseProduct);
            log.info("创建预置产品成功: 三相智能电表");
        } else {
            // 如果产品已存在，更新产品名称和描述
            elec3PhaseProduct.setProductName("三相智能电表");
            elec3PhaseProduct.setDescription("用于监测三相电力能耗");
            elec3PhaseProduct.setUpdateTime(now);
            this.updateById(elec3PhaseProduct);
            log.info("更新预置产品名称: 三相智能电表");
        }
        
        // 添加三相电表属性
        addPresetAttributes(elec3PhaseProduct.getId(), new String[][]{
            {"voltage_a", "A相电压", "float", "V", "A相实时电压"},
            {"voltage_b", "B相电压", "float", "V", "B相实时电压"},
            {"voltage_c", "C相电压", "float", "V", "C相实时电压"},
            {"current_a", "A相电流", "float", "A", "A相实时电流"},
            {"current_b", "B相电流", "float", "A", "B相实时电流"},
            {"current_c", "C相电流", "float", "A", "C相实时电流"},
            {"active_power", "总有功功率", "float", "kW", "三相总有功功率"},
            {"reactive_power", "总无功功率", "float", "kvar", "三相总无功功率"},
            {"power_factor", "功率因数", "float", "", "功率因数（0-1）"},
            {"total_energy", "总有功电能", "float", "kWh", "表底示数（累计值）"}
        }, now);
        
        // 2. 创建智能水表
        Product waterProduct = getByModel("WATER");
        if (waterProduct == null) {
            waterProduct = new Product();
            waterProduct.setProductName("智能水表");
            waterProduct.setProductModel("WATER");
            waterProduct.setProtocol("MQTT");
            waterProduct.setDescription("用于监测水耗");
            waterProduct.setStatus(1);
            waterProduct.setCreateTime(now);
            waterProduct.setUpdateTime(now);
            this.save(waterProduct);
            log.info("创建预置产品成功: 智能水表");
        } else {
            // 如果产品已存在，更新产品名称和描述（修复编码问题）
            waterProduct.setProductName("智能水表");
            waterProduct.setDescription("用于监测水耗");
            waterProduct.setUpdateTime(now);
            this.updateById(waterProduct);
            log.info("更新预置产品名称: 智能水表");
        }
        
        // 添加水表属性
        addPresetAttributes(waterProduct.getId(), new String[][]{
            {"total_flow", "总流量", "float", "m³", "表底示数（累计值）"},
            {"instantaneous_flow", "瞬时流量", "float", "m³/h", "实时流量"},
            {"water_pressure", "水压", "float", "MPa", "实时水压"},
            {"valve_status", "阀门状态", "int", "", "0-关闭，1-打开"}
        }, now);
        
        // 3. 创建智能气表
        Product gasProduct = getByModel("GAS");
        if (gasProduct == null) {
            gasProduct = new Product();
            gasProduct.setProductName("智能气表");
            gasProduct.setProductModel("GAS");
            gasProduct.setProtocol("MQTT");
            gasProduct.setDescription("用于监测气耗");
            gasProduct.setStatus(1);
            gasProduct.setCreateTime(now);
            gasProduct.setUpdateTime(now);
            this.save(gasProduct);
            log.info("创建预置产品成功: 智能气表");
        } else {
            // 如果产品已存在，更新产品名称和描述（修复编码问题）
            gasProduct.setProductName("智能气表");
            gasProduct.setDescription("用于监测气耗");
            gasProduct.setUpdateTime(now);
            this.updateById(gasProduct);
            log.info("更新预置产品名称: 智能气表");
        }
        
        // 添加气表属性
        addPresetAttributes(gasProduct.getId(), new String[][]{
            {"total_gas", "总用气量", "float", "m³", "表底示数（累计值）"},
            {"instantaneous_flow", "瞬时流量", "float", "m³/h", "实时流量"},
            {"gas_pressure", "气压", "float", "kPa", "实时气压"},
            {"gas_temperature", "气体温度", "float", "℃", "实时温度"}
        }, now);
        
        log.info("预置产品初始化完成");
    }
    
    /**
     * 为预置产品批量添加属性（内部方法）
     */
    private void addPresetAttributes(Long productId, String[][] attributes, LocalDateTime now) {
        for (String[] attr : attributes) {
            // 检查属性是否已存在
            LambdaQueryWrapper<Attribute> query = new LambdaQueryWrapper<>();
            query.eq(Attribute::getProductId, productId)
                 .eq(Attribute::getAddr, attr[0]);
            Attribute existAttr = attributeMapper.selectOne(query);
            
            if (existAttr == null) {
                Attribute attribute = new Attribute();
                attribute.setProductId(productId);
                attribute.setAddr(attr[0]);
                attribute.setAttrName(attr[1]);
                attribute.setDataType(attr[2]);
                attribute.setUnit(attr[3]);
                attribute.setDescription(attr[4]);
                attribute.setCreateTime(now);
                attributeMapper.insert(attribute);
            }
        }
    }
}
