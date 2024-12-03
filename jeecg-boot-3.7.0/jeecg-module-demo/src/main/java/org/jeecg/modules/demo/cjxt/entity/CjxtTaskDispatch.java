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
 * @Description: 任务派发表
 * @Author: jeecg-boot
 * @Date:   2024-06-14
 * @Version: V1.0
 */
@Data
@TableName("cjxt_task_dispatch")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_task_dispatch对象", description="任务派发表")
public class CjxtTaskDispatch implements Serializable {
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
	/**作废标识*/
	@Excel(name = "作废标识", width = 15)
    @ApiModelProperty(value = "作废标识")
    @TableLogic
    private java.lang.Integer delFlag;
    /**任务名称派发主键*/
//    @Excel(name = "任务名称派发主键", width = 15)
    @ApiModelProperty(value = "任务名称派发主键")
    private java.lang.String taskId;
    /**任务名称派发Code*/
//    @Excel(name = "任务名称派发Code", width = 15)
    @ApiModelProperty(value = "任务名称派发Code")
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
	@Excel(name = "模版ID", width = 15)
    @ApiModelProperty(value = "模版ID")
    private java.lang.String mbId;
	/**模版编码*/
	@Excel(name = "模版编码", width = 15)
    @ApiModelProperty(value = "模版编码")
    private java.lang.String mbCode;
	/**模版名称*/
	@Excel(name = "模版名称", width = 15)
    @ApiModelProperty(value = "模版名称")
    private java.lang.String mbName;
	/**表名*/
	@Excel(name = "表名", width = 15)
    @ApiModelProperty(value = "表名")
    private java.lang.String bm;
	/**地址ID*/
	@Excel(name = "地址ID", width = 15)
    @ApiModelProperty(value = "地址ID")
    private java.lang.String addressId;
    /**地址编码*/
    @Excel(name = "地址编码", width = 15)
    @ApiModelProperty(value = "地址编码")
    private java.lang.String addressCode;
	/**地址名称*/
	@Excel(name = "地址名称", width = 15)
    @ApiModelProperty(value = "地址名称")
    private java.lang.String addressName;
	/**派发人ID*/
	@Excel(name = "派发人ID", width = 15)
    @ApiModelProperty(value = "派发人ID")
    private java.lang.String dispatcherId;
	/**派发人名称*/
	@Excel(name = "派发人名称", width = 15)
    @ApiModelProperty(value = "派发人名称")
    private java.lang.String dispatcherName;
	/**接收人ID*/
	@Excel(name = "接收人ID", width = 15)
    @ApiModelProperty(value = "接收人ID")
    private java.lang.String receiverId;
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
	@Excel(name = "完成时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "完成时间")
    private java.util.Date wcsj;
	/**数据ID*/
	@Excel(name = "数据ID", width = 15)
    @ApiModelProperty(value = "数据ID")
    private java.lang.String dataId;
	/**核实状态*/
	@Excel(name = "核实状态", width = 15, dicCode = "hszt")
	@Dict(dicCode = "hszt")
    @ApiModelProperty(value = "核实状态")
    private java.lang.String hszt;
	/**上次核实时间*/
	@Excel(name = "上次核实时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "上次核实时间")
    private java.util.Date schssj;
	/**任务状态*/
	@Excel(name = "任务状态", width = 15, dicCode = "rwzt")
	@Dict(dicCode = "rwzt")
    @ApiModelProperty(value = "任务状态")
    private java.lang.String rwzt;
	/**补录状态*/
	@Excel(name = "补录状态", width = 15, dicCode = "sjbl_status")
	@Dict(dicCode = "sjbl_status")
    @ApiModelProperty(value = "补录状态")
    private java.lang.String status;
	/**补录表主键*/
	@Excel(name = "补录表主键", width = 15)
    @ApiModelProperty(value = "补录表主键")
    private java.lang.String blId;
    /**补录表主键*/
//    @Excel(name = "完善状态", width = 15)
    @ApiModelProperty(value = "完善状态")
    private java.lang.String wszt;
    /**审核描述*/
//    @Excel(name = "审核描述", width = 15)
    @ApiModelProperty(value = "审核描述")
    private java.lang.String errMsg;
}
