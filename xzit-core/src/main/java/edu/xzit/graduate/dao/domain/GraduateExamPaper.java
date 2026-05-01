package edu.xzit.graduate.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 毕业达成度试卷信息表 graduate_exam_paper
 *
 * @author system
 * @date 2025-01-22
 */
@Data
@TableName("graduate_exam_paper")
public class GraduateExamPaper extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 所属专业ID */
    @Excel(name = "所属专业ID")
    private Long majorId;

    /** 所属课程ID（可选） */
    @Excel(name = "所属课程ID")
    private Long courseId;

    /** 试卷名称 */
    @Excel(name = "试卷名称")
    private String paperName;

    /** 试卷种类，1 表示期末试卷、2 表示平时测验、3 表示实验 */
    @Excel(name = "试卷种类，1 表示期末试卷、2 表示平时测验、3 表示实验")
    private Long typeId;

    /** 试卷编号 */
    @Excel(name = "试卷编号")
    private String paperCode;

    /** 考试类型：MIDTERM(期中), FINAL(期末), QUIZ(测验), OTHER(其他) */
    @Excel(name = "考试类型")
    private String examType;

    /** 考试日期 */
    @Excel(name = "考试日期", dateFormat = "yyyy-MM-dd")
    private LocalDate examDate;

    /** 试卷总分 */
    @Excel(name = "试卷总分")
    private BigDecimal totalScore;

    /** 考试时长（分钟） */
    @Excel(name = "考试时长")
    private Integer examDuration;

    /** 显示排序 */
    @Excel(name = "排序")
    private Integer orderNo;

    /** 年级 */
    private Integer grade;

    /** 学期（改为字符串以支持自定义输入，如 "上/下/秋季"） */
    private String semester;

    /** 备注（持久化字段，覆盖 BaseEntity 中的非持久化 remark） */
    @Excel(name = "备注")
    private String remark;

    /** 逻辑删除 */
    @TableLogic
    private Long isValid;
}