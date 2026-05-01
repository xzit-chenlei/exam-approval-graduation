package edu.xzit.graduate.dao.service.impl;

import java.util.Arrays;
import java.util.List;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.core.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.xzit.graduate.dao.mapper.GraduateLabMapper;
import edu.xzit.graduate.dao.domain.GraduateLab;
import edu.xzit.graduate.dao.service.IGraduateLabRepoService;

/**
 * 毕业达成度实验成绩信息Service业务层处理
 *
 * @author vikTor
 * @date 2025-09-16
 */
@Service
public class GraduateLabRepoServiceImpl extends ServiceImpl<GraduateLabMapper,GraduateLab> implements IGraduateLabRepoService {
    @Autowired
    private GraduateLabMapper graduateLabMapper;

    /**
     * 查询毕业达成度实验成绩信息
     *
     * @param id 毕业达成度实验成绩信息主键
     * @return 毕业达成度实验成绩信息
     */
    @Override
    public GraduateLab selectGraduateLabById(Long id) {
        return graduateLabMapper.selectById(id);
    }

    /**
     * 查询毕业达成度实验成绩信息列表
     *
     * @param graduateLab 毕业达成度实验成绩信息
     * @return 毕业达成度实验成绩信息
     */
    @Override
    public List<GraduateLab> selectGraduateLabList(GraduateLab graduateLab) {
        LambdaQueryWrapper<GraduateLab> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(graduateLab.getMajorId() != null, GraduateLab::getMajorId, graduateLab.getMajorId())
                .eq(graduateLab.getCourseId() != null, GraduateLab::getCourseId, graduateLab.getCourseId())
                .like(graduateLab.getLabName() != null && !graduateLab.getLabName().isEmpty(), GraduateLab::getLabName, graduateLab.getLabName())
                .eq(graduateLab.getLabCode() != null && !graduateLab.getLabCode().isEmpty(), GraduateLab::getLabCode, graduateLab.getLabCode())
                .eq(graduateLab.getDeadline() != null, GraduateLab::getDeadline, graduateLab.getDeadline())
                .eq(graduateLab.getOpsScore() != null, GraduateLab::getOpsScore, graduateLab.getOpsScore())
                .eq(graduateLab.getRptScore() != null, GraduateLab::getRptScore, graduateLab.getRptScore())
                .eq(graduateLab.getGrade() != null && !graduateLab.getGrade().isEmpty(), GraduateLab::getGrade, graduateLab.getGrade())
                .eq(graduateLab.getSemester() != null && !graduateLab.getSemester().isEmpty(), GraduateLab::getSemester, graduateLab.getSemester())
                .eq(graduateLab.getOrderNo() != null, GraduateLab::getOrderNo, graduateLab.getOrderNo())
                .eq(GraduateLab::getIsValid, 0); // 只查询有效的记录
        return graduateLabMapper.selectList(queryWrapper);
    }

    /**
     * 新增毕业达成度实验成绩信息
     *
     * @param graduateLab 毕业达成度实验成绩信息
     * @return 结果
     */
    @Override
    public int insertGraduateLab(GraduateLab graduateLab) {
        graduateLab.setIsValid(0L); // 设置为有效记录
        return graduateLabMapper.insert(graduateLab);
    }

    /**
     * 修改毕业达成度实验成绩信息
     *
     * @param graduateLab 毕业达成度实验成绩信息
     * @return 结果
     */
    @Override
    public int updateGraduateLab(GraduateLab graduateLab) {
        return graduateLabMapper.updateById(graduateLab);
    }

    /**
     * 批量删除毕业达成度实验成绩信息
     *
     * @param ids 需要删除的毕业达成度实验成绩信息主键
     * @return 结果
     */
    @Override
    public int deleteGraduateLabByIds(Long[] ids) {

        if (ArrayUtil.isEmpty(ids)) {
            return 0;
        }

        LambdaQueryWrapper<GraduateLab> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(GraduateLab::getId, Arrays.asList(ids));
        // 逻辑删除，设置isValid为1
        GraduateLab lab = new GraduateLab();
        lab.setIsValid(1L);
        return graduateLabMapper.update(lab, queryWrapper);

    }

    /**
     * 删除毕业达成度实验成绩信息信息
     *
     * @param id 毕业达成度实验成绩信息主键
     * @return 结果
     */
    @Override
    public int deleteGraduateLabById(Long id) {
        GraduateLab lab = new GraduateLab();
        lab.setId(id);
        lab.setIsValid(1L);
        return graduateLabMapper.updateById(lab);
    }
}