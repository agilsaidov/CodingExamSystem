package com.project.judge.constant;

public class JudgeLanguage {
    
    // Popular languages
    public static final int C = 50;
    public static final int CPP = 54;
    public static final int CSHARP = 51;
    public static final int GO = 60;
    public static final int JAVA = 62;
    public static final int JAVASCRIPT = 63;
    public static final int KOTLIN = 78;
    public static final int PYTHON = 71;
    public static final int SWIFT = 83;
    public static final int TYPESCRIPT = 74;
    public static final int PHP = 68;
    public static final int SQL = 82;
    
    private JudgeLanguage() {
        throw new IllegalStateException("Constants error");
    }
    
    public static String getLanguageName(Integer languageId) {
        if (languageId == null) return "Unknown";
        
        return switch (languageId) {
            case C -> "C";
            case CPP -> "C++";
            case CSHARP -> "C#";
            case GO -> "Go";
            case JAVA -> "Java";
            case JAVASCRIPT -> "JavaScript";
            case KOTLIN -> "Kotlin";
            case PYTHON -> "Python";
            case SWIFT -> "Swift";
            case TYPESCRIPT -> "TypeScript";
            case PHP -> "PHP";
            case SQL -> "SQL";
            default -> "Unknown";
        };
    }
}