package edu.xzit.core.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.core.dao.domain.ExamTaskTeachingResearchOffice;

public interface IExamTaskTeachingResearchOfficeService extends IService<ExamTaskTeachingResearchOffice> {
    /** 根据教研室ID集合查询任务ID集合 */
    java.util.List<String> selectTaskIdsByOfficeIds(java.util.List<Long> officeIds);
}


