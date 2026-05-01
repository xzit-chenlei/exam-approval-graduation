package edu.xzit.core.dao.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 任务和教研室关联
 */
@TableName("exam_task_teaching_research_office")
public class ExamTaskTeachingResearchOffice {

    /** 流程实例id/任务id */
    @TableField("task_id")
    private String taskId;

    /** 教研室ID */
    @TableField("teaching_research_office_id")
    private Long teachingResearchOfficeId;

    /** 教研室名称 */
    @TableField("teaching_research_office_name")
    private String teachingResearchOfficeName;

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public Long getTeachingResearchOfficeId() { return teachingResearchOfficeId; }
    public void setTeachingResearchOfficeId(Long teachingResearchOfficeId) { this.teachingResearchOfficeId = teachingResearchOfficeId; }

    public String getTeachingResearchOfficeName() { return teachingResearchOfficeName; }
    public void setTeachingResearchOfficeName(String teachingResearchOfficeName) { this.teachingResearchOfficeName = teachingResearchOfficeName; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("taskId", getTaskId())
                .append("teachingResearchOfficeId", getTeachingResearchOfficeId())
                .append("teachingResearchOfficeName", getTeachingResearchOfficeName())
                .toString();
    }
}


