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
import edu.xzit.graduate.dao.domain.GraduateLab;
import edu.xzit.graduate.dao.service.IGraduateLabRepoService;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.core.core.common.core.page.TableDataInfo;

/**
 * 毕业达成度实验成绩信息Controller
 *
 * @author vikTor
 * @date 2025-09-16
 */
@RestController
@RequestMapping("/graduate/lab")
public class GraduateLabController extends BaseController {
    @Autowired
    private IGraduateLabRepoService graduateLabService;

/**
 * 查询毕业达成度实验成绩信息列表
 */
@PreAuthorize("@ss.hasPermi('graduate:lab:list')")
@GetMapping("/list")
    public TableDataInfo list(GraduateLab graduateLab) {
        startPage();
        List<GraduateLab> list = graduateLabService.selectGraduateLabList(graduateLab);
        return getDataTable(list);
    }

    /**
     * 导出毕业达成度实验成绩信息列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:lab:export')")
    @Log(title = "毕业达成度实验成绩信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GraduateLab graduateLab) {
        List<GraduateLab> list = graduateLabService.selectGraduateLabList(graduateLab);
        ExcelUtil<GraduateLab> util = new ExcelUtil<GraduateLab>(GraduateLab. class);
        util.exportExcel(response, list, "毕业达成度实验成绩信息数据");
    }

    /**
     * 获取毕业达成度实验成绩信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:lab:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(graduateLabService.selectGraduateLabById(id));
    }

    /**
     * 新增毕业达成度实验成绩信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:lab:add')")
    @Log(title = "毕业达成度实验成绩信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GraduateLab graduateLab) {
        return toAjax(graduateLabService.insertGraduateLab(graduateLab));
    }

    /**
     * 修改毕业达成度实验成绩信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:lab:edit')")
    @Log(title = "毕业达成度实验成绩信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GraduateLab graduateLab) {
        return toAjax(graduateLabService.updateGraduateLab(graduateLab));
    }

    /**
     * 删除毕业达成度实验成绩信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:lab:remove')")
    @Log(title = "毕业达成度实验成绩信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(graduateLabService.deleteGraduateLabByIds(ids));
    }
}
