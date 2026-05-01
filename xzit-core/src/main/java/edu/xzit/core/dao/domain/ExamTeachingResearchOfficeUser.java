package edu.xzit.core.dao.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 教研室与用户关联表 exam_teaching_research_office_user
 */
@Data
@TableName("exam_teaching_research_office_user")
public class ExamTeachingResearchOfficeUser {
    @TableId
    private Long teachingResearchOfficeId;
    private Long userId;
    private Date createTime;
}
