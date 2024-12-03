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
 * @Description: 动态表单
 * @Author: jeecg-boot
 * @Date:   2024-06-04
 * @Version: V1.0
 */
@Data
@TableName("onl_cgform_head")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="onl_cgform_head对象", description="动态表单")
public class OnlCgformHead implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**表名*/
	@Excel(name = "表名", width = 15)
    @ApiModelProperty(value = "表名")
    private java.lang.String tableName;
	/**表类型*/
	@Excel(name = "表类型", width = 15)
    @ApiModelProperty(value = "表类型")
    private java.lang.Integer tableType;
	/**表版本*/
	@Excel(name = "表版本", width = 15)
    @ApiModelProperty(value = "表版本")
    private java.lang.String tableVersion;
	/**表说明*/
	@Excel(name = "表说明", width = 15)
    @ApiModelProperty(value = "表说明")
    private java.lang.String tableTxt;
	/**是否带checkbox*/
	@Excel(name = "是否带checkbox", width = 15)
    @ApiModelProperty(value = "是否带checkbox")
    private java.lang.String isCheckbox;
	/**同步数据库状态*/
	@Excel(name = "同步数据库状态", width = 15)
    @ApiModelProperty(value = "同步数据库状态")
    private java.lang.String isDbSynch;
	/**是否分页*/
	@Excel(name = "是否分页", width = 15)
    @ApiModelProperty(value = "是否分页")
    private java.lang.String isPage;
	/**是否是树*/
	@Excel(name = "是否是树", width = 15)
    @ApiModelProperty(value = "是否是树")
    private java.lang.String isTree;
	/**主键生成序列*/
	@Excel(name = "主键生成序列", width = 15)
    @ApiModelProperty(value = "主键生成序列")
    private java.lang.String idSequence;
	/**主键类型*/
	@Excel(name = "主键类型", width = 15)
    @ApiModelProperty(value = "主键类型")
    private java.lang.String idType;
	/**查询模式*/
	@Excel(name = "查询模式", width = 15)
    @ApiModelProperty(value = "查询模式")
    private java.lang.String queryMode;
	/**映射关系*/
	@Excel(name = "映射关系", width = 15)
    @ApiModelProperty(value = "映射关系")
    private java.lang.String relationType;
	/**子表*/
	@Excel(name = "子表", width = 15)
    @ApiModelProperty(value = "子表")
    private java.lang.String subTableStr;
	/**附表排序序号*/
	@Excel(name = "附表排序序号", width = 15)
    @ApiModelProperty(value = "附表排序序号")
    private java.lang.String tabOrderNum;
	/**树形表单父id*/
	@Excel(name = "树形表单父id", width = 15)
    @ApiModelProperty(value = "树形表单父id")
    private java.lang.String treeParentIdField;
	/**树表主键字段*/
	@Excel(name = "树表主键字段", width = 15)
    @ApiModelProperty(value = "树表主键字段")
    private java.lang.String treeIdField;
    /**树表主键字段*/
    @Excel(name = "树开表单列字段", width = 15)
    @ApiModelProperty(value = "树开表单列字段")
    private java.lang.String treeFieldname;
	/**表单分类*/
	@Excel(name = "表单分类", width = 15)
    @ApiModelProperty(value = "表单分类")
    private java.lang.String formCategory;
	/**PC表单模板*/
	@Excel(name = "PC表单模板", width = 15)
    @ApiModelProperty(value = "PC表单模板")
    private java.lang.String formTemplate;
	/**表单模板样式(移动端)*/
	@Excel(name = "表单模板样式(移动端)", width = 15)
    @ApiModelProperty(value = "表单模板样式(移动端)")
    private java.lang.String formTemplateMobile;
	/**是否有横向滚动条*/
	@Excel(name = "是否有横向滚动条", width = 15)
    @ApiModelProperty(value = "是否有横向滚动条")
    private java.lang.String scroll;
	/**复制版本号*/
	@Excel(name = "复制版本号", width = 15)
    @ApiModelProperty(value = "复制版本号")
    private java.lang.String copyVersion;
	/**复制表类型1为复制表 0为原始表*/
	@Excel(name = "复制表类型1为复制表 0为原始表", width = 15)
    @ApiModelProperty(value = "复制表类型1为复制表 0为原始表")
    private java.lang.String copyType;
	/**原始表ID*/
	@Excel(name = "原始表ID", width = 15)
    @ApiModelProperty(value = "原始表ID")
    private java.lang.String physicId;
	/**扩展JSON*/
	@Excel(name = "扩展JSON", width = 15)
    @ApiModelProperty(value = "扩展JSON")
    private java.lang.String extConfigJson;
	/**主题模板*/
	@Excel(name = "主题模板", width = 15)
    @ApiModelProperty(value = "主题模板")
    private java.lang.String themeTemplate;
	/**是否用设计器表单*/
	@Excel(name = "是否用设计器表单", width = 15)
    @ApiModelProperty(value = "是否用设计器表单")
    private java.lang.String isDesForm;
	/**设计器表单编码*/
	@Excel(name = "设计器表单编码", width = 15)
    @ApiModelProperty(value = "设计器表单编码")
    private java.lang.String desFormCode;
	/**关联的应用ID*/
	@Excel(name = "关联的应用ID", width = 15)
    @ApiModelProperty(value = "关联的应用ID")
    private java.lang.String lowAppId;
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
