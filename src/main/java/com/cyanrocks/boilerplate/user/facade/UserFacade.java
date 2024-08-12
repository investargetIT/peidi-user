package com.cyanrocks.boilerplate.user.facade;

import com.tobee.brains.common.exception.TobeeBrainsBusinessException;
import com.tobee.brains.common.exception.TobeeBrainsRpcException;
import com.tobee.brains.common.util.LoginUserUtil;
import com.tobee.brains.user.constants.ValidateCodeTypeEnum;
import com.tobee.brains.user.model.request.EmailRegistrationRequest;
import com.tobee.brains.user.model.request.ForgetPasswordRequest;
import com.tobee.brains.user.model.request.UpdatePasswordRequest;
import com.tobee.brains.user.model.request.UpdateUserInfoRequest;
import com.tobee.brains.user.model.vo.UserInfoVO;
import com.tobee.brains.user.validate.errorcode.UserErrorCodeEnum;
import com.tobee.brains.user.validate.service.ValidateCodeService;
import com.tobee.brainservice.share.common.model.Result;
import com.tobee.brainservice.share.user.constants.AuthTypeEnum;
import com.tobee.brainservice.share.user.errorcode.UserServiceCodeEnum;
import com.tobee.brainservice.share.user.model.command.UpdateCredentialCmd;
import com.tobee.brainservice.share.user.model.command.UpdateUserAuthCmd;
import com.tobee.brainservice.share.user.model.command.UpdateUserInfoCmd;
import com.tobee.brainservice.share.user.model.dto.UserAuthDTO;
import com.tobee.brainservice.share.user.model.dto.UserInfoDTO;
import com.tobee.brainservice.share.user.service.UserRemoteService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Component
public class UserFacade {

    @Reference(version = "1.0.0")
    private UserRemoteService userRemoteService;;

    @Autowired
    private ValidateCodeService validateCodeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserInfoVO getCurrentUser() {
        Long userId = LoginUserUtil.getUserId();
        if (Objects.isNull(userId)) {
            return new UserInfoVO();
        }
        Result<UserInfoDTO> result = userRemoteService.getUserInfoById(userId);
        if (!result.isSuccess()) {
            throw new TobeeBrainsRpcException();
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserName(result.getData().getEmail());
        userInfoVO.setNickName(result.getData().getNickName());
        userInfoVO.setShowTutorial(result.getData().getShowTutorial());
        userInfoVO.setEmail(result.getData().getEmail());
        return userInfoVO;
    }

    public UserInfoVO updateCurrentUserInfo(UpdateUserInfoRequest request) {
        Long userId = LoginUserUtil.getUserId();
        if (Objects.isNull(userId)) {
            throw new RuntimeException("current user session is invalid");
        }
        UpdateUserInfoCmd command = new UpdateUserInfoCmd();
        command.setUserId(userId);
        String showTutorial = request.getShowTutorial() ? "y" : "n";
        command.setShowTutorial(showTutorial);
        userRemoteService.updateUserInfo(command);
        Result<UserInfoDTO> result = userRemoteService.getUserInfoById(userId);
        if (!result.isSuccess()) {
            throw new TobeeBrainsRpcException();
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserName(result.getData().getEmail());
        userInfoVO.setNickName(result.getData().getNickName());
        userInfoVO.setShowTutorial(result.getData().getShowTutorial());
        userInfoVO.setEmail(result.getData().getEmail());
        return userInfoVO;
    }

    public void registerUserAccountByMail(EmailRegistrationRequest registrationRequest) {
        validateCodeService.checkCodeEffective(registrationRequest.getEmail(), registrationRequest.getEmailCode(),
            ValidateCodeTypeEnum.EMAIL_REGISTER);
        UpdateUserAuthCmd updateUserAuthCmd = new UpdateUserAuthCmd();
        updateUserAuthCmd.setIdentifier(registrationRequest.getEmail());
        updateUserAuthCmd.setCredential(passwordEncoder.encode(registrationRequest.getPassword()));
        updateUserAuthCmd.setAuthType(AuthTypeEnum.EMAIL_PASSWORD.getKey());
        Result result = userRemoteService.registerNewUserByEmail(updateUserAuthCmd);
        if (!result.isSuccess()) {
            if (UserServiceCodeEnum.REGISTER_EMAIL_EXIST.name().equals(result.getCode())) {
                throw new TobeeBrainsBusinessException(UserErrorCodeEnum.EMAIL_ACCOUNT_ALREADY_EXIST.getCode(),
                    "EMAIL_ACCOUNT_ALREADY_EXIST");
            } else {
                throw new TobeeBrainsRpcException();
            }
        }
    }

    public void updatePasswordByEmail(UpdatePasswordRequest updatePasswordCommand) throws IOException {
        Result<UserAuthDTO> queryResult = userRemoteService
            .selectByIdentifierAndAuthType(updatePasswordCommand.getIdentifier(), AuthTypeEnum.EMAIL_PASSWORD.name());
        if (!queryResult.isSuccess()) {
            if (UserServiceCodeEnum.USER_ACCOUNT_NOT_EXIST.name().equals(queryResult.getCode())) {
                throw new TobeeBrainsBusinessException(UserErrorCodeEnum.USER_ACCOUNT_NOT_EXIST.getCode(),
                    "USER_ACCOUNT_NOT_EXIST");
            } else {
                throw new TobeeBrainsRpcException();
            }
        } else {
            // 由于密码做了加密，因此必须先将旧的密码查询出来，然后与用户填写的进行对比
            if (!passwordEncoder.matches(updatePasswordCommand.getOldPassword(),
                queryResult.getData().getCredential())) {
                throw new TobeeBrainsBusinessException(UserErrorCodeEnum.ORIGINAL_CREDENTIAL_NOT_MATCH.getCode(),
                    "ORIGINAL_CREDENTIAL_NOT_MATCH");
            }
        }
        UpdateCredentialCmd updateCredentialDTO = new UpdateCredentialCmd();
        updateCredentialDTO.setIdentifier(updatePasswordCommand.getIdentifier());
        updateCredentialDTO.setAuthType(AuthTypeEnum.EMAIL_PASSWORD.name());
        updateCredentialDTO.setNewCredential(passwordEncoder.encode(updatePasswordCommand.getNewPassword()));
        Result result = userRemoteService.updateCredentialByIdentifier(updateCredentialDTO);
        if (!result.isSuccess()) {
            throw new TobeeBrainsRpcException();
        }
    }

    public void forgetPasswordByEmail(ForgetPasswordRequest forgetPasswordCommand) {
        validateCodeService.checkCodeEffective(forgetPasswordCommand.getIdentifier(),
            forgetPasswordCommand.getValidateCode(), ValidateCodeTypeEnum.EMAIL_RESET_PASSWORD);
        UpdateCredentialCmd updateCredentialDTO = new UpdateCredentialCmd();
        updateCredentialDTO.setIdentifier(forgetPasswordCommand.getIdentifier());
        updateCredentialDTO.setNewCredential(passwordEncoder.encode(forgetPasswordCommand.getNewPassword()));
        updateCredentialDTO.setAuthType(AuthTypeEnum.EMAIL_PASSWORD.getKey());
        Result result = userRemoteService.updateCredentialByIdentifierAndAuthType(updateCredentialDTO);
        if (!result.isSuccess()) {
            if (UserServiceCodeEnum.USER_ACCOUNT_NOT_EXIST.name().equals(result.getCode())) {
                throw new TobeeBrainsBusinessException(UserErrorCodeEnum.USER_ACCOUNT_NOT_EXIST.getCode(),
                    "USER_ACCOUNT_NOT_EXIST");
            } else {
                throw new TobeeBrainsRpcException();
            }
        }
    }

}
