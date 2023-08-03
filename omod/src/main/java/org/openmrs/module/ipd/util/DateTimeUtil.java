package org.openmrs.module.ipd.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class DateTimeUtil {
    public static LocalDateTime convertUTCToLocalTimeZone(long utcTime) {
        return Instant.ofEpochSecond(utcTime).atZone(ZoneOffset.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime convertLocalTimeToUTC(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
    }
}