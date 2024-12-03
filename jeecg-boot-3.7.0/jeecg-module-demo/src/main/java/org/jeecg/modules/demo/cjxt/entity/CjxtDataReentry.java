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
 * @Description: 数据补录表
 * @Author: jeecg-boot
 * @Date:   2024-08-19
 * @Version: V1.0
 */
@Data
@TableName("cjxt_data_reentry")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_data_reentry对象", description="数据补录表")
public class CjxtDataReentry implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    @Excel(name = "主键", width = 15)
    private java.lang.String id;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private java.lang.String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private java.util.Date createTime;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private java.lang.String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private java.util.Date updateTime;
	/**所属部门*/
    @ApiModelProperty(value = "所属部门")
    private java.lang.String sysOrgCode;
	/**作废标识*/
    @ApiModelProperty(value = "作废标识")
    @TableLogic
    private java.lang.String delFlag;
	/**数据ID*/
	@Excel(name = "数据ID", width = 15)
    @ApiModelProperty(value = "数据ID")
    private java.lang.String dataId;
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
	/**地址名称*/
	@Excel(name = "地址名称", width = 15)
    @ApiModelProperty(value = "地址名称")
    private java.lang.String addressName;
	/**派发人ID*/
	@Excel(name = "派发人ID", width = 15)
    @ApiModelProperty(value = "派发人ID")
    private java.lang.String dispatcherId;
	/**派发人姓名*/
	@Excel(name = "派发人姓名", width = 15)
    @ApiModelProperty(value = "派发人姓名")
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
	@Excel(name = "截止日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "截止日期")
    private java.util.Date dueDate;
	/**补录状态*/
    @ApiModelProperty(value = "补录状态")
    @Dict(dicCode = "sjbl_tatus")
    private java.lang.String blzt;
	/**补录描述*/
	@Excel(name = "补录描述", width = 15)
    @ApiModelProperty(value = "补录描述")
    private java.lang.String blms;
}
