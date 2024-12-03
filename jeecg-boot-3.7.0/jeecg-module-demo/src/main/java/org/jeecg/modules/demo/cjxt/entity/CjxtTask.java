package org.jeecg.modules.demo.cjxt.entity;

import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.UnsupportedEncodingException;

/**
 * @Description: 任务派发
 * @Author: jeecg-boot
 * @Date:   2024-07-04
 * @Version: V1.0
 */
@Data
@TableName("cjxt_task")
@ApiModel(value="cjxt_task对象", description="任务派发")
public class CjxtTask implements Serializable {
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
//    @Excel(name = "作废标识 (1表示删除 0表示未删除)", width = 15)
    @ApiModelProperty(value = "作废标识 (1表示删除 0表示未删除)")
    @TableLogic
    private java.lang.Integer delFlag;
    /**pid*/
//    @Excel(name = "pid", width = 15)
    @ApiModelProperty(value = "pid")
    private java.lang.String pid;
    /**是否有子节点*/
//    @Excel(name = "是否有子节点", width = 15)
    @ApiModelProperty(value = "是否有子节点")
    private java.lang.String hasChild;
    /**任务编号*/
//    @Excel(name = "任务编号", width = 15)
    @ApiModelProperty(value = "任务编号")
    private java.lang.String taskCode;
    /**任务名称*/
    @Excel(name = "任务名称", width = 15)
    @ApiModelProperty(value = "任务名称")
    private java.lang.String taskName;
    /**任务描述*/
    @Excel(name = "任务描述", width = 15)
    @ApiModelProperty(value = "任务描述")
    private java.lang.String taskDescription;
    /**模版ID*/
//    @Excel(name = "模版ID", width = 15)
    @ApiModelProperty(value = "模版ID")
    private java.lang.String mbId;
    /**模版编码*/
//    @Excel(name = "模版编码", width = 15)
    @ApiModelProperty(value = "模版编码")
    private java.lang.String mbCode;
    /**模版名称*/
    @Excel(name = "模版名称", width = 15)
    @ApiModelProperty(value = "模版名称")
    private java.lang.String mbName;
    /**表名*/
//    @Excel(name = "表名", width = 15)
    @ApiModelProperty(value = "表名")
    private java.lang.String bm;
    /**地址ID*/
//    @Excel(name = "地址ID", width = 15)
    @ApiModelProperty(value = "地址ID")
    private java.lang.String addressId;
    /**地址编码*/
//    @Excel(name = "地址编码", width = 15)
    @ApiModelProperty(value = "地址编码")
    private java.lang.String addressCode;
    /**地址名称*/
//    @Excel(name = "地址名称", width = 15)
    @ApiModelProperty(value = "地址名称")
    private java.lang.String addressName;
    /**派发部门数据ID*/
//    @Excel(name = "派发部门数据ID", width = 15)
    @ApiModelProperty(value = "派发部门数据ID")
    private java.lang.String orgId;
    /**派发部门数据Code*/
//    @Excel(name = "派发部门数据Code", width = 15)
    @ApiModelProperty(value = "派发部门数据Code")
    private java.lang.String orgCode;
    /**派发部门名称*/
//    @Excel(name = "派发部门名称", width = 15)
    @ApiModelProperty(value = "派发部门名称")
    private java.lang.String orgName;
    /**派发部门id*/
//    @Excel(name = "派发部门id", width = 15)
    @ApiModelProperty(value = "派发部门id")
    private java.lang.String dispatcherOrgId;
    /**派发部门code*/
//    @Excel(name = "派发部门code", width = 15)
    @ApiModelProperty(value = "派发部门code")
    private java.lang.String dispatcherOrgCode;
    /**派发部门*/
    @Excel(name = "派发部门", width = 15)
    @ApiModelProperty(value = "派发部门")
    private java.lang.String dispatcherOrgName;
    /**派发人ID*/
//    @Excel(name = "派发人ID", width = 15)
    @ApiModelProperty(value = "派发人ID")
    private java.lang.String dispatcherId;
    /**派发人名称*/
    @Excel(name = "派发人名称", width = 15)
    @ApiModelProperty(value = "派发人名称")
    private java.lang.String dispatcherName;
    /**接收部门id*/
//    @Excel(name = "接收部门id", width = 15)
    @ApiModelProperty(value = "接收部门id")
    private java.lang.String receiverOrgId;
    /**接收部门code*/
//    @Excel(name = "接收部门code", width = 15)
    @ApiModelProperty(value = "接收部门code")
    private java.lang.String receiverOrgCode;
    /**接收部门*/
    @Excel(name = "接收部门", width = 15)
    @ApiModelProperty(value = "接收部门")
    private java.lang.String receiverOrgName;
    /**接收部门负责人ID*/
//    @Excel(name = "接收部门负责人ID", width = 15)
    @ApiModelProperty(value = "接收部门负责人ID")
    private java.lang.String receiverBmfzrId;
    /**接收部门负责人账号*/
//    @Excel(name = "接收部门负责人账号", width = 15)
    @ApiModelProperty(value = "接收部门负责人账号")
    private java.lang.String receiverBmfzrZh;
    /**接收部门负责人*/
    @Excel(name = "接收部门负责人", width = 15)
    @ApiModelProperty(value = "接收部门负责人")
    private java.lang.String receiverBmfzrName;
    /**接收人ID*/
//    @Excel(name = "接收人ID", width = 15)
    @ApiModelProperty(value = "接收人ID")
    private java.lang.String receiverId;
    /**接收人账号*/
//    @Excel(name = "接收人账号", width = 15)
    @ApiModelProperty(value = "接收人账号")
    private java.lang.String receiverZh;
    /**接收人名称*/
    @Excel(name = "接收人名称", width = 15)
    @ApiModelProperty(value = "接收人名称")
    private java.lang.String receiverName;
    /**截止日期*/
    @Excel(name = "截止日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "截止日期")
    private java.util.Date dueDate;
    /**完成时间*/
    @Excel(name = "完成时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "完成时间")
    private java.util.Date wcsj;
    /**任务状态*/
    @Excel(name = "任务状态", width = 15, dicCode = "pfrwzt")
    @Dict(dicCode = "pfrwzt")
    @ApiModelProperty(value = "任务状态")
    private java.lang.String rwzt;
    /**总采集*/
    @Excel(name = "总采集", width = 15)
    @ApiModelProperty(value = "总采集")
    private java.lang.Integer cjZs;
    /**已完成采集*/
    @Excel(name = "已完成采集", width = 15)
    @ApiModelProperty(value = "已完成采集")
    private java.lang.Integer cjYwc;
    /**剩余采集*/
    @Excel(name = "剩余采集", width = 15)
    @ApiModelProperty(value = "剩余采集")
    private java.lang.Integer cjSy;
    /**采集完成情况*/
    @Excel(name = "采集完成情况", width = 15)
    @ApiModelProperty(value = "采集完成情况")
    private java.lang.String cjWcqk;
    /**归档时间*/
    @Excel(name = "归档时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "归档时间")
    private java.util.Date gdsj;
    /**总用时*/
    @Excel(name = "总用时", width = 15)
    @ApiModelProperty(value = "总用时")
    private java.lang.String zys;
    /**撤回状态*/
    @ApiModelProperty(value = "撤回状态")
    private java.lang.String chzt;

    /**用户身份*/
    @TableField(exist = false)
    private java.lang.String userSf;

    /**区划ID*/
    @TableField(exist = false)
    private java.lang.String addressQhId;

    /**区划Code*/
    @TableField(exist = false)
    private java.lang.String addressQhCode;

    /**区划名称*/
    @TableField(exist = false)
    private java.lang.String addressQhName;

    /**任务名称*/
    @TableField(exist = false)
    private java.lang.String taskNameDto;

    /**补录Id*/
    @TableField(exist = false)
    private java.lang.String blId;

    /**完善状态*/
    @TableField(exist = false)
    private java.lang.String wsztDto;

}
