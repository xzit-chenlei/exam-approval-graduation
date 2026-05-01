package edu.xzit.graduate.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.graduate.dao.domain.GraduatePerformance;
import edu.xzit.graduate.dao.service.IGraduatePerformanceRepoService;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.core.core.common.core.page.TableDataInfo;

/**
 * 毕业达成度平时测验Controller
 *
 * @author vikTor
 * @date 2025-09-16
 */
@RestController
@RequestMapping("/graduate/performance")
public class GraduatePerformanceController extends BaseController {
    @Autowired
    private IGraduatePerformanceRepoService graduatePerformanceService;

/**
 * 查询毕业达成度平时测验列表
 */
@PreAuthorize("@ss.hasPermi('graduate:performance:list')")
@GetMapping("/list")
    public TableDataInfo list(GraduatePerformance graduatePerformance) {
        startPage();
        List<GraduatePerformance> list = graduatePerformanceService.selectGraduatePerformanceList(graduatePerformance);
        return getDataTable(list);
    }

    /**
     * 导出毕业达成度平时测验列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:performance:export')")
    @Log(title = "毕业达成度平时测验", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GraduatePerformance graduatePerformance) {
        List<GraduatePerformance> list = graduatePerformanceService.selectGraduatePerformanceList(graduatePerformance);
        ExcelUtil<GraduatePerformance> util = new ExcelUtil<GraduatePerformance>(GraduatePerformance. class);
        util.exportExcel(response, list, "毕业达成度平时测验数据");
    }

    /**
     * 获取毕业达成度平时测验详细信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:performance:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(graduatePerformanceService.selectGraduatePerformanceById(id));
    }

    /**
     * 新增毕业达成度平时测验
     */
    @PreAuthorize("@ss.hasPermi('graduate:performance:add')")
    @Log(title = "毕业达成度平时测验", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GraduatePerformance graduatePerformance) {
        return toAjax(graduatePerformanceService.insertGraduatePerformance(graduatePerformance));
    }

    /**
     * 修改毕业达成度平时测验
     */
    @PreAuthorize("@ss.hasPermi('graduate:performance:edit')")
    @Log(title = "毕业达成度平时测验", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GraduatePerformance graduatePerformance) {
        return toAjax(graduatePerformanceService.updateGraduatePerformance(graduatePerformance));
    }

    /**
     * 删除毕业达成度平时测验
     */
    @PreAuthorize("@ss.hasPermi('graduate:performance:remove')")
    @Log(title = "毕业达成度平时测验", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(graduatePerformanceService.deleteGraduatePerformanceByIds(ids));
    }
}
