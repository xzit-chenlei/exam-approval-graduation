package edu.xzit.graduate.controller;

import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.graduate.dao.domain.GraduateAbility;
import edu.xzit.graduate.dao.domain.GraduateCourse;
import edu.xzit.graduate.dao.domain.GraduateCourseAbility;
import edu.xzit.graduate.service.CourseAbilityService;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/graduate/matrix")
public class CourseAbilityController extends BaseController {

    @Autowired
    private CourseAbilityService matrixService;

    /** 查询课程×能力矩阵 */
    @PreAuthorize("@ss.hasPermi('graduate:matrix:query')")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam Long majorId, @RequestParam Integer grade) {
        return success(matrixService.getMatrixByMajor(majorId, grade));
    }

    /** 新增行：课程 */
    @PreAuthorize("@ss.hasPermi('graduate:matrix:addRow')")
    @Log(title = "毕业达成度矩阵-新增课程", businessType = BusinessType.INSERT)
    @PostMapping("/row")
    public AjaxResult addRow(@RequestBody GraduateCourse course) {
        return success(matrixService.addCourse(course));
    }

    /** 修改行：课程 */
    @PreAuthorize("@ss.hasPermi('graduate:matrix:editRow')")
    @Log(title = "毕业达成度矩阵-修改课程", businessType = BusinessType.UPDATE)
    @PutMapping("/row")
    public AjaxResult editRow(@RequestBody GraduateCourse course) {
        matrixService.updateCourse(course);
        return success();
    }

    /** 删除行：课程 */
    @PreAuthorize("@ss.hasPermi('graduate:matrix:removeRow')")
    @Log(title = "毕业达成度矩阵-删除课程", businessType = BusinessType.DELETE)
    @DeleteMapping("/row/{courseId}")
    public AjaxResult removeRow(@PathVariable Long courseId) {
        matrixService.deleteCourse(courseId);
        return success();
    }

    /** 新增列：能力 */
    @PreAuthorize("@ss.hasPermi('graduate:matrix:addCol')")
    @Log(title = "毕业达成度矩阵-新增能力", businessType = BusinessType.INSERT)
    @PostMapping("/col")
    public AjaxResult addCol(@RequestBody GraduateAbility ability) {
        return success(matrixService.addAbility(ability));
    }

    /** 修改列：能力 */
    @PreAuthorize("@ss.hasPermi('graduate:matrix:editCol')")
    @Log(title = "毕业达成度矩阵-修改能力", businessType = BusinessType.UPDATE)
    @PutMapping("/col")
    public AjaxResult editCol(@RequestBody GraduateAbility ability) {
        matrixService.updateAbility(ability);
        return success();
    }

    /** 删除列：能力 */
    @PreAuthorize("@ss.hasPermi('graduate:matrix:removeCol')")
    @Log(title = "毕业达成度矩阵-删除能力", businessType = BusinessType.DELETE)
    @DeleteMapping("/col/{abilityId}")
    public AjaxResult removeCol(@PathVariable Long abilityId) {
        matrixService.deleteAbility(abilityId);
        return success();
    }

    /** 设置或更新单元格 */
    @PreAuthorize("@ss.hasPermi('graduate:matrix:editCell')")
    @Log(title = "毕业达成度矩阵-设置单元格", businessType = BusinessType.UPDATE)
    @PostMapping("/cell")
    public AjaxResult upsertCell(@RequestBody CellDTO dto) {
        matrixService.upsertCell(dto.getCourseId(), dto.getAbilityId(), dto.getGrade(), dto.getLevel(), dto.getWeight(), dto.getRemark());
        return success();
    }

    /** 清除单元格 */
    @PreAuthorize("@ss.hasPermi('graduate:matrix:clearCell')")
    @Log(title = "毕业达成度矩阵-清除单元格", businessType = BusinessType.DELETE)
    @DeleteMapping("/cell")
    public AjaxResult clearCell(@RequestParam Long courseId, @RequestParam Long abilityId) {
        matrixService.clearCell(courseId, abilityId);
        return success();
    }

    /**
     * 导出Excel模板
     * 生成包含课程名称和能力名称的Excel模板，用户可以在Excel中填写数据后导入
     */
    @PreAuthorize("@ss.hasPermi('graduate:matrix:export')")
    @GetMapping("/exportTemplate")
    public void exportTemplate(@RequestParam Long majorId, @RequestParam Integer grade, HttpServletResponse response) {
        Workbook workbook = null;
        ServletOutputStream out = null;
        try {
            // 1. 获取矩阵数据
            CourseAbilityService.MatrixResult matrixResult = matrixService.getMatrixByMajor(majorId, grade);
            List<GraduateCourse> courses = matrixResult.getCourses();
            List<GraduateAbility> abilities = matrixResult.getAbilities();
            Map<String, GraduateCourseAbility> cells = matrixResult.getCells();

            // 2. 分离父级和子级能力
            List<GraduateAbility> parentAbilities = abilities.stream()
                    .filter(a -> a.getParentId() == null)
                    .sorted(Comparator.comparing(GraduateAbility::getOrderNo, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(GraduateAbility::getId))
                    .collect(Collectors.toList());
            
            Map<Long, List<GraduateAbility>> childrenMap = abilities.stream()
                    .filter(a -> a.getParentId() != null)
                    .collect(Collectors.groupingBy(
                            GraduateAbility::getParentId,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    list -> list.stream()
                                            .sorted(Comparator.comparing(GraduateAbility::getOrderNo, Comparator.nullsLast(Integer::compareTo))
                                                    .thenComparing(GraduateAbility::getId))
                                            .collect(Collectors.toList())
                            )
                    ));

            // 3. 构建扁平化的能力列表（用于列标题）
            List<AbilityColumnInfo> abilityColumns = new ArrayList<>();
            for (GraduateAbility parent : parentAbilities) {
                List<GraduateAbility> children = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
                if (children.isEmpty()) {
                    // 没有子能力，父能力作为单列
                    abilityColumns.add(new AbilityColumnInfo(parent.getId(), parent.getName(), null, 1));
                } else {
                    // 有子能力，子能力作为列
                    for (GraduateAbility child : children) {
                        abilityColumns.add(new AbilityColumnInfo(child.getId(), child.getName(), parent.getName(), 1));
                    }
                }
            }

            // 4. 创建Excel工作簿
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("课程能力矩阵");

            // 5. 创建样式
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
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);

            // 6. 创建表头行
            Row headerRow = sheet.createRow(0);
            Cell courseHeaderCell = headerRow.createCell(0);
            courseHeaderCell.setCellValue("课程名称");
            courseHeaderCell.setCellStyle(headerStyle);

            // 创建能力列标题
            for (int i = 0; i < abilityColumns.size(); i++) {
                Cell cell = headerRow.createCell(i + 1);
                AbilityColumnInfo colInfo = abilityColumns.get(i);
                cell.setCellValue(colInfo.getDisplayName());
                cell.setCellStyle(headerStyle);
            }

            // 7. 创建数据行
            for (int rowIdx = 0; rowIdx < courses.size(); rowIdx++) {
                GraduateCourse course = courses.get(rowIdx);
                Row dataRow = sheet.createRow(rowIdx + 1);
                
                // 课程名称列
                Cell courseCell = dataRow.createCell(0);
                courseCell.setCellValue(course.getName());
                courseCell.setCellStyle(dataStyle);

                // 能力数据列
                for (int colIdx = 0; colIdx < abilityColumns.size(); colIdx++) {
                    AbilityColumnInfo colInfo = abilityColumns.get(colIdx);
                    Cell dataCell = dataRow.createCell(colIdx + 1);
                    dataCell.setCellStyle(dataStyle);
                    
                    // 从cells中获取现有值
                    String key = course.getId() + "_" + colInfo.getAbilityId();
                    GraduateCourseAbility cell = cells.get(key);
                    if (cell != null && cell.getLevel() != null) {
                        dataCell.setCellValue(cell.getLevel().doubleValue());
                    } else {
                        // 空单元格，用户可以填写
                        dataCell.setCellValue("");
                    }
                }
            }

            // 8. 设置列宽
            sheet.setColumnWidth(0, 4000); // 课程名称列
            for (int i = 0; i < abilityColumns.size(); i++) {
                sheet.setColumnWidth(i + 1, 3000);
            }

            // 9. 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = "课程能力矩阵模板_" + majorId + "_" + grade + ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            // 10. 写入响应流
            out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } catch (Exception e) {
            logger.error("导出Excel模板失败", e);
        } finally {
            IOUtils.closeQuietly(workbook);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * 导入Excel数据
     * 读取Excel文件，批量保存单元格数据
     */
    @PreAuthorize("@ss.hasPermi('graduate:matrix:import')")
    @Log(title = "毕业达成度矩阵-导入数据", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult importExcel(@RequestParam("file") MultipartFile file, 
                                   @RequestParam Long majorId, 
                                   @RequestParam Integer grade) {
        Workbook workbook = null;
        try {
            // 1. 读取Excel文件
            workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null) {
                return error("Excel文件格式错误：找不到工作表");
            }

            // 2. 获取矩阵数据，用于验证课程和能力
            CourseAbilityService.MatrixResult matrixResult = matrixService.getMatrixByMajor(majorId, grade);
            List<GraduateCourse> courses = matrixResult.getCourses();
            List<GraduateAbility> abilities = matrixResult.getAbilities();

            // 3. 构建课程名称到ID的映射
            Map<String, Long> courseNameToId = courses.stream()
                    .collect(Collectors.toMap(GraduateCourse::getName, GraduateCourse::getId, (a, b) -> a));

            // 4. 读取表头，构建能力名称到ID的映射
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return error("Excel文件格式错误：找不到表头行");
            }

            Map<Integer, Long> columnIndexToAbilityId = new HashMap<>();
            for (int colIdx = 1; colIdx < headerRow.getLastCellNum(); colIdx++) {
                Cell cell = headerRow.getCell(colIdx);
                if (cell != null) {
                    String abilityName = getCellStringValue(cell);
                    if (abilityName != null && !abilityName.trim().isEmpty()) {
                        // 查找匹配的能力（支持父子层级显示格式）
                        GraduateAbility matchedAbility = abilities.stream()
                                .filter(a -> abilityName.equals(a.getName()) || 
                                           abilityName.contains(a.getName()))
                                .findFirst()
                                .orElse(null);
                        if (matchedAbility != null) {
                            columnIndexToAbilityId.put(colIdx, matchedAbility.getId());
                        }
                    }
                }
            }

            // 5. 读取数据行并批量保存
            List<Map<String, Object>> importResults = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                final int currentRowIdx = rowIdx; // 创建final变量供lambda使用
                Row dataRow = sheet.getRow(rowIdx);
                if (dataRow == null) continue;

                // 获取课程名称
                Cell courseCell = dataRow.getCell(0);
                if (courseCell == null) continue;

                String courseName = getCellStringValue(courseCell);
                if (courseName == null || courseName.trim().isEmpty()) continue;

                Long courseId = courseNameToId.get(courseName.trim());
                if (courseId == null) {
                    failCount++;
                    Map<String, Object> errorDetail = new HashMap<>();
                    errorDetail.put("row", currentRowIdx + 1);
                    errorDetail.put("course", courseName);
                    errorDetail.put("status", "失败");
                    errorDetail.put("message", "课程不存在");
                    importResults.add(errorDetail);
                    continue;
                }

                // 读取该行的所有能力数据
                for (Map.Entry<Integer, Long> entry : columnIndexToAbilityId.entrySet()) {
                    int colIdx = entry.getKey();
                    Long abilityId = entry.getValue();
                    Cell dataCell = dataRow.getCell(colIdx);
                    
                    if (dataCell == null) continue;

                    String cellValue = getCellStringValue(dataCell);
                    if (cellValue == null || cellValue.trim().isEmpty()) {
                        // 空值，跳过（不更新）
                        continue;
                    }

                    try {
                        BigDecimal level = new BigDecimal(cellValue.trim());
                        
                        // 验证范围（0-100）
                        if (level.compareTo(BigDecimal.ZERO) < 0 || level.compareTo(new BigDecimal("100")) > 0) {
                            failCount++;
                            String abilityName = abilities.stream()
                                    .filter(a -> a.getId().equals(abilityId))
                                    .findFirst()
                                    .map(GraduateAbility::getName)
                                    .orElse("未知");
                            Map<String, Object> errorDetail = new HashMap<>();
                            errorDetail.put("row", currentRowIdx + 1);
                            errorDetail.put("course", courseName);
                            errorDetail.put("ability", abilityName);
                            errorDetail.put("status", "失败");
                            errorDetail.put("message", "数值超出范围（0-100）");
                            importResults.add(errorDetail);
                            continue;
                        }

                        // 保存单元格数据
                        matrixService.upsertCell(courseId, abilityId, grade, level, null, null);
                        successCount++;
                    } catch (NumberFormatException e) {
                        failCount++;
                        String abilityName = abilities.stream()
                                .filter(a -> a.getId().equals(abilityId))
                                .findFirst()
                                .map(GraduateAbility::getName)
                                .orElse("未知");
                        Map<String, Object> errorDetail = new HashMap<>();
                        errorDetail.put("row", currentRowIdx + 1);
                        errorDetail.put("course", courseName);
                        errorDetail.put("ability", abilityName);
                        errorDetail.put("status", "失败");
                        errorDetail.put("message", "数值格式错误：" + cellValue);
                        importResults.add(errorDetail);
                    }
                }
            }

            workbook.close();

            // 6. 返回导入结果
            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("totalCount", successCount + failCount);
            result.put("details", importResults);

            if (failCount > 0) {
                return AjaxResult.error("导入完成，成功：" + successCount + "条，失败：" + failCount + "条", result);
            } else {
                return AjaxResult.success("导入成功，共导入 " + successCount + " 条数据", result);
            }
        } catch (Exception e) {
            logger.error("导入Excel数据失败", e);
            return error("导入失败：" + e.getMessage());
        } finally {
            IOUtils.closeQuietly(workbook);
        }
    }

    /**
     * 获取单元格的字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * 能力列信息内部类
     */
    @Data
    private static class AbilityColumnInfo {
        private Long abilityId;
        private String displayName;
        private String parentName;
        private int colspan;

        AbilityColumnInfo(Long abilityId, String displayName, String parentName, int colspan) {
            this.abilityId = abilityId;
            this.displayName = displayName;
            this.parentName = parentName;
            this.colspan = colspan;
        }
    }

    @Data
    public static class CellDTO {
        private Long courseId;
        private Long abilityId;
        private Integer grade;
        private BigDecimal level;
        private BigDecimal weight;
        private String remark;
    }
}

