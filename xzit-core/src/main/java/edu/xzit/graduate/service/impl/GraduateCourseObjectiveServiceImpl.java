package edu.xzit.graduate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import edu.xzit.graduate.dao.domain.GraduateCourseObjective;
import edu.xzit.graduate.dao.domain.GraduateAbility;
import edu.xzit.graduate.dao.domain.GraduateCourse;
import edu.xzit.graduate.dao.domain.GraduateMajor;
import edu.xzit.graduate.dao.service.IGraduateCourseObjectiveRepoService;
import edu.xzit.graduate.dao.service.IGraduateAbilityRepoService;
import edu.xzit.graduate.dao.service.IGraduateCourseRepoService;
import edu.xzit.graduate.dao.service.IGraduateMajorRepoService;
import edu.xzit.graduate.service.IGraduateCourseObjectiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程目标 业务服务实现
 *
 * @author chenlei
 * @date 2025-08-25
 */
@Service
public class GraduateCourseObjectiveServiceImpl implements IGraduateCourseObjectiveService {
    @Autowired
    private IGraduateCourseObjectiveRepoService courseObjectiveRepoService;
    
    @Autowired
    private IGraduateAbilityRepoService graduateAbilityRepoService;
    
    @Autowired
    private IGraduateCourseRepoService graduateCourseRepoService;
    
    @Autowired
    private IGraduateMajorRepoService graduateMajorRepoService;

    /**
     * 查询课程目标列表
     *
     * @param courseObjective 课程目标
     * @return 课程目标
     */
    @Override
    public List<GraduateCourseObjective> selectCourseObjectiveList(GraduateCourseObjective courseObjective) {
        LambdaQueryWrapper<GraduateCourseObjective> wrapper = Wrappers.lambdaQuery();
        if (courseObjective.getCourseId() != null) {
            wrapper.eq(GraduateCourseObjective::getCourseId, courseObjective.getCourseId());
        }
        if (courseObjective.getAbilityId() != null) {
            wrapper.eq(GraduateCourseObjective::getAbilityId, courseObjective.getAbilityId());
        }
        // 能力名称搜索：支持搜索父级能力名称和子级能力名称
        if (courseObjective.getAbilityName() != null && !courseObjective.getAbilityName().isEmpty()) {
            String searchKeyword = courseObjective.getAbilityName();
            
            // 查找所有匹配的能力ID（包括父级和子级）
            LambdaQueryWrapper<GraduateAbility> abilitySearchWrapper = Wrappers.lambdaQuery();
            abilitySearchWrapper.like(GraduateAbility::getName, searchKeyword);
            List<GraduateAbility> matchedAbilities = graduateAbilityRepoService.list(abilitySearchWrapper);
            
            if (!matchedAbilities.isEmpty()) {
                List<Long> targetAbilityIds = new java.util.ArrayList<>();
                
                for (GraduateAbility ability : matchedAbilities) {
                    if (ability.getParentId() == null) {
                        // 如果匹配的是父级能力，需要找到其所有子级能力
                        LambdaQueryWrapper<GraduateAbility> childWrapper = Wrappers.lambdaQuery();
                        childWrapper.eq(GraduateAbility::getParentId, ability.getId());
                        List<GraduateAbility> children = graduateAbilityRepoService.list(childWrapper);
                        for (GraduateAbility child : children) {
                            targetAbilityIds.add(child.getId());
                        }
                    } else {
                        // 如果匹配的是子级能力，直接添加
                        targetAbilityIds.add(ability.getId());
                    }
                }
                
                if (!targetAbilityIds.isEmpty()) {
                    wrapper.in(GraduateCourseObjective::getAbilityId, targetAbilityIds);
                } else {
                    // 如果没有找到任何匹配的子级能力，返回空结果
                    wrapper.eq(GraduateCourseObjective::getId, -1);
                }
            } else {
                // 如果没有找到任何匹配的能力，返回空结果
                wrapper.eq(GraduateCourseObjective::getId, -1);
            }
        }
        if (courseObjective.getObjective() != null && !courseObjective.getObjective().isEmpty()) {
            wrapper.like(GraduateCourseObjective::getObjective, courseObjective.getObjective());
        }
        
        // 如果指定了专业ID，需要先查询该专业下的所有课程ID，然后过滤
        if (courseObjective.getMajorId() != null) {
            LambdaQueryWrapper<GraduateCourse> courseWrapper = Wrappers.lambdaQuery();
            courseWrapper.eq(GraduateCourse::getMajorId, courseObjective.getMajorId());
            List<GraduateCourse> courses = graduateCourseRepoService.list(courseWrapper);
            
            if (courses.isEmpty()) {
                // 如果该专业下没有课程，返回空列表
                return new java.util.ArrayList<>();
            }
            
            List<Long> courseIds = courses.stream()
                    .map(GraduateCourse::getId)
                    .collect(java.util.stream.Collectors.toList());
            wrapper.in(GraduateCourseObjective::getCourseId, courseIds);
        }
        
        List<GraduateCourseObjective> list = courseObjectiveRepoService.list(wrapper);
        
        // 格式化能力名称显示为"父级-子级"格式
        formatAbilityNames(list);
        
        // 填充专业名称
        fillMajorNames(list);
        
        return list;
    }
    
    /**
     * 格式化能力名称为"父级-子级"格式
     */
    private void formatAbilityNames(List<GraduateCourseObjective> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        
        // 获取所有涉及的能力ID
        List<Long> abilityIds = list.stream()
                .map(GraduateCourseObjective::getAbilityId)
                .filter(id -> id != null)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        
        if (abilityIds.isEmpty()) {
            return;
        }
        
        // 查询所有相关的能力信息
        LambdaQueryWrapper<GraduateAbility> abilityWrapper = Wrappers.lambdaQuery();
        abilityWrapper.in(GraduateAbility::getId, abilityIds);
        List<GraduateAbility> abilities = graduateAbilityRepoService.list(abilityWrapper);
        
        // 获取所有父级能力ID
        List<Long> parentIds = abilities.stream()
                .map(GraduateAbility::getParentId)
                .filter(id -> id != null)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        
        // 查询父级能力信息
        java.util.Map<Long, String> parentNameMap = new java.util.HashMap<>();
        if (!parentIds.isEmpty()) {
            LambdaQueryWrapper<GraduateAbility> parentWrapper = Wrappers.lambdaQuery();
            parentWrapper.in(GraduateAbility::getId, parentIds);
            List<GraduateAbility> parentAbilities = graduateAbilityRepoService.list(parentWrapper);
            parentNameMap = parentAbilities.stream()
                    .collect(java.util.stream.Collectors.toMap(
                        GraduateAbility::getId, 
                        GraduateAbility::getName
                    ));
        }
        
        // 创建能力信息映射
        java.util.Map<Long, GraduateAbility> abilityMap = abilities.stream()
                .collect(java.util.stream.Collectors.toMap(
                    GraduateAbility::getId, 
                    ability -> ability
                ));
        
        // 格式化每个课程目标的能力名称
        for (GraduateCourseObjective obj : list) {
            if (obj.getAbilityId() != null) {
                GraduateAbility ability = abilityMap.get(obj.getAbilityId());
                if (ability != null && ability.getParentId() != null) {
                    String parentName = parentNameMap.get(ability.getParentId());
                    if (parentName != null) {
                        obj.setAbilityName(parentName + "-" + ability.getName());
                    }
                }
            }
        }
    }
    
    /**
     * 填充课程名称和专业名称
     */
    private void fillMajorNames(List<GraduateCourseObjective> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        
        // 获取所有涉及的课程ID
        List<Long> courseIds = list.stream()
                .map(GraduateCourseObjective::getCourseId)
                .filter(id -> id != null)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        
        if (courseIds.isEmpty()) {
            return;
        }
        
        // 查询所有相关的课程信息
        LambdaQueryWrapper<GraduateCourse> courseWrapper = Wrappers.lambdaQuery();
        courseWrapper.in(GraduateCourse::getId, courseIds);
        List<GraduateCourse> courses = graduateCourseRepoService.list(courseWrapper);
        
        // 创建课程ID到课程名称的映射
        java.util.Map<Long, String> courseNameMap = courses.stream()
                .collect(java.util.stream.Collectors.toMap(
                    GraduateCourse::getId, 
                    GraduateCourse::getName
                ));
        
        // 获取所有专业ID
        List<Long> majorIds = courses.stream()
                .map(GraduateCourse::getMajorId)
                .filter(id -> id != null)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        
        // 查询专业信息
        java.util.Map<Long, String> majorNameMap = new java.util.HashMap<>();
        if (!majorIds.isEmpty()) {
            LambdaQueryWrapper<GraduateMajor> majorWrapper = Wrappers.lambdaQuery();
            majorWrapper.in(GraduateMajor::getId, majorIds);
            List<GraduateMajor> majors = graduateMajorRepoService.list(majorWrapper);
            majorNameMap = majors.stream()
                    .collect(java.util.stream.Collectors.toMap(
                        GraduateMajor::getId, 
                        GraduateMajor::getName
                    ));
        }
        
        // 创建课程到专业ID的映射
        java.util.Map<Long, Long> courseMajorMap = courses.stream()
                .collect(java.util.stream.Collectors.toMap(
                    GraduateCourse::getId, 
                    GraduateCourse::getMajorId
                ));
        
        // 填充每个课程目标的课程名称和专业名称
        for (GraduateCourseObjective obj : list) {
            if (obj.getCourseId() != null) {
                // 填充课程名称
                String courseName = courseNameMap.get(obj.getCourseId());
                if (courseName != null) {
                    obj.setCourseName(courseName);
                }
                
                // 填充专业名称
                Long majorId = courseMajorMap.get(obj.getCourseId());
                if (majorId != null) {
                    String majorName = majorNameMap.get(majorId);
                    if (majorName != null) {
                        obj.setMajorName(majorName);
                    }
                }
            }
        }
    }

    /**
     * 查询课程目标详细
     *
     * @param id 课程目标主键
     * @return 课程目标
     */
    @Override
    public GraduateCourseObjective selectCourseObjectiveById(Long id) {
        GraduateCourseObjective courseObjective = courseObjectiveRepoService.getById(id);
        if (courseObjective != null) {
            if (courseObjective.getAbilityId() != null) {
                // 获取原始的能力名称（用于编辑表单）
                GraduateAbility ability = graduateAbilityRepoService.getById(courseObjective.getAbilityId());
                if (ability != null) {
                    courseObjective.setAbilityName(ability.getName());
                }
            }
            
            if (courseObjective.getCourseId() != null) {
                // 获取课程名称
                GraduateCourse course = graduateCourseRepoService.getById(courseObjective.getCourseId());
                if (course != null) {
                    courseObjective.setCourseName(course.getName());
                }
            }
        }
        return courseObjective;
    }

    /**
     * 新增课程目标
     *
     * @param courseObjective 课程目标
     * @return 结果
     */
    @Override
    public int insertCourseObjective(GraduateCourseObjective courseObjective) {
        // 根据能力名称查找能力ID
        if (courseObjective.getAbilityName() != null && !courseObjective.getAbilityName().isEmpty()) {
            LambdaQueryWrapper<GraduateAbility> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(GraduateAbility::getName, courseObjective.getAbilityName());
            wrapper.last("LIMIT 1"); // 限制只返回一个结果，避免TooManyResultsException
            List<GraduateAbility> abilities = graduateAbilityRepoService.list(wrapper);
            if (!abilities.isEmpty()) {
                GraduateAbility ability = abilities.get(0);
                courseObjective.setAbilityId(ability.getId());
            } else {
                // 如果找不到对应的能力，返回错误
                throw new RuntimeException("未找到对应的能力：" + courseObjective.getAbilityName());
            }
        }
        
        return courseObjectiveRepoService.save(courseObjective) ? 1 : 0;
    }

    /**
     * 修改课程目标
     *
     * @param courseObjective 课程目标
     * @return 结果
     */
    @Override
    public int updateCourseObjective(GraduateCourseObjective courseObjective) {
        // 根据能力名称查找能力ID
        if (courseObjective.getAbilityName() != null && !courseObjective.getAbilityName().isEmpty()) {
            LambdaQueryWrapper<GraduateAbility> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(GraduateAbility::getName, courseObjective.getAbilityName());
            wrapper.last("LIMIT 1"); // 限制只返回一个结果，避免TooManyResultsException
            List<GraduateAbility> abilities = graduateAbilityRepoService.list(wrapper);
            if (!abilities.isEmpty()) {
                GraduateAbility ability = abilities.get(0);
                courseObjective.setAbilityId(ability.getId());
            } else {
                // 如果找不到对应的能力，返回错误
                throw new RuntimeException("未找到对应的能力：" + courseObjective.getAbilityName());
            }
        }
        
        return courseObjectiveRepoService.updateById(courseObjective) ? 1 : 0;
    }

    /**
     * 批量删除课程目标
     *
     * @param ids 需要删除的课程目标主键集合
     * @return 结果
     */
    @Override
    public int deleteCourseObjectiveByIds(Long[] ids) {
        return courseObjectiveRepoService.removeByIds(java.util.Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 删除课程目标信息
     *
     * @param id 课程目标主键
     * @return 结果
     */
    @Override
    public int deleteCourseObjectiveById(Long id) {
                return courseObjectiveRepoService.removeById(id) ? 1 : 0;
     }

} 