package edu.xzit.core.dao.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.xzit.core.dao.domain.ExamTutorialStatistic;

import java.util.List;


/**
 * 辅导答疑统计Mapper接口
 *
 * @author chenlei
 * @date 2025-03-09
 */
public interface ExamTutorialStatisticMapper extends BaseMapper<ExamTutorialStatistic> {
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
     * 删除辅导答疑统计
     *
     * @param id 辅导答疑统计主键
     * @return 结果
     */
    int deleteExamTutorialStatisticById(Long id);

    /**
     * 批量删除辅导答疑统计
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteExamTutorialStatisticByIds(Long[] ids);
}
