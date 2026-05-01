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
 * 毕业达成度试卷题目表 graduate_exam_question
 *
 * @author system
 * @date 2025-01-22
 */
@Data
@TableName("graduate_exam_question")
public class GraduateExamQuestion extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 所属试卷ID */
    @Excel(name = "所属试卷ID")
    private Long paperId;

    /** 题目编号（如：1、2、3 或 A、B、C） */
    @Excel(name = "题目编号")
    private String questionNo;

    /** 题目类型：OBJECTIVE(客观题), SUBJECTIVE(主观题), CALCULATE(计算题), DESIGN(设计题) */
    @Excel(name = "题目类型")
    private String questionType;

    /** 考试目的/考查内容 */
    @Excel(name = "考试目的")
    private String examPurpose;

    /** 题目内容（可选） */
    @Excel(name = "题目内容")
    private String questionContent;

    /** 分值 */
    @Excel(name = "分值")
    private BigDecimal score;

    /** 难度等级：EASY(简单), MEDIUM(中等), HARD(困难) */
    @Excel(name = "难度等级")
    private String difficulty;

    /** 关联的能力ID（新） */
    @Excel(name = "能力ID")
    private Long abilityId;

    /** 关联的课程目标ID（历史字段，兼容保留） */
    @Excel(name = "课程目标ID")
    private Long objectiveId;

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