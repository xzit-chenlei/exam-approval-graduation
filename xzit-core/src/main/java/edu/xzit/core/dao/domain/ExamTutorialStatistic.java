package edu.xzit.core.dao.domain;

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
 * 辅导答疑统计对象 exam_tutorial_statistic
 *
 * @author chenlei
 * @date 2025-03-09
 */
@Data
@TableName("exam_tutorial_statistic")
public class ExamTutorialStatistic extends BaseEntity{
private static final long serialVersionUID = 1L;

        /**  */
            @TableId(value = "id", type = IdType.AUTO)
        private Long id;

        /** 老师 */
                @Excel(name = "老师")
        private String teacher;

        /** 课程 */
                @Excel(name = "课程")
        private String course;

        /** 辅导时间 */
                @JsonFormat(pattern = "yyyy-MM-dd")
                @Excel(name = "辅导时间", width = 30, dateFormat = "yyyy-MM-dd")
        private Date tutorialTime;

        /** 辅导地点 */
                @Excel(name = "辅导地点")
        private String tutorialLocation;

        /** 班级 */
                @Excel(name = "班级")
        private String tutorialClass;

        @Excel(name = "备注")
        private String remark;

        /**
         * 逻辑删除
         */
        @TableLogic
        private Long isValid;


}
