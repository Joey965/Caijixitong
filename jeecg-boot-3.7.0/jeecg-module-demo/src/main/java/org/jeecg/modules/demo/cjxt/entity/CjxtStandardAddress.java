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
 * @Description: 标准地址表
 * @Author: jeecg-boot
 * @Date:   2024-06-13
 * @Version: V1.0
 */
@Data
@TableName("cjxt_standard_address")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_standard_address对象", description="标准地址表")
public class CjxtStandardAddress implements Serializable {
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
    /**详细地址*/
    @Excel(name = "详细地址", width = 15)
    @ApiModelProperty(value = "详细地址")
    private java.lang.String detailAddress;

    /**民政地址id*/
    @ApiModelProperty(value = "民政地址id")
    private java.lang.String addressIdMz;
    /**民政地址编码*/
    @ApiModelProperty(value = "民政地址编码")
    private java.lang.String addressCodeMz;
    /**民政地址名称*/
    @Excel(name = "民政地址名称", width = 100)
    @ApiModelProperty(value = "民政地址名称")
    private java.lang.String addressNameMz;
    /**民政地址部门*/
    @Excel(name = "民政地址部门", width = 15)
    @ApiModelProperty(value = "民政地址部门")
    private java.lang.String addressDepartnameMz;
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
    /**经度*/
    @Excel(name = "经度", width = 15)
    @ApiModelProperty(value = "经度")
    private java.lang.String longitude;
    /**纬度*/
    @Excel(name = "纬度", width = 15)
    @ApiModelProperty(value = "纬度")
    private java.lang.String latitude;
    /**不动产编号*/
    @Excel(name = "不动产编号", width = 15)
    @ApiModelProperty(value = "不动产编号")
    private java.lang.String bdcbh;
    /**从业人员——二维码图片*/
    @ApiModelProperty(value = "单位从业人员——二维码图片")
    private java.lang.String cyryQrcode;
    /**地址门牌——二维码图片*/
    @ApiModelProperty(value = "地址门牌——二维码图片")
    private java.lang.String addressMpQrcode;

    @ApiModelProperty(value = "地址二维码")
    private java.lang.String taskAddressQrcode;

    @TableField(exist = false)
    private java.lang.String addressCodeDto;
    @TableField(exist = false)
    private java.lang.String mcText;
    @TableField(exist = false)
    private java.lang.String mcKey;
    /**民政地址部门*/
    @TableField(exist = false)
    private java.lang.String addressDepartnameMzDto;

    @TableField(exist = false)
    private java.lang.String dzmbCodeDto;
}
