package com.lotte.otp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by choi on 2018. 1. 29. PM 3:21.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoResponseMessage {

    private KakaoMessage message;
    private KakaoKeyboard keyboard;

    public KakaoResponseMessage(KakaoMessage message) {
        this.message = message;
    }
}
