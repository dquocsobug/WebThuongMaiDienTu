package com.example.webbanhang.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserVoucherResponse {

    private final Integer         id;
    private final VoucherResponse voucher;
    private final Boolean         isUsed;
    private final LocalDateTime   usedAt;
    private final LocalDateTime   assignedAt;
}