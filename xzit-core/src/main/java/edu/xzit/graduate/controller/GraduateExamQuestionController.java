package edu.xzit.graduate.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.page.TableDataInfo;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.graduate.dao.domain.GraduateExamQuestion;
import edu.xzit.graduate.dao.service.IGraduateExamQuestionRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 毕业达成度试卷题目 Controller
 *
 * @author system
 * @date 2025-01-22
 */
@RestController
@RequestMapping("/graduate/examQuestion")
public class GraduateExamQuestionController extends BaseController {
    
    @Autowired
    private IGraduateExamQuestionRepoService graduateExamQuestionRepoService;

    /**
     * 查询试卷题目列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:examQuestion:list')")
    @GetMapping("/list")
    public TableDataInfo list(GraduateExamQuestion graduateExamQuestion) {
        startPage();
        LambdaQueryWrapper<GraduateExamQuestion> wrapper = Wrappers.lambdaQuery();
        
        if (graduateExamQuestion.getPaperId() != null) {
            wrapper.eq(GraduateExamQuestion::getPaperId, graduateExamQuestion.getPaperId());
        }
        if (graduateExamQuestion.getQuestionNo() != null && !graduateExamQuestion.getQuestionNo().isEmpty()) {
            wrapper.like(GraduateExamQuestion::getQuestionNo, graduateExamQuestion.getQuestionNo());
        }
        if (graduateExamQuestion.getQuestionType() != null && !graduateExamQuestion.getQuestionType().isEmpty()) {
            wrapper.eq(GraduateExamQuestion::getQuestionType, graduateExamQuestion.getQuestionType());
        }
        if (graduateExamQuestion.getExamPurpose() != null && !graduateExamQuestion.getExamPurpose().isEmpty()) {
            wrapper.like(GraduateExamQuestion::getExamPurpose, graduateExamQuestion.getExamPurpose());
        }
        if (graduateExamQuestion.getDifficulty() != null && !graduateExamQuestion.getDifficulty().isEmpty()) {
            wrapper.eq(GraduateExamQuestion::getDifficulty, graduateExamQuestion.getDifficulty());
        }
        
        wrapper.orderByAsc(GraduateExamQuestion::getOrderNo, GraduateExamQuestion::getId);
        List<GraduateExamQuestion> list = graduateExamQuestionRepoService.list(wrapper);
        return getDataTable(list);
    }

    /**
     * 导出试卷题目列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:examQuestion:export')")
    @Log(title = "试卷题目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GraduateExamQuestion graduateExamQuestion) {
        LambdaQueryWrapper<GraduateExamQuestion> wrapper = Wrappers.lambdaQuery();
        
        if (graduateExamQuestion.getPaperId() != null) {
            wrapper.eq(GraduateExamQuestion::getPaperId, graduateExamQuestion.getPaperId());
        }
        if (graduateExamQuestion.getQuestionType() != null && !graduateExamQuestion.getQuestionType().isEmpty()) {
            wrapper.eq(GraduateExamQuestion::getQuestionType, graduateExamQuestion.getQuestionType());
        }
        
        wrapper.orderByAsc(GraduateExamQuestion::getOrderNo, GraduateExamQuestion::getId);
        List<GraduateExamQuestion> list = graduateExamQuestionRepoService.list(wrapper);
        ExcelUtil<GraduateExamQuestion> util = new ExcelUtil<GraduateExamQuestion>(GraduateExamQuestion.class);
        util.exportExcel(response, list, "试卷题目数据");
    }

    /**
     * 获取试卷题目详细信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:examQuestion:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(graduateExamQuestionRepoService.getById(id));
    }

    /**
     * 新增试卷题目
     */
    @PreAuthorize("@ss.hasPermi('graduate:examQuestion:add')")
    @Log(title = "试卷题目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GraduateExamQuestion graduateExamQuestion) {
        graduateExamQuestionRepoService.save(graduateExamQuestion);
        return success();
    }

    /**
     * 修改试卷题目
     */
    @PreAuthorize("@ss.hasPermi('graduate:examQuestion:edit')")
    @Log(title = "试卷题目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GraduateExamQuestion graduateExamQuestion) {
        graduateExamQuestionRepoService.updateById(graduateExamQuestion);
        return success();
    }

    /**
     * 删除试卷题目
     */
    @PreAuthorize("@ss.hasPermi('graduate:examQuestion:remove')")
    @Log(title = "试卷题目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        graduateExamQuestionRepoService.removeByIds(Arrays.asList(ids));
        return success();
    }
}