package edu.xzit.core.core.framework.web.service;

import edu.xzit.core.core.common.constant.CacheConstants;
import edu.xzit.core.core.common.constant.Constants;
import edu.xzit.core.core.common.constant.UserConstants;
import edu.xzit.core.core.common.core.domain.entity.SysUser;
import edu.xzit.core.core.common.core.domain.model.RegisterBody;
import edu.xzit.core.core.common.core.redis.RedisCache;
import edu.xzit.core.core.common.exception.user.CaptchaException;
import edu.xzit.core.core.common.exception.user.CaptchaExpireException;
import edu.xzit.core.core.common.utils.MessageUtils;
import edu.xzit.core.core.common.utils.SecurityUtils;
import edu.xzit.core.core.common.utils.StringUtils;
import edu.xzit.core.dao.service.ISysConfigService;
import edu.xzit.core.dao.service.ISysUserService;
import edu.xzit.core.core.framework.manager.AsyncManager;
import edu.xzit.core.core.framework.manager.factory.AsyncFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 注册校验方法
 * 
 * @author ruoyi
 */
@Component
public class SysRegisterService
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody)
    {
        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        if (registerBody.getDeptIds() != null && registerBody.getDeptIds().length > 0) {
            sysUser.setDeptIds(registerBody.getDeptIds());
        }

        // 验证码开关
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }

        if (StringUtils.isEmpty(username))
        {
            msg = "用户名不能为空";
        }
        else if (StringUtils.isEmpty(password))
        {
            msg = "用户密码不能为空";
        }
        else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            msg = "账户长度必须在2到20个字符之间";
        }
        else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            msg = "密码长度必须在5到20个字符之间";
        }
        else if (!userService.checkUserNameUnique(sysUser))
        {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        }
        else
        {
            sysUser.setNickName(username);
            sysUser.setPassword(SecurityUtils.encryptPassword(password));
            // 默认账号未启用
            sysUser.setStatus("1");
            boolean regFlag = userService.registerUser(sysUser);
            if (!regFlag)
            {
                msg = "注册失败,请联系系统管理人员";
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.register.success")));
            }
        }
        return msg;
    }

    /**
     * 校验验证码
     * 
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null)
        {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new CaptchaException();
        }
    }
}
