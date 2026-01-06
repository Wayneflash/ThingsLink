package com.iot.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品命令实体
 */
@Data
@TableName("tb_command")
public class Command implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long productId;
    
    private String addr;
    
    private String commandName;
    
    private String commandValue;
    
    private String description;
    
    private LocalDateTime createTime;
}
