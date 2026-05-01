package edu.xzit.graduate.controller;


import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.page.TableDataInfo;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.graduate.dao.domain.*;
import edu.xzit.graduate.dao.service.*;
import edu.xzit.graduate.service.CourseAbilityLinkDataService;
import edu.xzit.graduate.service.IGraduateCourseObjectiveService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程目标 Controller
 *
 * @author chenlei
 * @date 2025-08-25
 */
@RestController
@RequestMapping("/graduate/courseObjective")
public class GraduateCourseObjectiveController extends BaseController {
    @Autowired
    private IGraduateCourseObjectiveService courseObjectiveService;
    
    @Autowired
    private IGraduateCourseObjectiveRepoService courseObjectiveRepoService;
    
    @Autowired
    private IGraduateExamQuestionRepoService examQuestionRepoService;
    
    @Autowired
    private IGraduateStudentScoreRepoService studentScoreRepoService;

    @Autowired
    private IGraduateExamPaperRepoService examPaperRepoService;
    
    @Autowired
    private IGraduateCourseAbilityRepoService courseAbilityRepoService;
    
    @Autowired
    private IGraduateAbilityRepoService abilityRepoService;

    @Autowired
    private CourseAbilityLinkDataService courseAbilityLinkDataService;

    /**
     * 查询课程目标列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:courseObjective:list')")
    @GetMapping("/list")
    public TableDataInfo list(GraduateCourseObjective courseObjective) {
        startPage();
        // 查询逻辑已移至service层，包括专业ID过滤、能力名称格式化和专业名称填充
        List<GraduateCourseObjective> list = courseObjectiveService.selectCourseObjectiveList(courseObjective);
        return getDataTable(list);
    }

    /**
     * 导出课程目标列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:courseObjective:export')")
    @Log(title = "课程目标", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GraduateCourseObjective courseObjective) {
        // 查询逻辑已移至service层，包括专业ID过滤、能力名称格式化和专业名称填充
        List<GraduateCourseObjective> list = courseObjectiveService.selectCourseObjectiveList(courseObjective);
        ExcelUtil<GraduateCourseObjective> util = new ExcelUtil<>(GraduateCourseObjective.class);
        util.exportExcel(response, list, "课程目标数据");
    }

    /**
     * 获取课程目标详细信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:courseObjective:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(courseObjectiveService.selectCourseObjectiveById(id));
    }

    /**
     * 新增课程目标
     */
    @PreAuthorize("@ss.hasPermi('graduate:courseObjective:add')")
    @Log(title = "课程目标", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GraduateCourseObjective courseObjective) {
        return toAjax(courseObjectiveService.insertCourseObjective(courseObjective));
    }

    /**
     * 修改课程目标
     */
    @PreAuthorize("@ss.hasPermi('graduate:courseObjective:edit')")
    @Log(title = "课程目标", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GraduateCourseObjective courseObjective) {
        return toAjax(courseObjectiveService.updateCourseObjective(courseObjective));
    }

    /**
     * 删除课程目标
     */
    @PreAuthorize("@ss.hasPermi('graduate:courseObjective:remove')")
    @Log(title = "课程目标", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(courseObjectiveService.deleteCourseObjectiveByIds(ids));
    }

    /**
     * 获取课程目标达成度数据
     */
    @PreAuthorize("@ss.hasPermi('graduate:courseObjective:query')")
    @GetMapping("/achievement/{courseId}")
    public AjaxResult getObjectiveAchievement(@PathVariable("courseId") Long courseId) {
        try {
            // 1. 获取该课程的所有课程能力关系
            List<GraduateCourseAbility> courseAbilities = courseAbilityRepoService.list(
                Wrappers.lambdaQuery(GraduateCourseAbility.class)
                    .eq(GraduateCourseAbility::getCourseId, courseId)
                    .orderByAsc(GraduateCourseAbility::getId)
            );
            
            if (courseAbilities.isEmpty()) {
                return success(new ArrayList<>());
            }
            
            // 获取所有涉及的能力ID
            List<Long> abilityIds = courseAbilities.stream()
                .map(GraduateCourseAbility::getAbilityId)
                .collect(Collectors.toList());
            
            // 2. 获取该课程所有试卷的题目
            List<GraduateExamQuestion> allQuestions = examQuestionRepoService.list(
                Wrappers.lambdaQuery(GraduateExamQuestion.class)
                    .inSql(GraduateExamQuestion::getPaperId, 
                        "SELECT id FROM graduate_exam_paper WHERE course_id = " + courseId)
                    .orderByAsc(GraduateExamQuestion::getOrderNo)
            );
            
            // 3. 获取所有学生成绩数据
            List<GraduateStudentScore> allScores = new ArrayList<>();
            if (!allQuestions.isEmpty()) {
                List<Long> questionIds = allQuestions.stream()
                    .map(GraduateExamQuestion::getId)
                    .collect(Collectors.toList());
                
                allScores = studentScoreRepoService.list(
                    Wrappers.lambdaQuery(GraduateStudentScore.class)
                        .in(GraduateStudentScore::getQuestionId, questionIds)
                );
            }
            
            // 4. 计算每个课程能力关系的达成度
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (GraduateCourseAbility courseAbility : courseAbilities) {
                Map<String, Object> objectiveData = new HashMap<>();
                objectiveData.put("objectiveId", courseAbility.getId()); // 使用课程能力关系的ID
                
                // 获取能力名称
                GraduateAbility ability = abilityRepoService.getById(courseAbility.getAbilityId());
                String abilityName = ability != null ? ability.getName() : "未知能力";
                objectiveData.put("abilityName", abilityName);
                objectiveData.put("objective", "课程能力关系ID: " + courseAbility.getId()); // 临时显示
                
                // 获取该能力关联的所有题目（objective_id存储的是graduate_ability的id）
                List<GraduateExamQuestion> relatedQuestions = allQuestions.stream()
                    .filter(q -> courseAbility.getAbilityId().equals(q.getObjectiveId()))
                    .collect(Collectors.toList());
                
                if (relatedQuestions.isEmpty()) {
                    objectiveData.put("achievementRate", 0.0);
                    objectiveData.put("totalScore", 0.0);
                    objectiveData.put("averageScore", 0.0);
                    objectiveData.put("questionCount", 0);
                    objectiveData.put("studentCount", 0);
                } else {
                    // 计算总分值
                    BigDecimal totalScore = relatedQuestions.stream()
                        .map(GraduateExamQuestion::getScore)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    // 计算平均得分
                    BigDecimal totalStudentScore = BigDecimal.ZERO;
                    int studentCount = 0;
                    
                    for (GraduateExamQuestion question : relatedQuestions) {
                        List<GraduateStudentScore> questionScores = allScores.stream()
                            .filter(s -> s.getQuestionId().equals(question.getId()))
                            .collect(Collectors.toList());
                        
                        if (!questionScores.isEmpty()) {
                            BigDecimal questionTotalScore = questionScores.stream()
                                .map(GraduateStudentScore::getStudentScore)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                            
                            totalStudentScore = totalStudentScore.add(questionTotalScore);
                            studentCount = Math.max(studentCount, questionScores.size());
                        }
                    }
                    
                    // 计算达成度
                    BigDecimal achievementRate = BigDecimal.ZERO;
                    if (totalScore.compareTo(BigDecimal.ZERO) > 0 && studentCount > 0) {
                        BigDecimal averageScore = totalStudentScore.divide(BigDecimal.valueOf(studentCount), 4, RoundingMode.HALF_UP);
                        achievementRate = averageScore.divide(totalScore, 4, RoundingMode.HALF_UP);
                    }
                    
                    objectiveData.put("achievementRate", achievementRate.doubleValue());
                    objectiveData.put("totalScore", totalScore.doubleValue());
                    objectiveData.put("averageScore", totalStudentScore.doubleValue());
                    objectiveData.put("questionCount", relatedQuestions.size());
                    objectiveData.put("studentCount", studentCount);
                }
                
                result.add(objectiveData);
            }
            
            return success(result);
        } catch (Exception e) {
            return error("获取目标达成度数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取特定试卷的能力达成度数据
     */
    @PreAuthorize("@ss.hasPermi('graduate:courseObjective:query')")
    @GetMapping("/paper-achievement/{paperId}")
    public AjaxResult getPaperObjectiveAchievement(@PathVariable("paperId") Long paperId) {
        try {
            // 1. 获取试卷信息
            GraduateExamPaper examPaper = examPaperRepoService.getById(paperId);
            if (examPaper == null || examPaper.getCourseId() == null) {
                return success(new ArrayList<>());
            }
            
            Long courseId = examPaper.getCourseId();
            
            // 2. 获取该试卷的所有题目
            List<GraduateExamQuestion> paperQuestions = examQuestionRepoService.list(
                Wrappers.lambdaQuery(GraduateExamQuestion.class)
                    .eq(GraduateExamQuestion::getPaperId, paperId)
                    .orderByAsc(GraduateExamQuestion::getOrderNo)
            );
            
            if (paperQuestions.isEmpty()) {
                return success(new ArrayList<>());
            }
            
            // 3. 获取该课程的所有课程能力关系
            List<GraduateCourseAbility> courseAbilities = courseAbilityRepoService.list(
                Wrappers.lambdaQuery(GraduateCourseAbility.class)
                    .eq(GraduateCourseAbility::getCourseId, courseId)
                    .orderByAsc(GraduateCourseAbility::getId)
            );
            
            if (courseAbilities.isEmpty()) {
                return success(new ArrayList<>());
            }
            
            // 获取所有涉及的能力ID
            List<Long> abilityIds = courseAbilities.stream()
                .map(GraduateCourseAbility::getAbilityId)
                .collect(Collectors.toList());
            
            // 4. 获取该试卷的学生成绩数据
            List<Long> questionIds = paperQuestions.stream()
                .map(GraduateExamQuestion::getId)
                .collect(Collectors.toList());
            
            List<GraduateStudentScore> allScores = studentScoreRepoService.list(
                Wrappers.lambdaQuery(GraduateStudentScore.class)
                    .in(GraduateStudentScore::getQuestionId, questionIds)
            );
            
            // 5. 计算每个课程能力关系的达成度
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (GraduateCourseAbility courseAbility : courseAbilities) {
                Map<String, Object> objectiveData = new HashMap<>();
                objectiveData.put("objectiveId", courseAbility.getId()); // 使用课程能力关系的ID
                
                // 获取能力名称
                GraduateAbility ability = abilityRepoService.getById(courseAbility.getAbilityId());
                String abilityName = ability != null ? ability.getName() : "未知能力";
                objectiveData.put("abilityName", abilityName);
                objectiveData.put("objective", "课程能力关系ID: " + courseAbility.getId()); // 临时显示
                
                // 获取该能力关联的题目（仅限当前试卷，objective_id存储的是graduate_ability的id）
                List<GraduateExamQuestion> relatedQuestions = paperQuestions.stream()
                    .filter(q -> courseAbility.getAbilityId().equals(q.getObjectiveId()))
                    .collect(Collectors.toList());
                
                if (relatedQuestions.isEmpty()) {
                    objectiveData.put("achievementRate", 0.0);
                    objectiveData.put("totalScore", 0.0);
                    objectiveData.put("averageScore", 0.0);
                    objectiveData.put("questionCount", 0);
                    objectiveData.put("studentCount", 0);
                } else {
                    // 计算总分值
                    BigDecimal totalScore = relatedQuestions.stream()
                        .map(GraduateExamQuestion::getScore)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    // 计算平均得分
                    BigDecimal totalStudentScore = BigDecimal.ZERO;
                    int studentCount = 0;
                    
                    for (GraduateExamQuestion question : relatedQuestions) {
                        List<GraduateStudentScore> questionScores = allScores.stream()
                            .filter(s -> s.getQuestionId().equals(question.getId()))
                            .collect(Collectors.toList());
                        
                        if (!questionScores.isEmpty()) {
                            BigDecimal questionTotalScore = questionScores.stream()
                                .map(GraduateStudentScore::getStudentScore)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                            
                            totalStudentScore = totalStudentScore.add(questionTotalScore);
                            studentCount = Math.max(studentCount, questionScores.size());
                        }
                    }
                    
                    // 计算达成度
                    BigDecimal achievementRate = BigDecimal.ZERO;
                    if (totalScore.compareTo(BigDecimal.ZERO) > 0 && studentCount > 0) {
                        BigDecimal averageScore = totalStudentScore.divide(BigDecimal.valueOf(studentCount), 4, RoundingMode.HALF_UP);
                        achievementRate = averageScore.divide(totalScore, 4, RoundingMode.HALF_UP);
                    }
                    
                    objectiveData.put("achievementRate", achievementRate.doubleValue());
                    objectiveData.put("totalScore", totalScore.doubleValue());
                    objectiveData.put("averageScore", totalStudentScore.doubleValue());
                    objectiveData.put("questionCount", relatedQuestions.size());
                    objectiveData.put("studentCount", studentCount);
                }
                
                result.add(objectiveData);
            }
            
            return success(result);
        } catch (Exception e) {
            return error("获取试卷目标达成度数据失败：" + e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('graduate:courseObjective:query')")
    @GetMapping("/course-report/exam-result-analysis")
    public AjaxResult getExamResultAnalysis(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "major", required = false) String major,
            @RequestParam(value = "course", required = false) String course) {

        if (year == null || major == null || course == null) {
            return AjaxResult.success("参数不能为空");
        }

        Map result = courseAbilityLinkDataService.getCourseAbilityLinkData(year, major, course);
        if (result != null) {
            return AjaxResult.success(result);
        }

        return AjaxResult.error("查询错误");
    }
} 