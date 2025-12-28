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
        
        // 保存产品
        this.save(product);
        log.info("创建产品成功: {}", product.getProductName());
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
}
