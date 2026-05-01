package edu.xzit.core.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.xzit.core.dao.domain.ExamTeachingResearchOfficeUser;
import edu.xzit.core.core.common.core.domain.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExamTeachingResearchOfficeUserMapper extends BaseMapper<ExamTeachingResearchOfficeUser> {
    List<SysUser> selectUsersByOfficeId(@Param("officeId") Long officeId);
    int insertUsers(@Param("officeId") Long officeId, @Param("userIds") List<Long> userIds);
    int deleteUsers(@Param("officeId") Long officeId, @Param("userIds") List<Long> userIds);
    List<Long> selectOfficeIdsByUserId(@Param("userId") Long userId);
}
