/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.api.util;

import java.time.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    public static Date convertLocalDateTimeDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date convertEpochTimeToDate(Long utcTime) {
        return new Date(TimeUnit.SECONDS.toMillis(utcTime));
    }


}