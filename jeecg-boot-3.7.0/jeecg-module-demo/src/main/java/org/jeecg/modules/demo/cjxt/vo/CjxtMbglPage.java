package org.jeecg.modules.demo.cjxt.vo;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.jeecg.modules.demo.cjxt.entity.CjxtMbgl;
import org.jeecg.modules.demo.cjxt.entity.CjxtMbglPz;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelEntity;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description: 模板管理
 * @Author: jeecg-boot
 * @Date:   2024-06-05
 * @Version: V1.0
 */
@Data
@ApiModel(value="cjxt_mbglPage对象", description="模板管理")
public class CjxtMbglPage {

	/**主键*/
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
	/**作废标识 (1表示删除 0表示未删除)*/
	@Excel(name = "作废标识 (1表示删除 0表示未删除)", width = 15)
	@ApiModelProperty(value = "作废标识 (1表示删除 0表示未删除)")
	@TableLogic
	private Integer delFlag;
	/**表名*/
	@Excel(name = "表名", width = 15)
	@ApiModelProperty(value = "表名")
	private java.lang.String bm;
	/**模板名称*/
	@Excel(name = "模板名称", width = 15)
	@ApiModelProperty(value = "模板名称")
	private java.lang.String mbname;
	/**模版类型*/
	@Excel(name = "模版类型", width = 15, dicCode = "jxcs")
	@Dict(dicCode = "jxcs")
	@ApiModelProperty(value = "模版类型")
	private java.lang.String mblx;
	/**模板说明*/
	@Excel(name = "模板说明", width = 15)
	@ApiModelProperty(value = "模板说明")
	private java.lang.String mbsm;
	/**表单风格*/
//	@Excel(name = "表单风格", width = 15, dicCode = "bdfg")
	@Dict(dicCode = "bdfg")
	@ApiModelProperty(value = "表单风格")
	private java.lang.String bdfg;
	/**模板排序号*/
	@Excel(name = "模板排序号", width = 15)
	@ApiModelProperty(value = "模板排序号")
	private java.lang.Integer mbsort;
	/**表类型*/
	@Excel(name = "表类型", width = 15, dicCode = "blx")
	@Dict(dicCode = "blx")
	@ApiModelProperty(value = "表类型")
	private java.lang.String tableType;
	/**映射关系*/
//	@Excel(name = "映射关系", width = 15, dicCode = "ysgx")
	@Dict(dicCode = "ysgx")
	@ApiModelProperty(value = "映射关系")
	private java.lang.String relationType;
	/**附表排序序号*/
//	@Excel(name = "附表排序序号", width = 15)
	@ApiModelProperty(value = "附表排序序号")
	private java.lang.String tabOrderNum;
	/**附表*/
//	@Excel(name = "附表", width = 15)
	@ApiModelProperty(value = "附表")
	private java.lang.String subTableStr;
	/**是否同步数据库*/
//	@Excel(name = "是否同步数据库", width = 15)
	@ApiModelProperty(value = "是否同步数据库")
	private java.lang.String isDb;
	/**模版字段数*/
	//    @Excel(name = "模版字段数", width = 15)
	@ApiModelProperty(value = "模版字段数")
	private java.lang.String zdnum;
	/**模版编号*/
//	@Excel(name = "模版编号", width = 15)
	@ApiModelProperty(value = "模版编号")
	private java.lang.String mbbh;
	/**是否上报*/
//	@Excel(name = "是否上报", width = 15)
	@ApiModelProperty(value = "是否上报")
	private java.lang.String sfsb;
	/**是否历史*/
//	@Excel(name = "是否历史", width = 15)
	@ApiModelProperty(value = "是否历史")
	private java.lang.String sfls;
	/**是否创建*/
//	@Excel(name = "是否创建", width = 15, dicCode = "yn")
//	@Dict(dicCode = "yn")
	@ApiModelProperty(value = "是否创建")
    @TableField(exist = false)
	private java.lang.String sfcj;
	/**唯一字段*/
	@Excel(name = "唯一字段", width = 15)
	@ApiModelProperty(value = "唯一字段")
	private java.lang.String dbOnly;
	@ExcelCollection(name="动态模板配置")
	@ApiModelProperty(value = "动态模板配置")
	private List<CjxtMbglPz> cjxtMbglPzList;

	@TableField(exist = false)
	private String bmDto;
}
