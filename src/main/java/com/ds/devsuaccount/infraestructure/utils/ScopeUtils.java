package com.ds.devsuaccount.infraestructure.utils;

import org.springframework.util.StringUtils;

public class ScopeUtils {

    public static final String SCOPE_SUFFIX = "SCOPE_SUFFIX";

    public static final String ENV_SCOPE = "SCOPE";

    public static final String LOCAL_SCOPE = "local";

    public static final String TEST_SUFFIX = "test";

    private ScopeUtils() {
    }

    public static void calculateScopeSuffix() {
        String scope = getScopeValue();
        String[] tokens = StringUtils.split(scope, "-");

        if (tokens == null || tokens.length == 0) {
            System.setProperty(SCOPE_SUFFIX, scope);
            return;
        }

        System.setProperty(SCOPE_SUFFIX, tokens[tokens.length - 1]);
    }

    public static boolean isLocalScope() {
        return LOCAL_SCOPE.equalsIgnoreCase(getScopeValue());
    }

    public static boolean isTestScope() {
        return getScopeValue().endsWith(TEST_SUFFIX);
    }

    public static String getScopeValue() {
        String scope = System.getenv(ENV_SCOPE);
        if (StringUtils.hasLength(scope)) {
            return getValidScope(scope);
        }
        return LOCAL_SCOPE;
    }

    private static String getValidScope(String currentScope) {
        if (currentScope.equalsIgnoreCase(LOCAL_SCOPE))
            return currentScope;

        if (currentScope.equalsIgnoreCase("test"))
            return TEST_SUFFIX;

        return LOCAL_SCOPE;
    }

}
