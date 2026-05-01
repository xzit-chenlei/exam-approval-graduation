package edu.xzit.graduate.dao.service.impl;

import java.util.Arrays;
import java.util.List;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.core.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.xzit.graduate.dao.mapper.GraduatePerformanceMapper;
import edu.xzit.graduate.dao.domain.GraduatePerformance;
import edu.xzit.graduate.dao.service.IGraduatePerformanceRepoService;

/**
 * 毕业达成度平时测验Service业务层处理
 *
 * @author vikTor
 * @date 2025-09-16
 */
@Service
public class GraduatePerformanceRepoServiceImpl extends ServiceImpl<GraduatePerformanceMapper,GraduatePerformance> implements IGraduatePerformanceRepoService {
    @Autowired
    private GraduatePerformanceMapper graduatePerformanceMapper;

    /**
     * 查询毕业达成度平时测验
     *
     * @param id 毕业达成度平时测验主键
     * @return 毕业达成度平时测验
     */
    @Override
    public GraduatePerformance selectGraduatePerformanceById(Long id) {
        return graduatePerformanceMapper.selectById(id);
    }

    /**
     * 查询毕业达成度平时测验列表
     *
     * @param graduatePerformance 毕业达成度平时测验
     * @return 毕业达成度平时测验
     */
    @Override
    public List<GraduatePerformance> selectGraduatePerformanceList(GraduatePerformance graduatePerformance) {
        LambdaQueryWrapper<GraduatePerformance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(graduatePerformance.getMajorId() != null, GraduatePerformance::getMajorId, graduatePerformance.getMajorId())
                .eq(graduatePerformance.getCourseId() != null, GraduatePerformance::getCourseId, graduatePerformance.getCourseId())
                .like(graduatePerformance.getQuizName() != null && !graduatePerformance.getQuizName().isEmpty(), GraduatePerformance::getQuizName, graduatePerformance.getQuizName())
                .eq(graduatePerformance.getQuizCode() != null && !graduatePerformance.getQuizCode().isEmpty(), GraduatePerformance::getQuizCode, graduatePerformance.getQuizCode())
                .eq(graduatePerformance.getQuizDate() != null, GraduatePerformance::getQuizDate, graduatePerformance.getQuizDate())
                .eq(graduatePerformance.getTotalScore() != null, GraduatePerformance::getTotalScore, graduatePerformance.getTotalScore())
                .eq(graduatePerformance.getQuizDuration() != null, GraduatePerformance::getQuizDuration, graduatePerformance.getQuizDuration())
                .eq(graduatePerformance.getGrade() != null && !graduatePerformance.getGrade().isEmpty(), GraduatePerformance::getGrade, graduatePerformance.getGrade())
                .eq(graduatePerformance.getSemester() != null && !graduatePerformance.getSemester().isEmpty(), GraduatePerformance::getSemester, graduatePerformance.getSemester())
                .eq(graduatePerformance.getOrderNo() != null, GraduatePerformance::getOrderNo, graduatePerformance.getOrderNo())
                .eq(GraduatePerformance::getIsValid, 0); // 只查询有效的记录
        return graduatePerformanceMapper.selectList(queryWrapper);
    }

    /**
     * 新增毕业达成度平时测验
     *
     * @param graduatePerformance 毕业达成度平时测验
     * @return 结果
     */
    @Override
    public int insertGraduatePerformance(GraduatePerformance graduatePerformance) {
        graduatePerformance.setIsValid(0L); // 设置为有效记录
        return graduatePerformanceMapper.insert(graduatePerformance);
    }

    /**
     * 修改毕业达成度平时测验
     *
     * @param graduatePerformance 毕业达成度平时测验
     * @return 结果
     */
    @Override
    public int updateGraduatePerformance(GraduatePerformance graduatePerformance) {
        return graduatePerformanceMapper.updateById(graduatePerformance);
    }

    /**
     * 批量删除毕业达成度平时测验
     *
     * @param ids 需要删除的毕业达成度平时测验主键
     * @return 结果
     */
    @Override
    public int deleteGraduatePerformanceByIds(Long[] ids) {

        if (ArrayUtil.isEmpty(ids)) {
            return 0;
        }

        LambdaQueryWrapper<GraduatePerformance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(GraduatePerformance::getId, Arrays.asList(ids));
        // 逻辑删除，设置isValid为1
        GraduatePerformance performance = new GraduatePerformance();
        performance.setIsValid(1L);
        return graduatePerformanceMapper.update(performance, queryWrapper);

    }

    /**
     * 删除毕业达成度平时测验信息
     *
     * @param id 毕业达成度平时测验主键
     * @return 结果
     */
    @Override
    public int deleteGraduatePerformanceById(Long id) {
        GraduatePerformance performance = new GraduatePerformance();
        performance.setId(id);
        performance.setIsValid(1L);
        return graduatePerformanceMapper.updateById(performance);
    }
}