package org.jeecg.modules.demo.cjxt.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: APP角色菜单配置
 * @Author: jeecg-boot
 * @Date:   2024-10-10
 * @Version: V1.0
 */
@Data
@TableName("cjxt_role_menu")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_role_menu对象", description="APP角色菜单配置")
public class CjxtRoleMenu implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private java.lang.String id;
    /**所属部门*/
    @ApiModelProperty(value = "所属部门")
    private java.lang.String sysOrgCode;
    /**修改人*/
    @ApiModelProperty(value = "修改人")
    private java.lang.String updateBy;
    /**修改时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
    private java.util.Date updateTime;
    /**创建人*/
    @ApiModelProperty(value = "创建人")
    private java.lang.String createBy;
    /**创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private java.util.Date createTime;
    /**作废标识 (1表示删除 0表示未删除)*/
    @Excel(name = "作废标识 (1表示删除 0表示未删除)", width = 15)
    @ApiModelProperty(value = "作废标识 (1表示删除 0表示未删除)")
    @TableLogic
    private java.lang.Integer delFlag;
    /**角色ID*/
    @Excel(name = "角色ID", width = 15)
    @ApiModelProperty(value = "角色ID")
    private java.lang.String roleId;
    /**角色Code*/
    @Excel(name = "角色Code", width = 15)
    @ApiModelProperty(value = "角色Code")
    private java.lang.String roleCode;
    /**菜单ID*/
    @Excel(name = "菜单ID", width = 15)
    @ApiModelProperty(value = "菜单ID")
    private java.lang.String menuId;


    public CjxtRoleMenu() {
    }

    public CjxtRoleMenu(String roleId, String menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }
}
