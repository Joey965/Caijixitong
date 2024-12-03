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
 * @Description: 专项采集配置
 * @Author: jeecg-boot
 * @Date:   2024-09-11
 * @Version: V1.0
 */
@Data
@TableName("cjxt_zxcjpz")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_zxcjpz对象", description="专项采集配置")
public class CjxtZxcjpz implements Serializable {
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
	/**作废标识 (1表示删除 0表示未删除)*/
	@Excel(name = "作废标识 (1表示删除 0表示未删除)", width = 15)
    @ApiModelProperty(value = "作废标识 (1表示删除 0表示未删除)")
    @TableLogic
    private java.lang.Integer delFlag;
	/**模版ID*/
	@Excel(name = "模版ID", width = 15)
    @ApiModelProperty(value = "模版ID")
    private java.lang.String mbid;
	/**模版名称*/
	@Excel(name = "模版名称", width = 15)
    @ApiModelProperty(value = "模版名称")
    private java.lang.String mbmc;
	/**模版编号*/
	@Excel(name = "模版编号", width = 15)
    @ApiModelProperty(value = "模版编号")
    private java.lang.String mbCode;
	/**显示图标*/
	@Excel(name = "显示图标", width = 15)
    @ApiModelProperty(value = "显示图标")
    private java.lang.String iconPath;
	/**功能名称*/
	@Excel(name = "功能名称", width = 15)
    @ApiModelProperty(value = "功能名称")
    private java.lang.String gnmc;
	/**是否启用*/
	@Excel(name = "是否启用", width = 15, dicCode = "yn")
	@Dict(dicCode = "yn")
    @ApiModelProperty(value = "是否启用")
    private java.lang.String sfqy;
	/**模版排序*/
	@Excel(name = "模版排序", width = 15)
    @ApiModelProperty(value = "模版排序")
    private java.lang.String mbSort;

    /**模版ID*/
    @TableField(exist = false)
    private java.lang.String mbidDto;
}
