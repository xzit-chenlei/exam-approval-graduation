package edu.xzit.graduate.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.graduate.dao.domain.GraduateAbility;
import edu.xzit.graduate.dao.mapper.GraduateAbilityMapper;
import edu.xzit.graduate.dao.service.IGraduateAbilityRepoService;
import org.springframework.stereotype.Service;

@Service
public class GraduateAbilityRepoServiceImpl extends ServiceImpl<GraduateAbilityMapper, GraduateAbility>
        implements IGraduateAbilityRepoService {
}

