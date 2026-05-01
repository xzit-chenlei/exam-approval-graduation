package edu.xzit.core.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.core.common.core.domain.entity.SysUser;
import edu.xzit.core.dao.domain.ExamTeachingResearchOffice;
import edu.xzit.core.dao.mapper.ExamTeachingResearchOfficeMapper;
import edu.xzit.core.dao.mapper.ExamTeachingResearchOfficeUserMapper;
import edu.xzit.core.dao.service.IExamTeachingResearchOfficeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamTeachingResearchOfficeServiceImpl extends ServiceImpl<ExamTeachingResearchOfficeMapper, ExamTeachingResearchOffice>
        implements IExamTeachingResearchOfficeService {

    @Autowired
    private ExamTeachingResearchOfficeMapper officeMapper;
    @Autowired
    private ExamTeachingResearchOfficeUserMapper officeUserMapper;

    @Override
    public List<ExamTeachingResearchOffice> selectExamTeachingResearchOfficeList(ExamTeachingResearchOffice query) {
        LambdaQueryWrapper<ExamTeachingResearchOffice> wrapper = new LambdaQueryWrapper<>();
        if (query != null && query.getName() != null) {
            wrapper.like(ExamTeachingResearchOffice::getName, query.getName());
        }
        return list(wrapper);
    }

    @Override
    public int insertExamTeachingResearchOffice(ExamTeachingResearchOffice data) {
        return save(data) ? 1 : 0;
    }

    @Override
    public int updateExamTeachingResearchOffice(ExamTeachingResearchOffice data) {
        return updateById(data) ? 1 : 0;
    }

    @Override
    public int deleteExamTeachingResearchOfficeById(Long id) {
        return removeById(id) ? 1 : 0;
    }

    @Override
    public int deleteExamTeachingResearchOfficeByIds(Long[] ids) {
        if (ids == null || ids.length == 0) return 0;
        return removeByIds(java.util.Arrays.asList(ids)) ? 1 : 0;
    }

    @Override
    public List<SysUser> selectUsersByOfficeId(Long officeId) {
        return officeUserMapper.selectUsersByOfficeId(officeId);
    }

    @Override
    public int addUsers(Long officeId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return 0;
        return officeUserMapper.insertUsers(officeId, userIds);
    }

    @Override
    public int removeUsers(Long officeId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return 0;
        return officeUserMapper.deleteUsers(officeId, userIds);
    }

    @Override
    public List<ExamTeachingResearchOffice> selectByUserId(Long userId) {
        return officeMapper.selectByUserId(userId);
    }
}
