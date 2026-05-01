package edu.xzit.graduate.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.graduate.dao.domain.GraduateClass;
import edu.xzit.graduate.dao.domain.GraduateStudentScore;
import edu.xzit.graduate.dao.domain.GraduateExamQuestion;
import edu.xzit.graduate.dao.domain.GraduateStudentInfo;
import edu.xzit.graduate.dao.domain.GraduateExamPaper;
import edu.xzit.graduate.dao.domain.GraduateCourse;
import edu.xzit.graduate.dao.service.IGraduateClassRepoService;
import edu.xzit.graduate.dao.service.IGraduateStudentScoreRepoService;
import edu.xzit.graduate.dao.service.IGraduateExamQuestionRepoService;
import edu.xzit.graduate.dao.service.IGraduateStudentInfoRepoService;
import edu.xzit.graduate.dao.service.IGraduateExamPaperRepoService;
import edu.xzit.graduate.dao.service.IGraduateCourseRepoService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.util.IOUtils;
import javax.servlet.ServletOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 学生成绩表 Controller
 *
 * @author chenlei
 * @date 2025-01-27
 */
@RestController
@RequestMapping("/graduate/studentScore")
public class GraduateStudentScoreController extends BaseController {
    @Autowired
    private IGraduateStudentScoreRepoService graduateStudentScoreService;

    @Autowired
    private IGraduateExamQuestionRepoService graduateExamQuestionService;

    @Autowired
    private IGraduateStudentInfoRepoService graduateStudentInfoService;

    @Autowired
    private IGraduateClassRepoService graduateClassService;

    @Autowired
    private IGraduateExamPaperRepoService graduateExamPaperService;

    @Autowired
    private IGraduateCourseRepoService graduateCourseService;

    /**
     * 查询学生成绩表列表（按试卷分组）
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentScore:list')")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam Long paperId) {
        // 1. 获取试卷的所有题目
        LambdaQueryWrapper<GraduateExamQuestion> questionWrapper = Wrappers.lambdaQuery();
        questionWrapper.eq(GraduateExamQuestion::getPaperId, paperId)
                      .orderByAsc(GraduateExamQuestion::getOrderNo);
        List<GraduateExamQuestion> questions = graduateExamQuestionService.list(questionWrapper);

        if (questions.isEmpty()) {
            return success(new ArrayList<>());
        }

        // 2. 获取该试卷已存在的成绩（只显示有成绩的学生）
        LambdaQueryWrapper<GraduateStudentScore> scoreWrapper = Wrappers.lambdaQuery();
        scoreWrapper.eq(GraduateStudentScore::getPaperId, paperId);
        List<GraduateStudentScore> scores = graduateStudentScoreService.list(scoreWrapper);

        if (scores.isEmpty()) {
            return success(new ArrayList<>()); // 初始化时为空
        }

        // 3. 按学生分组
        Map<Long, List<GraduateStudentScore>> studentIdToScores = new HashMap<>();
        for (GraduateStudentScore s : scores) {
            studentIdToScores.computeIfAbsent(s.getStudentId(), k -> new ArrayList<>()).add(s);
        }

        // 4. 拉取相关学生信息
        List<Long> studentIds = new ArrayList<>(studentIdToScores.keySet());
        List<GraduateStudentInfo> students = studentIds.isEmpty() ? new ArrayList<>() : graduateStudentInfoService.listByIds(studentIds);
        Map<Long, GraduateStudentInfo> idToStudent = new HashMap<>();
        for (GraduateStudentInfo st : students) {
            idToStudent.put(st.getId(), st);
        }

        // 5. 构建结果数据（仅包含已有成绩的学生）
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<Long, List<GraduateStudentScore>> entry : studentIdToScores.entrySet()) {
            Long studentId = entry.getKey();
            GraduateStudentInfo student = idToStudent.get(studentId);
            Map<String, Object> row = new HashMap<>();
            row.put("studentId", studentId);
            row.put("studentCode", student != null ? student.getStudentCode() : null);
            row.put("studentName", student != null ? student.getStudentName() : null);

            // 初始化每题分数字段
            for (GraduateExamQuestion question : questions) {
                String scoreKey = "score_" + question.getId();
                row.put(scoreKey, null);
            }

            // 填充分数
            for (GraduateStudentScore sc : entry.getValue()) {
                String scoreKey = "score_" + sc.getQuestionId();
                row.put(scoreKey, sc.getStudentScore());
            }

            result.add(row);
        }

        return success(result);
    }

    /**
     * 获取试卷题目信息
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentScore:query')")
    @GetMapping("/questions/{paperId}")
    public AjaxResult getQuestions(@PathVariable("paperId") Long paperId) {
        LambdaQueryWrapper<GraduateExamQuestion> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(GraduateExamQuestion::getPaperId, paperId)
               .orderByAsc(GraduateExamQuestion::getOrderNo);
        List<GraduateExamQuestion> questions = graduateExamQuestionService.list(wrapper);
        return success(questions);
    }

    /**
     * 批量保存学生成绩
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentScore:edit')")
    @Log(title = "学生成绩表", businessType = BusinessType.UPDATE)
    @PostMapping("/batchSave")
    public AjaxResult batchSave(@RequestBody Map<String, Object> data) {
        try {
            Long paperId = Long.valueOf(data.get("paperId").toString());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> scores = (List<Map<String, Object>>) data.get("scores");

            // 获取试卷题目信息
            LambdaQueryWrapper<GraduateExamQuestion> questionWrapper = Wrappers.lambdaQuery();
            questionWrapper.eq(GraduateExamQuestion::getPaperId, paperId);
            List<GraduateExamQuestion> questions = graduateExamQuestionService.list(questionWrapper);

            List<GraduateStudentScore> scoreList = new ArrayList<>();

            for (Map<String, Object> scoreData : scores) {
                Long studentId = Long.valueOf(scoreData.get("studentId").toString());

                for (GraduateExamQuestion question : questions) {
                    String scoreKey = "score_" + question.getId();
                    if (scoreData.containsKey(scoreKey) && scoreData.get(scoreKey) != null) {
                        BigDecimal studentScore = new BigDecimal(scoreData.get(scoreKey).toString());

                        // 计算得分率
                        BigDecimal scoreRate = BigDecimal.ZERO;
                        if (question.getScore().compareTo(BigDecimal.ZERO) > 0) {
                            scoreRate = studentScore.divide(question.getScore(), 4, RoundingMode.HALF_UP);
                        }

                        // 查找是否已存在该记录
                        LambdaQueryWrapper<GraduateStudentScore> existingWrapper = Wrappers.lambdaQuery();
                        existingWrapper.eq(GraduateStudentScore::getPaperId, paperId)
                                      .eq(GraduateStudentScore::getStudentId, studentId)
                                      .eq(GraduateStudentScore::getQuestionId, question.getId());
                        GraduateStudentScore existingScore = graduateStudentScoreService.getOne(existingWrapper);

                        if (existingScore != null) {
                            // 更新现有记录
                            existingScore.setStudentScore(studentScore);
                            existingScore.setScoreRate(scoreRate);
                            graduateStudentScoreService.updateById(existingScore);
                        } else {
                            // 创建新记录
                            GraduateStudentScore newScore = new GraduateStudentScore();
                            newScore.setPaperId(paperId);
                            newScore.setStudentId(studentId);
                            newScore.setQuestionId(question.getId());
                            newScore.setQuestionNo(question.getQuestionNo());
                            newScore.setQuestionScore(question.getScore());
                            newScore.setStudentScore(studentScore);
                            newScore.setScoreRate(scoreRate);

                            // 设置学生信息
                            GraduateStudentInfo student = graduateStudentInfoService.getById(studentId);
                            if (student != null) {
                                newScore.setStudentCode(student.getStudentCode());
                                newScore.setStudentName(student.getStudentName());
                            }

                            scoreList.add(newScore);
                        }
                    }
                }
            }

            if (!scoreList.isEmpty()) {
                graduateStudentScoreService.saveBatch(scoreList);
            }

            return success("保存成功");
        } catch (Exception e) {
            return error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 删除学生成绩
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentScore:remove')")
    @Log(title = "学生成绩表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(graduateStudentScoreService.removeByIds(Arrays.asList(ids)) ? 1 : 0);
    }

    /**
     * 下载成绩导入模板
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentScore:list')")
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(Long paperId, HttpServletResponse response) {
        Workbook workbook = null;
        ServletOutputStream out = null;
        try {
            // 1. 获取试卷的所有题目
            LambdaQueryWrapper<GraduateExamQuestion> questionWrapper = Wrappers.lambdaQuery();
            questionWrapper.eq(GraduateExamQuestion::getPaperId, paperId)
                          .orderByAsc(GraduateExamQuestion::getOrderNo);
            List<GraduateExamQuestion> questions = graduateExamQuestionService.list(questionWrapper);

            // 2. 根据paperId查询试卷信息，获取courseId
            GraduateExamPaper examPaper = graduateExamPaperService.getById(paperId);
            if (examPaper == null || examPaper.getCourseId() == null) {
                throw new RuntimeException("试卷不存在或未关联课程");
            }

            // 3. 根据courseId查询课程信息，获取majorId和grade
            GraduateCourse course = graduateCourseService.getById(examPaper.getCourseId());
            if (course == null) {
                throw new RuntimeException("课程不存在");
            }

            Long majorId = course.getMajorId();
            Integer grade = course.getGrade();
            if (majorId == null || grade == null) {
                throw new RuntimeException("课程信息不完整，缺少专业ID或年级信息");
            }

            // 4. 根据majorId和grade（对应enteringClass）查询班级列表
            LambdaQueryWrapper<GraduateClass> classWrapper = Wrappers.lambdaQuery();
            classWrapper.eq(GraduateClass::getGraduateMajorId, majorId)
                       .eq(GraduateClass::getEnteringClass, grade);
            List<GraduateClass> classes = graduateClassService.list(classWrapper);

            if (classes.isEmpty()) {
                throw new RuntimeException("未找到符合条件的班级");
            }

            // 5. 获取所有班级的ID列表
            List<Long> classIds = classes.stream()
                    .map(GraduateClass::getId)
                    .collect(Collectors.toList());

            // 6. 根据班级ID列表查询所有学生
            LambdaQueryWrapper<GraduateStudentInfo> studentWrapper = Wrappers.lambdaQuery();
            studentWrapper.in(GraduateStudentInfo::getClassId, classIds)
                         .orderByAsc(GraduateStudentInfo::getStudentCode);
            List<GraduateStudentInfo> students = graduateStudentInfoService.list(studentWrapper);

            // 7. 创建Excel表格标题行
            List<List<String>> rows = new ArrayList<>();

            // 添加标题行
            List<String> headers = new ArrayList<>();
            headers.add("学号");
            headers.add("学生姓名");
            headers.add("班级");

            for (GraduateExamQuestion question : questions) {
                headers.add("题目" + question.getQuestionNo() + "(满分:" + question.getScore() + ")");
            }
            rows.add(headers);

            // 8. 添加学生数据行
            Map<Long, String> classIdToNameMap = classes.stream()
                    .collect(Collectors.toMap(GraduateClass::getId, GraduateClass::getClassName));

            for (GraduateStudentInfo student : students) {
                List<String> studentRow = new ArrayList<>();
                studentRow.add(student.getStudentCode() != null ? student.getStudentCode().toString() : "");
                studentRow.add(student.getStudentName() != null ? student.getStudentName() : "");
                
                // 获取班级名称
                String className = "";
                if (student.getClassId() != null) {
                    className = classIdToNameMap.getOrDefault(student.getClassId(), 
                            student.getClassName() != null ? student.getClassName() : "");
                }
                studentRow.add(className);

                // 为每道题添加空列（用于填写成绩）
                for (int i = 0; i < questions.size(); i++) {
                    studentRow.add("");
                }
                rows.add(studentRow);
            }

            // 9. 导出Excel
            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = "score_template.xlsx";
            response.setHeader("Content-disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("成绩导入模板");

            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.createRow(i);
                List<String> rowData = rows.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(rowData.get(j));
                }
            }

            out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } catch (Exception e) {
            logger.error("下载成绩导入模板失败，paperId: {}", paperId, e);
        } finally {
            IOUtils.closeQuietly(workbook);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * 导入成绩数据
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentScore:edit')")
    @Log(title = "学生成绩表", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult importScores(@RequestParam("file") MultipartFile file, Long paperId) throws Exception {
        try {
            // 1. 获取试卷的所有题目
            LambdaQueryWrapper<GraduateExamQuestion> questionWrapper = Wrappers.lambdaQuery();
            questionWrapper.eq(GraduateExamQuestion::getPaperId, paperId)
                          .orderByAsc(GraduateExamQuestion::getOrderNo);
            List<GraduateExamQuestion> questions = graduateExamQuestionService.list(questionWrapper);

            if (questions.isEmpty()) {
                return error("该试卷没有题目信息");
            }

            // 2. 读取Excel文件
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            // 3. 获取表头行，确定题目列的位置
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> questionColumnMap = new HashMap<>();

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String headerName = cell.getStringCellValue();
                    // 查找题目列（格式：题目X(满分:Y)）
                    for (GraduateExamQuestion question : questions) {
                        String expectedHeader = "题目" + question.getQuestionNo() + "(满分:" + question.getScore() + ")";
                        if (expectedHeader.equals(headerName)) {
                            questionColumnMap.put(question.getId().toString(), i);
                            break;
                        }
                    }
                }
            }

            List<GraduateStudentScore> scoreList = new ArrayList<>();
            List<GraduateStudentScore> updateScoreList = new ArrayList<>();

            // 4. 读取数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell studentCodeCell = row.getCell(0); // 第二列是学号
                Cell studentNameCell = row.getCell(1); // 第三列是学生姓名
                Cell classNameCell = row.getCell(2); // 第四列是班级名称

                Long studentId = null;
                Long studentCode = null;
                String studentName = null;
                String className = null;
                Long classId = null;


                // 解析学号
                if (studentCodeCell != null) {
                    try {
                        if (studentCodeCell.getCellType() == CellType.NUMERIC) {
                            studentCode = (long) studentCodeCell.getNumericCellValue();
                        } else if (studentCodeCell.getCellType() == CellType.STRING) {
                            String codeStr = studentCodeCell.getStringCellValue().trim();
                            if (!codeStr.isEmpty()) {
                                studentCode = Long.parseLong(codeStr);
                            }
                        }
                    } catch (Exception e) {
                        // 忽略无法解析的学号
                    }
                }

                // 解析学生姓名
                if (studentNameCell != null && studentNameCell.getCellType() == CellType.STRING) {
                    studentName = studentNameCell.getStringCellValue().trim();
                }

                // 解析班级名称
                if (classNameCell != null && classNameCell.getCellType() == CellType.STRING) {
                    className = classNameCell.getStringCellValue().trim();
                    
                    // 根据班级名称查找班级ID
                    if (!className.isEmpty()) {
                        LambdaQueryWrapper<GraduateClass> classQueryWrapper = Wrappers.lambdaQuery();
                        classQueryWrapper.eq(GraduateClass::getClassName, className);
                        GraduateClass graduateClass = graduateClassService.getOne(classQueryWrapper);
                        if (graduateClass != null) {
                            classId = graduateClass.getId();
                        }
                    }
                }

                // 如果没有学生ID但有学号和姓名，尝试查找或创建学生
                if (studentId == null && studentCode != null && studentName != null && !studentName.isEmpty()) {
                    // 查找是否已存在相同学号的学生
                    LambdaQueryWrapper<GraduateStudentInfo> studentQuery = Wrappers.lambdaQuery();
                    studentQuery.eq(GraduateStudentInfo::getStudentCode, studentCode);
                    GraduateStudentInfo existingStudent = graduateStudentInfoService.getOne(studentQuery);

                    if (existingStudent != null) {
                        studentId = existingStudent.getId();
                        // 如果学生存在，更新班级信息（如果Excel中提供了班级信息）
                        if (classId != null && !classId.equals(existingStudent.getClassId())) {
                            existingStudent.setClassId(classId);
                            existingStudent.setClassName(className);
                            graduateStudentInfoService.updateById(existingStudent);
                        }
                    } else {
                        // 创建新学生
                        GraduateStudentInfo newStudent = new GraduateStudentInfo();
                        newStudent.setStudentCode(studentCode);
                        newStudent.setStudentName(studentName);
                        newStudent.setClassName(className);
                        newStudent.setClassId(classId);
                        newStudent.setIsValid(0L);
                        graduateStudentInfoService.save(newStudent);
                        studentId = newStudent.getId();
                    }
                }

                // 如果没有有效学生信息，跳过该行
                if (studentId == null) {
                    continue;
                }

                // 获取学生信息
                GraduateStudentInfo student = graduateStudentInfoService.getById(studentId);
                if (student == null) continue;

                // 为每道题创建或更新成绩记录
                for (GraduateExamQuestion question : questions) {
                    String questionIdStr = question.getId().toString();
                    Integer columnIndex = questionColumnMap.get(questionIdStr);

                    if (columnIndex != null) {
                        Cell scoreCell = row.getCell(columnIndex);
                        if (scoreCell != null) {
                            try {
                                BigDecimal studentScore;
                                if (scoreCell.getCellType() == CellType.NUMERIC) {
                                    studentScore = BigDecimal.valueOf(scoreCell.getNumericCellValue());
                                } else if (scoreCell.getCellType() == CellType.STRING) {
                                    String scoreStr = scoreCell.getStringCellValue().trim();
                                    if (scoreStr.isEmpty()) continue;
                                    studentScore = new BigDecimal(scoreStr);
                                } else {
                                    continue;
                                }

                                // 计算得分率
                                BigDecimal scoreRate = BigDecimal.ZERO;
                                if (question.getScore() != null && question.getScore().compareTo(BigDecimal.ZERO) > 0) {
                                    scoreRate = studentScore.divide(question.getScore(), 4, RoundingMode.HALF_UP);
                                }

                                // 查找是否已存在该学生的该题成绩
                                LambdaQueryWrapper<GraduateStudentScore> scoreQueryWrapper = Wrappers.lambdaQuery();
                                scoreQueryWrapper.eq(GraduateStudentScore::getPaperId, paperId)
                                                .eq(GraduateStudentScore::getStudentId, studentId)
                                                .eq(GraduateStudentScore::getQuestionId, question.getId());
                                GraduateStudentScore existingScore = graduateStudentScoreService.getOne(scoreQueryWrapper);

                                if (existingScore != null) {
                                    // 更新现有成绩
                                    existingScore.setStudentScore(studentScore);
                                    existingScore.setScoreRate(scoreRate);
                                    updateScoreList.add(existingScore);
                                } else {
                                    // 创建新成绩记录
                                    GraduateStudentScore score = new GraduateStudentScore();
                                    score.setPaperId(paperId);
                                    score.setStudentId(studentId);
                                    score.setStudentCode(student.getStudentCode());
                                    score.setStudentName(student.getStudentName());
                                    score.setQuestionId(question.getId());
                                    score.setQuestionNo(question.getQuestionNo());
                                    score.setQuestionScore(question.getScore());
                                    score.setStudentScore(studentScore);
                                    score.setScoreRate(scoreRate);
                                    score.setIsValid(0L);

                                    scoreList.add(score);
                                }
                            } catch (Exception e) {
                                // 忽略无法解析的成绩
                            }
                        }
                    }
                }
            }

            workbook.close();

            // 批量保存和更新成绩
            boolean success = true;
            int totalImported = 0;
            
            // 保存新成绩
            if (!scoreList.isEmpty()) {
                success = graduateStudentScoreService.saveBatch(scoreList);
                totalImported += scoreList.size();
            }
            
            // 更新现有成绩
            if (!updateScoreList.isEmpty()) {
                success = graduateStudentScoreService.updateBatchById(updateScoreList) && success;
                totalImported += updateScoreList.size();
            }

            if (success) {
                return success("成功导入/更新 " + totalImported + " 条成绩记录");
            } else {
                return error("成绩导入失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return error("成绩导入失败: " + e.getMessage());
        }
    }

    /**
     * 按试卷+学生 移除该学生在该试卷下的所有成绩
     */
    @PreAuthorize("@ss.hasPermi('graduate:studentScore:remove')")
    @Log(title = "学生成绩表", businessType = BusinessType.DELETE)
    @DeleteMapping("/paper/{paperId}/student/{studentId}")
    public AjaxResult removeByPaperAndStudent(@PathVariable Long paperId, @PathVariable Long studentId) {
        LambdaQueryWrapper<GraduateStudentScore> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(GraduateStudentScore::getPaperId, paperId)
               .eq(GraduateStudentScore::getStudentId, studentId);
        boolean ok = graduateStudentScoreService.remove(wrapper);
        return toAjax(ok ? 1 : 0);
    }
}
