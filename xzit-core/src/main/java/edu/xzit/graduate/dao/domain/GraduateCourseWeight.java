package edu.xzit.graduate.dao.domain;

    import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 毕业达成度各课程三类考试权重对象 graduate_course_weight
 *
 * @author chenlei
 * @date 2025-09-30
 */
@Data
@TableName("graduate_course_weight")
public class GraduateCourseWeight extends BaseEntity{
private static final long serialVersionUID = 1L;

        /** 主键 */
            @TableId(value = "id", type = IdType.AUTO)
        private Long id;

        /** 所属课程ID（可选） */
                @Excel(name = "所属课程ID", readConverterExp = "可=选")
        private Long courseId;

        /** 所属试卷ID */
                @Excel(name = "所属试卷ID")
        private Long paperId;

        /** $column.columnComment */
                @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
        private BigDecimal finalW;

        /** $column.columnComment */
                @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
        private BigDecimal perfW;

        /** $column.columnComment */
                @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
        private BigDecimal labW;

        /** 年级 */
                @Excel(name = "年级")
        private String grade;

        /** 学期 */
                @Excel(name = "学期")
        private String semester;

        /** 逻辑删除 */
            @TableLogic
        private Long isValid;


}
