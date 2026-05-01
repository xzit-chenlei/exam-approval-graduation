package edu.xzit.graduate.dao.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.graduate.dao.domain .GraduateCourseAbilityPerformance;

/**
 * 毕业达成度课程能力交叉(平时现)Service接口
 *
 * @author chenlei
 * @date 2025-09-21
 */
public interface IGraduateCourseAbilityPerformanceRepoService extends IService<GraduateCourseAbilityPerformance> {
    /**
     * 查询毕业达成度课程能力交叉(平时现)
     *
     * @param id 毕业达成度课程能力交叉(平时现)主键
     * @return 毕业达成度课程能力交叉(平时现)
     */
        GraduateCourseAbilityPerformance selectGraduateCourseAbilityPerformanceById(Long id);

    /**
     * 查询毕业达成度课程能力交叉(平时现)列表
     *
     * @param graduateCourseAbilityPerformance 毕业达成度课程能力交叉(平时现)
     * @return 毕业达成度课程能力交叉(平时现)集合
     */
    List<GraduateCourseAbilityPerformance> selectGraduateCourseAbilityPerformanceList(GraduateCourseAbilityPerformance graduateCourseAbilityPerformance);

    /**
     * 新增毕业达成度课程能力交叉(平时现)
     *
     * @param graduateCourseAbilityPerformance 毕业达成度课程能力交叉(平时现)
     * @return 结果
     */
    int insertGraduateCourseAbilityPerformance(GraduateCourseAbilityPerformance graduateCourseAbilityPerformance);

    /**
     * 修改毕业达成度课程能力交叉(平时现)
     *
     * @param graduateCourseAbilityPerformance 毕业达成度课程能力交叉(平时现)
     * @return 结果
     */
    int updateGraduateCourseAbilityPerformance(GraduateCourseAbilityPerformance graduateCourseAbilityPerformance);

    /**
     * 批量删除毕业达成度课程能力交叉(平时现)
     *
     * @param ids 需要删除的毕业达成度课程能力交叉(平时现)主键集合
     * @return 结果
     */
    int deleteGraduateCourseAbilityPerformanceByIds(Long[] ids);

    /**
     * 删除毕业达成度课程能力交叉(平时现)信息
     *
     * @param id 毕业达成度课程能力交叉(平时现)主键
     * @return 结果
     */
    int deleteGraduateCourseAbilityPerformanceById(Long id);
}
