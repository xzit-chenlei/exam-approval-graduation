package edu.xzit.graduate.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import edu.xzit.graduate.dao.domain.GraduateExamPaper;
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
import edu.xzit.graduate.dao.domain.GraduateCourseWeight;
import edu.xzit.graduate.dao.service.IGraduateCourseWeightRepoService;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.core.core.common.core.page.TableDataInfo;

/**
 * 毕业达成度各课程三类考试权重Controller
 *
 * @author chenlei
 * @date 2025-09-30
 */
@RestController
@RequestMapping("/graduate/weight")
public class GraduateCourseWeightController extends BaseController {
    @Autowired
    private IGraduateCourseWeightRepoService graduateCourseWeightService;

/**
 * 查询毕业达成度各课程三类考试权重列表
 */
@PreAuthorize("@ss.hasPermi('graduate:weight:list')")
@GetMapping("/list")
public TableDataInfo list(GraduateCourseWeight graduateCourseWeight) {
    startPage();
    LambdaQueryWrapper<GraduateCourseWeight> wrapper = Wrappers.lambdaQuery();
    if (graduateCourseWeight.getCourseId() != null) {
        wrapper.eq(GraduateCourseWeight::getCourseId, graduateCourseWeight.getCourseId());
    }
    List<GraduateCourseWeight> list = graduateCourseWeightService.list(wrapper);
    return getDataTable(list);
}

    /**
     * 导出毕业达成度各课程三类考试权重列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:weight:export')")
    @Log(title = "毕业达成度各课程三类考试权重", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GraduateCourseWeight graduateCourseWeight) {
        List<GraduateCourseWeight> list = graduateCourseWeightService.selectGraduateCourseWeightList(graduateCourseWeight);
        ExcelUtil<GraduateCourseWeight> util = new ExcelUtil<GraduateCourseWeight>(GraduateCourseWeight. class);
        util.exportExcel(response, list, "毕业达成度各课程三类考试权重数据");
    }

    /**
     * 获取毕业达成度各课程三类考试权重详细信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:weight:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(graduateCourseWeightService.selectGraduateCourseWeightById(id));
    }

    /**
     * 新增毕业达成度各课程三类考试权重
     */
    @PreAuthorize("@ss.hasPermi('graduate:weight:add')")
    @Log(title = "毕业达成度各课程三类考试权重", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GraduateCourseWeight graduateCourseWeight) {
        return toAjax(graduateCourseWeightService.insertGraduateCourseWeight(graduateCourseWeight));
    }

    /**
     * 修改毕业达成度各课程三类考试权重
     */
    @PreAuthorize("@ss.hasPermi('graduate:weight:edit')")
    @Log(title = "毕业达成度各课程三类考试权重", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GraduateCourseWeight graduateCourseWeight) {
        return toAjax(graduateCourseWeightService.updateGraduateCourseWeight(graduateCourseWeight));
    }

    /**
     * 删除毕业达成度各课程三类考试权重
     */
    @PreAuthorize("@ss.hasPermi('graduate:weight:remove')")
    @Log(title = "毕业达成度各课程三类考试权重", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(graduateCourseWeightService.deleteGraduateCourseWeightByIds(ids));
    }
}
