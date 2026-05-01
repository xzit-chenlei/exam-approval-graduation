package edu.xzit.graduate.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.graduate.dao.domain.GraduateStudentScore;
import edu.xzit.graduate.dao.mapper.GraduateStudentScoreMapper;
import edu.xzit.graduate.dao.service.IGraduateStudentScoreRepoService;
import org.springframework.stereotype.Service;

/**
 * 学生成绩表 Service 实现类
 *
 * @author chenlei
 * @date 2025-01-27
 */
@Service
public class GraduateStudentScoreRepoServiceImpl extends ServiceImpl<GraduateStudentScoreMapper, GraduateStudentScore>
        implements IGraduateStudentScoreRepoService {

}

