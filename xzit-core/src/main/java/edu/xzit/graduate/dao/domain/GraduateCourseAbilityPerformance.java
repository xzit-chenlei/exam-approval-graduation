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
 * 毕业达成度课程能力交叉(平时现)对象 graduate_course_ability_performance
 *
 * @author chenlei
 * @date 2025-09-21
 */
@Data
@TableName("graduate_course_ability_performance")
public class GraduateCourseAbilityPerformance extends BaseEntity{
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

        /** $column.columnComment */
                @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
        private BigDecimal level;

        /** 可选：权重/分值 */
                @Excel(name = "可选：权重/分值")
        private BigDecimal weight;

        /** 年级 */
                @Excel(name = "年级")
        private Integer grade;

        /** 逻辑删除 */
            @TableLogic
        private Long isValid;


}
