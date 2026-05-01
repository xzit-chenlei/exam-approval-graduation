package edu.xzit.core.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.core.dao.domain.ExamModule;

import java.util.List;

/**
 * 考试模块Service接口
 *
 * @author
 * @date 2025-11-09
 */
public interface IExamModuleRepoService extends IService<ExamModule> {
    /**
     * 查询考试模块
     *
     * @param id 考试模块主键
     * @return 考试模块
     */
    ExamModule selectExamModuleById(Long id);

    /**
     * 查询考试模块列表
     *
     * @param examModule 考试模块
     * @return 考试模块集合
     */
    List<ExamModule> selectExamModuleList(ExamModule examModule);

    /**
     * 新增考试模块
     *
     * @param examModule 考试模块
     * @return 结果
     */
    int insertExamModule(ExamModule examModule);

    /**
     * 修改考试模块
     *
     * @param examModule 考试模块
     * @return 结果
     */
    int updateExamModule(ExamModule examModule);

    /**
     * 批量删除考试模块
     *
     * @param ids 需要删除的考试模块主键集合
     * @return 结果
     */
    int deleteExamModuleByIds(Long[] ids);

    /**
     * 删除考试模块信息
     *
     * @param id 考试模块主键
     * @return 结果
     */
    int deleteExamModuleById(Long id);
}

