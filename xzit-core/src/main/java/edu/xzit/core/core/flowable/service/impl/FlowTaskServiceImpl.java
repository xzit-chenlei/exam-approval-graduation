package edu.xzit.core.core.flowable.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.PictureType;
import com.deepoove.poi.data.Pictures;
import com.deepoove.poi.data.TextRenderData;
import com.deepoove.poi.data.style.Style;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.domain.entity.SysRole;
import edu.xzit.core.core.common.core.domain.entity.SysUser;
import edu.xzit.core.core.common.exception.ServiceException;
import edu.xzit.core.core.common.utils.ChartUtils;
import edu.xzit.core.core.common.utils.poi.WordUtil;
import edu.xzit.core.core.common.utils.SecurityUtils;
import edu.xzit.core.core.common.utils.poi.plugins.ComplexLoopRowTableRenderPolicy;
import edu.xzit.core.core.flowable.constant.ProcessConstants;
import edu.xzit.core.core.flowable.constant.StoreSceneNameConstants;
import edu.xzit.core.core.flowable.domain.ExamSignLocationInfo;
import edu.xzit.core.core.flowable.domain.ExamWordTemplateInfo;
import edu.xzit.core.core.flowable.domain.ExamWordTemplateNew;
import edu.xzit.core.core.flowable.domain.dto.FlowCommentDto;
import edu.xzit.core.core.flowable.domain.dto.FlowNextDto;
import edu.xzit.core.core.flowable.domain.dto.FlowTaskDto;
import edu.xzit.core.core.flowable.domain.dto.FlowViewerDto;
import edu.xzit.core.core.flowable.domain.vo.FlowQueryVo;
import edu.xzit.core.core.flowable.domain.vo.FlowTaskVo;
import edu.xzit.core.core.flowable.enums.ExamElectronicType;
import edu.xzit.core.core.flowable.enums.FlowComment;
import edu.xzit.core.core.flowable.enums.WordTemplateType;
import edu.xzit.core.examEnums.FormModuleType;
import edu.xzit.core.core.flowable.factory.FlowServiceFactory;
import edu.xzit.core.core.flowable.flow.CustomProcessDiagramGenerator;
import edu.xzit.core.core.flowable.flow.FindNextNodeUtil;
import edu.xzit.core.core.flowable.flow.FlowableUtils;
import edu.xzit.core.core.flowable.service.*;
import edu.xzit.core.dao.domain.SysForm;
import edu.xzit.core.dao.domain.SysStoreInfo;
import edu.xzit.core.dao.service.IExamTaskDeptService;
import edu.xzit.core.dao.domain.ExamTaskDept;
import edu.xzit.core.dao.service.ISysRoleService;
import edu.xzit.core.dao.service.ISysStoreInfoRepoService;
import edu.xzit.core.dao.service.ISysUserService;
import edu.xzit.core.model.dto.FileDTO;
import edu.xzit.core.service.SysDeptBizService;
import edu.xzit.core.util.HashUtil;
import edu.xzit.core.util.OssUtil;
import edu.xzit.graduate.service.CourseAbilityLinkDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.impl.cmd.AddMultiInstanceExecutionCmd;
import org.flowable.engine.impl.cmd.DeleteMultiInstanceExecutionCmd;
import org.flowable.variable.service.impl.util.CommandContextUtil;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static edu.xzit.core.core.common.utils.ChartUtils.CHART_COLORS;

/**
 * @author Tony
 * @date 2021-04-03
 **/
@Service
@Slf4j
public class FlowTaskServiceImpl extends FlowServiceFactory implements IFlowTaskService {

    /**
     * 标记驳回后强制选择下一节点审批人的流程变量
     */
    private static final String FORCE_SELECT_NEXT_ASSIGNEE = "forceSelectNextAssignee";
    /**
     * 记录触发强制选择的目标节点（便于前端识别）
     */
    private static final String FORCE_SELECT_NEXT_NODE = "forceSelectNextNode";

    /**
     * 审核内容（approval_opinion_table）固定 5 行配置
     * 根据不同流程 key 区分，对应模板中的 [reviewContent] 占位符
     */
    private static final Map<String, List<String>> APPROVAL_REVIEW_CONTENT_MAP = new HashMap<>();

    static {
        // 默认配置（未特别区分流程时使用）
        APPROVAL_REVIEW_CONTENT_MAP.put("default", Arrays.asList(
                "1.考核内容覆盖教学大纲要求，能有效支撑教学大纲规定的各项教学目标达成度　",
                "2.命题科学、难易得当、表述准确　",
                "3.题型、题量科学合理",
                "4.参考答案、评分标准规范齐全",
                "5.题目分值标准规范，试卷内容打印清晰"
        ));
    
        APPROVAL_REVIEW_CONTENT_MAP.put("flow_lf1rqhxe", Arrays.asList(
            "1.考核内容覆盖教学大纲要求，能有效支撑教学大纲规定的各项教学目标达成度　",
            "2.命题科学、难易得当、表述准确　",
            "3.题型、题量科学合理",
            "4.参考答案、评分标准规范齐全",
            "5.题目分值标准规范，试卷内容打印清晰"
        ));
        APPROVAL_REVIEW_CONTENT_MAP.put("flow_93qezqks", Arrays.asList(
            "1.考核内容覆盖教学大纲要求，能有效考核教学大纲规定的各项教学目标达成度　",
            "2.采用非试卷考核的原因充分、合理",
            "3.考核项目分解得当，与教学大纲课程目标吻合",
            "4.课程考核评分标准规范齐全"
        ));
    }

    /**
     * 流程签名位置配置：
     * flowKey + nodeId + sceneName -> Word 模板中的签名占位符（如 teacherSign、directorSign 等）
     * 方便在代码中集中维护映射关系。
     *
     * 使用示例（请按实际流程补充配置项）：
     * new SignaturePositionConfig("flow_lf1rqhxe", "Activity_083dyv5", "teacherSign", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
     * new SignaturePositionConfig("flow_lf1rqhxe", "Activity_11motab", "directorSign", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect())
     */
    private static final List<SignaturePositionConfig> SIGNATURE_POSITION_CONFIGS = Arrays.asList(
            // TODO 按实际流程补充配置示例：
            new SignaturePositionConfig("flow_lf1rqhxe", "Activity_083dyv5", "jiaoshi", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_lf1rqhxe", "Activity_1jqjknb", "xiaozu", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_lf1rqhxe", "Activity_1uhgnoc", "xiaozu2", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_lf1rqhxe", "Activity_09qv557", "zhuren", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_lf1rqhxe", "Activity_1qtp43q", "yuanzhang", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_lf1rqhxe", "Activity_1qtp43q", "tongyi", ExamElectronicType.ELECTRONIC_AGREE.getReflect()),
            new SignaturePositionConfig("flow_93qezqks", "Activity_11motab", "jiaoshi", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_93qezqks", "Activity_1uqcddv", "xiaozu", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_93qezqks", "Activity_1uwujle", "xiaozu2", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_93qezqks", "Activity_07cogfy", "zhuren", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_93qezqks", "Activity_0h2mou8", "yuanzhang", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_93qezqks", "Activity_0h2mou8", "tongyi", ExamElectronicType.ELECTRONIC_AGREE.getReflect()),
            new SignaturePositionConfig("flow_8teoxmeh", "Activity_1tjjzws", "pingjia", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_8teoxmeh", "Activity_18ovgbo", "shencha", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_72bddo0p", "Activity_1nxpxc0", "pingjia", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_72bddo0p", "Activity_031basy", "shencha", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_d4gkt8qp", "Activity_0h1ti5q", "zhuren", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect()),
            new SignaturePositionConfig("flow_d4gkt8qp", "Activity_00p4syb", "yuanzhang", ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect())
    );

    @Resource
    private ISysUserService sysUserService;
    @Resource
    private ISysRoleService sysRoleService;
    @Resource
    private ISysDeployFormService sysInstanceFormService;
    @Resource
    private ISysFormService sysFormService;

    @Resource
    private SysDeptBizService sysDeptBizService;

    @Resource
    private ISysStoreInfoRepoService sysStoreInfoRepoService;

    @Resource
    private ISysDeployFormService sysDeployFormService;

    @Resource
    private IExamSignLocationInfoRepoService examSignLocationInfoRepoService;

    @Resource
    private IExamTaskDeptService examTaskDeptService;

    @Resource
    private edu.xzit.core.dao.mapper.ExamTeachingResearchOfficeUserMapper examTeachingResearchOfficeUserMapper;

    @Resource
    private edu.xzit.core.dao.service.IExamTaskTeachingResearchOfficeService examTaskTeachingResearchOfficeService;


    @Resource
    IExamWordTemplateInfoRepoService examWordTemplateInfoRepoService;

    @Resource
    private IExamWordTemplateNewRepoService examWordTemplateNewRepoService;

    @Resource
    private edu.xzit.core.service.FormModuleService formModuleService;

    @Resource
    private CourseAbilityLinkDataService courseAbilityLinkDataService;



    /**
     * 完成任务
     *
     * @param taskVo 请求实体参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public AjaxResult complete(FlowTaskVo taskVo) {
        Task task = taskService.createTaskQuery().taskId(taskVo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            return AjaxResult.error("任务不存在");
        }
        // 下游指派所需的上下文
        Map<String, Object> variables = taskVo.getVariables();
        // 获取当前任务已有变量，确保能清理掉历史的 draft 状态
        Map<String, Object> currentVars = taskService.getVariables(taskVo.getTaskId());
        if (currentVars != null && "draft".equals(currentVars.get("status"))) {
            currentVars.remove("status");
            currentVars.put("status", null);
        }
        
        // 合并入参变量，入参优先
        // 对于 nodeFormDataMap，使用前端传递的新值（因为前端已经合并了所有历史数据）
        if (variables == null) {
            variables = currentVars;
        } else if (currentVars != null && !currentVars.isEmpty()) {
            // 合并其他变量（使用 putIfAbsent）
            for (Map.Entry<String, Object> entry : currentVars.entrySet()) {
                String key = entry.getKey();
                // nodeFormDataMap 必须使用前端传递的新值，不从 currentVars 中获取
                // 其他变量使用 putIfAbsent 避免覆盖前端传递的值
                if (!"nodeFormDataMap".equals(key)) {
                    variables.putIfAbsent(key, entry.getValue());
                }
            }
        }
        
        // 确保 nodeFormDataMap 是 JSON 字符串格式（如果前端传递的是对象，需要序列化）
        if (variables != null) {
            Object nodeFormDataMapObj = variables.get("nodeFormDataMap");
            if (nodeFormDataMapObj != null && !(nodeFormDataMapObj instanceof String)) {
                try {
                    variables.put("nodeFormDataMap", com.alibaba.fastjson2.JSON.toJSONString(nodeFormDataMapObj));
                } catch (Exception e) {
                    log.warn("序列化 nodeFormDataMap 失败", e);
                }
            }
        }
        String processDefinitionKey = null;
        if (StringUtils.isNotBlank(task.getProcessDefinitionId())) {
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            if (pd != null) {
                processDefinitionKey = pd.getKey();
            }
        }
        if (DelegationState.PENDING.equals(task.getDelegationState())) {
            taskService.addComment(taskVo.getTaskId(), taskVo.getInstanceId(), FlowComment.DELEGATE.getType(), taskVo.getComment());
            taskService.resolveTask(taskVo.getTaskId(), variables);
        } else {
            taskService.addComment(taskVo.getTaskId(), taskVo.getInstanceId(), FlowComment.NORMAL.getType(), taskVo.getComment());
            Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
            taskService.setAssignee(taskVo.getTaskId(), userId.toString());
            // 如果是暂存流程，移除暂存状态（兼容前端未传递 status 的情况，上面已清理 currentVars）
            taskService.complete(taskVo.getTaskId(), variables);
        }

        // 特定流程或后端标记需要在完成后指派下一节点审批人
        assignNextAssignees(task.getProcessInstanceId(), processDefinitionKey, task.getTaskDefinitionKey(), variables);
        return AjaxResult.success();
    }

    /**
     * 根据审批人选择结果，将下一节点任务分配/候选设置
     */
    private void assignNextAssignees(String processInstanceId, String processDefinitionKey, String currentTaskDefinitionKey, Map<String, Object> variables) {
        if (variables == null) {
            return;
        }
        Object approvalValue = variables.get(ProcessConstants.PROCESS_APPROVAL);
        if (Objects.isNull(approvalValue)) {

            return;
        }

        // 流程若已结束，直接返回，避免获取变量抛异常
        if (runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult() == null) {
            return;
        }
        boolean forceSelectFlag;
        try {
            forceSelectFlag = Boolean.TRUE.equals(runtimeService.getVariable(processInstanceId, FORCE_SELECT_NEXT_ASSIGNEE));
        } catch (FlowableObjectNotFoundException e) {
            // 实例已不存在，直接返回
            return;
        } catch (Exception e) {
            // 其他异常也直接返回，避免影响流程
            return;
        }
        // 定义流程key和对应需要筛选的节点ID映射
        Map<String, String> processNodeMap = new HashMap<>();
        processNodeMap.put("flow_lf1rqhxe", "Activity_083dyv5");
        processNodeMap.put("flow_93qezqks", "Activity_11motab");
        
        boolean inForceProcess = StringUtils.isNotBlank(processDefinitionKey) && processNodeMap.containsKey(processDefinitionKey);
        // 仅当流程匹配且当前节点匹配配置的节点时才进入指派逻辑
        if (!forceSelectFlag && (!inForceProcess || !processNodeMap.get(processDefinitionKey).equals(currentTaskDefinitionKey))) {
            return;
        }

        String approvalStr = approvalValue.toString();
        if (StringUtils.isBlank(approvalStr)) {
            return;
        }
        String[] userIds = approvalStr.split(",");
        List<String> cleanedUserIds = new ArrayList<>();
        for (String uid : userIds) {
            if (StringUtils.isNotBlank(uid)) {
                cleanedUserIds.add(uid.trim());
            }
        }
        if (cleanedUserIds.isEmpty()) {
            return;
        }

        // 获取当前实例的待办任务（通常是下一节点任务列表，包括并行）
        List<Task> nextTasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .list();

        if (CollectionUtils.isEmpty(nextTasks)) {
            return;
        }

        // 这里的 nextTasks 是当前节点完成后激活的“下一节点”任务列表；当前节点过滤已在入口校验
        List<Task> filteredTasks = nextTasks;

        if (CollectionUtils.isEmpty(filteredTasks)) {
            return;
        }

        if (cleanedUserIds.size() == filteredTasks.size()) {
            // 一一对应指派
            for (int i = 0; i < filteredTasks.size(); i++) {
                taskService.setAssignee(filteredTasks.get(i).getId(), cleanedUserIds.get(i));
            }
        } else {
            // 否则将全部审批人作为每个任务的候选人
            for (Task nextTask : filteredTasks) {
                for (String uid : cleanedUserIds) {
                    taskService.addCandidateUser(nextTask.getId(), uid);
                }
            }
        }
    }

    /**
     * 驳回任务
     *
     * @param flowTaskVo
     */
    @Override
    public void taskReject(FlowTaskVo flowTaskVo) {
        if (taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult().isSuspended()) {
            throw new ServiceException("任务处于挂起状态!");
        }
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult();
        // 获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
        // 获取所有节点信息
        Process process = repositoryService.getBpmnModel(processDefinition.getId()).getProcesses().get(0);
        // 获取全部节点列表，包含子节点
        Collection<FlowElement> allElements = FlowableUtils.getAllElements(process.getFlowElements(), null);
        // 获取当前任务节点元素
        FlowElement source = null;
        if (allElements != null) {
            for (FlowElement flowElement : allElements) {
                // 类型为用户节点
                if (flowElement.getId().equals(task.getTaskDefinitionKey())) {
                    // 获取节点信息
                    source = flowElement;
                }
            }
        }

        // 目的获取所有跳转到的节点 targetIds
        // 获取当前节点的所有父级用户任务节点
        // 深度优先算法思想：延边迭代深入
        List<UserTask> parentUserTaskList = FlowableUtils.iteratorFindParentUserTasks(source, null, null);
        if (parentUserTaskList == null || parentUserTaskList.size() == 0) {
            throw new ServiceException("当前节点为初始任务节点，不能驳回");
        }
        // 获取活动 ID 即节点 Key
        List<String> parentUserTaskKeyList = new ArrayList<>();
        parentUserTaskList.forEach(item -> parentUserTaskKeyList.add(item.getId()));
        // 获取全部历史节点活动实例，即已经走过的节点历史，数据采用开始时间升序
        List<HistoricTaskInstance> historicTaskInstanceList = historyService.createHistoricTaskInstanceQuery().processInstanceId(task.getProcessInstanceId()).orderByHistoricTaskInstanceStartTime().asc().list();
        // 数据清洗，将回滚导致的脏数据清洗掉
        List<String> lastHistoricTaskInstanceList = FlowableUtils.historicTaskInstanceClean(allElements, historicTaskInstanceList);
        // 此时历史任务实例为倒序，获取最后走的节点
        List<String> targetIds = new ArrayList<>();
        // 循环结束标识，遇到当前目标节点的次数
        int number = 0;
        StringBuilder parentHistoricTaskKey = new StringBuilder();
        for (String historicTaskInstanceKey : lastHistoricTaskInstanceList) {
            // 当会签时候会出现特殊的，连续都是同一个节点历史数据的情况，这种时候跳过
            if (parentHistoricTaskKey.toString().equals(historicTaskInstanceKey)) {
                continue;
            }
            parentHistoricTaskKey = new StringBuilder(historicTaskInstanceKey);
            if (historicTaskInstanceKey.equals(task.getTaskDefinitionKey())) {
                number++;
            }
            // 在数据清洗后，历史节点就是唯一一条从起始到当前节点的历史记录，理论上每个点只会出现一次
            // 在流程中如果出现循环，那么每次循环中间的点也只会出现一次，再出现就是下次循环
            // number == 1，第一次遇到当前节点
            // number == 2，第二次遇到，代表最后一次的循环范围
            if (number == 2) {
                break;
            }
            // 如果当前历史节点，属于父级的节点，说明最后一次经过了这个点，需要退回这个点
            if (parentUserTaskKeyList.contains(historicTaskInstanceKey)) {
                targetIds.add(historicTaskInstanceKey);
            }
        }


        // 目的获取所有需要被跳转的节点 currentIds
        // 取其中一个父级任务，因为后续要么存在公共网关，要么就是串行公共线路
        UserTask oneUserTask = parentUserTaskList.get(0);
        // 获取所有正常进行的任务节点 Key，这些任务不能直接使用，需要找出其中需要撤回的任务
        List<Task> runTaskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        List<String> runTaskKeyList = new ArrayList<>();
        runTaskList.forEach(item -> runTaskKeyList.add(item.getTaskDefinitionKey()));
        // 需驳回任务列表
        List<String> currentIds = new ArrayList<>();
        // 通过父级网关的出口连线，结合 runTaskList 比对，获取需要撤回的任务
        List<UserTask> currentUserTaskList = FlowableUtils.iteratorFindChildUserTasks(oneUserTask, runTaskKeyList, null, null);
        currentUserTaskList.forEach(item -> currentIds.add(item.getId()));


        // 规定：并行网关之前节点必须需存在唯一用户任务节点，如果出现多个任务节点，则并行网关节点默认为结束节点，原因为不考虑多对多情况
        if (targetIds.size() > 1 && currentIds.size() > 1) {
            throw new ServiceException("任务出现多对多情况，无法撤回");
        }

        // 特定流程+节点的驳回，标记后续节点需要强制选择审批人
        String processDefinitionKey = processDefinition.getKey();
        boolean needForceSelectAssignee =
                ("flow_lf1rqhxe".equals(processDefinitionKey) && targetIds.contains("Activity_083dyv5"))
                        || ("flow_93qezqks".equals(processDefinitionKey) && targetIds.contains("Activity_11motab"));
        if (needForceSelectAssignee) {
            // 设置流程变量，前端可据此弹窗选择下一节点审批人
            runtimeService.setVariable(task.getProcessInstanceId(), FORCE_SELECT_NEXT_ASSIGNEE, true);
            // 同时记录当前被驳回的节点，方便前端校验
            runtimeService.setVariable(task.getProcessInstanceId(), FORCE_SELECT_NEXT_NODE,
                    targetIds.isEmpty() ? "" : targetIds.get(0));
        }

        // 循环获取那些需要被撤回的节点的ID，用来设置驳回原因
        List<String> currentTaskIds = new ArrayList<>();
        currentIds.forEach(currentId -> runTaskList.forEach(runTask -> {
            if (currentId.equals(runTask.getTaskDefinitionKey())) {
                currentTaskIds.add(runTask.getId());
            }
        }));
        // 设置驳回意见
        currentTaskIds.forEach(item -> taskService.addComment(item, task.getProcessInstanceId(), FlowComment.REJECT.getType(), flowTaskVo.getComment()));

        try {
            // 如果父级任务多于 1 个，说明当前节点不是并行节点，原因为不考虑多对多情况
            if (targetIds.size() > 1) {
                // 1 对 多任务跳转，currentIds 当前节点(1)，targetIds 跳转到的节点(多)
                runtimeService.createChangeActivityStateBuilder()
                        .processInstanceId(task.getProcessInstanceId()).
                        moveSingleActivityIdToActivityIds(currentIds.get(0), targetIds).changeState();
            }
            // 如果父级任务只有一个，因此当前任务可能为网关中的任务
            if (targetIds.size() == 1) {
                // 1 对 1 或 多 对 1 情况，currentIds 当前要跳转的节点列表(1或多)，targetIds.get(0) 跳转到的节点(1)
                runtimeService.createChangeActivityStateBuilder()
                        .processInstanceId(task.getProcessInstanceId())
                        .moveActivityIdsToSingleActivityId(currentIds, targetIds.get(0)).changeState();
            }
        } catch (FlowableObjectNotFoundException e) {
            throw new ServiceException("未找到流程实例，流程可能已发生变化");
        } catch (FlowableException e) {
            throw new ServiceException("无法取消或开始活动");
        }

    }

    /**
     * 退回任务
     *
     * @param flowTaskVo 请求实体参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void taskReturn(FlowTaskVo flowTaskVo) {
        if (taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult().isSuspended()) {
            throw new ServiceException("任务处于挂起状态");
        }
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult();
        // 获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
        // 获取所有节点信息
        Process process = repositoryService.getBpmnModel(processDefinition.getId()).getProcesses().get(0);
        // 获取全部节点列表，包含子节点
        Collection<FlowElement> allElements = FlowableUtils.getAllElements(process.getFlowElements(), null);
        // 获取当前任务节点元素
        FlowElement source = null;
        // 获取跳转的节点元素
        FlowElement target = null;
        if (allElements != null) {
            for (FlowElement flowElement : allElements) {
                // 当前任务节点元素
                if (flowElement.getId().equals(task.getTaskDefinitionKey())) {
                    source = flowElement;
                }
                // 跳转的节点元素
                if (flowElement.getId().equals(flowTaskVo.getTargetKey())) {
                    target = flowElement;
                }
            }
        }

        // 标记是否退回到开始节点（即与开始事件直接相连的首个用户任务）
        boolean returnToStart = false;
        if (target instanceof UserTask) {
            UserTask targetUserTask = (UserTask) target;
            List<SequenceFlow> incomingFlows = targetUserTask.getIncomingFlows();
            if (CollectionUtils.isNotEmpty(incomingFlows)) {
                FlowElement initialFlowElement = process.getInitialFlowElement();
                String startElementId = Objects.nonNull(initialFlowElement) ? initialFlowElement.getId() : StringUtils.EMPTY;
                for (SequenceFlow incomingFlow : incomingFlows) {
                    FlowElement sourceElement = process.getFlowElement(incomingFlow.getSourceRef());
                    if (sourceElement instanceof StartEvent || (StringUtils.isNotBlank(startElementId) && startElementId.equals(incomingFlow.getSourceRef()))) {
                        returnToStart = true;
                        break;
                    }
                }
            }
        }

        // 从当前节点向前扫描
        // 如果存在路线上不存在目标节点，说明目标节点是在网关上或非同一路线上，不可跳转
        // 否则目标节点相对于当前节点，属于串行
        Boolean isSequential = FlowableUtils.iteratorCheckSequentialReferTarget(source, flowTaskVo.getTargetKey(), null, null);
        if (!isSequential) {
            throw new ServiceException("当前节点相对于目标节点，不属于串行关系，无法回退");
        }


        // 获取所有正常进行的任务节点 Key，这些任务不能直接使用，需要找出其中需要撤回的任务
        List<Task> runTaskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        List<String> runTaskKeyList = new ArrayList<>();
        runTaskList.forEach(item -> runTaskKeyList.add(item.getTaskDefinitionKey()));
        // 需退回任务列表
        List<String> currentIds = new ArrayList<>();
        // 通过父级网关的出口连线，结合 runTaskList 比对，获取需要撤回的任务
        List<UserTask> currentUserTaskList = FlowableUtils.iteratorFindChildUserTasks(target, runTaskKeyList, null, null);
        currentUserTaskList.forEach(item -> currentIds.add(item.getId()));

        // 循环获取那些需要被撤回的节点的ID，用来设置驳回原因
        List<String> currentTaskIds = new ArrayList<>();
        currentIds.forEach(currentId -> runTaskList.forEach(runTask -> {
            if (currentId.equals(runTask.getTaskDefinitionKey())) {
                currentTaskIds.add(runTask.getId());
            }
        }));
        // 设置回退意见
        currentTaskIds.forEach(currentTaskId -> taskService.addComment(currentTaskId, task.getProcessInstanceId(), FlowComment.REBACK.getType(), flowTaskVo.getComment()));

        try {
            // 1 对 1 或 多 对 1 情况，currentIds 当前要跳转的节点列表(1或多)，targetKey 跳转到的节点(1)
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(task.getProcessInstanceId())
                    .moveActivityIdsToSingleActivityId(currentIds, flowTaskVo.getTargetKey()).changeState();
        } catch (FlowableObjectNotFoundException e) {
            throw new ServiceException("未找到流程实例，流程可能已发生变化");
        } catch (FlowableException e) {
            throw new ServiceException("无法取消或开始活动");
        }

        // 如果退回到开始节点，将审批人指定为流程发起人
        if (returnToStart) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            if (processInstance != null && StringUtils.isNotBlank(processInstance.getStartUserId())) {
                List<Task> targetTasks = taskService.createTaskQuery()
                        .processInstanceId(task.getProcessInstanceId())
                        .taskDefinitionKey(flowTaskVo.getTargetKey())
                        .list();
                targetTasks.forEach(newTask -> taskService.setAssignee(newTask.getId(), processInstance.getStartUserId()));
            }
        }
    }


    /**
     * 获取所有可回退的节点
     *
     * @param flowTaskVo
     * @return
     */
    @Override
    public AjaxResult findReturnTaskList(FlowTaskVo flowTaskVo) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult();
        // 获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
        // 获取所有节点信息，暂不考虑子流程情况
        Process process = repositoryService.getBpmnModel(processDefinition.getId()).getProcesses().get(0);
        Collection<FlowElement> flowElements = process.getFlowElements();
        // 获取当前任务节点元素
        UserTask source = null;
        if (flowElements != null) {
            for (FlowElement flowElement : flowElements) {
                // 类型为用户节点
                if (flowElement.getId().equals(task.getTaskDefinitionKey())) {
                    source = (UserTask) flowElement;
                }
            }
        }
        // 获取节点的所有路线
        List<List<UserTask>> roads = FlowableUtils.findRoad(source, null, null, null);
        // 可回退的节点列表
        List<UserTask> userTaskList = new ArrayList<>();
        for (List<UserTask> road : roads) {
            if (userTaskList.size() == 0) {
                // 还没有可回退节点直接添加
                userTaskList = road;
            } else {
                // 如果已有回退节点，则比对取交集部分
                userTaskList.retainAll(road);
            }
        }
        return AjaxResult.success(userTaskList);
    }

    /**
     * 删除任务
     *
     * @param flowTaskVo 请求实体参数
     */
    @Override
    public void deleteTask(FlowTaskVo flowTaskVo) {
        // todo 待确认删除任务是物理删除任务 还是逻辑删除，让这个任务直接通过？
        taskService.deleteTask(flowTaskVo.getTaskId(), flowTaskVo.getComment());
    }

    /**
     * 认领/签收任务
     * 认领以后,这个用户就会成为任务的执行人,任务会从其他成员的任务列表中消失
     *
     * @param flowTaskVo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claim(FlowTaskVo flowTaskVo) {
        taskService.claim(flowTaskVo.getTaskId(), flowTaskVo.getUserId());
    }

    /**
     * 取消认领/签收任务
     *
     * @param flowTaskVo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unClaim(FlowTaskVo flowTaskVo) {
        taskService.unclaim(flowTaskVo.getTaskId());
    }

    /**
     * 委派任务
     * 任务委派只是委派人将当前的任务交给被委派人进行审批，处理任务后又重新回到委派人身上。
     *
     * @param flowTaskVo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(FlowTaskVo flowTaskVo) {
        if (Objects.isNull(flowTaskVo) || StringUtils.isBlank(flowTaskVo.getTaskId())) {
            throw new ServiceException("委派任务参数异常");
        }
        if (StringUtils.isBlank(flowTaskVo.getAssignee())) {
            throw new ServiceException("请选择委派办理人");
        }
        Task task = taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            throw new ServiceException("当前任务不存在或已处理");
        }
        if (task.isSuspended()) {
            throw new ServiceException("任务处于挂起状态, 暂无法委派");
        }
        if (StringUtils.isBlank(flowTaskVo.getInstanceId())) {
            flowTaskVo.setInstanceId(task.getProcessInstanceId());
        }
        // 记录委派意见
        taskService.addComment(task.getId(), flowTaskVo.getInstanceId(),
                FlowComment.DELEGATE.getType(), flowTaskVo.getComment());
        taskService.delegateTask(task.getId(), flowTaskVo.getAssignee());
    }

    /**
     * 任务归还
     * 被委派人完成任务之后，将任务归还委派人
     *
     * @param flowTaskVo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveTask(FlowTaskVo flowTaskVo) {
        taskService.resolveTask(flowTaskVo.getTaskId());
    }


    /**
     * 转办任务
     * 直接将办理人换成别人，这时任务的拥有者不再是转办人
     *
     * @param flowTaskVo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignTask(FlowTaskVo flowTaskVo) {
        // 直接转派就可以覆盖掉之前的
        taskService.setAssignee(flowTaskVo.getTaskId(), flowTaskVo.getAssignee());
//        // 删除指派人重新指派
//        taskService.deleteCandidateUser(flowTaskVo.getTaskId(),flowTaskVo.getAssignee());
//        taskService.addCandidateUser(flowTaskVo.getTaskId(),flowTaskVo.getAssignee());
//        // 如果要查询转给他人处理的任务，可以同时将OWNER进行设置：
//        taskService.setOwner(flowTaskVo.getTaskId(), flowTaskVo.getAssignee());

    }

    /**
     * 多实例加签
     * act_ru_task、act_ru_identitylink各生成一条记录
     *
     * @param flowTaskVo
     */
    @Override
    public void addMultiInstanceExecution(FlowTaskVo flowTaskVo) {
        managementService.executeCommand(new AddMultiInstanceExecutionCmd(flowTaskVo.getDefId(), flowTaskVo.getInstanceId(), flowTaskVo.getVariables()));
    }

    /**
     * 多实例减签
     * act_ru_task减1、act_ru_identitylink不变
     *
     * @param flowTaskVo
     */
    @Override
    public void deleteMultiInstanceExecution(FlowTaskVo flowTaskVo) {
        managementService.executeCommand(new DeleteMultiInstanceExecutionCmd(flowTaskVo.getCurrentChildExecutionId(), flowTaskVo.getFlag()));
    }

    /**
     * 我发起的流程
     *
     * @param queryVo 请求参数
     * @return
     */
    @Override
    public AjaxResult myProcess(FlowQueryVo queryVo) {
        Page<FlowTaskDto> page = new Page<>();
        Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId.toString())
                .orderByProcessInstanceStartTime()
                .desc();
        List<HistoricProcessInstance> historicProcessInstances = historicProcessInstanceQuery.listPage(queryVo.getPageSize() * (queryVo.getPageNum() - 1), queryVo.getPageSize());
        page.setTotal(historicProcessInstanceQuery.count());
        List<FlowTaskDto> flowList = new ArrayList<>();
        for (HistoricProcessInstance hisIns : historicProcessInstances) {
            FlowTaskDto flowTask = new FlowTaskDto();
            flowTask.setCreateTime(hisIns.getStartTime());
            flowTask.setFinishTime(hisIns.getEndTime());
            flowTask.setProcInsId(hisIns.getId());
            // 补充流程定义ID，前端继续编辑需要 procDefId
            flowTask.setProcDefId(hisIns.getProcessDefinitionId());

            // 计算耗时
            if (Objects.nonNull(hisIns.getEndTime())) {
                long time = hisIns.getEndTime().getTime() - hisIns.getStartTime().getTime();
                flowTask.setDuration(getDate(time));
            } else {
                long time = System.currentTimeMillis() - hisIns.getStartTime().getTime();
                flowTask.setDuration(getDate(time));
            }
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(hisIns.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setCategory(pd.getCategory());
            flowTask.setProcDefVersion(pd.getVersion());


            // 检查流程状态（从流程变量中获取）
            Map<String, Object> processVariables = null;
            try {
                // 先尝试从运行时变量获取
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                        .processInstanceId(hisIns.getId())
                        .singleResult();
                if (processInstance != null) {
                    processVariables = runtimeService.getVariables(hisIns.getId());
                }
            } catch (Exception e) {
                // 运行时实例可能不存在，继续从历史变量获取
            }
            // 表单模式过滤：
            // - formMode = custom：只要自定义表单（包括老数据：没有 formMode 但有 examFormId/examFormData）
            // - formMode = vform 或 为空：排除自定义表单（包括老数据：没有 formMode 但有 examFormId/examFormData），保留 vform + 纯老数据
            String queryFormMode = queryVo.getFormMode();
            
            if (processVariables == null || processVariables.isEmpty()) {
                // 如果运行时变量为空，尝试从历史变量中获取
                HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(hisIns.getId())
                        .includeProcessVariables()
                        .singleResult();
                if (historicInstance != null && historicInstance.getProcessVariables() != null) {
                    processVariables = historicInstance.getProcessVariables();
                }
            }
            
            // 判断是否为暂存状态
            if (processVariables != null && "draft".equals(processVariables.get("status"))) {
                flowTask.setStatus("draft");
            } else if (Objects.nonNull(hisIns.getEndTime())) {
                flowTask.setStatus("finished");
            } else {
                flowTask.setStatus("running");
            }

            // 回传表单模式，便于前端兜底过滤
            if (processVariables != null && processVariables.get("formMode") != null) {
                flowTask.setFormMode(processVariables.get("formMode").toString());
            }
            
            // 当前所处流程
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(hisIns.getId()).list();
            if (CollectionUtils.isNotEmpty(taskList)) {
                flowTask.setTaskId(taskList.get(0).getId());
                flowTask.setTaskName(taskList.get(0).getName());
                Map<String, Object> variables = taskService.getVariables(taskList.get(0).getId());
                if(Objects.nonNull(variables.get("deptName"))){
                    flowTask.setExDeptName(variables.get("deptName").toString());
                }
                if(Objects.nonNull(variables.get("officeName"))){
                    flowTask.setOfficeName(variables.get("officeName").toString());
                }
                if (StringUtils.isNotBlank(taskList.get(0).getAssignee())) {
                    // 当前任务节点办理人信息
                    SysUser sysUser = sysUserService.selectUserById(Long.parseLong(taskList.get(0).getAssignee()));
                    if (Objects.nonNull(sysUser)) {
                        flowTask.setAssigneeId(sysUser.getUserId());
                        flowTask.setAssigneeName(sysUser.getNickName());
//                        flowTask.setAssigneeDeptName(Objects.nonNull(sysUser.getDept()) ? sysUser.getDept().getDeptName() : "");
                    }
                }
            } else {
                List<HistoricTaskInstance> historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                                .includeProcessVariables()
                                .processInstanceId(hisIns.getId()).orderByHistoricTaskInstanceEndTime().desc().list();
                flowTask.setTaskId(historicTaskInstance.get(0).getId());
                flowTask.setTaskName(historicTaskInstance.get(0).getName());
                Map<String, Object> variables = historicTaskInstance.get(0).getProcessVariables();
                if(Objects.nonNull(variables.get("deptName"))){
                    flowTask.setExDeptName(variables.get("deptName").toString());
                }
                if(Objects.nonNull(variables.get("officeName"))){
                    flowTask.setOfficeName(variables.get("officeName").toString());
                }
                if (StringUtils.isNotBlank(historicTaskInstance.get(0).getAssignee())) {
                    // 当前任务节点办理人信息
                    SysUser sysUser = sysUserService.selectUserById(Long.parseLong(historicTaskInstance.get(0).getAssignee()));
                    if (Objects.nonNull(sysUser)) {
                        flowTask.setAssigneeId(sysUser.getUserId());
                        flowTask.setAssigneeName(sysUser.getNickName());
//                        flowTask.setAssigneeDeptName(Objects.nonNull(sysUser.getDept()) ? sysUser.getDept().getDeptName() : "");
                    }
                }
            }
            flowList.add(flowTask);
        }
        page.setRecords(flowList);
        return AjaxResult.success(page);
    }

    /**
     * 管理员查看所有流程
     *
     * @param queryVo 请求参数
     * @return
     */
    @Override
    public AjaxResult allProcess(FlowQueryVo queryVo) {
        Page<FlowTaskDto> page = new Page<>();
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
                .orderByProcessInstanceStartTime()
                .desc();
        
        // 先查询所有流程实例（不分页），用于后续筛选
        List<HistoricProcessInstance> allHistoricProcessInstances = historicProcessInstanceQuery.list();
        // 如果指定了流程实例ID，先按实例ID过滤
        if (queryVo.getProcInsId() != null && StringUtils.isNotBlank(queryVo.getProcInsId())) {
            allHistoricProcessInstances = allHistoricProcessInstances.stream()
                    .filter(his -> queryVo.getProcInsId().equals(his.getId()))
                    .collect(Collectors.toList());
        }

        // 预先根据课题组/教研室关联表过滤可用的流程实例ID（避免读取流程变量）
        Set<String> allowByDept = null;
        if (Objects.nonNull(queryVo.getDeptId())) {
            List<String> ids = examTaskDeptService.selectTaskIdsByDeptId(Collections.singletonList(queryVo.getDeptId()));
            allowByDept = new HashSet<>(ids);
        }
        Set<String> allowByOffice = null;
        if (Objects.nonNull(queryVo.getOfficeId())) {
            List<String> ids = examTaskTeachingResearchOfficeService.selectTaskIdsByOfficeIds(Collections.singletonList(queryVo.getOfficeId()));
            allowByOffice = new HashSet<>(ids);
        }
        
        // 流程名称筛选：先通过流程定义名称查找流程定义ID列表
        Set<String> processDefinitionIdSet = null;
        if (StringUtils.isNotBlank(queryVo.getProcDefName())) {
            List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionNameLike("%" + queryVo.getProcDefName() + "%")
                    .list();
            if (CollectionUtils.isNotEmpty(processDefinitions)) {
                processDefinitionIdSet = new HashSet<>();
                for (ProcessDefinition pd : processDefinitions) {
                    processDefinitionIdSet.add(pd.getId());
                }
            } else {
                // 如果没有找到匹配的流程定义，返回空结果
                page.setTotal(0);
                page.setRecords(new ArrayList<>());
                return AjaxResult.success(page);
            }
        }
        
        // 是否配备模板筛选：预先查询模板信息（按 formId+deptId 维度）
        Map<Long, Set<Long>> templateDeptMap = null;       // formId -> deptId set（配备模板）
        Map<Long, Set<Long>> updatedDeptMap = null;        // formId -> deptId set（30天内更新）
        Map<Long, Set<Long>> notUpdatedDeptMap = null;     // formId -> deptId set（30天前未更新）
        if (Objects.nonNull(queryVo.getTemplateStatus())) {
            // 计算30天前的时间
            Date thirtyDaysAgo = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
            
            // 查询word_template_type='1'的所有模板
            ExamWordTemplateInfo templateQuery = new ExamWordTemplateInfo();
            templateQuery.setWordTemplateType("1");
            List<ExamWordTemplateInfo> allTemplates = examWordTemplateInfoRepoService.selectExamWordTemplateInfoList(templateQuery);
            
            // 获取所有配备模板的deptId列表
            templateDeptMap = new HashMap<>();
            updatedDeptMap = new HashMap<>();
            notUpdatedDeptMap = new HashMap<>();
            for (ExamWordTemplateInfo template : allTemplates) {
                Long formId = template.getFormId();
                Long deptId = template.getDeptId();
                if (formId == null || deptId == null) continue;

                templateDeptMap.computeIfAbsent(formId, k -> new HashSet<>()).add(deptId);
                if (template.getUpdateTime() != null && template.getUpdateTime().after(thirtyDaysAgo)) {
                    updatedDeptMap.computeIfAbsent(formId, k -> new HashSet<>()).add(deptId);
                } else {
                    notUpdatedDeptMap.computeIfAbsent(formId, k -> new HashSet<>()).add(deptId);
                }
            }
        }
        
        // 筛选流程名称、课题组和教研室
        List<HistoricProcessInstance> filteredInstances = new ArrayList<>();
        for (HistoricProcessInstance hisIns : allHistoricProcessInstances) {
            boolean match = true;
            // 表单模式过滤：
            // - formMode = custom：只要自定义表单（包括老数据：没有 formMode 但有 examFormId/examFormData）
            // - formMode = vform 或 为空：排除自定义表单（包括老数据：没有 formMode 但有 examFormId/examFormData），保留 vform + 纯老数据
            if (match) {
                String queryFormMode = queryVo.getFormMode();
                Map<String, Object> variables = null;
                if (StringUtils.isNotBlank(queryFormMode)) {
                    HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
                            .processInstanceId(hisIns.getId())
                            .includeProcessVariables()
                            .singleResult();
                    if (historicInstance != null && historicInstance.getProcessVariables() != null) {
                        variables = historicInstance.getProcessVariables();
                    }
                }
                if (StringUtils.isNotBlank(queryFormMode) && variables != null) {
                    String instanceFormMode = variables.get("formMode") != null
                            ? variables.get("formMode").toString()
                            : null;
                    boolean looksCustom = variables.containsKey("examFormId") || variables.containsKey("examFormData");
                    if ("custom".equals(queryFormMode)) {
                        if (!"custom".equals(instanceFormMode) && !looksCustom) {
                            match = false;
                        }
                    } else {
                        // vform 或 未传：过滤掉自定义表单（包括老数据）
                        if ("custom".equals(instanceFormMode) || (instanceFormMode == null && looksCustom)) {
                            match = false;
                        }
                    }
                }
            }
            
            // 流程名称筛选
            if (processDefinitionIdSet != null && !processDefinitionIdSet.contains(hisIns.getProcessDefinitionId())) {
                match = false;
            }
            
            // 是否完结筛选
            if (match && Objects.nonNull(queryVo.getFinished())) {
                boolean isFinished = Objects.nonNull(hisIns.getEndTime());
                if (queryVo.getFinished() != isFinished) {
                    match = false;
                }
            }
            
            // 课题组和教研室筛选：使用关联表，不再读取流程变量
            if (match && allowByDept != null && !allowByDept.contains(hisIns.getId())) {
                match = false;
            }
            if (match && allowByOffice != null && !allowByOffice.contains(hisIns.getId())) {
                match = false;
            }
            
            // 是否配备模板筛选（使用关联表的deptId，不再读取流程变量）
            if (match && Objects.nonNull(queryVo.getTemplateStatus())) {
                Long processDeptId = examTaskDeptService.selectDeptIdByTaskId(hisIns.getId());

                // 通过部署ID查找挂载的表单，从而匹配模板的 formId
                String deployId = null;
                Long formId = null;
                ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionId(hisIns.getProcessDefinitionId())
                        .singleResult();
                if (pd != null) {
                    deployId = pd.getDeploymentId();
                }
                if (StringUtils.isNotBlank(deployId)) {
                    SysForm sysForm = sysDeployFormService.selectSysDeployFormByDeployId(deployId);
                    if (sysForm != null && sysForm.getFormId() != null) {
                        formId = sysForm.getFormId();
                    }
                }

                Set<Long> templateDeptIds = formId == null ? null : templateDeptMap == null ? null : templateDeptMap.get(formId);
                Set<Long> updatedDeptIds = formId == null ? null : updatedDeptMap == null ? null : updatedDeptMap.get(formId);
                Set<Long> notUpdatedDeptIds = formId == null ? null : notUpdatedDeptMap == null ? null : notUpdatedDeptMap.get(formId);

                if (processDeptId != null && templateDeptIds != null) {
                    boolean templateMatch = false;
                    switch (queryVo.getTemplateStatus()) {
                        case 1: // 未配备模板：流程的deptId不在模板deptId列表中
                            templateMatch = !templateDeptIds.contains(processDeptId);
                            break;
                        case 2: // 月内未更新模板：流程的deptId在月内未更新的列表中
                            templateMatch = notUpdatedDeptIds != null && notUpdatedDeptIds.contains(processDeptId);
                            break;
                        case 3: // 月内已更新模板：流程的deptId在月内已更新的列表中
                            templateMatch = updatedDeptIds != null && updatedDeptIds.contains(processDeptId);
                            break;
                        case 4: // 未配备||月内未更新：选项1和2的合集
                            templateMatch = !templateDeptIds.contains(processDeptId) ||
                                          (notUpdatedDeptIds != null && notUpdatedDeptIds.contains(processDeptId));
                            break;
                    }

                    if (!templateMatch) {
                        match = false;
                    }
                } else {
                    // 如果关联表没有deptId或找不到匹配的formId，根据选项判断
                    // 选项1和4：未配备模板，没有deptId也算未配备
                    if (queryVo.getTemplateStatus() == 1 || queryVo.getTemplateStatus() == 4) {
                        // 匹配
                    } else {
                        // 其他选项需要deptId+formId，没有则不匹配
                        match = false;
                    }
                }
            }
            
            if (match) {
                filteredInstances.add(hisIns);
            }
        }
        
        // 手动分页
        int total = filteredInstances.size();
        int pageNum = queryVo.getPageNum();
        int pageSize = queryVo.getPageSize();
        int fromIndex = pageSize * (pageNum - 1);
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<HistoricProcessInstance> historicProcessInstances;
        if (fromIndex >= total) {
            historicProcessInstances = new ArrayList<>();
        } else {
            historicProcessInstances = filteredInstances.subList(fromIndex, toIndex);
        }
        
        page.setTotal(total);
        
        List<FlowTaskDto> flowList = new ArrayList<>();
        for (HistoricProcessInstance hisIns : historicProcessInstances) {
            FlowTaskDto flowTask = new FlowTaskDto();
            flowTask.setCreateTime(hisIns.getStartTime());
            flowTask.setFinishTime(hisIns.getEndTime());
            flowTask.setProcInsId(hisIns.getId());

            // 计算耗时
            if (Objects.nonNull(hisIns.getEndTime())) {
                long time = hisIns.getEndTime().getTime() - hisIns.getStartTime().getTime();
                flowTask.setDuration(getDate(time));
            } else {
                long time = System.currentTimeMillis() - hisIns.getStartTime().getTime();
                flowTask.setDuration(getDate(time));
            }
            
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(hisIns.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setCategory(pd.getCategory());

            // 流程发起人信息
            if (StringUtils.isNotBlank(hisIns.getStartUserId())) {
                try {
                    SysUser startUser = sysUserService.selectUserById(Long.parseLong(hisIns.getStartUserId()));
                    if (Objects.nonNull(startUser)) {
                        flowTask.setStartUserId(startUser.getUserId().toString());
                        flowTask.setStartUserName(startUser.getNickName());
                    }
                } catch (Exception e) {
                    // 忽略异常
                }
            }

            // 当前所处流程
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(hisIns.getId()).list();
            if (CollectionUtils.isNotEmpty(taskList)) {
                flowTask.setTaskId(taskList.get(0).getId());
                flowTask.setTaskName(taskList.get(0).getName());
                Map<String, Object> variables = taskService.getVariables(taskList.get(0).getId());
                if(Objects.nonNull(variables.get("deptName"))){
                    flowTask.setExDeptName(variables.get("deptName").toString());
                }
                if(Objects.nonNull(variables.get("officeName"))){
                    flowTask.setOfficeName(variables.get("officeName").toString());
                }
                if (StringUtils.isNotBlank(taskList.get(0).getAssignee())) {
                    // 当前任务节点办理人信息
                    SysUser sysUser = sysUserService.selectUserById(Long.parseLong(taskList.get(0).getAssignee()));
                    if (Objects.nonNull(sysUser)) {
                        flowTask.setAssigneeId(sysUser.getUserId());
                        flowTask.setAssigneeName(sysUser.getNickName());
                    }
                }
            } else {
                List<HistoricTaskInstance> historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                                .includeProcessVariables()
                                .processInstanceId(hisIns.getId()).orderByHistoricTaskInstanceEndTime().desc().list();
                if (CollectionUtils.isNotEmpty(historicTaskInstance)) {
                    flowTask.setTaskId(historicTaskInstance.get(0).getId());
                    flowTask.setTaskName(historicTaskInstance.get(0).getName());
                    Map<String, Object> variables = historicTaskInstance.get(0).getProcessVariables();
                    if(Objects.nonNull(variables.get("deptName"))){
                        flowTask.setExDeptName(variables.get("deptName").toString());
                    }
                    if(Objects.nonNull(variables.get("officeName"))){
                        flowTask.setOfficeName(variables.get("officeName").toString());
                    }
                    if (StringUtils.isNotBlank(historicTaskInstance.get(0).getAssignee())) {
                        // 当前任务节点办理人信息
                        SysUser sysUser = sysUserService.selectUserById(Long.parseLong(historicTaskInstance.get(0).getAssignee()));
                        if (Objects.nonNull(sysUser)) {
                            flowTask.setAssigneeId(sysUser.getUserId());
                            flowTask.setAssigneeName(sysUser.getNickName());
                        }
                    }
                }
            }
            flowList.add(flowTask);
        }
        page.setRecords(flowList);
        return AjaxResult.success(page);
    }

    /**
     * 取消申请
     * 目前实现方式: 直接将当前流程变更为已完成
     *
     * @param flowTaskVo
     * @return
     */
    @Override
    public AjaxResult stopProcess(FlowTaskVo flowTaskVo) {
        List<Task> task = taskService.createTaskQuery().processInstanceId(flowTaskVo.getInstanceId()).list();
        if (CollectionUtils.isEmpty(task)) {
            throw new ServiceException("流程未启动或已执行完成，取消申请失败");
        }
        // 获取当前流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(flowTaskVo.getInstanceId())
                .singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        if (Objects.nonNull(bpmnModel)) {
            Process process = bpmnModel.getMainProcess();
            List<EndEvent> endNodes = process.findFlowElementsOfType(EndEvent.class, false);
            if (CollectionUtils.isNotEmpty(endNodes)) {
                // TODO 取消流程为什么要设置流程发起人?
//                SysUser loginUser = SecurityUtils.getLoginUser().getUser();
//                Authentication.setAuthenticatedUserId(loginUser.getUserId().toString());

//                taskService.addComment(task.getId(), processInstance.getProcessInstanceId(), FlowComment.STOP.getType(),
//                        StringUtils.isBlank(flowTaskVo.getComment()) ? "取消申请" : flowTaskVo.getComment());
                // 获取当前流程最后一个节点
                String endId = endNodes.get(0).getId();
                List<Execution> executions = runtimeService.createExecutionQuery()
                        .parentId(processInstance.getProcessInstanceId()).list();
                List<String> executionIds = new ArrayList<>();
                executions.forEach(execution -> executionIds.add(execution.getId()));
                // 变更流程为已结束状态
                runtimeService.createChangeActivityStateBuilder()
                        .moveExecutionsToSingleActivityId(executionIds, endId).changeState();
            }
        }

        return AjaxResult.success();
    }

    /**
     * 撤回流程  目前存在错误
     *
     * @param flowTaskVo
     * @return
     */
    @Override
    public AjaxResult revokeProcess(FlowTaskVo flowTaskVo) {
        Task task = taskService.createTaskQuery()
                .processInstanceId(flowTaskVo.getInstanceId())
                .singleResult();
        if (task == null) {
            throw new ServiceException("流程未启动或已执行完成，无法撤回");
        }

        SysUser loginUser = SecurityUtils.getLoginUser().getUser();
        List<HistoricTaskInstance> htiList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .orderByTaskCreateTime()
                .asc()
                .list();
        String myTaskId = null;
        for (HistoricTaskInstance hti : htiList) {
            if (loginUser.getUserId().toString().equals(hti.getAssignee())) {
                myTaskId = hti.getId();
                break;
            }
        }
        if (null == myTaskId) {
            throw new ServiceException("该任务非当前用户提交，无法撤回");
        }
        List<HistoricTaskInstance> historicTaskInstanceList = historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .orderByHistoricTaskInstanceStartTime()
                .asc()
                .list();
        Iterator<HistoricTaskInstance> it = historicTaskInstanceList.iterator();
        //循环节点，获取当前节点的上一节点的key
        String tarKey = "";
        while (it.hasNext()) {
            HistoricTaskInstance his = it.next();
            if (!task.getTaskDefinitionKey().equals(his.getTaskDefinitionKey())) {
                tarKey = his.getTaskDefinitionKey();
            }
        }
        // 跳转节点
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(flowTaskVo.getInstanceId())
                .moveActivityIdTo(task.getTaskDefinitionKey(), tarKey)
                .changeState();

        return AjaxResult.success();
    }

    /**
     * 代办任务列表
     *
     * @param queryVo 请求参数
     * @return
     */
    @Override
    public AjaxResult todoList(FlowQueryVo queryVo) {
        Page<FlowTaskDto> page = new Page<>();
        // 只查看自己的数据
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();


        // 预加载officeIdList和officeTaskIds，以便后续判断
        List<Long> officeIdList = examTeachingResearchOfficeUserMapper.selectOfficeIdsByUserId(sysUser.getUserId());
        List<String> officeTaskIds = examTaskTeachingResearchOfficeService.selectTaskIdsByOfficeIds(officeIdList);
        // 将officeTaskIds转换为Set，提高查询效率
        java.util.Set<String> officeTaskIdSet = CollectionUtils.isEmpty(officeTaskIds) 
            ? new java.util.HashSet<>() 
            : new java.util.HashSet<>(officeTaskIds);

        TaskQuery taskQuery = taskService.createTaskQuery()
                .active()
                .includeProcessVariables()
                .taskCandidateGroupIn(sysUser.getRoles().stream().map(role -> role.getRoleId().toString()).collect(Collectors.toList()))
                .taskCandidateOrAssigned(sysUser.getUserId().toString())
                .orderByTaskCreateTime().desc();

//        TODO 传入名称查询不到数据?
        if (StringUtils.isNotBlank(queryVo.getName())) {
            taskQuery.processDefinitionNameLike(queryVo.getName());
        }
        
        // 先查询所有符合条件的任务（不分页），用于过滤
        List<Task> allTaskList = taskQuery.list();
        
        // 缓存ProcessDefinition，避免重复查询
        Map<String, ProcessDefinition> processDefinitionCache = new HashMap<>();
        
        // 过滤任务：如果任务匹配特定流程和节点，则需要检查officeIdList条件 + formMode 条件
        List<Task> filteredTaskList = new ArrayList<>();
        for (Task task : allTaskList) {
            // 从缓存获取或查询流程定义信息
            ProcessDefinition pd = processDefinitionCache.get(task.getProcessDefinitionId());
            if (pd == null) {
                pd = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionId(task.getProcessDefinitionId())
                        .singleResult();
                processDefinitionCache.put(task.getProcessDefinitionId(), pd);
            }
            
            // 判断是否需要应用officeIdList条件
            if (needApplyOfficeCondition(pd.getKey(), task.getProcessDefinitionId(), task.getTaskDefinitionKey())) {
                // officeTaskIds 为空时直接过滤
                if (CollectionUtils.isEmpty(officeTaskIdSet)) {
                    continue;
                }
                // 需要应用officeIdList条件，检查任务是否在officeTaskIds中（按流程实例ID）
                if (!officeTaskIdSet.contains(task.getProcessInstanceId())) {
                    // 不在officeTaskIds中，过滤掉
                    continue;
                }
            }

            // 表单模式过滤：
            // - formMode = custom：只要自定义表单（包括老数据：没有 formMode 但有 examFormId/examFormData）
            // - formMode = vform 或 为空：排除自定义表单（包括老数据：没有 formMode 但有 examFormId/examFormData），保留 vform + 纯老数据
            String queryFormMode = queryVo.getFormMode();
            if (StringUtils.isNotBlank(queryFormMode)) {
                Map<String, Object> vars = taskService.getVariables(task.getId());
                String taskFormMode = vars != null && vars.get("formMode") != null
                        ? vars.get("formMode").toString()
                        : null;
                boolean looksCustom = vars != null && (vars.containsKey("examFormId") || vars.containsKey("examFormData"));
                if ("custom".equals(queryFormMode)) {
                    if (!"custom".equals(taskFormMode) && !looksCustom) {
                        continue;
                    }
                } else {
                    // vform：过滤掉自定义表单（包括老数据）
                    if ("custom".equals(taskFormMode) || (taskFormMode == null && looksCustom)) {
                        continue;
                    }
                }
            }

            // 不需要应用officeIdList条件，或者任务在officeTaskIds中，且满足 formMode 条件，保留
            filteredTaskList.add(task);
        }
        
        // 手动分页
        int total = filteredTaskList.size();
        int pageNum = queryVo.getPageNum();
        int pageSize = queryVo.getPageSize();
        int fromIndex = pageSize * (pageNum - 1);
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<Task> taskList;
        if (fromIndex >= total) {
            taskList = new ArrayList<>();
        } else {
            taskList = filteredTaskList.subList(fromIndex, toIndex);
        }
        
        page.setTotal(total);
        
        // 缓存HistoricProcessInstance，避免重复查询
        Map<String, HistoricProcessInstance> historicProcessInstanceCache = new HashMap<>();
        
        List<FlowTaskDto> flowList = new ArrayList<>();
        for (Task task : taskList) {
            FlowTaskDto flowTask = new FlowTaskDto();
            // 当前流程信息
            flowTask.setTaskId(task.getId());
            flowTask.setTaskDefKey(task.getTaskDefinitionKey());
            flowTask.setCreateTime(task.getCreateTime());
            flowTask.setProcDefId(task.getProcessDefinitionId());
            flowTask.setExecutionId(task.getExecutionId());
            flowTask.setTaskName(task.getName());

            Map<String, Object> variables = taskService.getVariables(task.getId());
            if(Objects.nonNull(variables.get("deptName"))){
                flowTask.setExDeptName(variables.get("deptName").toString());
            }
            if(Objects.nonNull(variables.get("officeName"))){
                flowTask.setOfficeName(variables.get("officeName").toString());
            }
            if (Objects.nonNull(variables.get("formMode"))) {
                flowTask.setFormMode(variables.get("formMode").toString());
            }
            // 流程定义信息（从缓存获取）
            ProcessDefinition pd = processDefinitionCache.get(task.getProcessDefinitionId());
            if (pd == null) {
                // 如果缓存中没有，重新查询（理论上不应该发生）
                pd = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionId(task.getProcessDefinitionId())
                        .singleResult();
                processDefinitionCache.put(task.getProcessDefinitionId(), pd);
            }
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息（从缓存获取或查询）
            HistoricProcessInstance historicProcessInstance = historicProcessInstanceCache.get(task.getProcessInstanceId());
            if (historicProcessInstance == null) {
                historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(task.getProcessInstanceId())
                        .singleResult();
                historicProcessInstanceCache.put(task.getProcessInstanceId(), historicProcessInstance);
            }
            SysUser startUser = sysUserService.selectUserById(Long.parseLong(historicProcessInstance.getStartUserId()));
            flowTask.setStartUserId(startUser.getUserId().toString());
            flowTask.setStartUserName(startUser.getNickName());
//            flowTask.setStartDeptName(Objects.nonNull(startUser.getDept()) ? startUser.getDept().getDeptName() : "");
            flowList.add(flowTask);
        }

        page.setRecords(flowList);
        return AjaxResult.success(page);
    }

    /**
     * 判断是否需要应用officeIdList条件
     * 只有在特定流程的特定节点时才返回true
     * 
     * @param processDefinitionKey 流程定义Key
     * @param processDefinitionId 流程定义ID（包含版本号）
     * @param taskDefinitionKey 任务节点Key
     * @return true表示需要应用officeIdList条件，false表示不需要
     */
    private boolean needApplyOfficeCondition(String processDefinitionKey, String processDefinitionId, String taskDefinitionKey) {
        // 配置需要应用officeIdList条件的流程和节点组合
        Map<String, String> processNodeMap = new HashMap<>();
        processNodeMap.put("flow_lf1rqhxe", "Activity_09qv557");
        processNodeMap.put("flow_93qezqks", "Activity_07cogfy");
        processNodeMap.put("flow_8teoxmeh", "Activity_1tjjzws");
        processNodeMap.put("flow_72bddo0p", "Activity_1nxpxc0");
        
        // 遍历所有配置的流程-节点组合
        for (Map.Entry<String, String> entry : processNodeMap.entrySet()) {
            String targetProcessDefinitionKey = entry.getKey();
            String targetTaskDefinitionKey = entry.getValue();
            
            // 检查流程定义Key是否匹配（支持精确匹配或ID中包含Key）
            boolean processMatches = targetProcessDefinitionKey.equals(processDefinitionKey) 
                    || (processDefinitionId != null && processDefinitionId.startsWith(targetProcessDefinitionKey + ":"));
            
            // 检查任务节点Key是否匹配
            boolean taskMatches = targetTaskDefinitionKey.equals(taskDefinitionKey);
            
            // 如果流程和节点都匹配，返回true
            if (processMatches && taskMatches) {
                return true;
            }
        }
        
        // 没有匹配的组合，返回false
        return false;
    }


    /**
     * 已办任务列表
     *
     * @param queryVo 请求参数
     * @return
     */
    @Override
    public AjaxResult finishedList(FlowQueryVo queryVo) {
        Page<FlowTaskDto> page = new Page<>();
        Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
        HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables()
                .finished()
                .taskAssignee(userId.toString())
                .orderByHistoricTaskInstanceEndTime()
                .desc();
        List<HistoricTaskInstance> historicTaskInstanceList = taskInstanceQuery.listPage(queryVo.getPageSize() * (queryVo.getPageNum() - 1), queryVo.getPageSize());
        List<FlowTaskDto> hisTaskList = new ArrayList<>();
        for (HistoricTaskInstance histTask : historicTaskInstanceList) {
            // 表单模式过滤：
            // - formMode = custom：只要自定义表单（包括老数据：没有 formMode 但有 examFormId/examFormData）
            // - formMode = vform 或 为空：排除自定义表单（包括老数据：没有 formMode 但有 examFormId/examFormData），保留 vform + 纯老数据
            String queryFormMode = queryVo.getFormMode();
            if (StringUtils.isNotBlank(queryFormMode)) {
                Map<String, Object> vars = histTask.getProcessVariables();
                String taskFormMode = vars != null && vars.get("formMode") != null
                        ? vars.get("formMode").toString()
                        : null;
                boolean looksCustom = vars != null && (vars.containsKey("examFormId") || vars.containsKey("examFormData"));
                if ("custom".equals(queryFormMode)) {
                    if (!"custom".equals(taskFormMode) && !looksCustom) {
                        continue;
                    }
                } else {
                    // vform：过滤掉自定义表单（包括老数据）
                    if ("custom".equals(taskFormMode) || (taskFormMode == null && looksCustom)) {
                        continue;
                    }
                }
            }
            FlowTaskDto flowTask = new FlowTaskDto();
            // 当前流程信息
            flowTask.setTaskId(histTask.getId());
            // 审批人员信息
            flowTask.setCreateTime(histTask.getCreateTime());
            flowTask.setFinishTime(histTask.getEndTime());
            flowTask.setDuration(getDate(histTask.getDurationInMillis()));
            flowTask.setProcDefId(histTask.getProcessDefinitionId());
            flowTask.setTaskDefKey(histTask.getTaskDefinitionKey());
            flowTask.setTaskName(histTask.getName());

            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(histTask.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(histTask.getProcessInstanceId());
            flowTask.setHisProcInsId(histTask.getProcessInstanceId());
            if (histTask.getProcessVariables() != null && histTask.getProcessVariables().get("formMode") != null) {
                flowTask.setFormMode(histTask.getProcessVariables().get("formMode").toString());
            }

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(histTask.getProcessInstanceId())
                    .singleResult();
            SysUser startUser = sysUserService.selectUserById(Long.parseLong(historicProcessInstance.getStartUserId()));
            flowTask.setStartUserId(startUser.getNickName());
            flowTask.setStartUserName(startUser.getNickName());
//            flowTask.setStartDeptName(Objects.nonNull(startUser.getDept()) ? startUser.getDept().getDeptName() : "");
            hisTaskList.add(flowTask);
        }
        page.setTotal(taskInstanceQuery.count());
        page.setRecords(hisTaskList);
        return AjaxResult.success(page);
    }

    /**
     * 流程历史流转记录
     *
     * @param procInsId 流程实例Id
     * @return
     */
    @Override
    public AjaxResult flowRecord(String procInsId, String deployId) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(procInsId)) {
            List<HistoricActivityInstance> list = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(procInsId)
                    .orderByHistoricActivityInstanceStartTime()
                    .desc().list();
            List<FlowTaskDto> hisFlowList = new ArrayList<>();
            for (HistoricActivityInstance histIns : list) {
                // 展示开始节点
//                if ("startEvent".equals(histIns.getActivityType())) {
//                    FlowTaskDto flowTask = new FlowTaskDto();
//                    // 流程发起人信息
//                    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
//                            .processInstanceId(histIns.getProcessInstanceId())
//                            .singleResult();
//                    SysUser startUser = sysUserService.selectUserById(Long.parseLong(historicProcessInstance.getStartUserId()));
//                    flowTask.setTaskName(startUser.getNickName() + "(" + startUser.getDept().getDeptName() + ")发起申请");
//                    flowTask.setFinishTime(histIns.getEndTime());
//                    hisFlowList.add(flowTask);
//                } else if ("endEvent".equals(histIns.getActivityType())) {
//                    FlowTaskDto flowTask = new FlowTaskDto();
//                    flowTask.setTaskName(StringUtils.isNotBlank(histIns.getActivityName()) ? histIns.getActivityName() : "结束");
//                    flowTask.setFinishTime(histIns.getEndTime());
//                    hisFlowList.add(flowTask);
//                } else
                if (StringUtils.isNotBlank(histIns.getTaskId())) {
                    FlowTaskDto flowTask = new FlowTaskDto();
                    flowTask.setTaskId(histIns.getTaskId());
                    flowTask.setTaskName(histIns.getActivityName());
                    flowTask.setCreateTime(histIns.getStartTime());
                    flowTask.setFinishTime(histIns.getEndTime());
                    if (StringUtils.isNotBlank(histIns.getAssignee())) {
                        SysUser sysUser = sysUserService.selectUserById(Long.parseLong(histIns.getAssignee()));
                        flowTask.setAssigneeId(sysUser.getUserId());
                        flowTask.setAssigneeName(sysUser.getNickName());
//                        flowTask.setDeptName(Objects.nonNull(sysUser.getDept()) ? sysUser.getDept().getDeptName() : "");
                    }
                    // 展示审批人员
                    List<HistoricIdentityLink> linksForTask = historyService.getHistoricIdentityLinksForTask(histIns.getTaskId());
                    StringBuilder stringBuilder = new StringBuilder();
                    for (HistoricIdentityLink identityLink : linksForTask) {
                        // 获选人,候选组/角色(多个)
                        if ("candidate".equals(identityLink.getType())) {
                            if (StringUtils.isNotBlank(identityLink.getUserId())) {
                                SysUser sysUser = sysUserService.selectUserById(Long.parseLong(identityLink.getUserId()));
                                stringBuilder.append(sysUser.getNickName()).append(",");
                            }
                            if (StringUtils.isNotBlank(identityLink.getGroupId())) {
                                SysRole sysRole = sysRoleService.selectRoleById(Long.parseLong(identityLink.getGroupId()));
                                stringBuilder.append(sysRole.getRoleName()).append(",");
                            }
                        }
                    }
                    if (StringUtils.isNotBlank(stringBuilder)) {
                        flowTask.setCandidate(stringBuilder.substring(0, stringBuilder.length() - 1));
                    }

                    flowTask.setDuration(histIns.getDurationInMillis() == null || histIns.getDurationInMillis() == 0 ? null : getDate(histIns.getDurationInMillis()));
                    // 获取意见评论内容
                    List<Comment> commentList = taskService.getProcessInstanceComments(histIns.getProcessInstanceId());
                    commentList.forEach(comment -> {
                        if (histIns.getTaskId().equals(comment.getTaskId())) {
                            flowTask.setComment(FlowCommentDto.builder().type(comment.getType()).comment(comment.getFullMessage()).build());
                        }
                    });
                    hisFlowList.add(flowTask);
                }
            }
            map.put("flowList", hisFlowList);
        }
        // 第一次申请获取初始化表单
        if (StringUtils.isNotBlank(deployId)) {
            SysForm sysForm = sysInstanceFormService.selectSysDeployFormByDeployId(deployId);
            if (Objects.isNull(sysForm)) {
                return AjaxResult.error("请先配置流程表单");
            }
            map.put("formData", JSONObject.parseObject(sysForm.getFormContent()));
        }
        return AjaxResult.success(map);
    }

    /**
     * 根据任务ID查询挂载的表单信息
     *
     * @param taskId 任务Id
     * @return
     */
    @Override
    public AjaxResult getTaskForm(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        SysForm sysForm = sysFormService.selectSysFormById(Long.parseLong(task.getFormKey()));
        return AjaxResult.success(sysForm.getFormContent());
    }

    /**
     * 获取流程过程图
     *
     * @param processId
     * @return
     */
    @Override
    public InputStream diagram(String processId) {
        String processDefinitionId;
        // 获取当前的流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        // 如果流程已经结束，则得到结束节点
        if (Objects.isNull(processInstance)) {
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();

            processDefinitionId = pi.getProcessDefinitionId();
        } else {// 如果流程没有结束，则取当前活动节点
            // 根据流程实例ID获得当前处于活动状态的ActivityId合集
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId = pi.getProcessDefinitionId();
        }

        // 获得活动的节点
        List<HistoricActivityInstance> highLightedFlowList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processId).orderByHistoricActivityInstanceStartTime().asc().list();

        List<String> highLightedFlows = new ArrayList<>();
        List<String> highLightedNodes = new ArrayList<>();
        //高亮线
        for (HistoricActivityInstance tempActivity : highLightedFlowList) {
            if ("sequenceFlow".equals(tempActivity.getActivityType())) {
                //高亮线
                highLightedFlows.add(tempActivity.getActivityId());
            } else {
                //高亮节点
                highLightedNodes.add(tempActivity.getActivityId());
            }
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessEngineConfiguration configuration = processEngine.getProcessEngineConfiguration();
        //获取自定义图片生成器
        ProcessDiagramGenerator diagramGenerator = new CustomProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedNodes, highLightedFlows, configuration.getActivityFontName(),
                configuration.getLabelFontName(), configuration.getAnnotationFontName(), configuration.getClassLoader(), 1.0, true);
        return in;

    }

    /**
     * 获取流程执行节点
     *
     * @param procInsId 流程实例id
     * @return
     */
    @Override
    public AjaxResult getFlowViewer(String procInsId, String executionId) {
        List<FlowViewerDto> flowViewerList = new ArrayList<>();
        FlowViewerDto flowViewerDto;
        // 获取任务开始节点(临时处理方式)
        List<HistoricActivityInstance> startNodeList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(procInsId)
                .orderByHistoricActivityInstanceStartTime()
                .asc().listPage(0, 3);
        for (HistoricActivityInstance startInstance : startNodeList) {
            if (!"sequenceFlow".equals(startInstance.getActivityType())) {
                flowViewerDto = new FlowViewerDto();
                if (!"sequenceFlow".equals(startInstance.getActivityType())) {
                    flowViewerDto.setKey(startInstance.getActivityId());
                    // 根据流程节点处理时间校验改节点是否已完成
                    flowViewerDto.setCompleted(!Objects.isNull(startInstance.getEndTime()));
                    flowViewerList.add(flowViewerDto);
                }
            }
        }
        // 历史节点
        List<HistoricActivityInstance> hisActIns = historyService.createHistoricActivityInstanceQuery()
                .executionId(executionId)
                .orderByHistoricActivityInstanceStartTime()
                .asc().list();
        for (HistoricActivityInstance activityInstance : hisActIns) {
            if (!"sequenceFlow".equals(activityInstance.getActivityType())) {
                flowViewerDto = new FlowViewerDto();
                flowViewerDto.setKey(activityInstance.getActivityId());
                // 根据流程节点处理时间校验改节点是否已完成
                flowViewerDto.setCompleted(!Objects.isNull(activityInstance.getEndTime()));
                flowViewerList.add(flowViewerDto);
            }
        }
        return AjaxResult.success(flowViewerList);
    }

    /**
     * 获取流程变量
     *
     * @param taskId
     * @return
     */
    @Override
    public AjaxResult processVariables(String taskId) {
        // 流程变量
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().finished().taskId(taskId).singleResult();
        if (Objects.nonNull(historicTaskInstance)) {
            return AjaxResult.success(historicTaskInstance.getProcessVariables());
        } else {
            Map<String, Object> variables = taskService.getVariables(taskId);
            return AjaxResult.success(variables);
        }
    }

    /**
     * 更新流程变量（用于暂存流程更新）
     *
     * @param taskId 任务ID
     * @param variables 流程变量
     * @return
     */
    @Override
    public AjaxResult updateProcessVariables(String taskId, Map<String, Object> variables) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (Objects.isNull(task)) {
            return AjaxResult.error("任务不存在");
        }
        // 更新任务变量
        taskService.setVariables(taskId, variables);
        // 更新流程实例变量
        runtimeService.setVariables(task.getProcessInstanceId(), variables);
        return AjaxResult.success("更新成功");
    }

    /**
     * 已完成流程修改课题组/教研室（高危）
     */
    @Override
    public AjaxResult updateFinishedDept(Map<String, Object> body) {
        String procInsId = body.get("procInsId") == null ? null : body.get("procInsId").toString();
        if (StringUtils.isBlank(procInsId)) {
            return AjaxResult.error("流程实例ID不能为空");
        }
        HistoricProcessInstance hisIns = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(procInsId)
                .includeProcessVariables()
                .singleResult();
        if (Objects.isNull(hisIns)) {
            return AjaxResult.error("流程实例不存在");
        }
        if (Objects.isNull(hisIns.getEndTime())) {
            return AjaxResult.error("仅已完成的流程允许修改课题组");
        }

        Long deptId = null;
        Long officeId = null;
        String deptName = null;
        String officeName = null;
        try {
            if (body.get("deptId") != null) {
                deptId = Long.parseLong(body.get("deptId").toString());
            }
            if (body.get("officeId") != null) {
                officeId = Long.parseLong(body.get("officeId").toString());
            }
        } catch (NumberFormatException e) {
            return AjaxResult.error("部门/教研室ID格式错误");
        }
        deptName = body.get("deptName") == null ? null : body.get("deptName").toString();
        officeName = body.get("officeName") == null ? null : body.get("officeName").toString();

        // 高危：仅更新历史变量表，保持审计
        updateHistoricStringVariable(procInsId, "deptId", deptId);
        updateHistoricStringVariable(procInsId, "deptName", deptName);
        updateHistoricStringVariable(procInsId, "officeId", officeId);
        updateHistoricStringVariable(procInsId, "officeName", officeName);

        // 同步更新 exam_task_dept 表
        if (deptId != null || StringUtils.isNotBlank(deptName)) {
            LambdaQueryWrapper<ExamTaskDept> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamTaskDept::getTaskId, procInsId);
            ExamTaskDept update = new ExamTaskDept();
            update.setTaskId(procInsId);
            if (deptId != null) {
                update.setDeptId(deptId);
            }
            if (StringUtils.isNotBlank(deptName)) {
                update.setDeptName(deptName);
            }
            // 如果不存在记录则插入，存在则更新
            ExamTaskDept existing = examTaskDeptService.getOne(wrapper, false);
            if (existing == null) {
                examTaskDeptService.save(update);
            } else {
                update.setDeptId(update.getDeptId() == null ? existing.getDeptId() : update.getDeptId());
                update.setDeptName(StringUtils.isBlank(update.getDeptName()) ? existing.getDeptName() : update.getDeptName());
                examTaskDeptService.update(update, wrapper);
            }
        }

        return AjaxResult.success("修改成功");
    }

    /**
     * 更新历史变量（字符串存储），不存在则跳过
     */
    private void updateHistoricStringVariable(String procInsId, String varName, Object value) {
        managementService.executeCommand(commandContext -> {
            String sql = "update ACT_HI_VARINST set TEXT_ = ?, LONG_ = null, DOUBLE_ = null where PROC_INST_ID_ = ? and NAME_ = ?";
            try (PreparedStatement ps = CommandContextUtil.getDbSqlSession(commandContext)
                    .getSqlSession()
                    .getConnection()
                    .prepareStatement(sql)) {
                ps.setString(1, value == null ? null : String.valueOf(value));
                ps.setString(2, procInsId);
                ps.setString(3, varName);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new FlowableException("更新历史变量失败", e);
            }
            return null;
        });
    }

    /**
     * 审批任务获取下一节点
     *
     * @param flowTaskVo 任务
     * @return
     */
    @Override
    public AjaxResult getNextFlowNode(FlowTaskVo flowTaskVo) {
        // Step 1. 获取当前节点并找到下一步节点
        Task task = taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            return AjaxResult.error("任务不存在或已被审批!");
        }
        // Step 2. 获取当前流程所有流程变量(网关节点时需要校验表达式)
        // 如果传入了variables，优先使用传入的variables；否则从任务变量中读取
        Map<String, Object> variables;
        if (flowTaskVo.getVariables() != null && !flowTaskVo.getVariables().isEmpty()) {
            variables = flowTaskVo.getVariables();
        } else {
            variables = taskService.getVariables(task.getId());
        }
        List<UserTask> nextUserTask = FindNextNodeUtil.getNextUserTasks(repositoryService, task, variables);
        if (CollectionUtils.isEmpty(nextUserTask)) {
            return AjaxResult.success("流程已完结!", null);
        }
        return getFlowAttribute(nextUserTask);
    }

    /**
     * 发起流程获取下一节点
     *
     * @param flowTaskVo 任务
     * @return
     */
    @Override
    public AjaxResult getNextFlowNodeByStart(FlowTaskVo flowTaskVo) {
        // Step 1. 查找流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(flowTaskVo.getDeploymentId()).singleResult();
        if (Objects.isNull(processDefinition)) {
            return AjaxResult.error("流程信息不存在!");
        }
        // Step 2. 获取下一任务节点(网关节点时需要校验表达式)
        List<UserTask> nextUserTask = FindNextNodeUtil.getNextUserTasksByStart(repositoryService, processDefinition, flowTaskVo.getVariables());
        if (CollectionUtils.isEmpty(nextUserTask)) {
            return AjaxResult.error("暂未查找到下一任务,请检查流程设计是否正确!");
        }
        return getFlowAttribute(nextUserTask);
    }


    /**
     * 获取任务节点属性,包含自定义属性等
     *
     * @param nextUserTask
     */
    private AjaxResult getFlowAttribute(List<UserTask> nextUserTask) {
        FlowNextDto flowNextDto = new FlowNextDto();
        for (UserTask userTask : nextUserTask) {
            MultiInstanceLoopCharacteristics multiInstance = userTask.getLoopCharacteristics();
            // 会签节点
            if (Objects.nonNull(multiInstance)) {
                flowNextDto.setVars(multiInstance.getInputDataItem());
                flowNextDto.setType(ProcessConstants.PROCESS_MULTI_INSTANCE);
                flowNextDto.setDataType(ProcessConstants.DYNAMIC);
            } else {
                // 读取自定义节点属性 判断是否是否需要动态指定任务接收人员、组
                String dataType = userTask.getAttributeValue(ProcessConstants.NAMASPASE, ProcessConstants.PROCESS_CUSTOM_DATA_TYPE);
                String userType = userTask.getAttributeValue(ProcessConstants.NAMASPASE, ProcessConstants.PROCESS_CUSTOM_USER_TYPE);
                flowNextDto.setVars(ProcessConstants.PROCESS_APPROVAL);
                flowNextDto.setType(userType);
                flowNextDto.setDataType(dataType);
            }
        }
        return AjaxResult.success(flowNextDto);
    }

    /**
     * 流程初始化表单
     *
     * @param deployId
     * @return
     */
    @Override
    public AjaxResult flowFormData(String deployId) {
        // 第一次申请获取初始化表单
        if (StringUtils.isNotBlank(deployId)) {
            SysForm sysForm = sysInstanceFormService.selectSysDeployFormByDeployId(deployId);
            if (Objects.isNull(sysForm)) {
                return AjaxResult.error("请先配置流程表单!");
            }
            return AjaxResult.success(JSONObject.parseObject(sysForm.getFormContent()));
        } else {
            return AjaxResult.error("参数错误!");
        }
    }

    /**
     * 流程节点信息
     *
     * @param procInsId
     * @return
     */
    @Override
    public AjaxResult flowXmlAndNode(String procInsId, String deployId) {
        try {
            List<FlowViewerDto> flowViewerList = new ArrayList<>();
            // 获取已经完成的节点
            List<HistoricActivityInstance> listFinished = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(procInsId)
                    .finished()
                    .list();

            // 保存已经完成的流程节点编号
            listFinished.forEach(s -> {
                FlowViewerDto flowViewerDto = new FlowViewerDto();
                flowViewerDto.setKey(s.getActivityId());
                flowViewerDto.setCompleted(true);
                // 退回节点不进行展示
                if (StringUtils.isBlank(s.getDeleteReason())) {
                    flowViewerList.add(flowViewerDto);
                }
            });

            // 获取代办节点
            List<HistoricActivityInstance> listUnFinished = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(procInsId)
                    .unfinished()
                    .list();

            // 保存需要代办的节点编号
            listUnFinished.forEach(s -> {
                // 删除已退回节点
                flowViewerList.removeIf(task -> task.getKey().equals(s.getActivityId()));
                FlowViewerDto flowViewerDto = new FlowViewerDto();
                flowViewerDto.setKey(s.getActivityId());
                flowViewerDto.setCompleted(false);
                flowViewerList.add(flowViewerDto);
            });
            Map<String, Object> result = new HashMap<>();
            // xmlData 数据
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
            InputStream inputStream = repositoryService.getResourceAsStream(definition.getDeploymentId(), definition.getResourceName());
            String xmlData = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            result.put("nodeData", flowViewerList);
            result.put("xmlData", xmlData);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("高亮历史任务失败");
        }
    }


    /**
     * 流程节点表单
     *
     * @param taskId 流程任务编号
     * @return
     */
    @Override
    public AjaxResult flowTaskForm(String taskId) throws Exception {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // 流程变量
        Map<String, Object> parameters = new HashMap<>();
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().finished().taskId(taskId).singleResult();
        if (Objects.nonNull(historicTaskInstance)) {
            parameters = historicTaskInstance.getProcessVariables();
        } else {
            parameters = taskService.getVariables(taskId);
        }
        // 追加流程/节点标识，便于前端获取
        parameters.put("processDefinitionId", task.getProcessDefinitionId());
        parameters.put("taskDefinitionKey", task.getTaskDefinitionKey());
        
        // 检查节点上是否绑定了自定义表单（examFormId）
        String nodeExamFormId = null;
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            if (processDefinition != null) {
                Process process = repositoryService.getBpmnModel(processDefinition.getId()).getProcesses().get(0);
                Collection<FlowElement> allElements = FlowableUtils.getAllElements(process.getFlowElements(), null);
                for (FlowElement flowElement : allElements) {
                    if (flowElement.getId().equals(task.getTaskDefinitionKey()) && flowElement instanceof UserTask) {
                        UserTask userTask = (UserTask) flowElement;
                        // 从 flowable:Properties 中读取 examFormId
                        if (userTask.getExtensionElements() != null) {
                            Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
                            for (Map.Entry<String, List<ExtensionElement>> entry : extensionElements.entrySet()) {
                                for (ExtensionElement extElement : entry.getValue()) {
                                    // 查找 flowable:Properties 类型的扩展元素
                                    if ("Properties".equals(extElement.getName()) || "flowable:Properties".equals(extElement.getNamespace())) {
                                        // 获取 properties 下的 property 子元素
                                        List<ExtensionElement> propertyElements = extElement.getChildElements().get("property");
                                        if (propertyElements != null) {
                                            for (ExtensionElement propertyElement : propertyElements) {
                                                // 获取 property 的 name 和 value 属性
                                                List<ExtensionAttribute> nameAttrs = propertyElement.getAttributes().get("name");
                                                List<ExtensionAttribute> valueAttrs = propertyElement.getAttributes().get("value");
                                                if (nameAttrs != null && !nameAttrs.isEmpty() && valueAttrs != null && !valueAttrs.isEmpty()) {
                                                    String propertyName = nameAttrs.get(0).getValue();
                                                    String propertyValue = valueAttrs.get(0).getValue();
                                                    if ("examFormId".equals(propertyName) && StringUtils.isNotBlank(propertyValue)) {
                                                        nodeExamFormId = propertyValue;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("读取节点上的 examFormId 属性失败", e);
        }
        
        // 如果节点上绑定了自定义表单，返回自定义表单数据
        if (StringUtils.isNotBlank(nodeExamFormId)) {
            try {
                Long examFormId = Long.parseLong(nodeExamFormId);
                Map<String, Object> formModules = formModuleService.getFormModules(examFormId, true);
                parameters.put("examFormId", examFormId);
                parameters.put("examFormTemplate", formModules);
                parameters.put("formMode", "custom");
                return AjaxResult.success(parameters);
            } catch (Exception e) {
                log.error("获取节点自定义表单失败: examFormId={}", nodeExamFormId, e);
            }
        }
        
        // 处理 vform 表单（原有逻辑）
        JSONObject oldVariables = JSONObject.parseObject(JSON.toJSONString(parameters.get("formJson")));
        List<JSONObject> oldFields = JSON.parseObject(JSON.toJSONString(oldVariables.get("widgetList")), new TypeReference<List<JSONObject>>() {
        });
        // 设置已填写的表单为禁用状态
        for (JSONObject oldField : oldFields) {
            JSONObject options = oldField.getJSONObject("options");
            options.put("disabled", true);
        }
        // TODO 暂时只处理用户任务上的表单
        if (StringUtils.isNotBlank(task.getFormKey())) {
            SysForm sysForm = sysFormService.selectSysFormById(Long.parseLong(task.getFormKey()));
            JSONObject data = JSONObject.parseObject(sysForm.getFormContent());
            List<JSONObject> newFields = JSON.parseObject(JSON.toJSONString(data.get("widgetList")), new TypeReference<List<JSONObject>>() {
            });
            // 表单回显时 加入子表单信息到流程变量中
            for (JSONObject newField : newFields) {
                String key = newField.getString("id");
                // 处理图片上传组件回显问题
                if ("picture-upload".equals(newField.getString("type"))) {
                    parameters.put(key, new ArrayList<>());
                } else {
                    parameters.put(key, null);
                }
            }
            oldFields.addAll(newFields);
        }
        oldVariables.put("widgetList", oldFields);
        parameters.put("formJson", oldVariables);
        return AjaxResult.success(parameters);
    }

    @Override
    @Transactional
    public AjaxResult generateWord(String procInsId) {
        List<String> urlList = new ArrayList<>();
        List<String> deleteFileList = new ArrayList<>();

        Long deptId = examTaskDeptService.selectDeptIdByTaskId(procInsId);
        if (Objects.isNull(deptId)) {
            return AjaxResult.error("流程实例ID未绑定课题组");
        }

        try {
            //删除原本的文件列表
            LambdaQueryWrapper<SysStoreInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysStoreInfo::getSceneName,StoreSceneNameConstants.FLOWABLE_WORD_APPROVAL_FORM)
                    .eq(SysStoreInfo::getObjId,procInsId);
            sysStoreInfoRepoService.remove(wrapper);

            // 获取历史流程实例信息
            HistoricProcessInstance historicTaskInstance = historyService.createHistoricProcessInstanceQuery()
                    .includeProcessVariables().finished().processInstanceId(procInsId).singleResult();
            String deploymentId = historicTaskInstance.getDeploymentId();
            if (StringUtils.isBlank(deploymentId)) {
                return AjaxResult.error("流程实例ID为空");
            }

            // 获取表单信息
            SysForm sysForm = sysDeployFormService.selectSysDeployFormByDeployId(deploymentId);
            if (Objects.isNull(sysForm)) {
                return AjaxResult.error("表单为空");
            }

            ExamWordTemplateInfo queryInfo = new ExamWordTemplateInfo();
            queryInfo.setFormId(sysForm.getFormId());
            queryInfo.setDeptId(deptId);

            // 根据表单查询word模板列表
            List<ExamWordTemplateInfo> examWordTemplateInfos = examWordTemplateInfoRepoService.selectExamWordTemplateInfoList(queryInfo);
            if (examWordTemplateInfos.isEmpty()) {
                throw new ServiceException("请联系管理员配置表单对应的word模板"); // 提前返回，避免不必要的处理
            }

            for (ExamWordTemplateInfo examWordTemplateInfo : examWordTemplateInfos) {
                Map<String, Object> data = new HashMap<>();

                // 查询word模板的地址，并下载文件到本地
                SysStoreInfo storeInfo = getTemplateStoreInfo(examWordTemplateInfo.getId());
                if (storeInfo == null) {
                    continue; // 跳过无效模板
                }
                String filePath = OssUtil.downLoadFile(storeInfo.getOssKey());
                if (filePath == null || !new File(filePath).exists()) {
                    log.warn("下载文件失败: {}", storeInfo.getOssKey());
                    throw new CustomException("下载文件失败");
                }
                deleteFileList.add(filePath);

                // 主表单
                if (examWordTemplateInfo.getWordTemplateType().equals(WordTemplateType.MAIN_FORM.getType())) {

                    Map<String, Object> newData = historicTaskInstance.getProcessVariables()
                            .entrySet()
                            .stream()
                            .filter(entry -> entry.getValue() != null)
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> {
                                        Object value = entry.getValue();
                                        String key = entry.getKey();
                                        String text;
                                        if (key.startsWith("date")) {
                                            try {
                                                // 解析日期字符串为 LocalDate
                                                LocalDate date = LocalDate.parse(value.toString(), DateTimeFormatter.ISO_LOCAL_DATE);
                                                // 格式化为目标形式
                                                text = date.format(DateTimeFormatter.ofPattern("yyyy年M月d日"));
                                            } catch (DateTimeParseException e) {
                                                // 解析失败，使用原始字符串
                                                text = value.toString();
                                            }
                                        } else {
                                            text = value.toString();
                                        }

                                        TextRenderData valueMap = new TextRenderData();
                                        valueMap.setText(text);

                                        Style style = new Style();
                                        style.setBold(false);
                                        valueMap.setStyle(style);

                                        return valueMap;
                                    },
                                    (existing, replacement) -> replacement
                            ));
                    data.putAll(newData);
                }
                /**
                 * todo 代码需要优化 枚举类 、常量 、字典
                 */

                List<ExamSignLocationInfo> examSignLocationInfos = examSignLocationInfoRepoService.list(
                        Wrappers.<ExamSignLocationInfo>lambdaQuery().eq(ExamSignLocationInfo::getFormId, examWordTemplateInfo.getId()));
                //查询签名人及其签名位置
                for(ExamSignLocationInfo signInfo : examSignLocationInfos){
                    String runtimeProcessDefId = historicTaskInstance.getProcessDefinitionId();
                    if (!runtimeProcessDefId.equals(signInfo.getFlowId())) {
                        throw new RuntimeException("签名文件配置错误: " + signInfo.getId());
                    }
                    // 3. 查询指定节点的历史任务记录

                    List<HistoricActivityInstance> historicTasks = historyService
                            .createHistoricActivityInstanceQuery()
                            .processInstanceId(procInsId)
                            .activityId(signInfo.getNodeId())
                            .orderByHistoricActivityInstanceStartTime()
                            .desc().list();
                    if (historicTasks.isEmpty()) {
                        throw new RuntimeException("未找到节点ID为 " + signInfo.getId() + " 的历史任务记录");
                    }
                    //查询该节点办理人id
                    String userIdStr = historicTasks.get(0).getAssignee();
                    Long userId = Long.parseLong(userIdStr);
                    //根据签名人id查询所有签名文件
                    LambdaQueryWrapper<SysStoreInfo> queryWrapper = Wrappers.<SysStoreInfo>lambdaQuery()
                            .eq(SysStoreInfo::getObjId, userId)
                            .eq(SysStoreInfo::getSceneName, ExamElectronicType.getByType(signInfo.getExamElectronicType()).getReflect());
                    SysStoreInfo signerStoreInfo = sysStoreInfoRepoService.getOne(queryWrapper);
                    if (signerStoreInfo == null) {
                        throw new CustomException(signInfo.getNodeName() + "未上传电子签名");
                    }
                    String signPath = OssUtil.downLoadFile(signerStoreInfo.getOssKey());
                    if (signPath == null || !new File(signPath).exists()) {
                        log.warn("下载签名文件失败: {}", signerStoreInfo.getOssKey());
                        throw new CustomException(signInfo.getNodeName() + "电子签名下载失败，请重新上传【或者联系管理员】");
                    }
                    deleteFileList.add(signPath);
                    data.put(signInfo.getSignatureLocation(), new PictureRenderData(80, 40, signPath));
                }


                // 渲染文件
                XWPFTemplate compile = XWPFTemplate.compile(filePath);
                compile.render(data);
                String tempDirPath = System.getProperty("java.io.tmpdir");
                String fileName = edu.xzit.core.core.common.utils.file.FileUtils.getNameNotSuffix(filePath);
                Path tempFilePath = Files.createTempFile(Paths.get(tempDirPath), fileName, ".docx");
                compile.writeToFile(tempFilePath.toString());

                SysStoreInfo sysStoreInfo = uploadFile(tempFilePath.toString(), StoreSceneNameConstants.FLOWABLE_WORD_APPROVAL_FORM, Long.parseLong(procInsId));
                if (sysStoreInfo == null) {
                    throw new CustomException("生成word失败");
                }
                urlList.add(sysStoreInfo.getUrl());
            }

            // 删除临时文件
            cleanUp(deleteFileList);

            return AjaxResult.success(urlList);
        } catch (Exception e) {
            log.error("生成Word文档失败", e);
            return AjaxResult.error("生成Word文档失败: " + e.getMessage());
        }
    }

    @Override
    public void generateWordNew(HttpServletResponse response, String procInsId) {
        String templateLocalPath = null;
        try {
            // 获取历史流程实例信息
            HistoricProcessInstance historicTaskInstance = historyService.createHistoricProcessInstanceQuery()
                    .includeProcessVariables().finished().processInstanceId(procInsId).singleResult();
            if (historicTaskInstance == null) {
                throw new ServiceException("未找到对应的流程实例");
            }

            String deploymentId = historicTaskInstance.getDeploymentId();
            if (StringUtils.isBlank(deploymentId)) {
                throw new ServiceException("未找到对应的流程实例");
            }

            // 根据流程定义 key 做数据预处理与模板构建
            Map<String, Object> data = new HashMap<>();
            WordExportContext context = buildWordExportContext(historicTaskInstance, data);
            templateLocalPath = context.getTemplateLocalPath();
            WordUtil wordUtil = context.getWordUtil();

            // 设置导出文件名（不带后缀）
            wordUtil.setFileName("reviewAndApprovalProcess_" + procInsId);
            // 直接写入响应流，由浏览器下载
            wordUtil.exportWord(response);

        } catch (Exception e) {
            log.error("使用新模板生成Word文档失败", e);
        } finally {
            if (StringUtils.isNotBlank(templateLocalPath)) {
                try {
                    Files.deleteIfExists(Paths.get(templateLocalPath));
                } catch (IOException ioException) {
                    log.warn("删除临时模板文件失败: {}", templateLocalPath, ioException);
                }
            }
        }
    }

    /**
     * 将 examFormData（List<Map<String, Object>>）转换为以 fieldId 为 key 的 Map<String, Object>
     *
     * @param value 流程变量 examFormData 的原始值
     * @return 以 fieldId 为键的 Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildExamFormData(Object value) {
        Map<String, Object> result = new HashMap<>();
        if (!(value instanceof List)) {
            return result;
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) value;
        for (Map<String, Object> item : list) {
            if (item == null) {
                continue;
            }
            Object fieldId = item.get("fieldId");
            if (fieldId != null) {
                // 这里保留整条记录，模板中可通过 examFormData.xxx.data[...] 访问
                result.put(fieldId.toString(), item);
                // 如果只想要 data，可以改成：
                // result.put(fieldId.toString(), item.get("data"));
            }
        }
        return result;
    }

        /**
     * 从 assessment_content_table 中获取列数，并计算目标列数（cols - 3）
     * 
     * @param examFormDataMap examFormData 映射
     * @return 目标列数（cols - 3），如果获取失败则返回默认值 3
     */
        @SuppressWarnings("unchecked")
        private int getTargetColumnCountFromAssessmentContentTable(Map<String, Object> examFormDataMap) {
            if (examFormDataMap == null) {
                return 3; // 默认值
            }
            
            Object examATableObj = examFormDataMap.get("assessment_content_table");
            if (!(examATableObj instanceof Map)) {
                return 3; // 默认值
            }
            
            Map<String, Object> examATable = (Map<String, Object>) examATableObj;
            Object colsObj = examATable.get("cols");
            int cols = colsObj instanceof Number ? ((Number) colsObj).intValue() : 6; // 默认6列
            
            // 固定列数：numbera(1) + objectivea(1) + scorea(1) = 3
            int fixedCols = 3;
            // 目标列数：cols - 固定列数
            return cols - fixedCols;
        }

    /**
     * 从 exam_a_table 中获取列数，并计算目标列数（cols - 3）
     * 
     * @param examFormDataMap examFormData 映射
     * @return 目标列数（cols - 3），如果获取失败则返回默认值 3
     */
    @SuppressWarnings("unchecked")
    private int getTargetColumnCountFromExamATable(Map<String, Object> examFormDataMap) {
        if (examFormDataMap == null) {
            return 3; // 默认值
        }
        
        Object examATableObj = examFormDataMap.get("exam_a_table");
        if (!(examATableObj instanceof Map)) {
            return 3; // 默认值
        }
        
        Map<String, Object> examATable = (Map<String, Object>) examATableObj;
        Object colsObj = examATable.get("cols");
        int cols = colsObj instanceof Number ? ((Number) colsObj).intValue() : 6; // 默认6列
        
        // 固定列数：numbera(1) + objectivea(1) + scorea(1) = 3
        int fixedCols = 3;
        // 目标列数：cols - 固定列数
        return cols - fixedCols;
    }

    /**
     * 从 examFormDataMap 中提取 fieldId=exam_a_table 的表格数据，
     * 并按模板占位符 [numbera]、[objectivea]、[targeta1]、[targeta2]、[targeta3]、[scorea]
     * 构造循环行所需的 Map 列表。
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildExamATableRows(Map<String, Object> examFormDataMap) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (examFormDataMap == null) {
            return rows;
        }

        Object examATableObj = examFormDataMap.get("exam_a_table");
        if (!(examATableObj instanceof Map)) {
            return rows;
        }
        Map<String, Object> examATable = (Map<String, Object>) examATableObj;

        List<List<Object>> data = (List<List<Object>>) examATable.get("data");
        // 获取列数
        Object colsObj = examATable.get("cols");
        int cols = colsObj instanceof Number ? ((Number) colsObj).intValue() : 6; // 默认6列
        if (data == null) {
            return rows;
        }

        // 固定列数：numbera(1) + objectivea(1) + scorea(1) = 3
        int fixedCols = 3;
        // 动态目标列数：cols - 固定列数
        int targetCols = cols - fixedCols;

        // 跳过最后一行，最后一行单独处理
        for (int i = 0; i < data.size() - 1; i++) {
            List<Object> row = data.get(i);
            // 跳过全空行
            if (row == null || row.stream().allMatch(Objects::isNull)) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();

            // 第一列：题目编号（data[row][0]）
            item.put("numbera", getCellString(row, 0));

            // 第二列：考核目的（data[row][1]）
            item.put("objectivea", getCellString(row, 1));

            // 中间列：目标列（targeta1, targeta2, ...），从 data[row][2] 到 data[row][cols-2]
            for (int j = 0; j < targetCols; j++) {
                String key = "targeta" + (j + 1);
                String value = getCellString(row, j + 2); // data[row][2] 开始
                item.put(key, value);
            }

            // 最后一列：分值（data[row][cols-1]）
            item.put("scorea", getCellString(row, cols - 1));

            rows.add(item);
        }

        return rows;
    }

    /**
     * 从 examFormDataMap 中提取 fieldId=exam_b_table 的表格数据，
     * 并按模板占位符 [numberb]、[objectiveb]、[targetb1]、[targetb2]、...、[scoreb]
     * 构造循环行所需的 Map 列表。
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildExamBTableRows(Map<String, Object> examFormDataMap) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (examFormDataMap == null) {
            return rows;
        }

        Object examBTableObj = examFormDataMap.get("exam_b_table");
        if (!(examBTableObj instanceof Map)) {
            return rows;
        }
        Map<String, Object> examBTable = (Map<String, Object>) examBTableObj;

        List<List<Object>> data = (List<List<Object>>) examBTable.get("data");
        // 获取列数
        Object colsObj = examBTable.get("cols");
        int cols = colsObj instanceof Number ? ((Number) colsObj).intValue() : 6; // 默认6列
        if (data == null) {
            return rows;
        }

        // 固定列数：numberb(1) + objectiveb(1) + scoreb(1) = 3
        int fixedCols = 3;
        // 动态目标列数：cols - 固定列数
        int targetCols = cols - fixedCols;

        // 跳过最后一行，最后一行单独处理
        for (int i = 0; i < data.size() - 1; i++) {
            List<Object> row = data.get(i);
            // 跳过全空行
            if (row == null || row.stream().allMatch(Objects::isNull)) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();

            // 第一列：题目编号（data[row][0]）
            item.put("numberb", getCellString(row, 0));

            // 第二列：考核目的（data[row][1]）
            item.put("objectiveb", getCellString(row, 1));

            // 中间列：目标列（targetb1, targetb2, ...），从 data[row][2] 到 data[row][cols-2]
            for (int j = 0; j < targetCols; j++) {
                String key = "targetb" + (j + 1);
                String value = getCellString(row, j + 2); // data[row][2] 开始
                item.put(key, value);
            }

            // 最后一列：分值（data[row][cols-1]）
            item.put("scoreb", getCellString(row, cols - 1));

            rows.add(item);
        }

        return rows;
    }

        /**
     * 从 examFormDataMap 中提取 fieldId=exam_a_table 的表格数据，
     * 并按模板占位符 [numbera]、[objectivea]、[targeta1]、[targeta2]、[targeta3]、[scorea]
     * 构造循环行所需的 Map 列表。
     */
        @SuppressWarnings("unchecked")
        private List<Map<String, Object>> buildAssessmentContentTableRows(Map<String, Object> examFormDataMap) {
            List<Map<String, Object>> rows = new ArrayList<>();
            if (examFormDataMap == null) {
                return rows;
            }
    
            Object examATableObj = examFormDataMap.get("assessment_content_table");
            if (!(examATableObj instanceof Map)) {
                return rows;
            }
            Map<String, Object> examATable = (Map<String, Object>) examATableObj;
    
            List<List<Object>> data = (List<List<Object>>) examATable.get("data");
            // 获取列数
            Object colsObj = examATable.get("cols");
            int cols = colsObj instanceof Number ? ((Number) colsObj).intValue() : 6; // 默认6列
            if (data == null) {
                return rows;
            }
    
            // 固定列数：numbera(1) + objectivea(1) + scorea(1) = 3
            int fixedCols = 3;
            // 动态目标列数：cols - 固定列数
            int targetCols = cols - fixedCols;
    
            // 跳过最后一行，最后一行单独处理
            for (int i = 0; i < data.size() - 1; i++) {
                List<Object> row = data.get(i);
                // 跳过全空行
                if (row == null || row.stream().allMatch(Objects::isNull)) {
                    continue;
                }
    
                Map<String, Object> item = new HashMap<>();
    
                // 第一列：题目编号（data[row][0]）
                item.put("numbera", getCellString(row, 0));

                // 第二列：考核项目/题目（data[row][0]）
                item.put("question", getCellString(row, 1));
    
                // 第三列：考核目的（data[row][1]）
                item.put("objectivea", getCellString(row, 2));
    
                // 中间列：目标列（targeta1, targeta2, ...），从 data[row][2] 到 data[row][cols-2]
                for (int j = 0; j < targetCols; j++) {
                    String key = "target" + (j + 1);
                    String value = getCellString(row, j + 2); // data[row][2] 开始
                    item.put(key, value);
                }
                rows.add(item);
            }
    
            return rows;
        }   

    /**
     * 从 exam_a_table 的最后一行提取数据，生成 tscorea1, tscorea2, ..., ttscorea 标签
     * 从第三列开始（跳过numbera和objectivea），最后一列固定为ttscorea
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildExamATableLastRow(Map<String, Object> examFormDataMap) {
        Map<String, Object> result = new HashMap<>();
        if (examFormDataMap == null) {
            return result;
        }

        Object examATableObj = examFormDataMap.get("exam_a_table");
        if (!(examATableObj instanceof Map)) {
            return result;
        }
        Map<String, Object> examATable = (Map<String, Object>) examATableObj;

        List<List<Object>> data = (List<List<Object>>) examATable.get("data");
        if (data == null || data.isEmpty()) {
            return result;
        }

        // 获取最后一行
        List<Object> lastRow = data.get(data.size() - 1);
        if (lastRow == null) {
            return result;
        }

        // 获取列数
        Object colsObj = examATable.get("cols");
        int cols = colsObj instanceof Number ? ((Number) colsObj).intValue() : 6;

        // 固定列数：numbera(1) + objectivea(1) + scorea(1) = 3
        int fixedCols = 3;
        // 动态目标列数：cols - 固定列数
        int targetCols = cols - fixedCols;

        // 从第三列开始（data数组的index 2，对应targeta1的位置），生成 tscorea1, tscorea2, ...
        // targeta1对应data[lastRow][2]，targeta2对应data[lastRow][3]，以此类推
        for (int j = 0; j < targetCols; j++) {
            String key = "tscorea" + (j + 1);
            String value = getCellString(lastRow, j + 2); // j=0时index=2（第三列），对应targeta1的位置
            result.put(key, value);
        }

        // 最后一列固定为 ttscorea（对应scorea的位置）
        result.put("ttscorea", getCellString(lastRow, cols - 1));

        return result;
    }

    /**
     * 从 exam_b_table 的最后一行提取数据，生成 tscoreb1, tscoreb2, ..., ttscoreb 标签
     * 从第三列开始（跳过numberb和objectiveb），最后一列固定为ttscoreb
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildExamBTableLastRow(Map<String, Object> examFormDataMap) {
        Map<String, Object> result = new HashMap<>();
        if (examFormDataMap == null) {
            return result;
        }

        Object examBTableObj = examFormDataMap.get("exam_b_table");
        if (!(examBTableObj instanceof Map)) {
            return result;
        }
        Map<String, Object> examBTable = (Map<String, Object>) examBTableObj;

        List<List<Object>> data = (List<List<Object>>) examBTable.get("data");
        if (data == null || data.isEmpty()) {
            return result;
        }

        // 获取最后一行
        List<Object> lastRow = data.get(data.size() - 1);
        if (lastRow == null) {
            return result;
        }

        // 获取列数
        Object colsObj = examBTable.get("cols");
        int cols = colsObj instanceof Number ? ((Number) colsObj).intValue() : 6;

        // 固定列数：numberb(1) + objectiveb(1) + scoreb(1) = 3
        int fixedCols = 3;
        // 动态目标列数：cols - 固定列数
        int targetCols = cols - fixedCols;

        // 从第三列开始（data数组的index 2，对应targetb1的位置），生成 tscoreb1, tscoreb2, ...
        // targetb1对应data[lastRow][2]，targetb2对应data[lastRow][3]，以此类推
        for (int j = 0; j < targetCols; j++) {
            String key = "tscoreb" + (j + 1);
            String value = getCellString(lastRow, j + 2); // j=0时index=2（第三列），对应targetb1的位置
            result.put(key, value);
        }

        // 最后一列固定为 ttscoreb（对应scoreb的位置）
        result.put("ttscoreb", getCellString(lastRow, cols - 1));

        return result;
    }

        /**
     * 从 assessment_content_table 的最后一行提取数据，生成 tscorea1, tscorea2, ..., ttscorea 标签
     * 从第三列开始（跳过numbera和objectivea），最后一列固定为ttscorea
     */
        @SuppressWarnings("unchecked")
        private Map<String, Object> buildAssessmentContentTableLastRow(Map<String, Object> examFormDataMap) {
            Map<String, Object> result = new HashMap<>();
            if (examFormDataMap == null) {
                return result;
            }
    
            Object examATableObj = examFormDataMap.get("assessment_content_table");
            if (!(examATableObj instanceof Map)) {
                return result;
            }
            Map<String, Object> examATable = (Map<String, Object>) examATableObj;
    
            List<List<Object>> data = (List<List<Object>>) examATable.get("data");
            if (data == null || data.isEmpty()) {
                return result;
            }
    
            // 获取最后一行
            List<Object> lastRow = data.get(data.size() - 1);
            if (lastRow == null) {
                return result;
            }
    
            // 获取列数
            Object colsObj = examATable.get("cols");
            int cols = colsObj instanceof Number ? ((Number) colsObj).intValue() : 6;
    
            // 固定列数：numbera(1) + objectivea(1) + scorea(1) = 3
            int fixedCols = 3;
            // 动态目标列数：cols - 固定列数
            int targetCols = cols - fixedCols;
    
            // 从第三列开始（data数组的index 2，对应targeta1的位置），生成 tscorea1, tscorea2, ...
            // targeta1对应data[lastRow][2]，targeta2对应data[lastRow][3]，以此类推
            for (int j = 0; j < targetCols; j++) {
                String key = "tscore" + (j + 1);
                String value = getCellString(lastRow, j + 3); // j=0时index=2（第三列），对应targeta1的位置
                result.put(key, value);
            }
    
    
            return result;
        }
    

    /**
     * 根据流程定义 key 构建导出上下文（数据预处理 + 模板加载 + WordUtil 配置）
     */
    private WordExportContext buildWordExportContext(HistoricProcessInstance historicTaskInstance, Map<String, Object> data) {
        String flowKey = historicTaskInstance.getProcessDefinitionKey();
        switch (flowKey) {
            case "flow_lf1rqhxe":
                return buildWordExportContextForLf1rqhxe(historicTaskInstance, data);
            case "flow_93qezqks":
                return buildWordExportContextFor93qezqks(historicTaskInstance, data);
            case "flow_8teoxmeh":
                return buildWordExportContextFor8teoxmeh(historicTaskInstance, data);
            case "flow_72bddo0p":
                return buildWordExportContextFor72bddo0p(historicTaskInstance, data);
            case "flow_d4gkt8qp":
                return buildWordExportContextForD4gkt8qp(historicTaskInstance, data);
            // 后续新增流程时，在此追加 case 分支
            default:
                throw new ServiceException("当前流程暂不支持导出: " + flowKey);
        }
    }

    /**
     * flow_lf1rqhxe 的专用数据处理与模板构建
     */
    private WordExportContext buildWordExportContextForLf1rqhxe(HistoricProcessInstance historicTaskInstance, Map<String, Object> data) {
                            // 仅构造 examFormData 的映射（fieldId -> 对应对象），并为 A/B 卷表格准备循环数据
                            Object examFormDataObj = historicTaskInstance.getProcessVariables().get("examFormData");
                            if (ObjectUtil.isNull(examFormDataObj)) {
                                throw new ServiceException("未找到对应的表单数据");
                            }
                            Map<String, Object> examFormDataMap = buildExamFormData(examFormDataObj);
                    
                            // 从 exam_a_table 获取列数，计算目标列数（cols - 3）
                            int targetColumnCount = getTargetColumnCountFromExamATable(examFormDataMap);
                    
                            List<Map<String, Object>> examaRows = buildExamATableRows(examFormDataMap);
                            List<Map<String, Object>> exambRows = buildExamBTableRows(examFormDataMap);
                            // 课程目标与毕业要求指标点映射表行数据
                            List<Map<String, Object>> targetCourseRows = buildTargetCourseRows(examFormDataMap);
                            // 审核内容表（approval_opinion_table）行数据
                            List<Map<String, Object>> approvalOpinionRows = buildApprovalOpinionRows(
                                    historicTaskInstance.getProcessDefinitionKey(), examFormDataMap);
                    
                            // 提取A表和B表最后一行的数据，生成tscorea1/tscorea2等和tscoreb1/tscoreb2等标签
                            Map<String, Object> examALastRowData = buildExamATableLastRow(examFormDataMap);
                            Map<String, Object> examBLastRowData = buildExamBTableLastRow(examFormDataMap);
                            data.putAll(examALastRowData);
                            data.putAll(examBLastRowData);
                    
                            // 生成标题（根据学年学期）
                            String title = buildTitle(historicTaskInstance);
                            data.put("title", title);
                    
                            // 筛选出 DATE_PICKER 和 TEXT_INPUT 类型的数据，提取 data 字段并插入到 Word 中
                            extractFormModuleData(examFormDataMap, data);
                    
                            // 从 nodeFormDataMap 中提取节点表单数据（DATE_PICKER 和 TEXT_INPUT 类型）
                            extractNodeFormData(historicTaskInstance.getProcessVariables(), data);
                    
                            // 处理审批方式单选（approval_method_radio），生成带勾选的文本，插入到 {{approval_method}} 标签
                            String approvalMethodText = buildApprovalMethodText(examFormDataMap);
                            if (StringUtils.isNotBlank(approvalMethodText)) {
                                data.put("approval_method", approvalMethodText);
                            }
                    
                            // 根据配置自动插入流程节点签名图片
                            fillSignaturePictures(historicTaskInstance, data);
                    
                            // 根据流程key以及行列数匹配模板（第二个参数使用 a表的列数-3）
                            List<ExamWordTemplateNew> examWordTemplateNews = examWordTemplateNewRepoService.getByFlowableKeyAndColumn(
                                    historicTaskInstance.getProcessDefinitionKey(), targetColumnCount, 0);
                            if (examWordTemplateNews.isEmpty()) {
                throw new ServiceException("未找到对应的模板");
            }
            Long templateId = examWordTemplateNews.get(0).getId();
                            List<SysStoreInfo> sysStoreInfos = sysStoreInfoRepoService.getBySceneNameAndObjId(
                                    StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE_NEW, templateId);
                            if (sysStoreInfos.isEmpty()) {
                throw new ServiceException("未找到对应的模板");
            }
                            SysStoreInfo templateStoreInfo = sysStoreInfos.get(0);
                            String templateLocalPath = OssUtil.downLoadFile(templateStoreInfo.getOssKey());
                            if (StringUtils.isBlank(templateLocalPath) || !new File(templateLocalPath).exists()) {
                                throw new ServiceException("模板下载失败");
                            }
                            //获取流程节点审批人
                            //根据审批人id获取签名文件
                            //根据签名文件以及签名位置SIGNATURE_POSITION_CONFIGS
                            //将图片插入图片
                    
                            // 使用 WordUtil 根据下载的模板生成 Word，注册 exama / examb / courseObject / reviewContenttable 循环行策略，并绑定数据
                            WordUtil wordUtil = WordUtil.fromFilePath(templateLocalPath);
                            wordUtil.registerRenderPolicy("exama", new LoopRowTableRenderPolicy());
                            wordUtil.registerRenderPolicy("examb", new LoopRowTableRenderPolicy());
                            wordUtil.registerRenderPolicy("courseObject", new LoopRowTableRenderPolicy());
                            wordUtil.registerRenderPolicy("approval_opinion_table", new LoopRowTableRenderPolicy());
                            wordUtil.addData("exama", examaRows);
                            wordUtil.addData("examb", exambRows);
                            wordUtil.addData("courseObject", targetCourseRows);
                            wordUtil.addData("approval_opinion_table", approvalOpinionRows);
                            // 其他数据（如 examFormData 等）
                            wordUtil.addData(data);
                    
                            return new WordExportContext(templateLocalPath, wordUtil);
    }

    /**
     * flow_93qezqks 的专用数据处理与模板构建
     * 当前与 flow_lf1rqhxe 复用相同逻辑，如后续有差异可在此方法中单独调整
     */
    private WordExportContext buildWordExportContextFor93qezqks(HistoricProcessInstance historicTaskInstance, Map<String, Object> data) {
                    // 仅构造 examFormData 的映射（fieldId -> 对应对象）
        Object examFormDataObj = historicTaskInstance.getProcessVariables().get("examFormData");
        if (ObjectUtil.isNull(examFormDataObj)) {
            throw new ServiceException("未找到对应的表单数据");
        }
        Map<String, Object> examFormDataMap = buildExamFormData(examFormDataObj);

        // 从 exam_a_table 获取列数，计算目标列数（cols - 3）
        int targetColumnCount = getTargetColumnCountFromAssessmentContentTable(examFormDataMap);

        List<Map<String, Object>> examaRows = buildAssessmentContentTableRows(examFormDataMap);
        // 课程目标与毕业要求指标点映射表行数据
        List<Map<String, Object>> targetCourseRows = buildCourseObjectTableRows(examFormDataMap);
        // 审核内容表（approval_opinion_table）行数据
        List<Map<String, Object>> approvalOpinionRows = buildApprovalOpinionRows(
                historicTaskInstance.getProcessDefinitionKey(), examFormDataMap);

        // 提取A表和B表最后一行的数据，生成tscorea1/tscorea2等和tscoreb1/tscoreb2等标签
        Map<String, Object> examALastRowData = buildAssessmentContentTableLastRow(examFormDataMap);
        data.putAll(examALastRowData);

        // 生成标题（根据学年学期）
        String title = buildTitle(historicTaskInstance);
        data.put("title", title);

        // 筛选出 DATE_PICKER 和 TEXT_INPUT 类型的数据，提取 data 字段并插入到 Word 中
        extractFormModuleData(examFormDataMap, data);

        // 从 nodeFormDataMap 中提取节点表单数据（DATE_PICKER 和 TEXT_INPUT 类型）
        extractNodeFormData(historicTaskInstance.getProcessVariables(), data);

        // 处理审批方式单选（approval_method_radio），生成带勾选的文本，插入到 {{approval_method}} 标签
        String approvalMethodText = buildApprovalMethodText(examFormDataMap);
        if (StringUtils.isNotBlank(approvalMethodText)) {
            data.put("approval_method", approvalMethodText);
        }

        // 根据配置自动插入流程节点签名图片
        fillSignaturePictures(historicTaskInstance, data);

        // 根据流程key以及行列数匹配模板（第二个参数使用 a表的列数-3）
        List<ExamWordTemplateNew> examWordTemplateNews = examWordTemplateNewRepoService.getByFlowableKeyAndColumn(
                historicTaskInstance.getProcessDefinitionKey(), targetColumnCount, 0);
        if (examWordTemplateNews.isEmpty()) {
            throw new ServiceException("未找到对应的模板");
        }
        Long templateId = examWordTemplateNews.get(0).getId();
        List<SysStoreInfo> sysStoreInfos = sysStoreInfoRepoService.getBySceneNameAndObjId(
                StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE_NEW, templateId);
        if (sysStoreInfos.isEmpty()) {
            throw new ServiceException("未找到对应的模板");
        }
        SysStoreInfo templateStoreInfo = sysStoreInfos.get(0);
        String templateLocalPath = OssUtil.downLoadFile(templateStoreInfo.getOssKey());
        if (StringUtils.isBlank(templateLocalPath) || !new File(templateLocalPath).exists()) {
            throw new ServiceException("模板下载失败");
        }
        //获取流程节点审批人
        //根据审批人id获取签名文件
        //根据签名文件以及签名位置SIGNATURE_POSITION_CONFIGS
        //将图片插入图片

        // 使用 WordUtil 根据下载的模板生成 Word，注册 exama / examb / courseObject / reviewContenttable 循环行策略，并绑定数据
        WordUtil wordUtil = WordUtil.fromFilePath(templateLocalPath);
        wordUtil.registerRenderPolicy("assessment_content_table", new LoopRowTableRenderPolicy());
        wordUtil.registerRenderPolicy("course_object_table", new LoopRowTableRenderPolicy());
        wordUtil.registerRenderPolicy("approval_opinion_table", new LoopRowTableRenderPolicy());
        wordUtil.addData("assessment_content_table", examaRows);
        wordUtil.addData("course_object_table", targetCourseRows);
        wordUtil.addData("approval_opinion_table", approvalOpinionRows);
        // 其他数据（如 examFormData 等）
            wordUtil.addData(data);

        return new WordExportContext(templateLocalPath, wordUtil);
    }

    /**
     * flow_8teoxmeh 的专用数据处理与模板构建 评价表-非考试
     * 需要将 examFormDataObj 中 fieldId = criterion_table 的表格数据
     * 作为循环行填充到 Word 模板中的 {{criterion_table}} 表格里。
     */
    private WordExportContext buildWordExportContextFor8teoxmeh(HistoricProcessInstance historicTaskInstance, Map<String, Object> data) {
        // 仅构造 examFormData 的映射（fieldId -> 对应对象）
        Object examFormDataObj = historicTaskInstance.getProcessVariables().get("examFormData");
        if (ObjectUtil.isNull(examFormDataObj)) {
            throw new ServiceException("未找到对应的表单数据");
        }
        Map<String, Object> examFormDataMap = buildExamFormData(examFormDataObj);

        // 从 criterion_table 构造循环行数据
        List<Map<String, Object>> criterionTableRows = buildCriterionTableRows(examFormDataMap);
        // 从 regular_assessment_table 构造循环行数据
        List<Map<String, Object>> regularAssessmentTableRows = buildRegularAssessmentTableRows(examFormDataMap);
        // 从 comprehensive_evaluation_table 提取数据，生成 meets_requirement1 到 meets_requirement6 文本插入
        Map<String, Object> comprehensiveEvaluationData = buildComprehensiveEvaluationData(examFormDataMap);
        data.putAll(comprehensiveEvaluationData);

        // 生成标题（根据学年学期）
        String title = buildTitle(historicTaskInstance);
        data.put("title", title);

        // 筛选出 DATE_PICKER 和 TEXT_INPUT 类型的数据，提取 data 字段并插入到 Word 中
        extractFormModuleData(examFormDataMap, data);

        // 从 nodeFormDataMap 中提取节点表单数据（DATE_PICKER 和 TEXT_INPUT 类型）
        extractNodeFormData(historicTaskInstance.getProcessVariables(), data);

        // 根据配置自动插入流程节点签名图片
        fillSignaturePictures(historicTaskInstance, data);

        // 根据流程key匹配模板（该流程模板不区分列数，列数参数传 0）
        List<ExamWordTemplateNew> examWordTemplateNews = examWordTemplateNewRepoService.getByFlowableKeyAndColumn(
                historicTaskInstance.getProcessDefinitionKey(), 0, 0);
        if (examWordTemplateNews.isEmpty()) {
            throw new ServiceException("未找到对应的模板");
        }
        Long templateId = examWordTemplateNews.get(0).getId();
        List<SysStoreInfo> sysStoreInfos = sysStoreInfoRepoService.getBySceneNameAndObjId(
                StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE_NEW, templateId);
        if (sysStoreInfos.isEmpty()) {
            throw new ServiceException("未找到对应的模板");
        }
        SysStoreInfo templateStoreInfo = sysStoreInfos.get(0);
        String templateLocalPath = OssUtil.downLoadFile(templateStoreInfo.getOssKey());
        if (StringUtils.isBlank(templateLocalPath) || !new File(templateLocalPath).exists()) {
            throw new ServiceException("模板下载失败");
        }

        // 使用 WordUtil 根据下载的模板生成 Word，注册 criterion_table 和 regular_assessment_table 循环行策略，并绑定数据
        WordUtil wordUtil = WordUtil.fromFilePath(templateLocalPath);
        wordUtil.registerRenderPolicy("criterion_table", new LoopRowTableRenderPolicy());
        wordUtil.registerRenderPolicy("regular_assessment_table", new LoopRowTableRenderPolicy());
        wordUtil.addData("criterion_table", criterionTableRows);
        wordUtil.addData("regular_assessment_table", regularAssessmentTableRows);
        // 其他数据（如 examFormData、comprehensive_evaluation 的 meets_requirement1-6 等）
        wordUtil.addData(data);

        return new WordExportContext(templateLocalPath, wordUtil);
    }

    /**
     * flow_72bddo0p 的专用数据处理与模板构建
     * 与 flow_8teoxmeh 基本一样，但多了一个 final_exam_table 表格行循环
     * 包含 criterion_table、regular_assessment_table、comprehensive_evaluation_table 和 final_exam_table 四个表格
     */
    private WordExportContext buildWordExportContextFor72bddo0p(HistoricProcessInstance historicTaskInstance, Map<String, Object> data) {
        // 仅构造 examFormData 的映射（fieldId -> 对应对象）
        Object examFormDataObj = historicTaskInstance.getProcessVariables().get("examFormData");
        if (ObjectUtil.isNull(examFormDataObj)) {
            throw new ServiceException("未找到对应的表单数据");
        }
        Map<String, Object> examFormDataMap = buildExamFormData(examFormDataObj);

        // 从 criterion_table 构造循环行数据
        List<Map<String, Object>> criterionTableRows = buildCriterionTableRows(examFormDataMap);
        // 从 regular_assessment_table 构造循环行数据
        List<Map<String, Object>> regularAssessmentTableRows = buildRegularAssessmentTableRows(examFormDataMap);
        // 从 comprehensive_evaluation_table 提取数据，生成 meets_requirement1 到 meets_requirement6 文本插入
        Map<String, Object> comprehensiveEvaluationData = buildComprehensiveEvaluationData(examFormDataMap);
        data.putAll(comprehensiveEvaluationData);
        // 从 final_exam_table 构造循环行数据
        List<Map<String, Object>> finalExamTableRows = buildFinalExamTableRows(examFormDataMap);

        // 生成标题（根据学年学期）
        String title = buildTitle(historicTaskInstance);
        data.put("title", title);

        // 筛选出 DATE_PICKER 和 TEXT_INPUT 类型的数据，提取 data 字段并插入到 Word 中
        extractFormModuleData(examFormDataMap, data);

        // 从 nodeFormDataMap 中提取节点表单数据（DATE_PICKER 和 TEXT_INPUT 类型）
        extractNodeFormData(historicTaskInstance.getProcessVariables(), data);

        // 根据配置自动插入流程节点签名图片
        fillSignaturePictures(historicTaskInstance, data);

        // 根据流程key匹配模板（该流程模板不区分列数，列数参数传 0）
        List<ExamWordTemplateNew> examWordTemplateNews = examWordTemplateNewRepoService.getByFlowableKeyAndColumn(
                historicTaskInstance.getProcessDefinitionKey(), 0, 0);
        if (examWordTemplateNews.isEmpty()) {
            throw new ServiceException("未找到对应的模板");
        }
        Long templateId = examWordTemplateNews.get(0).getId();
        List<SysStoreInfo> sysStoreInfos = sysStoreInfoRepoService.getBySceneNameAndObjId(
                StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE_NEW, templateId);
        if (sysStoreInfos.isEmpty()) {
            throw new ServiceException("未找到对应的模板");
        }
        SysStoreInfo templateStoreInfo = sysStoreInfos.get(0);
        String templateLocalPath = OssUtil.downLoadFile(templateStoreInfo.getOssKey());
        if (StringUtils.isBlank(templateLocalPath) || !new File(templateLocalPath).exists()) {
            throw new ServiceException("模板下载失败");
        }

        // 使用 WordUtil 根据下载的模板生成 Word，注册所有表格循环行策略，并绑定数据
        WordUtil wordUtil = WordUtil.fromFilePath(templateLocalPath);
        wordUtil.registerRenderPolicy("criterion_table", new LoopRowTableRenderPolicy());
        wordUtil.registerRenderPolicy("regular_assessment_table", new LoopRowTableRenderPolicy());
        wordUtil.registerRenderPolicy("final_exam_table", new LoopRowTableRenderPolicy());
        wordUtil.addData("criterion_table", criterionTableRows);
        wordUtil.addData("regular_assessment_table", regularAssessmentTableRows);
        wordUtil.addData("final_exam_table", finalExamTableRows);
        // 其他数据（如 examFormData、comprehensive_evaluation 的 meets_requirement1-6 等）
        wordUtil.addData(data);

        return new WordExportContext(templateLocalPath, wordUtil);
    }

    /**
     * 课程目标达成评价报告 数据处理与模板构建
     * flow_d4gkt8qp 的专用数据处理与模板构建
     */
    private WordExportContext buildWordExportContextForD4gkt8qp(HistoricProcessInstance historicTaskInstance, Map<String, Object> data) {
        // 仅构造 examFormData 的映射（fieldId -> 对应对象）
        Object examFormDataObj = historicTaskInstance.getProcessVariables().get("examFormData");
        if (ObjectUtil.isNull(examFormDataObj)) {
            throw new ServiceException("未找到对应的表单数据");
        }
        Map<String, Object> examFormDataMap = buildExamFormData(examFormDataObj);

        // 根据实际需求添加表格数据处理逻辑
        // content_method_table 表格构造循环行数据
        List<Map<String, Object>> contentMethodTableRows = buildContentMethodTableRows(examFormDataMap);
        data.put("content_method_table", contentMethodTableRows);
        // analysis_form_table 表格构造循环行数据
        List<Map<String, Object>> analysisFormTableRows = buildAnalysisFormTableRows(examFormDataMap);
        data.put("analysis_form_table", analysisFormTableRows);
        // 构建考核内容与评价方法分析表（表3）的行数据
        List<Map<String, Object>> requirementAlignmentRows = buildRequirementAlignmentRows(examFormDataMap);
        data.put("requirement_alignment_table", requirementAlignmentRows);


        // 课程考核结果分析数据处理（方案A：仅流程变量，从流程变量读取入学年级-专业-课程）
        Map<String, Object> queryVariablesMap = historicTaskInstance.getProcessVariables();
        Integer year = null;
        if (queryVariablesMap != null && queryVariablesMap.get("academicYear") != null) {
            try {
                year = Integer.parseInt(queryVariablesMap.get("academicYear").toString());
            } catch (NumberFormatException ignored) { }
        }
        String major = (queryVariablesMap != null && queryVariablesMap.get("major") != null)
                ? queryVariablesMap.get("major").toString() : "";
        String course = (queryVariablesMap != null && queryVariablesMap.get("course") != null)
                ? queryVariablesMap.get("course").toString() : "";
        if (year == null || StringUtils.isBlank(major) || StringUtils.isBlank(course)) {
            year = year != null ? year : 0;
        }
        Map<String, Object> courseAbilityLinkData = courseAbilityLinkDataService.getCourseAbilityLinkData(year, major, course);
        List cratHeaderConfigList = (List)courseAbilityLinkData.get("config");
        // 设置表头的data
        for(int i = 0; i < cratHeaderConfigList.size(); i++){
            // 构建键名
            String insertKeyName = "crat_title_" + (i + 1);
            Map aHeaderConfig = (Map)cratHeaderConfigList.get(i);
            Double weight =  Double.parseDouble(aHeaderConfig.get("weight").toString());
            // 构建插入值字符串
            String insertValue = aHeaderConfig.get("examType").toString() + "（" + weight + "%）";
            data.put(insertKeyName, insertValue);
        }
        List<Map<String, Object>> cratRows = buildCourseAbilityLinkTableRows(courseAbilityLinkData);
        data.put("course_result_analysis_table", cratRows);



        // 生成标题（根据学年学期）
        String title = buildTitle(historicTaskInstance);
        data.put("title", title);

        // 筛选出 DATE_PICKER 和 TEXT_INPUT 类型的数据，提取 data 字段并插入到 Word 中
        extractFormModuleData(examFormDataMap, data);

        // 从 nodeFormDataMap 中提取节点表单数据（DATE_PICKER 和 TEXT_INPUT 类型）
        extractNodeFormData(historicTaskInstance.getProcessVariables(), data);

        // 根据配置自动插入流程节点签名图片
        fillSignaturePictures(historicTaskInstance, data);

        // 根据流程key匹配模板（该流程模板不区分列数，列数参数传 0）
        int targetColSize = cratHeaderConfigList.size();
        List<ExamWordTemplateNew> examWordTemplateNews = examWordTemplateNewRepoService.getByFlowableKeyAndColumn(
                historicTaskInstance.getProcessDefinitionKey(), targetColSize, 0);
        if (examWordTemplateNews.isEmpty()) {
            throw new ServiceException("未找到对应的模板");
        }
        Long templateId = examWordTemplateNews.get(0).getId();
        List<SysStoreInfo> sysStoreInfos = sysStoreInfoRepoService.getBySceneNameAndObjId(
                StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE_NEW, templateId);
        if (sysStoreInfos.isEmpty()) {
            throw new ServiceException("未找到对应的模板");
        }
        SysStoreInfo templateStoreInfo = sysStoreInfos.get(0);
        String templateLocalPath = OssUtil.downLoadFile(templateStoreInfo.getOssKey());
        if (StringUtils.isBlank(templateLocalPath) || !new File(templateLocalPath).exists()) {
            throw new ServiceException("模板下载失败");
        }

        // 使用 WordUtil 根据下载的模板生成 Word，注册表格循环行策略，并绑定数据
        WordUtil wordUtil = WordUtil.fromFilePath(templateLocalPath);
        // 根据实际需求注册表格循环行策略
        wordUtil.registerRenderPolicy("content_method_table", new LoopRowTableRenderPolicy());
        wordUtil.registerRenderPolicy("analysis_form_table", new ComplexLoopRowTableRenderPolicy());
        wordUtil.registerRenderPolicy("course_result_analysis_table", new LoopRowTableRenderPolicy());
        wordUtil.registerRenderPolicy("requirement_alignment_table", new LoopRowTableRenderPolicy());

        // 构建图表插入
        JFreeChart chart = ChartUtils.genMultiSeriesChart("课程目标达成度评价值分析图", buildCRADData(courseAbilityLinkData));
        data.put(
                "course_result_analysis_diagram",
                Pictures.ofBufferedImage(
                    chart.createBufferedImage(
                            cratHeaderConfigList.size() > 3 ? 800 : 1200,
                            430
                    ),
                    PictureType.PNG
                ).size(
                        cratHeaderConfigList.size() > 3 ? 400 : 600,
                        215
                ).create()
        );

        // 其他数据（如 examFormData 等）
        wordUtil.addData(data);

        return new WordExportContext(templateLocalPath, wordUtil);
    }

    /**
     * 构建 课程目标达成度评价值分析图 图表数据
     * @param rowData
     * @return
     */
    private DefaultCategoryDataset buildCRADData(Map<String, Object> rowData){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<String> columnNames = new ArrayList<>();
        List<String> rowNames = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        List cratHeaderConfigList = (List)rowData.get("config");
        // 设置表头的data
        for(int i = 0; i < cratHeaderConfigList.size(); i++){
            // 构建键名
            String insertKeyName = "crat_title_" + (i + 1);
            Map aHeaderConfig = (Map)cratHeaderConfigList.get(i);
            Double weight =  Double.parseDouble(aHeaderConfig.get("weight").toString());
            // 构建插入值字符串
            rowNames.add(aHeaderConfig.get("examType").toString());
        }
        rowNames.add("课程总成绩");


        List<Map<String, Object>> data = buildCourseAbilityLinkTableRows(rowData);
        for(Map<String, Object> rowObj : data){
            Map row = (Map)rowObj;
            try {
                String colName = row.get("crat_0").toString();
                columnNames.add("课程目标" + colName);
                Double val1 = Double.parseDouble(row.get("crat_3").toString());
                values.add(val1);

                if (row.size() > 5) {
                    Double val2 = Double.parseDouble(row.get("crat_6").toString());
                    values.add(val2);
                } else {
                    Double sumVal = Double.parseDouble(row.get("crat_4").toString());
                    values.add(sumVal);
                    continue;
                }

                if (row.size() > 8) {
                    Double val3 = Double.parseDouble(row.get("crat_9").toString());
                    values.add(val3);
                } else {
                    Double sumVal = Double.parseDouble(row.get("crat_7").toString());
                    values.add(sumVal);
                    continue;
                }
                Double sumVal = Double.parseDouble(row.get("crat_10").toString());
                values.add(sumVal);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < columnNames.size(); i++) {
            for (int j = 0; j < rowNames.size(); j++) {
                dataset.addValue(
                        values.get(i * rowNames.size() + j),
                        rowNames.get(j),
                        columnNames.get(i)
                );
            }
        }


        return dataset;
    }

    /**
     * 根据流程变量或系统时间生成标题
     * 四个流程共用"学年 + 学期 + 各自表名后缀"的规则：
     *  - flow_8teoxmeh / flow_72bddo0p：课程考核内容对课程评价依据合理性审核表
     *  - flow_lf1rqhxe：课程考核命题审批表
     *  - flow_93qezqks：课程非试卷考核命题审批表
     *  其他流程沿用原来的“课程考核命题审批表”后缀
     */
    private String buildTitle(HistoricProcessInstance historicTaskInstance) {
        String flowKey = historicTaskInstance.getProcessDefinitionKey();
        Map<String, Object> variables = historicTaskInstance.getProcessVariables();

        // 1. 先确定学年与学期（从流程变量获取，缺省则按当前时间计算）
        String academicYear = null;
        String semester = null;

        if (variables != null) {
            Object academicYearObj = variables.get("academicYear");
            if (academicYearObj != null) {
                academicYear = academicYearObj.toString();
            }
            Object semesterObj = variables.get("semester");
            if (semesterObj != null) {
                semester = semesterObj.toString();
            }
        }

        if (academicYear == null || semester == null) {
            LocalDate now = LocalDate.now();
            int year = now.getYear();
            int month = now.getMonthValue();

            // 9-2月是第一学期，3-8月是第二学期
            if (month >= 9 || month <= 2) {
                // 第一学期（9-2月）：当前年～下一年
                if (month >= 9) {
                    academicYear = year + "～" + (year + 1);
                } else {
                    // 1-2月属于上一学年的第一学期
                    academicYear = (year - 1) + "～" + year;
                }
                semester = "第一学期";
            } else {
                // 第二学期（3-8月）：上一年～当前年
                academicYear = (year - 1) + "～" + year;
                semester = "第二学期";
            }
        }

        // 2. 根据流程 key 选择不同的表名后缀
        String suffix;
        if ("flow_8teoxmeh".equals(flowKey) || "flow_72bddo0p".equals(flowKey)) {
            suffix = "课程考核内容对课程评价依据合理性审核表";
        } else if ("flow_lf1rqhxe".equals(flowKey)) {
            suffix = "课程考核命题审批表";
        } else if ("flow_93qezqks".equals(flowKey)) {
            suffix = "课程非试卷考核命题审批表";
        } else {
            // 其他流程保留原来的默认后缀
            suffix = "课程考核命题审批表";
        }

        return academicYear + "学年" + semester + suffix;
    }

    /**
     * 从 examFormDataMap 中筛选出 DATE_PICKER 和 TEXT_INPUT 类型的数据，
     * 提取 data 字段（List<Map<String, String>>），以 fieldId 为标签，value 为数据插入到 Word 中
     */
    @SuppressWarnings("unchecked")
    private void extractFormModuleData(Map<String, Object> examFormDataMap, Map<String, Object> data) {
        if (examFormDataMap == null || examFormDataMap.isEmpty()) {
            return;
        }

        // 筛选出 type 为 DATE_PICKER("3") 或 TEXT_INPUT("4") 的项
        for (Map.Entry<String, Object> entry : examFormDataMap.entrySet()) {
            Object itemObj = entry.getValue();
            if (!(itemObj instanceof Map)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) itemObj;

            // 获取 type 字段
            Object typeObj = item.get("type");
            if (typeObj == null) {
                continue;
            }
            String type = typeObj.toString();

            // 只处理 DATE_PICKER 和 TEXT_INPUT 类型
            if (!FormModuleType.DATE_PICKER.getType().equals(type) 
                    && !FormModuleType.TEXT_INPUT.getType().equals(type)) {
                continue;
            }

            // 获取 data 字段
            Object dataObj = item.get("data");
            if (dataObj == null) {
                continue;
            }

            // data 应该是 List<Map<String, String>> 类型
            if (!(dataObj instanceof List)) {
                continue;
            }
            List<?> dataList = (List<?>) dataObj;

            // 遍历 data 列表，提取 fieldId 和 value
            for (Object dataItemObj : dataList) {
                if (!(dataItemObj instanceof Map)) {
                    continue;
                }
                Map<String, Object> dataItem = (Map<String, Object>) dataItemObj;

                // 获取 fieldId 和 value
                Object fieldIdObj = dataItem.get("fieldId");
                Object valueObj = dataItem.get("value");

                if (fieldIdObj != null && valueObj != null) {
                    String fieldId = fieldIdObj.toString();
                    String value = valueObj.toString();
                    // 以 fieldId 为标签，value 为数据插入到 Word 中
                    data.put(fieldId, value);
                }
            }
        }
    }

    /**
     * 从流程变量中的 nodeFormDataMap 提取节点表单数据（DATE_PICKER 和 TEXT_INPUT 类型），
     * 以 fieldId 为标签，value 为数据插入到 Word 中
     * 
     * nodeFormDataMap 结构：
     * {
     *   "Activity_xxx": {
     *     "nodeId": "Activity_xxx",
     *     "nodeFormId": 6,
     *     "formData": [
     *       {
     *         "id": 52,
     *         "fieldId": "date_1",
     *         "type": 3,
     *         "data": [
     *           {
     *             "fieldId": "group_date",
     *             "label": "教学组签字日期",
     *             "value": "2025/12/22"
     *           }
     *         ]
     *       }
     *     ],
     *     "updateTime": "2025-12-26T13:42:19.425Z"
     *   }
     * }
     */
    @SuppressWarnings("unchecked")
    private void extractNodeFormData(Map<String, Object> processVariables, Map<String, Object> data) {
        if (processVariables == null || processVariables.isEmpty()) {
            return;
        }

        // 获取 nodeFormDataMap
        Object nodeFormDataMapObj = processVariables.get("nodeFormDataMap");
        if (nodeFormDataMapObj == null) {
            return;
        }

        // nodeFormDataMap 可能是 JSON 字符串，需要解析
        Map<String, Object> nodeFormDataMap = null;
        if (nodeFormDataMapObj instanceof String) {
            try {
                nodeFormDataMap = JSON.parseObject((String) nodeFormDataMapObj, 
                    new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                // 解析失败，忽略
                return;
            }
        } else if (nodeFormDataMapObj instanceof Map) {
            nodeFormDataMap = (Map<String, Object>) nodeFormDataMapObj;
        } else {
            return;
        }

        if (nodeFormDataMap == null || nodeFormDataMap.isEmpty()) {
            return;
        }

        // 遍历所有节点的表单数据
        for (Map.Entry<String, Object> nodeEntry : nodeFormDataMap.entrySet()) {
            Object nodeDataObj = nodeEntry.getValue();
            if (!(nodeDataObj instanceof Map)) {
                continue;
            }
            Map<String, Object> nodeData = (Map<String, Object>) nodeDataObj;

            // 获取 formData 字段
            Object formDataObj = nodeData.get("formData");
            if (formDataObj == null || !(formDataObj instanceof List)) {
                continue;
            }
            List<?> formDataList = (List<?>) formDataObj;

            // 遍历该节点的所有表单模块
            for (Object moduleObj : formDataList) {
                if (!(moduleObj instanceof Map)) {
                    continue;
                }
                Map<String, Object> module = (Map<String, Object>) moduleObj;

                // 获取 type 字段（可能是数字或字符串）
                Object typeObj = module.get("type");
                if (typeObj == null) {
                    continue;
                }
                String type = typeObj.toString();

                // 只处理 DATE_PICKER("3") 和 TEXT_INPUT("4") 类型
                if (!FormModuleType.DATE_PICKER.getType().equals(type) 
                        && !FormModuleType.TEXT_INPUT.getType().equals(type)) {
                    continue;
                }

                // 获取 data 字段
                Object dataObj = module.get("data");
                if (dataObj == null || !(dataObj instanceof List)) {
                    continue;
                }
                List<?> dataList = (List<?>) dataObj;

                // 遍历 data 列表，提取 fieldId 和 value
                for (Object dataItemObj : dataList) {
                    if (!(dataItemObj instanceof Map)) {
                        continue;
                    }
                    Map<String, Object> dataItem = (Map<String, Object>) dataItemObj;

                    // 获取 fieldId 和 value
                    Object fieldIdObj = dataItem.get("fieldId");
                    Object valueObj = dataItem.get("value");

                    if (fieldIdObj != null && valueObj != null) {
                        String fieldId = fieldIdObj.toString();
                        String value = valueObj.toString();
                        // 以 fieldId 为标签，value 为数据插入到 Word 中
                        // 如果 fieldId 已存在，则覆盖（后面的节点数据覆盖前面的）
                        data.put(fieldId, value);
                    }
                }
            }
        }
    }

    /**
     * 构建审批方式文本：
     * examFormDataMap 中 fieldId = approval_method_radio 的结构类似：
     * {
     *   id=8,
     *   fieldId=approval_method_radio,
     *   type=5,
     *   data=[{fieldId=exam, label=exam, value=考试}, {fieldId=inspect, label=inspect, value=考察}],
     *   selected=考试
     * }
     * 需要生成形如：√考试  考查 或 考试  √考查 的字符串，插入到 {{approval_method}} 标签
     */
    @SuppressWarnings("unchecked")
    private String buildApprovalMethodText(Map<String, Object> examFormDataMap) {
        if (examFormDataMap == null || examFormDataMap.isEmpty()) {
            return null;
        }

        Object moduleObj = examFormDataMap.get("approval_method_radio");
        if (!(moduleObj instanceof Map)) {
            return null;
        }
        Map<String, Object> module = (Map<String, Object>) moduleObj;

        Object selectedObj = module.get("selected");
        if (selectedObj == null) {
            return null;
        }
        String selected = selectedObj.toString();

        Object dataObj = module.get("data");
        if (!(dataObj instanceof List)) {
            return null;
        }
        List<?> options = (List<?>) dataObj;
        if (options.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Object optObj : options) {
            if (!(optObj instanceof Map)) {
                continue;
            }
            Map<String, Object> opt = (Map<String, Object>) optObj;
            Object valueObj = opt.get("value");
            Object labelObj = opt.get("label");
            if (labelObj == null && valueObj == null) {
                continue;
            }
            String label = labelObj != null ? labelObj.toString() : "";
            String value = valueObj != null ? valueObj.toString() : "";
            if (StringUtils.isBlank(label) && StringUtils.isBlank(value)) {
                continue;
            }

            // 兼容 selected 可能等于 value 或 label 的情况
            boolean checked = selected.equals(value) || selected.equals(label);
            if (sb.length() > 0) {
                sb.append("  "); // 选项之间空两格
            }
            // 两个选项前都显示方框：☑ 表示选中，☐ 表示未选中，文本使用 label
            sb.append(checked ? "☑" : "☐").append(label);
        }

        return sb.toString();
    }

    /**
     * 课程目标与毕业要求指标点映射表（target_course_table）行数据构建：
     * examFormDataMap 中 fieldId = target_course_table 的结构类似：
     * {
     *   fieldId=target_course_table,
     *   type=1,
     *   rows=3,
     *   cols=2,
     *   data=[[课程目标1, 指标点1], [课程目标2, 指标点2], ...]
     * }
     * 模板中使用 {{courseObject}} 作为循环表占位符，行内使用 [course]、[object] 两个标签。
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildTargetCourseRows(Map<String, Object> examFormDataMap) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (examFormDataMap == null || examFormDataMap.isEmpty()) {
            return rows;
        }

        Object moduleObj = examFormDataMap.get("target_course_table");
        if (!(moduleObj instanceof Map)) {
            return rows;
        }
        Map<String, Object> module = (Map<String, Object>) moduleObj;

        Object dataObj = module.get("data");
        if (!(dataObj instanceof List)) {
            return rows;
        }
        List<?> dataList = (List<?>) dataObj;
        if (dataList.isEmpty()) {
            return rows;
        }

        for (Object rowObj : dataList) {
            if (!(rowObj instanceof List)) {
                continue;
            }
            List<Object> row = (List<Object>) rowObj;
            // 跳过全空行
            if (row == null || row.stream().allMatch(Objects::isNull)) {
                continue;
            }

            String course = getCellString(row, 0);
            String object = getCellString(row, 1);
            // 如果两列都为空，则跳过
            if (StringUtils.isBlank(course) && StringUtils.isBlank(object)) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("course", course);
            item.put("object", object);
            rows.add(item);
        }

        return rows;
    }

        /**
     * 课程目标与毕业要求指标点映射表（target_course_table）行数据构建：
     * examFormDataMap 中 fieldId = target_course_table 的结构类似：
     * {
     *   fieldId=target_course_table,
     *   type=1,
     *   rows=3,
     *   cols=2,
     *   data=[[课程目标1, 指标点1], [课程目标2, 指标点2], ...]
     * }
     * 模板中使用 {{courseObject}} 作为循环表占位符，行内使用 [course]、[object] 两个标签。
     */
        @SuppressWarnings("unchecked")
        private List<Map<String, Object>> buildCourseObjectTableRows(Map<String, Object> examFormDataMap) {
            List<Map<String, Object>> rows = new ArrayList<>();
            if (examFormDataMap == null || examFormDataMap.isEmpty()) {
                return rows;
            }
    
            Object moduleObj = examFormDataMap.get("course_object_table");
            if (!(moduleObj instanceof Map)) {
                return rows;
            }
            Map<String, Object> module = (Map<String, Object>) moduleObj;
    
            Object dataObj = module.get("data");
            if (!(dataObj instanceof List)) {
                return rows;
            }
            List<?> dataList = (List<?>) dataObj;
            if (dataList.isEmpty()) {
                return rows;
            }
    
            for (Object rowObj : dataList) {
                if (!(rowObj instanceof List)) {
                    continue;
                }
                List<Object> row = (List<Object>) rowObj;
                // 跳过全空行
                if (row == null || row.stream().allMatch(Objects::isNull)) {
                    continue;
                }
    
                String course = getCellString(row, 0);
                String object = getCellString(row, 1);
                // 如果两列都为空，则跳过
                if (StringUtils.isBlank(course) && StringUtils.isBlank(object)) {
                    continue;
                }
    
                Map<String, Object> item = new HashMap<>();
                item.put("course_object", course);
                item.put("course_object_remark", object);
                rows.add(item);
            }
    
            return rows;
        }

        /**
     * 课程考核内容对课程评价依据合理性审核表中的“课程目标-毕业要求指标点-平时/期末”表格行数据构建：
     * examFormDataMap 中 fieldId = criterion_table 的结构类似：
     * {
     *   fieldId=criterion_table,
     *   type=1,
     *   rows=n,
     *   cols=4,
     *   data=[[课程目标1, 指标点1, 平时1, 期末1], [课程目标2, 指标点2, 平时2, 期末2], ...]
     * }
     * 模板中使用 {{criterion_table}} 作为循环表占位符，行内使用
     * [course_objective]、[graduation_metric]、[regular_grade]、[final_exam] 四个标签。
     */
        @SuppressWarnings("unchecked")
        private List<Map<String, Object>> buildCriterionTableRows(Map<String, Object> examFormDataMap) {
            List<Map<String, Object>> rows = new ArrayList<>();
            if (examFormDataMap == null || examFormDataMap.isEmpty()) {
                return rows;
            }

            Object moduleObj = examFormDataMap.get("criterion_table");
            if (!(moduleObj instanceof Map)) {
                return rows;
            }
            Map<String, Object> module = (Map<String, Object>) moduleObj;

            Object dataObj = module.get("data");
            if (!(dataObj instanceof List)) {
                return rows;
            }
            List<?> dataList = (List<?>) dataObj;
            if (dataList.isEmpty()) {
                return rows;
            }

            for (Object rowObj : dataList) {
                if (!(rowObj instanceof List)) {
                    continue;
                }
                List<Object> row = (List<Object>) rowObj;
                // 跳过全空行
                if (row == null || row.stream().allMatch(Objects::isNull)) {
                    continue;
                }

                String courseObjective = getCellString(row, 0);
                String graduationMetric = getCellString(row, 1);
                String regularGrade = getCellString(row, 2);
                String finalExam = getCellString(row, 3);

                // 若整行完全为空，则跳过
                if (StringUtils.isBlank(courseObjective)
                        && StringUtils.isBlank(graduationMetric)
                        && StringUtils.isBlank(regularGrade)
                        && StringUtils.isBlank(finalExam)) {
                    continue;
                }

                Map<String, Object> item = new HashMap<>();
                item.put("course_objective", courseObjective);
                item.put("graduation_metric", graduationMetric);
                item.put("regular_grade", regularGrade);
                item.put("final_exam", finalExam);
                rows.add(item);
            }

            return rows;
        }

        /**
     * 平时考核支撑课程目标达成的途径表格行数据构建：
     * examFormDataMap 中 fieldId = regular_assessment_table 的结构类似：
     * {
     *   fieldId=regular_assessment_table,
     *   type=1,
     *   rows=n,
     *   cols=3,
     *   data=[[课程目标1, 平时考核项目1, 分值1], [课程目标2, 平时考核项目2, 分值2], ...]
     * }
     * 模板中使用 {{regular_assessment_table}} 作为循环表占位符，行内使用
     * [course_objective]、[regular_grade]、[regular_grade_score] 三个标签。
     */
        @SuppressWarnings("unchecked")
        private List<Map<String, Object>> buildRegularAssessmentTableRows(Map<String, Object> examFormDataMap) {
            List<Map<String, Object>> rows = new ArrayList<>();
            if (examFormDataMap == null || examFormDataMap.isEmpty()) {
                return rows;
            }

            Object moduleObj = examFormDataMap.get("regular_assessment_table");
            if (!(moduleObj instanceof Map)) {
                return rows;
            }
            Map<String, Object> module = (Map<String, Object>) moduleObj;

            Object dataObj = module.get("data");
            if (!(dataObj instanceof List)) {
                return rows;
            }
            List<?> dataList = (List<?>) dataObj;
            if (dataList.isEmpty()) {
                return rows;
            }

            for (Object rowObj : dataList) {
                if (!(rowObj instanceof List)) {
                    continue;
                }
                List<Object> row = (List<Object>) rowObj;
                // 跳过全空行
                if (row == null || row.stream().allMatch(Objects::isNull)) {
                    continue;
                }

                String courseObjective = getCellString(row, 0);
                String regularGrade = getCellString(row, 1);
                String regularGradeScore = getCellString(row, 2);

                // 若整行完全为空，则跳过
                if (StringUtils.isBlank(courseObjective)
                        && StringUtils.isBlank(regularGrade)
                        && StringUtils.isBlank(regularGradeScore)) {
                    continue;
                }

                Map<String, Object> item = new HashMap<>();
                item.put("course_objective", courseObjective);
                item.put("regular_grade", regularGrade);
                item.put("regular_grade_score", regularGradeScore);
                rows.add(item);
            }

            return rows;
        }

        /**
     * 综合评价数据构建（文本插入方式，不再是表格行循环）：
     * 从 comprehensive_evaluation_table 的 data 中读取六行数据，生成 meets_requirement1 到 meets_requirement6。
     * examFormDataMap 中 fieldId = comprehensive_evaluation_table 的结构类似：
     * {
     *   fieldId=comprehensive_evaluation_table,
     *   type=1,
     *   rows=n,
     *   cols=1,
     *   data=[[是否符合要求1], [是否符合要求2], ...]
     * }
     * 模板中使用 {{meets_requirement1}} 到 {{meets_requirement6}} 作为文本占位符进行插入。
     * 评估标准固定为以下六行（在Word模板中固定显示，不需要从数据中读取）：
     * 1. 考核形式是否合理
     * 2. 考核要求是否明确
     * 3. 考核内容是否覆盖毕业要求指标点
     * 4. 考核观测点是否明确，学习成果物是否可衡量
     * 5. 平时作业、报告、设计及其它材料内容是否完整、符合规范
     * 6. 批阅过程是否规范、严谨
     */
        @SuppressWarnings("unchecked")
        private Map<String, Object> buildComprehensiveEvaluationData(Map<String, Object> examFormDataMap) {
            Map<String, Object> result = new HashMap<>();
            
            // 从 examFormDataMap 中读取 data，获取 meets_requirement 的值
            List<?> dataList = new ArrayList<>();
            if (examFormDataMap != null && !examFormDataMap.isEmpty()) {
                Object moduleObj = examFormDataMap.get("comprehensive_evaluation_table");
                if (moduleObj instanceof Map) {
                    Map<String, Object> module = (Map<String, Object>) moduleObj;
                    Object dataObj = module.get("data");
                    if (dataObj instanceof List) {
                        dataList = (List<?>) dataObj;
                    }
                }
            }

            // 生成 meets_requirement1 到 meets_requirement6，从 data 中读取对应的值
            for (int i = 0; i < 6; i++) {
                String meetsRequirement = "";
                
                // 如果 data 中有对应行的数据，读取 meets_requirement（第一列，索引为0）
                if (i < dataList.size() && dataList.get(i) instanceof List) {
                    List<Object> row = (List<Object>) dataList.get(i);
                    if (row != null && !row.isEmpty()) {
                        meetsRequirement = getCellString(row, 0);
                    }
                }
                
                result.put("meets_requirement" + (i + 1), meetsRequirement);
            }

            return result;
        }

        /**
     * 期末考查支撑课程目标达成的途径表格行数据构建：
     * examFormDataMap 中 fieldId = final_exam_table 的结构类似：
     * {
     *   fieldId=final_exam_table,
     *   type=1,
     *   rows=n,
     *   cols=3,
     *   data=[[课程目标1, 期末考查项目1, 分值1], [课程目标2, 期末考查项目2, 分值2], ...]
     * }
     * 模板中使用 {{final_exam_table}} 作为循环表占位符，行内使用
     * [course_objective]、[final_exam]、[final_exam_score] 三个标签。
     */
        @SuppressWarnings("unchecked")
        private List<Map<String, Object>> buildFinalExamTableRows(Map<String, Object> examFormDataMap) {
            List<Map<String, Object>> rows = new ArrayList<>();
            if (examFormDataMap == null || examFormDataMap.isEmpty()) {
                return rows;
            }

            Object moduleObj = examFormDataMap.get("final_exam_table");
            if (!(moduleObj instanceof Map)) {
                return rows;
            }
            Map<String, Object> module = (Map<String, Object>) moduleObj;

            Object dataObj = module.get("data");
            if (!(dataObj instanceof List)) {
                return rows;
            }
            List<?> dataList = (List<?>) dataObj;
            if (dataList.isEmpty()) {
                return rows;
            }

            for (Object rowObj : dataList) {
                if (!(rowObj instanceof List)) {
                    continue;
                }
                List<Object> row = (List<Object>) rowObj;
                // 跳过全空行
                if (row == null || row.stream().allMatch(Objects::isNull)) {
                    continue;
                }

                String courseObjective = getCellString(row, 0);
                String finalExam = getCellString(row, 1);
                String finalExamScore = getCellString(row, 2);

                // 若整行完全为空，则跳过
                if (StringUtils.isBlank(courseObjective)
                        && StringUtils.isBlank(finalExam)
                        && StringUtils.isBlank(finalExamScore)) {
                    continue;
                }

                Map<String, Object> item = new HashMap<>();
                item.put("course_objective", courseObjective);
                item.put("final_exam", finalExam);
                item.put("final_exam_score", finalExamScore);
                rows.add(item);
            }

            return rows;
        }

    /**
     * 课程目标达成评价报告 教学内容及教学方法分析表格 行数据构建：
     * examFormDataMap 中 fieldId = content_method_table 的结构类似：
     * {
     *   fieldId=content_method_table,
     *   type=1,
     *   rows=n,
     *   cols=2,
     *   data=[[课程目标1, 分析内容1], [课程目标2, 分析内容2], ...]
     * }
     * 模板中使用 {{content_method_table}} 作为循环表占位符，行内使用
     * [course_object]、[analysis_content] 两个标签。
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildContentMethodTableRows(Map<String, Object> examFormDataMap) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (examFormDataMap == null || examFormDataMap.isEmpty()) {
            return rows;
        }

        Object moduleObj = examFormDataMap.get("content_method_table");
        if (!(moduleObj instanceof Map)) {
            return rows;
        }
        Map<String, Object> module = (Map<String, Object>) moduleObj;

        Object dataObj = module.get("data");
        if (!(dataObj instanceof List)) {
            return rows;
        }
        List<?> dataList = (List<?>) dataObj;
        if (dataList.isEmpty()) {
            return rows;
        }

        for (Object rowObj : dataList) {
            if (!(rowObj instanceof List)) {
                continue;
            }
            List<Object> row = (List<Object>) rowObj;
            // 跳过全空行
            if (row == null || row.stream().allMatch(Objects::isNull)) {
                continue;
            }

            String courseObject = getCellString(row, 0);
            String analysisContent = getCellString(row, 1);

            // 若整行完全为空，则跳过
            if (StringUtils.isBlank(courseObject)
                    && StringUtils.isBlank(analysisContent)) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("course_object", courseObject);
            item.put("analysis_content", analysisContent);
            rows.add(item);
        }

        return rows;
    }

    /**
     * 课程考核结果分析表 渲染数据转换。
     * 将 /api/graduate/course-report/exam-result-analysis 返回的 {config, data} 转为表格行列表。
     * 每行对应一个课程目标，含 objective、assessments（与 config 同序）、objectiveAchievement（加权达成值）。
     *
     * @param courseAbilityLinkData API 返回的 {config: [{examType, weight}, ...], data: [[{evaluationCriteria, averange/target, achievedValue}, ...], ...]}
     * @return [{objective, assessments, objectiveAchievement}, ...]
     *
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildCourseAbilityLinkTableRows(Map<String, Object> courseAbilityLinkData) {
        List<Map<String, Object>> rows = new ArrayList<>();
        // 准备达成值需要的数据
        List cratHeaderConfigList = (List)courseAbilityLinkData.get("config");
        List<Double> weightList = new ArrayList<>();
        for (int i = 0; i < cratHeaderConfigList.size(); i++) {
            Map aConfigMap = (Map) cratHeaderConfigList.get(i);
            weightList.add(Double.parseDouble(aConfigMap.get("weight").toString()));
        }
        // 数据行生成
        List dataList = (List) courseAbilityLinkData.get("data");
        for(int i = 0; i < dataList.size(); i++) {
            Map<String, Object> row = new HashMap<>();
            List aDataListItem = (List)dataList.get(i);
            row.put("crat_0", i + 1);
            int cnt = 0;
            List<Double> achievedValueList = new ArrayList<>();
            for(int j = 0; j < weightList.size(); j++) {
                // 构建键值
                String keyValue1 = "crat_" + (j * 3 + 1);
                String keyValue2 = "crat_" + (j * 3 + 2);
                String keyValue3 = "crat_" + (j * 3 + 3);
                // 获取对应填充值
                Map aMapItem = (Map) aDataListItem.get(j);
                String insertValue1 = aMapItem.get("evaluationCriteria").toString();
                String insertValue2 = aMapItem.get("averange/target").toString();
                if (Objects.equals(insertValue2, "0")) cnt++;
                String insertValue3 = aMapItem.get("achievedValue").toString();
                achievedValueList.add(Double.parseDouble(insertValue3));
                row.put(keyValue1, insertValue1);
                row.put(keyValue2, insertValue2);
                row.put(keyValue3, insertValue3);
            }
            double finalAchievedValue = 0d;
            for(int j = 0; j < weightList.size(); j++) {
                finalAchievedValue += weightList.get(j) * achievedValueList.get(j);
            }
            String finalAchievedValueKey = "crat_" + (weightList.size() * 3 + 1);
            row.put(finalAchievedValueKey, finalAchievedValue);
            if (cnt == aDataListItem.size()) {
                continue;
            }
            rows.add(row);
        }

        return rows;
    }

    /**
     * 从单个考核单元格解析达成值数值（0~1）。
     * achievedValue 可能为 "74.1%"、"0"；若 evaluationCriteria 含 无数据/无相关题目/计算错误 则视为 0。
     */
    private double parseAchievedValueDecimal(Map<String, Object> cell) {
        Object ecObj = cell.get("evaluationCriteria");
        if (ecObj != null) {
            String s = ecObj.toString();
            if (s.contains("无数据") || s.contains("无相关题目") || s.contains("计算错误")) {
                return 0;
            }
        }
        Object avObj = cell.get("achievedValue");
        if (avObj == null) {
            return 0;
        }
        String av = avObj.toString().trim();
        if (av.isEmpty()) {
            return 0;
        }
        try {
            if (av.endsWith("%")) {
                return Double.parseDouble(av.substring(0, av.length() - 1).trim()) / 100.0;
            }
            double v = Double.parseDouble(av);
            return v > 1 ? v / 100.0 : v;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 课程目标达成评价报告 考核内容与评价方法合理性分析表 行数据构建
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildAnalysisFormTableRows(Map<String, Object> examFormDataMap) {
        Object analysisFormObj = examFormDataMap.get("analysis_form_table");
        if (!(analysisFormObj instanceof Map)) {
            return new ArrayList<>();
        }
        Map<String, Object> analysisForm = (Map<String, Object>) analysisFormObj;
        Object dataObj = analysisForm.get("parentRows");
        if (!(dataObj instanceof List)) {
            return new ArrayList<>();
        }
        List<?> dataList = (List<?>) dataObj;
        if (dataList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> rows = new ArrayList<>();

        for (Object rowObj : dataList) {
            if (!(rowObj instanceof LinkedHashMap)) {
                continue;
            }
            LinkedHashMap<String, List> row = (LinkedHashMap<String, List>) rowObj;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("objective", row.get("parent"));
            List<Map<String, String>> subjectiveDetails = new ArrayList<>();
            for (Object childObj : row.get("children")) {
                if (!(childObj instanceof Map)) {
                    continue;
                }
                Map<String, String> child = (Map<String, String>) childObj;
                Map<String, String> childItem = new LinkedHashMap<>();
                childItem.put("exam_type", child.get("child"));
                childItem.put("exam_content", child.get("value"));
                subjectiveDetails.add(childItem);
            }
            item.put("details", subjectiveDetails);
            rows.add(item);
        }

            return rows;
        }

    /**
     * 审核内容表（approval_opinion_table）动态行数据构建：
     * examFormDataMap 中 fieldId = approval_opinion_table 的结构类似：
     * {
     *   fieldId=approval_opinion_table,
     *   type=1,
     *   rows=n,
     *   cols=3,
     *   data=[[√, , ], [, √, ], ...],
     *   rowHeaders=[... 行说明 ...],
     *   colHeaders=[优秀, 良好, 整改]
     * }
     * 模板中使用 {{reviewContenttable}} 作为循环表占位符，行内使用
     * [reviewContent]、[excellent]、[Good]、[rectification] 四个占位符。
     * 其中 reviewContent 按流程 key 从 APPROVAL_REVIEW_CONTENT_MAP 中获取对应的 List，
     * List 有多少条就生成多少行，不再强制限定为 5 条。
     * 其它三列从 data 中取对应单元格。
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildApprovalOpinionRows(String flowKey, Map<String, Object> examFormDataMap) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (examFormDataMap == null || examFormDataMap.isEmpty()) {
            return rows;
        }

        Object moduleObj = examFormDataMap.get("approval_opinion_table");
        if (!(moduleObj instanceof Map)) {
            return rows;
        }
        Map<String, Object> module = (Map<String, Object>) moduleObj;

        Object dataObj = module.get("data");
        if (!(dataObj instanceof List)) {
            return rows;
        }
        List<?> dataList = (List<?>) dataObj;

        // 根据当前流程 key 获取对应的审核内容配置；若无专门配置则使用 default
        List<String> reviewContentList = APPROVAL_REVIEW_CONTENT_MAP.getOrDefault(
                flowKey, APPROVAL_REVIEW_CONTENT_MAP.get("default"));

        // 按配置 List 的行数生成；若 data 行数不足，用空值补齐
        int maxRows = reviewContentList.size();
        for (int i = 0; i < maxRows; i++) {
            List<Object> row = null;
            if (i < dataList.size() && dataList.get(i) instanceof List) {
                row = (List<Object>) dataList.get(i);
            }
            if (row == null) {
                row = new ArrayList<>();
            }

            Map<String, Object> item = new HashMap<>();
            // 固定的审核内容说明（随流程 key 变化）
            String reviewContent = reviewContentList.get(i);
            item.put("reviewContent", reviewContent);

            // 三个结论列：优秀、良好、整改
            String excellent = getCellString(row, 0);
            String good = getCellString(row, 1);
            String rectification = getCellString(row, 2);

            item.put("excellent", excellent);
            // 模板中占位符为 [Good]，因此 key 使用首字母大写的 Good
            item.put("Good", good);
            item.put("rectification", rectification);

            rows.add(item);
        }

        return rows;
    }

    /**
     * 课程目标与毕业要求指标点对应关系分析（requirement_alignment_table）行数据构建：
     * examFormDataMap 中 fieldId = requirement_alignment_table 的结构类似：
     * {
     *   fieldId=target_course_table,
     *   type=1,
     *   rows=3,
     *   cols=3,
     *   data=[[课程目标1, 毕业要求1, 毕业要求指标点1], [课程目标2, 毕业要求2, 毕业要求指标点2], ...]
     * }
     * 模板中使用 {{requirement_alignment_table}} 作为循环表占位符，行内使用 [course]、[request]、[indexPoint] 三个标签。
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildRequirementAlignmentRows(Map<String, Object> examFormDataMap) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (examFormDataMap == null || examFormDataMap.isEmpty()) {
            return rows;
        }
        Object moduleObj = examFormDataMap.get("requirement_alignment_table");
        if (!(moduleObj instanceof Map)) {
            return rows;
        }
        Map<String, Object> module = (Map<String, Object>) moduleObj;
        Object dataObj = module.get("data");
        if (!(dataObj instanceof List)) {
            return rows;
        }
        List<?> dataList = (List<?>) dataObj;
        if(dataList.isEmpty()){
            return rows;
        }
        for (Object rowObj : dataList) {
            if (!(rowObj instanceof List)) {
                continue;
            }
            List<Object> row = (List<Object>) rowObj;
            if (row == null || row.stream().allMatch(Objects::isNull)) {
                continue;
            }
            String course = getCellString(row, 0);
            String request = getCellString(row, 1);
            String indexPoint = getCellString(row, 2);
            if (StringUtils.isBlank(course) && StringUtils.isBlank(request) && StringUtils.isBlank(indexPoint)) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("course", course);
            item.put("request", request);
            item.put("indexPoint", indexPoint);
            rows.add(item);
        }
        return rows;
    }



    /**
     * 根据配置为当前流程实例填充签名图片占位符：
     * 1. 通过配置表 SIGNATURE_POSITION_CONFIGS 找到当前流程 key 对应的签名节点
     * 2. 对每个节点：
     *    - 查询该节点的最新历史任务，取 assignee 作为签名人
     *    - 根据签名人 ID，从文件存储表中查找电子签名文件（使用电子签名场景）
     *    - 下载签名图片到本地临时文件
     *    - 将图片包装成 PictureRenderData，放入 data 中，对应配置的占位符 key
     *
     * 模板中可直接使用 {{teacherSign}}、{{directorSign}} 等占位符来显示签名图片。
     */
    private void fillSignaturePictures(HistoricProcessInstance historicTaskInstance, Map<String, Object> data) {
        if (SIGNATURE_POSITION_CONFIGS == null || SIGNATURE_POSITION_CONFIGS.isEmpty()) {
            return;
        }

        String flowKey = historicTaskInstance.getProcessDefinitionKey();
        String procInsId = historicTaskInstance.getId();

        for (SignaturePositionConfig config : SIGNATURE_POSITION_CONFIGS) {
            if (!flowKey.equals(config.getFlowKey())) {
                continue;
            }

            try {
                // 查询指定节点的历史任务记录（取最近一次）
                List<HistoricActivityInstance> historicTasks = historyService
                        .createHistoricActivityInstanceQuery()
                        .processInstanceId(procInsId)
                        .activityId(config.getNodeId())
                        .orderByHistoricActivityInstanceStartTime()
                        .desc()
                        .list();

                if (historicTasks == null || historicTasks.isEmpty()) {
                    log.warn("未找到流程实例 {} 节点 {} 的历史任务记录，跳过签名插入", procInsId, config.getNodeId());
                    continue;
                }

                // 获取最新一次任务的办理人 ID
                String userIdStr = historicTasks.get(0).getAssignee();
                if (StringUtils.isBlank(userIdStr)) {
                    log.warn("流程实例 {} 节点 {} 办理人为空，跳过签名插入", procInsId, config.getNodeId());
                    continue;
                }
                Long userId = Long.parseLong(userIdStr);

                // 根据签名人 ID 查询签名文件（使用配置的 sceneName/场景）
                String sceneName = config.getSceneName();
                if (StringUtils.isBlank(sceneName)) {
                    sceneName = ExamElectronicType.ELECTRONIC_SIGNATURE.getReflect();
                }
                LambdaQueryWrapper<SysStoreInfo> queryWrapper = Wrappers.<SysStoreInfo>lambdaQuery()
                        .eq(SysStoreInfo::getObjId, userId)
                        .eq(SysStoreInfo::getSceneName, sceneName);
                SysStoreInfo signerStoreInfo = sysStoreInfoRepoService.getOne(queryWrapper);
                if (signerStoreInfo == null) {
                    log.warn("用户 {} 未上传电子签名文件，跳过签名插入，占位符={}", userId, config.getSignatureLocation());
                    continue;
                }

                String signPath = OssUtil.downLoadFile(signerStoreInfo.getOssKey());
                if (signPath == null || !new File(signPath).exists()) {
                    log.warn("下载签名文件失败: ossKey={}, 占位符={}", signerStoreInfo.getOssKey(), config.getSignatureLocation());
                    continue;
                }

                // 将签名图片绑定到模板占位符
                data.put(config.getSignatureLocation(), new PictureRenderData(80, 40, signPath));
        } catch (Exception e) {
                log.error("填充签名图片失败，flowKey={}, nodeId={}, placeholder={}", config.getFlowKey(),
                        config.getNodeId(), config.getSignatureLocation(), e);
            }
        }
    }

    /**
     * 签名位置配置：定义流程 key、节点 key、文件场景(sceneName) 与 Word 模板占位符之间的映射关系
     */
    private static class SignaturePositionConfig {
        private final String flowKey;
        private final String nodeId;
        private final String signatureLocation;
        private final String sceneName;

        SignaturePositionConfig(String flowKey, String nodeId, String signatureLocation, String sceneName) {
            this.flowKey = flowKey;
            this.nodeId = nodeId;
            this.signatureLocation = signatureLocation;
            this.sceneName = sceneName;
        }

        public String getFlowKey() {
            return flowKey;
        }

        public String getNodeId() {
            return nodeId;
        }

        public String getSignatureLocation() {
            return signatureLocation;
        }

        public String getSceneName() {
            return sceneName;
        }
    }

    /**
     * 导出上下文，便于主流程控制文件名、输出和清理
     */
    private static class WordExportContext {
        private final String templateLocalPath;
        private final WordUtil wordUtil;

        WordExportContext(String templateLocalPath, WordUtil wordUtil) {
            this.templateLocalPath = templateLocalPath;
            this.wordUtil = wordUtil;
        }

        public String getTemplateLocalPath() {
            return templateLocalPath;
        }

        public WordUtil getWordUtil() {
            return wordUtil;
        }
    }

    private String getCellString(List<Object> row, int index) {
        if (row == null || index >= row.size()) {
            return "";
        }
        Object v = row.get(index);
        if (v == null) {
            return "";
        }
        // 布尔类型用于勾选矩阵：true => "√"；false/其他 => 空
        if (v instanceof Boolean) {
            return Boolean.TRUE.equals(v) ? "√" : "";
        }
        return v.toString();
    }

    private SysStoreInfo getTemplateStoreInfo(Long templateId) {
        LambdaQueryWrapper<SysStoreInfo> queryWrapper = Wrappers.<SysStoreInfo>lambdaQuery()
                .eq(SysStoreInfo::getSceneName, StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE)
                .eq(SysStoreInfo::getObjId, templateId);
        return sysStoreInfoRepoService.getOne(queryWrapper);
    }


    private void cleanUp(List<String> deleteFileList) {
        for (String filePath : deleteFileList) {
            if (filePath != null) {
                Path path = Paths.get(filePath);
                if (Files.exists(path)) {
                    try {
                        Files.delete(path);
                        log.info("Deleted file: {}", filePath);
                    } catch (IOException e) {
                        log.warn("Failed to delete temp file: {}", e.getMessage());
                    }
                } else {
                    log.info("File does not exist: {}", filePath);
                }
            }
        }
    }

    private static class CustomException extends RuntimeException {
        public CustomException(String message) {
            super(message);
        }
    }


    private SysStoreInfo uploadFile(String file, String sceneName, Long objId) {
        try {
            FileDTO fileDTO = OssUtil.uploadFileByPath(file);

            SysStoreInfo sysStoreInfo = new SysStoreInfo();
            sysStoreInfo.setOssKey(fileDTO.getKey());
            sysStoreInfo.setUrl(fileDTO.getUrl());
            sysStoreInfo.setSceneName(sceneName);
            sysStoreInfo.setObjId(objId);
            sysStoreInfo.setShortLink(HashUtil.generateMD5Hash());
            sysStoreInfoRepoService.save(sysStoreInfo);
            return sysStoreInfo;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 流程节点信息
     *
     * @param procInsId
     * @param elementId
     * @return
     */
    @Override
    public AjaxResult flowTaskInfo(String procInsId, String elementId) {
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(procInsId)
                .activityId(elementId)
                .list();
        // 退回任务后有多条数据 只取待办任务进行展示
        list.removeIf(task -> StringUtils.isNotBlank(task.getDeleteReason()));
        if (CollectionUtils.isEmpty(list)) {
            return AjaxResult.success();
        }
        if (list.size() > 1) {
            list.removeIf(task -> Objects.nonNull(task.getEndTime()));
        }
        HistoricActivityInstance histIns = list.get(0);
        FlowTaskDto flowTask = new FlowTaskDto();
        flowTask.setTaskId(histIns.getTaskId());
        flowTask.setTaskName(histIns.getActivityName());
        flowTask.setCreateTime(histIns.getStartTime());
        flowTask.setFinishTime(histIns.getEndTime());
        if (StringUtils.isNotBlank(histIns.getAssignee())) {
            SysUser sysUser = sysUserService.selectUserById(Long.parseLong(histIns.getAssignee()));
            flowTask.setAssigneeId(sysUser.getUserId());
            flowTask.setAssigneeName(sysUser.getNickName());
//            flowTask.setDeptName(Objects.nonNull(sysUser.getDept()) ? sysUser.getDept().getDeptName() : "");

        }
        // 流程变量信息
//        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
//                .includeProcessVariables().finished().taskId(histIns.getTaskId()).singleResult();
//        flowTask.setVariables(historicTaskInstance.getProcessVariables());

        // 展示审批人员
        List<HistoricIdentityLink> linksForTask = historyService.getHistoricIdentityLinksForTask(histIns.getTaskId());
        StringBuilder stringBuilder = new StringBuilder();
        for (HistoricIdentityLink identityLink : linksForTask) {
            // 获选人,候选组/角色(多个)
            if ("candidate".equals(identityLink.getType())) {
                if (StringUtils.isNotBlank(identityLink.getUserId())) {
                    SysUser sysUser = sysUserService.selectUserById(Long.parseLong(identityLink.getUserId()));
                    stringBuilder.append(sysUser.getNickName()).append(",");
                }
                if (StringUtils.isNotBlank(identityLink.getGroupId())) {
                    SysRole sysRole = sysRoleService.selectRoleById(Long.parseLong(identityLink.getGroupId()));
                    stringBuilder.append(sysRole.getRoleName()).append(",");
                }
            }
        }
        if (StringUtils.isNotBlank(stringBuilder)) {
            flowTask.setCandidate(stringBuilder.substring(0, stringBuilder.length() - 1));
        }

        flowTask.setDuration(histIns.getDurationInMillis() == null || histIns.getDurationInMillis() == 0 ? null : getDate(histIns.getDurationInMillis()));
        // 获取意见评论内容
        List<Comment> commentList = taskService.getProcessInstanceComments(histIns.getProcessInstanceId());
        commentList.forEach(comment -> {
            if (histIns.getTaskId().equals(comment.getTaskId())) {
                flowTask.setComment(FlowCommentDto.builder().type(comment.getType()).comment(comment.getFullMessage()).build());
            }
        });
        return AjaxResult.success(flowTask);
    }

    /**
     * 将Object类型的数据转化成Map<String,Object>
     *
     * @param obj
     * @return
     * @throws Exception
     */
    public Map<String, Object> obj2Map(Object obj) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
        }
        return map;
    }

    /**
     * 流程完成时间处理
     *
     * @param ms
     * @return
     */
    private String getDate(long ms) {

        long day = ms / (24 * 60 * 60 * 1000);
        long hour = (ms / (60 * 60 * 1000) - day * 24);
        long minute = ((ms / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long second = (ms / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);

        if (day > 0) {
            return day + "天" + hour + "小时" + minute + "分钟";
        }
        if (hour > 0) {
            return hour + "小时" + minute + "分钟";
        }
        if (minute > 0) {
            return minute + "分钟";
        }
        if (second > 0) {
            return second + "秒";
        } else {
            return 0 + "秒";
        }
    }
}
