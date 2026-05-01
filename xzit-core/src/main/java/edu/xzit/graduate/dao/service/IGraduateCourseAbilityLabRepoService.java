package edu.xzit.graduate.dao.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.graduate.dao.domain .GraduateCourseAbilityLab;

/**
 * 毕业达成度课程能力交叉(实验课)Service接口
 *
 * @author chenlei
 * @date 2025-09-21
 */
public interface IGraduateCourseAbilityLabRepoService extends IService<GraduateCourseAbilityLab> {
    /**
     * 查询毕业达成度课程能力交叉(实验课)
     *
     * @param id 毕业达成度课程能力交叉(实验课)主键
     * @return 毕业达成度课程能力交叉(实验课)
     */
        GraduateCourseAbilityLab selectGraduateCourseAbilityLabById(Long id);

    /**
     * 查询毕业达成度课程能力交叉(实验课)列表
     *
     * @param graduateCourseAbilityLab 毕业达成度课程能力交叉(实验课)
     * @return 毕业达成度课程能力交叉(实验课)集合
     */
    List<GraduateCourseAbilityLab> selectGraduateCourseAbilityLabList(GraduateCourseAbilityLab graduateCourseAbilityLab);

    /**
     * 新增毕业达成度课程能力交叉(实验课)
     *
     * @param graduateCourseAbilityLab 毕业达成度课程能力交叉(实验课)
     * @return 结果
     */
    int insertGraduateCourseAbilityLab(GraduateCourseAbilityLab graduateCourseAbilityLab);

    /**
     * 修改毕业达成度课程能力交叉(实验课)
     *
     * @param graduateCourseAbilityLab 毕业达成度课程能力交叉(实验课)
     * @return 结果
     */
    int updateGraduateCourseAbilityLab(GraduateCourseAbilityLab graduateCourseAbilityLab);

    /**
     * 批量删除毕业达成度课程能力交叉(实验课)
     *
     * @param ids 需要删除的毕业达成度课程能力交叉(实验课)主键集合
     * @return 结果
     */
    int deleteGraduateCourseAbilityLabByIds(Long[] ids);

    /**
     * 删除毕业达成度课程能力交叉(实验课)信息
     *
     * @param id 毕业达成度课程能力交叉(实验课)主键
     * @return 结果
     */
    int deleteGraduateCourseAbilityLabById(Long id);
}
