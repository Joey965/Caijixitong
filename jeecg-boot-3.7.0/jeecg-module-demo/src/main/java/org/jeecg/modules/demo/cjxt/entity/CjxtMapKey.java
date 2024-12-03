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
 * @Description: 地图应用KEY
 * @Author: jeecg-boot
 * @Date:   2024-11-05
 * @Version: V1.0
 */
@Data
@TableName("cjxt_map_key")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_map_key对象", description="地图应用KEY")
public class CjxtMapKey implements Serializable {
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
	/**地图名称*/
	@Excel(name = "地图名称", width = 15)
    @ApiModelProperty(value = "地图名称")
    private java.lang.String mapName;
	/**key名称*/
	@Excel(name = "key名称", width = 15)
    @ApiModelProperty(value = "key名称")
    private java.lang.String keyName;
	/**web端key*/
	@Excel(name = "web端key", width = 15)
    @ApiModelProperty(value = "web端key")
    private java.lang.String keyWeb;
	/**web端密钥*/
	@Excel(name = "web端密钥", width = 15)
    @ApiModelProperty(value = "web端密钥")
    private java.lang.String keyWebMy;
	/**web服务key*/
	@Excel(name = "web服务key", width = 15)
    @ApiModelProperty(value = "web服务key")
    private java.lang.String keyWebService;
	/**key启用状态*/
	@Excel(name = "key启用状态", width = 15, dicCode = "yn")
	@Dict(dicCode = "yn")
    @ApiModelProperty(value = "key启用状态")
    private java.lang.String keyStatus;
	/**排序*/
	@Excel(name = "排序", width = 15)
    @ApiModelProperty(value = "排序")
    private java.lang.Integer keyNum;
	/**是否启用*/
	@Excel(name = "是否启用", width = 15, dicCode = "yn")
	@Dict(dicCode = "yn")
    @ApiModelProperty(value = "是否启用")
    private java.lang.String sfqy;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private java.lang.String bz;
    /**key名称*/
    @TableField(exist = false)
    private java.lang.String keyNameDto;
    /**web端*/
    @TableField(exist = false)
    private java.lang.String keyWebDto;
    /**web端密钥*/
    @TableField(exist = false)
    private java.lang.String keyWebMyDto;
    /**web服务*/
    @TableField(exist = false)
    private java.lang.String keyWebServiceDto;
}
