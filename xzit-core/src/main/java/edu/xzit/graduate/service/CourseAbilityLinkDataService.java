package edu.xzit.graduate.service;

import java.util.Map;

public interface CourseAbilityLinkDataService {
    
    /**
     * 根据学年、专业、课程获取课程能力链接数据
     * @param year 学年
     * @param major 专业
     * @param course 课程
     * @return 课程能力链接数据
     */
    Map<String, Object> getCourseAbilityLinkData(Integer year, String major, String course);
}
