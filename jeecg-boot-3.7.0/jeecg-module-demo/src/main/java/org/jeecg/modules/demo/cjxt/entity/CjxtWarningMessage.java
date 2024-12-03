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
 * @Description: 预警提示
 * @Author: jeecg-boot
 * @Date:   2024-07-31
 * @Version: V1.0
 */
@Data
@TableName("cjxt_warning_message")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_warning_message对象", description="预警提示")
public class CjxtWarningMessage implements Serializable {
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
	/**用户id*/
	@Excel(name = "用户id", width = 15)
    @ApiModelProperty(value = "用户id")
    private java.lang.String userId;
    /**用户账号*/
    @Excel(name = "用户账号", width = 15)
    @ApiModelProperty(value = "用户账号")
    private java.lang.String username;
	/**用户姓名*/
	@Excel(name = "用户姓名", width = 15)
    @ApiModelProperty(value = "用户姓名")
    private java.lang.String realname;
	/**预警消息内容*/
	@Excel(name = "预警消息内容", width = 15)
    @ApiModelProperty(value = "预警消息内容")
    private java.lang.String message;
	/**阅读状态*/
	@Excel(name = "阅读状态", width = 15, dicCode = "yjts_status")
	@Dict(dicCode = "yjts_status")
    @ApiModelProperty(value = "阅读状态")
    private java.lang.String status;
	/**已存在数据ID*/
	@Excel(name = "已存在数据ID", width = 15)
    @ApiModelProperty(value = "已存在数据ID")
    private java.lang.String dataId;
	/**存在数据表*/
	@Excel(name = "存在数据表", width = 15)
    @ApiModelProperty(value = "存在数据表")
    private java.lang.String bm;
    /**迁移状态*/
    @Excel(name = "迁移状态", width = 15)
    @ApiModelProperty(value = "迁移状态")
    private java.lang.String qyStatus;
    /**消息类型(0-预警消息 1-提醒消息)*/
//    @Excel(name = "消息类型", width = 15)
    @ApiModelProperty(value = "消息类型(0-预警消息 1-提醒消息)")
    private java.lang.String msgType;
}
