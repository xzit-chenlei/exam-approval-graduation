package edu.xzit.graduate.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.graduate.dao.domain.GraduateCourseAbility;
import edu.xzit.graduate.dao.mapper.GraduateCourseAbilityMapper;
import edu.xzit.graduate.dao.service.IGraduateCourseAbilityRepoService;
import org.springframework.stereotype.Service;

@Service
public class GraduateCourseAbilityRepoServiceImpl extends ServiceImpl<GraduateCourseAbilityMapper, GraduateCourseAbility>
        implements IGraduateCourseAbilityRepoService {
}

