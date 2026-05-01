package edu.xzit.core.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.core.dao.domain.SysUserDept;

import java.util.List;

public interface ISysUserDeptService extends IService<SysUserDept> {

    public List<Long> selectUserIdsByDeptId(List<Long> deptIds);

    public List<Long> selectDeptIdsByUserId(List<Long> userId);
}
