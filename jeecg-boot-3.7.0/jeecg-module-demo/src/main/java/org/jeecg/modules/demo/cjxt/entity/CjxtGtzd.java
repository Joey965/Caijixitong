package org.jeecg.modules.demo.cjxt.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 共通字段配置
 * @Author: jeecg-boot
 * @Date:   2024-06-06
 * @Version: V1.0
 */
@Data
@TableName("cjxt_gtzd")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_gtzd对象", description="共通字段配置")
public class CjxtGtzd implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
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
	/**删除标识*/
	@Excel(name = "删除标识", width = 15)
    @ApiModelProperty(value = "删除标识")
    @TableLogic
    private java.lang.String delFlag;
	/**字段名称*/
	@Excel(name = "字段名称", width = 15)
    @ApiModelProperty(value = "字段名称")
    private java.lang.String zdname;
	/**字段备注*/
	@Excel(name = "字段备注", width = 15)
    @ApiModelProperty(value = "字段备注")
    private java.lang.String zdbz;
	/**字段长度*/
	@Excel(name = "字段长度", width = 15)
    @ApiModelProperty(value = "字段长度")
    private java.lang.String zdcd;
	/**默认值*/
	@Excel(name = "默认值", width = 15)
    @ApiModelProperty(value = "默认值")
    private java.lang.String mrz;
	/**字段类型*/
	@Excel(name = "字段类型", width = 15, dicCode = "zdlxtype")
	@Dict(dicCode = "zdlxtype")
    @ApiModelProperty(value = "字段类型")
    private java.lang.String zdlx;
	/**控件类型*/
	@Excel(name = "控件类型", width = 15, dicCode = "kjlxtype")
	@Dict(dicCode = "kjlxtype")
    @ApiModelProperty(value = "控件类型")
    private java.lang.String kjlx;
	/**是否主键*/
	@Excel(name = "是否主键", width = 15, dicCode = "yn")
    @Dict(dicCode = "yn")
    @ApiModelProperty(value = "是否主键")
    private java.lang.String isKey;
	/**字段序号*/
	@Excel(name = "字段序号", width = 15)
    @ApiModelProperty(value = "字段序号")
    private java.lang.Integer zdnum;
	/**字段模版*/
	@Excel(name = "字段模版", width = 15, dicCode = "zdmb")
	@Dict(dicCode = "zdmb")
    @ApiModelProperty(value = "字段模版")
    private java.lang.String zdmb;
    @Excel(name = "页面属性", width = 15, dicCode = "ymsx")
    @Dict(dicCode = "ymsx")
    @ApiModelProperty(value = "页面属性")
    private java.lang.String ymsx;

    @TableField(exist = false)
    private java.lang.String zdnameDto;
}
