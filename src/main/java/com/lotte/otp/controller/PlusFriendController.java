package com.lotte.otp.controller;

import com.lotte.otp.domain.KakaoKeyboard;
import com.lotte.otp.domain.KakaoMessage;
import com.lotte.otp.domain.KakaoRequestMessage;
import com.lotte.otp.domain.KakaoResponseMessage;
import com.lotte.otp.domain.type.ChatBotStep;
import com.lotte.otp.domain.type.UserAuthStatus;
import com.lotte.otp.service.ChatRedisService;
import com.lotte.otp.service.PlusFriendService;
import com.lotte.otp.service.User2NdAuthService;
import com.lotte.otp.util.ChattingText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by choi on 2018. 1. 29. PM 2:31.
 */
@RestController
@RequestMapping(value = "/kakaoApi", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PlusFriendController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private User2NdAuthService user2NdAuthService;
    @Autowired
    private PlusFriendService plusFriendService;
    @Autowired
    private ChatRedisService chatRedisService;

    @RequestMapping(value = "/keyboard", method = RequestMethod.GET)
    public KakaoKeyboard getKeyboard() {
        return ChattingText.DEFAULT_KEYBOARD;
    }

    /**
     * 카카오톡 메시지를 처리하는 컨트롤러
     * @param message
     * @return
     */
    @RequestMapping(value = "/message", method = RequestMethod.POST)
    public KakaoResponseMessage message(@RequestBody KakaoRequestMessage message) {
//        KakaoResponseMessage response;
//        //연동이 되어있는 회원의 경우
//        if (UserAuthStatus.CONNECTION_OTP.equals(user2NdAuthService.isUser2NdAuthWithUserKey(message.getUser_key()))) {
//            String responseMessage = plusFriendService.chat(message);
//            response = new KakaoResponseMessage(
//                    new KakaoMessage(responseMessage),
//                    new KakaoKeyboard("buttons", new String[]{
//                            ChattingText.REQUEST_OTP_BUTTON,
//                            ChattingText.OTP_EXPIRATION_TIME_BUTTON,
//                            ChattingText.LOGIN_HISTORY_BUTTON
//                    })
//            );
//        } else {
//            //연동이 되지 않은 회원의 경우
//            String step = String.valueOf(chatRedisService.getStep(message.getUser_key()));
//            if (ChatBotStep.valueOf(step) == ChatBotStep.NO_BASE
//                    && (message.getContent().equals("아이디 등록") || message.getContent().equals(ChattingText.REGIST_ID_BUTTON))) {
//                chatRedisService.nextStep(message.getUser_key());
//                step = String.valueOf(chatRedisService.getStep(message.getUser_key()));
//            }
//
//            switch (ChatBotStep.valueOf(step)) {
//                case NO_BASE:
//                    response = new KakaoResponseMessage(
//                            new KakaoMessage(ChatBotStep.NO_BASE.getMessage()),
//                            new KakaoKeyboard("buttons", new String[]{ChattingText.REGIST_ID_BUTTON})
//                    );
//
//                    if (message.getContent().equals("아이디 등록") || message.getContent().equals(ChattingText.REGIST_ID_BUTTON)) {
//                        chatRedisService.nextStep(message.getUser_key());
//                    }
//                    break;
//                case REQUEST_INFO:
//                    response = new KakaoResponseMessage(
//                            new KakaoMessage(ChatBotStep.REQUEST_INFO.getMessage())
//                    );
//
//                    chatRedisService.nextStep(message.getUser_key());
//                    break;
//                case SUCCESS:
//                    String responseMessage = plusFriendService.connectWebService(message);
//                    if (responseMessage.equals(ChatBotStep.SUCCESS.getMessage())) {
//                        response = new KakaoResponseMessage(
//                                new KakaoMessage(responseMessage),
//                                new KakaoKeyboard("buttons", new String[]{
//                                        ChattingText.REQUEST_OTP_BUTTON,
//                                        ChattingText.OTP_EXPIRATION_TIME_BUTTON,
//                                        ChattingText.LOGIN_HISTORY_BUTTON
//                                })
//                        );
//                    } else {    //Exception이 발생한 경우
//                        response = new KakaoResponseMessage(new KakaoMessage(responseMessage));
////                        chatRedisService.setStep(message.getUser_key(), ChatBotStep.REQUEST_INFO);
//                    }
//                    break;
//                default:
//                    logger.error("[카카오 플러스친구] => ChatBotStep is Null");
//                    response = new KakaoResponseMessage(
//                            new KakaoMessage("현재 기능이 정상적으로 작동하지 않습니다. 관리자 채팅으로 문의주세요.")
//                    );
//                    break;
//            }
//        }

        return plusFriendService.getMessage(message);
    }

}
