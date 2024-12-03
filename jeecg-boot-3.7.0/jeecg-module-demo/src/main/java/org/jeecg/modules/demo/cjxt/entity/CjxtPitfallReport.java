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
 * @Description: 隐患上报
 * @Author: jeecg-boot
 * @Date:   2024-08-01
 * @Version: V1.0
 */
@Data
@TableName("cjxt_pitfall_report")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_pitfall_report对象", description="隐患上报")
public class CjxtPitfallReport implements Serializable {
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
	/**作废标识*/
	@Excel(name = "作废标识", width = 15)
    @ApiModelProperty(value = "作废标识")
    @TableLogic
    private java.lang.Integer delFlag;
	/**上报人id*/
	@Excel(name = "上报人id", width = 15)
    @ApiModelProperty(value = "上报人id")
    private java.lang.String sbrId;
	/**上报人姓名*/
	@Excel(name = "上报人姓名", width = 15)
    @ApiModelProperty(value = "上报人姓名")
    private java.lang.String sbrName;
	/**上报人手机号码*/
	@Excel(name = "上报人手机号码", width = 15)
    @ApiModelProperty(value = "上报人手机号码")
    private java.lang.String sbrPhone;
	/**上报部门ID*/
	@Excel(name = "上报部门ID", width = 15)
    @ApiModelProperty(value = "上报部门ID")
    private java.lang.String sbbmid;
	/**上报部门*/
	@Excel(name = "上报部门", width = 15)
    @ApiModelProperty(value = "上报部门")
    private java.lang.String sbbm;
	/**处理部门id*/
	@Excel(name = "处理部门id", width = 15)
    @ApiModelProperty(value = "处理部门id")
    private java.lang.String clbmId;
	/**处理部门*/
	@Excel(name = "处理部门", width = 15)
    @ApiModelProperty(value = "处理部门")
    private java.lang.String clbm;
	/**处理人id*/
	@Excel(name = "处理人id", width = 15)
    @ApiModelProperty(value = "处理人id")
    private java.lang.String clrId;
	/**处理人*/
	@Excel(name = "处理人", width = 15)
    @ApiModelProperty(value = "处理人")
    private java.lang.String clr;
	/**处理状态*/
	@Excel(name = "处理状态", width = 15, dicCode = "clzt")
	@Dict(dicCode = "clzt")
    @ApiModelProperty(value = "处理状态")
    private java.lang.String clzt;
	/**处理描述*/
	@Excel(name = "处理描述", width = 15)
    @ApiModelProperty(value = "处理描述")
    private java.lang.String clms;
	/**隐患类型*/
    @Excel(name = "隐患类型", width = 15, dicCode = "yhlx")
    @Dict(dicCode = "yhlx")
    @ApiModelProperty(value = "隐患类型")
    private java.lang.String yhlx;
	/**隐患描述*/
	@Excel(name = "隐患描述", width = 15)
    @ApiModelProperty(value = "隐患描述")
    private java.lang.String yhms;
	/**是否保密*/
	@Excel(name = "是否保密", width = 15, dicCode = "yn")
	@Dict(dicCode = "yn")
    @ApiModelProperty(value = "是否保密")
    private java.lang.String sfbm;
	/**隐患地点*/
	@Excel(name = "隐患地点", width = 15)
    @ApiModelProperty(value = "隐患地点")
    private java.lang.String yhdd;
	/**隐患时间*/
	@Excel(name = "隐患时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "隐患时间")
    private java.util.Date yhsj;
	/**现场图片*/
	@Excel(name = "现场图片", width = 15)
    @ApiModelProperty(value = "现场图片")
    private java.lang.String xctp;
	/**现场视频*/
	@Excel(name = "现场视频", width = 15)
    @ApiModelProperty(value = "现场视频")
    private java.lang.String xcsp;
	/**经度*/
	@Excel(name = "经度", width = 15)
    @ApiModelProperty(value = "经度")
    private java.lang.String longitude;
	/**纬度*/
	@Excel(name = "纬度", width = 15)
    @ApiModelProperty(value = "纬度")
    private java.lang.String latitude;
}
