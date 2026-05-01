package edu.xzit.core.dao.service.impl;

import java.util.Arrays;
import java.util.List;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.core.common.utils.DateUtils;
import edu.xzit.core.dao.domain.ExamListenClassPlan;
import edu.xzit.core.dao.mapper.ExamListenClassPlanMapper;
import edu.xzit.core.dao.service.IExamListenClassPlanRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 听课计划Service业务层处理
 *
 * @author chenlei
 * @date 2025-03-09
 */
@Service
public class ExamListenClassPlanRepoServiceImpl extends ServiceImpl<ExamListenClassPlanMapper, ExamListenClassPlan> implements IExamListenClassPlanRepoService {
    @Autowired
    private ExamListenClassPlanMapper examListenClassPlanMapper;

    /**
     * 查询听课计划
     *
     * @param id 听课计划主键
     * @return 听课计划
     */
    @Override
    public ExamListenClassPlan selectExamListenClassPlanById(Long id) {
        return getById(id);
    }

    /**
     * 查询听课计划列表
     *
     * @param examListenClassPlan 听课计划
     * @return 听课计划
     */
    @Override
    public List<ExamListenClassPlan> selectExamListenClassPlanList(ExamListenClassPlan examListenClassPlan) {
        return list(new LambdaUpdateWrapper<>(examListenClassPlan));
    }

    /**
     * 新增听课计划
     *
     * @param examListenClassPlan 听课计划
     * @return 结果
     */
    @Override
    public int insertExamListenClassPlan(ExamListenClassPlan examListenClassPlan) {
        return save(examListenClassPlan) ? 1 : 0;
    }

    /**
     * 修改听课计划
     *
     * @param examListenClassPlan 听课计划
     * @return 结果
     */
    @Override
    public int updateExamListenClassPlan(ExamListenClassPlan examListenClassPlan) {
        return updateById(examListenClassPlan) ? 1 : 0;
    }

    /**
     * 批量删除听课计划
     *
     * @param ids 需要删除的听课计划主键
     * @return 结果
     */
    @Override
    public int deleteExamListenClassPlanByIds(Long[] ids) {

        if (ArrayUtil.isEmpty(ids)) {
            return 0;
        }

        LambdaQueryWrapper<ExamListenClassPlan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ExamListenClassPlan::getId, Arrays.asList(ids));
        return remove(queryWrapper) ? 1 : 0;

    }

    /**
     * 删除听课计划信息
     *
     * @param id 听课计划主键
     * @return 结果
     */
    @Override
    public int deleteExamListenClassPlanById(Long id) {
        return examListenClassPlanMapper.deleteExamListenClassPlanById(id);
    }
}
