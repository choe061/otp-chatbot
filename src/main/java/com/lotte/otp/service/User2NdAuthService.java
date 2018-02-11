package com.lotte.otp.service;

import com.lotte.otp.domain.BlockUserVO;
import com.lotte.otp.domain.User2NdAuthVO;
import com.lotte.otp.domain.UserAuthStatus;
import com.lotte.otp.repository.BlockUserMapper;
import com.lotte.otp.repository.User2NdAuthMapper;
import com.lotte.otp.util.DateUtils;
import com.lotte.otp.util.OTP;
import com.lotte.otp.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by choi on 2018. 1. 29. AM 10:12.
 */
@Service
@Transactional
public class User2NdAuthService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private User2NdAuthMapper user2NdAuthMapper;
    @Autowired
    private BlockUserMapper blockUserMapper;

    private static final int EXIST_USER_AUTH = 1;

    public UserAuthStatus isUser2NdAuthWithID(String id) {
        int user = user2NdAuthMapper.findUser2NdAuthWithID(id);
        if (user >= EXIST_USER_AUTH) {
            return UserAuthStatus.CONNECTION_OTP;
        }
        return UserAuthStatus.UNAUTHORIZED;
    }

    public UserAuthStatus isUser2NdAuthWithUserKey(String kakaoUserKey) {
        int user = user2NdAuthMapper.findUser2NdAuthWithUserKey(kakaoUserKey);
        if (user >= EXIST_USER_AUTH) {
            return UserAuthStatus.CONNECTION_OTP;
        }
        return UserAuthStatus.UNAUTHORIZED;
    }

    public boolean authenticateOtp(String id, String otp) {
        User2NdAuthVO user2NdAuth = user2NdAuthMapper.getUser2ndAuth(id);
        boolean result = OTP.vertify(
                DateUtils.convertStrToLongDate(user2NdAuth.getLast_published_at()),
                user2NdAuth.getSecret_key(),
                otp
                );
        if (result && !SecurityUtils.isTimeoutKey(DateUtils.convertStrToLongDate(user2NdAuth.getLast_published_at()), 1)) {
            return true;
        }
        return false;
    }

    public void blockUserIp(BlockUserVO blockUser) {
        blockUserMapper.blockUserIp(blockUser);
    }

    public boolean getBlockUserIp(String id, String ip) {
        int block = blockUserMapper.getBlockUserIp(id, ip);
        if (block >= 1) {
            return true;
        }
        return false;
    }
}
