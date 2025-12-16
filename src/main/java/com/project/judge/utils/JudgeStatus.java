package com.project.judge.utils;

public class JudgeStatus {
    
    // Processing statuses
    public static final int IN_QUEUE = 1;
    public static final int PROCESSING = 2;
    
    // Success
    public static final int ACCEPTED = 3;
    
    // Failure
    public static final int WRONG_ANSWER = 4;
    public static final int TIME_LIMIT_EXCEEDED = 5;
    public static final int COMPILATION_ERROR = 6;
    public static final int RUNTIME_ERROR_SIGSEGV = 7;
    public static final int RUNTIME_ERROR_SIGXFSZ = 8;
    public static final int RUNTIME_ERROR_SIGFPE = 9;
    public static final int RUNTIME_ERROR_SIGABRT = 10;
    public static final int RUNTIME_ERROR_NZEC = 11;
    public static final int RUNTIME_ERROR_OTHER = 12;
    public static final int INTERNAL_ERROR = 13;
    public static final int EXEC_FORMAT_ERROR = 14;
    
    private JudgeStatus() {
        throw new IllegalStateException("Utility class");
    }
    
    public static boolean isAccepted(Integer statusId) {
        return statusId != null && statusId == ACCEPTED;
    }
    
    public static boolean isProcessing(Integer statusId) {
        return statusId != null && (statusId == IN_QUEUE || statusId == PROCESSING);
    }
    
    public static boolean isError(Integer statusId) {
        return statusId != null && statusId >= COMPILATION_ERROR;
    }
    
    public static boolean isWrongAnswer(Integer statusId) {
        return statusId != null && statusId == WRONG_ANSWER;
    }
    
    public static String getStatusDescription(Integer statusId) {
        if (statusId == null) return "Unknown";
        
        return switch (statusId) {
            case IN_QUEUE -> "In Queue";
            case PROCESSING -> "Processing";
            case ACCEPTED -> "Accepted";
            case WRONG_ANSWER -> "Wrong Answer";
            case TIME_LIMIT_EXCEEDED -> "Time Limit Exceeded";
            case COMPILATION_ERROR -> "Compilation Error";
            case RUNTIME_ERROR_SIGSEGV -> "Runtime Error (SIGSEGV)";
            case RUNTIME_ERROR_SIGXFSZ -> "Runtime Error (SIGXFSZ)";
            case RUNTIME_ERROR_SIGFPE -> "Runtime Error (SIGFPE)";
            case RUNTIME_ERROR_SIGABRT -> "Runtime Error (SIGABRT)";
            case RUNTIME_ERROR_NZEC -> "Runtime Error (NZEC)";
            case RUNTIME_ERROR_OTHER -> "Runtime Error (Other)";
            case INTERNAL_ERROR -> "Internal Error";
            case EXEC_FORMAT_ERROR -> "Exec Format Error";
            default -> "Unknown";
        };
    }
}