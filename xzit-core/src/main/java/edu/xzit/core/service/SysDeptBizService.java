package edu.xzit.core.service;

import edu.xzit.core.core.common.core.domain.TreeSelect;
import edu.xzit.core.core.common.core.domain.entity.SysDept;
import edu.xzit.core.dao.service.ISysDeptService;
import edu.xzit.core.dao.service.ISysUserDeptService;
import edu.xzit.core.model.dto.DeptDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class SysDeptBizService {

    @Autowired
    private ISysDeptService sysDeptService;

    @Autowired
    private ISysUserDeptService sysUserDeptService;

    public List<Long> selectChildDeptIdByUserId(Long userId) {
        List<TreeSelect> allDepts = sysDeptService.selectDeptTreeList(new SysDept());
        List<Long> userIds = new ArrayList<>();
        userIds.add(userId);
        List<Long> deptIds = sysUserDeptService.selectDeptIdsByUserId(userIds);
        List<Long> result = new ArrayList<>();

        for (Long deptId : deptIds) {
            // 1. 在完整部门树中找到对应的部门
            Optional<TreeSelect> deptOpt = findDeptById(allDepts, deptId);
            if (deptOpt.isPresent()) {
                TreeSelect dept = deptOpt.get();
                // 2. 获取该部门下的所有叶子节点
                List<TreeSelect> leafNodes = findLeafNodes(dept);
                // 3. 为每个叶子节点创建DTO并添加到结果集
                for (TreeSelect leaf : leafNodes) {
                    result.add(leaf.getId());
                }
            }
        }
        return result;

    }

    public String getDeptGroup(Long userId) {
        List<TreeSelect> allDepts = sysDeptService.selectDeptTreeList(new SysDept());
        List<Long> userIds = new ArrayList<>();
        userIds.add(userId);
        List<Long> deptIds = sysUserDeptService.selectDeptIdsByUserId(userIds);
        StringBuilder result = new StringBuilder();

        for (Long deptId : deptIds) {
            // 1. 在完整部门树中找到对应的部门
            Optional<TreeSelect> deptOpt = findDeptById(allDepts, deptId);
            if (deptOpt.isPresent()) {
                TreeSelect dept = deptOpt.get();
                // 2. 获取该部门下的所有叶子节点
                List<TreeSelect> leafNodes = findLeafNodes(dept);
                // 3. 为每个叶子节点创建DTO并添加到结果集
                for (TreeSelect leaf : leafNodes) {
                    String fullPath = getFullDeptPath(allDepts, leaf.getId());

                    result.append(",");
                    result.append(fullPath);
                }
            }
        }
        return result.toString();
    }

    public List<DeptDTO> selectDeptListByUserId(Long userId) {
        List<TreeSelect> allDepts = sysDeptService.selectDeptTreeList(new SysDept());
        List<Long> userIds = new ArrayList<>();
        userIds.add(userId);
        List<Long> deptIds = sysUserDeptService.selectDeptIdsByUserId(userIds);
        List<DeptDTO> result = new ArrayList<>();

        for (Long deptId : deptIds) {
            // 1. 在完整部门树中找到对应的部门
            Optional<TreeSelect> deptOpt = findDeptById(allDepts, deptId);
            if (deptOpt.isPresent()) {
                TreeSelect dept = deptOpt.get();
                // 2. 获取该部门下的所有叶子节点
                List<TreeSelect> leafNodes = findLeafNodes(dept);
                // 3. 为每个叶子节点创建DTO并添加到结果集
                for (TreeSelect leaf : leafNodes) {
                    String fullPath = getFullDeptPath(allDepts, leaf.getId());
                    DeptDTO dto = new DeptDTO();
                    dto.setDeptId(leaf.getId());
                    dto.setDeptName(fullPath);
                    result.add(dto);
                }
            }
        }
        return result;
    }

    /**
     * 获取指定节点下的所有叶子节点
     * @param node 起始节点
     * @return 叶子节点列表
     */
    private List<TreeSelect> findLeafNodes(TreeSelect node) {
        List<TreeSelect> leafNodes = new ArrayList<>();
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            // 当前节点就是叶子节点
            leafNodes.add(node);
        } else {
            // 递归查找子节点的叶子节点
            for (TreeSelect child : node.getChildren()) {
                leafNodes.addAll(findLeafNodes(child));
            }
        }
        return leafNodes;
    }

    /**
     * 获取从根节点到指定部门的完整路径
     * @param depts 部门树
     * @param targetDeptId 目标部门ID
     * @return 完整路径字符串，如"总公司/技术部/开发组"
     */
    private String getFullDeptPath(List<TreeSelect> depts, Long targetDeptId) {
        for (TreeSelect dept : depts) {
            if (dept.getId().equals(targetDeptId)) {
                return dept.getLabel(); // 找到目标节点，返回当前节点名称
            }
            // 递归查找子节点
            String path = getFullDeptPath(dept.getChildren(), targetDeptId);
            if (path != null) {
                // 拼接父节点名称
                return dept.getLabel() + "/" + path;
            }
        }
        return null;
    }

    /**
     * 在树形结构中查找指定ID的部门
     * @param depts 部门列表
     * @param deptId 要查找的部门ID
     * @return 找到的部门Optional
     */
    private Optional<TreeSelect> findDeptById(List<TreeSelect> depts, Long deptId) {
        for (TreeSelect dept : depts) {
            if (dept.getId().equals(deptId)) {
                return Optional.of(dept);
            }
            Optional<TreeSelect> found = findDeptById(dept.getChildren(), deptId);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }


}
