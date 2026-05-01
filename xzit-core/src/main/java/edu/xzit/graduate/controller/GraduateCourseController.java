package edu.xzit.graduate.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.page.TableDataInfo;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.core.common.utils.poi.ExcelUtil;
import edu.xzit.graduate.dao.domain.GraduateCourse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import edu.xzit.graduate.dao.domain.GraduateExamPaper;
import edu.xzit.graduate.dao.domain.GraduateExamQuestion;
import edu.xzit.graduate.dao.domain.GraduateStudentScore;
import edu.xzit.graduate.dao.domain.GraduateAbility;
import edu.xzit.graduate.dao.domain.GraduateCourseWeight;
import edu.xzit.graduate.dao.domain.GraduateMajor;
import edu.xzit.graduate.dao.service.IGraduateCourseRepoService;
import edu.xzit.graduate.dao.service.IGraduateMajorRepoService;
import edu.xzit.graduate.dao.service.IGraduateExamPaperRepoService;
import edu.xzit.graduate.dao.service.IGraduateExamQuestionRepoService;
import edu.xzit.graduate.dao.service.IGraduateStudentScoreRepoService;
import edu.xzit.graduate.dao.service.IGraduateAbilityRepoService;
import edu.xzit.graduate.dao.service.IGraduateCourseWeightRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 毕业达成度课程 Controller
 *
 * @author system
 * @date 2025-01-22
 */
@RestController
@RequestMapping("/graduate/course")
public class GraduateCourseController extends BaseController {
    
    @Autowired
    private IGraduateCourseRepoService graduateCourseRepoService;

    @Autowired
    private IGraduateExamPaperRepoService graduateExamPaperRepoService;

    @Autowired
    private IGraduateExamQuestionRepoService graduateExamQuestionRepoService;

    @Autowired
    private IGraduateStudentScoreRepoService graduateStudentScoreRepoService;

    @Autowired
    private IGraduateAbilityRepoService graduateAbilityRepoService;

    @Autowired
    private IGraduateCourseWeightRepoService graduateCourseWeightRepoService;

    @Autowired
    private IGraduateMajorRepoService graduateMajorRepoService;

    /**
     * 入学年级-专业-课程 级联选项，供流程发起页「入学年级-专业-课程」级联框使用。
     * 结构：第一级入学年级(grade)，第二级专业(major_id)，第三级课程(course id)。
     * 数据来源：graduate_course（grade、major_id、id/name）、graduate_major（id、name），仅有效数据(is_valid=0)。
     */
    @GetMapping("/cascaderOptions")
    public AjaxResult cascaderOptions() {
        LambdaQueryWrapper<GraduateCourse> cWrapper = Wrappers.<GraduateCourse>lambdaQuery()
                .eq(GraduateCourse::getIsValid, 0)
                .orderByAsc(GraduateCourse::getGrade, GraduateCourse::getMajorId, GraduateCourse::getOrderNo, GraduateCourse::getId);
        List<GraduateCourse> allCourses = graduateCourseRepoService.list(cWrapper);
        if (allCourses == null || allCourses.isEmpty()) {
            return success(Collections.emptyList());
        }
        List<GraduateMajor> allMajors = graduateMajorRepoService.list(
                Wrappers.<GraduateMajor>lambdaQuery().eq(GraduateMajor::getIsValid, 0));
        Map<Long, String> majorIdToName = allMajors.stream()
                .collect(Collectors.toMap(GraduateMajor::getId, m -> m.getName() != null ? m.getName() : "", (a, b) -> a));

        Set<Integer> grades = allCourses.stream()
                .map(GraduateCourse::getGrade)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> new TreeSet<Integer>(Comparator.naturalOrder())));

        List<Map<String, Object>> rootList = new ArrayList<>();
        for (Integer grade : grades) {
            Map<String, Object> gradeNode = new LinkedHashMap<>();
            gradeNode.put("label", String.valueOf(grade));
            gradeNode.put("value", grade);

            List<GraduateCourse> coursesInGrade = allCourses.stream()
                    .filter(c -> grade.equals(c.getGrade()))
                    .collect(Collectors.toList());
            Set<Long> majorIdsInGrade = coursesInGrade.stream()
                    .map(GraduateCourse::getMajorId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            List<Map<String, Object>> majorChildren = new ArrayList<>();
            for (Long majorId : majorIdsInGrade) {
                String majorName = majorIdToName.getOrDefault(majorId, "专业" + majorId);
                Map<String, Object> majorNode = new LinkedHashMap<>();
                majorNode.put("label", majorName);
                majorNode.put("value", majorId);

                List<Map<String, Object>> courseChildren = coursesInGrade.stream()
                        .filter(c -> majorId.equals(c.getMajorId()))
                        .map(c -> {
                            Map<String, Object> courseNode = new LinkedHashMap<>();
                            courseNode.put("label", c.getName() != null ? c.getName() : "");
                            courseNode.put("value", c.getId());
                            return courseNode;
                        })
                        .collect(Collectors.toList());
                majorNode.put("children", courseChildren);
                majorChildren.add(majorNode);
            }
            gradeNode.put("children", majorChildren);
            rootList.add(gradeNode);
        }
        return success(rootList);
    }

    /**
     * 查询课程列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:course:list')")
    @GetMapping("/list")
    public TableDataInfo list(GraduateCourse graduateCourse) {
        startPage();
        LambdaQueryWrapper<GraduateCourse> wrapper = Wrappers.lambdaQuery();
        
        if (graduateCourse.getMajorId() != null) {
            wrapper.eq(GraduateCourse::getMajorId, graduateCourse.getMajorId());
        }
        if (graduateCourse.getName() != null && !graduateCourse.getName().isEmpty()) {
            wrapper.like(GraduateCourse::getName, graduateCourse.getName());
        }
        if (graduateCourse.getCode() != null && !graduateCourse.getCode().isEmpty()) {
            wrapper.like(GraduateCourse::getCode, graduateCourse.getCode());
        }
        
        wrapper.orderByAsc(GraduateCourse::getOrderNo, GraduateCourse::getId);
        List<GraduateCourse> list = graduateCourseRepoService.list(wrapper);
        return getDataTable(list);
    }

    /**
     * 导出课程列表
     */
    @PreAuthorize("@ss.hasPermi('graduate:course:export')")
    @Log(title = "课程", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GraduateCourse graduateCourse) {
        LambdaQueryWrapper<GraduateCourse> wrapper = Wrappers.lambdaQuery();
        
        if (graduateCourse.getMajorId() != null) {
            wrapper.eq(GraduateCourse::getMajorId, graduateCourse.getMajorId());
        }
        if (graduateCourse.getName() != null && !graduateCourse.getName().isEmpty()) {
            wrapper.like(GraduateCourse::getName, graduateCourse.getName());
        }
        
        wrapper.orderByAsc(GraduateCourse::getOrderNo, GraduateCourse::getId);
        List<GraduateCourse> list = graduateCourseRepoService.list(wrapper);
        ExcelUtil<GraduateCourse> util = new ExcelUtil<GraduateCourse>(GraduateCourse.class);
        util.exportExcel(response, list, "课程数据");
    }

    /**
     * 获取课程详细信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:course:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(graduateCourseRepoService.getById(id));
    }

    /**
     * 新增课程
     */
    @PreAuthorize("@ss.hasPermi('graduate:course:add')")
    @Log(title = "课程", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GraduateCourse graduateCourse) {
        graduateCourseRepoService.save(graduateCourse);
        return success();
    }

    /**
     * 修改课程
     */
    @PreAuthorize("@ss.hasPermi('graduate:course:edit')")
    @Log(title = "课程", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GraduateCourse graduateCourse) {
        graduateCourseRepoService.updateById(graduateCourse);
        return success();
    }

    /**
     * 删除课程
     */
    @PreAuthorize("@ss.hasPermi('graduate:course:remove')")
    @Log(title = "课程", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        graduateCourseRepoService.removeByIds(Arrays.asList(ids));
        return success();
    }

    /**
     * 获取课程的综合毕业能力达成度（按类别权重加权计算）
     * 返回：按类别（期末、平时、实验）分组的能力达成度，以及加权平均的最终达成度
     */
    @PreAuthorize("@ss.hasPermi('graduate:course:query')")
    @GetMapping("/abilityAchievement/{courseId}")
    public AjaxResult getCourseAbilityAchievement(@PathVariable("courseId") Long courseId) {
        try {
            // 1. 获取课程信息
            GraduateCourse course = graduateCourseRepoService.getById(courseId);
            if (course == null) {
                return error("课程不存在");
            }

            // 2. 获取课程权重
            LambdaQueryWrapper<GraduateCourseWeight> weightWrapper = Wrappers.lambdaQuery();
            weightWrapper.eq(GraduateCourseWeight::getCourseId, courseId);
            List<GraduateCourseWeight> weights = graduateCourseWeightRepoService.list(weightWrapper);
            GraduateCourseWeight courseWeight = weights.isEmpty() ? null : weights.get(0);

            BigDecimal finalWeight = courseWeight != null && courseWeight.getFinalW() != null 
                    ? courseWeight.getFinalW() : BigDecimal.ZERO;
            BigDecimal perfWeight = courseWeight != null && courseWeight.getPerfW() != null 
                    ? courseWeight.getPerfW() : BigDecimal.ZERO;
            BigDecimal labWeight = courseWeight != null && courseWeight.getLabW() != null 
                    ? courseWeight.getLabW() : BigDecimal.ZERO;

            // 3. 获取该课程下的所有试卷（按类型分组）
            LambdaQueryWrapper<GraduateExamPaper> paperWrapper = Wrappers.lambdaQuery();
            paperWrapper.eq(GraduateExamPaper::getCourseId, courseId);
            List<GraduateExamPaper> papers = graduateExamPaperRepoService.list(paperWrapper);

            // 按类型分组：1=期末，2=平时，3=实验
            GraduateExamPaper finalPaper = papers.stream()
                    .filter(p -> p.getTypeId() != null && p.getTypeId().equals(1L))
                    .findFirst().orElse(null);
            GraduateExamPaper perfPaper = papers.stream()
                    .filter(p -> p.getTypeId() != null && p.getTypeId().equals(2L))
                    .findFirst().orElse(null);
            GraduateExamPaper labPaper = papers.stream()
                    .filter(p -> p.getTypeId() != null && p.getTypeId().equals(3L))
                    .findFirst().orElse(null);

            // 4. 获取专业ID，用于查询能力列表
            Long majorId = course.getMajorId();
            List<GraduateAbility> abilities = new ArrayList<>();
            if (majorId != null) {
                List<GraduateAbility> allAbilities = graduateAbilityRepoService.list(
                    Wrappers.lambdaQuery(GraduateAbility.class)
                            .eq(GraduateAbility::getMajorId, majorId)
                            .orderByAsc(GraduateAbility::getOrderNo)
                );
                abilities = allAbilities.stream()
                        .filter(a -> a.getParentId() != null)
                        .collect(Collectors.toList());
            }

            // 5. 计算各类别的能力达成度
            Map<String, List<Map<String, Object>>> categoryAchievements = new HashMap<>();
            categoryAchievements.put("final", calculatePaperTypeAchievements(finalPaper, abilities));
            categoryAchievements.put("perf", calculatePaperTypeAchievements(perfPaper, abilities));
            categoryAchievements.put("lab", calculatePaperTypeAchievements(labPaper, abilities));

            // 6. 计算加权平均的最终达成度
            List<Map<String, Object>> finalAchievements = new ArrayList<>();
            for (GraduateAbility ability : abilities) {
                Long abilityId = ability.getId();
                String abilityName = ability.getName();

                // 获取各类别的达成度
                BigDecimal finalRate = getAchievementRateByAbilityId(categoryAchievements.get("final"), abilityId);
                BigDecimal perfRate = getAchievementRateByAbilityId(categoryAchievements.get("perf"), abilityId);
                BigDecimal labRate = getAchievementRateByAbilityId(categoryAchievements.get("lab"), abilityId);

                // 计算加权平均达成度 = 期末权重 * 期末达成度 + 平时权重 * 平时达成度 + 实验权重 * 实验达成度
                BigDecimal weightedRate = BigDecimal.ZERO;
                BigDecimal totalWeight = finalWeight.add(perfWeight).add(labWeight);
                
                if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal weightedSum = finalRate.multiply(finalWeight)
                            .add(perfRate.multiply(perfWeight))
                            .add(labRate.multiply(labWeight));
                    weightedRate = weightedSum.divide(totalWeight, 2, RoundingMode.HALF_UP);
                } else {
                    // 如果没有权重，使用简单平均
                    int count = 0;
                    BigDecimal sum = BigDecimal.ZERO;
                    if (finalRate.compareTo(BigDecimal.ZERO) > 0) {
                        sum = sum.add(finalRate);
                        count++;
                    }
                    if (perfRate.compareTo(BigDecimal.ZERO) > 0) {
                        sum = sum.add(perfRate);
                        count++;
                    }
                    if (labRate.compareTo(BigDecimal.ZERO) > 0) {
                        sum = sum.add(labRate);
                        count++;
                    }
                    if (count > 0) {
                        weightedRate = sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
                    }
                }

                // 只显示至少在一个类别中有达成度的能力
                if (finalRate.compareTo(BigDecimal.ZERO) > 0 || 
                    perfRate.compareTo(BigDecimal.ZERO) > 0 || 
                    labRate.compareTo(BigDecimal.ZERO) > 0) {
                    Map<String, Object> achievement = new HashMap<>();
                    achievement.put("abilityId", abilityId);
                    achievement.put("abilityName", abilityName);
                    achievement.put("finalRate", finalRate);
                    achievement.put("perfRate", perfRate);
                    achievement.put("labRate", labRate);
                    achievement.put("finalWeight", finalWeight.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP));
                    achievement.put("perfWeight", perfWeight.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP));
                    achievement.put("labWeight", labWeight.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP));
                    achievement.put("weightedRate", weightedRate);
                    finalAchievements.add(achievement);
                }
            }

            // 7. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("courseName", course.getName());
            result.put("categoryAchievements", categoryAchievements);
            result.put("finalAchievements", finalAchievements);
            result.put("weights", new HashMap<String, Object>() {{
                put("finalWeight", finalWeight.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP));
                put("perfWeight", perfWeight.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP));
                put("labWeight", labWeight.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP));
            }});

            return success(result);
        } catch (Exception e) {
            logger.error("获取课程毕业能力达成度失败，courseId: {}", courseId, e);
            return error("获取课程毕业能力达成度失败：" + e.getMessage());
        }
    }

    /**
     * 计算指定试卷类型的能力达成度
     */
    private List<Map<String, Object>> calculatePaperTypeAchievements(GraduateExamPaper paper, List<GraduateAbility> abilities) {
        List<Map<String, Object>> achievements = new ArrayList<>();
        
        if (paper == null) {
            return achievements;
        }

        try {
            // 获取试卷的所有题目
            LambdaQueryWrapper<GraduateExamQuestion> questionWrapper = Wrappers.lambdaQuery();
            questionWrapper.eq(GraduateExamQuestion::getPaperId, paper.getId())
                          .orderByAsc(GraduateExamQuestion::getOrderNo);
            List<GraduateExamQuestion> questions = graduateExamQuestionRepoService.list(questionWrapper);

            if (questions.isEmpty()) {
                return achievements;
            }

            // 获取该试卷的所有成绩
            LambdaQueryWrapper<GraduateStudentScore> scoreWrapper = Wrappers.lambdaQuery();
            scoreWrapper.eq(GraduateStudentScore::getPaperId, paper.getId());
            List<GraduateStudentScore> scores = graduateStudentScoreRepoService.list(scoreWrapper);

            // 按能力ID分组题目
            Map<Long, List<GraduateExamQuestion>> abilityToQuestions = questions.stream()
                    .filter(q -> q.getAbilityId() != null)
                    .collect(Collectors.groupingBy(GraduateExamQuestion::getAbilityId));

            for (GraduateAbility ability : abilities) {
                Long abilityId = ability.getId();
                List<GraduateExamQuestion> relatedQuestions = abilityToQuestions.getOrDefault(abilityId, new ArrayList<>());
                
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
                        questionScores.forEach(s -> studentIds.add(s.getStudentId()));
                    }
                }

                int studentCount = studentIds.size();

                // 计算平均分
                BigDecimal averageScore = BigDecimal.ZERO;
                if (studentCount > 0 && totalScore.compareTo(BigDecimal.ZERO) > 0) {
                    averageScore = totalStudentScore.divide(BigDecimal.valueOf(studentCount), 2, RoundingMode.HALF_UP);
                }

                // 计算达成度百分比
                BigDecimal achievementRate = BigDecimal.ZERO;
                if (totalScore.compareTo(BigDecimal.ZERO) > 0 && averageScore.compareTo(BigDecimal.ZERO) > 0) {
                    achievementRate = averageScore.divide(totalScore, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                }

                Map<String, Object> achievement = new HashMap<>();
                achievement.put("abilityId", abilityId);
                achievement.put("abilityName", ability.getName());
                achievement.put("achievementRate", achievementRate);
                achievements.add(achievement);
            }
        } catch (Exception e) {
            logger.error("计算试卷能力达成度失败，paperId: {}", paper.getId(), e);
        }

        return achievements;
    }

    /**
     * 从类别达成度列表中获取指定能力的达成度
     */
    private BigDecimal getAchievementRateByAbilityId(List<Map<String, Object>> achievements, Long abilityId) {
        if (achievements == null || achievements.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return achievements.stream()
                .filter(a -> abilityId.equals(a.get("abilityId")))
                .findFirst()
                .map(a -> {
                    Object rate = a.get("achievementRate");
                    if (rate instanceof BigDecimal) {
                        return (BigDecimal) rate;
                    } else if (rate instanceof Number) {
                        return BigDecimal.valueOf(((Number) rate).doubleValue());
                    }
                    return BigDecimal.ZERO;
                })
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 导出课程毕业能力达成度Excel
     */
    @PreAuthorize("@ss.hasPermi('graduate:course:export')")
    @Log(title = "课程毕业能力达成度", businessType = BusinessType.EXPORT)
    @GetMapping("/abilityAchievement/export/{courseId}")
    public void exportCourseAbilityAchievement(HttpServletResponse response, @PathVariable("courseId") Long courseId) {
        XSSFWorkbook workbook = null;
        try {
            // 1. 获取课程信息
            GraduateCourse course = graduateCourseRepoService.getById(courseId);
            if (course == null) {
                logger.error("课程不存在，courseId: {}", courseId);
                return;
            }

            // 2. 获取课程能力达成度数据（复用现有方法）
            AjaxResult result = getCourseAbilityAchievement(courseId);
            if (!result.get("code").equals(200)) {
                logger.error("获取课程毕业能力达成度失败，courseId: {}", courseId);
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String courseName = (String) data.get("courseName");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> finalAchievements = (List<Map<String, Object>>) data.get("finalAchievements");
            @SuppressWarnings("unchecked")
            Map<String, Object> weights = (Map<String, Object>) data.get("weights");

            // 3. 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = "课程毕业能力达成度_" + courseName + "_" + System.currentTimeMillis() + ".xlsx";
            response.setHeader("Content-disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            // 4. 创建Excel工作簿
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("毕业能力达成度");

            // 5. 创建样式
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            CellStyle infoStyle = workbook.createCellStyle();
            infoStyle.setAlignment(HorizontalAlignment.LEFT);
            infoStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // 6. 创建标题行
            int rowIndex = 0;
            Row titleRow = sheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(courseName + " - 毕业能力达成度（加权平均）");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            // 7. 创建权重信息行
            Row weightRow = sheet.createRow(rowIndex++);
            Cell weightCell = weightRow.createCell(0);
            Integer finalWeight = weights != null ? ((Number) weights.get("finalWeight")).intValue() : 0;
            Integer perfWeight = weights != null ? ((Number) weights.get("perfWeight")).intValue() : 0;
            Integer labWeight = weights != null ? ((Number) weights.get("labWeight")).intValue() : 0;
            weightCell.setCellValue(String.format("权重配置：期末 %d%% | 平时 %d%% | 实验 %d%%", 
                finalWeight, perfWeight, labWeight));
            weightCell.setCellStyle(infoStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

            // 8. 创建表头行
            Row headerRow = sheet.createRow(rowIndex++);
            int colIndex = 0;
            String[] headers = {"毕业能力", "期末达成度", "平时达成度", "实验达成度", "最终达成度"};
            for (String header : headers) {
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(header);
                cell.setCellStyle(headerStyle);
            }

            // 9. 填充数据行
            for (Map<String, Object> achievement : finalAchievements) {
                Row dataRow = sheet.createRow(rowIndex++);
                colIndex = 0;

                String abilityName = (String) achievement.get("abilityName");
                BigDecimal finalRate = achievement.get("finalRate") != null ? 
                    new BigDecimal(achievement.get("finalRate").toString()) : BigDecimal.ZERO;
                BigDecimal perfRate = achievement.get("perfRate") != null ? 
                    new BigDecimal(achievement.get("perfRate").toString()) : BigDecimal.ZERO;
                BigDecimal labRate = achievement.get("labRate") != null ? 
                    new BigDecimal(achievement.get("labRate").toString()) : BigDecimal.ZERO;
                BigDecimal weightedRate = achievement.get("weightedRate") != null ? 
                    new BigDecimal(achievement.get("weightedRate").toString()) : BigDecimal.ZERO;

                dataRow.createCell(colIndex++).setCellValue(abilityName != null ? abilityName : "");
                dataRow.createCell(colIndex++).setCellValue(String.format("%.1f%%", finalRate.doubleValue()));
                dataRow.createCell(colIndex++).setCellValue(String.format("%.1f%%", perfRate.doubleValue()));
                dataRow.createCell(colIndex++).setCellValue(String.format("%.1f%%", labRate.doubleValue()));
                dataRow.createCell(colIndex++).setCellValue(String.format("%.1f%%", weightedRate.doubleValue()));

                // 应用样式
                for (int i = 0; i < colIndex; i++) {
                    dataRow.getCell(i).setCellStyle(dataStyle);
                }
            }

            // 10. 自动调整列宽
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小列宽
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }

            // 11. 写入响应
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            logger.error("导出课程毕业能力达成度Excel失败，courseId: {}", courseId, e);
        } finally {
            org.apache.poi.util.IOUtils.closeQuietly(workbook);
        }
    }

}