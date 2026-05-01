package edu.xzit.graduate.dao.service.impl;

import java.util.Arrays;
import java.util.List;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.core.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.xzit.graduate.dao.mapper.GraduateCourseAbilityLabMapper;
import edu.xzit.graduate.dao.domain.GraduateCourseAbilityLab;
import edu.xzit.graduate.dao.service.IGraduateCourseAbilityLabRepoService;

/**
 * 毕业达成度课程能力交叉(实验课)Service业务层处理
 *
 * @author chenlei
 * @date 2025-09-21
 */
@Service
public class GraduateCourseAbilityLabRepoServiceImpl extends ServiceImpl<GraduateCourseAbilityLabMapper,GraduateCourseAbilityLab> implements IGraduateCourseAbilityLabRepoService {
    @Autowired
    private GraduateCourseAbilityLabMapper graduateCourseAbilityLabMapper;

    /**
     * 查询毕业达成度课程能力交叉(实验课)
     *
     * @param id 毕业达成度课程能力交叉(实验课)主键
     * @return 毕业达成度课程能力交叉(实验课)
     */
    @Override
    public GraduateCourseAbilityLab selectGraduateCourseAbilityLabById(Long id) {
        return graduateCourseAbilityLabMapper.selectGraduateCourseAbilityLabById(id);
    }

    /**
     * 查询毕业达成度课程能力交叉(实验课)列表
     *
     * @param graduateCourseAbilityLab 毕业达成度课程能力交叉(实验课)
     * @return 毕业达成度课程能力交叉(实验课)
     */
    @Override
    public List<GraduateCourseAbilityLab> selectGraduateCourseAbilityLabList(GraduateCourseAbilityLab graduateCourseAbilityLab) {
        return graduateCourseAbilityLabMapper.selectGraduateCourseAbilityLabList(graduateCourseAbilityLab);
    }

    /**
     * 新增毕业达成度课程能力交叉(实验课)
     *
     * @param graduateCourseAbilityLab 毕业达成度课程能力交叉(实验课)
     * @return 结果
     */
    @Override
    public int insertGraduateCourseAbilityLab(GraduateCourseAbilityLab graduateCourseAbilityLab) {
        return save(graduateCourseAbilityLab) ? 1 : 0;
    }

    /**
     * 修改毕业达成度课程能力交叉(实验课)
     *
     * @param graduateCourseAbilityLab 毕业达成度课程能力交叉(实验课)
     * @return 结果
     */
    @Override
    public int updateGraduateCourseAbilityLab(GraduateCourseAbilityLab graduateCourseAbilityLab) {
        return updateById(graduateCourseAbilityLab) ? 1 : 0;
    }

    /**
     * 批量删除毕业达成度课程能力交叉(实验课)
     *
     * @param ids 需要删除的毕业达成度课程能力交叉(实验课)主键
     * @return 结果
     */
    @Override
    public int deleteGraduateCourseAbilityLabByIds(Long[] ids) {

        if (ArrayUtil.isEmpty(ids)) {
            return 0;
        }

        LambdaQueryWrapper<GraduateCourseAbilityLab> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(GraduateCourseAbilityLab::getId, Arrays.asList(ids));
        return remove(queryWrapper) ? 1 : 0;

    }

    /**
     * 删除毕业达成度课程能力交叉(实验课)信息
     *
     * @param id 毕业达成度课程能力交叉(实验课)主键
     * @return 结果
     */
    @Override
    public int deleteGraduateCourseAbilityLabById(Long id) {
        return graduateCourseAbilityLabMapper.deleteGraduateCourseAbilityLabById(id);
    }
}
