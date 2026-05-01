package edu.xzit.core.dao.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.core.dao.domain.ExamListenClassPlan;

/**
 * 听课计划Service接口
 *
 * @author chenlei
 * @date 2025-03-09
 */
public interface IExamListenClassPlanRepoService extends IService<ExamListenClassPlan> {
    /**
     * 查询听课计划
     *
     * @param id 听课计划主键
     * @return 听课计划
     */
        ExamListenClassPlan selectExamListenClassPlanById(Long id);

    /**
     * 查询听课计划列表
     *
     * @param examListenClassPlan 听课计划
     * @return 听课计划集合
     */
    List<ExamListenClassPlan> selectExamListenClassPlanList(ExamListenClassPlan examListenClassPlan);

    /**
     * 新增听课计划
     *
     * @param examListenClassPlan 听课计划
     * @return 结果
     */
    int insertExamListenClassPlan(ExamListenClassPlan examListenClassPlan);

    /**
     * 修改听课计划
     *
     * @param examListenClassPlan 听课计划
     * @return 结果
     */
    int updateExamListenClassPlan(ExamListenClassPlan examListenClassPlan);

    /**
     * 批量删除听课计划
     *
     * @param ids 需要删除的听课计划主键集合
     * @return 结果
     */
    int deleteExamListenClassPlanByIds(Long[] ids);

    /**
     * 删除听课计划信息
     *
     * @param id 听课计划主键
     * @return 结果
     */
    int deleteExamListenClassPlanById(Long id);
}
