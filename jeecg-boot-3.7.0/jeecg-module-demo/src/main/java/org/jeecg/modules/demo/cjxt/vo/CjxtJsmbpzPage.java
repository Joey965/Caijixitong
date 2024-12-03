package org.jeecg.modules.demo.cjxt.vo;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import org.jeecg.modules.demo.cjxt.entity.CjxtJsmbpz;
import org.jeecg.modules.demo.cjxt.entity.CjxtJsmbpzDtl;
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
 * @Description: 角色模版配置
 * @Author: jeecg-boot
 * @Date:   2024-07-24
 * @Version: V1.0
 */
@Data
@ApiModel(value="cjxt_jsmbpzPage对象", description="角色模版配置")
public class CjxtJsmbpzPage {

	/**主键ID*/
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
	/**作废标识 (1表示删除 0表示未删除)*/
	@Excel(name = "作废标识 (1表示删除 0表示未删除)", width = 15)
	@ApiModelProperty(value = "作废标识 (1表示删除 0表示未删除)")
    private java.lang.Integer delFlag;
	/**角色编码*/
	@Excel(name = "角色编码", width = 15)
	@ApiModelProperty(value = "角色编码")
    private java.lang.String roleCode;
	/**角色名称*/
	@Excel(name = "角色名称", width = 15)
	@ApiModelProperty(value = "角色名称")
    private java.lang.String roleName;
	/**模版ID*/
	@Excel(name = "模版ID", width = 15)
	@ApiModelProperty(value = "模版ID")
    private java.lang.String mbId;
	/**模版编号*/
	@Excel(name = "模版编号", width = 15)
	@ApiModelProperty(value = "模版编号")
    private java.lang.String mbbh;
	/**模版名称*/
	@Excel(name = "模版名称", width = 15)
	@ApiModelProperty(value = "模版名称")
    private java.lang.String mbname;

	@ExcelCollection(name="角色模版配置子表")
	@ApiModelProperty(value = "角色模版配置子表")
	private List<CjxtJsmbpzDtl> cjxtJsmbpzDtlList;


	@TableField(exist = false)
	private java.lang.String mbIdDto;

	@TableField(exist = false)
	private java.lang.String roleCodeDto;

}
