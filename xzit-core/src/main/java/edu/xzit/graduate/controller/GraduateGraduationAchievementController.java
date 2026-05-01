package edu.xzit.graduate.controller;

import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.enums.BusinessType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import edu.xzit.graduate.dao.domain.GraduateAbility;
import edu.xzit.graduate.dao.domain.GraduateCourse;
import edu.xzit.graduate.dao.domain.GraduateCourseAbility;
import edu.xzit.graduate.dao.domain.GraduateCourseWeight;
import edu.xzit.graduate.dao.domain.GraduateExamPaper;
import edu.xzit.graduate.dao.domain.GraduateExamQuestion;
import edu.xzit.graduate.dao.domain.GraduateStudentScore;
import edu.xzit.graduate.dao.service.IGraduateAbilityRepoService;
import edu.xzit.graduate.dao.service.IGraduateCourseAbilityRepoService;
import edu.xzit.graduate.dao.service.IGraduateCourseRepoService;
import edu.xzit.graduate.dao.service.IGraduateCourseWeightRepoService;
import edu.xzit.graduate.dao.service.IGraduateExamPaperRepoService;
import edu.xzit.graduate.dao.service.IGraduateExamQuestionRepoService;
import edu.xzit.graduate.dao.service.IGraduateStudentScoreRepoService;
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
 * 毕业达成度 Controller
 *
 * @author system
 * @date 2025-01-22
 */
@RestController
@RequestMapping("/graduate/graduationAchievement")
public class GraduateGraduationAchievementController extends BaseController {

    @Autowired
    private IGraduateAbilityRepoService abilityRepoService;

    @Autowired
    private IGraduateCourseRepoService courseRepoService;

    @Autowired
    private IGraduateCourseAbilityRepoService courseAbilityRepoService;


    @Autowired
    private IGraduateExamQuestionRepoService examQuestionRepoService;

    @Autowired
    private IGraduateStudentScoreRepoService studentScoreRepoService;

    @Autowired
    private IGraduateCourseWeightRepoService courseWeightRepoService;

    @Autowired
    private IGraduateExamPaperRepoService examPaperRepoService;

    /**
     * 获取毕业达成度数据
     */
    @PreAuthorize("@ss.hasPermi('graduate:graduationAchievement:query')")
    @GetMapping("/{majorId}")
    public AjaxResult getGraduationAchievement(@PathVariable("majorId") Long majorId,
                                               @RequestParam(value = "grade", required = false) Integer grade) {
        try {
            // 1. 获取该专业的所有能力（用于前端展示父/子级），并单独取子级能力用于计算
            List<GraduateAbility> allAbilities = abilityRepoService.list(
                    Wrappers.lambdaQuery(GraduateAbility.class)
                            .eq(GraduateAbility::getMajorId, majorId)
                            .orderByAsc(GraduateAbility::getOrderNo)
            );

            if (allAbilities.isEmpty()) {
                return success(new HashMap<>());
            }

            // 子级能力：用于计算（有父级的能力）
            List<GraduateAbility> abilities = allAbilities.stream()
                    .filter(a -> a.getParentId() != null)
                    .collect(java.util.stream.Collectors.toList());

            // 2. 获取该专业的所有课程
            List<GraduateCourse> courses = courseRepoService.list(
                    Wrappers.lambdaQuery(GraduateCourse.class)
                            .eq(GraduateCourse::getMajorId, majorId)
                            .orderByAsc(GraduateCourse::getOrderNo)
            );

            if (courses.isEmpty()) {
                return success(new HashMap<>());
            }

            // 3. 获取课程-能力矩阵权重
            List<GraduateCourseAbility> courseAbilities = courseAbilityRepoService.list(
                    Wrappers.lambdaQuery(GraduateCourseAbility.class)
                            .in(GraduateCourseAbility::getCourseId, courses.stream().map(GraduateCourse::getId).collect(Collectors.toList()))
                            .in(GraduateCourseAbility::getAbilityId, abilities.stream().map(GraduateAbility::getId).collect(Collectors.toList()))
            );

            // 4. 获取课程ID列表（用于后续查询）
            List<Long> courseIds = courses.stream().map(GraduateCourse::getId).collect(Collectors.toList());

            // 5. 获取所有课程对应的试卷
            List<GraduateExamPaper> allPapers = new ArrayList<>();
            if (!courseIds.isEmpty()) {
                allPapers = examPaperRepoService.list(
                        Wrappers.lambdaQuery(GraduateExamPaper.class)
                                .in(GraduateExamPaper::getCourseId, courseIds)
                );
            }

            // 6. 获取课程权重数据
            List<GraduateCourseWeight> courseWeights = new ArrayList<>();
            if (!courseIds.isEmpty()) {
                courseWeights = courseWeightRepoService.list(
                        Wrappers.lambdaQuery(GraduateCourseWeight.class)
                                .in(GraduateCourseWeight::getCourseId, courseIds)
                );
            }

            // 7. 获取所有试卷题目
            List<GraduateExamQuestion> questions = new ArrayList<>();
            if (!allPapers.isEmpty()) {
                List<Long> paperIds = allPapers.stream().map(GraduateExamPaper::getId).collect(Collectors.toList());
                questions = examQuestionRepoService.list(
                        Wrappers.lambdaQuery(GraduateExamQuestion.class)
                                .in(GraduateExamQuestion::getPaperId, paperIds)
                );
            }

            // 6. 获取所有学生成绩
            List<GraduateStudentScore> scores = new ArrayList<>();
            if (!questions.isEmpty()) {
                List<Long> questionIds = questions.stream().map(GraduateExamQuestion::getId).collect(Collectors.toList());
                scores = studentScoreRepoService.list(
                        Wrappers.lambdaQuery(GraduateStudentScore.class)
                                .in(GraduateStudentScore::getQuestionId, questionIds)
                );
            }

            // 7. 计算每个能力的毕业达成度
            Map<String, Object> result = new HashMap<>();
            Map<Long, Object> achievements = new HashMap<>();

            for (GraduateAbility ability : abilities) {
                Map<String, Object> abilityData = new HashMap<>();
                Map<Long, Double> courseAchievements = new HashMap<>();
                Map<Long, Double> weights = new HashMap<>();

                // 计算每个课程对该能力的达成度
                for (GraduateCourse course : courses) {
                    // 获取权重
                    GraduateCourseAbility courseAbility = courseAbilities.stream()
                            .filter(ca -> ca.getCourseId().equals(course.getId()) && ca.getAbilityId().equals(ability.getId()))
                            .findFirst()
                            .orElse(null);

                    double weight = courseAbility != null ? courseAbility.getLevel().doubleValue() : 0.0;
                    weights.put(course.getId(), weight);

                    if (weight > 0) {
                        // 计算该课程对该能力的达成度（使用新的按试卷类型计算的方法）
                        double courseAchievement = calculateCourseAbilityAchievementByPapers(
                                course.getId(), ability.getId(), allPapers, courseWeights, questions, scores);
                        courseAchievements.put(course.getId(), courseAchievement);
                    } else {
                        courseAchievements.put(course.getId(), 0.0);
                    }
                }

                // 计算毕业达成度：加权平均
                double graduationAchievement = 0.0;
                double totalWeight = 0.0;

                for (GraduateCourse course : courses) {
                    double weight = weights.get(course.getId());
                    double achievement = courseAchievements.get(course.getId());

                    if (weight > 0) {
                        graduationAchievement += achievement * weight / 100.0;
                        totalWeight += weight;
                    }
                }

                if (totalWeight > 0) {
                    graduationAchievement = graduationAchievement / (totalWeight / 100.0);
                }

                abilityData.put("courseAchievements", courseAchievements);
                abilityData.put("weights", weights);
                abilityData.put("graduationAchievement", graduationAchievement);

                achievements.put(ability.getId(), abilityData);
            }

            // 返回全部能力（包含父/子），前端自行组装父子展示
            result.put("abilities", allAbilities);
            result.put("courses", courses);
            result.put("achievements", achievements);

            return success(result);
        } catch (Exception e) {
            return error("获取毕业达成度数据失败：" + e.getMessage());
        }
    }

    /**
     * 计算课程对特定能力的达成度
     */
    private double calculateCourseAbilityAchievement(Long courseId, Long abilityId,
                                                     List<GraduateExamQuestion> questions,
                                                     List<GraduateStudentScore> scores) {
        try {
            // 获取该课程下该能力对应的课程能力关系
            GraduateCourseAbility courseAbility = courseAbilityRepoService.getOne(
                    Wrappers.lambdaQuery(GraduateCourseAbility.class)
                            .eq(GraduateCourseAbility::getCourseId, courseId)
                            .eq(GraduateCourseAbility::getAbilityId, abilityId)
                            .last("LIMIT 1")
            );

            if (courseAbility == null) {
                return 0.0;
            }

            // 获取该能力对应的题目（题目中的objective_id现在存储的是graduate_ability的id）
            List<GraduateExamQuestion> relatedQuestions = questions.stream()
                    .filter(q -> abilityId.equals(q.getAbilityId()))
                    .collect(Collectors.toList());

            if (relatedQuestions.isEmpty()) {
                return 0.0;
            }

            // 计算总分值
            BigDecimal totalScore = relatedQuestions.stream()
                    .map(GraduateExamQuestion::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalScore.compareTo(BigDecimal.ZERO) <= 0) {
                return 0.0;
            }

            // 计算平均得分
            BigDecimal totalStudentScore = BigDecimal.ZERO;
            int studentCount = 0;

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
                    studentCount = Math.max(studentCount, questionScores.size());
                }
            }

            // 计算达成度
            if (studentCount > 0) {
                BigDecimal averageScore = totalStudentScore.divide(BigDecimal.valueOf(studentCount), 4, RoundingMode.HALF_UP);
                return averageScore.divide(totalScore, 4, RoundingMode.HALF_UP).doubleValue();
            }

            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 计算单张试卷对特定能力的达成度
     */
    private double calculatePaperAbilityAchievement(Long paperId, Long abilityId,
                                                   List<GraduateExamQuestion> questions,
                                                   List<GraduateStudentScore> scores) {
        try {
            // 获取该试卷下该能力对应的题目
            List<GraduateExamQuestion> relatedQuestions = questions.stream()
                    .filter(q -> q.getPaperId().equals(paperId) && abilityId.equals(q.getAbilityId()))
                    .collect(Collectors.toList());

            if (relatedQuestions.isEmpty()) {
                return 0.0;
            }

            // 计算总分值
            BigDecimal totalScore = relatedQuestions.stream()
                    .map(GraduateExamQuestion::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalScore.compareTo(BigDecimal.ZERO) <= 0) {
                return 0.0;
            }

            // 计算平均得分
            BigDecimal totalStudentScore = BigDecimal.ZERO;
            int studentCount = 0;

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
                    studentCount = Math.max(studentCount, questionScores.size());
                }
            }

            // 计算达成度
            if (studentCount > 0) {
                BigDecimal averageScore = totalStudentScore.divide(BigDecimal.valueOf(studentCount), 4, RoundingMode.HALF_UP);
                return averageScore.divide(totalScore, 4, RoundingMode.HALF_UP).doubleValue();
            }

            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 根据试卷类型计算课程对特定能力的达成度
     */
    private double calculateCourseAbilityAchievementByPapers(Long courseId, Long abilityId,
                                                           List<GraduateExamPaper> allPapers,
                                                           List<GraduateCourseWeight> courseWeights,
                                                           List<GraduateExamQuestion> questions,
                                                           List<GraduateStudentScore> scores) {
        try {
            // 获取该课程的所有试卷
            List<GraduateExamPaper> coursePapers = allPapers.stream()
                    .filter(p -> p.getCourseId().equals(courseId))
                    .collect(Collectors.toList());

            if (coursePapers.isEmpty()) {
                return 0.0;
            }

            // 获取该课程的权重配置
            GraduateCourseWeight courseWeight = courseWeights.stream()
                    .filter(cw -> cw.getCourseId().equals(courseId))
                    .findFirst()
                    .orElse(null);

            if (courseWeight == null) {
                return 0.0;
            }

            // 分别计算三种试卷类型的达成度
            double finalAchievement = 0.0;  // 期末试卷达成度
            double perfAchievement = 0.0;   // 平时测验达成度
            double labAchievement = 0.0;    // 实验达成度

            // 计算期末试卷达成度 (typeId = 1)
            GraduateExamPaper finalPaper = coursePapers.stream()
                    .filter(p -> p.getTypeId() != null && p.getTypeId().equals(1L))
                    .findFirst()
                    .orElse(null);
            if (finalPaper != null) {
                finalAchievement = calculatePaperAbilityAchievement(finalPaper.getId(), abilityId, questions, scores);
            }

            // 计算平时测验达成度 (typeId = 2)
            GraduateExamPaper perfPaper = coursePapers.stream()
                    .filter(p -> p.getTypeId() != null && p.getTypeId().equals(2L))
                    .findFirst()
                    .orElse(null);
            if (perfPaper != null) {
                perfAchievement = calculatePaperAbilityAchievement(perfPaper.getId(), abilityId, questions, scores);
            }

            // 计算实验达成度 (typeId = 3)
            GraduateExamPaper labPaper = coursePapers.stream()
                    .filter(p -> p.getTypeId() != null && p.getTypeId().equals(3L))
                    .findFirst()
                    .orElse(null);
            if (labPaper != null) {
                labAchievement = calculatePaperAbilityAchievement(labPaper.getId(), abilityId, questions, scores);
            }

            // 获取权重
            BigDecimal finalWeight = courseWeight.getFinalW() != null ? courseWeight.getFinalW() : BigDecimal.ZERO;
            BigDecimal perfWeight = courseWeight.getPerfW() != null ? courseWeight.getPerfW() : BigDecimal.ZERO;
            BigDecimal labWeight = courseWeight.getLabW() != null ? courseWeight.getLabW() : BigDecimal.ZERO;

            // 计算加权平均达成度
            double weightedSum = finalAchievement * finalWeight.doubleValue() +
                               perfAchievement * perfWeight.doubleValue() +
                               labAchievement * labWeight.doubleValue();

            double totalWeight = finalWeight.add(perfWeight).add(labWeight).doubleValue();

            if (totalWeight > 0) {
                return weightedSum / totalWeight;
            }

            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 导出毕业达成度数据
     */
    @PreAuthorize("@ss.hasPermi('graduate:graduationAchievement:export')")
    @Log(title = "毕业达成度", businessType = BusinessType.EXPORT)
    @GetMapping("/export/{majorId}")
    public void export(HttpServletResponse response, @PathVariable("majorId") Long majorId,
                       @RequestParam(value = "grade", required = false) Integer grade) {
        XSSFWorkbook workbook = null;
        try {
            // 获取数据
            AjaxResult result = getGraduationAchievement(majorId, grade);
            if (!result.get("code").equals(200)) {
                logger.error("获取毕业达成度数据失败，majorId: {}, grade: {}", majorId, grade);
                return;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            
            @SuppressWarnings("unchecked")
            List<GraduateAbility> allAbilities = (List<GraduateAbility>) data.get("abilities");
            @SuppressWarnings("unchecked")
            List<GraduateCourse> courses = (List<GraduateCourse>) data.get("courses");
            @SuppressWarnings("unchecked")
            Map<Long, Object> achievements = (Map<Long, Object>) data.get("achievements");

            // 构建父级能力映射
            Map<Long, GraduateAbility> parentMap = new HashMap<>();
            for (GraduateAbility ability : allAbilities) {
                if (ability.getParentId() == null) {
                    parentMap.put(ability.getId(), ability);
                }
            }

            // 只导出子级能力（与前端显示逻辑一致）
            List<GraduateAbility> childAbilities = allAbilities.stream()
                    .filter(a -> a.getParentId() != null && parentMap.containsKey(a.getParentId()))
                    .collect(Collectors.toList());

            // 按父级分组后排序（按父级orderNo排序，与前端逻辑一致）
            childAbilities.sort((x, y) -> {
                GraduateAbility xParent = parentMap.get(x.getParentId());
                GraduateAbility yParent = parentMap.get(y.getParentId());
                if (xParent == null || yParent == null) return 0;
                
                // 先按父级的orderNo排序
                Integer xParentOrderNo = xParent.getOrderNo() != null ? xParent.getOrderNo() : 0;
                Integer yParentOrderNo = yParent.getOrderNo() != null ? yParent.getOrderNo() : 0;
                if (!xParentOrderNo.equals(yParentOrderNo)) {
                    return xParentOrderNo - yParentOrderNo;
                }
                // 如果父级orderNo相同，按父级ID排序（保持稳定排序）
                if (!xParent.getId().equals(yParent.getId())) {
                    return Long.compare(xParent.getId(), yParent.getId());
                }
                
                // 同一父级下，按子级的orderNo排序
                Integer xOrder = x.getOrderNo() != null ? x.getOrderNo() : 0;
                Integer yOrder = y.getOrderNo() != null ? y.getOrderNo() : 0;
                if (!xOrder.equals(yOrder)) return xOrder - yOrder;
                
                // 如果orderNo相同，按ID排序（保持稳定排序）
                return Long.compare(x.getId(), y.getId());
            });

            // 设置响应头（参考ExcelUtil的结构）
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = "毕业达成度数据_" + System.currentTimeMillis() + ".xlsx";
            response.setHeader("Content-disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            
            // 创建Excel工作簿
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("毕业达成度数据");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            int colIndex = 0;
            headerRow.createCell(colIndex++).setCellValue("专业毕业要求");
            headerRow.createCell(colIndex++).setCellValue("指标点");
            headerRow.createCell(colIndex++).setCellValue("毕业达成度");
            
            // 添加课程列标题
            for (GraduateCourse course : courses) {
                headerRow.createCell(colIndex++).setCellValue(course.getName());
            }
            
            // 填充数据，并记录父级合并信息
            int rowIndex = 1;
            Map<Long, Integer> parentStartRowMap = new HashMap<>(); // 记录每个父级开始的行号
            Map<Long, Integer> parentRowCountMap = new HashMap<>(); // 记录每个父级的行数
            
            // 先统计每个父级的行数
            for (GraduateAbility ability : childAbilities) {
                Long parentId = ability.getParentId();
                parentRowCountMap.put(parentId, parentRowCountMap.getOrDefault(parentId, 0) + 1);
            }
            
            // 填充数据
            for (GraduateAbility ability : childAbilities) {
                Row dataRow = sheet.createRow(rowIndex);
                colIndex = 0;
                
                // 获取父级信息
                GraduateAbility parent = parentMap.get(ability.getParentId());
                String parentName = parent != null ? parent.getName() : "";
                String parentRemark = parent != null && parent.getRemark() != null ? parent.getRemark() : "";
                String abilityName = ability.getName();
                String abilityRemark = ability.getRemark() != null ? ability.getRemark() : "";
                
                Long parentId = ability.getParentId();
                
                // 记录父级开始行号（第一次遇到该父级时）
                if (!parentStartRowMap.containsKey(parentId)) {
                    parentStartRowMap.put(parentId, rowIndex);
                }
                
                // 专业毕业要求列（父级名称 + 描述）- 只在第一行创建，后续行用于合并
                if (parentStartRowMap.get(parentId).equals(rowIndex)) {
                    dataRow.createCell(colIndex).setCellValue(parentName + (parentRemark.isEmpty() ? "" : "\n" + parentRemark));
                } else {
                    dataRow.createCell(colIndex); // 创建空单元格用于合并
                }
                colIndex++;
                
                // 指标点列（能力名称 + 描述）
                dataRow.createCell(colIndex++).setCellValue(abilityName + (abilityRemark.isEmpty() ? "" : "\n" + abilityRemark));
                
                // 毕业达成度
                @SuppressWarnings("unchecked")
                Map<String, Object> abilityData = (Map<String, Object>) achievements.get(ability.getId());
                double graduationAchievement = 0.0;
                if (abilityData != null) {
                    Object graduationAchievementObj = abilityData.get("graduationAchievement");
                    if (graduationAchievementObj != null) {
                        graduationAchievement = ((Number) graduationAchievementObj).doubleValue();
                    }
                }
                dataRow.createCell(colIndex++).setCellValue(String.format("%.1f%%", graduationAchievement * 100));
                
                // 添加课程数据
                @SuppressWarnings("unchecked")
                Map<Long, Double> courseAchievements = abilityData != null ? 
                    (Map<Long, Double>) abilityData.get("courseAchievements") : new HashMap<>();
                @SuppressWarnings("unchecked")
                Map<Long, Double> weights = abilityData != null ? 
                    (Map<Long, Double>) abilityData.get("weights") : new HashMap<>();
                
                for (GraduateCourse course : courses) {
                    double achievement = courseAchievements.getOrDefault(course.getId(), 0.0);
                    double weight = weights.getOrDefault(course.getId(), 0.0);
                    
                    // 课程列显示：达成度 + 权重
                    String courseData = String.format("%.1f%%\n权重: %.1f%%", 
                        achievement * 100, weight);
                    dataRow.createCell(colIndex++).setCellValue(courseData);
                }
                
                rowIndex++;
            }
            
            // 合并父级单元格（第一列：专业毕业要求）
            for (Map.Entry<Long, Integer> entry : parentStartRowMap.entrySet()) {
                Long parentId = entry.getKey();
                int startRow = entry.getValue();
                int rowCount = parentRowCountMap.getOrDefault(parentId, 1);
                
                if (rowCount > 1) {
                    // 合并第一列（专业毕业要求列）的单元格
                    CellRangeAddress region = new CellRangeAddress(startRow, startRow + rowCount - 1, 0, 0);
                    sheet.addMergedRegion(region);
                    
                    // 设置合并单元格的样式（垂直居中、自动换行）
                    Row mergedRow = sheet.getRow(startRow);
                    if (mergedRow != null) {
                        Cell mergedCell = mergedRow.getCell(0);
                        if (mergedCell != null) {
                            CellStyle cellStyle = workbook.createCellStyle();
                            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                            cellStyle.setWrapText(true);
                            mergedCell.setCellStyle(cellStyle);
                        }
                    }
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < colIndex; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 写入响应（参考ExcelUtil的结构）
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            logger.error("导出数据异常", e);
        } finally {
            // 使用IOUtils安全关闭资源（参考ExcelUtil的结构）
            IOUtils.closeQuietly(workbook);
        }
    }
}
