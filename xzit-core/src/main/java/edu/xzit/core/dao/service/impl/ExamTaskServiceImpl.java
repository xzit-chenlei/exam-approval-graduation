package edu.xzit.core.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.dao.domain.ExamTaskDept;
import edu.xzit.core.dao.mapper.ExamTaskDeptMapper;
import edu.xzit.core.dao.service.IExamTaskDeptService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class ExamTaskServiceImpl extends ServiceImpl<ExamTaskDeptMapper, ExamTaskDept> implements IExamTaskDeptService {

    @Override
    public List<String> selectTaskIdsByDeptId(List<Long> deptIds) {

        if (!CollectionUtils.isEmpty(deptIds)){
            LambdaQueryWrapper<ExamTaskDept> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ExamTaskDept::getDeptId,deptIds);
            return list(queryWrapper).stream().map(ExamTaskDept::getTaskId).collect(Collectors.toList());

        }
        return Collections.emptyList();
    }

    @Override
    public Long selectDeptIdByTaskId(String taskId) {

        if (taskId == null) {
            return null; // 或者 throw new IllegalArgumentException("taskId不能为空");
        }

        LambdaQueryWrapper<ExamTaskDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ExamTaskDept::getTaskId, taskId);
        queryWrapper.last("LIMIT 1"); // 明确限制只查一条

        List<ExamTaskDept> result = list(queryWrapper);

        return result.isEmpty() ? null : result.get(0).getDeptId();
    }


}
