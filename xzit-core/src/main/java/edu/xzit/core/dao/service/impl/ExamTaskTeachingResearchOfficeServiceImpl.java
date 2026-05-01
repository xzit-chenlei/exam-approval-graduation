package edu.xzit.core.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.dao.domain.ExamTaskTeachingResearchOffice;
import edu.xzit.core.dao.mapper.ExamTaskTeachingResearchOfficeMapper;
import edu.xzit.core.dao.service.IExamTaskTeachingResearchOfficeService;
import org.springframework.stereotype.Service;

@Service
public class ExamTaskTeachingResearchOfficeServiceImpl extends ServiceImpl<ExamTaskTeachingResearchOfficeMapper, ExamTaskTeachingResearchOffice>
        implements IExamTaskTeachingResearchOfficeService {

    @Override
    public java.util.List<String> selectTaskIdsByOfficeIds(java.util.List<Long> officeIds) {
        if (officeIds == null || officeIds.isEmpty()) return java.util.Collections.emptyList();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamTaskTeachingResearchOffice> w = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        w.in(ExamTaskTeachingResearchOffice::getTeachingResearchOfficeId, officeIds);
        return list(w).stream().map(ExamTaskTeachingResearchOffice::getTaskId).distinct().collect(java.util.stream.Collectors.toList());
    }
}


