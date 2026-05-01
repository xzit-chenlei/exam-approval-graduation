package edu.xzit.core.dao.service.impl;

import java.util.Arrays;
import java.util.List;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.core.common.utils.DateUtils;
import edu.xzit.core.dao.domain.ExamTutorialStatistic;
import edu.xzit.core.dao.mapper.ExamTutorialStatisticMapper;
import edu.xzit.core.dao.service.IExamTutorialStatisticRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 辅导答疑统计Service业务层处理
 *
 * @author chenlei
 * @date 2025-03-09
 */
@Service
public class ExamTutorialStatisticRepoServiceImpl extends ServiceImpl<ExamTutorialStatisticMapper, ExamTutorialStatistic> implements IExamTutorialStatisticRepoService {
    @Autowired
    private ExamTutorialStatisticMapper examTutorialStatisticMapper;

    /**
     * 查询辅导答疑统计
     *
     * @param id 辅导答疑统计主键
     * @return 辅导答疑统计
     */
    @Override
    public ExamTutorialStatistic selectExamTutorialStatisticById(Long id) {
        return examTutorialStatisticMapper.selectExamTutorialStatisticById(id);
    }

    /**
     * 查询辅导答疑统计列表
     *
     * @param examTutorialStatistic 辅导答疑统计
     * @return 辅导答疑统计
     */
    @Override
    public List<ExamTutorialStatistic> selectExamTutorialStatisticList(ExamTutorialStatistic examTutorialStatistic) {
        return examTutorialStatisticMapper.selectExamTutorialStatisticList(examTutorialStatistic);
    }

    /**
     * 新增辅导答疑统计
     *
     * @param examTutorialStatistic 辅导答疑统计
     * @return 结果
     */
    @Override
    public int insertExamTutorialStatistic(ExamTutorialStatistic examTutorialStatistic) {
        return save(examTutorialStatistic) ? 1 : 0;
    }

    /**
     * 修改辅导答疑统计
     *
     * @param examTutorialStatistic 辅导答疑统计
     * @return 结果
     */
    @Override
    public int updateExamTutorialStatistic(ExamTutorialStatistic examTutorialStatistic) {
        return updateById(examTutorialStatistic) ? 1 : 0;
    }

    /**
     * 批量删除辅导答疑统计
     *
     * @param ids 需要删除的辅导答疑统计主键
     * @return 结果
     */
    @Override
    public int deleteExamTutorialStatisticByIds(Long[] ids) {

        if (ArrayUtil.isEmpty(ids)) {
            return 0;
        }

        LambdaQueryWrapper<ExamTutorialStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ExamTutorialStatistic::getId, Arrays.asList(ids));
        return remove(queryWrapper) ? 1 : 0;

    }

    /**
     * 删除辅导答疑统计信息
     *
     * @param id 辅导答疑统计主键
     * @return 结果
     */
    @Override
    public int deleteExamTutorialStatisticById(Long id) {
        return examTutorialStatisticMapper.deleteExamTutorialStatisticById(id);
    }
}
