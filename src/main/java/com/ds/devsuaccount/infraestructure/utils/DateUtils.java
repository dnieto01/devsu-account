package com.ds.devsuaccount.infraestructure.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DateUtils {

    public static LocalDate parseShortDate(String date) {
        return LocalDate.parse(date);
    }

    public static Boolean isTodayString(String date) {
        LocalDate today = LocalDate.now();
        LocalDate compareDate = parseShortDate(date);
        return compareDate.isEqual(today);
    }
}
