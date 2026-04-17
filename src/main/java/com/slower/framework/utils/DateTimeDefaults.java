package com.slower.framework.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeDefaults {
    private static final DateTimeFormatter DT_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private DateTimeDefaults() {
    }

    public static String defaultPreferredDemoDateTime() {
        // default: tomorrow, next full hour
        LocalDateTime dt = LocalDateTime.now().plusDays(1).withMinute(0).plusHours(1);
        return DT_LOCAL.format(dt);
    }
}

