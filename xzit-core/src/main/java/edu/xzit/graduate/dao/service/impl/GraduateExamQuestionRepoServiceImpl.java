package edu.xzit.graduate.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.graduate.dao.domain.GraduateExamQuestion;
import edu.xzit.graduate.dao.mapper.GraduateExamQuestionMapper;
import edu.xzit.graduate.dao.service.IGraduateExamQuestionRepoService;
import org.springframework.stereotype.Service;

/**
 * 毕业达成度试卷题目 Service 实现类
 *
 * @author system
 * @date 2025-01-22
 */
@Service
public class GraduateExamQuestionRepoServiceImpl extends ServiceImpl<GraduateExamQuestionMapper, GraduateExamQuestion>
        implements IGraduateExamQuestionRepoService {
}