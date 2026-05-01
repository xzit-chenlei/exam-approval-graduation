package edu.xzit.graduate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.xzit.graduate.dao.domain.GraduateAbility;
import edu.xzit.graduate.dao.domain.GraduateCourse;
import edu.xzit.graduate.dao.domain.GraduateCourseAbility;
import edu.xzit.graduate.dao.domain.GraduateCourseAbilityLab;
import edu.xzit.graduate.dao.service.IGraduateAbilityRepoService;
import edu.xzit.graduate.dao.service.IGraduateCourseAbilityLabRepoService;
import edu.xzit.graduate.dao.service.IGraduateCourseAbilityRepoService;
import edu.xzit.graduate.dao.service.IGraduateCourseRepoService;
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
public class CourseAbilityLabService {

    private final IGraduateCourseRepoService courseRepoService;
    private final IGraduateAbilityRepoService abilityRepoService;
    private final IGraduateCourseAbilityLabRepoService cellRepoService;

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

        LambdaQueryWrapper<GraduateCourseAbilityLab> cellQ = new QueryWrapper<GraduateCourseAbilityLab>().lambda()
                .in(GraduateCourseAbilityLab::getCourseId, courseIds)
                .in(GraduateCourseAbilityLab::getAbilityId, abilityIds)
                .eq(GraduateCourseAbilityLab::getGrade, grade);
        List<GraduateCourseAbilityLab> cells = cellRepoService.list(cellQ);

        Map<String, GraduateCourseAbilityLab> cellMap = new HashMap<>();
        for (GraduateCourseAbilityLab cell : cells) {
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
        LambdaQueryWrapper<GraduateCourseAbilityLab> q = new QueryWrapper<GraduateCourseAbilityLab>().lambda()
                .eq(GraduateCourseAbilityLab::getCourseId, courseId);
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
        LambdaQueryWrapper<GraduateCourseAbilityLab> q = new QueryWrapper<GraduateCourseAbilityLab>().lambda()
                .eq(GraduateCourseAbilityLab::getAbilityId, abilityId);
        cellRepoService.remove(q);
        abilityRepoService.removeById(abilityId);
    }

    /** 设置或更新单元格（H/M/L 和可选权重） */
    @Transactional(rollbackFor = Exception.class)
    public void upsertCell(Long courseId, Long abilityId, Integer grade, BigDecimal level, BigDecimal weight, String remark) {
        LambdaQueryWrapper<GraduateCourseAbilityLab> q = new QueryWrapper<GraduateCourseAbilityLab>().lambda()
                .eq(GraduateCourseAbilityLab::getCourseId, courseId)
                .eq(GraduateCourseAbilityLab::getAbilityId, abilityId)
                .eq(GraduateCourseAbilityLab::getGrade, grade);
        GraduateCourseAbilityLab exist = cellRepoService.getOne(q);
        if (exist == null) {
            GraduateCourseAbilityLab cell = new GraduateCourseAbilityLab();
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
        LambdaQueryWrapper<GraduateCourseAbilityLab> q = new QueryWrapper<GraduateCourseAbilityLab>().lambda()
                .eq(GraduateCourseAbilityLab::getCourseId, courseId)
                .eq(GraduateCourseAbilityLab::getAbilityId, abilityId);
        cellRepoService.remove(q);
    }

    private static String key(Long courseId, Long abilityId) {
        return courseId + "_" + abilityId;
    }

    @Data
    public static class MatrixResult {
        private final List<GraduateCourse> courses;
        private final List<GraduateAbility> abilities;
        private final Map<String, GraduateCourseAbilityLab> cells;
    }

}
