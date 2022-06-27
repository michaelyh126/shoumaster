package org.jeecg.modules.demo.student_records.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * @Description: 学生出入记录表
 * @Author: jeecg-boot
 * @Date:   2022-05-02
 * @Version: V1.0
 */
@Data
@TableName("student_records")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="student_records对象", description="学生出入记录表")
public class StudentRecords implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
	/**创建日期*/
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
	/**更新日期*/
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;
	/**所属部门*/
    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
	/**自增主键*/
	@Excel(name = "自增主键", width = 15)
    @ApiModelProperty(value = "自增主键")
    private Integer aid;
	/**唯一识别id*/
	@Excel(name = "唯一识别id", width = 15)
    @ApiModelProperty(value = "唯一识别id")
    private String openid;
	/**姓名*/
	@Excel(name = "姓名", width = 15)
    @ApiModelProperty(value = "姓名")
    private String name;
	/**学号*/
	@Excel(name = "学号", width = 15)
    @ApiModelProperty(value = "学号")
    private String no;
	/**手机号*/
	@Excel(name = "手机号", width = 15)
    @ApiModelProperty(value = "手机号")
    private String phone;
	/**小区*/
	@Excel(name = "小区", width = 15)
    @ApiModelProperty(value = "小区")
    private String community;
	/**状态*/
	@Excel(name = "状态", width = 15, dictTable = "gun_in_out", dicText = "gun_in_out", dicCode = "id")
	@Dict(dictTable = "gun_in_out", dicText = "gun_in_out", dicCode = "id")
    @ApiModelProperty(value = "状态")
    private String state;
	/**签到时间*/
	@Excel(name = "签到时间", width = 15)
    @ApiModelProperty(value = "签到时间")
    private Date signTime;
	/**异常*/
	@Excel(name = "异常", width = 15)
    @ApiModelProperty(value = "异常")
    private String isException;

	private Date returnTime;
	private String longitude;
	private String latitude;
	private String distance;
}
