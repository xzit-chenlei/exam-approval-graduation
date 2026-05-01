package edu.xzit.core.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.ObjectMetadata;
import edu.xzit.core.core.common.exception.ServiceException;
import edu.xzit.core.core.common.utils.file.FileUtils;
import edu.xzit.core.core.common.utils.spring.SpringUtils;
import edu.xzit.core.model.dto.FileDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.shaded.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Slf4j
public class OssUtil {

    private static final String ENDPOINT = SpringUtils.getRequiredProperty("aliyun.oss.endpoint");
    private static final String ACCESS_KEY_ID = SpringUtils.getRequiredProperty("aliyun.access-key-id");
    private static final String ACCESS_KEY_SECRET = SpringUtils.getRequiredProperty("aliyun.access-key-secret");
    private static final String BUCKET_NAME = SpringUtils.getRequiredProperty("aliyun.oss.bucket-name");

    private static final OSSClient ossClient = new OSSClient(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);

    public static FileDTO uploadFile(MultipartFile file) {

        String savePathName = DateUtil.format(new Date(), "yyyyMMdd") + "/" + UUID.fastUUID() + "/" + file.getOriginalFilename();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            ossClient.putObject(BUCKET_NAME, savePathName, file.getInputStream(),metadata);
            URL url = ossClient.generatePresignedUrl(BUCKET_NAME, savePathName, new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 10));
            FileDTO fileDTO = new FileDTO();
            fileDTO.setKey(savePathName);
            fileDTO.setUrl(url.toString());
            return fileDTO;
        } catch (Throwable oe) {
            log.error("upload error", oe);
            throw new ServiceException("上传文件失败");
        }
    }

    public static FileDTO uploadFileByPath(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("指定的文件路径无效或文件不存在");
        }

        String savePathName = DateUtil.format(new Date(), "yyyyMMdd") + "/" + UUID.randomUUID().toString() + "/" + file.getName();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            ObjectMetadata metadata = new ObjectMetadata();
            // 这里您可能无法直接获取文件的ContentType，除非您有其他方式来确定它
            // metadata.setContentType(file.getContentType()); // 这行代码将不会编译，因为File类没有getContentType方法
            // 一种替代方法是使用文件的扩展名来猜测ContentType，或者使用第三方库来检测
            // 例如，使用Files.probeContentType(file.toPath()) (需要Java 7+) 或 Apache Tika
            // 这里我们暂时设置为null，或者您可以设置一个默认值，比如"application/octet-stream"
            metadata.setContentType("application/octet-stream"); // 默认类型，表示二进制数据

            ossClient.putObject(BUCKET_NAME, savePathName, inputStream, metadata);

            URL url = ossClient.generatePresignedUrl(BUCKET_NAME, savePathName, new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 10));
            FileDTO fileDTO = new FileDTO();
            fileDTO.setKey(savePathName);
            fileDTO.setUrl(url.toString());
            return fileDTO;
        } catch (Throwable oe) {
            Logger log = LoggerFactory.getLogger(OssUtil.class); // 确保替换为正确的类名
            log.error("upload error", oe);
            throw new ServiceException("上传文件失败");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // 日志记录或忽略关闭流时的异常
                }
            }
        }
    }

    public static String getDownloadUrl(String key,Integer seconds){
        return ossClient.generatePresignedUrl(BUCKET_NAME, key, new Date(System.currentTimeMillis() + 1000L * seconds)).toString();
    }

    public static String downLoadFile(String objectName) {

        String tempDirPath = System.getProperty("java.io.tmpdir");
        Path tempFilePath = null;
        try {
            String fileName = FileUtils.getNameNotSuffix(objectName);
            tempFilePath = Files.createTempFile(Paths.get(tempDirPath), fileName, "."+FilenameUtils.getExtension(objectName));
            ossClient.getObject(new GetObjectRequest(BUCKET_NAME, objectName), tempFilePath.toFile());

            // 如果是 .doc 文件，转换为 .docx
            if ("doc".equalsIgnoreCase(FilenameUtils.getExtension(objectName))) {
                // 定义转换后的 .docx 路径
                Path docxPath = Paths.get(tempFilePath.toString().replace(".doc", ".docx"));

                // 调用 LibreOffice 命令行转换
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "soffice",
                        "--headless",
                        "--convert-to", "docx",
                        "--outdir", tempFilePath.getParent().toString(),
                        tempFilePath.toString()
                );
                Process process = processBuilder.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    // 删除原始 .doc 文件，更新路径为 .docx
                    Files.delete(tempFilePath);
                    tempFilePath = docxPath;
                } else {
                    throw new RuntimeException("DOC 转 DOCX 失败");
                }
            }
            return tempFilePath.toString(); // 返回文件路径
        } catch (OSSException oe) {
            log.error("OSS error: " + oe.getErrorMessage());
            log.error("Error Code: " + oe.getErrorCode());
            log.error("Request ID: " + oe.getRequestId());
            log.error("Host ID: " + oe.getHostId());
        } catch (ClientException ce) {
            log.error("Client error: " + ce.getMessage());
        } catch (IOException e) {
            // 可以选择记录日志或执行其他错误处理逻辑
            log.error("IO error: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null; // 如果发生异常，返回null或抛出异常
    }

}
