package edu.xzit.core.core.common.utils;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import edu.xzit.core.dao.domain.SysJob;

/**
 * 定时任务处理（禁止并发执行）
 *
 * @author ruoyi
 *
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends AbstractQuartzJob {
    @Override
    protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception {
        JobInvokeUtil.invokeMethod(sysJob);
    }
}
