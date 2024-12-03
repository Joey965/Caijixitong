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
 * @Description: 随手拍
 * @Author: jeecg-boot
 * @Date:   2024-08-19
 * @Version: V1.0
 */
@Data
@TableName("cjxt_snap_shot")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_snap_shot对象", description="随手拍")
public class CjxtSnapShot implements Serializable {
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
	/**作废标识*/
	@Excel(name = "作废标识", width = 15)
    @ApiModelProperty(value = "作废标识")
    @TableLogic
    private java.lang.String delFlag;
	/**上报人id*/
	@Excel(name = "上报人id", width = 15)
    @ApiModelProperty(value = "上报人id")
    private java.lang.String sbrId;
	/**上报人姓名*/
	@Excel(name = "上报人姓名", width = 15)
    @ApiModelProperty(value = "上报人姓名")
    private java.lang.String sbrName;
	/**上报人手机号*/
	@Excel(name = "上报人手机号", width = 15)
    @ApiModelProperty(value = "上报人手机号")
    private java.lang.String sbrPhone;
	/**上报时间*/
	@Excel(name = "上报时间", width = 15)
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "上报时间")
    private java.util.Date sbTime;
	/**上报标题*/
	@Excel(name = "上报标题", width = 15)
    @ApiModelProperty(value = "上报标题")
    private java.lang.String sbTitle;
	/**上报类型*/
	@Excel(name = "上报类型", width = 15, dicCode = "sblx")
	@Dict(dicCode = "sblx")
    @ApiModelProperty(value = "上报类型")
    private java.lang.String sbType;
	/**上报图片*/
	@Excel(name = "上报图片", width = 15)
    @ApiModelProperty(value = "上报图片")
    private java.lang.String sbPic;
	/**上报内容*/
	@Excel(name = "上报内容", width = 15)
    @ApiModelProperty(value = "上报内容")
    private java.lang.String sbContent;
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
	/**纬度*/
	@Excel(name = "纬度", width = 15)
    @ApiModelProperty(value = "纬度")
    private java.lang.String latitude;
	/**经度*/
	@Excel(name = "经度", width = 15)
    @ApiModelProperty(value = "经度")
    private java.lang.String longitude;
	/**地址*/
	@Excel(name = "地址", width = 15)
    @ApiModelProperty(value = "地址")
    private java.lang.String fullAddress;
    /**处理状态*/
    @Excel(name = "处理状态", width = 15, dicCode = "clzt")
    @Dict(dicCode = "clzt")
    @ApiModelProperty(value = "处理状态")
    private java.lang.String clzt;
    /**处理描述*/
    @Excel(name = "处理描述", width = 35)
    @ApiModelProperty(value = "处理描述")
    private java.lang.String clms;
}
