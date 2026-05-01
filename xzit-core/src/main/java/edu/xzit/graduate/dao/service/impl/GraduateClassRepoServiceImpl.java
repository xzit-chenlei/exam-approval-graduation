package edu.xzit.graduate.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.graduate.dao.domain.GraduateClass;
import edu.xzit.graduate.dao.mapper.GraduateClassMapper;
import edu.xzit.graduate.dao.service.IGraduateClassRepoService;
import org.springframework.stereotype.Service;

@Service
public class GraduateClassRepoServiceImpl extends ServiceImpl<GraduateClassMapper, GraduateClass>
        implements IGraduateClassRepoService {
}



