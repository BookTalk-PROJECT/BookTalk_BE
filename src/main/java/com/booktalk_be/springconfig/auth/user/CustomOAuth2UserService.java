package com.booktalk_be.springconfig.auth.user;

import com.booktalk_be.domain.auth.model.entity.AuthenticateType;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.model.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        //oauth2의 소셜 제공 도메인 분류
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        System.out.println("현재 인증된 소셜 로그인 도메인: " + registrationId);

        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = getEmail(registrationId, attributes);

        AuthenticateType authenticateType = castingAuthority(registrationId);

        Member member = memberRepository.findByEmailAndAuthType(email,authenticateType)
                    .orElseGet(() -> memberRepository.save(Member.builder()
                                    .name(registrationId)
                                    .password("-")
                                    .email(email)
                                    .authType(authenticateType)
                                    .build()));
        CustomOAuth2User oAuthUser = new CustomOAuth2User(attributes,member);

        System.out.println("로그 찍었당"+oAuth2User.getAttributes());

        return oAuthUser;
    }

    private String getEmail(String registrationId, Map<String, Object> attributes ){
        if(registrationId.equals("kakao")){
            @SuppressWarnings("unchecked") Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            return (String) kakaoAccount.get("email");
        }else if(registrationId.equals("naver")){
            @SuppressWarnings("unchecked") Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return (String) response.get("email");
        }
        return null;
    }

    private AuthenticateType castingAuthority(String registrationId){
        if(registrationId.equals("kakao")){
            return AuthenticateType.KAKAO;
        }else if(registrationId.equals("naver")){
            return AuthenticateType.NAVER;
        }
        return null;
    }
}
