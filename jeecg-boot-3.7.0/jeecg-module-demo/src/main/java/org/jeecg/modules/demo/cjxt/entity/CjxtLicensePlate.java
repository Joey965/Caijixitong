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
 * @Description: 车辆核查
 * @Author: jeecg-boot
 * @Date:   2024-08-20
 * @Version: V1.0
 */
@Data
@TableName("cjxt_license_plate")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_license_plate对象", description="车辆核查")
public class CjxtLicensePlate implements Serializable {
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
	/**车牌号码*/
	@Excel(name = "车牌号码", width = 15)
    @ApiModelProperty(value = "车牌号码")
    private java.lang.String cphm;
	/**车辆类型*/
    @Excel(name = "车辆类型", width = 15, dicCode = "hpzl")
    @Dict(dicCode = "hpzl")
    @ApiModelProperty(value = "车辆类型")
    private java.lang.String cllx;
	/**车身颜色*/
	@Excel(name = "车身颜色", width = 15)
    @ApiModelProperty(value = "车身颜色")
    private java.lang.String csys;
	/**发动机号*/
	@Excel(name = "发动机号", width = 15)
    @ApiModelProperty(value = "发动机号")
    private java.lang.String fdjh;
	/**车辆品牌*/
	@Excel(name = "车辆品牌", width = 15)
    @ApiModelProperty(value = "车辆品牌")
    private java.lang.String clpp;
	/**车辆照片*/
	@Excel(name = "车辆照片", width = 15)
    @ApiModelProperty(value = "车辆照片")
    private java.lang.String clzp;
	/**使用人身份证*/
	@Excel(name = "使用人身份证", width = 15)
    @ApiModelProperty(value = "使用人身份证")
    private java.lang.String syrsfz;
	/**使用人*/
	@Excel(name = "使用人", width = 15)
    @ApiModelProperty(value = "使用人")
    private java.lang.String syr;
	/**驾驶人姓名*/
	@Excel(name = "驾驶人姓名", width = 15)
    @ApiModelProperty(value = "驾驶人姓名")
    private java.lang.String jsrxm;
	/**驾驶人性别*/
	@Excel(name = "驾驶人性别", width = 15, dicCode = "sex")
	@Dict(dicCode = "sex")
    @ApiModelProperty(value = "驾驶人性别")
    private java.lang.String jsrxb;
	/**驾驶人电话*/
	@Excel(name = "驾驶人电话", width = 15)
    @ApiModelProperty(value = "驾驶人电话")
    private java.lang.String jsrdh;
	/**驾驶证号*/
	@Excel(name = "驾驶证号", width = 15)
    @ApiModelProperty(value = "驾驶证号")
    private java.lang.String jszh;
	/**经度*/
	@Excel(name = "经度", width = 15)
    @ApiModelProperty(value = "经度")
    private java.lang.String longitude;
	/**纬度*/
	@Excel(name = "纬度", width = 15)
    @ApiModelProperty(value = "纬度")
    private java.lang.String latitude;
	/**采集地址*/
	@Excel(name = "采集地址", width = 15)
    @ApiModelProperty(value = "采集地址")
    private java.lang.String cjdz;
	/**采集时间*/
	@Excel(name = "采集时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "采集时间")
    private java.util.Date cjsj;
}
