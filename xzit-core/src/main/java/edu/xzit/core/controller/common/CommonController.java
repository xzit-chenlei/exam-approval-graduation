package edu.xzit.core.controller.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.xzit.core.core.common.constant.Constants;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.utils.StringUtils;
import edu.xzit.core.core.common.utils.file.FileUploadUtils;
import edu.xzit.core.core.common.utils.file.FileUtils;
import edu.xzit.core.core.flowable.constant.StoreSceneNameConstants;
import edu.xzit.core.core.framework.config.RuoYiConfig;
import edu.xzit.core.core.framework.config.ServerConfig;
import edu.xzit.core.dao.domain.SysStoreInfo;
import edu.xzit.core.dao.service.ISysStoreInfoRepoService;
import edu.xzit.core.model.dto.FileDTO;
import edu.xzit.core.util.HashUtil;
import edu.xzit.core.util.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 通用请求处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private ISysStoreInfoRepoService sysStoreInfoRepoService;

    private static final String FILE_DELIMETER = ",";

    /**
     * 通用下载请求
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    @GetMapping("/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request) {
        try {
            if (!FileUtils.checkAllowDownload(fileName)) {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = RuoYiConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete) {
                FileUtils.deleteFile(filePath);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求（单个）
     *
     * 过时。旧版本上传文件接口，后续切换到新版本接口
     */
    @PostMapping("/upload")
    public AjaxResult uploadFile(MultipartFile file) throws Exception {
        try {
            FileDTO fileDTO = OssUtil.uploadFile(file);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", fileDTO.getUrl());
            ajax.put("fileName", file.getOriginalFilename());
            ajax.put("newFileName", file.getOriginalFilename());
            ajax.put("originalFilename", file.getOriginalFilename());
            ajax.put("ossKey", fileDTO.getKey());
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }


    @PostMapping("/v3/upload")
    public AjaxResult uploadFileV3(@RequestParam("file")  MultipartFile file,@RequestParam("sceneName") String sceneName ,@RequestParam("objId") Long objId)throws Exception {
        try {
            FileDTO fileDTO = OssUtil.uploadFile(file);

            SysStoreInfo sysStoreInfo = new SysStoreInfo();
            sysStoreInfo.setOssKey(fileDTO.getKey());
            sysStoreInfo.setUrl(fileDTO.getUrl());
            sysStoreInfo.setSceneName(sceneName);
            sysStoreInfo.setObjId(objId);
            sysStoreInfo.setShortLink(HashUtil.generateMD5Hash());
            sysStoreInfoRepoService.save(sysStoreInfo);

            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", fileDTO.getUrl());
            ajax.put("fileName", file.getOriginalFilename());
            ajax.put("newFileName", file.getOriginalFilename());
            ajax.put("originalFilename", file.getOriginalFilename());
            ajax.put("id", sysStoreInfo.getId());
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @DeleteMapping("/v3/delete")
    public AjaxResult deleteFileV3(@RequestParam("sceneName") String sceneName ,@RequestParam("objId") Long objId)throws Exception {
        LambdaQueryWrapper<SysStoreInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysStoreInfo::getSceneName, sceneName)
                .eq(SysStoreInfo::getObjId, objId);
        sysStoreInfoRepoService.remove(queryWrapper);
        return AjaxResult.success();
    }


    @GetMapping("/v2/getTmpUrl")
    public AjaxResult getTmpUrl(String hashCode) {

        LambdaQueryWrapper<SysStoreInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysStoreInfo::getShortLink, hashCode);
        SysStoreInfo sysStoreInfo = sysStoreInfoRepoService.getOne(queryWrapper);
        if (sysStoreInfo == null || sysStoreInfo.getOssKey() == null) {
            return AjaxResult.error("文件不存在");
        }

        return AjaxResult.success("success",OssUtil.getDownloadUrl(sysStoreInfo.getOssKey(), 10));
    }

    /**
     * 获取文件路径
     *
     * 获取文件表中的文件路径和文件名称
     */
    @GetMapping("/v3/getTmpUrl")
    public AjaxResult getTmpUrlBySceneNameAndObjId(String sceneName , Long objId) {

        LambdaQueryWrapper<SysStoreInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysStoreInfo::getObjId, objId)
                .eq(SysStoreInfo::getSceneName, sceneName);
        SysStoreInfo sysStoreInfo = sysStoreInfoRepoService.getOne(queryWrapper);


        // 检查查询结果是否为空或OSS密钥是否为空
        if (sysStoreInfo == null || sysStoreInfo.getOssKey() == null || sysStoreInfo.getOssKey().isEmpty()) {
            return AjaxResult.success("文件未找到"); // 使用更明确的错误信息
        }

        // 提取文件名（即OSS密钥中最后一个'/'之后的部分）
        String fileName = FileUtils.getName(sysStoreInfo.getOssKey());

        // 准备返回结果
        FileDTO fileDTO = new FileDTO();
        fileDTO.setKey(fileName);
        fileDTO.setUrl(sysStoreInfo.getUrl());


        return AjaxResult.success(fileDTO); //
    }

    /**
     * 获取文件路径列表
     *
     * 获取文件表中的文件路径和文件名称
     */
    @GetMapping("/v3/getTmpUrlList")
    public AjaxResult getTmpUrlListBySceneNameAndObjId(String formName , Long objId) {
        List<String> sceneNameList = new ArrayList<>();
        if(StoreSceneNameConstants.ASSESSMENT_APPROVAL.equals(formName)){
            sceneNameList = StoreSceneNameConstants.getAssessmentApprovalSceneNameList();
        }
        LambdaQueryWrapper<SysStoreInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysStoreInfo::getObjId, objId)
                .in(SysStoreInfo::getSceneName, sceneNameList);
        List<SysStoreInfo> sysStoreInfoList = sysStoreInfoRepoService.list(queryWrapper);


        // 检查查询结果是否为空或OSS密钥是否为空
        if (Objects.isNull(sysStoreInfoList)) {
            return AjaxResult.success("文件未找到"); // 使用更明确的错误信息
        }

        List<FileDTO> fileList = sysStoreInfoList.stream().map(sysStoreInfo -> {
            FileDTO fileDTO = new FileDTO();
            fileDTO.setKey(FileUtils.getName(sysStoreInfo.getOssKey()));
            fileDTO.setUrl(sysStoreInfo.getUrl());
            return fileDTO;
        }).collect(Collectors.toList());

        return AjaxResult.success(fileList);
    }

    /**
     * 通用上传请求（多个）
     */
    @PostMapping("/uploads")
    public AjaxResult uploadFiles(List<MultipartFile> files) throws Exception {
        try {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            List<String> urls = new ArrayList<String>();
            List<String> fileNames = new ArrayList<String>();
            List<String> newFileNames = new ArrayList<String>();
            List<String> originalFilenames = new ArrayList<String>();
            for (MultipartFile file : files) {
                // 上传并返回新文件名称
                String fileName = FileUploadUtils.upload(filePath, file);
                String url = serverConfig.getUrl() + fileName;
                urls.add(url);
                fileNames.add(fileName);
                newFileNames.add(FileUtils.getName(fileName));
                originalFilenames.add(file.getOriginalFilename());
            }
            AjaxResult ajax = AjaxResult.success();
            ajax.put("urls", StringUtils.join(urls, FILE_DELIMETER));
            ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMETER));
            ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMETER));
            ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMETER));
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/download/resource")
    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {
            if (!FileUtils.checkAllowDownload(resource)) {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            // 本地资源路径
            String localPath = RuoYiConfig.getProfile();
            // 数据库资源地址
            String downloadPath = localPath + StringUtils.substringAfter(resource, Constants.RESOURCE_PREFIX);
            // 下载名称
            String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath, response.getOutputStream());
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }
}
