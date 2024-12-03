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
 * @Description: 轮播图
 * @Author: jeecg-boot
 * @Date:   2024-06-12
 * @Version: V1.0
 */
@Data
@TableName("cjxt_lbt")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_lbt对象", description="轮播图")
public class CjxtLbt implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键ID*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;
	/**所属部门*/
    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
	/**修改人*/
    @ApiModelProperty(value = "修改人")
    private String updateBy;
	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
	/**作废标识 (1表示删除 0表示未删除)*/
	@Excel(name = "作废标识 (1表示删除 0表示未删除)", width = 15)
    @ApiModelProperty(value = "作废标识 (1表示删除 0表示未删除)")
    @TableLogic
    private Integer delFlag;
	/**轮播图标题*/
	@Excel(name = "轮播图标题", width = 15)
    @ApiModelProperty(value = "轮播图标题")
    private String title;
	/**轮播图图片路径*/
	@Excel(name = "轮播图图片路径", width = 15)
    @ApiModelProperty(value = "轮播图图片路径")
    private String imagePath;
	/**显示顺序*/
	@Excel(name = "显示顺序", width = 15)
    @ApiModelProperty(value = "显示顺序")
    private Integer displayOrder;
	/**发布状态*/
	@Excel(name = "发布状态", width = 15)
    @ApiModelProperty(value = "发布状态")
    private String fbzt;
}
