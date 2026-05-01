package edu.xzit.graduate.dao.service.impl;

import java.util.Arrays;
import java.util.List;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import edu.xzit.graduate.dao.mapper.GraduateCourseWeightMapper;
import edu.xzit.graduate.dao.domain.GraduateCourseWeight;
import edu.xzit.graduate.dao.service.IGraduateCourseWeightRepoService;

/**
 * 毕业达成度各课程三类考试权重Service业务层处理
 *
 * @author chenlei
 * @date 2025-09-30
 */
@Service
public class GraduateCourseWeightRepoServiceImpl extends ServiceImpl<GraduateCourseWeightMapper,GraduateCourseWeight> implements IGraduateCourseWeightRepoService {

    /**
     * 查询毕业达成度各课程三类考试权重
     *
     * @param id 毕业达成度各课程三类考试权重主键
     * @return 毕业达成度各课程三类考试权重
     */
    @Override
    public GraduateCourseWeight selectGraduateCourseWeightById(Long id) {
        return this.getById(id);
    }

    /**
     * 查询毕业达成度各课程三类考试权重列表
     *
     * @param graduateCourseWeight 毕业达成度各课程三类考试权重
     * @return 毕业达成度各课程三类考试权重
     */
    @Override
    public List<GraduateCourseWeight> selectGraduateCourseWeightList(GraduateCourseWeight graduateCourseWeight) {
        LambdaQueryWrapper<GraduateCourseWeight> queryWrapper = new LambdaQueryWrapper<>();
        // 如果courseId不为空，则添加查询条件
        if (graduateCourseWeight.getCourseId() != null) {
            queryWrapper.eq(GraduateCourseWeight::getCourseId, graduateCourseWeight.getCourseId());
        }
        return this.list(queryWrapper);
    }

    /**
     * 新增毕业达成度各课程三类考试权重
     *
     * @param graduateCourseWeight 毕业达成度各课程三类考试权重
     * @return 结果
     */
    @Override
    public int insertGraduateCourseWeight(GraduateCourseWeight graduateCourseWeight) {
        return this.save(graduateCourseWeight) ? 1 : 0;
    }

    /**
     * 修改毕业达成度各课程三类考试权重
     *
     * @param graduateCourseWeight 毕业达成度各课程三类考试权重
     * @return 结果
     */
    @Override
    public int updateGraduateCourseWeight(GraduateCourseWeight graduateCourseWeight) {
        return this.updateById(graduateCourseWeight) ? 1 : 0;
    }

    /**
     * 批量删除毕业达成度各课程三类考试权重
     *
     * @param ids 需要删除的毕业达成度各课程三类考试权重主键
     * @return 结果
     */
    @Override
    public int deleteGraduateCourseWeightByIds(Long[] ids) {

        if (ArrayUtil.isEmpty(ids)) {
            return 0;
        }

        LambdaQueryWrapper<GraduateCourseWeight> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(GraduateCourseWeight::getId, Arrays.asList(ids));
        return this.remove(queryWrapper) ? 1 : 0;

    }

    /**
     * 删除毕业达成度各课程三类考试权重信息
     *
     * @param id 毕业达成度各课程三类考试权重主键
     * @return 结果
     */
    @Override
    public int deleteGraduateCourseWeightById(Long id) {
        return this.removeById(id) ? 1 : 0;
    }
}