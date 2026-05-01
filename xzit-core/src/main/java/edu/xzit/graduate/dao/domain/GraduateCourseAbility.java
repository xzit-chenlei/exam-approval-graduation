package edu.xzit.graduate.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程×能力交叉表 graduate_course_ability
 */
@Data
@TableName("graduate_course_ability")
public class GraduateCourseAbility extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 课程ID */
    @Excel(name = "课程ID")
    private Long courseId;

    /** 能力ID */
    @Excel(name = "能力ID")
    private Long abilityId;

    /** 等级：H/M/L */
    @Excel(name = "等级")
    private BigDecimal level;

    /** 权重/分值，可为空 */
    @Excel(name = "权重")
    private java.math.BigDecimal weight;

    /** 备注（持久化字段，覆盖 BaseEntity 中的非持久化 remark） */
    @Excel(name = "备注")
    private String remark;

    /** 年级（25届/26届...） */
    @Excel(name = "年级")
    private Integer grade;

    /** 逻辑删除 */
    @TableLogic
    private Long isValid;
}

