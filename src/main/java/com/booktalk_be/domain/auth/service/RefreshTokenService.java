package com.booktalk_be.domain.auth.service;

import com.booktalk_be.domain.auth.model.entity.Refresh_Token;
import com.booktalk_be.domain.member.model.entity.Member;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

public interface RefreshTokenService {
    boolean validateExistMember(int memberId);

    Refresh_Token saveRefreshToken(int memberId);

    Refresh_Token getRefreshTokenByMember(int memberId);
}
