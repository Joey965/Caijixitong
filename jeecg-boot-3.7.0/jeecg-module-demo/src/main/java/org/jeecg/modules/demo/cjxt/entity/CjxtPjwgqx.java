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
 * @Description: 人员网格权限
 * @Author: jeecg-boot
 * @Date:   2024-07-22
 * @Version: V1.0
 */
@Data
@TableName("cjxt_pjwgqx")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cjxt_pjwgqx对象", description="人员网格权限")
public class CjxtPjwgqx implements Serializable {
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
    /**作废标识 (1表示删除 0表示未删除)*/
    @Excel(name = "作废标识 (1表示删除 0表示未删除)", width = 15)
    @ApiModelProperty(value = "作废标识 (1表示删除 0表示未删除)")
    @TableLogic
    private java.lang.Integer delFlag;
    /**网格名称*/
    @Excel(name = "网格名称", width = 15)
    @ApiModelProperty(value = "网格名称")
    private java.lang.String wgName;
    /**网格ID*/
    @Excel(name = "网格ID", width = 15)
    @ApiModelProperty(value = "网格ID")
    private java.lang.String wgId;
    /**网格编码*/
    @Excel(name = "网格编码", width = 15)
    @ApiModelProperty(value = "网格编码")
    private java.lang.String wgCode;
    /**人员id*/
    @Excel(name = "人员id", width = 15)
    @ApiModelProperty(value = "人员id")
    private java.lang.String pjId;
    /**人员账号*/
    @Excel(name = "人员账号", width = 15)
    @ApiModelProperty(value = "人员账号")
    private java.lang.String pjZh;
    /**人员姓名*/
    @Excel(name = "人员姓名", width = 15)
    @ApiModelProperty(value = "人员姓名")
    private java.lang.String pjName;
    /**人员部门*/
    @Excel(name = "人员部门", width = 15)
    @ApiModelProperty(value = "人员部门")
    private java.lang.String pjDepart;
    /**联系电话*/
    @Excel(name = "联系电话", width = 15)
    @ApiModelProperty(value = "联系电话")
    private java.lang.String pjLxdh;
    /**排序*/
    @Excel(name = "排序", width = 15)
    @ApiModelProperty(value = "排序")
    private java.lang.Integer departOrder;
}
