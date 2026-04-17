package com.slower.framework.utils;

public final class XPathUtils {
    private XPathUtils() {
    }

    public static String literal(String value) {
        if (value == null) {
            return "''";
        }
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        StringBuilder sb = new StringBuilder("concat(");
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            String part = String.valueOf(chars[i]);
            if (i > 0) {
                sb.append(", ");
            }
            if ("'".equals(part)) {
                sb.append("\"").append(part).append("\"");
            } else {
                sb.append("'").append(part).append("'");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String ciContains(String xPathTextExpr, String needle) {
        String n = needle == null ? "" : needle.toLowerCase();
        return "contains(translate(" + xPathTextExpr + ", 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), " + literal(n) + ")";
    }
}

