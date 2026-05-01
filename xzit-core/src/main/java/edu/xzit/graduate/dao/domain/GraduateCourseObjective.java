package edu.xzit.graduate.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 课程目标表 graduate_course_objective
 *
 * @author chenlei
 * @date 2025-08-25
 */
@Data
@TableName("graduate_course_objective")
public class GraduateCourseObjective extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 课程ID */
    @Excel(name = "课程ID")
    private Long courseId;

    /** 毕业达成度能力ID */
    @Excel(name = "毕业达成度能力ID")
    private Long abilityId;

    /** 毕业达成度能力名称 */
    @Excel(name = "毕业达成度能力名称")
    private String abilityName;

    /** 课程名称（非持久化字段，用于显示） */
    @Excel(name = "课程名称")
    @TableField(exist = false)
    private String courseName;

    /** 专业名称（非持久化字段，用于显示） */
    @Excel(name = "专业名称")
    @TableField(exist = false)
    private String majorName;

    /** 专业ID（非持久化字段，用于查询过滤） */
    @TableField(exist = false)
    private Long majorId;

    /** 课程目标 */
    @Excel(name = "课程目标")
    private String objective;

    /** 显示排序 */
    @Excel(name = "排序")
    private Integer orderNo;

    /** 备注（持久化字段，覆盖 BaseEntity 中的非持久化 remark） */
    @Excel(name = "备注")
    private String remark;

    /** 逻辑删除 */
    @TableLogic
    private Long isValid;
} 