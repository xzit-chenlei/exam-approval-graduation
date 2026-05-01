package edu.xzit.core.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.xzit.core.dao.domain.SysUserDept;
import edu.xzit.core.dao.domain.SysUserPost;
import edu.xzit.core.dao.domain.SysUserRole;

import java.util.List;

public interface SysUserDeptMapper extends BaseMapper<SysUserDept> {

    /**
     * 通过用户ID删除用户和岗位关联
     *
     * @param userId 用户ID
     * @return 结果
     */
    public int deleteUserDeptByUserId(Long userId);

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param deptId 部门ID
     * @return 结果
     */
    public int countUserDeptById(Long deptId);

    /**
     * 批量删除用户和部门关联
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteUserDept(Long[] ids);

    /**
     * 批量新增用户岗位信息
     *
     * @param userPostList 用户岗位列表
     * @return 结果
     */
    public int batchUserDept(List<SysUserDept> userPostList);
}
