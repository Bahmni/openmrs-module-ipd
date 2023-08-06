package org.openmrs.module.ipd.api.util;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTimeUtilTest {

    @Test
    public void shouldConvertEPOCUTCToLocalTimeZone() {
        LocalDateTime localDateTime = DateTimeUtil.convertEpocUTCToLocalTimeZone(1690906304);

        Assertions.assertEquals(2023,localDateTime.getYear());
        Assertions.assertEquals(8, localDateTime.getMonthValue());
        Assertions.assertEquals(1, localDateTime.getDayOfMonth());
        Assertions.assertEquals(21, localDateTime.getHour());
        Assertions.assertEquals(41, localDateTime.getMinute());
        Assertions.assertEquals(44, localDateTime.getSecond());
    }

    @Test
    public void shouldConvertLocalTimeZoneToEPOCUTC() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 8, 1, 21, 41, 44);
        long epocUTCTime = DateTimeUtil.convertLocalDateTimeToUTCEpoc(localDateTime);

        Assertions.assertEquals(1690906304,epocUTCTime);
    }

    @Test
    public void shouldConvertDateToLocalDateTime() {
        Date date = Date.from(Instant.ofEpochSecond(1690906304));
        LocalDateTime localDateTime = DateTimeUtil.convertDateToLocalDateTime(date);

        Assertions.assertEquals(1690906304, localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond());
    }
}