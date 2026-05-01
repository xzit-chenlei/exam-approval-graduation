package edu.xzit.core.core.common.utils.poi;

import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.policy.DynamicTableRenderPolicy;
import com.deepoove.poi.policy.TableRenderPolicy;
import com.deepoove.poi.util.TableTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.util.List;
import java.util.Map;

/**
 * 通用动态表格渲染策略
 * 支持在指定行位置插入动态数据行
 *
 * @author system
 */
@Slf4j
public class DynamicTablePolicy extends DynamicTableRenderPolicy {

    /**
     * 动态表格配置数据
     */
    public static class DynamicTableConfig {
        /**
         * 数据行列表
         */
        private List<RowRenderData> rows;

        /**
         * 起始行索引（从0开始）
         */
        private int startRow;

        /**
         * 表格列数
         */
        private int columnCount;

        /**
         * 需要合并的单元格配置
         * key: 行索引, value: 合并配置数组 [起始列, 结束列]
         */
        private Map<Integer, int[]> mergeCells;

        /**
         * 是否移除起始行的原有数据
         */
        private boolean removeStartRow = true;

        public DynamicTableConfig() {
        }

        public DynamicTableConfig(List<RowRenderData> rows, int startRow, int columnCount) {
            this.rows = rows;
            this.startRow = startRow;
            this.columnCount = columnCount;
        }

        public List<RowRenderData> getRows() {
            return rows;
        }

        public void setRows(List<RowRenderData> rows) {
            this.rows = rows;
        }

        public int getStartRow() {
            return startRow;
        }

        public void setStartRow(int startRow) {
            this.startRow = startRow;
        }

        public int getColumnCount() {
            return columnCount;
        }

        public void setColumnCount(int columnCount) {
            this.columnCount = columnCount;
        }

        public Map<Integer, int[]> getMergeCells() {
            return mergeCells;
        }

        public void setMergeCells(Map<Integer, int[]> mergeCells) {
            this.mergeCells = mergeCells;
        }

        public boolean isRemoveStartRow() {
            return removeStartRow;
        }

        public void setRemoveStartRow(boolean removeStartRow) {
            this.removeStartRow = removeStartRow;
        }
    }

    @Override
    public void render(XWPFTable table, Object data) throws Exception {
        if (null == data) {
            return;
        }

        DynamicTableConfig config = (DynamicTableConfig) data;
        List<RowRenderData> rows = config.getRows();

        if (rows == null || rows.isEmpty()) {
            return;
        }

        int startRow = config.getStartRow();
        int columnCount = config.getColumnCount();

        // 移除起始行的原有数据（如果配置需要）
        if (config.isRemoveStartRow() && startRow < table.getNumberOfRows()) {
            table.removeRow(startRow);
        }

        // 循环插入数据行
        for (int i = 0; i < rows.size(); i++) {
            int currentRowIndex = startRow + i;
            XWPFTableRow newRow = table.insertNewTableRow(currentRowIndex);

            // 创建单元格
            for (int j = 0; j < columnCount; j++) {
                newRow.createCell();
            }

            // 渲染行数据
            TableRenderPolicy.Helper.renderRow(table.getRow(currentRowIndex), rows.get(i));

            // 处理单元格合并
            if (config.getMergeCells() != null && config.getMergeCells().containsKey(i)) {
                int[] mergeConfig = config.getMergeCells().get(i);
                if (mergeConfig != null && mergeConfig.length >= 2) {
                    // 支持同一行配置多段 [startCol, endCol]，例如 {2,7, 8,13, 14,19, 20,25}
                    for (int idx = 0; idx + 1 < mergeConfig.length; idx += 2) {
                        int startCol = mergeConfig[idx];
                        int endCol = mergeConfig[idx + 1];
                        TableTools.mergeCellsHorizonal(table, currentRowIndex, startCol, endCol);
                    }
                }
            }
        }
    }
}















