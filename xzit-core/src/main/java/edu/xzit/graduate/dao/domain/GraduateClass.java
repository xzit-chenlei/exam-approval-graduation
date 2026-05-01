package edu.xzit.graduate.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 毕业达成度班级表 graduate_class
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("graduate_class")
public class GraduateClass extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 班级名称 */
    @Excel(name = "班级名称")
    private String className;

    /** 毕业达成度专业表ID */
    @Excel(name = "专业ID")
    private Long graduateMajorId;

    /** 专业名称 */
    @Excel(name = "专业名称")
    private String graduateMajorName;

    /** 入学年级（YEAR类型，使用数字年份） */
    @Excel(name = "入学年级")
    private Integer enteringClass;

    /** 备注（持久化字段，覆盖 BaseEntity 中的非持久化 remark） */
    @Excel(name = "备注")
    private String remark;

    /** 逻辑删除 */
    @TableLogic
    private Long isValid;
}


