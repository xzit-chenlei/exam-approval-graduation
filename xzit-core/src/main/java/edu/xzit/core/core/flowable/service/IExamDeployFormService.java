package edu.xzit.core.core.flowable.service;


import edu.xzit.core.dao.domain.ExamDeployForm;
import edu.xzit.core.dao.domain.ExamForm;

import java.util.List;

/**
 * 考试流程实例关联表单Service接口
 *
 * @author Tony
 * @date 2021-04-03
 */
public interface IExamDeployFormService {
    /**
     * 查询考试流程实例关联表单
     *
     * @param id 考试流程实例关联表单ID
     * @return 考试流程实例关联表单
     */
    ExamDeployForm selectExamDeployFormById(Long id);

    /**
     * 查询考试流程实例关联表单列表
     *
     * @param examDeployForm 考试流程实例关联表单
     * @return 考试流程实例关联表单集合
     */
    List<ExamDeployForm> selectExamDeployFormList(ExamDeployForm examDeployForm);

    /**
     * 新增考试流程实例关联表单
     *
     * @param examDeployForm 考试流程实例关联表单
     * @return 结果
     */
    int insertExamDeployForm(ExamDeployForm examDeployForm);

    /**
     * 修改考试流程实例关联表单
     *
     * @param examDeployForm 考试流程实例关联表单
     * @return 结果
     */
    int updateExamDeployForm(ExamDeployForm examDeployForm);

    /**
     * 批量删除考试流程实例关联表单
     *
     * @param ids 需要删除的考试流程实例关联表单ID
     * @return 结果
     */
    int deleteExamDeployFormByIds(Long[] ids);

    /**
     * 删除考试流程实例关联表单信息
     *
     * @param id 考试流程实例关联表单ID
     * @return 结果
     */
    int deleteExamDeployFormById(Long id);

    /**
     * 查询流程挂着的表单
     *
     * @param deployId
     * @return
     */
    ExamForm selectExamDeployFormByDeployId(String deployId);
}

