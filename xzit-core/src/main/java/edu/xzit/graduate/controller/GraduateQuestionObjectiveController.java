package edu.xzit.graduate.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.page.TableDataInfo;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.graduate.dao.domain.GraduateQuestionObjective;
import edu.xzit.graduate.dao.service.IGraduateQuestionObjectiveRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 毕业达成度题目与课程目标关联 Controller
 *
 * @author system
 * @date 2025-01-22
 */
@RestController
@RequestMapping("/graduate/questionObjective")
public class GraduateQuestionObjectiveController extends BaseController {
    
    @Autowired
    private IGraduateQuestionObjectiveRepoService graduateQuestionObjectiveRepoService;

    /**
     * 查询题目与课程目标关联列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:questionObjective:list')")
    @GetMapping("/list")
    public TableDataInfo list(GraduateQuestionObjective graduateQuestionObjective) {
        startPage();
        LambdaQueryWrapper<GraduateQuestionObjective> wrapper = Wrappers.lambdaQuery();
        
        if (graduateQuestionObjective.getQuestionId() != null) {
            wrapper.eq(GraduateQuestionObjective::getQuestionId, graduateQuestionObjective.getQuestionId());
        }
        if (graduateQuestionObjective.getObjectiveId() != null) {
            wrapper.eq(GraduateQuestionObjective::getObjectiveId, graduateQuestionObjective.getObjectiveId());
        }
        if (graduateQuestionObjective.getAssessmentMethod() != null && !graduateQuestionObjective.getAssessmentMethod().isEmpty()) {
            wrapper.like(GraduateQuestionObjective::getAssessmentMethod, graduateQuestionObjective.getAssessmentMethod());
        }
        
        wrapper.orderByAsc(GraduateQuestionObjective::getId);
        List<GraduateQuestionObjective> list = graduateQuestionObjectiveRepoService.list(wrapper);
        return getDataTable(list);
    }

    /**
     * 导出题目与课程目标关联列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:questionObjective:export')")
    @Log(title = "题目与课程目标关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GraduateQuestionObjective graduateQuestionObjective) {
        LambdaQueryWrapper<GraduateQuestionObjective> wrapper = Wrappers.lambdaQuery();
        
        if (graduateQuestionObjective.getQuestionId() != null) {
            wrapper.eq(GraduateQuestionObjective::getQuestionId, graduateQuestionObjective.getQuestionId());
        }
        if (graduateQuestionObjective.getObjectiveId() != null) {
            wrapper.eq(GraduateQuestionObjective::getObjectiveId, graduateQuestionObjective.getObjectiveId());
        }
        
        wrapper.orderByAsc(GraduateQuestionObjective::getId);
        List<GraduateQuestionObjective> list = graduateQuestionObjectiveRepoService.list(wrapper);
        ExcelUtil<GraduateQuestionObjective> util = new ExcelUtil<GraduateQuestionObjective>(GraduateQuestionObjective.class);
        util.exportExcel(response, list, "题目与课程目标关联数据");
    }

    /**
     * 获取题目与课程目标关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:questionObjective:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(graduateQuestionObjectiveRepoService.getById(id));
    }

    /**
     * 新增题目与课程目标关联
     */
    @PreAuthorize("@ss.hasPermi('graduate:questionObjective:add')")
    @Log(title = "题目与课程目标关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GraduateQuestionObjective graduateQuestionObjective) {
        graduateQuestionObjectiveRepoService.save(graduateQuestionObjective);
        return success();
    }

    /**
     * 修改题目与课程目标关联
     */
    @PreAuthorize("@ss.hasPermi('graduate:questionObjective:edit')")
    @Log(title = "题目与课程目标关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GraduateQuestionObjective graduateQuestionObjective) {
        graduateQuestionObjectiveRepoService.updateById(graduateQuestionObjective);
        return success();
    }

    /**
     * 删除题目与课程目标关联
     */
    @PreAuthorize("@ss.hasPermi('graduate:questionObjective:remove')")
    @Log(title = "题目与课程目标关联", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        graduateQuestionObjectiveRepoService.removeByIds(Arrays.asList(ids));
        return success();
    }
}