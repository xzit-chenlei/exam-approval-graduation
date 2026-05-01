package edu.xzit.core.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.dao.domain.SysUserDept;
import edu.xzit.core.dao.mapper.SysUserDeptMapper;
import edu.xzit.core.dao.service.ISysUserDeptService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SysUserDeptService extends ServiceImpl<SysUserDeptMapper, SysUserDept>  implements ISysUserDeptService {


    @Override
    public List<Long> selectUserIdsByDeptId(List<Long> deptIds) {
        if (Objects.nonNull(deptIds)){
            LambdaQueryWrapper<SysUserDept> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SysUserDept::getDeptId,deptIds);
            return list(queryWrapper).stream().map(SysUserDept::getUserId).collect(Collectors.toList());

        }
        return null;
    }

    @Override
    public List<Long> selectDeptIdsByUserId(List<Long> userId) {
        if(Objects.nonNull(userId)){
            LambdaQueryWrapper<SysUserDept> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SysUserDept::getUserId,userId);
            return list(queryWrapper).stream().map(SysUserDept::getDeptId).collect(Collectors.toList());
        }
        return null;
    }
}
