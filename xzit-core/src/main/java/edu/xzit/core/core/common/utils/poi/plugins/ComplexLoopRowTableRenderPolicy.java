package edu.xzit.core.core.common.utils.poi.plugins;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.exception.RenderException;
import com.deepoove.poi.policy.RenderPolicy;
import com.deepoove.poi.render.compute.EnvModel;
import com.deepoove.poi.render.compute.RenderDataCompute;
import com.deepoove.poi.render.processor.DocumentProcessor;
import com.deepoove.poi.render.processor.EnvIterator;
import com.deepoove.poi.resolver.TemplateResolver;
import com.deepoove.poi.template.ElementTemplate;
import com.deepoove.poi.template.MetaTemplate;
import com.deepoove.poi.template.run.RunTemplate;
import com.deepoove.poi.util.ReflectionUtils;
import com.deepoove.poi.util.TableTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.util.*;

/**
 * 复杂表格的行循环功能实现，
 * 目前仅适配 考核内容与评价方法合理性分析表 即 {analysis_form_table}，
 * 部分内容由 AI 生成
 * 数据格式需求如下：
 * [
 *   {
 *     "objective": "objective_1",
 *     "details": [
 *       {
 *         "exam_type": "type_1",
 *         "exam_content": "test content"
 *       },
 *       {
 *         "exam_type": "type_2",
 *         "exam_content": "test content 2"
 *       },
 *       {
 *         "exam_type": "analyze",
 *         "exam_content": "analysis content"
 *       }
 *     ]
 *   }
 * ]
 *
 * @author zbx
 */
@Slf4j
public class ComplexLoopRowTableRenderPolicy implements RenderPolicy {

    private String prefix;    // 自定义标签前缀（例如"["）
    private String suffix;    // 自定义标签后缀（例如"]"）
    private boolean onSameLine; // 控制模板行索引计算方式（是否与标签同行）

    // 以下构造函数支持自定义标签语法和行定位逻辑
    public ComplexLoopRowTableRenderPolicy() {
        this(false);
    }

    public ComplexLoopRowTableRenderPolicy(boolean onSameLine) {
        this("[", "]", onSameLine); // 默认使用"[]"作为循环行内的标签语法[4](@ref)
    }

    public ComplexLoopRowTableRenderPolicy(String prefix, String suffix) {
        this(prefix, suffix, false);
    }

    public ComplexLoopRowTableRenderPolicy(String prefix, String suffix, boolean onSameLine) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.onSameLine = onSameLine;
    }

    @Override
    public void render(ElementTemplate eleTemplate, Object data, XWPFTemplate template) {
        RunTemplate runTemplate = (RunTemplate) eleTemplate;
        XWPFRun run = runTemplate.getRun();
        try {
            // 1. 检查标签是否在表格内（poi-tl要求行循环标签必须位于表格中）
            if (!TableTools.isInsideTable(run)) {
                throw new IllegalStateException(
                        "The template tag " + runTemplate.getSource() + " must be inside a table");
            }

            // 2. 获取标签所在单元格、行和表格对象
            XWPFTableCell tagCell = (XWPFTableCell) ((XWPFParagraph) run.getParent()).getBody();
            XWPFTable table = tagCell.getTableRow().getTable();
            run.setText("", 0); // 清空原标签位置文本，避免残留

            // 3. 计算模板行的索引（模板行是待循环复制的基准行）
            int templateRowIndex = getTemplateRowIndex(tagCell);

            // 4. 仅当数据为集合时执行循环渲染
            if (null != data && data instanceof Iterable) {
                Iterator<?> iterator = ((Iterable<?>) data).iterator();
                XWPFTableRow templateRow = table.getRow(templateRowIndex);
                int insertPosition = templateRowIndex; // 新行插入位置

                // 5. 创建标签解析器，支持自定义前缀后缀（用于识别循环行内的占位符，如[name]）
                TemplateResolver resolver = new TemplateResolver(template.getConfig().copy(prefix, suffix));

                boolean firstFlag = true; // 标记首行，用于处理跨行单元格合并
                int index = 0;
                boolean hasNext = iterator.hasNext();
                while (hasNext) {
                    Object root = iterator.next(); // 当前迭代的数据对象
                    hasNext = iterator.hasNext();



                    // 解析新行中的占位符并渲染数据
                    Map<String, Object> rootMap = (LinkedHashMap<String, Object>) root;

                    List<Object> details =  (List<Object>) rootMap.get("details");

                    int subjectiveStartPos = templateRowIndex;
                    

                    int detailsSize = details.size();
                    for (int detailIndex = 0; detailIndex < detailsSize; detailIndex++) {
                        Object detail = details.get(detailIndex);

                        // 刷新插入位置和模板位置
                        insertPosition = templateRowIndex++;

                        LinkedHashMap<String, Object> detailMap = (LinkedHashMap<String, Object>) detail;
                        detailMap.put("objective", ((LinkedHashMap<String, List>)root).get("objective"));

                        // 插入新行并复制模板行的样式和结构
                        XWPFTableRow nextRow = table.insertNewTableRow(insertPosition);
                        setTableRow(table, templateRow, insertPosition);

                        // 通过XML操作确保新行完全复制模板行的属性
                        XmlCursor newCursor = templateRow.getCtRow().newCursor();
                        newCursor.toPrevSibling();
                        XmlObject object = newCursor.getObject();
                        nextRow = new XWPFTableRow((CTRow) object, table);

                        // 处理跨行单元格合并（vMerge）：
                        //  - 首行保留STMerge.RESTART（合并开始标记）
                        //  - 后续行改为STMerge.CONTINUE（合并延续标记）
                        if (!firstFlag) {
                            List<XWPFTableCell> tableCells = nextRow.getTableCells();
                            for (XWPFTableCell cell : tableCells) {
                                CTTcPr tcPr = TableTools.getTcPr(cell);
                                CTVMerge vMerge = tcPr.getVMerge();
                                if (null == vMerge) continue;
                                if (STMerge.RESTART == vMerge.getVal()) {
                                    vMerge.setVal(STMerge.CONTINUE); // 非首行改为延续合并
                                }
                            }
                        } else {
                            firstFlag = false;
                        }
                        setTableRow(table, nextRow, insertPosition);

                        // 数据渲染配置
                        RenderDataCompute dataCompute = template.getConfig()
                                .getRenderDataComputeFactory()
                                .newCompute(EnvModel.of(detailMap, EnvIterator.makeEnv(index++, hasNext)));

                        List<XWPFTableCell> cells = nextRow.getTableCells();
                        cells.forEach(cell -> {
                            // 解析单元格内的标签（如[name]）
                            List<MetaTemplate> templates = resolver.resolveBodyElements(cell.getBodyElements());
                            // 执行实际渲染，将数据填充到标签位置
                            new DocumentProcessor(template, resolver, dataCompute).process(templates);
                        });

                        // 如果是 details 中的最后一项，则需要合并单元格
                        if (detailIndex == detailsSize - 1) {
                            // 先获取后两个 cell 中的内容（在合并前获取）
                            String content1 = cells.get(1).getText();
                            String content2 = cells.get(2).getText();
                            String mergeContent = content1 + "：" + content2;

                            // 合并单元格（使用当前行的索引 insertPosition）
                            // 合并后，第二个单元格（索引1）会保留，第三个单元格（索引2）会被删除
                            TableTools.mergeCellsHorizonal(table, insertPosition, 1, 2);
                            
                            // 合并后，重新获取单元格列表（因为合并后单元格结构会变化）
                            List<XWPFTableCell> mergedCells = nextRow.getTableCells();
                            if (mergedCells.size() > 1) {
                                // 清空合并后的单元格内容
                                XWPFTableCell mergedCell = mergedCells.get(1);
                                // 删除所有段落
                                for (int i = mergedCell.getParagraphs().size() - 1; i >= 0; i--) {
                                    mergedCell.removeParagraph(i);
                                }
                                // 创建新段落并设置内容
                                XWPFParagraph paragraph = mergedCell.addParagraph();
                                XWPFRun mergedRun = paragraph.createRun();
                                mergedRun.setText(mergeContent);
                            }
                        }
                    }

                    int subjectiveEndPos = templateRowIndex - 1;
                    
                    // 只有当有多行时才需要合并（至少2行）
                    if (subjectiveEndPos > subjectiveStartPos && details.size() > 0) {
                        try {
                            // 使用 TableTools.mergeCellsVertically 合并第一列（索引0）的单元格
                            // 参数：table, columnIndex, startRow, endRow（包含）
                            TableTools.mergeCellsVertically(table, 0, subjectiveStartPos, subjectiveEndPos);
                            log.debug("成功合并单元格：列0，从行{}到行{}", subjectiveStartPos, subjectiveEndPos);
                        } catch (Exception e) {
                            log.error("合并单元格失败：列0，从行{}到行{}，错误：{}", subjectiveStartPos, subjectiveEndPos, e.getMessage(), e);
                        }
                    }
                }
            }

            // 10. 删除原始模板行（循环完成后清理）
            table.removeRow(templateRowIndex);
            afterloop(table, data); // 可供子类扩展的钩子方法
        } catch (Exception e) {
            throw new RenderException("HackLoopTable for " + eleTemplate + " error: " + e.getMessage(), e);
        }
    }

    /**
     * 计算模板行索引。
     * 若onSameLine为true，模板行即标签所在行；否则为标签的下一行（标准行为）
     */
    private int getTemplateRowIndex(XWPFTableCell tagCell) {
        XWPFTableRow tagRow = tagCell.getTableRow();
        return onSameLine ? getRowIndex(tagRow) : (getRowIndex(tagRow) + 1);
    }

    /**
     * 循环执行后的扩展点（子类可重写以添加自定义逻辑，如统计行插入）。
     */
    protected void afterloop(XWPFTable table, Object data) {
    }

    /**
     * 通过反射强制更新表格的行列表，确保新行样式生效。
     */
    @SuppressWarnings("unchecked")
    private void setTableRow(XWPFTable table, XWPFTableRow templateRow, int pos) {
        List<XWPFTableRow> rows = (List<XWPFTableRow>) ReflectionUtils.getValue("tableRows", table);
        rows.set(pos, templateRow);
        table.getCTTbl().setTrArray(pos, templateRow.getCtRow());
    }

    private int getRowIndex(XWPFTableRow row) {
        List<XWPFTableRow> rows = row.getTable().getRows();
        return rows.indexOf(row);
    }
}