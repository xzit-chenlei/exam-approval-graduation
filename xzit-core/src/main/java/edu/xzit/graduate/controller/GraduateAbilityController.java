package edu.xzit.graduate.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.page.TableDataInfo;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.graduate.dao.domain.GraduateAbility;
import edu.xzit.graduate.dao.service.IGraduateAbilityRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 毕业达成度能力 Controller
 *
 * @author system
 * @date 2025-01-22
 */
@RestController
@RequestMapping("/graduate/ability")
public class GraduateAbilityController extends BaseController {
    
    @Autowired
    private IGraduateAbilityRepoService graduateAbilityRepoService;

    /**
     * 查询毕业达成度能力列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:ability:list')")
    @GetMapping("/list")
    public TableDataInfo list(GraduateAbility graduateAbility) {
        startPage();
        LambdaQueryWrapper<GraduateAbility> wrapper = Wrappers.lambdaQuery();
        
        if (graduateAbility.getMajorId() != null) {
            wrapper.eq(GraduateAbility::getMajorId, graduateAbility.getMajorId());
        }
        if (graduateAbility.getParentId() != null) {
            wrapper.eq(GraduateAbility::getParentId, graduateAbility.getParentId());
        }
        if (graduateAbility.getName() != null && !graduateAbility.getName().isEmpty()) {
            wrapper.like(GraduateAbility::getName, graduateAbility.getName());
        }
        
        wrapper.orderByAsc(GraduateAbility::getOrderNo, GraduateAbility::getId);
        List<GraduateAbility> list = graduateAbilityRepoService.list(wrapper);
        return getDataTable(list);
    }

    /**
     * 导出毕业达成度能力列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:ability:export')")
    @Log(title = "毕业达成度能力", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GraduateAbility graduateAbility) {
        LambdaQueryWrapper<GraduateAbility> wrapper = Wrappers.lambdaQuery();
        
        if (graduateAbility.getMajorId() != null) {
            wrapper.eq(GraduateAbility::getMajorId, graduateAbility.getMajorId());
        }
        if (graduateAbility.getParentId() != null) {
            wrapper.eq(GraduateAbility::getParentId, graduateAbility.getParentId());
        }
        if (graduateAbility.getName() != null && !graduateAbility.getName().isEmpty()) {
            wrapper.like(GraduateAbility::getName, graduateAbility.getName());
        }
        
        wrapper.orderByAsc(GraduateAbility::getOrderNo, GraduateAbility::getId);
        List<GraduateAbility> list = graduateAbilityRepoService.list(wrapper);
        ExcelUtil<GraduateAbility> util = new ExcelUtil<GraduateAbility>(GraduateAbility.class);
        util.exportExcel(response, list, "毕业达成度能力数据");
    }

    /**
     * 获取毕业达成度能力详细信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:ability:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(graduateAbilityRepoService.getById(id));
    }

    /**
     * 新增毕业达成度能力
     */
    @PreAuthorize("@ss.hasPermi('graduate:ability:add')")
    @Log(title = "毕业达成度能力", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GraduateAbility graduateAbility) {
        graduateAbilityRepoService.save(graduateAbility);
        return success();
    }

    /**
     * 修改毕业达成度能力
     */
    @PreAuthorize("@ss.hasPermi('graduate:ability:edit')")
    @Log(title = "毕业达成度能力", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GraduateAbility graduateAbility) {
        graduateAbilityRepoService.updateById(graduateAbility);
        return success();
    }

    /**
     * 删除毕业达成度能力
     */
    @PreAuthorize("@ss.hasPermi('graduate:ability:remove')")
    @Log(title = "毕业达成度能力", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        graduateAbilityRepoService.removeByIds(Arrays.asList(ids));
        return success();
    }

    /**
     * 获取格式化的能力列表用于下拉选择
     * 只返回子级能力，格式为：父级名称-子级名称
     */
    @PreAuthorize("@ss.hasPermi('graduate:ability:list')")
    @GetMapping("/dropdown")
    public AjaxResult getAbilityDropdown(@RequestParam(required = false) Long majorId) {
        LambdaQueryWrapper<GraduateAbility> wrapper = Wrappers.lambdaQuery();
        
        // 只查询有父级的能力（子级能力）
        wrapper.isNotNull(GraduateAbility::getParentId);
        
        if (majorId != null) {
            wrapper.eq(GraduateAbility::getMajorId, majorId);
        }
        
        wrapper.orderByAsc(GraduateAbility::getOrderNo, GraduateAbility::getId);
        List<GraduateAbility> childAbilities = graduateAbilityRepoService.list(wrapper);
        
        // 获取所有父级能力用于格式化显示名称
        LambdaQueryWrapper<GraduateAbility> parentWrapper = Wrappers.lambdaQuery();
        parentWrapper.isNull(GraduateAbility::getParentId);
        if (majorId != null) {
            parentWrapper.eq(GraduateAbility::getMajorId, majorId);
        }
        List<GraduateAbility> parentAbilities = graduateAbilityRepoService.list(parentWrapper);
        
        // 创建父级能力映射
        java.util.Map<Long, String> parentNameMap = parentAbilities.stream()
                .collect(java.util.stream.Collectors.toMap(
                    GraduateAbility::getId, 
                    GraduateAbility::getName
                ));
        
        // 格式化子级能力列表
        List<java.util.Map<String, Object>> dropdownList = childAbilities.stream()
                .map(ability -> {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("id", ability.getId());
                    item.put("originalName", ability.getName());
                    
                    // 获取父级名称
                    String parentName = parentNameMap.get(ability.getParentId());
                    if (parentName != null) {
                        item.put("name", parentName + "-" + ability.getName());
                    } else {
                        item.put("name", ability.getName());
                    }
                    
                    item.put("parentId", ability.getParentId());
                    item.put("majorId", ability.getMajorId());
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());
        
        return success(dropdownList);
    }
}