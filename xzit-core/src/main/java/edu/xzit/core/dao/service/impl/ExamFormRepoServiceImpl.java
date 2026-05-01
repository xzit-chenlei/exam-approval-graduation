package edu.xzit.core.dao.service.impl;

import java.util.List;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.dao.domain.ExamForm;
import edu.xzit.core.dao.mapper.ExamFormMapper;
import edu.xzit.core.dao.service.IExamFormRepoService;
import org.springframework.stereotype.Service;

/**
 * 考试表单Service业务层处理
 *
 * @author
 * @date 2025-11-09
 */
@Service
public class ExamFormRepoServiceImpl extends ServiceImpl<ExamFormMapper, ExamForm> implements IExamFormRepoService {

    /**
     * 查询考试表单
     *
     * @param id 考试表单主键
     * @return 考试表单
     */
    @Override
    public ExamForm selectExamFormById(Long id) {
        return getById(id);
    }

    /**
     * 查询考试表单列表
     *
     * @param examForm 考试表单
     * @return 考试表单集合
     */
    @Override
    public List<ExamForm> selectExamFormList(ExamForm examForm) {
        if (examForm == null) {
            return list();
        }
        LambdaQueryWrapper<ExamForm> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(examForm.getId() != null, ExamForm::getId, examForm.getId());
        queryWrapper.eq(StrUtil.isNotBlank(examForm.getName()), ExamForm::getName, examForm.getName());
        queryWrapper.eq(StrUtil.isNotBlank(examForm.getModuleIds()), ExamForm::getModuleIds, examForm.getModuleIds());
        queryWrapper.eq(StrUtil.isNotBlank(examForm.getRemark()), ExamForm::getRemark, examForm.getRemark());
        return list(queryWrapper);
    }

    /**
     * 新增考试表单
     *
     * @param examForm 考试表单
     * @return 结果
     */
    @Override
    public int insertExamForm(ExamForm examForm) {
        return save(examForm) ? 1 : 0;
    }

    /**
     * 修改考试表单
     *
     * @param examForm 考试表单
     * @return 结果
     */
    @Override
    public int updateExamForm(ExamForm examForm) {
        return updateById(examForm) ? 1 : 0;
    }

    /**
     * 批量删除考试表单
     *
     * @param ids 需要删除的考试表单主键
     * @return 结果
     */
    @Override
    public int deleteExamFormByIds(Long[] ids) {

        if (ArrayUtil.isEmpty(ids)) {
            return 0;
        }

        return removeByIds(java.util.Arrays.asList(ids)) ? 1 : 0;

    }

    /**
     * 删除考试表单信息
     *
     * @param id 考试表单主键
     * @return 结果
     */
    @Override
    public int deleteExamFormById(Long id) {
        return removeById(id) ? 1 : 0;
    }
}

