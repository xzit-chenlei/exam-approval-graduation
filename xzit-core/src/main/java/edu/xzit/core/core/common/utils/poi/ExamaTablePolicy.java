package edu.xzit.core.core.common.utils.poi;

import com.deepoove.poi.policy.DynamicTableRenderPolicy;
import com.deepoove.poi.util.TableTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFTable;

/**
 * 专用于 exama 表格的结构调整策略：
 * - 不新增、不删除模板中的原有行，只在第 10 行上做单元格横向合并。
 *
 * 表结构假设（0-based 索引）：
 * - 第 9 行和第 10 行的第 0、1 列在模板中已经做了纵向合并（本策略不处理）
 * - 第 10 行有 24 个细分单元格，对应列 3~26（即“第 4 列”被拆成 24 个）
 * - 本策略将这 24 个单元格合并为 4 个单元格，每 6 列一组，
 *   但需要注意：合并后列索引会前移，所以采用“逐段向后”的方式合并：
 *   第 1 组合并当前列 3~8（0-based: 3~8），
 *   第 2 组合并此时的列 4~9（原来的 9~14），
 *   第 3 组合并此时的列 5~10（原来的 15~20），
 *   第 4 组合并此时的列 6~11（原来的 21~26）。
 *
 * 注意：模板中需要在 exama 表对应位置提供占位符（如 {{@exama}}），以触发该策略执行。
 *
 * @author system
 */
@Slf4j
public class ExamaTablePolicy extends DynamicTableRenderPolicy {

    @Override
    public void render(XWPFTable table, Object data) throws Exception {
        if (table == null) {
            return;
        }

        // 目标行为第 10 行（0-based 索引 9）
        int targetRowIndex = 9;
        if (table.getNumberOfRows() <= targetRowIndex) {
            log.warn("ExamaTablePolicy: 表格行数不足，无法对第 10 行进行合并操作");
            return;
        }

        try {
            // 将第 10 行（索引 9）的 24 个单元格合并成 4 个大单元格
            // 合并时列索引会收缩，所以按“当前位置”逐段合并：
            // 第 1 组：当前列 3~8
            TableTools.mergeCellsHorizonal(table, targetRowIndex, 3, 9);
            // 第 2 组：此时的列 4~9（对应原始的 10~15）
            TableTools.mergeCellsHorizonal(table, targetRowIndex, 4, 11);
            // 第 3 组：此时的列 5~10（对应原始的 16~21）
            TableTools.mergeCellsHorizonal(table, targetRowIndex, 5, 13);
            // 第 4 组：此时的列 6~11（对应原始的 22~27）
            TableTools.mergeCellsHorizonal(table, targetRowIndex, 6, 15);
        } catch (Exception e) {
            log.error("ExamaTablePolicy: 合并第 10 行单元格失败", e);
        }
    }
}


