package edu.xzit.graduate.dao.service.impl;

import java.util.Arrays;
import java.util.List;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.core.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.xzit.graduate.dao.mapper.GraduateCourseAbilityPerformanceMapper;
import edu.xzit.graduate.dao.domain.GraduateCourseAbilityPerformance;
import edu.xzit.graduate.dao.service.IGraduateCourseAbilityPerformanceRepoService;

/**
 * 毕业达成度课程能力交叉(平时现)Service业务层处理
 *
 * @author chenlei
 * @date 2025-09-21
 */
@Service
public class GraduateCourseAbilityPerformanceRepoServiceImpl extends ServiceImpl<GraduateCourseAbilityPerformanceMapper,GraduateCourseAbilityPerformance> implements IGraduateCourseAbilityPerformanceRepoService {
    @Autowired
    private GraduateCourseAbilityPerformanceMapper graduateCourseAbilityPerformanceMapper;

    /**
     * 查询毕业达成度课程能力交叉(平时现)
     *
     * @param id 毕业达成度课程能力交叉(平时现)主键
     * @return 毕业达成度课程能力交叉(平时现)
     */
    @Override
    public GraduateCourseAbilityPerformance selectGraduateCourseAbilityPerformanceById(Long id) {
        return graduateCourseAbilityPerformanceMapper.selectGraduateCourseAbilityPerformanceById(id);
    }

    /**
     * 查询毕业达成度课程能力交叉(平时现)列表
     *
     * @param graduateCourseAbilityPerformance 毕业达成度课程能力交叉(平时现)
     * @return 毕业达成度课程能力交叉(平时现)
     */
    @Override
    public List<GraduateCourseAbilityPerformance> selectGraduateCourseAbilityPerformanceList(GraduateCourseAbilityPerformance graduateCourseAbilityPerformance) {
        return graduateCourseAbilityPerformanceMapper.selectGraduateCourseAbilityPerformanceList(graduateCourseAbilityPerformance);
    }

    /**
     * 新增毕业达成度课程能力交叉(平时现)
     *
     * @param graduateCourseAbilityPerformance 毕业达成度课程能力交叉(平时现)
     * @return 结果
     */
    @Override
    public int insertGraduateCourseAbilityPerformance(GraduateCourseAbilityPerformance graduateCourseAbilityPerformance) {
        return save(graduateCourseAbilityPerformance) ? 1 : 0;
    }

    /**
     * 修改毕业达成度课程能力交叉(平时现)
     *
     * @param graduateCourseAbilityPerformance 毕业达成度课程能力交叉(平时现)
     * @return 结果
     */
    @Override
    public int updateGraduateCourseAbilityPerformance(GraduateCourseAbilityPerformance graduateCourseAbilityPerformance) {
        return updateById(graduateCourseAbilityPerformance) ? 1 : 0;
    }

    /**
     * 批量删除毕业达成度课程能力交叉(平时现)
     *
     * @param ids 需要删除的毕业达成度课程能力交叉(平时现)主键
     * @return 结果
     */
    @Override
    public int deleteGraduateCourseAbilityPerformanceByIds(Long[] ids) {

        if (ArrayUtil.isEmpty(ids)) {
            return 0;
        }

        LambdaQueryWrapper<GraduateCourseAbilityPerformance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(GraduateCourseAbilityPerformance::getId, Arrays.asList(ids));
        return remove(queryWrapper) ? 1 : 0;

    }

    /**
     * 删除毕业达成度课程能力交叉(平时现)信息
     *
     * @param id 毕业达成度课程能力交叉(平时现)主键
     * @return 结果
     */
    @Override
    public int deleteGraduateCourseAbilityPerformanceById(Long id) {
        return graduateCourseAbilityPerformanceMapper.deleteGraduateCourseAbilityPerformanceById(id);
    }
}
