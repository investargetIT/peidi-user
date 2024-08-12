package com.cyanrocks.boilerplate.user.security.authentication.assembler;

import com.tobee.brains.user.security.authentication.UserInfoDetails;
import com.tobee.brainservice.share.user.model.dto.UserInfoDTO;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 */
public class UserInfoDetailsAssembler {

    public static UserDetails buildUserDetails(UserInfoDTO userInfoDTO) {
        UserInfoDetails userInfoDetails = new UserInfoDetails();
        userInfoDetails.setUserId(userInfoDTO.getId());
        userInfoDetails.setIdentifier(userInfoDTO.getIdentifier());
        userInfoDetails.setPassword(userInfoDTO.getCredential());
        userInfoDetails.setAuthType(userInfoDTO.getAuthType());
        userInfoDetails.setNickName(userInfoDTO.getNickName());
        userInfoDetails.setPhone(userInfoDTO.getPhone());
        userInfoDetails.setEmail(userInfoDTO.getEmail());
        userInfoDetails.setAdminId(userInfoDTO.getAdminId());
        return userInfoDetails;
    }
}
