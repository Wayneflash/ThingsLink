package com.iot.platform.controller;

import com.iot.platform.common.Result;
import com.iot.platform.entity.Attribute;
import com.iot.platform.entity.Command;
import com.iot.platform.entity.Product;
import com.iot.platform.entity.Device;
import com.iot.platform.mapper.DeviceMapper;
import com.iot.platform.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 产品管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductController {
    
    @Resource
    private ProductService productService;
    
    @Resource
    private DeviceMapper deviceMapper;
    
    /**
     * 创建产品
     */
    @PostMapping("/create")
    public Result<Product> createProduct(@RequestBody Map<String, Object> params) {
        try {
            Product product = new Product();
            
            // 支持name和productName两种参数名
            String productName = params.get("name") != null ? 
                params.get("name").toString() : 
                (params.get("productName") != null ? params.get("productName").toString() : null);
            
            // 支持model和productModel两种参数名
            String productModel = params.get("model") != null ? 
                params.get("model").toString() : 
                (params.get("productModel") != null ? params.get("productModel").toString() : null);
            
            // 支持code作为productModel的别名
            if (productModel == null && params.get("code") != null) {
                productModel = params.get("code").toString();
            }
            
            // 验证必填字段
            if (productName == null || productName.trim().isEmpty()) {
                return Result.error("产品名称不能为空");
            }
            if (productModel == null || productModel.trim().isEmpty()) {
                return Result.error("产品型号不能为空");
            }
            
            product.setProductName(productName);
            product.setProductModel(productModel);
            
            if (params.get("protocol") != null) {
                product.setProtocol(params.get("protocol").toString());
            }
            if (params.get("description") != null) {
                product.setDescription(params.get("description").toString());
            }
            if (params.get("status") != null) {
                product.setStatus(Integer.valueOf(params.get("status").toString()));
            } else {
                product.setStatus(1); // 默认启用
            }
            
            Product result = productService.createProduct(product);
            return Result.success(result, "创建成功");
        } catch (Exception e) {
            log.error("创建产品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取产品列表
     */
    @PostMapping("/list")
    public Result<Map<String, Object>> getProductList(@RequestBody(required = false) Map<String, Object> request) {
        try {
            // 如果request为null，初始化为空对象
            if (request == null) {
                request = new HashMap<>();
            }
            
            // 从请求中获取分页参数
            Integer page = request.get("page") != null ? (Integer) request.get("page") : 1;
            Integer pageSize = request.get("pageSize") != null ? (Integer) request.get("pageSize") : 20;
            String keyword = request.get("keyword") != null ? (String) request.get("keyword") : null;
            String category = request.get("category") != null ? (String) request.get("category") : null;
            
            List<Product> list = productService.list(); // 暂时返回所有产品，后续可添加分页和筛选逻辑
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("total", list.size());
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) list.size() / pageSize));
            result.put("list", list);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取产品列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取产品详情
     */
    @PostMapping("/detail")
    public Result<Map<String, Object>> getProduct(@RequestBody Map<String, Object> request) {
        try {
            Long id = request.get("id") != null ? Long.valueOf(request.get("id").toString()) : null;
            if (id == null) {
                return Result.error("产品ID不能为空");
            }
            
            Product product = productService.getById(id);
            if (product == null) {
                return Result.error("产品不存在");
            }
            
            // 获取产品属性和命令
            List<Attribute> attributes = productService.getProductAttributes(id);
            List<Command> commands = productService.getProductCommands(id);
            
            // 构造返回数据
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("id", product.getId());
            result.put("productName", product.getProductName());
            result.put("productModel", product.getProductModel());
            result.put("protocol", product.getProtocol());
            result.put("description", product.getDescription());
            result.put("status", product.getStatus());
            result.put("attrs", attributes != null ? attributes : new java.util.ArrayList<>());
            result.put("commands", commands != null ? commands : new java.util.ArrayList<>());
            result.put("createTime", product.getCreateTime());
            result.put("updateTime", product.getUpdateTime());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取产品详情失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 添加产品属性
     */
    @PostMapping("/attribute/create")
    public Result<Attribute> addAttribute(@RequestBody Attribute attribute) {
        try {
            Attribute result = productService.addAttribute(attribute);
            return Result.success(result, "添加成功");
        } catch (Exception e) {
            log.error("添加产品属性失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取产品属性列表
     */
    @GetMapping("/{productId}/attributes")
    public Result<List<Attribute>> getProductAttributes(@PathVariable Long productId) {
        try {
            List<Attribute> list = productService.getProductAttributes(productId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取产品属性失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 添加产品命令 ⭐️ 新增
     */
    @PostMapping("/command/create")
    public Result<Command> addCommand(@RequestBody Command command) {
        try {
            Command result = productService.addCommand(command);
            return Result.success(result, "添加成功");
        } catch (Exception e) {
            log.error("添加产品命令失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取产品命令列表 ⭐️ 新增
     */
    @GetMapping("/{productId}/commands")
    public Result<List<Command>> getProductCommands(@PathVariable Long productId) {
        try {
            List<Command> list = productService.getProductCommands(productId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取产品命令失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新产品
     */
    @PostMapping("/update")  // 添加更新产品接口
    public Result<Product> updateProduct(@RequestBody Product product) {
        try {
            if (product.getId() == null) {
                return Result.error("产品ID不能为空");
            }
            productService.updateById(product);
            Product result = productService.getById(product.getId());
            return Result.success(result, "更新成功");
        } catch (Exception e) {
            log.error("更新产品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除产品
     */
    @PostMapping("/delete")
    public Result<Void> deleteProduct(@RequestBody Map<String, Object> params) {
        try {
            Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;
            if (id == null) {
                return Result.error("产品ID不能为空");
            }
            
            // 检查产品是否存在
            Product product = productService.getById(id);
            if (product == null) {
                return Result.error("产品不存在");
            }
            
            // 检查该产品下是否有设备
            LambdaQueryWrapper<Device> deviceQuery = new LambdaQueryWrapper<>();
            deviceQuery.eq(Device::getProductId, id);
            Long deviceCount = deviceMapper.selectCount(deviceQuery);
            if (deviceCount != null && deviceCount > 0) {
                return Result.error("该产品下有设备，无法删除");
            }
            
            productService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除产品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除产品属性
     */
    @PostMapping("/attribute/delete")
    public Result<Void> deleteAttribute(@RequestBody Map<String, Object> params) {
        try {
            Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;
            if (id == null) {
                return Result.error("属性ID不能为空");
            }
            productService.deleteAttribute(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除产品属性失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除产品命令
     */
    @PostMapping("/command/delete")
    public Result<Void> deleteCommand(@RequestBody Map<String, Object> params) {
        try {
            Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;
            if (id == null) {
                return Result.error("命令ID不能为空");
            }
            productService.deleteCommand(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除产品命令失败", e);
            return Result.error(e.getMessage());
        }
    }
}