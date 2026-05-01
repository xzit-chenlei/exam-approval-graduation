package edu.xzit.graduate.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.graduate.dao.domain.GraduateExamPaper;
import edu.xzit.graduate.dao.mapper.GraduateExamPaperMapper;
import edu.xzit.graduate.dao.service.IGraduateExamPaperRepoService;
import org.springframework.stereotype.Service;

/**
 * 毕业达成度试卷信息 Service 实现类
 *
 * @author system
 * @date 2025-01-22
 */
@Service
public class GraduateExamPaperRepoServiceImpl extends ServiceImpl<GraduateExamPaperMapper, GraduateExamPaper>
        implements IGraduateExamPaperRepoService {
}