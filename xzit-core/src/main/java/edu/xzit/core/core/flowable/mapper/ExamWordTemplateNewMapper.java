package edu.xzit.core.core.flowable.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.xzit.core.core.flowable.domain.ExamWordTemplateNew;

import java.util.List;

/**
 * 新word模板Mapper
 */
public interface ExamWordTemplateNewMapper extends BaseMapper<ExamWordTemplateNew> {

    ExamWordTemplateNew selectExamWordTemplateNewById(Long id);

    List<ExamWordTemplateNew> selectExamWordTemplateNewList(ExamWordTemplateNew examWordTemplateNew);

    int insertExamWordTemplateNew(ExamWordTemplateNew examWordTemplateNew);

    int updateExamWordTemplateNew(ExamWordTemplateNew examWordTemplateNew);

    int deleteExamWordTemplateNewById(Long id);

    int deleteExamWordTemplateNewByIds(Long[] ids);
}









