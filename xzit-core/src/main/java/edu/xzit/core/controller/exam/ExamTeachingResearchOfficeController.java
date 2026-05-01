package edu.xzit.core.controller.exam;

import edu.xzit.core.core.common.annotation.Log;
import edu.xzit.core.core.common.core.controller.BaseController;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.common.core.page.TableDataInfo;
import edu.xzit.core.core.common.enums.BusinessType;
import edu.xzit.core.dao.domain.ExamTeachingResearchOffice;
import edu.xzit.core.dao.service.IExamTeachingResearchOfficeService;
import edu.xzit.core.core.common.core.domain.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam/teachingResearchOffice")
public class ExamTeachingResearchOfficeController extends BaseController {

    @Autowired
    private IExamTeachingResearchOfficeService officeService;

    @PreAuthorize("@ss.hasPermi('exam:teachingResearchOffice:list')")
    @GetMapping("/list")
    public TableDataInfo list(ExamTeachingResearchOffice query) {
        startPage();
        List<ExamTeachingResearchOffice> list = officeService.selectExamTeachingResearchOfficeList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('exam:teachingResearchOffice:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(officeService.getById(id));
    }

    @PreAuthorize("@ss.hasPermi('exam:teachingResearchOffice:add')")
    @Log(title = "教研室", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ExamTeachingResearchOffice body) {
        return toAjax(officeService.insertExamTeachingResearchOffice(body));
    }

    @PreAuthorize("@ss.hasPermi('exam:teachingResearchOffice:edit')")
    @Log(title = "教研室", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ExamTeachingResearchOffice body) {
        return toAjax(officeService.updateExamTeachingResearchOffice(body));
    }

    @PreAuthorize("@ss.hasPermi('exam:teachingResearchOffice:remove')")
    @Log(title = "教研室", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        return toAjax(officeService.deleteExamTeachingResearchOfficeById(id));
    }

    // 用户关联
    @PreAuthorize("@ss.hasPermi('exam:teachingResearchOffice:user:list')")
    @GetMapping("/{officeId}/users")
    public TableDataInfo listUsers(@PathVariable Long officeId) {
        startPage();
        List<SysUser> users = officeService.selectUsersByOfficeId(officeId);
        return getDataTable(users);
    }

    @PreAuthorize("@ss.hasPermi('exam:teachingResearchOffice:user:add')")
    @Log(title = "教研室用户", businessType = BusinessType.INSERT)
    @PostMapping("/{officeId}/users")
    public AjaxResult addUsers(@PathVariable Long officeId, @RequestBody IdsBody body) {
        return toAjax(officeService.addUsers(officeId, body.getUserIds()));
    }

    @PreAuthorize("@ss.hasPermi('exam:teachingResearchOffice:user:remove')")
    @Log(title = "教研室用户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{officeId}/users")
    public AjaxResult removeUsers(@PathVariable Long officeId, @RequestBody IdsBody body) {
        return toAjax(officeService.removeUsers(officeId, body.getUserIds()));
    }

    @PreAuthorize("@ss.hasPermi('exam:teachingResearchOffice:list')")
    @GetMapping("/byUser/{userId}")
    public AjaxResult listByUser(@PathVariable Long userId) {
        return success(officeService.selectByUserId(userId));
    }

    // 公共接口：获取全部教研室（无权限校验）
    @GetMapping("/public/listAll")
    public AjaxResult listAll() {
        List<ExamTeachingResearchOffice> list = officeService.selectExamTeachingResearchOfficeList(new ExamTeachingResearchOffice());
        return success(list);
    }

    public static class IdsBody {
        private List<Long> userIds;
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
    }
}
