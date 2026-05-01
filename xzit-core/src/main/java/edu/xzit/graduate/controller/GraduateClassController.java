package edu.xzit.graduate.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.page.TableDataInfo;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.graduate.dao.domain.GraduateClass;
import edu.xzit.graduate.dao.service.IGraduateClassRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 毕业达成度班级 Controller
 */
@RestController
@RequestMapping("/graduate/class")
public class GraduateClassController extends BaseController {
    @Autowired
    private IGraduateClassRepoService graduateClassService;

    /**
     * 查询毕业达成度班级列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:class:list')")
    @GetMapping("/list")
    public TableDataInfo list(GraduateClass query) {
        startPage();
        LambdaQueryWrapper<GraduateClass> wrapper = Wrappers.lambdaQuery();
        if (query.getGraduateMajorId() != null) {
            wrapper.eq(GraduateClass::getGraduateMajorId, query.getGraduateMajorId());
        }
        if (query.getClassName() != null && !query.getClassName().isEmpty()) {
            wrapper.like(GraduateClass::getClassName, query.getClassName());
        }
        if (query.getEnteringClass() != null) {
            wrapper.eq(GraduateClass::getEnteringClass, query.getEnteringClass());
        }
        wrapper.orderByDesc(GraduateClass::getId);
        List<GraduateClass> list = graduateClassService.list(wrapper);
        return getDataTable(list);
    }

    /**
     * 导出毕业达成度班级列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:class:export')")
    @Log(title = "毕业达成度班级", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GraduateClass query) {
        LambdaQueryWrapper<GraduateClass> wrapper = Wrappers.lambdaQuery();
        if (query.getGraduateMajorId() != null) {
            wrapper.eq(GraduateClass::getGraduateMajorId, query.getGraduateMajorId());
        }
        if (query.getClassName() != null && !query.getClassName().isEmpty()) {
            wrapper.like(GraduateClass::getClassName, query.getClassName());
        }
        if (query.getEnteringClass() != null) {
            wrapper.eq(GraduateClass::getEnteringClass, query.getEnteringClass());
        }
        wrapper.orderByDesc(GraduateClass::getId);
        List<GraduateClass> list = graduateClassService.list(wrapper);
        ExcelUtil<GraduateClass> util = new ExcelUtil<>(GraduateClass.class);
        util.exportExcel(response, list, "毕业达成度班级数据");
    }

    /**
     * 获取毕业达成度班级详细信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:class:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(graduateClassService.getById(id));
    }

    /**
     * 新增毕业达成度班级
     */
    @PreAuthorize("@ss.hasPermi('graduate:class:add')")
    @Log(title = "毕业达成度班级", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GraduateClass data) {
        return toAjax(graduateClassService.save(data) ? 1 : 0);
    }

    /**
     * 修改毕业达成度班级
     */
    @PreAuthorize("@ss.hasPermi('graduate:class:edit')")
    @Log(title = "毕业达成度班级", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GraduateClass data) {
        return toAjax(graduateClassService.updateById(data) ? 1 : 0);
    }

    /**
     * 删除毕业达成度班级
     */
    @PreAuthorize("@ss.hasPermi('graduate:class:remove')")
    @Log(title = "毕业达成度班级", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(graduateClassService.removeByIds(Arrays.asList(ids)) ? 1 : 0);
    }
}


