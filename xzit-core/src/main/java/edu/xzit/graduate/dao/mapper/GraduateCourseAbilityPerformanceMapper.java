package edu.xzit.graduate.dao.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

import edu.xzit.graduate.dao.domain.GraduateCourseAbilityPerformance;

/**
 * 毕业达成度课程能力交叉(平时现)Mapper接口
 *
 * @author chenlei
 * @date 2025-09-21
 */
public interface GraduateCourseAbilityPerformanceMapper extends BaseMapper<GraduateCourseAbilityPerformance> {
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
     * 删除毕业达成度课程能力交叉(平时现)
     *
     * @param id 毕业达成度课程能力交叉(平时现)主键
     * @return 结果
     */
    int deleteGraduateCourseAbilityPerformanceById(Long id);

    /**
     * 批量删除毕业达成度课程能力交叉(平时现)
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteGraduateCourseAbilityPerformanceByIds(Long[] ids);
}
