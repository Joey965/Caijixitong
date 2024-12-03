package org.jeecg.modules.demo.cjxt.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * @Description: 动态模板配置
 * @Author: jeecg-boot
 * @Date:   2024-06-06
 * @Version: V1.0
 */
@ApiModel(value="cjxt_mbgl_pz对象", description="动态模板配置")
@Data
@TableName("cjxt_mbgl_pz")
public class CjxtMbglPz implements Serializable {
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
    /**删除标识*/
//    @Excel(name = "删除标识", width = 15)
    @ApiModelProperty(value = "删除标识")
    @TableLogic
    private java.lang.String delFlag;
    /**字段名称*/
    @Excel(name = "字段名称", width = 15)
    @ApiModelProperty(value = "字段名称")
    private java.lang.String dbFieldName;
    /**字段备注*/
    @Excel(name = "字段备注", width = 15)
    @ApiModelProperty(value = "字段备注")
    private java.lang.String dbFieldTxt;
    /**字段类型*/
    @Excel(name = "字段类型", width = 15, dicCode = "zdlxtype")
    @ApiModelProperty(value = "字段类型")
    private java.lang.String dbType;
    /**字段长度*/
    @Excel(name = "字段长度", width = 15)
    @ApiModelProperty(value = "字段长度")
    private java.lang.String dbLength;
    /**是否主键*/
    @Excel(name = "是否主键", width = 15)
    @ApiModelProperty(value = "是否主键")
    private java.lang.String dbIsKey;
    /**控件类型*/
    @Excel(name = "控件类型", width = 15, dicCode = "kjlxtype")
    @ApiModelProperty(value = "控件类型")
    private java.lang.String fieldShowType;
    /**字典code*/
    @Excel(name = "字典code", width = 15)
    @ApiModelProperty(value = "字典code")
    private java.lang.String dictField;
    /**默认值*/
    @Excel(name = "默认值", width = 15)
    @ApiModelProperty(value = "默认值")
    private java.lang.String dbDefaultVal;
    /**是否查询*/
    @Excel(name = "是否查询", width = 15, dicCode = "yn")
    @ApiModelProperty(value = "是否查询")
    private java.lang.String isQuery;
    /**排序*/
    @Excel(name = "排序", width = 15)
    @ApiModelProperty(value = "排序")
    private java.lang.Integer orderNum;
    /**外键主表名*/
//    @Excel(name = "外键主表名", width = 15)
    @ApiModelProperty(value = "外键主表名")
    private java.lang.String mainTable;
    /**外键*/
    @ApiModelProperty(value = "外键")
    private java.lang.String mbglId;
    /**OCR识别*/
    @Excel(name = "OCR识别", width = 15)
    @ApiModelProperty(value = "OCR识别")
    private java.lang.String isOcr;
    /**是否必填*/
    @Excel(name = "是否必填", width = 15, dicCode = "yn")
    @ApiModelProperty(value = "是否必填")
    private java.lang.String dbIsNull;
    /**是否共通*/
//    @Excel(name = "是否共通", width = 15, dicCode = "yn")
    @ApiModelProperty(value = "是否共通")
    private java.lang.String isCommon;
    /**原字段名称*/
//    @Excel(name = "原字段名称", width = 15)
    @ApiModelProperty(value = "原字段名称")
    private java.lang.String dbFieldNameOld;
    /**模版管理编号*/
//    @Excel(name = "模版管理编号", width = 15)
    @ApiModelProperty(value = "模版管理编号")
    private java.lang.String mbglMbbh;
    /**表单是否显示*/
//    @Excel(name = "表单是否显示", width = 15)
    @ApiModelProperty(value = "表单是否显示")
    private java.lang.String isShowFrom;
    /**列表是否显示*/
//    @Excel(name = "列表是否显示", width = 15)
    @ApiModelProperty(value = "列表是否显示")
    private java.lang.String isShowList;
    /**是否标题*/
    @Excel(name = "是否标题", width = 15, dicCode = "yn")
    @ApiModelProperty(value = "是否标题")
    private java.lang.String isTitle;
    /**字段描述*/
//    @Excel(name = "字段描述", width = 15)
    @ApiModelProperty(value = "字段描述")
    private java.lang.String dbDescribe;
    /**校验类型*/
//    @Excel(name = "校验类型", width = 15)
    @ApiModelProperty(value = "校验类型")
    private java.lang.String dbJylx;
    /**页面属性*/
//    @Excel(name = "页面属性", width = 15, dicCode = "ymsx")
    @ApiModelProperty(value = "页面属性")
    private java.lang.String ymsx;
    /**父字段名称*/
//    @Excel(name = "父字段名称", width = 15)
    @ApiModelProperty(value = "父字段名称")
    private java.lang.String fname;
    /**是否父字段*/
//    @Excel(name = "是否父字段", width = 15, dicCode = "yn")
    @ApiModelProperty(value = "是否父字段")
    private java.lang.String isfather;
    /**字段值*/
//    @Excel(name = "字段值", width = 15)
    @ApiModelProperty(value = "字段值")
    private java.lang.String fvalue;
    /**原字段长度*/
//    @Excel(name = "原字段长度", width = 15)
    @ApiModelProperty(value = "原字段长度")
    private java.lang.String dbLengthOld;
    /**原字段类型*/
//    @Excel(name = "原字段类型", width = 15)
    @ApiModelProperty(value = "原字段类型")
    private java.lang.String dbTypeOld;
    /**是否加密*/
//    @Excel(name = "是否加密", width = 15)
    @ApiModelProperty(value = "是否加密")
    private java.lang.String sfjm;

    @ApiModelProperty(value = "APP是否查询")
    private java.lang.String appIsQuery;

    @ApiModelProperty(value = "APP查询排序")
    private java.lang.String appQueryOrder;

    /**列表展示字段*/
    @Excel(name = "列表展示字段", width = 15, dicCode = "yn")
    @ApiModelProperty(value = "列表展示字段")
    private java.lang.String lbxszd;

    /**详情列表展示字段*/
    @Excel(name = "详情列表展示字段", width = 15, dicCode = "yn")
    @ApiModelProperty(value = "详情列表展示字段")
    private java.lang.String xqlbxszd;

    /**字段修改前名称*/
    @TableField(exist = false)
    private java.lang.String dbFieldNameDto;
    /**字段长度修改值*/
    @TableField(exist = false)
    private java.lang.String dbLengthDto;
    /**字段类型修改值*/
    @TableField(exist = false)
    private java.lang.String dbTypeDto;
    /**临时存储数据库字段数据*/
    @TableField(exist = false)
    private java.lang.String dataValue;
    /**临时存储数据库字段脱敏数据*/
    @TableField(exist = false)
    private java.lang.String dataTmValue;
    /**字段校验类型Dto*/
    @TableField(exist = false)
    private java.lang.String dbJylxDto;

}
