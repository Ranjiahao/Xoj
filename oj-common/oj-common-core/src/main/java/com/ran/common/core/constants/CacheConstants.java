package com.ran.common.core.constants;

public class CacheConstants {
    /**
     * 登录用户标识缓存 [有过期时间] <p>
     * eg: key={logintoken:+uuid} v={LoginUser类型数据结构}
     */
    public final static String LOGIN_TOKEN_KEY = "logintoken:";

    /**
     * 手机验证码缓存 [有过期时间] <p>
     * eg: key={phone:code:+手机号} v={验证码}
     */
    public final static String PHONE_CODE_KEY = "phone:code:";

    /**
     * 手机验证码发送次数缓存 [有过期时间] <p>
     * eg: key={code:times:+手机号} v={发送次数}
     */
    public final static String CODE_TIMES_KEY = "code:times:";

    /**
     * 用户上传头像次数缓存 [有过期时间] <p>
     * eg: key={user:upload:times} v={userId1:上传次数, userId2:上传次数}
     */
    public static final String USER_UPLOAD_TIMES_KEY = "user:upload:times";

    /**
     * 用户详情信息缓存 [有过期时间] <p>
     * eg: key={user:detail:+用户id} v={User类型数据结构}
     */
    public final static String USER_DETAIL = "user:detail:";

    /**
     * 竞赛详情缓存 <p>
     * eg: key={exam:detail:+竞赛id} v={Exam类型数据结构}
     */
    public final static String EXAM_DETAIL = "exam:detail:";

    /**
     * 未完赛竞赛缓存 <p>
     * eg: key={exam:unfinished:list} v={竞赛id列表}
     */
    public final static String EXAM_UNFINISHED_LIST = "exam:unfinished:list";

    /**
     * 竞赛历史缓存 <p>
     * eg: key={exam:history:list} v={竞赛id列表}
     */
    public final static String EXAM_HISTORY_LIST = "exam:history:list";

    /**
     * 用户竞赛列表缓存 <p>
     * eg: key={user:exam:list:+用户id} v={竞赛id列表}
     */
    public final static String USER_EXAM_LIST = "user:exam:list:";

    /**
     *  题目列表缓存 <p>
     *  eg: key={question:list} v={题目id列表}
     */
    public static final String QUESTION_LIST = "question:list";

    /**
     * 竞赛题目列表缓存 [有过期时间]<p>
     * eg: key={exam:question:list:+竞赛id} v={题目id列表}
     */
    public static final String EXAM_QUESTION_LIST = "exam:question:list:";
}
