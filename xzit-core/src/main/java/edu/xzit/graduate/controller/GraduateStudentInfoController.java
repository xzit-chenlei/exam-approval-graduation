package edu.xzit.graduate.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.page.TableDataInfo;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.graduate.dao.domain.GraduateStudentInfo;
import edu.xzit.graduate.dao.service.IGraduateStudentInfoRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生信息表 Controller
 *
 * @author chenlei
 * @date 2025-01-27
 */
@RestController
@RequestMapping("/graduate/studentInfo")
public class GraduateStudentInfoController extends BaseController {
    @Autowired
    private IGraduateStudentInfoRepoService graduateStudentInfoService;

    @Autowired
    private edu.xzit.graduate.dao.service.IGraduateClassRepoService graduateClassService;

    /**
     * 查询学生信息表列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentInfo:list')")
    @GetMapping("/list")
    public TableDataInfo list(GraduateStudentInfo graduateStudentInfo,
                              @RequestParam(value = "grade", required = false) Integer grade,
                              @RequestParam(value = "majorId", required = false) Long majorId) {
        startPage();
        LambdaQueryWrapper<GraduateStudentInfo> wrapper = Wrappers.lambdaQuery();
        if (graduateStudentInfo.getStudentCode() != null) {
            wrapper.eq(GraduateStudentInfo::getStudentCode, graduateStudentInfo.getStudentCode());
        }
        if (graduateStudentInfo.getStudentName() != null && !graduateStudentInfo.getStudentName().isEmpty()) {
            wrapper.like(GraduateStudentInfo::getStudentName, graduateStudentInfo.getStudentName());
        }
        if (graduateStudentInfo.getClassId() != null) {
            wrapper.eq(GraduateStudentInfo::getClassId, graduateStudentInfo.getClassId());
        }
        if (graduateStudentInfo.getClassName() != null && !graduateStudentInfo.getClassName().isEmpty()) {
            wrapper.like(GraduateStudentInfo::getClassName, graduateStudentInfo.getClassName());
        }
        // 按年级/专业过滤（通过班级表映射到 classId）
        if (grade != null || majorId != null) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<edu.xzit.graduate.dao.domain.GraduateClass> classWrapper = Wrappers.lambdaQuery();
            if (grade != null) {
                classWrapper.eq(edu.xzit.graduate.dao.domain.GraduateClass::getEnteringClass, grade);
            }
            if (majorId != null) {
                classWrapper.eq(edu.xzit.graduate.dao.domain.GraduateClass::getGraduateMajorId, majorId);
            }
            java.util.List<edu.xzit.graduate.dao.domain.GraduateClass> classes = graduateClassService.list(classWrapper);
            if (classes != null && !classes.isEmpty()) {
                java.util.List<Long> classIds = new java.util.ArrayList<>();
                for (edu.xzit.graduate.dao.domain.GraduateClass c : classes) {
                    if (c.getId() != null) classIds.add(c.getId());
                }
                if (!classIds.isEmpty()) {
                    wrapper.in(GraduateStudentInfo::getClassId, classIds);
                } else {
                    return getDataTable(java.util.Collections.emptyList());
                }
            } else {
                return getDataTable(java.util.Collections.emptyList());
            }
        }

        List<GraduateStudentInfo> list = graduateStudentInfoService.list(wrapper);
        return getDataTable(list);
    }

    /**
     * 导出学生信息表列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentInfo:export')")
    @Log(title = "学生信息表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response,
                       GraduateStudentInfo graduateStudentInfo,
                       @RequestParam(value = "grade", required = false) Integer grade,
                       @RequestParam(value = "majorId", required = false) Long majorId) {
        LambdaQueryWrapper<GraduateStudentInfo> wrapper = Wrappers.lambdaQuery();
        if (graduateStudentInfo.getStudentCode() != null) {
            wrapper.eq(GraduateStudentInfo::getStudentCode, graduateStudentInfo.getStudentCode());
        }
        if (graduateStudentInfo.getStudentName() != null && !graduateStudentInfo.getStudentName().isEmpty()) {
            wrapper.like(GraduateStudentInfo::getStudentName, graduateStudentInfo.getStudentName());
        }
        if (graduateStudentInfo.getClassId() != null) {
            wrapper.eq(GraduateStudentInfo::getClassId, graduateStudentInfo.getClassId());
        }
        if (graduateStudentInfo.getClassName() != null && !graduateStudentInfo.getClassName().isEmpty()) {
            wrapper.like(GraduateStudentInfo::getClassName, graduateStudentInfo.getClassName());
        }
        if (grade != null || majorId != null) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<edu.xzit.graduate.dao.domain.GraduateClass> classWrapper = Wrappers.lambdaQuery();
            if (grade != null) {
                classWrapper.eq(edu.xzit.graduate.dao.domain.GraduateClass::getEnteringClass, grade);
            }
            if (majorId != null) {
                classWrapper.eq(edu.xzit.graduate.dao.domain.GraduateClass::getGraduateMajorId, majorId);
            }
            java.util.List<edu.xzit.graduate.dao.domain.GraduateClass> classes = graduateClassService.list(classWrapper);
            if (classes != null && !classes.isEmpty()) {
                java.util.List<Long> classIds = new java.util.ArrayList<>();
                for (edu.xzit.graduate.dao.domain.GraduateClass c : classes) {
                    if (c.getId() != null) classIds.add(c.getId());
                }
                if (!classIds.isEmpty()) {
                    wrapper.in(GraduateStudentInfo::getClassId, classIds);
                } else {
                    ExcelUtil<GraduateStudentInfo> utilEmpty = new ExcelUtil<>(GraduateStudentInfo.class);
                    utilEmpty.exportExcel(response, java.util.Collections.emptyList(), "学生信息表数据");
                    return;
                }
            } else {
                ExcelUtil<GraduateStudentInfo> utilEmpty = new ExcelUtil<>(GraduateStudentInfo.class);
                utilEmpty.exportExcel(response, java.util.Collections.emptyList(), "学生信息表数据");
                return;
            }
        }
        List<GraduateStudentInfo> list = graduateStudentInfoService.list(wrapper);
        ExcelUtil<GraduateStudentInfo> util = new ExcelUtil<>(GraduateStudentInfo.class);
        util.exportExcel(response, list, "学生信息表数据");
    }

    /**
     * 获取学生信息表详细信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentInfo:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(graduateStudentInfoService.getById(id));
    }

    /**
     * 新增学生信息表
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentInfo:add')")
    @Log(title = "学生信息表", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GraduateStudentInfo graduateStudentInfo) {
        return toAjax(graduateStudentInfoService.save(graduateStudentInfo) ? 1 : 0);
    }

    /**
     * 修改学生信息表
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentInfo:edit')")
    @Log(title = "学生信息表", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GraduateStudentInfo graduateStudentInfo) {
        return toAjax(graduateStudentInfoService.updateById(graduateStudentInfo) ? 1 : 0);
    }

    /**
     * 删除学生信息表
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentInfo:remove')")
    @Log(title = "学生信息表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(graduateStudentInfoService.removeByIds(Arrays.asList(ids)) ? 1 : 0);
    }

    /**
     * 学生信息导入
     * 模板中不包含班级字段，使用当前筛选条件传入的 classId 统一录入班级
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentInfo:import')")
    @Log(title = "学生信息表", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "classId", required = false) Long classId) throws Exception {
        ExcelUtil<GraduateStudentInfo> util = new ExcelUtil<>(GraduateStudentInfo.class);
        List<GraduateStudentInfo> list = util.importExcel(file.getInputStream());

        String className = null;
        if (classId != null) {
            edu.xzit.graduate.dao.domain.GraduateClass cls = graduateClassService.getById(classId);
            className = cls != null ? cls.getClassName() : null;
        }

        final String classNameFinal = className;
        List<GraduateStudentInfo> toSave = list.stream().map(item -> {
            item.setId(null);
            item.setClassId(classId);
            item.setClassName(classNameFinal);
            return item;
        }).collect(Collectors.toList());

        boolean ok = graduateStudentInfoService.saveBatch(toSave);
        return ok ? success("导入成功：" + toSave.size() + " 条") : AjaxResult.error("导入失败");
    }

    /**
     * 下载学生信息导入模板（不含班级字段）
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil<GraduateStudentInfo> util = new ExcelUtil<>(GraduateStudentInfo.class);
        util.importTemplateExcel(response, "学生信息导入模板");
    }
}

