package edu.xzit.core.core.common.utils.poi;

import com.deepoove.poi.XWPFTemplate;
import edu.xzit.core.core.common.exception.UtilException;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Word文档相关处理工具类
 * 基于poi-tl模板引擎
 *
 * @author system
 */
@Slf4j
public class WordUtil {

    /**
     * Word模板对象
     */
    private XWPFTemplate template;

    /**
     * 模板文件名
     */
    private String templateFileName;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 模板数据映射
     */
    private Map<String, Object> wordTemplateData;

    /**
     * 自定义渲染策略映射
     */
    private Map<String, Object> renderPolicyMap;

    /**
     * 通过本地文件路径构建 WordUtil，适用于运行时从对象存储下载的模板
     *
     * @param filePath 模板本地路径
     * @return WordUtil实例
     */
    public static WordUtil fromFilePath(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new UtilException("模板文件不存在：" + filePath);
        }

        try (InputStream is = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return new WordUtil(file.getName(), baos.toByteArray());
        } catch (IOException e) {
            throw new UtilException("读取模板文件失败：" + e.getMessage());
        }
    }

    /**
     * 私有构造器，允许通过字节内容初始化模板
     *
     * @param templateFileName 模板文件名
     * @param content          模板二进制内容
     */
    private WordUtil(String templateFileName, byte[] content) {
        this.templateFileName = templateFileName;
        if (content == null) {
            throw new IllegalArgumentException("Template content not found: " + templateFileName);
        }
        this.template = XWPFTemplate.compile(new ByteArrayInputStream(content));
        this.wordTemplateData = new HashMap<>();
        this.renderPolicyMap = new HashMap<>();
    }

    /**
     * 注册自定义渲染策略
     *
     * @param key    模板变量名
     * @param policy 渲染策略
     * @return WordUtil实例
     */
    public WordUtil registerRenderPolicy(String key, Object policy) {
        this.renderPolicyMap.put(key, policy);
        return this;
    }

    /**
     * 添加通用数据
     *
     * @param key   模板变量名
     * @param value 数据值
     * @return WordUtil实例
     */
    public WordUtil addData(String key, Object value) {
        this.wordTemplateData.put(key, value);
        return this;
    }

    /**
     * 批量添加通用数据
     *
     * @param map 数据映射
     * @return WordUtil实例
     */
    public WordUtil addData(Map<String, Object> map) {
        this.wordTemplateData.putAll(map);
        return this;
    }

    /**
     * 设置文件名
     *
     * @param fileName 文件名
     * @return WordUtil实例
     */
    public WordUtil setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * 应用渲染策略到模板
     * 在导出前调用此方法以应用自定义渲染策略
     *
     * @return WordUtil实例
     */
    private WordUtil applyRenderPolicies() {
        if (renderPolicyMap != null && !renderPolicyMap.isEmpty()) {
            for (Map.Entry<String, Object> entry : renderPolicyMap.entrySet()) {
                try {
                    com.deepoove.poi.policy.RenderPolicy policy = (com.deepoove.poi.policy.RenderPolicy) entry.getValue();
                    template.getConfig().customPolicy(entry.getKey(), policy);
                } catch (Exception e) {
                    log.warn("注册渲染策略失败: key={}, error={}", entry.getKey(), e.getMessage());
                }
            }
        }
        return this;
    }

    /**
     * 设置响应头并导出Word文档
     *
     * @param response HTTP响应对象
     */
    public void exportWord(HttpServletResponse response) {
        try {
            // 应用渲染策略
            applyRenderPolicies();

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setCharacterEncoding("UTF-8");

            // 生成文件名
            String downloadFileName = generateFileName();
            response.setHeader("Content-Disposition", "attachment; filename=" + downloadFileName);

            // 渲染并导出
            template.render(wordTemplateData).writeAndClose(response.getOutputStream());

        } catch (IOException e) {
            log.error("导出Word文档异常", e);
            throw new UtilException("导出Word文档失败：" + e.getMessage());
        }
    }

    /**
     * 生成文件名
     *
     * @return 编码后的文件名
     */
    private String generateFileName() {
        String baseFileName = (fileName != null && !fileName.isEmpty()) ? fileName : "Word文档";
        return encodingFilename(baseFileName);
    }

    /**
     * 编码文件名
     *
     * @param filename 原始文件名
     * @return 编码后的文件名
     */
    private String encodingFilename(String filename) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new java.util.Date());
        return timestamp + "_" + filename + ".docx";
    }
}
