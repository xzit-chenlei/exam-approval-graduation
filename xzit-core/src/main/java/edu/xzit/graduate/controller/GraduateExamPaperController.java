package edu.xzit.graduate.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.page.TableDataInfo;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.graduate.dao.domain.GraduateExamPaper;
import edu.xzit.graduate.dao.domain.GraduateExamQuestion;
import edu.xzit.graduate.dao.domain.GraduateStudentScore;
import edu.xzit.graduate.dao.domain.GraduateAbility;
import edu.xzit.graduate.dao.domain.GraduateCourse;
import edu.xzit.graduate.dao.service.IGraduateExamPaperRepoService;
import edu.xzit.graduate.dao.service.IGraduateExamQuestionRepoService;
import edu.xzit.graduate.dao.service.IGraduateStudentScoreRepoService;
import edu.xzit.graduate.dao.service.IGraduateAbilityRepoService;
import edu.xzit.graduate.dao.service.IGraduateCourseRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 毕业达成度试卷信息 Controller
 *
 * @author system
 * @date 2025-01-22
 */
@RestController
@RequestMapping("/graduate/examPaper")
public class GraduateExamPaperController extends BaseController {
    
    @Autowired
    private IGraduateExamPaperRepoService graduateExamPaperRepoService;

    @Autowired
    private IGraduateExamQuestionRepoService graduateExamQuestionRepoService;

    @Autowired
    private IGraduateStudentScoreRepoService graduateStudentScoreRepoService;

    @Autowired
    private IGraduateAbilityRepoService graduateAbilityRepoService;

    @Autowired
    private IGraduateCourseRepoService graduateCourseRepoService;

    /**
     * 查询试卷信息列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:examPaper:list')")
    @GetMapping("/list")
    public TableDataInfo list(GraduateExamPaper graduateExamPaper) {
        startPage();
        LambdaQueryWrapper<GraduateExamPaper> wrapper = Wrappers.lambdaQuery();
        
        if (graduateExamPaper.getMajorId() != null) {
            wrapper.eq(GraduateExamPaper::getMajorId, graduateExamPaper.getMajorId());
        }
        if (graduateExamPaper.getCourseId() != null) {
            wrapper.eq(GraduateExamPaper::getCourseId, graduateExamPaper.getCourseId());
        }
        if (graduateExamPaper.getPaperName() != null && !graduateExamPaper.getPaperName().isEmpty()) {
            wrapper.like(GraduateExamPaper::getPaperName, graduateExamPaper.getPaperName());
        }
        if (graduateExamPaper.getPaperCode() != null && !graduateExamPaper.getPaperCode().isEmpty()) {
            wrapper.like(GraduateExamPaper::getPaperCode, graduateExamPaper.getPaperCode());
        }
        if (graduateExamPaper.getExamType() != null && !graduateExamPaper.getExamType().isEmpty()) {
            wrapper.eq(GraduateExamPaper::getExamType, graduateExamPaper.getExamType());
        }
        if (graduateExamPaper.getGrade() != null) {
            wrapper.eq(GraduateExamPaper::getGrade, graduateExamPaper.getGrade());
        }
        if (graduateExamPaper.getSemester() != null && !graduateExamPaper.getSemester().isEmpty()) {
            wrapper.eq(GraduateExamPaper::getSemester, graduateExamPaper.getSemester());
        }
        if (graduateExamPaper.getTypeId() != null) {
            wrapper.eq(GraduateExamPaper::getTypeId, graduateExamPaper.getTypeId());
        }
        
        wrapper.orderByAsc(GraduateExamPaper::getOrderNo, GraduateExamPaper::getId);
        List<GraduateExamPaper> list = graduateExamPaperRepoService.list(wrapper);
        return getDataTable(list);
    }

    /**
     * 导出试卷信息列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:examPaper:export')")
    @Log(title = "试卷信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GraduateExamPaper graduateExamPaper) {
        LambdaQueryWrapper<GraduateExamPaper> wrapper = Wrappers.lambdaQuery();
        
        if (graduateExamPaper.getMajorId() != null) {
            wrapper.eq(GraduateExamPaper::getMajorId, graduateExamPaper.getMajorId());
        }
        if (graduateExamPaper.getCourseId() != null) {
            wrapper.eq(GraduateExamPaper::getCourseId, graduateExamPaper.getCourseId());
        }
        if (graduateExamPaper.getPaperName() != null && !graduateExamPaper.getPaperName().isEmpty()) {
            wrapper.like(GraduateExamPaper::getPaperName, graduateExamPaper.getPaperName());
        }
        
        wrapper.orderByAsc(GraduateExamPaper::getOrderNo, GraduateExamPaper::getId);
        List<GraduateExamPaper> list = graduateExamPaperRepoService.list(wrapper);
        ExcelUtil<GraduateExamPaper> util = new ExcelUtil<GraduateExamPaper>(GraduateExamPaper.class);
        util.exportExcel(response, list, "试卷信息数据");
    }

    /**
     * 获取试卷信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:examPaper:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(graduateExamPaperRepoService.getById(id));
    }

    /**
     * 新增试卷信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:examPaper:add')")
    @Log(title = "试卷信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GraduateExamPaper graduateExamPaper) {
        graduateExamPaperRepoService.save(graduateExamPaper);
        return success();
    }

    /**
     * 修改试卷信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:examPaper:edit')")
    @Log(title = "试卷信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GraduateExamPaper graduateExamPaper) {
        graduateExamPaperRepoService.updateById(graduateExamPaper);
        return success();
    }

    /**
     * 删除试卷信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:examPaper:remove')")
    @Log(title = "试卷信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        graduateExamPaperRepoService.removeByIds(Arrays.asList(ids));
        return success();
    }

    /**
     * 获取试卷的题目与毕业能力关系及达成度
     * 返回：题目列表（包含能力信息）、每个毕业能力的总分、平均分、达成度百分比
     */
    @PreAuthorize("@ss.hasPermi('graduate:examPaper:query')")
    @GetMapping("/abilityAchievement/{paperId}")
    public AjaxResult getAbilityAchievement(@PathVariable("paperId") Long paperId) {
        try {
            // 1. 获取试卷信息
            GraduateExamPaper paper = graduateExamPaperRepoService.getById(paperId);
            if (paper == null) {
                return error("试卷不存在");
            }

            // 2. 获取试卷的所有题目（包含 abilityId 和 score）
            LambdaQueryWrapper<GraduateExamQuestion> questionWrapper = Wrappers.lambdaQuery();
            questionWrapper.eq(GraduateExamQuestion::getPaperId, paperId)
                          .orderByAsc(GraduateExamQuestion::getOrderNo);
            List<GraduateExamQuestion> questions = graduateExamQuestionRepoService.list(questionWrapper);

            if (questions.isEmpty()) {
                return success(new HashMap<String, Object>() {{
                    put("questions", new ArrayList<>());
                    put("abilityAchievements", new ArrayList<>());
                }});
            }

            // 3. 获取该试卷的所有成绩
            LambdaQueryWrapper<GraduateStudentScore> scoreWrapper = Wrappers.lambdaQuery();
            scoreWrapper.eq(GraduateStudentScore::getPaperId, paperId);
            List<GraduateStudentScore> scores = graduateStudentScoreRepoService.list(scoreWrapper);

            // 4. 获取课程信息，用于查询能力列表
            GraduateCourse course = null;
            Long majorId = null;
            if (paper.getCourseId() != null) {
                course = graduateCourseRepoService.getById(paper.getCourseId());
                if (course != null) {
                    majorId = course.getMajorId();
                }
            }

            // 5. 获取该专业的所有能力（子级能力，用于计算）
            List<GraduateAbility> abilities = new ArrayList<>();
            if (majorId != null) {
                List<GraduateAbility> allAbilities = graduateAbilityRepoService.list(
                    Wrappers.lambdaQuery(GraduateAbility.class)
                            .eq(GraduateAbility::getMajorId, majorId)
                            .orderByAsc(GraduateAbility::getOrderNo)
                );
                // 只取子级能力（有父级的能力）
                abilities = allAbilities.stream()
                        .filter(a -> a.getParentId() != null)
                        .collect(Collectors.toList());
            }

            // 6. 构建题目列表（包含能力信息）
            List<Map<String, Object>> questionList = new ArrayList<>();
            for (GraduateExamQuestion question : questions) {
                Map<String, Object> questionMap = new HashMap<>();
                questionMap.put("id", question.getId());
                questionMap.put("questionNo", question.getQuestionNo());
                questionMap.put("examPurpose", question.getExamPurpose());
                questionMap.put("score", question.getScore());
                questionMap.put("abilityId", question.getAbilityId());
                
                // 查找能力名称
                String abilityName = "未关联能力";
                if (question.getAbilityId() != null) {
                    GraduateAbility ability = abilities.stream()
                            .filter(a -> a.getId().equals(question.getAbilityId()))
                            .findFirst()
                            .orElse(null);
                    if (ability != null) {
                        abilityName = ability.getName();
                    }
                }
                questionMap.put("abilityName", abilityName);
                
                questionList.add(questionMap);
            }

            // 7. 计算每个毕业能力的达成度
            List<Map<String, Object>> abilityAchievements = new ArrayList<>();
            
            // 按能力ID分组题目
            Map<Long, List<GraduateExamQuestion>> abilityToQuestions = questions.stream()
                    .filter(q -> q.getAbilityId() != null)
                    .collect(Collectors.groupingBy(GraduateExamQuestion::getAbilityId));

            for (GraduateAbility ability : abilities) {
                Long abilityId = ability.getId();
                List<GraduateExamQuestion> relatedQuestions = abilityToQuestions.getOrDefault(abilityId, new ArrayList<>());
                
                // 只显示和题目有关联的能力，跳过没有关联题目的能力
                if (relatedQuestions.isEmpty()) {
                    continue;
                }

                // 计算该能力对应题目的总分
                BigDecimal totalScore = relatedQuestions.stream()
                        .map(GraduateExamQuestion::getScore)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 计算该能力对应题目的平均分
                BigDecimal totalStudentScore = BigDecimal.ZERO;
                int studentCount = 0;
                Set<Long> studentIds = new HashSet<>();

                for (GraduateExamQuestion question : relatedQuestions) {
                    List<GraduateStudentScore> questionScores = scores.stream()
                            .filter(s -> s.getQuestionId().equals(question.getId()))
                            .collect(Collectors.toList());

                    if (!questionScores.isEmpty()) {
                        BigDecimal questionTotalScore = questionScores.stream()
                                .map(GraduateStudentScore::getStudentScore)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        totalStudentScore = totalStudentScore.add(questionTotalScore);
                        
                        // 统计学生数量（去重）
                        questionScores.forEach(s -> studentIds.add(s.getStudentId()));
                    }
                }

                studentCount = studentIds.size();

                // 计算平均分
                BigDecimal averageScore = BigDecimal.ZERO;
                if (studentCount > 0 && totalScore.compareTo(BigDecimal.ZERO) > 0) {
                    // 平均分 = 所有题目得分总和 / 学生数量
                    averageScore = totalStudentScore.divide(BigDecimal.valueOf(studentCount), 2, RoundingMode.HALF_UP);
                }

                // 计算达成度百分比 = 平均分 / 总分 * 100
                BigDecimal achievementRate = BigDecimal.ZERO;
                if (totalScore.compareTo(BigDecimal.ZERO) > 0 && averageScore.compareTo(BigDecimal.ZERO) > 0) {
                    achievementRate = averageScore.divide(totalScore, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                }

                Map<String, Object> achievement = new HashMap<>();
                achievement.put("abilityId", abilityId);
                achievement.put("abilityName", ability.getName());
                achievement.put("totalScore", totalScore);
                achievement.put("averageScore", averageScore);
                achievement.put("achievementRate", achievementRate);
                achievement.put("questionCount", relatedQuestions.size());
                achievement.put("studentCount", studentCount);
                
                abilityAchievements.add(achievement);
            }

            // 8. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("questions", questionList);
            result.put("abilityAchievements", abilityAchievements);
            result.put("paperName", paper.getPaperName());

            return success(result);
        } catch (Exception e) {
            logger.error("获取毕业能力达成度失败，paperId: {}", paperId, e);
            return error("获取毕业能力达成度失败：" + e.getMessage());
        }
    }
}