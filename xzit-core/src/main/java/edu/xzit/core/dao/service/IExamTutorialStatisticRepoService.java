package edu.xzit.core.dao.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.core.dao.domain.ExamTutorialStatistic;

/**
 * 辅导答疑统计Service接口
 *
 * @author chenlei
 * @date 2025-03-09
 */
public interface IExamTutorialStatisticRepoService extends IService<ExamTutorialStatistic> {
    /**
     * 查询辅导答疑统计
     *
     * @param id 辅导答疑统计主键
     * @return 辅导答疑统计
     */
        ExamTutorialStatistic selectExamTutorialStatisticById(Long id);

    /**
     * 查询辅导答疑统计列表
     *
     * @param examTutorialStatistic 辅导答疑统计
     * @return 辅导答疑统计集合
     */
    List<ExamTutorialStatistic> selectExamTutorialStatisticList(ExamTutorialStatistic examTutorialStatistic);

    /**
     * 新增辅导答疑统计
     *
     * @param examTutorialStatistic 辅导答疑统计
     * @return 结果
     */
    int insertExamTutorialStatistic(ExamTutorialStatistic examTutorialStatistic);

    /**
     * 修改辅导答疑统计
     *
     * @param examTutorialStatistic 辅导答疑统计
     * @return 结果
     */
    int updateExamTutorialStatistic(ExamTutorialStatistic examTutorialStatistic);

    /**
     * 批量删除辅导答疑统计
     *
     * @param ids 需要删除的辅导答疑统计主键集合
     * @return 结果
     */
    int deleteExamTutorialStatisticByIds(Long[] ids);

    /**
     * 删除辅导答疑统计信息
     *
     * @param id 辅导答疑统计主键
     * @return 结果
     */
    int deleteExamTutorialStatisticById(Long id);
}
