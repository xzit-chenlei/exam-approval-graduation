package edu.xzit.core.controller.exam;

import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.page.TableDataInfo;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.core.dao.domain.ExamListenClassPlan;
import edu.xzit.core.dao.service.IExamListenClassPlanRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 听课计划Controller
 *
 * @author chenlei
 * @date 2025-03-09
 */
@RestController
@RequestMapping("/exam/listenClassPlan")
public class ExamListenClassPlanController extends BaseController {
    @Autowired
    private IExamListenClassPlanRepoService examListenClassPlanService;

/**
 * 查询听课计划列表
 */
@PreAuthorize("@ss.hasPermi('exam:listenClassPlan:list')")
@GetMapping("/list")
    public TableDataInfo list(ExamListenClassPlan examListenClassPlan) {
        startPage();
        List<ExamListenClassPlan> list = examListenClassPlanService.selectExamListenClassPlanList(examListenClassPlan);
        return getDataTable(list);
    }

    /**
     * 导出听课计划列表
     */
    @PreAuthorize("@ss.hasPermi('exam:listenClassPlan:export')")
    @Log(title = "听课计划", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExamListenClassPlan examListenClassPlan) {
        List<ExamListenClassPlan> list = examListenClassPlanService.selectExamListenClassPlanList(examListenClassPlan);
        ExcelUtil<ExamListenClassPlan> util = new ExcelUtil<>(ExamListenClassPlan.class);
        util.exportExcel(response, list, "听课计划数据");
    }

    /**
     * 获取听课计划详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:listenClassPlan:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(examListenClassPlanService.selectExamListenClassPlanById(id));
    }

    /**
     * 新增听课计划
     */
    @PreAuthorize("@ss.hasPermi('exam:listenClassPlan:add')")
    @Log(title = "听课计划", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ExamListenClassPlan examListenClassPlan) {
        return toAjax(examListenClassPlanService.insertExamListenClassPlan(examListenClassPlan));
    }

    /**
     * 修改听课计划
     */
    @PreAuthorize("@ss.hasPermi('exam:listenClassPlan:edit')")
    @Log(title = "听课计划", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ExamListenClassPlan examListenClassPlan) {
        return toAjax(examListenClassPlanService.updateExamListenClassPlan(examListenClassPlan));
    }

    /**
     * 删除听课计划
     */
    @PreAuthorize("@ss.hasPermi('exam:listenClassPlan:remove')")
    @Log(title = "听课计划", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(examListenClassPlanService.deleteExamListenClassPlanByIds(ids));
    }
}
