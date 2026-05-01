package edu.xzit.core.core.flowable.service.impl;

import edu.xzit.core.core.flowable.service.IExamDeployFormService;
import edu.xzit.core.dao.domain.ExamDeployForm;
import edu.xzit.core.dao.domain.ExamForm;
import edu.xzit.core.dao.mapper.ExamDeployFormMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 考试流程实例关联表单Service业务层处理
 *
 * @author Tony
 * @date 2021-04-03
 */
@Service
public class ExamDeployFormServiceImpl implements IExamDeployFormService {
    @Autowired
    private ExamDeployFormMapper examDeployFormMapper;

    /**
     * 查询考试流程实例关联表单
     *
     * @param id 考试流程实例关联表单ID
     * @return 考试流程实例关联表单
     */
    @Override
    public ExamDeployForm selectExamDeployFormById(Long id) {
        return examDeployFormMapper.selectExamDeployFormById(id);
    }

    /**
     * 查询考试流程实例关联表单列表
     *
     * @param examDeployForm 考试流程实例关联表单
     * @return 考试流程实例关联表单
     */
    @Override
    public List<ExamDeployForm> selectExamDeployFormList(ExamDeployForm examDeployForm) {
        return examDeployFormMapper.selectExamDeployFormList(examDeployForm);
    }

    /**
     * 新增 / 覆盖考试流程实例关联表单
     *
     * 业务含义：
     * - 同一个流程定义（deployId）只允许绑定一个自定义工单
     * - 如果该流程之前已经绑定过工单，则本次应视为“覆盖绑定”，更新原有记录
     *
     * @param examDeployForm 考试流程实例关联表单
     * @return 结果
     */
    @Override
    public int insertExamDeployForm(ExamDeployForm examDeployForm) {
        // 先按 deployId 查询是否已存在绑定记录
        ExamDeployForm query = new ExamDeployForm();
        query.setDeployId(examDeployForm.getDeployId());
        java.util.List<ExamDeployForm> existList = examDeployFormMapper.selectExamDeployFormList(query);

        if (existList == null || existList.isEmpty()) {
            // 未绑定过，正常新增
            return examDeployFormMapper.insertExamDeployForm(examDeployForm);
        } else {
            // 已有绑定，执行覆盖：更新原记录的 formId
            ExamDeployForm exist = existList.get(0);
            examDeployForm.setId(exist.getId());
            return examDeployFormMapper.updateExamDeployForm(examDeployForm);
        }
    }

    /**
     * 修改考试流程实例关联表单
     *
     * @param examDeployForm 考试流程实例关联表单
     * @return 结果
     */
    @Override
    public int updateExamDeployForm(ExamDeployForm examDeployForm) {
        return examDeployFormMapper.updateExamDeployForm(examDeployForm);
    }

    /**
     * 批量删除考试流程实例关联表单
     *
     * @param ids 需要删除的考试流程实例关联表单ID
     * @return 结果
     */
    @Override
    public int deleteExamDeployFormByIds(Long[] ids) {
        return examDeployFormMapper.deleteExamDeployFormByIds(ids);
    }

    /**
     * 删除考试流程实例关联表单信息
     *
     * @param id 考试流程实例关联表单ID
     * @return 结果
     */
    @Override
    public int deleteExamDeployFormById(Long id) {
        return examDeployFormMapper.deleteExamDeployFormById(id);
    }

    /**
     * 查询流程挂着的表单
     *
     * @param deployId
     * @return
     */
    @Override
    public ExamForm selectExamDeployFormByDeployId(String deployId) {
        return examDeployFormMapper.selectExamDeployFormByDeployId(deployId);
    }
}
















