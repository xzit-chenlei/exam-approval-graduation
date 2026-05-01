package edu.xzit.core.core.flowable.controller;

import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.page.TableDataInfo;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.core.core.flowable.domain.ExamWordTemplateNew;
import edu.xzit.core.core.flowable.domain.dto.ExamWordTemplateNewDto;
import edu.xzit.core.core.flowable.service.IExamWordTemplateNewRepoService;
import edu.xzit.core.service.ExamWordTemplateNewBizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * word模板主表控制器
 */
@RestController
@RequestMapping("/exam/wordTemplateNew")
public class ExamWordTemplateNewController extends BaseController {

    @Autowired
    private IExamWordTemplateNewRepoService examWordTemplateNewRepoService;

    @Autowired
    private ExamWordTemplateNewBizService examWordTemplateNewBizService;

    /**
     * 查询列表
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:list')")
    @GetMapping("/list")
    public TableDataInfo list(ExamWordTemplateNew examWordTemplateNew) {
        startPage();
        List<ExamWordTemplateNew> list = examWordTemplateNewRepoService.selectExamWordTemplateNewList(examWordTemplateNew);
        return getDataTable(list);
    }

    /**
     * 导出
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:export')")
    @Log(title = "word模板主表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExamWordTemplateNew examWordTemplateNew) {
        List<ExamWordTemplateNew> list = examWordTemplateNewRepoService.selectExamWordTemplateNewList(examWordTemplateNew);
        ExcelUtil<ExamWordTemplateNew> util = new ExcelUtil<>(ExamWordTemplateNew.class);
        util.exportExcel(response, list, "word模板主表数据");
    }

    /**
     * 详情
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return examWordTemplateNewBizService.getExamWordTemplateNewById(id);
    }

    /**
     * 新增
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:add')")
    @Log(title = "word模板主表", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ExamWordTemplateNewDto examWordTemplateNew) {
        return examWordTemplateNewBizService.addExamWordTemplateNew(examWordTemplateNew);
    }

    /**
     * 修改
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:edit')")
    @Log(title = "word模板主表", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ExamWordTemplateNewDto examWordTemplateNew) {
        return examWordTemplateNewBizService.updateExamWordTemplateNew(examWordTemplateNew);
    }

    /**
     * 删除
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:remove')")
    @Log(title = "word模板主表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(examWordTemplateNewRepoService.deleteExamWordTemplateNewByIds(ids));
    }
}

