package edu.xzit.core.core.flowable.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.core.core.flowable.domain.ExamSignLocationInfo;
import edu.xzit.core.core.flowable.service.IExamSignLocationInfoRepoService;
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
import edu.xzit.core.core.common.core.page.TableDataInfo;

/**
 * 签名位置信息Controller
 *
 * @author chenlei
 * @date 2025-03-05
 */
@RestController
@RequestMapping("/exam/sign")
public class ExamSignLocationInfoController extends BaseController {
    @Autowired
    private IExamSignLocationInfoRepoService examSignLocationInfoService;

/**
 * 查询签名位置信息列表
 */
@PreAuthorize("@ss.hasPermi('flowable:form:list')")
@GetMapping("/list")
    public TableDataInfo list(ExamSignLocationInfo examSignLocationInfo) {
        startPage();
        List<ExamSignLocationInfo> list = examSignLocationInfoService.selectExamSignLocationInfoList(examSignLocationInfo);
        return getDataTable(list);
    }

    /**
     * 导出签名位置信息列表
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:export')")
    @Log(title = "签名位置信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExamSignLocationInfo examSignLocationInfo) {
        List<ExamSignLocationInfo> list = examSignLocationInfoService.selectExamSignLocationInfoList(examSignLocationInfo);
        ExcelUtil<ExamSignLocationInfo> util = new ExcelUtil<ExamSignLocationInfo>(ExamSignLocationInfo. class);
        util.exportExcel(response, list, "签名位置信息数据");
    }

    /**
     * 获取签名位置信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(examSignLocationInfoService.selectExamSignLocationInfoById(id));
    }

    /**
     * 新增签名位置信息
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:add')")
    @Log(title = "签名位置信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ExamSignLocationInfo examSignLocationInfo) {
        return toAjax(examSignLocationInfoService.insertExamSignLocationInfo(examSignLocationInfo));
    }

    /**
     * 修改签名位置信息
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:edit')")
    @Log(title = "签名位置信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ExamSignLocationInfo examSignLocationInfo) {
        return toAjax(examSignLocationInfoService.updateExamSignLocationInfo(examSignLocationInfo));
    }

    /**
     * 删除签名位置信息
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:remove')")
    @Log(title = "签名位置信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(examSignLocationInfoService.deleteExamSignLocationInfoByIds(ids));
    }
}
