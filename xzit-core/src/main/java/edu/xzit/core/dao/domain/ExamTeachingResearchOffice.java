package edu.xzit.core.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 教研室实体 exam_teaching_research_office
 */
@Data
@TableName("exam_teaching_research_office")
public class ExamTeachingResearchOffice extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String name;
    private String remark;
    @TableLogic
    private Integer isValid;
}
