package edu.xzit.core.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.core.dao.domain.ExamForm;

import java.util.List;

/**
 * 考试表单Service接口
 *
 * @author
 * @date 2025-11-09
 */
public interface IExamFormRepoService extends IService<ExamForm> {
    /**
     * 查询考试表单
     *
     * @param id 考试表单主键
     * @return 考试表单
     */
    ExamForm selectExamFormById(Long id);

    /**
     * 查询考试表单列表
     *
     * @param examForm 考试表单
     * @return 考试表单集合
     */
    List<ExamForm> selectExamFormList(ExamForm examForm);

    /**
     * 新增考试表单
     *
     * @param examForm 考试表单
     * @return 结果
     */
    int insertExamForm(ExamForm examForm);

    /**
     * 修改考试表单
     *
     * @param examForm 考试表单
     * @return 结果
     */
    int updateExamForm(ExamForm examForm);

    /**
     * 批量删除考试表单
     *
     * @param ids 需要删除的考试表单主键集合
     * @return 结果
     */
    int deleteExamFormByIds(Long[] ids);

    /**
     * 删除考试表单信息
     *
     * @param id 考试表单主键
     * @return 结果
     */
    int deleteExamFormById(Long id);
}

