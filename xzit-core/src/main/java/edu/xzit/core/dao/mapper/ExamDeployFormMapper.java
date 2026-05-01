package edu.xzit.core.dao.mapper;


import edu.xzit.core.dao.domain.ExamDeployForm;
import edu.xzit.core.dao.domain.ExamForm;

import java.util.List;

/**
 * 考试流程实例关联表单Mapper接口
 * 
 * @author Tony
 * @date 2021-03-30
 */
public interface ExamDeployFormMapper 
{
    /**
     * 查询考试流程实例关联表单
     * 
     * @param id 考试流程实例关联表单ID
     * @return 考试流程实例关联表单
     */
    public ExamDeployForm selectExamDeployFormById(Long id);

    /**
     * 查询考试流程实例关联表单列表
     * 
     * @param ExamDeployForm 考试流程实例关联表单
     * @return 考试流程实例关联表单集合
     */
    public List<ExamDeployForm> selectExamDeployFormList(ExamDeployForm ExamDeployForm);

    /**
     * 新增考试流程实例关联表单
     * 
     * @param ExamDeployForm 考试流程实例关联表单
     * @return 结果
     */
    public int insertExamDeployForm(ExamDeployForm ExamDeployForm);

    /**
     * 修改考试流程实例关联表单
     * 
     * @param ExamDeployForm 考试流程实例关联表单
     * @return 结果
     */
    public int updateExamDeployForm(ExamDeployForm ExamDeployForm);

    /**
     * 删除考试流程实例关联表单
     * 
     * @param id 考试流程实例关联表单ID
     * @return 结果
     */
    public int deleteExamDeployFormById(Long id);

    /**
     * 批量删除考试流程实例关联表单
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteExamDeployFormByIds(Long[] ids);



    /**
     * 查询流程挂着的表单
     * @param deployId
     * @return
     */
    ExamForm selectExamDeployFormByDeployId(String deployId);
}

