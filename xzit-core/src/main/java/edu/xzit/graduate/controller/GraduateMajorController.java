package edu.xzit.graduate.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.page.TableDataInfo;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.graduate.dao.domain.GraduateMajor;
import edu.xzit.graduate.dao.service.IGraduateMajorRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 毕业达成度专业 Controller
 *
 * @author chenlei
 * @date 2025-08-25
 */
@RestController
@RequestMapping("/graduate/major")
public class GraduateMajorController extends BaseController {
    @Autowired
    private IGraduateMajorRepoService graduateMajorService;

    /**
     * 查询毕业达成度专业列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:major:list')")
    @GetMapping("/list")
    public TableDataInfo list(GraduateMajor graduateMajor) {
        startPage();
        LambdaQueryWrapper<GraduateMajor> wrapper = Wrappers.lambdaQuery();
        if (graduateMajor.getName() != null && !graduateMajor.getName().isEmpty()) {
            wrapper.like(GraduateMajor::getName, graduateMajor.getName());
        }
        List<GraduateMajor> list = graduateMajorService.list(wrapper);
        return getDataTable(list);
    }

    /**
     * 导出毕业达成度专业列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:major:export')")
    @Log(title = "毕业达成度专业", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GraduateMajor graduateMajor) {
        LambdaQueryWrapper<GraduateMajor> wrapper = Wrappers.lambdaQuery();
        if (graduateMajor.getName() != null && !graduateMajor.getName().isEmpty()) {
            wrapper.like(GraduateMajor::getName, graduateMajor.getName());
        }
        List<GraduateMajor> list = graduateMajorService.list(wrapper);
        ExcelUtil<GraduateMajor> util = new ExcelUtil<>(GraduateMajor.class);
        util.exportExcel(response, list, "毕业达成度专业数据");
    }

    /**
     * 获取毕业达成度专业详细信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:major:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(graduateMajorService.getById(id));
    }

    /**
     * 新增毕业达成度专业
     */
    @PreAuthorize("@ss.hasPermi('graduate:major:add')")
    @Log(title = "毕业达成度专业", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GraduateMajor graduateMajor) {
        return toAjax(graduateMajorService.save(graduateMajor) ? 1 : 0);
    }

    /**
     * 修改毕业达成度专业
     */
    @PreAuthorize("@ss.hasPermi('graduate:major:edit')")
    @Log(title = "毕业达成度专业", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GraduateMajor graduateMajor) {
        return toAjax(graduateMajorService.updateById(graduateMajor) ? 1 : 0);
    }

    /**
     * 删除毕业达成度专业
     */
    @PreAuthorize("@ss.hasPermi('graduate:major:remove')")
    @Log(title = "毕业达成度专业", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(graduateMajorService.removeByIds(Arrays.asList(ids)) ? 1 : 0);
    }
}

