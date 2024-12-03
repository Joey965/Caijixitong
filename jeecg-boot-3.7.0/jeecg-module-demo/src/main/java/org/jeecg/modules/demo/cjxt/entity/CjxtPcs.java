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
 * @Description: 派出所基本信息
 * @Author: jeecg-boot
 * @Date:   2024-08-07
 * @Version: V1.0
 */
@Data
@TableName("cjxt_pcs")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_pcs对象", description="派出所基本信息")
public class CjxtPcs implements Serializable {
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
	/**作废标识 */
	@Excel(name = "作废标识 ", width = 15)
    @ApiModelProperty(value = "作废标识 ")
    @TableLogic
    private java.lang.Integer delFlag;
	/**部门id*/
	@Excel(name = "部门id", width = 15)
    @ApiModelProperty(value = "部门id")
    private java.lang.String orgId;
	/**部门code*/
	@Excel(name = "部门code", width = 15)
    @ApiModelProperty(value = "部门code")
    private java.lang.String orgCode;
	/**部门名称*/
	@Excel(name = "部门名称", width = 15)
    @ApiModelProperty(value = "部门名称")
    private java.lang.String orgName;
	/**社区民警*/
	@Excel(name = "社区民警", width = 15)
    @ApiModelProperty(value = "社区民警")
    private java.lang.String sqmj;
	/**联系方式*/
	@Excel(name = "联系方式", width = 15)
    @ApiModelProperty(value = "联系方式")
    private java.lang.String lxfs;
	/**户籍电话*/
	@Excel(name = "户籍电话", width = 15)
    @ApiModelProperty(value = "户籍电话")
    private java.lang.String hjdh;
	/**派出所地址*/
	@Excel(name = "派出所地址", width = 15)
    @ApiModelProperty(value = "派出所地址")
    private java.lang.String pcsdz;
	/**便民信息*/
	@Excel(name = "便民信息", width = 15)
    @ApiModelProperty(value = "便民信息")
    private java.lang.String bmxx;
	/**社区民警图片*/
	@Excel(name = "社区民警图片", width = 15)
    @ApiModelProperty(value = "社区民警图片")
    private java.lang.String sqmjTp;
	/**联系微信号*/
	@Excel(name = "联系微信号", width = 15)
    @ApiModelProperty(value = "联系微信号")
    private java.lang.String lxwxh;
}
