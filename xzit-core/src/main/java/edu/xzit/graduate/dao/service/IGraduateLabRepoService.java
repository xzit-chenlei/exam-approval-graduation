package edu.xzit.graduate.dao.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.graduate.dao.domain .GraduateLab;

/**
 * 毕业达成度实验成绩信息Service接口
 *
 * @author vikTor
 * @date 2025-09-16
 */
public interface IGraduateLabRepoService extends IService<GraduateLab> {
    /**
     * 查询毕业达成度实验成绩信息
     *
     * @param id 毕业达成度实验成绩信息主键
     * @return 毕业达成度实验成绩信息
     */
        GraduateLab selectGraduateLabById(Long id);

    /**
     * 查询毕业达成度实验成绩信息列表
     *
     * @param graduateLab 毕业达成度实验成绩信息
     * @return 毕业达成度实验成绩信息集合
     */
    List<GraduateLab> selectGraduateLabList(GraduateLab graduateLab);

    /**
     * 新增毕业达成度实验成绩信息
     *
     * @param graduateLab 毕业达成度实验成绩信息
     * @return 结果
     */
    int insertGraduateLab(GraduateLab graduateLab);

    /**
     * 修改毕业达成度实验成绩信息
     *
     * @param graduateLab 毕业达成度实验成绩信息
     * @return 结果
     */
    int updateGraduateLab(GraduateLab graduateLab);

    /**
     * 批量删除毕业达成度实验成绩信息
     *
     * @param ids 需要删除的毕业达成度实验成绩信息主键集合
     * @return 结果
     */
    int deleteGraduateLabByIds(Long[] ids);

    /**
     * 删除毕业达成度实验成绩信息信息
     *
     * @param id 毕业达成度实验成绩信息主键
     * @return 结果
     */
    int deleteGraduateLabById(Long id);
}
