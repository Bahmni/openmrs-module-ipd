package org.openmrs.module.ipd.util;

import java.time.*;

public class DateTimeUtil {
    public static LocalDateTime convertUTCToLocalTimeZone(long utcTime) {
        return Instant.ofEpochSecond(utcTime).atZone(ZoneOffset.systemDefault()).toLocalDateTime();
    }

    public static long convertLocalTimeToUTCEpoc(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toEpochSecond();
    }

    public static long convertLocalTimeToEpoc(LocalDate localDate) {
        return localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}