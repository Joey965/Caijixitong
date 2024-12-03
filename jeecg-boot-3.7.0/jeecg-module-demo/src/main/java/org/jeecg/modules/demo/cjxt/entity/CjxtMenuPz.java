package org.jeecg.modules.demo.cjxt.entity;

import java.io.Serializable;
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
import java.io.UnsupportedEncodingException;

/**
 * @Description: APP功能菜单
 * @Author: jeecg-boot
 * @Date:   2024-10-10
 * @Version: V1.0
 */
@Data
@TableName("cjxt_menu_pz")
@ApiModel(value="cjxt_menu_pz对象", description="APP功能菜单")
public class CjxtMenuPz implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private java.lang.String id;
    /**创建人*/
    @ApiModelProperty(value = "创建人")
    private java.lang.String createBy;
    /**创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private java.util.Date createTime;
    /**修改人*/
    @ApiModelProperty(value = "修改人")
    private java.lang.String updateBy;
    /**修改时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
    private java.util.Date updateTime;
    /**所属部门*/
    @ApiModelProperty(value = "所属部门")
    private java.lang.String sysOrgCode;
    /**作废标识 (1表示删除 0表示未删除)*/
    @Excel(name = "作废标识 (1表示删除 0表示未删除)", width = 15)
    @ApiModelProperty(value = "作废标识 (1表示删除 0表示未删除)")
    @TableLogic
    private java.lang.Integer delFlag;
    /**pid*/
    @Excel(name = "pid", width = 15)
    @ApiModelProperty(value = "pid")
    private java.lang.String pid;
    /**是否有子节点*/
    @Excel(name = "是否有子节点", width = 15)
    @ApiModelProperty(value = "是否有子节点")
    private java.lang.String hasChild;
    /**PC菜单名称*/
    @Excel(name = "PC菜单名称", width = 15)
    @ApiModelProperty(value = "PC菜单名称")
    private java.lang.String pcName;
    /**菜单名称*/
    @Excel(name = "菜单名称", width = 15)
    @ApiModelProperty(value = "菜单名称")
    private java.lang.String appName;
    /**图标*/
    @Excel(name = "图标", width = 15)
    @ApiModelProperty(value = "图标")
    private java.lang.String tb;
    /**key名称*/
    @Excel(name = "key名称", width = 15)
    @ApiModelProperty(value = "key名称")
    private java.lang.String keyName;
    /**useCount*/
    @Excel(name = "useCount", width = 15)
    @ApiModelProperty(value = "useCount")
    private java.lang.String useCount;
    /**微信小程序页面路径*/
    @Excel(name = "微信小程序页面路径", width = 15)
    @ApiModelProperty(value = "微信小程序页面路径")
    private java.lang.String wxPage;
    /**APP/H5页面路径*/
    @Excel(name = "APP/H5页面路径", width = 15)
    @ApiModelProperty(value = "APP/H5页面路径")
    private java.lang.String appPage;
    /**角标数字*/
    @Excel(name = "角标数字", width = 15)
    @ApiModelProperty(value = "角标数字")
    private java.lang.String isRemind;
    /**排序*/
    @Excel(name = "排序", width = 15)
    @ApiModelProperty(value = "排序")
    private java.lang.String orderNum;
    /**父菜单名称*/
    @Excel(name = "父菜单名称", width = 15)
    @ApiModelProperty(value = "父菜单名称")
    private java.lang.String parentName;
}
