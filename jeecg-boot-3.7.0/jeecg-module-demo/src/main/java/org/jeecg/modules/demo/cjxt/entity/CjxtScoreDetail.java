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
 * @Description: 积分明细表
 * @Author: jeecg-boot
 * @Date:   2024-06-17
 * @Version: V1.0
 */
@Data
@TableName("cjxt_score_detail")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_score_detail对象", description="积分明细表")
public class CjxtScoreDetail implements Serializable {
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
	/**用户ID*/
	@Excel(name = "用户ID", width = 15)
    @ApiModelProperty(value = "用户ID")
    private java.lang.String userId;
	/**用户名称*/
	@Excel(name = "用户名称", width = 15)
    @ApiModelProperty(value = "用户名称")
    private java.lang.String userName;
	/**规则ID*/
	@Excel(name = "规则ID", width = 15)
    @ApiModelProperty(value = "规则ID")
    private java.lang.String ruleId;
	/**规则名称*/
	@Excel(name = "规则名称", width = 15)
    @ApiModelProperty(value = "规则名称")
    private java.lang.String ruleName;
	/**积分数值*/
	@Excel(name = "积分数值", width = 15)
    @ApiModelProperty(value = "积分数值")
    private java.lang.String score;
	/**数据ID*/
	@Excel(name = "数据ID", width = 15)
    @ApiModelProperty(value = "数据ID")
    private java.lang.String dataId;
	/**模版ID*/
	@Excel(name = "模版ID", width = 15)
    @ApiModelProperty(value = "模版ID")
    private java.lang.String mbId;
	/**模版名称*/
	@Excel(name = "模版名称", width = 15)
    @ApiModelProperty(value = "模版名称")
    private java.lang.String mbName;

    /**积分总值*/
    @TableField(exist = false)
    private java.lang.String scoreCount;
}
