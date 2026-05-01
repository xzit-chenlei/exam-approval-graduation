package edu.xzit.core.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import edu.xzit.core.core.common.exception.ServiceException;
import edu.xzit.core.dao.domain.ExamForm;
import edu.xzit.core.dao.domain.ExamModule;
import edu.xzit.core.dao.service.IExamFormRepoService;
import edu.xzit.core.dao.service.IExamModuleRepoService;
import edu.xzit.core.core.flowable.service.IExamDeployFormService;
import lombok.RequiredArgsConstructor;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormModuleService {

    private static final Logger log = LoggerFactory.getLogger(FormModuleService.class);

    private final IExamFormRepoService examFormRepoService;
    private final IExamModuleRepoService examModuleRepoService;
    private final IExamDeployFormService examDeployFormService;

    @Resource
    private TaskService taskService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;
    @Resource
    private RepositoryService repositoryService;

    /**
     * 查询表单及模块数据
     *
     * @param formId 表单ID
     * @return 结构化数据
     */
    public Map<String, Object> getFormModules(Long formId) {
        return getFormModules(formId, true);
    }

    /**
     * 查询表单及模块数据
     *
     * @param formId 表单ID
     * @param isTemplate 是否为模板（true=模板，返回结构；false=实例，返回数据）
     * @return 结构化数据
     */
    public Map<String, Object> getFormModules(Long formId, boolean isTemplate) {
        ExamForm examForm = examFormRepoService.getById(formId);
        if (examForm == null) {
            throw new ServiceException("考试表单不存在");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", examForm.getName());
        result.put("isTemplate", isTemplate);  // 标识是否为模板

        List<Long> moduleIds = parseModuleIds(examForm.getModuleIds());
        if (moduleIds.isEmpty()) {
            result.put("modules", Collections.emptyMap());
            return result;
        }

        List<ExamModule> moduleList = examModuleRepoService.listByIds(moduleIds);
        Map<Long, ExamModule> moduleMap = moduleList.stream()
                .collect(Collectors.toMap(ExamModule::getId, Function.identity(), (a, b) -> a));

        Map<String, Object> modules = new LinkedHashMap<>();
        for (Long moduleId : moduleIds) {
            ExamModule module = moduleMap.get(moduleId);
            if (module == null) {
                continue;
            }
            modules.put(buildModuleKey(moduleId), buildModuleContent(module, isTemplate));
        }

        result.put("modules", modules);
        return result;
    }

    private List<Long> parseModuleIds(String moduleIdsStr) {
        if (StrUtil.isBlank(moduleIdsStr)) {
            return Collections.emptyList();
        }
        List<Long> ids = new ArrayList<>();
        for (String idStr : moduleIdsStr.split(",")) {
            String trimmed = StrUtil.trim(idStr);
            if (StrUtil.isBlank(trimmed)) {
                continue;
            }
            Long moduleId = Convert.toLong(trimmed, null);
            if (moduleId != null) {
                ids.add(moduleId);
            } else {
                log.warn("非法模块ID：{}", trimmed);
            }
        }
        return ids;
    }

    private String buildModuleKey(Long moduleId) {
        return "module_" + moduleId;
    }

    private Map<String, Object> buildModuleContent(ExamModule module) {
        return buildModuleContent(module, true);
    }

    /**
     * 构建模块内容
     *
     * @param module 模块实体
     * @param isTemplate 是否为模板（true=模板，返回结构；false=实例，返回数据）
     * @return 模块内容
     */
    private Map<String, Object> buildModuleContent(ExamModule module, boolean isTemplate) {
        Map<String, Object> moduleContent = new LinkedHashMap<>();
        
        // 包含模块ID，由后端生成
        moduleContent.put("id", module.getId());
        moduleContent.put("name", module.getName());
        
        // 获取模块类型，默认为1（数据类型）
        Integer moduleType = module.getType();
        if (moduleType == null) {
            moduleType = 1; // 兼容旧数据，默认为数据类型
        }
        moduleContent.put("type", moduleType);
        
        // 模块字段标识
        moduleContent.put("fieldId", module.getFieldId());
        
        // 根据类型构建不同的数据结构
        if (moduleType == 1 || moduleType == 6) {
            // 类型1: 二维数据列表；类型6: 勾选矩阵
            List<String> rowHeaders = parseStringList(module.getRowHeaders());
            List<String> colHeaders = parseStringList(module.getColHeaders());
            List<List<Object>> templateData = parseMatrix(module.getData());

            int rows = !rowHeaders.isEmpty() ? rowHeaders.size() : templateData.size();
            int cols = !colHeaders.isEmpty() ? colHeaders.size() : (templateData.isEmpty() ? 0 : templateData.get(0).size());

            moduleContent.put("rows", rows);
            moduleContent.put("cols", cols);
            moduleContent.put("rowHeaders", rowHeaders);
            moduleContent.put("colHeaders", colHeaders);
            
            // 如果是模板，返回空数据数组（明确这是模板结构）
            // 如果是实例，返回实际数据
            if (isTemplate) {
                // 模板：返回空数据，前端会初始化
                moduleContent.put("data", Collections.emptyList());
            } else {
                // 实例：返回实际数据
                moduleContent.put("data", templateData);
            }

            if (moduleType == 1) {
                // 仅类型1需要固定行列配置
                Map<String, Object> options = parseOptions(module.getRemark());
                moduleContent.put("fixedRowCount", Convert.toInt(options.getOrDefault("fixedRowCount", 0), 0));
                moduleContent.put("fixedRowPosition", Convert.toStr(options.getOrDefault("fixedRowPosition", "top"), "top"));
                moduleContent.put("fixedColCount", Convert.toInt(options.getOrDefault("fixedColCount", 0), 0));
                moduleContent.put("fixedColPosition", Convert.toStr(options.getOrDefault("fixedColPosition", "left"), "left"));
            }
        } else if (moduleType == 2 || moduleType == 3 || moduleType == 4 || moduleType == 5 || moduleType == 7) {
            // 类型2: 文件上传，类型3: 日期选择，类型4/5: 文本/单选，类型7: 图片上传
            // data字段存储的是对象数组，需要解析为List<Map>
            List<Map<String, Object>> templateData = parseObjectList(module.getData());
            
            if (isTemplate) {
                // 模板：返回结构数据（保留字段定义，但清空值）
                if (moduleType == 2) {
                    // 文件类型：返回空数组
                    moduleContent.put("data", Collections.emptyList());
                } else if (moduleType == 7) {
                    // 图片上传类型：返回空数组
                    moduleContent.put("data", Collections.emptyList());
                } else if (moduleType == 3) {
                    // 日期类型：保留字段定义，值设为null
                    List<Map<String, Object>> structureData = new ArrayList<>();
                    for (Map<String, Object> item : templateData) {
                        Map<String, Object> structureItem = new LinkedHashMap<>();
                        structureItem.put("fieldId", item.get("fieldId"));
                        structureItem.put("label", item.get("label"));
                        structureItem.put("value", null);  // 值设为null
                        structureData.add(structureItem);
                    }
                    moduleContent.put("data", structureData);
                } else if (moduleType == 4) {
                    // 文本类型：保留字段定义，值设为空
                    List<Map<String, Object>> structureData = new ArrayList<>();
                    for (Map<String, Object> item : templateData) {
                        Map<String, Object> structureItem = new LinkedHashMap<>();
                        structureItem.put("fieldId", item.get("fieldId"));
                        structureItem.put("label", item.get("label"));
                        structureItem.put("value", "");  // 值设为空
                        structureItem.put("inputType", item.get("inputType"));
                        structureData.add(structureItem);
                    }
                    moduleContent.put("data", structureData);
                } else if (moduleType == 5) {
                    // 单选类型：保留选项定义
                    List<Map<String, Object>> structureData = new ArrayList<>();
                    for (Map<String, Object> item : templateData) {
                        Map<String, Object> structureItem = new LinkedHashMap<>();
                        structureItem.put("fieldId", item.get("fieldId"));
                        structureItem.put("label", item.get("label"));
                        structureItem.put("value", item.get("value"));  // 保留选项值
                        structureData.add(structureItem);
                    }
                    moduleContent.put("data", structureData);
                    // 模板：默认选中第一个选项
                    moduleContent.put("selected", templateData.isEmpty() ? "" : Convert.toStr(templateData.get(0).get("value"), ""));
                }
            } else {
                // 实例：返回实际数据
                moduleContent.put("data", templateData);
                if (moduleType == 5) {
                    // 单选类型：尝试从 remark 中读取 selected，如果没有则使用第一个选项
                    String selected = "";
                    try {
                        Map<String, Object> options = parseOptions(module.getRemark());
                        selected = Convert.toStr(options.getOrDefault("selected", ""), "");
                    } catch (Exception e) {
                        // 忽略解析错误
                    }
                    if (StrUtil.isBlank(selected) && !templateData.isEmpty()) {
                        selected = Convert.toStr(templateData.get(0).get("value"), "");
                    }
                    moduleContent.put("selected", selected);
                }
            }
        } else if (moduleType == 10) {
            // 类型10: 数据链接模块，data字段存储的是包含对象的数组，需要取出第一个元素作为data对象
            String rawData = module.getData();
            Map<String, Object> dataObj = new LinkedHashMap<>();
            
            if (StrUtil.isNotBlank(rawData)) {
                try {
                    // 尝试解析为数组格式 [{"link":"xxx"}]
                    List<Map<String, Object>> dataList = parseObjectList(rawData);
                    if (!dataList.isEmpty()) {
                        // 取出第一个元素作为data对象
                        dataObj = dataList.get(0);
                    } else {
                        // 如果解析为空列表，尝试直接解析为对象格式 {"link":"xxx"}
                        try {
                            Map<String, Object> directObj = JSON.parseObject(rawData, new TypeReference<Map<String, Object>>() {});
                            if (directObj != null && !directObj.isEmpty()) {
                                dataObj = directObj;
                            } else {
                                dataObj.put("link", "");
                            }
                        } catch (Exception e2) {
                            // 直接解析也失败，使用默认值
                            dataObj.put("link", "");
                        }
                    }
                } catch (Exception e) {
                    // 解析失败，尝试直接解析为对象
                    try {
                        Map<String, Object> directObj = JSON.parseObject(rawData, new TypeReference<Map<String, Object>>() {});
                        if (directObj != null && !directObj.isEmpty()) {
                            dataObj = directObj;
                        } else {
                            dataObj.put("link", "");
                        }
                    } catch (Exception e2) {
                        // 所有解析都失败，使用默认值
                        dataObj.put("link", "");
                    }
                }
            } else {
                // 如果没有数据，创建默认的空对象
                dataObj.put("link", "");
            }
            
            // 确保data对象至少包含link字段
            if (!dataObj.containsKey("link")) {
                dataObj.put("link", "");
            }
            
            moduleContent.put("data", dataObj);
        } else if (moduleType == 8) {
            // 类型8: 父子标题数据列表
            // parentRows 存储在 data 字段中（JSON格式）
            List<Map<String, Object>> parentRows = parseObjectList(module.getData());
            moduleContent.put("parentRows", parentRows);
        } else if (moduleType == 9) {
            // 类型9: 列父子标题数据列表
            // colParents 存储在 remark 字段中（JSON格式）
            // rowHeaders 存储在 rowHeaders 字段中
            // data 存储在 data 字段中
            Map<String, Object> colParentsData = parseOptions(module.getRemark());
            Object colParentsObj = colParentsData.get("colParents");
            List<Map<String, Object>> colParents = new ArrayList<>();
            if (colParentsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> colParentsList = (List<Map<String, Object>>) colParentsObj;
                colParents = colParentsList;
            }
            moduleContent.put("colParents", colParents);
            
            List<String> rowHeaders = parseStringList(module.getRowHeaders());
            moduleContent.put("rowHeaders", rowHeaders);
            
            List<List<Object>> data = parseMatrix(module.getData());
            moduleContent.put("data", data);
            moduleContent.put("rows", rowHeaders.isEmpty() ? data.size() : rowHeaders.size());
        }
        
        return moduleContent;
    }

    private List<String> parseStringList(String json) {
        if (StrUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return JSON.parseArray(json, String.class);
        } catch (Exception e) {
            log.warn("解析字符串数组失败: {}", json, e);
            return Collections.emptyList();
        }
    }

    private List<List<Object>> parseMatrix(String json) {
        if (StrUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return JSON.parseObject(json, new TypeReference<List<List<Object>>>() {
            });
        } catch (Exception e) {
            log.warn("解析二维数组失败: {}", json, e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析对象数组（用于文件类型和日期类型）
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseObjectList(String json) {
        if (StrUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            // 使用 parseArray 解析 JSON 数组，而不是 parseObject
            // parseArray 返回 List<Map>（原始类型），需要转换为 List<Map<String, Object>>
            List<Map> rawList = JSON.parseArray(json, Map.class);
            if (rawList == null) {
                return Collections.emptyList();
            }
            // 转换为 List<Map<String, Object>>
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map map : rawList) {
                if (map != null) {
                    result.add((Map<String, Object>) map);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("解析对象数组失败: {}", json, e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析 remark 中的配置
     */
    private Map<String, Object> parseOptions(String remark) {
        if (StrUtil.isBlank(remark)) {
            return Collections.emptyMap();
        }
        try {
            return JSON.parseObject(remark, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.warn("解析模块remark失败: {}", remark, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 保存表单及模块数据
     *
     * @param formId 表单ID（如果为null则新建，否则更新）
     * @param formName 表单名称
     * @param modules 模块数据列表
     * @return 保存后的表单ID
     */
    public Long saveFormModules(Long formId, String formName, List<Map<String, Object>> modules) {
        List<Long> moduleIds = new ArrayList<>();
        
        // 保存或更新模块
        for (Map<String, Object> moduleData : modules) {
            ExamModule module = new ExamModule();
            
            // 如果模块有ID，则更新；否则新建
            Object moduleIdObj = moduleData.get("id");
            if (moduleIdObj != null) {
                Long moduleId = Convert.toLong(moduleIdObj, null);
                if (moduleId != null && moduleId > 0) {
                    module.setId(moduleId);
                }
            }
            
            // 模块名称不能为空
            String moduleName = Convert.toStr(moduleData.get("name"), "");
            if (StrUtil.isBlank(moduleName)) {
                moduleName = "未命名模块";
            }
            module.setName(moduleName);
            
            // 获取模块类型，默认为1（数据类型）
            Object typeObj = moduleData.get("type");
            Integer moduleType = Convert.toInt(typeObj, 1);
            // 目前支持的类型：1-10（数据类型、文件类型、日期选择、文本输入、单选、勾选矩阵、图片上传、父子标题数据列表、列父子标题数据列表）
            if (moduleType == null || moduleType < 1 || moduleType > 10) {
                moduleType = 1; // 默认类型为数据类型
            }
            module.setType(moduleType);
            
            // 模块字段标识
            Object fieldIdObj = moduleData.get("fieldId");
            String fieldId = Convert.toStr(fieldIdObj, null);
            if (StrUtil.isNotBlank(fieldId)) {
                module.setFieldId(fieldId.trim());
            }
            
            // 根据类型处理不同的数据字段
            if (moduleType == 1) {
                // 类型1: 二维数据列表，需要rowHeaders、colHeaders、data
                Object rowHeadersObj = moduleData.get("rowHeaders");
                if (rowHeadersObj != null) {
                    String rowHeadersJson = JSON.toJSONString(rowHeadersObj);
                    module.setRowHeaders(rowHeadersJson);
                }
                
                Object colHeadersObj = moduleData.get("colHeaders");
                if (colHeadersObj != null) {
                    String colHeadersJson = JSON.toJSONString(colHeadersObj);
                    module.setColHeaders(colHeadersJson);
                }
                
                Object dataObj = moduleData.get("data");
                if (dataObj != null) {
                    String dataJson = JSON.toJSONString(dataObj);
                    module.setData(dataJson);
                }
                // 保存固定行列配置到 remark
                Map<String, Object> opts = new LinkedHashMap<>();
                opts.put("fixedRowCount", moduleData.getOrDefault("fixedRowCount", 0));
                opts.put("fixedRowPosition", moduleData.getOrDefault("fixedRowPosition", "top"));
                opts.put("fixedColCount", moduleData.getOrDefault("fixedColCount", 0));
                opts.put("fixedColPosition", moduleData.getOrDefault("fixedColPosition", "left"));
                module.setRemark(JSON.toJSONString(opts));
            } else if (moduleType == 2 || moduleType == 3 || moduleType == 4 || moduleType == 5 || moduleType == 7) {
                // 类型2: 文件上传，类型3: 日期选择，类型4: 文本输入，类型5: 单选，类型7: 图片上传，都只需要data字段
                Object dataObj = moduleData.get("data");
                if (dataObj != null) {
                    String dataJson = JSON.toJSONString(dataObj);
                    module.setData(dataJson);
                } else {
                    module.setData("[]"); // 空数组
                }
                // 这些类型不需要rowHeaders和colHeaders
                module.setRowHeaders(null);
                module.setColHeaders(null);
                module.setRemark(null);
            } else if (moduleType == 10) {
                // 类型10: 数据链接模块，data是一个对象（包含link字段），需要包装成ArrayList保存
                Object dataObj = moduleData.get("data");
                if (dataObj != null) {
                    // 将对象包装成ArrayList，以便后端能正确反序列化
                    List<Object> dataList = new ArrayList<>();
                    dataList.add(dataObj);
                    String dataJson = JSON.toJSONString(dataList);
                    module.setData(dataJson);
                } else {
                    // 如果没有data，创建一个包含空对象的数组
                    List<Map<String, Object>> dataList = new ArrayList<>();
                    Map<String, Object> emptyData = new LinkedHashMap<>();
                    emptyData.put("link", "");
                    dataList.add(emptyData);
                    module.setData(JSON.toJSONString(dataList));
                }
                // 这些类型不需要rowHeaders和colHeaders
                module.setRowHeaders(null);
                module.setColHeaders(null);
                module.setRemark(null);
            } else if (moduleType == 6) {
                // 类型6: 勾选矩阵，与二维表类似但无需固定行列配置
                Object rowHeadersObj = moduleData.get("rowHeaders");
                if (rowHeadersObj != null) {
                    String rowHeadersJson = JSON.toJSONString(rowHeadersObj);
                    module.setRowHeaders(rowHeadersJson);
                }

                Object colHeadersObj = moduleData.get("colHeaders");
                if (colHeadersObj != null) {
                    String colHeadersJson = JSON.toJSONString(colHeadersObj);
                    module.setColHeaders(colHeadersJson);
                }

                Object dataObj = moduleData.get("data");
                if (dataObj != null) {
                    String dataJson = JSON.toJSONString(dataObj);
                    module.setData(dataJson);
                }
                // 不需要固定行列配置
                module.setRemark(null);
            } else if (moduleType == 8) {
                // 类型8: 父子标题数据列表
                // parentRows 存储在 data 字段中（JSON格式）
                Object parentRowsObj = moduleData.get("parentRows");
                if (parentRowsObj != null) {
                    String parentRowsJson = JSON.toJSONString(parentRowsObj);
                    module.setData(parentRowsJson);
                } else {
                    module.setData("[]"); // 空数组
                }
                // 不需要rowHeaders和colHeaders
                module.setRowHeaders(null);
                module.setColHeaders(null);
                module.setRemark(null);
            } else if (moduleType == 9) {
                // 类型9: 列父子标题数据列表
                // colParents 存储在 remark 字段中（JSON格式）
                // rowHeaders 存储在 rowHeaders 字段中
                // data 存储在 data 字段中
                Object colParentsObj = moduleData.get("colParents");
                Map<String, Object> remarkData = new LinkedHashMap<>();
                if (colParentsObj != null) {
                    remarkData.put("colParents", colParentsObj);
                }
                module.setRemark(JSON.toJSONString(remarkData));
                
                Object rowHeadersObj = moduleData.get("rowHeaders");
                if (rowHeadersObj != null) {
                    String rowHeadersJson = JSON.toJSONString(rowHeadersObj);
                    module.setRowHeaders(rowHeadersJson);
                }
                
                Object dataObj = moduleData.get("data");
                if (dataObj != null) {
                    String dataJson = JSON.toJSONString(dataObj);
                    module.setData(dataJson);
                } else {
                    module.setData("[]"); // 空数组
                }
                
                // 不需要colHeaders
                module.setColHeaders(null);
            }
            
            // 保存或更新模块
            boolean success;
            if (module.getId() != null) {
                log.info("更新模块: id={}, name={}", module.getId(), module.getName());
                success = examModuleRepoService.updateById(module);
                if (!success) {
                    log.error("更新模块失败: id={}", module.getId());
                    throw new ServiceException("更新模块失败: " + module.getName());
                }
                moduleIds.add(module.getId());
            } else {
                log.info("新建模块: name={}", module.getName());
                success = examModuleRepoService.save(module);
                if (!success) {
                    log.error("保存模块失败: name={}", module.getName());
                    throw new ServiceException("保存模块失败: " + module.getName());
                }
                if (module.getId() == null) {
                    log.error("保存模块后ID为空: name={}", module.getName());
                    throw new ServiceException("保存模块后ID为空: " + module.getName());
                }
                moduleIds.add(module.getId());
                log.info("模块保存成功: id={}, name={}", module.getId(), module.getName());
            }
        }
        
        // 保存或更新表单
        ExamForm examForm = new ExamForm();
        if (formId != null) {
            examForm.setId(formId);
        }
        examForm.setName(formName);
        examForm.setModuleIds(StrUtil.join(",", moduleIds));
        
        boolean formSuccess;
        if (formId != null) {
            log.info("更新表单: id={}, name={}, moduleIds={}", formId, formName, examForm.getModuleIds());
            formSuccess = examFormRepoService.updateById(examForm);
            if (!formSuccess) {
                log.error("更新表单失败: id={}", formId);
                throw new ServiceException("更新表单失败");
            }
            return formId;
        } else {
            log.info("新建表单: name={}, moduleIds={}", formName, examForm.getModuleIds());
            formSuccess = examFormRepoService.save(examForm);
            if (!formSuccess) {
                log.error("保存表单失败: name={}", formName);
                throw new ServiceException("保存表单失败");
            }
            if (examForm.getId() == null) {
                log.error("保存表单后ID为空: name={}", formName);
                throw new ServiceException("保存表单后ID为空");
            }
            log.info("表单保存成功: id={}, name={}", examForm.getId(), formName);
            return examForm.getId();
        }
    }

    /**
     * 获取流程实例的自定义表单数据（模板+实例数据）
     *
     * @param taskId 任务ID（可选）
     * @param procInsId 流程实例ID（可选）
     * @return 包含模板和实例数据的Map
     */
    public Map<String, Object> getFormInstanceData(String taskId, String procInsId) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 1. 获取流程变量
        Map<String, Object> variables = null;
        String actualProcInsId = null;
        String deployId = null;
        String processDefinitionId = null;
        
        if (StrUtil.isNotBlank(taskId)) {
            // 优先从任务获取流程变量
            try {
                HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery()
                        .includeProcessVariables()
                        .finished()
                        .taskId(taskId)
                        .singleResult();
                if (historicTask != null) {
                    variables = historicTask.getProcessVariables();
                    actualProcInsId = historicTask.getProcessInstanceId();
                } else {
                    // 运行时任务
                    org.flowable.task.api.Task task = taskService.createTaskQuery()
                            .taskId(taskId)
                            .singleResult();
                    if (task != null) {
                        variables = taskService.getVariables(taskId);
                        actualProcInsId = task.getProcessInstanceId();
                    }
                }
            } catch (Exception e) {
                log.warn("通过 taskId 获取流程变量失败: {}", taskId, e);
            }
        }
        
        if (variables == null && StrUtil.isNotBlank(procInsId)) {
            // 从流程实例获取变量
            actualProcInsId = procInsId;
            try {
                // 先尝试从运行时获取
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                        .processInstanceId(procInsId)
                        .singleResult();
                if (processInstance != null) {
                    variables = runtimeService.getVariables(procInsId);
                    if (processDefinitionId == null) {
                        processDefinitionId = processInstance.getProcessDefinitionId();
                    }
                } else {
                    // 从历史实例获取
                    HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
                            .processInstanceId(procInsId)
                            .includeProcessVariables()
                            .singleResult();
                    if (historicInstance != null) {
                        if (historicInstance.getProcessVariables() != null) {
                            variables = historicInstance.getProcessVariables();
                        }
                        if (processDefinitionId == null) {
                            processDefinitionId = historicInstance.getProcessDefinitionId();
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("通过 procInsId 获取流程变量失败: {}", procInsId, e);
            }
        }
        
        if (variables == null) {
            log.warn("无法获取流程变量: taskId={}, procInsId={}", taskId, procInsId);
            result.put("template", null);
            result.put("instanceData", null);
            return result;
        }
        
        // 2. 从流程变量中获取实例数据
        Object examFormDataObj = variables.get("examFormData");
        Object examFormIdObj = variables.get("examFormId");
        Object examFormNameObj = variables.get("examFormName");
        Object deployIdObj = variables.get("deployId");
        
        List<Map<String, Object>> instanceData = null;
        if (examFormDataObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) examFormDataObj;
            instanceData = dataList;
        }
        
        Long examFormId = null;
        if (examFormIdObj != null) {
            if (examFormIdObj instanceof Number) {
                examFormId = ((Number) examFormIdObj).longValue();
            } else if (examFormIdObj instanceof String) {
                try {
                    examFormId = Long.parseLong((String) examFormIdObj);
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
        }
        
        String examFormName = Convert.toStr(examFormNameObj, null);
        if (deployIdObj != null) {
            deployId = Convert.toStr(deployIdObj, null);
        }
        
        // 3. 如果没有 deployId，尝试从流程定义获取
        if (StrUtil.isBlank(deployId) && actualProcInsId != null) {
            try {
                HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(actualProcInsId)
                        .singleResult();
                if (historicInstance != null && historicInstance.getDeploymentId() != null) {
                    deployId = historicInstance.getDeploymentId();
                }
            } catch (Exception e) {
                log.warn("从流程实例获取 deployId 失败", e);
            }
        }
        
        // 4. 获取模板结构
        Map<String, Object> template = null;
        if (StrUtil.isNotBlank(deployId)) {
            try {
                ExamForm examForm = examDeployFormService.selectExamDeployFormByDeployId(deployId);
                if (examForm != null) {
                    template = getFormModules(examForm.getId(), true); // 获取模板（isTemplate=true）
                    template.put("id", examForm.getId());
                    template.put("name", examForm.getName());
                }
            } catch (Exception e) {
                log.warn("获取表单模板失败: deployId={}", deployId, e);
            }
        } else if (examFormId != null) {
            // 如果没有 deployId 但有 examFormId，直接获取模板
            try {
                template = getFormModules(examFormId, true);
            } catch (Exception e) {
                log.warn("获取表单模板失败: examFormId={}", examFormId, e);
            }
        }
        
        // 5. 检查节点上是否绑定了自定义表单（examFormId）
        Long nodeExamFormId = null;
        String nodeTaskDefinitionKey = null;
        if (StrUtil.isNotBlank(taskId)) {
            try {
                org.flowable.task.api.Task task = taskService.createTaskQuery()
                        .taskId(taskId)
                        .singleResult();
                if (task != null) {
                    nodeTaskDefinitionKey = task.getTaskDefinitionKey();
                    processDefinitionId = task.getProcessDefinitionId();
                    log.debug("查找节点绑定的表单: taskId={}, taskDefinitionKey={}", taskId, nodeTaskDefinitionKey);
                    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                            .processDefinitionId(task.getProcessDefinitionId())
                            .singleResult();
                    if (processDefinition != null) {
                        Process process = repositoryService.getBpmnModel(processDefinition.getId()).getProcesses().get(0);
                        Collection<FlowElement> allElements = edu.xzit.core.core.flowable.flow.FlowableUtils.getAllElements(process.getFlowElements(), null);
                        for (FlowElement flowElement : allElements) {
                            if (flowElement.getId().equals(nodeTaskDefinitionKey) && flowElement instanceof UserTask) {
                                UserTask userTask = (UserTask) flowElement;
                                log.debug("找到 UserTask 节点: {}", nodeTaskDefinitionKey);
                                // 从 flowable:Properties 中读取 examFormId
                                // 使用 FlowableUtils 工具方法查找 Properties 扩展元素
                                ExtensionElement propertiesElement = edu.xzit.core.core.flowable.flow.FlowableUtils
                                        .getExtensionElementFromFlowElementByName(userTask, "properties");
                                if (propertiesElement != null) {
                                    log.debug("找到 Properties 扩展元素: name={}, namespace={}", 
                                            propertiesElement.getName(), propertiesElement.getNamespace());
                                    // 获取 properties 下的 property 子元素
                                    List<ExtensionElement> propertyElements = propertiesElement.getChildElements().get("property");
                                    if (propertyElements != null && !propertyElements.isEmpty()) {
                                        log.debug("Property 子元素数量: {}", propertyElements.size());
                                        for (ExtensionElement propertyElement : propertyElements) {
                                            // 获取 property 的 name 和 value 属性
                                            List<ExtensionAttribute> nameAttrs = propertyElement.getAttributes().get("name");
                                            List<ExtensionAttribute> valueAttrs = propertyElement.getAttributes().get("value");
                                            if (nameAttrs != null && !nameAttrs.isEmpty() && valueAttrs != null && !valueAttrs.isEmpty()) {
                                                String propertyName = nameAttrs.get(0).getValue();
                                                String propertyValue = valueAttrs.get(0).getValue();
                                                log.debug("Property: name={}, value={}", propertyName, propertyValue);
                                                if ("examFormId".equals(propertyName) && StringUtils.isNotBlank(propertyValue)) {
                                                    try {
                                                        nodeExamFormId = Long.parseLong(propertyValue);
                                                        log.info("成功读取节点绑定的表单ID: {}", nodeExamFormId);
                                                        break;
                                                    } catch (NumberFormatException e) {
                                                        log.warn("节点 examFormId 格式错误: {}", propertyValue);
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        log.debug("Properties 扩展元素下没有 property 子元素");
                                    }
                                } else {
                                    log.debug("节点没有找到 Properties 扩展元素");
                                    // 如果使用工具方法找不到，尝试手动遍历（用于调试）
                                    if (userTask.getExtensionElements() != null) {
                                        Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
                                        log.debug("节点扩展元素数量: {}", extensionElements.size());
                                        for (Map.Entry<String, List<ExtensionElement>> entry : extensionElements.entrySet()) {
                                            log.debug("扩展元素键: {}, 值数量: {}", entry.getKey(), entry.getValue().size());
                                            for (ExtensionElement extElement : entry.getValue()) {
                                                log.debug("扩展元素名称: {}, 命名空间: {}", extElement.getName(), extElement.getNamespace());
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    } else {
                        log.warn("未找到流程定义: processDefinitionId={}", task.getProcessDefinitionId());
                    }
                } else {
                    log.warn("未找到任务: taskId={}", taskId);
                }
            } catch (Exception e) {
                log.error("读取节点上的 examFormId 属性失败: taskId={}", taskId, e);
            }
        }
        
        // 6. 读取所有历史节点的表单数据（无论当前节点是否绑定表单，都要读取历史数据）
        Map<String, Object> nodeFormDataMap = null;
        Object nodeFormDataMapObj = variables.get("nodeFormDataMap");
        if (nodeFormDataMapObj != null) {
            if (nodeFormDataMapObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) nodeFormDataMapObj;
                nodeFormDataMap = map;
            } else if (nodeFormDataMapObj instanceof String) {
                // 如果是字符串，尝试解析 JSON
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> parsedMap = JSON.parseObject((String) nodeFormDataMapObj, Map.class);
                    nodeFormDataMap = parsedMap;
                } catch (Exception e) {
                    // 解析失败，忽略
                }
            }
        }
        
        // 6.1. 为所有历史节点获取表单模板结构
        Map<String, Map<String, Object>> historicalNodeTemplates = new LinkedHashMap<>();
        if (nodeFormDataMap != null && !nodeFormDataMap.isEmpty()) {
            for (Map.Entry<String, Object> entry : nodeFormDataMap.entrySet()) {
                String nodeId = entry.getKey();
                Object nodeDataObj = entry.getValue();
                if (nodeDataObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nodeData = (Map<String, Object>) nodeDataObj;
                    Object nodeFormIdObj = nodeData.get("nodeFormId");
                    if (nodeFormIdObj != null) {
                        Long historicalNodeFormId = null;
                        if (nodeFormIdObj instanceof Number) {
                            historicalNodeFormId = ((Number) nodeFormIdObj).longValue();
                        } else if (nodeFormIdObj instanceof String) {
                            try {
                                historicalNodeFormId = Long.parseLong((String) nodeFormIdObj);
                            } catch (NumberFormatException e) {
                                continue;
                            }
                        }
                        
                        if (historicalNodeFormId != null) {
                            try {
                                Map<String, Object> historicalTemplate = getFormModules(historicalNodeFormId, true);
                                if (historicalTemplate != null) {
                                    historicalNodeTemplates.put(nodeId, historicalTemplate);
                                }
                            } catch (Exception e) {
                                // 获取失败，忽略
                            }
                        }
                    }
                }
            }
        }
        
        // 7. 如果当前节点绑定了自定义表单，获取节点表单模板和实例数据
        Map<String, Object> nodeTemplate = null;
        List<Map<String, Object>> nodeInstanceData = null;
        if (nodeExamFormId != null) {
            try {
                nodeTemplate = getFormModules(nodeExamFormId, true);
                
                // 优先从 nodeFormDataMap 中获取当前节点的数据
                if (nodeFormDataMap != null && nodeTaskDefinitionKey != null) {
                    Object currentNodeDataObj = nodeFormDataMap.get(nodeTaskDefinitionKey);
                    if (currentNodeDataObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> currentNodeData = (Map<String, Object>) currentNodeDataObj;
                        Object formDataObj = currentNodeData.get("formData");
                        if (formDataObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> formDataList = (List<Map<String, Object>>) formDataObj;
                            nodeInstanceData = formDataList;
                        }
                    }
                }
                
                // 兼容旧数据：如果 nodeFormDataMap 中没有当前节点的数据，尝试从 nodeFormData 获取
                if (nodeInstanceData == null) {
                    Object nodeFormDataObj = variables.get("nodeFormData");
                    if (nodeFormDataObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> nodeDataList = (List<Map<String, Object>>) nodeFormDataObj;
                        nodeInstanceData = nodeDataList;
                        
                        // 如果有旧数据但没有 Map，将其添加到 Map 中（兼容旧流程）
                        if (nodeFormDataMap == null) {
                            nodeFormDataMap = new LinkedHashMap<>();
                        }
                        // 如果当前节点还没有数据，添加旧数据
                        if (nodeTaskDefinitionKey != null && !nodeFormDataMap.containsKey(nodeTaskDefinitionKey)) {
                            Map<String, Object> currentNodeData = new LinkedHashMap<>();
                            currentNodeData.put("nodeId", nodeTaskDefinitionKey);
                            Object nodeFormIdObj = variables.get("nodeFormId");
                            if (nodeFormIdObj != null) {
                                currentNodeData.put("nodeFormId", nodeFormIdObj);
                            }
                            currentNodeData.put("formData", nodeDataList);
                            currentNodeData.put("updateTime", java.time.Instant.now().toString());
                            nodeFormDataMap.put(nodeTaskDefinitionKey, currentNodeData);
                        }
                    }
                }
            } catch (Exception e) {
                // 获取失败，忽略
            }
        }
        
        // 8. 构建返回结果
        result.put("template", template);
        result.put("instanceData", instanceData);
        result.put("examFormId", examFormId);
        result.put("examFormName", examFormName);
        result.put("deployId", deployId);
        // 节点绑定的表单
        result.put("nodeExamFormId", nodeExamFormId);
        result.put("nodeTemplate", nodeTemplate);
        result.put("nodeInstanceData", nodeInstanceData);
        result.put("nodeFormDataMap", nodeFormDataMap);  // 所有历史节点的表单数据
        result.put("historicalNodeTemplates", historicalNodeTemplates);  // 所有历史节点的表单模板结构
        result.put("nodeTaskDefinitionKey", nodeTaskDefinitionKey);
        result.put("processDefinitionId", processDefinitionId);  // 流程定义ID
        result.put("processVariables", variables);  // 流程变量，供前端数据链接等使用（academicYear、major、course 等）
        
        return result;
    }

    /**
     * 根据流程定义ID获取第一个用户任务节点绑定的自定义表单
     *
     * @param deployId 流程定义ID
     * @return 第一个节点的表单数据，如果没有则返回null
     */
    public Map<String, Object> getFirstNodeFormByDeployId(String deployId) {
        if (StrUtil.isBlank(deployId)) {
            return null;
        }
        
        try {
            // 1. 根据 deployId 查找流程定义
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployId)
                    .latestVersion()
                    .singleResult();
            
            if (processDefinition == null) {
                log.warn("未找到流程定义: deployId={}", deployId);
                return null;
            }
            
            // 2. 获取 BPMN 模型
            Process process = repositoryService.getBpmnModel(processDefinition.getId()).getProcesses().get(0);
            if (process == null) {
                log.warn("未找到流程模型: processDefinitionId={}", processDefinition.getId());
                return null;
            }
            
            // 3. 查找第一个用户任务节点
            Collection<FlowElement> allElements = edu.xzit.core.core.flowable.flow.FlowableUtils.getAllElements(process.getFlowElements(), null);
            UserTask firstUserTask = null;
            for (FlowElement flowElement : allElements) {
                if (flowElement instanceof UserTask) {
                    firstUserTask = (UserTask) flowElement;
                    break; // 找到第一个就退出
                }
            }
            
            if (firstUserTask == null) {
                log.debug("流程中没有用户任务节点: deployId={}", deployId);
                return null;
            }
            
            // 4. 从 flowable:Properties 中读取 examFormId
            ExtensionElement propertiesElement = edu.xzit.core.core.flowable.flow.FlowableUtils
                    .getExtensionElementFromFlowElementByName(firstUserTask, "properties");
            
            if (propertiesElement == null) {
                log.debug("第一个用户任务节点没有 Properties 扩展元素: taskId={}", firstUserTask.getId());
                return null;
            }
            
            // 5. 获取 properties 下的 property 子元素
            List<ExtensionElement> propertyElements = propertiesElement.getChildElements().get("property");
            if (propertyElements == null || propertyElements.isEmpty()) {
                log.debug("Properties 中没有 property 子元素: taskId={}", firstUserTask.getId());
                return null;
            }
            
            // 6. 查找 examFormId 属性
            Long nodeExamFormId = null;
            for (ExtensionElement propertyElement : propertyElements) {
                List<ExtensionAttribute> nameAttrs = propertyElement.getAttributes().get("name");
                List<ExtensionAttribute> valueAttrs = propertyElement.getAttributes().get("value");
                if (nameAttrs != null && !nameAttrs.isEmpty() && valueAttrs != null && !valueAttrs.isEmpty()) {
                    String propertyName = nameAttrs.get(0).getValue();
                    String propertyValue = valueAttrs.get(0).getValue();
                    if ("examFormId".equals(propertyName) && StringUtils.isNotBlank(propertyValue)) {
                        try {
                            nodeExamFormId = Long.parseLong(propertyValue);
                            log.info("成功读取第一个节点绑定的表单ID: deployId={}, taskId={}, examFormId={}", 
                                    deployId, firstUserTask.getId(), nodeExamFormId);
                            break;
                        } catch (NumberFormatException e) {
                            log.warn("第一个节点 examFormId 格式错误: {}", propertyValue);
                        }
                    }
                }
            }
            
            // 7. 如果找到了 examFormId，获取表单数据
            if (nodeExamFormId != null) {
                Map<String, Object> nodeForm = getFormModules(nodeExamFormId, true);
                if (nodeForm != null) {
                    nodeForm.put("id", nodeExamFormId);  // 添加表单ID，供前端使用
                    nodeForm.put("nodeTaskDefinitionKey", firstUserTask.getId());
                    return nodeForm;
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("获取第一个节点表单失败: deployId={}", deployId, e);
            return null;
        }
    }
}
