package com.booktalk_be.domain.auth.service;

import com.booktalk_be.domain.auth.model.entity.Refresh_Token;

public interface RefreshTokenService {
    boolean validateExistMember(int memberId);

    Refresh_Token saveRefreshToken(int memberId);

    Refresh_Token getRefreshTokenByMember(int memberId);

    void deleteRefreshToken(int memberId);
}
