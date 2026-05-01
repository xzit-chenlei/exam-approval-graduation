package edu.xzit.graduate.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.graduate.dao.domain.GraduateQuestionObjective;
import edu.xzit.graduate.dao.mapper.GraduateQuestionObjectiveMapper;
import edu.xzit.graduate.dao.service.IGraduateQuestionObjectiveRepoService;
import org.springframework.stereotype.Service;

/**
 * 毕业达成度题目与课程目标关联 Service 实现类
 *
 * @author system
 * @date 2025-01-22
 */
@Service
public class GraduateQuestionObjectiveRepoServiceImpl extends ServiceImpl<GraduateQuestionObjectiveMapper, GraduateQuestionObjective>
        implements IGraduateQuestionObjectiveRepoService {
}