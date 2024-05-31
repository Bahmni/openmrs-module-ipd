package org.openmrs.module.ipd.api.util;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTimeUtilTest {

    @Test
    public void shouldConvertEPOCUTCToLocalTimeZone() {
        ZoneId defaultZoneId = TimeZone.getDefault().toZoneId();
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Kolkata")));

        LocalDateTime localDateTime = DateTimeUtil.convertEpocUTCToLocalTimeZone(1690906304);

        assertEquals(2023,localDateTime.getYear());
        assertEquals(8, localDateTime.getMonthValue());
        assertEquals(1, localDateTime.getDayOfMonth());
        assertEquals(21, localDateTime.getHour());
        assertEquals(41, localDateTime.getMinute());
        assertEquals(44, localDateTime.getSecond());

        TimeZone.setDefault(TimeZone.getTimeZone(defaultZoneId));
    }

    @Test
    public void shouldConvertLocalTimeZoneToEPOCUTC() {
        ZoneId defaultZoneId = TimeZone.getDefault().toZoneId();
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Kolkata")));

        LocalDateTime localDateTime = LocalDateTime.of(2023, 8, 1, 21, 41, 44);
        long epocUTCTime = DateTimeUtil.convertLocalDateTimeToUTCEpoc(localDateTime);

        assertEquals(1690906304,epocUTCTime);

        TimeZone.setDefault(TimeZone.getTimeZone(defaultZoneId));
    }

    @Test
    public void shouldConvertDateToLocalDateTime() {
        ZoneId defaultZoneId = TimeZone.getDefault().toZoneId();
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Kolkata")));

        Date date = Date.from(Instant.ofEpochSecond(1690906304));
        LocalDateTime localDateTime = DateTimeUtil.convertDateToLocalDateTime(date);

        assertEquals(1690906304, localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond());

        TimeZone.setDefault(TimeZone.getTimeZone(defaultZoneId));
    }
}