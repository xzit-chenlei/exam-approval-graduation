package edu.xzit.core.core.flowable.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StoreSceneNameConstants {

    /**
     * 电子签章
     */
    public static final String ELECTRONIC_AGREE = "electronic_agree";

    /**
     * 电子签名
     */
    public static final String ELECTRONIC_SIGNATURE = "electronic_signature";

    /**
     * word生成模板场景
     */
    public static final String FLOWABLE_WORD_TEMPLATE = "flowable_wordTemplate";

    /**
     * 新word模板主表场景
     */
    public static final String FLOWABLE_WORD_TEMPLATE_NEW = "flowable_wordTemplate_new";

    /**
     * 审批表场景
     */
    public static final String FLOWABLE_WORD_APPROVAL_FORM = "flowable_word_approval_form";

    /**
     * A卷场景
     */
    public static final String VOLUME_A = "volume_a";

    /**
     * A卷答案场景
     */
    public static final String VOLUME_A_ANSWER = "volume_a_answer";

    /**
     * B卷场景
     */
    public static final String VOLUME_B = "volume_b";

    /**
     * B卷答案表场景
     */
    public static final String VOLUME_B_ANSWER = "volume_b_answer";

    /**
     * 审核表场景
     */
    public static final String AUDIT_FORM = "audit_form";

    /**
     * 评价报告场景
     */
    public static final String ASSESSMENT_REPORT = "assessment_report";


    /**
     * 审批表表单
     */
    public static final String ASSESSMENT_APPROVAL = "assessment_approval";





    /**
     *
     * 获取考核审批的场景名
     * @return
     */
    public static List<String> getAssessmentApprovalSceneNameList(){
        return Collections.unmodifiableList(Arrays.asList(FLOWABLE_WORD_APPROVAL_FORM,VOLUME_A,VOLUME_A_ANSWER,VOLUME_B,VOLUME_B_ANSWER));
    }

}
