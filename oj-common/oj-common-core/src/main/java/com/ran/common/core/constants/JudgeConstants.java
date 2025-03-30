package com.ran.common.core.constants;

public class JudgeConstants {

    public static final Integer ERROR_SCORE = 0;

    public static final Integer DEFAULT_SCORE = 100;

    public static final String CODE_DIR_POOL = "user-code-pool";

    public static final String DOCKER_USER_CODE_DIR = "/usr/share/java";

    public static final String USER_CODE_JAVA_CLASS_NAME = "Solution.java";

    public static final String USER_CODE_JAVA_FILE_NAME = "Solution";

    public static final String JAVA_CONTAINER_PREFIX = "/";

    public static final String[] DOCKER_JAVAC_CMD = new String[] {"javac", "/usr/share/java/Solution.java"};

    // eg: java -cp  /usr/share/java  Solution 1 2
    public static final String[] DOCKER_JAVA_EXEC_CMD = new String[]{"java", "-cp", DOCKER_USER_CODE_DIR, USER_CODE_JAVA_FILE_NAME};
}