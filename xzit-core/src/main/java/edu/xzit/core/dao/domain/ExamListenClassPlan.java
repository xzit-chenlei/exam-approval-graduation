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
 * 听课计划对象 exam_listen_class_plan
 *
 * @author chenlei
 * @date 2025-03-09
 */
@Data
@TableName("exam_listen_class_plan")
public class ExamListenClassPlan extends BaseEntity{
private static final long serialVersionUID = 1L;

        /** $column.columnComment */
            @TableId(value = "id", type = IdType.AUTO)
        private Long id;

        /** 听课教师 */
                @Excel(name = "听课教师")
        private String listeningTeacher;

        /** 被听课教师 */
                @Excel(name = "被听课教师")
        private String listenedTeacher;

        /** 课程 */
                @Excel(name = "课程")
        private String course;

        /** 班级 */
                @Excel(name = "班级")
        private String listenClass;

        /** 听课时间 */
                @JsonFormat(pattern = "yyyy-MM-dd")
                @Excel(name = "听课时间", width = 30, dateFormat = "yyyy-MM-dd")
        private Date listenTime;

        /** 听课地点 */
                @Excel(name = "听课地点")
        private String listenLocation;

        @Excel(name = "备注")
        private String remark;

        /**
         * 逻辑删除
         */
        @TableLogic
        private Long isValid;


}
