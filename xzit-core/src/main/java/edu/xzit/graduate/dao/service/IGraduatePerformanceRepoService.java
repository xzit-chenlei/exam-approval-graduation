package edu.xzit.graduate.dao.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.graduate.dao.domain .GraduatePerformance;

/**
 * 毕业达成度平时测验Service接口
 *
 * @author vikTor
 * @date 2025-09-16
 */
public interface IGraduatePerformanceRepoService extends IService<GraduatePerformance> {
    /**
     * 查询毕业达成度平时测验
     *
     * @param id 毕业达成度平时测验主键
     * @return 毕业达成度平时测验
     */
        GraduatePerformance selectGraduatePerformanceById(Long id);

    /**
     * 查询毕业达成度平时测验列表
     *
     * @param graduatePerformance 毕业达成度平时测验
     * @return 毕业达成度平时测验集合
     */
    List<GraduatePerformance> selectGraduatePerformanceList(GraduatePerformance graduatePerformance);

    /**
     * 新增毕业达成度平时测验
     *
     * @param graduatePerformance 毕业达成度平时测验
     * @return 结果
     */
    int insertGraduatePerformance(GraduatePerformance graduatePerformance);

    /**
     * 修改毕业达成度平时测验
     *
     * @param graduatePerformance 毕业达成度平时测验
     * @return 结果
     */
    int updateGraduatePerformance(GraduatePerformance graduatePerformance);

    /**
     * 批量删除毕业达成度平时测验
     *
     * @param ids 需要删除的毕业达成度平时测验主键集合
     * @return 结果
     */
    int deleteGraduatePerformanceByIds(Long[] ids);

    /**
     * 删除毕业达成度平时测验信息
     *
     * @param id 毕业达成度平时测验主键
     * @return 结果
     */
    int deleteGraduatePerformanceById(Long id);
}
