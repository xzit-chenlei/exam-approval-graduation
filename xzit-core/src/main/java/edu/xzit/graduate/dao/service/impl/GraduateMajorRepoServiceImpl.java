package edu.xzit.graduate.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.graduate.dao.domain.GraduateMajor;
import edu.xzit.graduate.dao.mapper.GraduateMajorMapper;
import edu.xzit.graduate.dao.service.IGraduateMajorRepoService;
import org.springframework.stereotype.Service;

@Service
public class GraduateMajorRepoServiceImpl extends ServiceImpl<GraduateMajorMapper, GraduateMajor>
        implements IGraduateMajorRepoService {

}

