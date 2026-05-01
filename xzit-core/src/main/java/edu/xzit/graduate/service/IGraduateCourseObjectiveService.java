package edu.xzit.graduate.service;

import edu.xzit.graduate.dao.domain.GraduateCourseObjective;

import java.util.List;

/**
 * 课程目标 业务服务接口
 *
 * @author chenlei
 * @date 2025-08-25
 */
public interface IGraduateCourseObjectiveService {

    /**
     * 查询课程目标列表
     *
     * @param courseObjective 课程目标
     * @return 课程目标集合
     */
    List<GraduateCourseObjective> selectCourseObjectiveList(GraduateCourseObjective courseObjective);

    /**
     * 查询课程目标详细
     *
     * @param id 课程目标主键
     * @return 课程目标
     */
    GraduateCourseObjective selectCourseObjectiveById(Long id);

    /**
     * 新增课程目标
     *
     * @param courseObjective 课程目标
     * @return 结果
     */
    int insertCourseObjective(GraduateCourseObjective courseObjective);

    /**
     * 修改课程目标
     *
     * @param courseObjective 课程目标
     * @return 结果
     */
    int updateCourseObjective(GraduateCourseObjective courseObjective);

    /**
     * 批量删除课程目标
     *
     * @param ids 需要删除的课程目标主键集合
     * @return 结果
     */
    int deleteCourseObjectiveByIds(Long[] ids);

    /**
     * 删除课程目标信息
     *
     * @param id 课程目标主键
     * @return 结果
     */
    int deleteCourseObjectiveById(Long id);
} 