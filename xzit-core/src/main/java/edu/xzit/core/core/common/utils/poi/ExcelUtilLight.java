package edu.xzit.core.core.common.utils.poi;

import cn.hutool.core.util.StrUtil;
import edu.xzit.core.core.common.exception.ServiceException;
import edu.xzit.core.core.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

@Slf4j
public class ExcelUtilLight {

    public static ExcelData readExcel(InputStream inputStream) {
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(inputStream);
        } catch (IOException e) {
            log.error("读取excel文件失败", e);
            throw new ServiceException(e.getMessage());
        }

        Sheet sheet = workbook.getSheetAt(0); // Assuming the first sheet

        if (sheet == null) {
            throw new ServiceException("文件sheet不存在");
        }

        // Assuming the first row contains column headers
        Row headerRow = sheet.getRow(0);
        int rows = sheet.getLastRowNum();

        if (rows <= 0) {
            return new ExcelData();
        }


        // Iterating through each cell in the header row

        List<String> headers = new ArrayList<>();

        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            Cell cell = headerRow.getCell(i);
            if (Objects.nonNull(cell)) {
                Object obj = getCellValue(headerRow, i);

                if (obj instanceof Date) {
                    headers.add(cn.hutool.core.date.DateUtil.format((Date) obj, "yy年MM月"));
                    continue;
                }

                String value = getCellValue(headerRow, i).toString();
                headers.add(value);
            } else {
                headers.add(StrUtil.EMPTY);
            }
        }


        List<List<Object>> data = new ArrayList<>();
        for (int i = 1; i <= rows; i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            List<Object> rowData = new ArrayList<>();
            for (int j = 0; j < headerRow.getPhysicalNumberOfCells(); j++) {
                Object val = getCellValue(row, j);
                rowData.add(val);
            }
            data.add(rowData);
        }

        ExcelData excelData = new ExcelData();
        excelData.setHeaders(headers);
        excelData.setData(data);
        return excelData;

    }

    private static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    public static Object getCellValue(Row row, int column) {
        if (row == null) {
            return row;
        }
        Object val = "";
        try {
            Cell cell = row.getCell(column);
            if (StringUtils.isNotNull(cell)) {
                if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                    val = cell.getNumericCellValue();
                    if (DateUtil.isCellDateFormatted(cell)) {
                        val = DateUtil.getJavaDate((Double) val); // POI Excel 日期格式转换
                    } else {
                        if ((Double) val % 1 != 0) {
                            val = new BigDecimal(val.toString());
                        } else {
                            val = new DecimalFormat("0").format(val);
                        }
                    }
                } else if (cell.getCellType() == CellType.STRING) {
                    val = cell.getStringCellValue();
                } else if (cell.getCellType() == CellType.BOOLEAN) {
                    val = cell.getBooleanCellValue();
                } else if (cell.getCellType() == CellType.ERROR) {
                    val = cell.getErrorCellValue();
                }

            }
        } catch (Exception e) {
            return val;
        }
        return val;
    }

    public static class ExcelData {
        private List<String> headers;
        private List<List<Object>> data;

        public List<String> getHeaders() {
            return headers;
        }

        public void setHeaders(List<String> headers) {
            this.headers = headers;
        }

        public List<List<Object>> getData() {
            return data;
        }

        public void setData(List<List<Object>> data) {
            this.data = data;
        }
    }


}
