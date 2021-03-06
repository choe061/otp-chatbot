package com.lotte.otp.util;

import com.lotte.otp.exception.KeyTimeoutException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Created by choi on 2018. 2. 4. PM 5:39.
 */
public class DateUtils {
    public static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    public static String formatDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 H시 m분 s초");
        return localDateTime.format(dateTimeFormatter);
    }

    public static LocalDateTime currentDateTime(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZONE_SEOUL).withZoneSameInstant(ZONE_SEOUL).toLocalDateTime();
    }

    public static long convertToLong(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZONE_SEOUL).withZoneSameInstant(ZONE_SEOUL).toInstant()).getTime();
    }

    /**
     * 만료 시간을 계산해주는 기능
     * @param currentDateTime
     * @param min
     * @return
     */
    public static LocalDateTime expireMin(LocalDateTime currentDateTime, int min) {
        return currentDateTime.plusMinutes(min);
    }

    /**
     * 남은 초를 반환
     * @param expiration
     * @return
     */
    public static int remainSeconds(LocalDateTime expiration) {
        long expirationTime = convertToLong(expiration);
        long currentTime = convertToLong(currentDateTime(LocalDateTime.now()));
        long remainTime = (expirationTime - currentTime) / 1000;
        if (remainTime < 0) {
            throw new KeyTimeoutException();
        }
        return (int) remainTime;
    }
}
