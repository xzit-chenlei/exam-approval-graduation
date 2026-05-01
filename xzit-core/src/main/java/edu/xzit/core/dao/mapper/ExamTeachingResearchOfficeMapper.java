package edu.xzit.core.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.xzit.core.dao.domain.ExamTeachingResearchOffice;
import java.util.List;

public interface ExamTeachingResearchOfficeMapper extends BaseMapper<ExamTeachingResearchOffice> {
    List<ExamTeachingResearchOffice> selectExamTeachingResearchOfficeList(ExamTeachingResearchOffice query);
    int insertExamTeachingResearchOffice(ExamTeachingResearchOffice data);
    int updateExamTeachingResearchOffice(ExamTeachingResearchOffice data);
    int deleteExamTeachingResearchOfficeById(Long id);
    int deleteExamTeachingResearchOfficeByIds(Long[] ids);
    List<ExamTeachingResearchOffice> selectByUserId(Long userId);
}
