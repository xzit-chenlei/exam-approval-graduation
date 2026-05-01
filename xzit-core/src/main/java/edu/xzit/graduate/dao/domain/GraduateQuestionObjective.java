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
 * 毕业达成度题目与课程目标关联表 graduate_question_objective
 *
 * @author system
 * @date 2025-01-22
 */
@Data
@TableName("graduate_question_objective")
public class GraduateQuestionObjective extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 题目ID */
    @Excel(name = "题目ID")
    private Long questionId;

    /** 课程目标ID */
    @Excel(name = "课程目标ID")
    private Long objectiveId;

    /** 权重系数（该题目对此目标的贡献度） */
    @Excel(name = "权重系数")
    private BigDecimal weight;

    /** 分值比例（该目标在此题目中占的分值百分比） */
    @Excel(name = "分值比例")
    private BigDecimal scoreRatio;

    /** 评价方式 */
    @Excel(name = "评价方式")
    private String assessmentMethod;

    /** 备注（持久化字段，覆盖 BaseEntity 中的非持久化 remark） */
    @Excel(name = "备注")
    private String remark;

    /** 逻辑删除 */
    @TableLogic
    private Long isValid;
}