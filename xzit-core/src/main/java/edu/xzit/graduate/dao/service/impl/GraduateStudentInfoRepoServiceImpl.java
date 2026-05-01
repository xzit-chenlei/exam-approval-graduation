package edu.xzit.graduate.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.graduate.dao.domain.GraduateStudentInfo;
import edu.xzit.graduate.dao.mapper.GraduateStudentInfoMapper;
import edu.xzit.graduate.dao.service.IGraduateStudentInfoRepoService;
import org.springframework.stereotype.Service;

/**
 * 学生信息表 Service 实现类
 *
 * @author chenlei
 * @date 2025-01-27
 */
@Service
public class GraduateStudentInfoRepoServiceImpl extends ServiceImpl<GraduateStudentInfoMapper, GraduateStudentInfo>
        implements IGraduateStudentInfoRepoService {

}

