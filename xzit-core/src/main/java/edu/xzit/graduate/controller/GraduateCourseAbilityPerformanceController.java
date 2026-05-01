package edu.xzit.graduate.controller;

import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import edu.xzit.graduate.dao.domain.GraduateAbility;
import edu.xzit.graduate.dao.domain.GraduateCourse;
import edu.xzit.graduate.service.CourseAbilityPerfService;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.graduate.dao.domain.GraduateCourseAbilityPerformance;
import edu.xzit.graduate.dao.service.IGraduateCourseAbilityPerformanceRepoService;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.core.core.common.core.page.TableDataInfo;

/**
 * 毕业达成度课程能力交叉(平时现)Controller
 *
 * @author chenlei
 * @date 2025-09-21
 */
@RestController
@RequestMapping("/graduatePerf/matrix")
public class GraduateCourseAbilityPerformanceController extends BaseController {
    
    @Autowired
    private CourseAbilityPerfService courseAbilityPerfService;

    /** 查询课程×能力矩阵 */
    @PreAuthorize("@ss.hasPermi('graduatePerf:matrix:query')")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam Long majorId, @RequestParam Integer grade) {
        return success(courseAbilityPerfService.getMatrix(majorId, grade));
    }

    /** 新增行：课程 */
    @PreAuthorize("@ss.hasPermi('graduatePerf:matrix:addRow')")
    @Log(title = "毕业达成度矩阵-新增课程", businessType = BusinessType.INSERT)
    @PostMapping("/row")
    public AjaxResult addRow(@RequestBody GraduateCourse course) {
        return success(courseAbilityPerfService.addCourse(course));
    }

    /** 修改行：课程 */
    @PreAuthorize("@ss.hasPermi('graduatePerf:matrix:editRow')")
    @Log(title = "毕业达成度矩阵-修改课程", businessType = BusinessType.UPDATE)
    @PutMapping("/row")
    public AjaxResult editRow(@RequestBody GraduateCourse course) {
        courseAbilityPerfService.updateCourse(course);
        return success();
    }

    /** 删除行：课程 */
    @PreAuthorize("@ss.hasPermi('graduatePerf:matrix:removeRow')")
    @Log(title = "毕业达成度矩阵-删除课程", businessType = BusinessType.DELETE)
    @DeleteMapping("/row/{courseId}")
    public AjaxResult removeRow(@PathVariable Long courseId) {
        courseAbilityPerfService.deleteCourse(courseId);
        return success();
    }

    /** 新增列：能力 */
    @PreAuthorize("@ss.hasPermi('graduatePerf:matrix:addCol')")
    @Log(title = "毕业达成度矩阵-新增能力", businessType = BusinessType.INSERT)
    @PostMapping("/col")
    public AjaxResult addCol(@RequestBody GraduateAbility ability) {
        return success(courseAbilityPerfService.addAbility(ability));
    }

    /** 修改列：能力 */
    @PreAuthorize("@ss.hasPermi('graduatePerf:matrix:editCol')")
    @Log(title = "毕业达成度矩阵-修改能力", businessType = BusinessType.UPDATE)
    @PutMapping("/col")
    public AjaxResult editCol(@RequestBody GraduateAbility ability) {
        courseAbilityPerfService.updateAbility(ability);
        return success();
    }

    /** 删除列：能力 */
    @PreAuthorize("@ss.hasPermi('graduatePerf:matrix:removeCol')")
    @Log(title = "毕业达成度矩阵-删除能力", businessType = BusinessType.DELETE)
    @DeleteMapping("/col/{abilityId}")
    public AjaxResult removeCol(@PathVariable Long abilityId) {
        courseAbilityPerfService.deleteAbility(abilityId);
        return success();
    }

    /** 设置或更新单元格 */
    @PreAuthorize("@ss.hasPermi('graduatePerf:matrix:editCell')")
    @Log(title = "毕业达成度矩阵-设置单元格", businessType = BusinessType.UPDATE)
    @PostMapping("/cell")
    public AjaxResult upsertCell(@RequestBody CellDTO dto) {
        courseAbilityPerfService.upsertCell(dto.getCourseId(), dto.getAbilityId(), dto.getGrade(), dto.getLevel(), dto.getWeight(), dto.getRemark());
        return success();
    }

    /** 清除单元格 */
    @PreAuthorize("@ss.hasPermi('graduatePerf:matrix:clearCell')")
    @Log(title = "毕业达成度矩阵-清除单元格", businessType = BusinessType.DELETE)
    @DeleteMapping("/cell")
    public AjaxResult clearCell(@RequestParam Long courseId, @RequestParam Long abilityId) {
        courseAbilityPerfService.clearCell(courseId, abilityId);
        return success();
    }

    @Data
    public static class CellDTO {
        private Long courseId;
        private Long abilityId;
        private Integer grade;
        private BigDecimal level;
        private BigDecimal weight;
        private String remark;
    }
}
