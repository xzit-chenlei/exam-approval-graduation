package edu.xzit.core.core.flowable.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import edu.xzit.core.core.flowable.domain.ExamWordTemplateInfo;
import edu.xzit.core.core.flowable.domain.dto.ExamWordTemplateInfoDto;
import edu.xzit.core.core.flowable.service.IExamWordTemplateInfoRepoService;
import edu.xzit.core.service.ExamWordTemplateInfoBizService;
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
 * word模板信息Controller
 *
 * @author chenlei
 * @date 2025-03-09
 */
@RestController
@RequestMapping("/exam/wordTemplateInfo")
public class ExamWordTemplateInfoController extends BaseController {
    @Autowired
    private IExamWordTemplateInfoRepoService examWordTemplateInfoService;

    @Autowired
    private ExamWordTemplateInfoBizService examWordTemplateInfoBizService;

/**
 * 查询word模板信息列表
 */
@PreAuthorize("@ss.hasPermi('flowable:form:list')")
@GetMapping("/list")
    public TableDataInfo list(ExamWordTemplateInfo examWordTemplateInfo) {
        startPage();
        List<ExamWordTemplateInfo> list = examWordTemplateInfoService.selectExamWordTemplateInfoList(examWordTemplateInfo);
        return getDataTable(list);
    }

    /**
     * 导出word模板信息列表
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:export')")
    @Log(title = "word模板信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExamWordTemplateInfo examWordTemplateInfo) {
        List<ExamWordTemplateInfo> list = examWordTemplateInfoService.selectExamWordTemplateInfoList(examWordTemplateInfo);
        ExcelUtil<ExamWordTemplateInfo> util = new ExcelUtil<ExamWordTemplateInfo>(ExamWordTemplateInfo. class);
        util.exportExcel(response, list, "word模板信息数据");
    }

    /**
     * 获取word模板信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return examWordTemplateInfoBizService.getExamWordTemplateInfoById(id);
    }

    /**
     * 新增word模板信息
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:add')")
    @Log(title = "word模板信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ExamWordTemplateInfoDto examWordTemplateInfo) {
        return examWordTemplateInfoBizService.addExamWordTemplateInfo(examWordTemplateInfo);
    }

    /**
     * 修改word模板信息
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:edit')")
    @Log(title = "word模板信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ExamWordTemplateInfoDto examWordTemplateInfo) {
        return examWordTemplateInfoBizService.updateExamWordTemplateInfo(examWordTemplateInfo);
    }

    /**
     * 删除word模板信息
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:remove')")
    @Log(title = "word模板信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(examWordTemplateInfoService.deleteExamWordTemplateInfoByIds(ids));
    }
}
