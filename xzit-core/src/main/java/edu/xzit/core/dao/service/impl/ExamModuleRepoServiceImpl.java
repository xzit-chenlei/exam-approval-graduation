package edu.xzit.core.dao.service.impl;

import java.util.List;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.dao.domain.ExamModule;
import edu.xzit.core.dao.mapper.ExamModuleMapper;
import edu.xzit.core.dao.service.IExamModuleRepoService;
import org.springframework.stereotype.Service;

/**
 * 考试模块Service业务层处理
 *
 * @author
 * @date 2025-11-09
 */
@Service
public class ExamModuleRepoServiceImpl extends ServiceImpl<ExamModuleMapper, ExamModule> implements IExamModuleRepoService {

    /**
     * 查询考试模块
     *
     * @param id 考试模块主键
     * @return 考试模块
     */
    @Override
    public ExamModule selectExamModuleById(Long id) {
        return getById(id);
    }

    /**
     * 查询考试模块列表
     *
     * @param examModule 考试模块
     * @return 考试模块集合
     */
    @Override
    public List<ExamModule> selectExamModuleList(ExamModule examModule) {
        if (examModule == null) {
            return list();
        }
        LambdaQueryWrapper<ExamModule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(examModule.getId() != null, ExamModule::getId, examModule.getId());
        queryWrapper.eq(StrUtil.isNotBlank(examModule.getName()), ExamModule::getName, examModule.getName());
        queryWrapper.eq(StrUtil.isNotBlank(examModule.getRowHeaders()), ExamModule::getRowHeaders, examModule.getRowHeaders());
        queryWrapper.eq(StrUtil.isNotBlank(examModule.getColHeaders()), ExamModule::getColHeaders, examModule.getColHeaders());
        queryWrapper.eq(StrUtil.isNotBlank(examModule.getData()), ExamModule::getData, examModule.getData());
        queryWrapper.eq(StrUtil.isNotBlank(examModule.getRemark()), ExamModule::getRemark, examModule.getRemark());
        return list(queryWrapper);
    }

    /**
     * 新增考试模块
     *
     * @param examModule 考试模块
     * @return 结果
     */
    @Override
    public int insertExamModule(ExamModule examModule) {
        return save(examModule) ? 1 : 0;
    }

    /**
     * 修改考试模块
     *
     * @param examModule 考试模块
     * @return 结果
     */
    @Override
    public int updateExamModule(ExamModule examModule) {
        return updateById(examModule) ? 1 : 0;
    }

    /**
     * 批量删除考试模块
     *
     * @param ids 需要删除的考试模块主键
     * @return 结果
     */
    @Override
    public int deleteExamModuleByIds(Long[] ids) {

        if (ArrayUtil.isEmpty(ids)) {
            return 0;
        }

        return removeByIds(java.util.Arrays.asList(ids)) ? 1 : 0;

    }

    /**
     * 删除考试模块信息
     *
     * @param id 考试模块主键
     * @return 结果
     */
    @Override
    public int deleteExamModuleById(Long id) {
        return removeById(id) ? 1 : 0;
    }
}

