package edu.xzit.graduate.dao.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.graduate.dao.domain .GraduateCourseWeight;

/**
 * 毕业达成度各课程三类考试权重Service接口
 *
 * @author chenlei
 * @date 2025-09-30
 */
public interface IGraduateCourseWeightRepoService extends IService<GraduateCourseWeight> {
    /**
     * 查询毕业达成度各课程三类考试权重
     *
     * @param id 毕业达成度各课程三类考试权重主键
     * @return 毕业达成度各课程三类考试权重
     */
        GraduateCourseWeight selectGraduateCourseWeightById(Long id);

    /**
     * 查询毕业达成度各课程三类考试权重列表
     *
     * @param graduateCourseWeight 毕业达成度各课程三类考试权重
     * @return 毕业达成度各课程三类考试权重集合
     */
    List<GraduateCourseWeight> selectGraduateCourseWeightList(GraduateCourseWeight graduateCourseWeight);

    /**
     * 新增毕业达成度各课程三类考试权重
     *
     * @param graduateCourseWeight 毕业达成度各课程三类考试权重
     * @return 结果
     */
    int insertGraduateCourseWeight(GraduateCourseWeight graduateCourseWeight);

    /**
     * 修改毕业达成度各课程三类考试权重
     *
     * @param graduateCourseWeight 毕业达成度各课程三类考试权重
     * @return 结果
     */
    int updateGraduateCourseWeight(GraduateCourseWeight graduateCourseWeight);

    /**
     * 批量删除毕业达成度各课程三类考试权重
     *
     * @param ids 需要删除的毕业达成度各课程三类考试权重主键集合
     * @return 结果
     */
    int deleteGraduateCourseWeightByIds(Long[] ids);

    /**
     * 删除毕业达成度各课程三类考试权重信息
     *
     * @param id 毕业达成度各课程三类考试权重主键
     * @return 结果
     */
    int deleteGraduateCourseWeightById(Long id);
}
