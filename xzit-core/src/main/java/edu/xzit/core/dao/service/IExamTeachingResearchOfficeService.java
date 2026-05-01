package edu.xzit.core.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.core.core.common.core.domain.entity.SysUser;
import edu.xzit.core.dao.domain.ExamTeachingResearchOffice;

import java.util.List;

public interface IExamTeachingResearchOfficeService extends IService<ExamTeachingResearchOffice> {
    List<ExamTeachingResearchOffice> selectExamTeachingResearchOfficeList(ExamTeachingResearchOffice query);
    int insertExamTeachingResearchOffice(ExamTeachingResearchOffice data);
    int updateExamTeachingResearchOffice(ExamTeachingResearchOffice data);
    int deleteExamTeachingResearchOfficeById(Long id);
    int deleteExamTeachingResearchOfficeByIds(Long[] ids);

    // 用户关联
    List<SysUser> selectUsersByOfficeId(Long officeId);
    int addUsers(Long officeId, List<Long> userIds);
    int removeUsers(Long officeId, List<Long> userIds);

    List<ExamTeachingResearchOffice> selectByUserId(Long userId);
}
