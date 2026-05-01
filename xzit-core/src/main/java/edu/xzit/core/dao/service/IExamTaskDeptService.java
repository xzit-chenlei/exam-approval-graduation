package edu.xzit.core.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.core.dao.domain.ExamTaskDept;

import java.util.List;

public interface IExamTaskDeptService extends IService<ExamTaskDept> {

    public List<String> selectTaskIdsByDeptId(List<Long> deptIds);

    public Long selectDeptIdByTaskId(String taskId);


}
