package com.example.moviebooking.auth.service;
import com.example.moviebooking.auth.dto.*;
import com.example.moviebooking.auth.entity.*;
import com.example.moviebooking.auth.repository.UserRepository;
import com.example.moviebooking.auth.security.JwtService;
import com.example.moviebooking.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.Locale;
@Service public class AuthService {
 private final UserRepository users; private final OtpService otp; private final MobileNormalizer mobile; private final JwtService jwt;
 private final String adminEmail;
 public AuthService(UserRepository users,OtpService otp,MobileNormalizer mobile,JwtService jwt,@Value("${app.admin.email:}") String adminEmail){this.users=users;this.otp=otp;this.mobile=mobile;this.jwt=jwt;this.adminEmail=normalize(adminEmail);}
 private static String normalize(String email){return email==null?"":email.trim().toLowerCase(Locale.ROOT);}
 private User account(String address){return users.findByEmailIgnoreCase(address).orElse(null);}
 private void requireAuthorizedAdmin(String address){User existing=account(address);if(existing!=null&&existing.role==Role.ADMIN&&!address.equals(adminEmail))throw new ApiException(403,"ADMIN_EMAIL_UNAUTHORIZED","This email is not authorized for admin login.");}
 public OtpResponse requestEmail(EmailOtpRequest r){String address=normalize(r.email());requireAuthorizedAdmin(address);return otp.requestEmail(address);}
 @Transactional(noRollbackFor=ApiException.class) public EmailOtpResult verifyEmail(EmailOtpVerifyRequest r){String address=normalize(r.email());User existing=account(address);boolean reservedAdmin=!adminEmail.isBlank()&&adminEmail.equals(address);if((reservedAdmin&&(existing==null||existing.role!=Role.ADMIN))||(existing!=null&&existing.role==Role.ADMIN&&!reservedAdmin))throw new ApiException(403,"ADMIN_EMAIL_UNAUTHORIZED","This email is not authorized for admin login.");otp.verifyEmail(address,r.otp());existing=account(address);if(existing!=null&&!existing.enabled)throw new ApiException(401,"UNAUTHORIZED","Unable to authenticate this account");if(existing==null||existing.name==null||existing.name.isBlank()||existing.name.equals("Movie lover")||existing.mobile==null||existing.mobile.isBlank())return new EmailOtpResult(true,null,null,null);return authenticated(existing);}
 @Transactional(noRollbackFor=ApiException.class) public EmailOtpResult registerEmail(EmailOtpRegistrationRequest r){String address=normalize(r.email());if(!adminEmail.isBlank()&&adminEmail.equals(address))throw new ApiException(403,"ADMIN_EMAIL_UNAUTHORIZED","This email is not authorized for admin login.");requireAuthorizedAdmin(address);otp.requireVerifiedEmail(address);String number=mobile.normalize(r.mobile());var emailUser=account(address);var numberUser=users.findByMobile(number).orElse(null);if(numberUser!=null&&emailUser!=null&&!numberUser.id.equals(emailUser.id))throw new ApiException(409,"DUPLICATE_MOBILE","This mobile number is already in use");if(numberUser!=null&&emailUser==null&&numberUser.email!=null&&!numberUser.email.isBlank())throw new ApiException(409,"DUPLICATE_MOBILE","This mobile number is already in use");var user=emailUser!=null?emailUser:numberUser!=null?numberUser:new User();if(!user.enabled)throw new ApiException(401,"UNAUTHORIZED","Unable to authenticate this account");user.name=r.name().trim();user.mobile=number;user.email=address;user=users.saveAndFlush(user);otp.consumeVerifiedEmail(address);return authenticated(user);}
 private EmailOtpResult authenticated(User user){return new EmailOtpResult(false,UserResponse.of(user),jwt.generate(user),"Bearer");}
 public User get(String id){return users.findById(id).orElseThrow(()->new ApiException(404,"USER_NOT_FOUND","User not found"));}
 public UserResponse current(String id){return UserResponse.of(get(id));}
 @Transactional public UserResponse update(String id,UpdateProfileRequest r){var u=get(id);String email=r.email().trim().toLowerCase(Locale.ROOT);if(!email.equalsIgnoreCase(u.email))throw new ApiException(400,"EMAIL_CHANGE_REQUIRES_VERIFICATION","Changing your sign-in email requires email verification");u.name=r.name().trim();return UserResponse.of(users.saveAndFlush(u));}
 public Page<UserResponse> list(int page,int size){return users.findAll(PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,100)),Sort.by("createdAt").descending())).map(UserResponse::of);}
 @Transactional public UserResponse role(String id,Role role){var u=get(id);u.role=role;return UserResponse.of(users.save(u));}
 @Transactional public UserResponse status(String id,boolean enabled){var u=get(id);u.enabled=enabled;return UserResponse.of(users.save(u));}
}
