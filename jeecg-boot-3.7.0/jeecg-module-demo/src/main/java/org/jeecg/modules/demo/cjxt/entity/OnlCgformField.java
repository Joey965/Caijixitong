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
 * @Description: 动态表单字段
 * @Author: jeecg-boot
 * @Date:   2024-06-04
 * @Version: V1.0
 */
@Data
@TableName("onl_cgform_field")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="onl_cgform_field对象", description="动态表单字段")
public class OnlCgformField implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**表ID*/
	@Excel(name = "表ID", width = 15)
    @ApiModelProperty(value = "表ID")
    private java.lang.String cgformHeadId;
	/**字段名字*/
	@Excel(name = "字段名字", width = 15)
    @ApiModelProperty(value = "字段名字")
    private java.lang.String dbFieldName;
	/**字段备注*/
	@Excel(name = "字段备注", width = 15)
    @ApiModelProperty(value = "字段备注")
    private java.lang.String dbFieldTxt;
	/**原字段名*/
	@Excel(name = "原字段名", width = 15)
    @ApiModelProperty(value = "原字段名")
    private java.lang.String dbFieldNameOld;
	/**是否主键 0否 1是*/
	@Excel(name = "是否主键 0否 1是", width = 15)
    @ApiModelProperty(value = "是否主键 0否 1是")
    private java.lang.String dbIsKey;
	/**是否允许为空0否1是*/
	@Excel(name = "是否允许为空0否1是", width = 15)
    @ApiModelProperty(value = "是否允许为空0否1是")
    private java.lang.String dbIsNull;
	/**是否需要同步数据库字段， 1是0否*/
	@Excel(name = "是否需要同步数据库字段， 1是0否", width = 15)
    @ApiModelProperty(value = "是否需要同步数据库字段， 1是0否")
    private java.lang.String dbIsPersist;
	/**数据库字段类型*/
	@Excel(name = "数据库字段类型", width = 15)
    @ApiModelProperty(value = "数据库字段类型")
    private java.lang.String dbType;
	/**数据库字段长度*/
	@Excel(name = "数据库字段长度", width = 15)
    @ApiModelProperty(value = "数据库字段长度")
    private java.lang.String dbLength;
	/**小数点*/
	@Excel(name = "小数点", width = 15)
    @ApiModelProperty(value = "小数点")
    private java.lang.String dbPointLength;
	/**表字段默认值*/
	@Excel(name = "表字段默认值", width = 15)
    @ApiModelProperty(value = "表字段默认值")
    private java.lang.String dbDefaultVal;
	/**字典code*/
	@Excel(name = "字典code", width = 15)
    @ApiModelProperty(value = "字典code")
    private java.lang.String dictField;
	/**字典表*/
	@Excel(name = "字典表", width = 15)
    @ApiModelProperty(value = "字典表")
    private java.lang.String dictTable;
	/**字典Text*/
	@Excel(name = "字典Text", width = 15)
    @ApiModelProperty(value = "字典Text")
    private java.lang.String dictText;
	/**表单控件类型*/
	@Excel(name = "表单控件类型", width = 15)
    @ApiModelProperty(value = "表单控件类型")
    private java.lang.String fieldShowType;
	/**跳转URL*/
	@Excel(name = "跳转URL", width = 15)
    @ApiModelProperty(value = "跳转URL")
    private java.lang.String fieldHref;
	/**表单控件长度*/
	@Excel(name = "表单控件长度", width = 15)
    @ApiModelProperty(value = "表单控件长度")
    private java.lang.String fieldLength;
	/**查询模式*/
	@Excel(name = "查询模式", width = 15)
    @ApiModelProperty(value = "查询模式")
    private java.lang.String queryMode;
	/**表单字段校验规则*/
	@Excel(name = "表单字段校验规则", width = 15)
    @ApiModelProperty(value = "表单字段校验规则")
    private java.lang.String fieldValidType;
	/**字段是否必填*/
	@Excel(name = "字段是否必填", width = 15)
    @ApiModelProperty(value = "字段是否必填")
    private java.lang.String fieldMustInput;
    /**扩展参数JSON*/
    @Excel(name = "扩展参数JSON", width = 15)
    @ApiModelProperty(value = "扩展参数JSON")
    private java.lang.String fieldExtendJson;
	/**控件默认值*/
	@Excel(name = "控件默认值", width = 15)
    @ApiModelProperty(value = "控件默认值")
    private java.lang.String fieldDefaultValue;
	/**是否查询条件0否 1是*/
	@Excel(name = "是否查询条件0否 1是", width = 15)
    @ApiModelProperty(value = "是否查询条件0否 1是")
    private java.lang.String isQuery;
	/**表单是否显示0否 1是*/
	@Excel(name = "表单是否显示0否 1是", width = 15)
    @ApiModelProperty(value = "表单是否显示0否 1是")
    private java.lang.String isShowForm;
	/**列表是否显示0否 1是*/
	@Excel(name = "列表是否显示0否 1是", width = 15)
    @ApiModelProperty(value = "列表是否显示0否 1是")
    private java.lang.String isShowList;
	/**是否是只读（1是 0否）*/
	@Excel(name = "是否是只读（1是 0否）", width = 15)
    @ApiModelProperty(value = "是否是只读（1是 0否）")
    private java.lang.String isReadOnly;
	/**外键主表名*/
	@Excel(name = "外键主表名", width = 15)
    @ApiModelProperty(value = "外键主表名")
    private java.lang.String mainTable;
	/**外键主键字段*/
	@Excel(name = "外键主键字段", width = 15)
    @ApiModelProperty(value = "外键主键字段")
    private java.lang.String mainField;
	/**排序*/
	@Excel(name = "排序", width = 15)
    @ApiModelProperty(value = "排序")
    private java.lang.String orderNum;
	/**自定义值转换器*/
	@Excel(name = "自定义值转换器", width = 15)
    @ApiModelProperty(value = "自定义值转换器")
    private java.lang.String converter;
	/**查询默认值*/
	@Excel(name = "查询默认值", width = 15)
    @ApiModelProperty(value = "查询默认值")
    private java.lang.String queryDefVal;
	/**查询配置字典text*/
	@Excel(name = "查询配置字典text", width = 15)
    @ApiModelProperty(value = "查询配置字典text")
    private java.lang.String queryDictText;
	/**查询配置字典code*/
	@Excel(name = "查询配置字典code", width = 15)
    @ApiModelProperty(value = "查询配置字典code")
    private java.lang.String queryDictField;
	/**查询配置字典table*/
	@Excel(name = "查询配置字典table", width = 15)
    @ApiModelProperty(value = "查询配置字典table")
    private java.lang.String queryDictTable;
	/**查询显示控件*/
	@Excel(name = "查询显示控件", width = 15)
    @ApiModelProperty(value = "查询显示控件")
    private java.lang.String queryShowType;
	/**是否启用查询配置1是0否*/
	@Excel(name = "是否启用查询配置1是0否", width = 15)
    @ApiModelProperty(value = "是否启用查询配置1是0否")
    private java.lang.String queryConfigFlag;
	/**查询字段校验类型*/
	@Excel(name = "查询字段校验类型", width = 15)
    @ApiModelProperty(value = "查询字段校验类型")
    private java.lang.String queryValidType;
	/**查询字段是否必填1是0否*/
	@Excel(name = "查询字段是否必填1是0否", width = 15)
    @ApiModelProperty(value = "查询字段是否必填1是0否")
    private java.lang.String queryMustInput;
	/**是否支持排序1是0否*/
	@Excel(name = "是否支持排序1是0否", width = 15)
    @ApiModelProperty(value = "是否支持排序1是0否")
    private java.lang.String sortFlag;
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
}
