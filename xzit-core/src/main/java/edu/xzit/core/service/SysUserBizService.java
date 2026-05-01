package edu.xzit.core.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.xzit.core.core.common.core.domain.entity.SysUser;
import edu.xzit.core.dao.service.ISysUserDeptService;
import edu.xzit.core.dao.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class SysUserBizService {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysUserDeptService sysUserDeptServcie;

    public List<SysUser> queryUserList(SysUser sysUser) {
        if (sysUser == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        // 用户账号模糊查询
        if (sysUser.getUserName() != null && !sysUser.getUserName().trim().isEmpty()) {
            queryWrapper.like(SysUser::getUserName, sysUser.getUserName().trim());
        }
        // 用户姓名模糊查询
        if (sysUser.getNickName() != null && !sysUser.getNickName().trim().isEmpty()) {
            queryWrapper.like(SysUser::getNickName, sysUser.getNickName().trim());
        }

        // 其它非空字段仍按等值查询（状态、手机号等）
        if (sysUser.getPhonenumber() != null && !sysUser.getPhonenumber().trim().isEmpty()) {
            queryWrapper.like(SysUser::getPhonenumber, sysUser.getPhonenumber().trim());
        }
        if (sysUser.getStatus() != null && !sysUser.getStatus().trim().isEmpty()) {
            queryWrapper.eq(SysUser::getStatus, sysUser.getStatus().trim());
        }

        Optional<List<Long>> userIdsOpt = Optional.ofNullable(sysUser.getDeptIds())
                .filter(ids -> ids.length > 0)
                .map(Arrays::asList)
                .map(sysUserDeptServcie::selectUserIdsByDeptId)
                .filter(ids -> !ids.isEmpty());

        if (userIdsOpt.isPresent()) {
            queryWrapper.in(SysUser::getUserId, userIdsOpt.get());
        } else {
            // If deptIds was provided but resulted in empty userIds, return empty list
            if (sysUser.getDeptIds() != null && sysUser.getDeptIds().length > 0) {
                return Collections.emptyList();
            }
        }

        return sysUserService.list(queryWrapper);
    }

    /**
     * 查询用户列表（排除管理员ID=1）
     */
    public List<SysUser> queryUserListExcludeAdmin(SysUser sysUser) {
        if (sysUser == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        // 用户账号模糊查询
        if (sysUser.getUserName() != null && !sysUser.getUserName().trim().isEmpty()) {
            queryWrapper.like(SysUser::getUserName, sysUser.getUserName().trim());
        }
        // 用户姓名模糊查询
        if (sysUser.getNickName() != null && !sysUser.getNickName().trim().isEmpty()) {
            queryWrapper.like(SysUser::getNickName, sysUser.getNickName().trim());
        }
        // 其它非空字段仍按等值查询（状态、手机号等）
        if (sysUser.getPhonenumber() != null && !sysUser.getPhonenumber().trim().isEmpty()) {
            queryWrapper.like(SysUser::getPhonenumber, sysUser.getPhonenumber().trim());
        }
        if (sysUser.getStatus() != null && !sysUser.getStatus().trim().isEmpty()) {
            queryWrapper.eq(SysUser::getStatus, sysUser.getStatus().trim());
        }

        Optional<List<Long>> userIdsOpt = Optional.ofNullable(sysUser.getDeptIds())
                .filter(ids -> ids.length > 0)
                .map(Arrays::asList)
                .map(sysUserDeptServcie::selectUserIdsByDeptId)
                .filter(ids -> !ids.isEmpty());

        if (userIdsOpt.isPresent()) {
            queryWrapper.in(SysUser::getUserId, userIdsOpt.get());
        } else {
            if (sysUser.getDeptIds() != null && sysUser.getDeptIds().length > 0) {
                return Collections.emptyList();
            }
        }

        // 排除管理员ID=1
        queryWrapper.ne(SysUser::getUserId, 1L);

        return sysUserService.list(queryWrapper);
    }


}
