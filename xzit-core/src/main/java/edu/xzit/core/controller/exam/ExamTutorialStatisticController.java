package edu.xzit.core.controller.exam;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import edu.xzit.core.dao.domain.ExamTutorialStatistic;
import edu.xzit.core.dao.service.IExamTutorialStatisticRepoService;
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
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.core.core.common.core.page.TableDataInfo;

/**
 * 辅导答疑统计Controller
 *
 * @author chenlei
 * @date 2025-03-09
 */
@RestController
@RequestMapping("/exam/tutorialStatistic")
public class ExamTutorialStatisticController extends BaseController {
    @Autowired
    private IExamTutorialStatisticRepoService examTutorialStatisticService;

/**
 * 查询辅导答疑统计列表
 */
@PreAuthorize("@ss.hasPermi('exam:tutorialStatistic:list')")
@GetMapping("/list")
    public TableDataInfo list(ExamTutorialStatistic examTutorialStatistic) {
        startPage();
        List<ExamTutorialStatistic> list = examTutorialStatisticService.selectExamTutorialStatisticList(examTutorialStatistic);
        return getDataTable(list);
    }

    /**
     * 导出辅导答疑统计列表
     */
    @PreAuthorize("@ss.hasPermi('exam:listenClassPlan:export')")
    @Log(title = "辅导答疑统计", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExamTutorialStatistic examTutorialStatistic) {
        List<ExamTutorialStatistic> list = examTutorialStatisticService.selectExamTutorialStatisticList(examTutorialStatistic);
        ExcelUtil<ExamTutorialStatistic> util = new ExcelUtil<ExamTutorialStatistic>(ExamTutorialStatistic. class);
        util.exportExcel(response, list, "辅导答疑统计数据");
    }

    /**
     * 获取辅导答疑统计详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:listenClassPlan:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(examTutorialStatisticService.selectExamTutorialStatisticById(id));
    }

    /**
     * 新增辅导答疑统计
     */
    @PreAuthorize("@ss.hasPermi('exam:listenClassPlan:add')")
    @Log(title = "辅导答疑统计", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ExamTutorialStatistic examTutorialStatistic) {
        return toAjax(examTutorialStatisticService.insertExamTutorialStatistic(examTutorialStatistic));
    }

    /**
     * 修改辅导答疑统计
     */
    @PreAuthorize("@ss.hasPermi('exam:listenClassPlan:edit')")
    @Log(title = "辅导答疑统计", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ExamTutorialStatistic examTutorialStatistic) {
        return toAjax(examTutorialStatisticService.updateExamTutorialStatistic(examTutorialStatistic));
    }

    /**
     * 删除辅导答疑统计
     */
    @PreAuthorize("@ss.hasPermi('exam:listenClassPlan:remove')")
    @Log(title = "辅导答疑统计", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(examTutorialStatisticService.deleteExamTutorialStatisticByIds(ids));
    }
}
