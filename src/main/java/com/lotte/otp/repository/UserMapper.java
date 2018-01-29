package com.lotte.otp.repository;

import com.lotte.otp.domain.UserVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Created by choi on 2018. 1. 26. PM 3:59.
 */
@Repository
public interface UserMapper {

    int duplicateUserId(@Param("id") String id);

    void createUser(@Param("userVO") UserVO userVO);

    int login(@Param("id") String id, @Param("pw") String pw);  //uuid 리턴


}
