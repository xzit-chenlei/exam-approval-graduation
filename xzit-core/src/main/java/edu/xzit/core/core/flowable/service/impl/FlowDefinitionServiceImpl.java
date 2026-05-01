package edu.xzit.core.core.flowable.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.domain.entity.SysUser;
import edu.xzit.core.core.common.exception.ServiceException;
import edu.xzit.core.core.common.utils.SecurityUtils;
import edu.xzit.core.core.flowable.constant.ProcessConstants;
import edu.xzit.core.core.flowable.domain.dto.FlowNodeDTO;
import edu.xzit.core.core.flowable.domain.dto.NodeInfoDto;
import edu.xzit.core.core.flowable.enums.FlowComment;
import edu.xzit.core.core.flowable.factory.FlowServiceFactory;
import edu.xzit.core.core.flowable.service.IFlowDefinitionService;
import edu.xzit.core.core.flowable.service.ISysDeployFormService;
import edu.xzit.core.dao.domain.ExamTaskDept;
import edu.xzit.core.dao.domain.FlowProcDefDto;
import edu.xzit.core.dao.domain.SysDeployForm;
import edu.xzit.core.dao.domain.SysForm;
import edu.xzit.core.dao.mapper.ExamTaskDeptMapper;
import edu.xzit.core.dao.mapper.FlowDeployMapper;
import edu.xzit.core.dao.service.IExamTaskDeptService;
import edu.xzit.core.dao.domain.ExamTaskTeachingResearchOffice;
import edu.xzit.core.dao.service.IExamTaskTeachingResearchOfficeService;
import edu.xzit.core.dao.service.ISysDeptService;
import edu.xzit.core.dao.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 流程定义
 *
 * @author Tony
 * @date 2021-04-03
 */
@Service
@Slf4j
public class FlowDefinitionServiceImpl extends FlowServiceFactory implements IFlowDefinitionService {

    @Resource
    private ISysDeployFormService sysDeployFormService;

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private ISysDeptService sysDeptService;

    @Resource
    private IExamTaskDeptService examTaskDeptService;
    @Resource
    private IExamTaskTeachingResearchOfficeService examTaskTeachingResearchOfficeService;

    @Resource
    private FlowDeployMapper flowDeployMapper;

    private static final String BPMN_FILE_SUFFIX = ".bpmn";

    @Override
    public boolean exist(String processDefinitionKey) {
        ProcessDefinitionQuery processDefinitionQuery
                = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey);
        long count = processDefinitionQuery.count();
        return count > 0 ? true : false;
    }


    /**
     * 流程定义列表
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 流程定义分页列表数据
     */
    @Override
    public Page<FlowProcDefDto> list(String name, Boolean fuzzyQuery, Integer pageNum, Integer pageSize) {
        Page<FlowProcDefDto> page = new Page<>();
        PageHelper.startPage(pageNum, pageSize);

        final List<FlowProcDefDto> dataList;
        if (Objects.equals(Boolean.FALSE, fuzzyQuery)) {
            dataList = flowDeployMapper.selectDeployListNonFuzzy(name);
        } else {
            dataList = flowDeployMapper.selectDeployList(name);
        }

        // 加载挂表单
        for (FlowProcDefDto procDef : dataList) {
            SysForm sysForm = sysDeployFormService.selectSysDeployFormByDeployId(procDef.getDeploymentId());
            if (Objects.nonNull(sysForm)) {
                procDef.setFormName(sysForm.getFormName());
                procDef.setFormId(sysForm.getFormId());
            }
        }
        page.setTotal(new PageInfo(dataList).getTotal());
        page.setRecords(dataList);
        return page;
    }


    /**
     * 导入流程文件
     * <p>
     * 当每个key的流程第一次部署时，指定版本为1。对其后所有使用相同key的流程定义，
     * 部署时版本会在该key当前已部署的最高版本号基础上加1。key参数用于区分流程定义
     *
     * @param name
     * @param category
     * @param in
     */
    @Override
    @Transactional
    public void importFile(String name,String key, String category, InputStream in) {

        String lastDeployId = null;
        if (StrUtil.isNotBlank(key)){
            lastDeployId = flowDeployMapper.selectDeployIdByDeployKey(key);
        }

        Deployment deploy = repositoryService.createDeployment().addInputStream(name + BPMN_FILE_SUFFIX, in).name(name).category(category).deploy();
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();
        repositoryService.setProcessDefinitionCategory(definition.getId(), category);

        if (StrUtil.isBlank(key)){
            return;
        }

        // 更新流程实例关联表单
        if (StrUtil.isBlank(lastDeployId)) {
            return;
        }


        SysForm sysForm = sysDeployFormService.selectSysDeployFormByDeployId(lastDeployId);
        if (Objects.isNull(sysForm)) {
            return;
        }

        SysDeployForm sysDeployForm = new SysDeployForm();
        sysDeployForm.setDeployId(deploy.getId());
        sysDeployForm.setFormId(sysForm.getFormId());
        sysDeployFormService.insertSysDeployForm(sysDeployForm);


    }

    /**
     * 读取xml
     *
     * @param deployId
     * @return
     */
    @Override
    public AjaxResult readXml(String deployId) throws IOException {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
        InputStream inputStream = repositoryService.getResourceAsStream(definition.getDeploymentId(), definition.getResourceName());
        String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());

        // 3. 获取BPMN模型对象
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definition.getId());

// 4. 获取主流程中的所有节点
        Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();

// 5. 遍历节点，打印信息或进行其他操作
        for (FlowElement flowElement : flowElements) {
            System.out.println("节点ID: " + flowElement.getId());
            System.out.println("节点名称: " + flowElement.getName());
            System.out.println("节点类型: " + flowElement.getClass().getSimpleName()); // 如 UserTask, ServiceTask, ExclusiveGateway
            // 可以根据具体类型做更详细处理，例如：
            if (flowElement instanceof UserTask) {
                UserTask userTask = (UserTask) flowElement;
                System.out.println("处理人/组: " + userTask.getAssignee() + " / " + userTask.getCandidateGroups());
            }
            System.out.println("---------------------");
        }
        return AjaxResult.success("", result);
    }

    /**
     * 读取xml
     *
     * @param deployId
     * @return
     */
    @Override
    public InputStream readImage(String deployId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
        //获得图片流
        DefaultProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        //输出为图片
        return diagramGenerator.generateDiagram(
                bpmnModel,
                "png",
                Collections.emptyList(),
                Collections.emptyList(),
                "宋体",
                "宋体",
                "宋体",
                null,
                1.0,
                false);

    }

    @Override
    public List<FlowNodeDTO> getFlowNodeList() {

        List<FlowProcDefDto> dataList = flowDeployMapper.selectDeployList(null);

        List<FlowNodeDTO> nodeList = new ArrayList<>();
        for (FlowProcDefDto procDef : dataList) {

            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(procDef.getDeploymentId()).singleResult();

            FlowNodeDTO  flowNodeDTO = new FlowNodeDTO();
            flowNodeDTO.setId(procDef.getId());
            flowNodeDTO.setName(procDef.getName());
            // 3. 获取BPMN模型对象
            BpmnModel bpmnModel = repositoryService.getBpmnModel(definition.getId());
            // 4. 获取主流程中的所有节点
            Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();

            List<NodeInfoDto> nodeInfoDtoList = new ArrayList<>();

            flowElements.stream().filter(flowElement -> flowElement instanceof UserTask).forEach(flowElement -> {
                NodeInfoDto nodeInfoDto = new NodeInfoDto();
                nodeInfoDto.setId(flowElement.getId());
                nodeInfoDto.setName(flowElement.getName());
                nodeInfoDtoList.add(nodeInfoDto);
            });
            flowNodeDTO.setNodeInfoDtoList(nodeInfoDtoList);
            nodeList.add(flowNodeDTO);
        }
        return nodeList;
    }

    /**
     * 根据流程定义ID启动流程实例
     *
     * @param procDefId 流程模板ID
     * @param variables 流程变量
     * @return
     */
    @Override
    public AjaxResult startProcessInstanceById(String procDefId, Map<String, Object> variables) {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId)
                    .latestVersion().singleResult();
            if (Objects.nonNull(processDefinition) && processDefinition.isSuspended()) {
                return AjaxResult.error("流程已被挂起,请先激活流程");
            }
            // 设置流程发起人Id到流程中
            SysUser sysUser = SecurityUtils.getLoginUser().getUser();
            identityService.setAuthenticatedUserId(sysUser.getUserId().toString());
            variables.put(ProcessConstants.PROCESS_INITIATOR, sysUser.getUserId());

            // 检查是否为暂存模式
            boolean isDraft = Boolean.TRUE.equals(variables.get("isDraft")) || "true".equals(String.valueOf(variables.get("isDraft")));
            if (isDraft) {
                // 暂存模式：设置状态为暂存
                variables.put("status", "draft");
            }

            // 注意：第一个节点的表单数据不应该在流程启动时保存
            // 因为第一个节点会自动完成，会在完成时由前端传递 nodeFormDataMap 并保存
            // 这样可以避免重复保存的问题

            // 流程发起时 跳过发起人节点
            ProcessInstance processInstance = runtimeService.startProcessInstanceById(procDefId, variables);
            // 给第一步申请人节点设置任务执行人和意见
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
            if (Objects.nonNull(task)) {
                taskService.setAssignee(task.getId(), sysUser.getUserId().toString());
                if (isDraft) {
                    // 暂存模式：不完成第一个任务，只添加注释
                    taskService.addComment(task.getId(), processInstance.getProcessInstanceId(), FlowComment.NORMAL.getType(), sysUser.getNickName() + "暂存流程");
                } else {
                    // 正常提交：完成第一个任务
                    taskService.addComment(task.getId(), processInstance.getProcessInstanceId(), FlowComment.NORMAL.getType(), sysUser.getNickName() + "发起流程申请");
                    taskService.complete(task.getId(), variables);
                }
            }

            ExamTaskDept examTaskDept = new ExamTaskDept();
            examTaskDept.setTaskId(processInstance.getProcessInstanceId());
            if(Objects.nonNull(variables.get("deptId"))) {
                examTaskDept.setDeptId(Long.parseLong(variables.get("deptId").toString()));
            }
            examTaskDeptService.save(examTaskDept);
            // 对于特定流程，如果流程变量中有 approval，需要设置下一节点的审批人
            // 暂存模式(isDraft=true)时不做指派，待正式提交再指派
            if (!isDraft) {
                String processDefinitionKey = processDefinition.getKey();
                List<String> forceAssignProcessKeys = Arrays.asList("flow_lf1rqhxe", "flow_93qezqks");
                if (forceAssignProcessKeys.contains(processDefinitionKey) && variables.containsKey(ProcessConstants.PROCESS_APPROVAL)) {
                    Object approvalValue = variables.get(ProcessConstants.PROCESS_APPROVAL);
                    if (Objects.nonNull(approvalValue)) {
                        String approvalStr = approvalValue.toString();
                        if (StrUtil.isNotBlank(approvalStr)) {
                            String[] userIds = approvalStr.split(",");

                            // 并行网关场景：下一个节点会产生多个用户任务
                            List<Task> nextTasks = taskService.createTaskQuery()
                                    .processInstanceId(processInstance.getProcessInstanceId())
                                    .list();

                            if (nextTasks != null && !nextTasks.isEmpty()) {
                                // 去掉首尾空格并过滤空字符串
                                List<String> cleanedUserIds = new ArrayList<>();
                                for (String uid : userIds) {
                                    if (StrUtil.isNotBlank(uid)) {
                                        cleanedUserIds.add(uid.trim());
                                    }
                                }

                                // 如果审批人人数与并行任务数一致，则一一对应指派
                                if (cleanedUserIds.size() == nextTasks.size()) {
                                    for (int i = 0; i < nextTasks.size(); i++) {
                                        String assigneeId = cleanedUserIds.get(i);
                                        taskService.setAssignee(nextTasks.get(i).getId(), assigneeId);
                                    }
                                } else {
                                    // 否则，将所有审批人作为每个任务的候选人
                                    for (Task nextTask : nextTasks) {
                                        for (String uid : cleanedUserIds) {
                                            taskService.addCandidateUser(nextTask.getId(), uid);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 任务-教研室关联记录
            ExamTaskTeachingResearchOffice examTaskTro = new ExamTaskTeachingResearchOffice();
            examTaskTro.setTaskId(processInstance.getProcessInstanceId());
            if (Objects.nonNull(variables.get("officeId"))) {
                examTaskTro.setTeachingResearchOfficeId(Long.parseLong(variables.get("officeId").toString()));
            }
            if (Objects.nonNull(variables.get("officeName"))) {
                examTaskTro.setTeachingResearchOfficeName(variables.get("officeName").toString());
            }
            examTaskTeachingResearchOfficeService.save(examTaskTro);
            return AjaxResult.success("流程启动成功");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("流程启动错误");
        }
    }


    /**
     * 激活或挂起流程定义
     *
     * @param state    状态
     * @param deployId 流程部署ID
     */
    @Override
    public void updateState(Integer state, String deployId) {
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
        // 激活
        if (state == 1) {
            repositoryService.activateProcessDefinitionById(procDef.getId(), true, null);
        }
        // 挂起
        if (state == 2) {
            repositoryService.suspendProcessDefinitionById(procDef.getId(), true, null);
        }
    }


    /**
     * 删除流程定义
     *
     * @param deployId 流程部署ID act_ge_bytearray 表中 deployment_id值
     */
    @Override
    public void delete(String deployId) {
        // true 允许级联删除 ,不设置会导致数据库外键关联异常
        repositoryService.deleteDeployment(deployId, true);
    }


}
