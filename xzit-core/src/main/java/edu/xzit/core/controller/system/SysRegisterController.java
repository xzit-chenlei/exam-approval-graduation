package edu.xzit.core.controller.system;

import edu.xzit.core.core.common.annotation.Anonymous;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.domain.model.RegisterBody;
import edu.xzit.core.core.common.core.domain.entity.SysDept;
import edu.xzit.core.core.common.utils.StringUtils;
import edu.xzit.core.core.framework.web.service.SysRegisterService;
import edu.xzit.core.dao.service.ISysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册验证
 *
 * @author ruoyi
 */
@RestController
public class SysRegisterController extends BaseController {
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysDeptService deptService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user) {
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }

    @Anonymous
    @GetMapping("/register/dept")
    public AjaxResult deptOptions() {
        SysDept query = new SysDept();
        query.setStatus("0");
        return success(deptService.selectDeptTreeListAll(query));
    }
}
