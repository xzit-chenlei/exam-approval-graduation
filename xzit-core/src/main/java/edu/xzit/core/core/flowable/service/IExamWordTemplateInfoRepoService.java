package edu.xzit.core.core.flowable.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.core.core.flowable.domain.ExamWordTemplateInfo;

/**
 * word模板信息Service接口
 *
 * @author chenlei
 * @date 2025-03-09
 */
public interface IExamWordTemplateInfoRepoService extends IService<ExamWordTemplateInfo> {
    /**
     * 查询word模板信息
     *
     * @param id word模板信息主键
     * @return word模板信息
     */
        ExamWordTemplateInfo selectExamWordTemplateInfoById(Long id);

    /**
     * 查询word模板信息列表
     *
     * @param examWordTemplateInfo word模板信息
     * @return word模板信息集合
     */
    List<ExamWordTemplateInfo> selectExamWordTemplateInfoList(ExamWordTemplateInfo examWordTemplateInfo);

    /**
     * 新增word模板信息
     *
     * @param examWordTemplateInfo word模板信息
     * @return 结果
     */
    int insertExamWordTemplateInfo(ExamWordTemplateInfo examWordTemplateInfo);

    /**
     * 修改word模板信息
     *
     * @param examWordTemplateInfo word模板信息
     * @return 结果
     */
    int updateExamWordTemplateInfo(ExamWordTemplateInfo examWordTemplateInfo);

    /**
     * 批量删除word模板信息
     *
     * @param ids 需要删除的word模板信息主键集合
     * @return 结果
     */
    int deleteExamWordTemplateInfoByIds(Long[] ids);

    /**
     * 删除word模板信息信息
     *
     * @param id word模板信息主键
     * @return 结果
     */
    int deleteExamWordTemplateInfoById(Long id);
}
