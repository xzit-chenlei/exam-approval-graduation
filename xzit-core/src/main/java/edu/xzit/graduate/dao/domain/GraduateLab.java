package edu.xzit.graduate.dao.domain;

    import java.math.BigDecimal;
    import java.util.Date;
    import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 毕业达成度实验成绩信息对象 graduate_lab
 *
 * @author vikTor
 * @date 2025-09-16
 */
@Data
@TableName("graduate_lab")
public class GraduateLab extends BaseEntity {
        private static final long serialVersionUID = 1L;

        /** 主键 */
            @TableId(value = "id", type = IdType.AUTO)
        private Long id;

        /** 所属专业ID */
                @Excel(name = "所属专业ID")
        private Long majorId;

        /** 所属课程ID（可选） */
                @Excel(name = "所属课程ID", readConverterExp = "可=选")
        private Long courseId;

        /** 实验名称 */
                @Excel(name = "实验名称")
        private String labName;

        /** 实验编号 */
                @Excel(name = "实验编号")
        private String labCode;

        /** 实验截止日期 */
                @JsonFormat(pattern = "yyyy-MM-dd")
                @Excel(name = "实验截止日期", width = 30, dateFormat = "yyyy-MM-dd")
        private Date deadline;

        /** 实验操作总分 */
                @Excel(name = "实验操作总分")
        private BigDecimal opsScore;

        /** 实验报告总分 */
                @Excel(name = "实验报告总分")
        private BigDecimal rptScore;

        /** 年级 */
                @Excel(name = "年级")
        private String grade;

        /** 学期 */
                @Excel(name = "学期")
        private String semester;

        /** 显示排序 */
                @Excel(name = "显示排序")
        private Long orderNo;

        /** 逻辑删除 */
            @TableLogic
        private Long isValid;


}
