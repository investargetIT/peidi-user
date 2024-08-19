package com.cyanrocks.boilerplate.facade;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.constants.ErrorCodeEnum;
import com.cyanrocks.boilerplate.constants.ValidateCodeTypeEnum;
import com.cyanrocks.boilerplate.dao.entity.User;
import com.cyanrocks.boilerplate.dao.mapper.UserMapper;
import com.cyanrocks.boilerplate.exception.BusinessException;
import com.cyanrocks.boilerplate.vo.request.EmailRegistrationRequest;
import com.cyanrocks.boilerplate.validate.service.ValidateCodeService;
import com.cyanrocks.boilerplate.vo.request.ForgetPasswordRequest;
import com.cyanrocks.boilerplate.vo.request.SmsRegistrationRequest;
import com.cyanrocks.boilerplate.vo.request.UpdatePasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Component
public class UserFacade {

    @Autowired
    private ValidateCodeService validateCodeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;


    public void registerUserAccountByMail(EmailRegistrationRequest registrationRequest) {
        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        if (null != userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getEmail,registrationRequest.getEmail()))){
            throw new BusinessException(ErrorCodeEnum.EMAIL_ACCOUNT_ALREADY_EXIST.getCode(),
                    "EMAIL_ACCOUNT_ALREADY_EXIST");
        }
        userMapper.insert(user);
    }

    public void registerUserAccountBySms(SmsRegistrationRequest registrationRequest) {
        validateCodeService.checkCodeEffective(registrationRequest.getMobile(), registrationRequest.getMobileCode(),
            ValidateCodeTypeEnum.SMS_REGISTER);
        User user = new User();
        user.setMobile(registrationRequest.getMobile());
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        if (null != userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getEmail,registrationRequest.getMobile()))){
            throw new BusinessException(ErrorCodeEnum.PHONE_ACCOUNT_ALREADY_EXIST.getCode(),
                    "PHONE_ACCOUNT_ALREADY_EXIST");
        }
        userMapper.insert(user);
    }

    public void updatePasswordByEmail(UpdatePasswordRequest updatePasswordCommand) throws IOException {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getEmail,updatePasswordCommand.getIdentifier()));
        if (null == user) {
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST.getCode(),
                    "USER_ACCOUNT_NOT_EXIST");
        } else {
            // 由于密码做了加密，因此必须先将旧的密码查询出来，然后与用户填写的进行对比
            if (!passwordEncoder.matches(updatePasswordCommand.getOldPassword(),user.getPassword())) {
                throw new BusinessException(ErrorCodeEnum.ORIGINAL_CREDENTIAL_NOT_MATCH.getCode(),
                    "ORIGINAL_CREDENTIAL_NOT_MATCH");
            }
        }
        user.setPassword(passwordEncoder.encode(updatePasswordCommand.getNewPassword()));
        userMapper.update(user,Wrappers.<User>lambdaQuery().eq(User::getEmail,updatePasswordCommand.getIdentifier()));
    }

    public void updatePasswordBySms(UpdatePasswordRequest updatePasswordCommand) throws IOException {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getMobile,updatePasswordCommand.getIdentifier()));
        if (null == user) {
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST.getCode(),
                    "USER_ACCOUNT_NOT_EXIST");
        } else {
            // 由于密码做了加密，因此必须先将旧的密码查询出来，然后与用户填写的进行对比
            if (!passwordEncoder.matches(updatePasswordCommand.getOldPassword(),user.getPassword())) {
                throw new BusinessException(ErrorCodeEnum.ORIGINAL_CREDENTIAL_NOT_MATCH.getCode(),
                        "ORIGINAL_CREDENTIAL_NOT_MATCH");
            }
        }
        user.setPassword(passwordEncoder.encode(updatePasswordCommand.getNewPassword()));
        userMapper.update(user,Wrappers.<User>lambdaQuery().eq(User::getMobile,updatePasswordCommand.getIdentifier()));
    }

    public void forgetPasswordByEmail(ForgetPasswordRequest forgetPasswordCommand) {
        validateCodeService.checkCodeEffective(forgetPasswordCommand.getIdentifier(), forgetPasswordCommand.getValidateCode(),
                ValidateCodeTypeEnum.SMS_RESET_PASSWORD);
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getEmail,forgetPasswordCommand.getIdentifier()));
        if (null == user) {
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST.getCode(),
                    "USER_ACCOUNT_NOT_EXIST");
        }
        user.setPassword(passwordEncoder.encode(forgetPasswordCommand.getNewPassword()));
        userMapper.update(user,Wrappers.<User>lambdaQuery().eq(User::getEmail,forgetPasswordCommand.getIdentifier()));
    }

    public void forgetPasswordBySms(ForgetPasswordRequest forgetPasswordCommand) {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getEmail,forgetPasswordCommand.getIdentifier()));
        if (null == user) {
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST.getCode(),
                    "USER_ACCOUNT_NOT_EXIST");
        }
        user.setPassword(passwordEncoder.encode(forgetPasswordCommand.getNewPassword()));
        userMapper.update(user,Wrappers.<User>lambdaQuery().eq(User::getEmail,forgetPasswordCommand.getIdentifier()));
    }

}
