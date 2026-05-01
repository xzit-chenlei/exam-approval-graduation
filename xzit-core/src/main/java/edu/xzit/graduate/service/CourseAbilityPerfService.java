package edu.xzit.graduate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.xzit.graduate.dao.domain.*;
import edu.xzit.graduate.dao.service.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CourseAbilityPerfService {

    private final IGraduateCourseRepoService courseRepoService;
    private final IGraduateAbilityRepoService abilityRepoService;
    private final IGraduateCourseAbilityPerformanceRepoService cellRepoService;

    /**
     * 查询二维表：返回课程列表、能力列表与单元格映射
     */
    public MatrixResult getMatrix(Long majorId, Integer grade) {
        LambdaQueryWrapper<GraduateCourse> courseQ = new QueryWrapper<GraduateCourse>().lambda()
                .eq(GraduateCourse::getMajorId, majorId)
                .eq(GraduateCourse::getGrade, grade)
                .orderByAsc(GraduateCourse::getOrderNo, GraduateCourse::getId);
        List<GraduateCourse> courses = courseRepoService.list(courseQ);

        LambdaQueryWrapper<GraduateAbility> abilityQ = new QueryWrapper<GraduateAbility>().lambda()
                .eq(GraduateAbility::getMajorId, majorId)
                .orderByAsc(GraduateAbility::getOrderNo, GraduateAbility::getId);
        List<GraduateAbility> abilities = abilityRepoService.list(abilityQ);

        // 年级必须参与权重过滤
        if (grade == null) {
            grade = Integer.parseInt(java.time.LocalDate.now().toString().substring(0, 4));
        }

        if (courses.isEmpty() || abilities.isEmpty()) {
            return new MatrixResult(courses, abilities, new HashMap<>());
        }

        List<Long> courseIds = courses.stream().map(GraduateCourse::getId).collect(Collectors.toList());
        List<Long> abilityIds = abilities.stream().map(GraduateAbility::getId).collect(Collectors.toList());

        LambdaQueryWrapper<GraduateCourseAbilityPerformance> cellQ = new QueryWrapper<GraduateCourseAbilityPerformance>().lambda()
                .in(GraduateCourseAbilityPerformance::getCourseId, courseIds)
                .in(GraduateCourseAbilityPerformance::getAbilityId, abilityIds)
                .eq(GraduateCourseAbilityPerformance::getGrade, grade);
        List<GraduateCourseAbilityPerformance> cells = cellRepoService.list(cellQ);

        Map<String, GraduateCourseAbilityPerformance> cellMap = new HashMap<>();
        for (GraduateCourseAbilityPerformance cell : cells) {
            cellMap.put(key(cell.getCourseId(), cell.getAbilityId()), cell);
        }
        return new MatrixResult(courses, abilities, cellMap);
    }

    /** 新增行：课程 */
    @Transactional(rollbackFor = Exception.class)
    public Long addCourse(GraduateCourse course) {
        Objects.requireNonNull(course.getMajorId(), "majorId required");
        courseRepoService.save(course);
        return course.getId();
    }

    /** 修改行：课程 */
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(GraduateCourse course) {
        courseRepoService.updateById(course);
    }

    /** 删除行：课程（级联删除交叉单元格，逻辑删除） */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long courseId) {
        LambdaQueryWrapper<GraduateCourseAbilityPerformance> q = new QueryWrapper<GraduateCourseAbilityPerformance>().lambda()
                .eq(GraduateCourseAbilityPerformance::getCourseId, courseId);
        cellRepoService.remove(q);
        courseRepoService.removeById(courseId);
    }

    /** 新增列：能力 */
    @Transactional(rollbackFor = Exception.class)
    public Long addAbility(GraduateAbility ability) {
        Objects.requireNonNull(ability.getMajorId(), "majorId required");
        abilityRepoService.save(ability);
        return ability.getId();
    }

    /** 修改列：能力 */
    @Transactional(rollbackFor = Exception.class)
    public void updateAbility(GraduateAbility ability) {
        abilityRepoService.updateById(ability);
    }

    /** 删除列：能力（级联删除交叉单元格，逻辑删除） */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAbility(Long abilityId) {
        LambdaQueryWrapper<GraduateCourseAbilityPerformance> q = new QueryWrapper<GraduateCourseAbilityPerformance>().lambda()
                .eq(GraduateCourseAbilityPerformance::getAbilityId, abilityId);
        cellRepoService.remove(q);
        abilityRepoService.removeById(abilityId);
    }

    /** 设置或更新单元格（H/M/L 和可选权重） */
    @Transactional(rollbackFor = Exception.class)
    public void upsertCell(Long courseId, Long abilityId, Integer grade, BigDecimal level, BigDecimal weight, String remark) {
        LambdaQueryWrapper<GraduateCourseAbilityPerformance> q = new QueryWrapper<GraduateCourseAbilityPerformance>().lambda()
                .eq(GraduateCourseAbilityPerformance::getCourseId, courseId)
                .eq(GraduateCourseAbilityPerformance::getAbilityId, abilityId)
                .eq(GraduateCourseAbilityPerformance::getGrade, grade);
        GraduateCourseAbilityPerformance exist = cellRepoService.getOne(q);
        if (exist == null) {
            GraduateCourseAbilityPerformance cell = new GraduateCourseAbilityPerformance();
            cell.setCourseId(courseId);
            cell.setAbilityId(abilityId);
            cell.setGrade(grade);
            cell.setLevel(level);
            cell.setWeight(weight);
            cell.setRemark(remark);
            cellRepoService.save(cell);
        } else {
            exist.setLevel(level);
            exist.setWeight(weight);
            exist.setRemark(remark);
            exist.setGrade(grade);
            cellRepoService.updateById(exist);
        }
    }

    /** 清除单元格（逻辑删除该交叉记录） */
    @Transactional(rollbackFor = Exception.class)
    public void clearCell(Long courseId, Long abilityId) {
        LambdaQueryWrapper<GraduateCourseAbilityPerformance> q = new QueryWrapper<GraduateCourseAbilityPerformance>().lambda()
                .eq(GraduateCourseAbilityPerformance::getCourseId, courseId)
                .eq(GraduateCourseAbilityPerformance::getAbilityId, abilityId);
        cellRepoService.remove(q);
    }

    private static String key(Long courseId, Long abilityId) {
        return courseId + "_" + abilityId;
    }

    @Data
    public static class MatrixResult {
        private final List<GraduateCourse> courses;
        private final List<GraduateAbility> abilities;
        private final Map<String, GraduateCourseAbilityPerformance> cells;
    }

}
