package edu.xzit.graduate.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 学生成绩表 graduate_student_score
 *
 * @author chenlei
 * @date 2025-01-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("graduate_student_score")
public class GraduateStudentScore extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 试卷ID */
    @Excel(name = "试卷ID")
    private Long paperId;

    /** 学生ID */
    @Excel(name = "学生ID")
    private Long studentId;

    /** 学号 */
    @Excel(name = "学号")
    private Long studentCode;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String studentName;

    /** 题目ID */
    @Excel(name = "题目ID")
    private Long questionId;

    /** 题目编号 */
    @Excel(name = "题目编号")
    private String questionNo;

    /** 题目分值 */
    @Excel(name = "题目分值")
    private BigDecimal questionScore;

    /** 学生得分 */
    @Excel(name = "学生得分")
    private BigDecimal studentScore;

    /** 得分率 */
    @Excel(name = "得分率")
    private BigDecimal scoreRate;

    /** 备注（持久化字段，覆盖 BaseEntity 中的非持久化 remark） */
    @Excel(name = "备注")
    private String remark;

    /** 逻辑删除 */
    @TableLogic
    private Long isValid;
}

