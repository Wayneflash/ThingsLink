package com.iot.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("tb_user")
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;      // 用户名
    
    private String password;      // 密码（加密存储）
    
    @TableField("real_name")
    private String realName;      // 真实姓名
    
    private String phone;         // 手机号
    
    @TableField("group_id")
    private Long groupId;         // 用户分组ID
    
    private String email;         // 邮箱
    
    private Integer status;       // 状态：0-禁用，1-启用
    
    @TableField("role_id")
    private Long roleId;          // 角色ID（单选，必填）
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
}
