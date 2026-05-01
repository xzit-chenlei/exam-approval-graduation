package edu.xzit.graduate.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.graduate.dao.domain.GraduateCourse;
import edu.xzit.graduate.dao.mapper.GraduateCourseMapper;
import edu.xzit.graduate.dao.service.IGraduateCourseRepoService;
import org.springframework.stereotype.Service;

@Service
public class GraduateCourseRepoServiceImpl extends ServiceImpl<GraduateCourseMapper, GraduateCourse>
        implements IGraduateCourseRepoService {
}

