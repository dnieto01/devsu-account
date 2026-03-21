package com.ds.devsuaccount.infraestructure.utils;

import org.springframework.util.StringUtils;

public class ScopeUtils {

    public static final String SCOPE_SUFFIX = "SCOPE_SUFFIX";

    public static final String ENV_SCOPE = "SCOPE";

    public static final String LOCAL_SCOPE = "local";

    public static final String TEST_SUFFIX = "test";

    public static final String PROD_SCOPE = "prod";

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
        if (!StringUtils.hasLength(currentScope)) {
            return LOCAL_SCOPE;
        }
        String trimmed = currentScope.trim();

        if (trimmed.equalsIgnoreCase(LOCAL_SCOPE)
                || trimmed.equalsIgnoreCase(TEST_SUFFIX)
                || trimmed.equalsIgnoreCase(PROD_SCOPE)
                || trimmed.equalsIgnoreCase("dev")) {
            return trimmed.toLowerCase();
        }

        if (trimmed.contains("-")) {
            return trimmed;
        }

        return trimmed.toLowerCase();
    }

}
