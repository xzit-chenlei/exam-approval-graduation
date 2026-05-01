package edu.xzit.core.core.flowable.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.core.core.flowable.domain.ExamWordTemplateNew;

import java.util.List;

/**
 * 新word模板Service接口
 */
public interface IExamWordTemplateNewRepoService extends IService<ExamWordTemplateNew> {

    ExamWordTemplateNew selectExamWordTemplateNewById(Long id);

    List<ExamWordTemplateNew> selectExamWordTemplateNewList(ExamWordTemplateNew examWordTemplateNew);

    List<ExamWordTemplateNew> getByFlowableKeyAndColumn(String flowableKey, Integer column, Integer row);

    int insertExamWordTemplateNew(ExamWordTemplateNew examWordTemplateNew);

    int updateExamWordTemplateNew(ExamWordTemplateNew examWordTemplateNew);

    int deleteExamWordTemplateNewByIds(Long[] ids);

    int deleteExamWordTemplateNewById(Long id);
}









