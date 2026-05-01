package edu.xzit.graduate.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import edu.xzit.graduate.dao.domain.GraduateCourse;
import edu.xzit.graduate.dao.domain.GraduateCourseAbility;
import edu.xzit.graduate.dao.domain.GraduateCourseWeight;
import edu.xzit.graduate.dao.domain.GraduateExamPaper;
import edu.xzit.graduate.dao.domain.GraduateExamQuestion;
import edu.xzit.graduate.dao.domain.GraduateMajor;
import edu.xzit.graduate.dao.domain.GraduateStudentScore;
import edu.xzit.graduate.dao.service.IGraduateCourseAbilityRepoService;
import edu.xzit.graduate.dao.service.IGraduateCourseRepoService;
import edu.xzit.graduate.dao.service.IGraduateCourseWeightRepoService;
import edu.xzit.graduate.dao.service.IGraduateExamPaperRepoService;
import edu.xzit.graduate.dao.service.IGraduateExamQuestionRepoService;
import edu.xzit.graduate.dao.service.IGraduateMajorRepoService;
import edu.xzit.graduate.dao.service.IGraduateStudentScoreRepoService;
import edu.xzit.graduate.service.CourseAbilityLinkDataService;

@Slf4j
@Service
public class CourseAbilityLinkDataServiceImpl implements CourseAbilityLinkDataService {
    

    @Autowired
    private IGraduateCourseWeightRepoService courseWeightRepoService;

    @Autowired
    private IGraduateCourseRepoService courseRepoService;

    @Autowired
    private IGraduateMajorRepoService majorRepoService;

    @Autowired
    private IGraduateCourseAbilityRepoService courseAbilityRepoService;

    @Autowired
    private IGraduateExamPaperRepoService examPaperRepoService;

    @Autowired
    private IGraduateExamQuestionRepoService examQuestionRepoService;

    @Autowired
    private IGraduateStudentScoreRepoService studentScoreRepoService;

    @Override
    public Map<String, Object> getCourseAbilityLinkData(Integer year, String major, String course) {
        
        try {
            // 1. 根据参数查询课程信息
            List<GraduateCourse> courses = courseRepoService.list(
                    Wrappers.lambdaQuery(GraduateCourse.class)
                            .eq(year != null, GraduateCourse::getGrade, year)
                            .like(course != null, GraduateCourse::getName, course)
                            .eq(GraduateCourse::getIsValid, 0)
            );

            if (courses.isEmpty()) {
                return new HashMap<>();
            }

            // 如果有专业参数，过滤课程
            if (major != null) {
                GraduateMajor majorEntity = majorRepoService.list(
                        Wrappers.lambdaQuery(GraduateMajor.class)
                                .like(GraduateMajor::getName, major)
                                .eq(GraduateMajor::getIsValid, 0)
                ).stream().findFirst().orElse(null);

                if (majorEntity != null) {
                    courses = courses.stream()
                            .filter(c -> c.getMajorId() != null && c.getMajorId().equals(majorEntity.getId()))
                            .collect(Collectors.toList());
                }
            }

            if (courses.isEmpty()) {
                return new HashMap<>();
            }

            // 2. 获取配置数据（考试类型和权重）
            List<Map<String, Object>> config = new ArrayList<>();

            // 3. 获取数据内容（按课程目标分组）
            List<List<Map<String, Object>>> data = new ArrayList<>();

            for (GraduateCourse currentCourse : courses) {
                // 获取该课程的所有课程目标
                List<GraduateCourseAbility> courseAbilities = courseAbilityRepoService.list(
                        Wrappers.lambdaQuery(GraduateCourseAbility.class)
                                .eq(GraduateCourseAbility::getCourseId, currentCourse.getId())
                                .orderByAsc(GraduateCourseAbility::getId)
                );

                if (courseAbilities.isEmpty()) {
                    continue;
                }

                // 获取课程权重信息
                List<GraduateCourseWeight> courseWeights = courseWeightRepoService.list(
                        Wrappers.lambdaQuery(GraduateCourseWeight.class)
                                .eq(GraduateCourseWeight::getCourseId, currentCourse.getId())
                                .eq(GraduateCourseWeight::getIsValid, 0)
                );
                // 没查到填入空配置
                if (courseWeights.isEmpty()) {
                    // 期末考核配置
                    Map<String, Object> finalConfig = new HashMap<>();
                    finalConfig.put("examType", "期末考核");
                    finalConfig.put("weight", "0"); // 默认权重
                    config.add(finalConfig);

                    // 平时考核配置
                    Map<String, Object> performanceConfig = new HashMap<>();
                    performanceConfig.put("examType", "平时考核");
                    performanceConfig.put("weight", "0"); // 默认权重
                    config.add(performanceConfig);

                    // 实验考核配置
                    Map<String, Object> labConfig = new HashMap<>();
                    labConfig.put("examType", "实验考核");
                    labConfig.put("weight", "0"); // 默认权重
                    config.add(labConfig);
                }

                // 否则按查询到的配置写入配置
                BigDecimal finalW = courseWeights.get(0).getFinalW();
                if (finalW.compareTo(BigDecimal.ZERO) != 0) {
                    Map<String, Object> finalConfig = new HashMap<>();
                    finalConfig.put("examType", "期末考核");
                    finalConfig.put("weight", finalW); // 默认权重
                    config.add(finalConfig);
                }
                BigDecimal perfW = courseWeights.get(0).getPerfW();
                if (perfW.compareTo(BigDecimal.ZERO) != 0) {
                    Map<String, Object> performanceConfig = new HashMap<>();
                    performanceConfig.put("examType", "平时考核");
                    performanceConfig.put("weight", perfW); // 默认权重
                    config.add(performanceConfig);
                }
                BigDecimal labW = courseWeights.get(0).getLabW();
                if (labW.compareTo(BigDecimal.ZERO) != 0) {
                    // 实验考核配置
                    Map<String, Object> labConfig = new HashMap<>();
                    labConfig.put("examType", "实验考核");
                    labConfig.put("weight", labW); // 默认权重
                    config.add(labConfig);
                }

                // 为每个课程目标创建数据行
                for (GraduateCourseAbility courseAbility : courseAbilities) {
                    List<Map<String, Object>> rowData = new ArrayList<>();

                    if (finalW.compareTo(BigDecimal.ZERO) != 0) {
                        // 期末考核数据
                        Map<String, Object> finalData = calculateExamData(currentCourse.getId(), courseAbility.getAbilityId(), 1, "期末考核");
                        rowData.add(finalData);
                    }

                    if (perfW.compareTo(BigDecimal.ZERO) != 0) {
                        // 平时考核数据
                        Map<String, Object> performanceData = calculateExamData(currentCourse.getId(), courseAbility.getAbilityId(), 2, "平时考核");
                        rowData.add(performanceData);
                    }

                    if (labW.compareTo(BigDecimal.ZERO) != 0) {
                        // 实验考核数据
                        Map<String, Object> labData = calculateExamData(currentCourse.getId(), courseAbility.getAbilityId(), 3, "实验考核");
                        rowData.add(labData);
                    }

                    data.add(rowData);
                }
            }

            // 4. 构建响应结果
            Map<String, Object> result = new HashMap<>();
            result.put("config", config);
            result.put("data", data);

            return result;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算考试数据
     */
    private Map<String, Object> calculateExamData(Long courseId, Long abilityId, Integer examType, String examTypeName) {
        Map<String, Object> examData = new HashMap<>();

        try {
            // 获取该课程和考试类型的试卷（使用typeId字段：1=期末，2=平时，3=实验）
            List<GraduateExamPaper> examPapers = examPaperRepoService.list(
                    Wrappers.lambdaQuery(GraduateExamPaper.class)
                            .eq(GraduateExamPaper::getCourseId, courseId)
                            .eq(GraduateExamPaper::getTypeId, Long.valueOf(examType))
                            .eq(GraduateExamPaper::getIsValid, 0)
            );

            if (examPapers.isEmpty()) {
                examData.put("evaluationCriteria", examTypeName + "无数据");
                examData.put("averange/target", "0");
                examData.put("achievedValue", "0");
                return examData;
            }

            // 获取所有试卷的题目
            List<Long> paperIds = examPapers.stream()
                    .map(GraduateExamPaper::getId)
                    .collect(Collectors.toList());

            List<GraduateExamQuestion> allQuestions = examQuestionRepoService.list(
                    Wrappers.lambdaQuery(GraduateExamQuestion.class)
                            .in(GraduateExamQuestion::getPaperId, paperIds)
                            .eq(GraduateExamQuestion::getObjectiveId, abilityId)
                            .orderByAsc(GraduateExamQuestion::getOrderNo)
                            .orderByAsc(GraduateExamQuestion::getId)
            );

            if (allQuestions.isEmpty()) {
                examData.put("evaluationCriteria", examTypeName + "无相关题目");
                examData.put("averange/target", "0");
                examData.put("achievedValue", "0");
                return examData;
            }

            // 构建评价依据：收集所有题目的详细信息
            StringBuilder criteriaBuilder = new StringBuilder();
            for (GraduateExamQuestion question : allQuestions) {
                // 确定题目名称：优先使用questionNo，其次questionContent，最后examPurpose
                String questionName = null;
                if (question.getQuestionNo() != null && !question.getQuestionNo().trim().isEmpty()) {
                    questionName = question.getQuestionNo().trim();
                } else if (question.getQuestionContent() != null && !question.getQuestionContent().trim().isEmpty()) {
                    questionName = question.getQuestionContent().trim();
                } else if (question.getExamPurpose() != null && !question.getExamPurpose().trim().isEmpty()) {
                    questionName = question.getExamPurpose().trim();
                } else {
                    questionName = "题目";
                }
                
                // 获取分值
                BigDecimal score = question.getScore() != null ? question.getScore() : BigDecimal.ZERO;
                
                // 格式化：题目名称 (分值分)
                if (criteriaBuilder.length() > 0) {
                    criteriaBuilder.append("\n");
                }
                criteriaBuilder.append(questionName).append(" (").append(score.stripTrailingZeros().toPlainString()).append("分)");
            }

            // 获取学生成绩数据
            List<Long> questionIds = allQuestions.stream()
                    .map(GraduateExamQuestion::getId)
                    .collect(Collectors.toList());

            List<GraduateStudentScore> allScores = studentScoreRepoService.list(
                    Wrappers.lambdaQuery(GraduateStudentScore.class)
                            .in(GraduateStudentScore::getQuestionId, questionIds)
            );

            // 计算总分和平均分
            BigDecimal totalScore = allQuestions.stream()
                    .map(GraduateExamQuestion::getScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalStudentScore = allScores.stream()
                    .map(GraduateStudentScore::getStudentScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int studentCount = allScores.isEmpty() ? 0 :
                    (int) allScores.stream()
                            .map(GraduateStudentScore::getStudentId)
                            .distinct()
                            .count();

            BigDecimal averageScore = studentCount > 0 ?
                    totalStudentScore.divide(BigDecimal.valueOf(studentCount), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            // 计算达成值
            BigDecimal achievedValue = totalScore.compareTo(BigDecimal.ZERO) > 0 ?
                    averageScore.divide(totalScore, 4, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            // 设置评价依据为详细的题目列表
            examData.put("evaluationCriteria", criteriaBuilder.toString());
            examData.put("averange/target", averageScore.toString() + "/" + totalScore.toString());
            examData.put("achievedValue", achievedValue.toString());

        } catch (Exception e) {
            examData.put("evaluationCriteria", examTypeName + "计算错误");
            examData.put("averange/target", "0");
            examData.put("achievedValue", "0");
        }

        return examData;
    }
}
