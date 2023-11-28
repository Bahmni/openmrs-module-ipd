package org.openmrs.module.ipd.api.util;

import java.time.*;
import java.util.Date;

public class DateTimeUtil {
    public static LocalDateTime convertEpocUTCToLocalTimeZone(long utcTime) {
        return Instant.ofEpochSecond(utcTime).atZone(ZoneOffset.systemDefault()).toLocalDateTime();
    }

    public static long convertLocalDateTimeToUTCEpoc(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toEpochSecond();
    }

    public static LocalDateTime convertDateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}