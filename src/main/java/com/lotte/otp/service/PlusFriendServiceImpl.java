package com.lotte.otp.service;

import com.lotte.otp.domain.*;
import com.lotte.otp.exception.GenerateOtpException;
import com.lotte.otp.exception.KeyTimeoutException;
import com.lotte.otp.exception.UnAuthorizedUserException;
import com.lotte.otp.repository.User2NdAuthRepository;
import com.lotte.otp.repository.UserConnectionHistoryRepository;
import com.lotte.otp.repository.UserRepository;
import com.lotte.otp.util.ChattingText;
import com.lotte.otp.util.DateUtils;
import com.lotte.otp.util.OTP;
import com.lotte.otp.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by choi on 2018. 2. 2. PM 2:04.
 */
@Service("plusFriendService")
public class PlusFriendServiceImpl implements PlusFriendService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private User2NdAuthRepository user2NdAuthRepository;
    @Autowired
    private UserConnectionHistoryRepository userConnectionHistoryRepository;
    @Autowired
    private ChatRedisService chatRedisService;

    /**
     * 연동된 회원은 채팅 진행
     * 1. 발급 or 재발급
     * 2. 로그인 내역 확인
     * @param message
     * @return
     */
    @Override
    public String chat(KakaoRequestMessage message) {
        switch (message.getContent()) {
            case ChattingText.REQUEST_OTP_BUTTON:
                LocalDateTime currentDateTime = LocalDateTime.now();
                User2NdAuth user2NdAuth = user2NdAuthRepository.findByKakaoUserKey(message.getUser_key());
                String secretKey = user2NdAuth.getSecretKey();

                try {
                    String otp = OTP.create(DateUtils.convertToLong(currentDateTime), secretKey);
                    user2NdAuth.setLastPublishedDate(currentDateTime);
                    user2NdAuthRepository.save(user2NdAuth);
                    return "OTP : " + otp +
                            "\n발급 일시 : " + currentDateTime +
                            "\n만료 일시 : " + DateUtils.expireMin(currentDateTime, 1);
                } catch (GenerateOtpException e) {
                    logger.info("에러 내용 => " + e.getMessage());
                    return e.getMessage();
                }
            case ChattingText.OTP_EXPIRATION_TIME_BUTTON:
                LocalDateTime publishedDateTime
                        = user2NdAuthRepository.findByKakaoUserKey(message.getUser_key()).getLastPublishedDate();

                if (SecurityUtils.isTimeoutKey(publishedDateTime, 1)) {
                    return "이전에 받은 OTP는 만료되었습니다. 새로운 OTP를 요청하세요.";
                }

                try {
                    int remainSeconds = DateUtils.remainSeconds(publishedDateTime);
                    LocalDateTime expirationTime = DateUtils.expireMin(publishedDateTime, 1);
                    return "만료 일시 : " + expirationTime + "\n현재 OTP는 " + remainSeconds + "초 남았습니다.";
                } catch (KeyTimeoutException kte) {
                    return "이전에 받은 OTP는 만료되었습니다. 새로운 OTP를 요청하세요.";
                }
            case ChattingText.LOGIN_HISTORY_BUTTON:
                UserConnectionHistory history = userConnectionHistoryRepository.findTopByKakaoUserKey(message.getUser_key());
                String text = "[최근 로그인 내역]";
                text += "\n일시 : " + history.getAccessed_date();
                text += "\n접속 환경 : " + history.getOs() + " " + history.getBrowser();
                text += "\nIP : " + history.getIp();
                return text;
            default:
                return ChattingText.NO_MATCHING[(int)(Math.random() * 10) % ChattingText.NO_MATCHING.length];
        }
    }

    /**
     * 웹과 연동 플로우
     * 1. 먼저 연동이 되어 있는지 확인 - 컨트롤러단에서 확인하여 chat()메소드와 분기 호출
     * 2. 안되어있으면 키발급 프로세스 시작
     *
     * @return ChatBotStep.SUCCESS.getMessage() or 실패 메시지
     */
    @Override
    public String connectWebService(KakaoRequestMessage message) {
        UserConnection userConnection = tokenizeText(message.getContent());
        if (userConnection == null) {
            logger.info("[플친 연동 Service] OTP 연동에 실패했습니다. 에러 내용 => 토크나이징 실패");
            return "잘못된 입력입니다.\n" + ChatBotStep.REQUEST_INFO.getMessage();
        }

        try {
            vertifyUserConnection(userConnection);
            User2NdAuth user2NdAuth = new User2NdAuth(
                    userRepository.findById(userConnection.getId()).getUuid(),
                    SecurityUtils.generateSecretKey(),
                    message.getUser_key()
            );
            logger.info("[플친 연동 Service] Auth => " + user2NdAuth.getUuid() + ", " + user2NdAuth.getKakaoUserKey() + ", " + user2NdAuth.getSecretKey());
            user2NdAuthRepository.save(user2NdAuth);
            return ChatBotStep.SUCCESS.getMessage();
        } catch (KeyTimeoutException | UnAuthorizedUserException e) {
            logger.info("[플친 연동 Service] OTP 연동에 실패했습니다. 에러 내용 => " + e.getMessage());
            String responseMessage = "";
            if (e.getErrorCode() == 401) {          //UnAuthorizedUserException
                responseMessage = "잘못된 정보 입력으로 OTP 연동에 실패했습니다. 사용자 정보를 다시 입력해주세요.";
            } else if (e.getErrorCode() == 408) {   //KeyTimeoutException
                responseMessage = "임시 키의 제한시간이 만료되어 OTP 연동에 실패했습니다. 키를 다시 발급받으세요.\n(임시 키 만료시간은 5분입니다.)";
            }
            return responseMessage;
        }
    }

    private UserConnection tokenizeText(String text) {
        UserConnection tokens = null;
        try {
            tokens = SecurityUtils.tokenizeText(text);
        } catch (Exception e) {
            return null;
        }
        return tokens;
    }

    /**
     * 1) 유저 + tempKey + 키 만료일시 => 모두 만족, OK
     * 2) temp_key가 일치하지 않는 경우 => UnAuthorizedUserException
     * 3) 만료일시가 지난 경우 => KeyTimeoutException
     * @param userConnectionQueue
     * @return
     */
    private void vertifyUserConnection(UserConnection userConnectionQueue) {
        int tempKey = chatRedisService.getTempKey(userConnectionQueue.getId());
        if (tempKey != userConnectionQueue.getTemp_key()) {
            throw new UnAuthorizedUserException();
        }
    }

}
