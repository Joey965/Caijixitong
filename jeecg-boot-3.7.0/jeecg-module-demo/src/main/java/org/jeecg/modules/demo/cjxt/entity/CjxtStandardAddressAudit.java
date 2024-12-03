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
 * @Description: 地址审核
 * @Author: jeecg-boot
 * @Date:   2024-08-07
 * @Version: V1.0
 */
@Data
@TableName("cjxt_standard_address_audit")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_standard_address_audit对象", description="地址审核")
public class CjxtStandardAddressAudit implements Serializable {
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
	/**民政地址id*/
	@Excel(name = "民政地址id", width = 15)
    @ApiModelProperty(value = "民政地址id")
    private java.lang.String addressIdMz;
	/**民政地址编码*/
	@Excel(name = "民政地址编码", width = 15)
    @ApiModelProperty(value = "民政地址编码")
    private java.lang.String addressCodeMz;
    /**民政地址部门*/
    @Excel(name = "民政地址部门", width = 15)
    @ApiModelProperty(value = "民政地址部门")
    private java.lang.String addressDepartnameMz;
	/**民政地址名称*/
	@Excel(name = "民政地址名称", width = 15)
    @ApiModelProperty(value = "民政地址名称")
    private java.lang.String addressNameMz;
	/**地址编码*/
	@Excel(name = "地址编码", width = 15)
    @ApiModelProperty(value = "地址编码")
    private java.lang.String addressCode;
	/**地址名称*/
	@Excel(name = "地址名称", width = 15)
    @ApiModelProperty(value = "地址名称")
    private java.lang.String addressName;
	/**省市区*/
	@Excel(name = "省市区", width = 15)
    @ApiModelProperty(value = "省市区")
    private java.lang.String ssqCode;
	/**省份编码*/
	@Excel(name = "省份编码", width = 15)
    @ApiModelProperty(value = "省份编码")
    private java.lang.String provinceCode;
	/**省份名称*/
	@Excel(name = "省份名称", width = 15)
    @ApiModelProperty(value = "省份名称")
    private java.lang.String provinceName;
	/**城市编码*/
	@Excel(name = "城市编码", width = 15)
    @ApiModelProperty(value = "城市编码")
    private java.lang.String cityCode;
	/**城市名称*/
	@Excel(name = "城市名称", width = 15)
    @ApiModelProperty(value = "城市名称")
    private java.lang.String cityName;
	/**区县编码*/
	@Excel(name = "区县编码", width = 15)
    @ApiModelProperty(value = "区县编码")
    private java.lang.String districtCode;
	/**区县名称*/
	@Excel(name = "区县名称", width = 15)
    @ApiModelProperty(value = "区县名称")
    private java.lang.String districtName;
	/**街道编码*/
	@Excel(name = "街道编码", width = 15)
    @ApiModelProperty(value = "街道编码")
    private java.lang.String streetCode;
	/**街道名称*/
	@Excel(name = "街道名称", width = 15)
    @ApiModelProperty(value = "街道名称")
    private java.lang.String streetName;
	/**补充说明*/
	@Excel(name = "补充说明", width = 15)
    @ApiModelProperty(value = "补充说明")
    private java.lang.String detailAddress;
	/**路名*/
	@Excel(name = "路名", width = 15)
    @ApiModelProperty(value = "路名")
    private java.lang.String detailLm;
	/**路号名*/
	@Excel(name = "路号名", width = 15)
    @ApiModelProperty(value = "路号名")
    private java.lang.Integer detailLhm;
	/**名称*/
	@Excel(name = "名称", width = 15)
    @ApiModelProperty(value = "名称")
    private java.lang.String detailMc;
	/**地址类型*/
	@Excel(name = "地址类型", width = 15, dicCode = "dzlx")
	@Dict(dicCode = "dzlx")
    @ApiModelProperty(value = "地址类型")
    private java.lang.String dzType;
	/**小区名*/
	@Excel(name = "小区名", width = 15)
    @ApiModelProperty(value = "小区名")
    private java.lang.String dz1Xqm;
	/**楼栋*/
	@Excel(name = "楼栋", width = 15)
    @ApiModelProperty(value = "楼栋")
    private java.lang.String dz1Ld;
	/**单元*/
	@Excel(name = "单元", width = 15)
    @ApiModelProperty(value = "单元")
    private java.lang.String dz1Dy;
	/**室*/
	@Excel(name = "室", width = 15)
    @ApiModelProperty(value = "室")
    private java.lang.String dz1S;
	/**村名*/
	@Excel(name = "村名", width = 15)
    @ApiModelProperty(value = "村名")
    private java.lang.String dz2Cm;
	/**组名*/
	@Excel(name = "组名", width = 15)
    @ApiModelProperty(value = "组名")
    private java.lang.String dz2Zm;
	/**号名*/
	@Excel(name = "号名", width = 15)
    @ApiModelProperty(value = "号名")
    private java.lang.String dz2Hm;
	/**大厦名*/
	@Excel(name = "大厦名", width = 15)
    @ApiModelProperty(value = "大厦名")
    private java.lang.String dz3Dsm;
	/**楼栋名*/
	@Excel(name = "楼栋名", width = 15)
    @ApiModelProperty(value = "楼栋名")
    private java.lang.String dz3Ldm;
	/**室名*/
	@Excel(name = "室名", width = 15)
    @ApiModelProperty(value = "室名")
    private java.lang.String dz3Sm;
	/**排(排号室)*/
	@Excel(name = "排(排号室)", width = 15)
    @ApiModelProperty(value = "排(排号室)")
    private java.lang.String dz5P;
	/**号(排号室)*/
	@Excel(name = "号(排号室)", width = 15)
    @ApiModelProperty(value = "号(排号室)")
    private java.lang.String dz5H;
	/**室(排号室)*/
	@Excel(name = "室(排号室)", width = 15)
    @ApiModelProperty(value = "室(排号室)")
    private java.lang.String dz5S;
	/**室(宿舍)*/
	@Excel(name = "室(宿舍)", width = 15)
    @ApiModelProperty(value = "室(宿舍)")
    private java.lang.String dz6S;
	/**邮政编码*/
	@Excel(name = "邮政编码", width = 15)
    @ApiModelProperty(value = "邮政编码")
    private java.lang.String postalCode;
	/**纬度*/
	@Excel(name = "纬度", width = 15)
    @ApiModelProperty(value = "纬度")
    private java.lang.String latitude;
	/**经度*/
	@Excel(name = "经度", width = 15)
    @ApiModelProperty(value = "经度")
    private java.lang.String longitude;
	/**不动产编号*/
	@Excel(name = "不动产编号", width = 15)
    @ApiModelProperty(value = "不动产编号")
    private java.lang.String bdcbh;
	/**提交人ID*/
	@Excel(name = "提交人ID", width = 15)
    @ApiModelProperty(value = "提交人ID")
    private java.lang.String tjrId;
	/**提交人zh*/
	@Excel(name = "提交人zh", width = 15)
    @ApiModelProperty(value = "提交人zh")
    private java.lang.String tjrZh;
	/**提交人*/
	@Excel(name = "提交人", width = 15)
    @ApiModelProperty(value = "提交人")
    private java.lang.String tjrName;
	/**提交人部门id*/
	@Excel(name = "提交人部门id", width = 15)
    @ApiModelProperty(value = "提交人部门id")
    private java.lang.String tjrOrgId;
	/**提交人部门code*/
	@Excel(name = "提交人部门code", width = 15)
    @ApiModelProperty(value = "提交人部门code")
    private java.lang.String tjrOrgCode;
	/**提交部门*/
	@Excel(name = "提交部门", width = 15)
    @ApiModelProperty(value = "提交部门")
    private java.lang.String tjrOrgName;
	/**审核人ID*/
	@Excel(name = "审核人ID", width = 15)
    @ApiModelProperty(value = "审核人ID")
    private java.lang.String shrId;
	/**审核人zh*/
	@Excel(name = "审核人zh", width = 15)
    @ApiModelProperty(value = "审核人zh")
    private java.lang.String shrZh;
	/**审核人*/
	@Excel(name = "审核人", width = 15)
    @ApiModelProperty(value = "审核人")
    private java.lang.String shrName;
	/**审核人部门id*/
	@Excel(name = "审核人部门id", width = 15)
    @ApiModelProperty(value = "审核人部门id")
    private java.lang.String shrOrgId;
	/**审核人部门code*/
	@Excel(name = "审核人部门code", width = 15)
    @ApiModelProperty(value = "审核人部门code")
    private java.lang.String shrOrgCode;
	/**提交状态*/
	@Excel(name = "提交状态", width = 15, dicCode = "tj_status")
	@Dict(dicCode = "tj_status")
    @ApiModelProperty(value = "提交状态")
    private java.lang.String tjzt;
	/**审核部门*/
	@Excel(name = "审核部门", width = 15)
    @ApiModelProperty(value = "审核部门")
    private java.lang.String shrOrgName;
	/**审核状态*/
	@Excel(name = "审核状态", width = 15, dicCode = "audit_status")
    @Dict(dicCode = "audit_status")
    @ApiModelProperty(value = "审核状态")
    private java.lang.String shzt;
	/**审核说明*/
	@Excel(name = "审核说明", width = 15)
    @ApiModelProperty(value = "审核说明")
    private java.lang.String shRemark;

    /**用户身份角色*/
    @TableField(exist = false)
    private java.lang.String userSf;
    /**数据标识*/
    @TableField(exist = false)
    private java.lang.String falg;
    /**民政地址部门*/
    @TableField(exist = false)
    private java.lang.String addressDepartnameMzDto;
    /**地址信息*/
    @TableField(exist = false)
    private java.lang.String addressNameDto;
}
