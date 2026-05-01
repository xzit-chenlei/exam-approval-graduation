package edu.xzit.core.controller.exam;

import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.page.TableDataInfo;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.core.flowable.service.IExamDeployFormService;
import edu.xzit.core.dao.domain.ExamDeployForm;
import edu.xzit.core.dao.domain.ExamForm;
import edu.xzit.core.dao.domain.ExamModule;
import edu.xzit.core.dao.service.IExamFormRepoService;
import edu.xzit.core.dao.service.IExamModuleRepoService;
import edu.xzit.core.service.FormModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 表单模块查询接口
 */
@RestController
@RequestMapping("/exam/form-module")
@RequiredArgsConstructor
public class FormModuleController extends BaseController {

    private final FormModuleService formModuleService;
    private final IExamFormRepoService examFormRepoService;
    private final IExamModuleRepoService examModuleRepoService;
    private final IExamDeployFormService examDeployFormService;

    /**
     * 根据流程定义查询挂载的考试表单及模块
     *
     * @param deployId 流程定义ID
     * @return 表单及模块数据（包括流程表单和第一个节点的表单）
     */
    @GetMapping("/deploy/{deployId}")
    public AjaxResult getFormModulesByDeploy(@PathVariable String deployId) {
        Map<String, Object> result = new java.util.HashMap<>();
        
        // 1. 获取流程级别的表单
        ExamForm examForm = examDeployFormService.selectExamDeployFormByDeployId(deployId);
        if (examForm == null) {
            return error("当前流程未挂载考试表单");
        }
        Map<String, Object> formModules = formModuleService.getFormModules(examForm.getId());
        formModules.put("id", examForm.getId());
        formModules.put("remark", examForm.getRemark());
        result.put("processForm", formModules);
        
        // 2. 获取第一个节点的表单（如果有）
        Map<String, Object> firstNodeForm = formModuleService.getFirstNodeFormByDeployId(deployId);
        if (firstNodeForm != null) {
            result.put("firstNodeForm", firstNodeForm);
            result.put("firstNodeFormId", firstNodeForm.get("id"));
            result.put("firstNodeTaskDefinitionKey", firstNodeForm.get("nodeTaskDefinitionKey"));
        }
        
        // 3. 为了兼容旧代码，同时返回流程表单数据（放在顶层）
        result.putAll(formModules);
        
        return success(result);
    }

    /**
     * 查询表单列表（分页）
     *
     * @param examForm 查询条件
     * @return 表单列表
     */
    @GetMapping("/form/list")
    public TableDataInfo listForm(ExamForm examForm) {
        startPage();
        List<ExamForm> list = examFormRepoService.selectExamFormList(examForm);
        return getDataTable(list);
    }

    /**
     * 查询表单及模块信息
     *
     * @param formId 表单ID
     * @return 表单及模块信息
     */
    @GetMapping("/{formId}")
    public AjaxResult getFormModules(@PathVariable Long formId) {
        return success(formModuleService.getFormModules(formId));
    }

    /**
     * 获取表单详细信息
     *
     * @param id 表单ID
     * @return 表单信息
     */
    @GetMapping("/form/{id}")
    public AjaxResult getFormInfo(@PathVariable("id") Long id) {
        return success(examFormRepoService.selectExamFormById(id));
    }

    /**
     * 新增表单
     *
     * @param examForm 表单信息
     * @return 结果
     */
    @Log(title = "考试表单", businessType = BusinessType.INSERT)
    @PostMapping("/form")
    public AjaxResult addForm(@RequestBody ExamForm examForm) {
        return toAjax(examFormRepoService.insertExamForm(examForm));
    }

    /**
     * 修改表单
     *
     * @param examForm 表单信息
     * @return 结果
     */
    @Log(title = "考试表单", businessType = BusinessType.UPDATE)
    @PutMapping("/form")
    public AjaxResult editForm(@RequestBody ExamForm examForm) {
        return toAjax(examFormRepoService.updateExamForm(examForm));
    }

    /**
     * 删除表单
     *
     * @param ids 表单ID数组
     * @return 结果
     */
    @Log(title = "考试表单", businessType = BusinessType.DELETE)
    @DeleteMapping("/form/{ids}")
    public AjaxResult removeForm(@PathVariable Long[] ids) {
        return toAjax(examFormRepoService.deleteExamFormByIds(ids));
    }

    /**
     * 新增模块
     *
     * @param examModule 模块信息
     * @return 结果
     */
    @Log(title = "考试模块", businessType = BusinessType.INSERT)
    @PostMapping("/module")
    public AjaxResult addModule(@RequestBody ExamModule examModule) {
        return toAjax(examModuleRepoService.insertExamModule(examModule));
    }

    /**
     * 保存表单及模块数据
     *
     * @param requestData 请求数据，包含formId、formName、modules
     * @return 保存后的表单ID
     */
    @Log(title = "保存表单及模块", businessType = BusinessType.UPDATE)
    @PostMapping("/save")
    public AjaxResult saveFormModules(@RequestBody Map<String, Object> requestData) {
        Long formId = null;
        Object formIdObj = requestData.get("formId");
        if (formIdObj != null) {
            if (formIdObj instanceof Number) {
                formId = ((Number) formIdObj).longValue();
            } else if (formIdObj instanceof String) {
                String formIdStr = (String) formIdObj;
                if (!formIdStr.isEmpty()) {
                    try {
                        formId = Long.parseLong(formIdStr);
                    } catch (NumberFormatException e) {
                        // 忽略，保持为null
                    }
                }
            }
        }
        
        String formName = (String) requestData.get("formName");
        if (formName == null || formName.trim().isEmpty()) {
            return error("表单名称不能为空");
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> modules = (List<Map<String, Object>>) requestData.get("modules");
        if (modules == null) {
            modules = new java.util.ArrayList<>();
        }
        
        Long savedFormId = formModuleService.saveFormModules(formId, formName, modules);
        return success(savedFormId);
    }

    /**
     * 挂载考试表单到流程定义
     */
    @Log(title = "考试表单", businessType = BusinessType.INSERT)
    @PostMapping("/addExamDeployForm")
    public AjaxResult addExamDeployForm(@RequestBody ExamDeployForm examDeployForm) {
        return toAjax(examDeployFormService.insertExamDeployForm(examDeployForm));
    }

    /**
     * 根据流程任务或实例获取自定义表单数据（模板+实例数据）
     *
     * @param taskId 任务ID（可选）
     * @param procInsId 流程实例ID（可选）
     * @return 表单模板和实例数据
     */
    @GetMapping("/instance/data")
    public AjaxResult getFormInstanceData(
            @RequestParam(required = false) String taskId,
            @RequestParam(required = false) String procInsId) {
        return success(formModuleService.getFormInstanceData(taskId, procInsId));
    }
}
