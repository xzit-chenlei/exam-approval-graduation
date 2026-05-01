package edu.xzit.core.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.domain.entity.SysUser;
import edu.xzit.core.core.common.core.domain.model.LoginUser;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.core.common.utils.SecurityUtils;
import edu.xzit.core.core.common.utils.StringUtils;
import edu.xzit.core.core.common.utils.file.FileUploadUtils;
import edu.xzit.core.core.common.utils.file.MimeTypeUtils;
import edu.xzit.core.core.flowable.constant.StoreSceneNameConstants;
import edu.xzit.core.core.framework.config.RuoYiConfig;
import edu.xzit.core.core.framework.web.service.TokenService;
import edu.xzit.core.dao.domain.SysStoreInfo;
import edu.xzit.core.dao.service.ISysStoreInfoRepoService;
import edu.xzit.core.dao.service.ISysUserService;
import edu.xzit.core.service.SysDeptBizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 个人信息 业务处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/user/profile")
public class
SysProfileController extends BaseController {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysDeptBizService sysDeptBizService;

    @Autowired
    private ISysStoreInfoRepoService sysStoreInfoRepoService;

    /**
 * 个人信息
 */
@GetMapping
public AjaxResult profile() {
    LoginUser loginUser = getLoginUser();
    if (loginUser == null) {
        return AjaxResult.error("用户未登录");
    }

    SysUser user = loginUser.getUser();

    // 合并查询两种类型的数据
    List<SysStoreInfo> storeInfos = sysStoreInfoRepoService.list(new LambdaQueryWrapper<SysStoreInfo>()
            .eq(SysStoreInfo::getObjId, user.getUserId())
            .in(SysStoreInfo::getSceneName, Arrays.asList(
                    StoreSceneNameConstants.ELECTRONIC_SIGNATURE,
                    StoreSceneNameConstants.ELECTRONIC_AGREE
            ))
    );

    String electronicSignatureUrl = storeInfos.stream()
            .filter(info -> StoreSceneNameConstants.ELECTRONIC_SIGNATURE.equals(info.getSceneName()))
            .findFirst()
            .map(SysStoreInfo::getUrl)
            .orElse(null);

    String electronicAgreeUrl = storeInfos.stream()
            .filter(info -> StoreSceneNameConstants.ELECTRONIC_AGREE.equals(info.getSceneName()))
            .findFirst()
            .map(SysStoreInfo::getUrl)
            .orElse(null);

    user.setElectronicSignatureUrl(electronicSignatureUrl);
    user.setElectronicAgreeUrl(electronicAgreeUrl);

    AjaxResult ajax = AjaxResult.success(user);
    ajax.put("roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()));
    ajax.put("postGroup", userService.selectUserPostGroup(loginUser.getUsername()));
    ajax.put("deptList", sysDeptBizService.selectDeptListByUserId(loginUser.getUserId()));
    return ajax;
}

private void setElectronicUrl(SysUser user, List<SysStoreInfo> storeInfos, String sceneName, Consumer<String> setter) {
    Optional.ofNullable(storeInfos.stream()
            .filter(info -> sceneName.equals(info.getSceneName()))
            .findFirst()
            .map(SysStoreInfo::getUrl)
            .orElse(null))
        .ifPresent(setter);
}


    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateProfile(@RequestBody SysUser user) {
        LoginUser loginUser = getLoginUser();
        SysUser currentUser = loginUser.getUser();
        currentUser.setNickName(user.getNickName());
        currentUser.setEmail(user.getEmail());
        currentUser.setPhonenumber(user.getPhonenumber());
        currentUser.setSex(user.getSex());
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(currentUser)) {
            return error("修改用户'" + loginUser.getUsername() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(currentUser)) {
            return error("修改用户'" + loginUser.getUsername() + "'失败，邮箱账号已存在");
        }
        if (userService.updateUserProfile(currentUser) > 0) {
            // 更新缓存用户信息
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(String oldPassword, String newPassword) {
        LoginUser loginUser = getLoginUser();
        String userName = loginUser.getUsername();
        String password = loginUser.getPassword();
        if (!SecurityUtils.matchesPassword(oldPassword, password)) {
            return error("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, password)) {
            return error("新密码不能与旧密码相同");
        }
        newPassword = SecurityUtils.encryptPassword(newPassword);
        if (userService.resetUserPwd(userName, newPassword) > 0) {
            // 更新缓存用户密码
            loginUser.getUser().setPassword(newPassword);
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file) throws Exception {
        if (!file.isEmpty()) {
            LoginUser loginUser = getLoginUser();
            String avatar = FileUploadUtils.upload(RuoYiConfig.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION);
            if (userService.updateUserAvatar(loginUser.getUsername(), avatar)) {
                AjaxResult ajax = AjaxResult.success();
                ajax.put("imgUrl", avatar);
                // 更新缓存用户头像
                loginUser.getUser().setAvatar(avatar);
                tokenService.setLoginUser(loginUser);
                return ajax;
            }
        }
        return error("上传图片异常，请联系管理员");
    }
}
