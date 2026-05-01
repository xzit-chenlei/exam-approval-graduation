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
 * 学生信息表 graduate_student_info
 *
 * @author chenlei
 * @date 2025-01-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("graduate_student_info")
public class GraduateStudentInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 学号 */
    @Excel(name = "学号")
    private Long studentCode;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String studentName;

    /** 班级ID */
    @Excel(name = "班级ID")
    private Long classId;

    /** 班级名称 */
    @Excel(name = "班级名称")
    private String className;

    /** 备注（持久化字段，覆盖 BaseEntity 中的非持久化 remark） */
    @Excel(name = "备注")
    private String remark;

    /** 逻辑删除 */
    @TableLogic
    private Long isValid;
}
