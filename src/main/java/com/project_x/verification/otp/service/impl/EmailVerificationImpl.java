package com.project_x.verification.otp.service.impl;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.user.entity.User;
import com.project_x.user.enums.UserType;
import com.project_x.user.service.UserService;
import com.project_x.verification.otp.dto.request.SignUpOtpRequest;
import com.project_x.verification.otp.dto.request.VerifyOtpEventRequest;
import com.project_x.verification.otp.dto.request.VerifyOtpRequest;
import com.project_x.verification.otp.entity.Otp;
import com.project_x.verification.otp.enums.OtpEvent;
import com.project_x.verification.otp.service.EmailVerification;
import com.project_x.verification.otp.service.OtpService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class EmailVerificationImpl implements EmailVerification {
    private final UserService userService;
    private final OtpService otpService;

    public EmailVerificationImpl(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @Override
    public ResponseEntity<ApiResponse> generateSignUpOtp(SignUpOtpRequest signUpOtpRequest) {
        String formattedEmail = signUpOtpRequest.email().toLowerCase().trim();

        userService.findUserByEmail(formattedEmail);

        boolean generateOtp = otpService.handleGenerateOtp(
                signUpOtpRequest.email().toLowerCase().trim(), OtpEvent.SIGN_UP, 300L, "EMAIL VERIFICATION",
                UserType.USER, "otp"
        );

        if (!generateOtp) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseUtil.error(99, "something went wrong", null, null));
        }

        return ResponseEntity.status(HttpStatus.OK).body(ResponseUtil.success(0, "Success", String.format("OTP sent to %s ", formattedEmail), "",null));
    }

    @Override
    public ResponseEntity<ApiResponse> verifyOtp(VerifyOtpRequest verifyOtpRequest) {
        VerifyOtpEventRequest request = VerifyOtpEventRequest.builder()
                .otp(verifyOtpRequest.otp())
                .otpMedium(verifyOtpRequest.otpMedium())
                .otpEvent(OtpEvent.SIGN_UP)
                .build();

        var result =  otpService.validateOtp(verifyOtpRequest.otp(), verifyOtpRequest.otpMedium(), OtpEvent.SIGN_UP);

        if (!result.isValid()) {
            return ResponseEntity
                    .status(result.httpStatus())
                    .body(ResponseUtil.error(99, result.errorMessage(), result.details(), null));
        }

        Otp otp = result.otp();
        otpService.deleteOtp(otp);

        return handleVerifySignUpOtp(otp);
    }


    @Transactional
    @Override
    public ResponseEntity<ApiResponse> handleVerifySignUpOtp(Otp otp) {
        User user = userService.findUserByEmail(otp.getOtpMedium());

        if (user.isEmailVerified()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseUtil.error(99, "User Already verified", "", ""));
        }


        user.setEmailVerified(true); //Marked user as a verified user
        userService.save(user);


//        TODO: SEND WELCOME EMAIL/ OTHER THINGS
//        try {
//            List<Param> params = new ArrayList<>();
//            params.add(Param.builder().name("first_name").value(user.getFirstname()).build());
//            params.add(Param.builder().name("year").value(String.valueOf(LocalDate.now().getYear())).build());
//            messagingHandler.sendEmailNotificationToQueue(List.of(user.getEmail()), new ArrayList<>(), new ArrayList<>(),
//                    MessageType.Template, EmailTemplate.WELCOME_EMAIL_TEMPLATE, "WELCOME TO PROJECTX", "noreply@siryoungtech.com", params, true);
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        }
        return ResponseEntity.status(HttpStatus.OK).body(ResponseUtil.success(0, "Email verification successful" , "","", null));
    }
}
