package com.example.webbanhang.service.impl;

import com.example.webbanhang.dto.request.AssignVoucherRequest;
import com.example.webbanhang.dto.request.VoucherRequest;
import com.example.webbanhang.dto.response.*;
import com.example.webbanhang.entity.User;
import com.example.webbanhang.entity.UserVoucher;
import com.example.webbanhang.entity.Voucher;
import com.example.webbanhang.enums.VoucherType;
import com.example.webbanhang.exception.BadRequestException;
import com.example.webbanhang.exception.ConflictException;
import com.example.webbanhang.exception.ResourceNotFoundException;
import com.example.webbanhang.repository.UserRepository;
import com.example.webbanhang.repository.UserVoucherRepository;
import com.example.webbanhang.repository.VoucherRepository;
import com.example.webbanhang.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository     voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository        userRepository;

    // ── Mapper ────────────────────────────────────────────────────────────────

    private VoucherResponse toVoucherResponse(Voucher v) {
        return VoucherResponse.builder()
                .voucherId(v.getVoucherId())
                .code(v.getCode())
                .voucherName(v.getVoucherName())
                .voucherType(v.getVoucherType())
                .discountValue(v.getDiscountValue())
                .maxDiscount(v.getMaxDiscount())
                .minOrderAmount(v.getMinOrderAmount())
                .usageLimit(v.getUsageLimit())
                .usedCount(v.getUsedCount())
                .startDate(v.getStartDate())
                .endDate(v.getEndDate())
                .isActive(v.getIsActive())
                .isLoyalOnly(v.getIsLoyalOnly())
                .valid(v.isValid())
                .createdAt(v.getCreatedAt())
                .build();
    }

    private UserVoucherResponse toUserVoucherResponse(UserVoucher uv) {
        return UserVoucherResponse.builder()
                .id(uv.getId())
                .voucher(toVoucherResponse(uv.getVoucher()))
                .isUsed(uv.getIsUsed())
                .usedAt(uv.getUsedAt())
                .assignedAt(uv.getAssignedAt())
                .build();
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private Voucher findVoucherByCode(String code) {
        return voucherRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", "code", code));
    }

    private void validateVoucherUsable(Integer userId, Voucher voucher, BigDecimal orderAmount) {
        if (!voucher.isValid()) {
            throw new BadRequestException("Voucher '" + voucher.getCode() + "' không còn hiệu lực");
        }
        if (orderAmount.compareTo(voucher.getMinOrderAmount()) < 0) {
            throw new BadRequestException(
                    "Đơn hàng tối thiểu " + voucher.getMinOrderAmount() + " để dùng voucher này");
        }
        // Kiểm tra user có voucher này và chưa dùng
        UserVoucher uv = userVoucherRepository
                .findByUserUserIdAndVoucherVoucherId(userId, voucher.getVoucherId())
                .orElseThrow(() -> new BadRequestException("Bạn không có voucher này"));

        if (Boolean.TRUE.equals(uv.getIsUsed())) {
            throw new BadRequestException("Voucher này đã được sử dụng");
        }
    }

    // ── User ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<UserVoucherResponse> getMyVouchers(Integer userId) {
        return userVoucherRepository.findUnusedValidVouchersByUserId(userId)
                .stream().map(this::toUserVoucherResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplyVoucherResponse previewVoucher(Integer userId,
                                               String voucherCode,
                                               BigDecimal orderAmount) {
        Voucher voucher = findVoucherByCode(voucherCode);
        validateVoucherUsable(userId, voucher, orderAmount);

        BigDecimal discount = computeDiscount(voucher, orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discount).max(BigDecimal.ZERO);

        return ApplyVoucherResponse.builder()
                .voucherCode(voucher.getCode())
                .voucherName(voucher.getVoucherName())
                .originalAmount(orderAmount)
                .discountAmount(discount)
                .finalAmount(finalAmount)
                .build();
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public BigDecimal calculateDiscount(Integer userId, String voucherCode, BigDecimal orderAmount) {
        Voucher voucher = findVoucherByCode(voucherCode);
        validateVoucherUsable(userId, voucher, orderAmount);
        return computeDiscount(voucher, orderAmount);
    }

    @Override
    @Transactional
    public void markVoucherUsed(Integer userId, String voucherCode) {
        Voucher voucher = findVoucherByCode(voucherCode);
        int updated = userVoucherRepository.markAsUsed(userId, voucher.getVoucherId());
        if (updated == 0) {
            throw new BadRequestException("Không thể đánh dấu voucher đã dùng");
        }
        voucherRepository.incrementUsedCount(voucher.getVoucherId());
        log.info("[Voucher] User {} dùng voucher {}", userId, voucherCode);
    }

    private BigDecimal computeDiscount(Voucher voucher, BigDecimal orderAmount) {
        if (voucher.getVoucherType() == VoucherType.FIXED) {
            // Giảm cố định, không vượt quá tổng đơn
            return voucher.getDiscountValue().min(orderAmount);
        } else {
            // Giảm theo %
            BigDecimal discount = orderAmount
                    .multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            // Áp trần maxDiscount nếu có
            if (voucher.getMaxDiscount() != null) {
                discount = discount.min(voucher.getMaxDiscount());
            }
            return discount;
        }
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VoucherResponse> getAll(String keyword, Boolean isActive, Pageable pageable) {
        Page<Voucher> page = voucherRepository.findWithFilters(
                StringUtils.hasText(keyword) ? keyword : null,
                null,
                isActive,
                null,
                pageable);
        List<VoucherResponse> content = page.getContent().stream()
                .map(this::toVoucherResponse).toList();
        return PageResponse.of(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getById(Integer voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", voucherId));
        return toVoucherResponse(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse create(VoucherRequest request) {
        if (voucherRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Mã voucher '" + request.getCode() + "' đã tồn tại");
        }
        validateVoucherRequest(request);

        Voucher voucher = Voucher.builder()
                .code(request.getCode().toUpperCase())
                .voucherName(request.getVoucherName())
                .voucherType(request.getVoucherType())
                .discountValue(request.getDiscountValue())
                .maxDiscount(request.getMaxDiscount())
                .minOrderAmount(request.getMinOrderAmount() != null
                        ? request.getMinOrderAmount() : BigDecimal.ZERO)
                .usageLimit(request.getUsageLimit())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isLoyalOnly(request.getIsLoyalOnly() != null ? request.getIsLoyalOnly() : false)
                .build();

        voucherRepository.save(voucher);
        log.info("[Voucher] Tạo voucher: {}", voucher.getCode());
        return toVoucherResponse(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse update(Integer voucherId, VoucherRequest request) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", voucherId));

        // Nếu đổi code, kiểm tra trùng
        if (!voucher.getCode().equalsIgnoreCase(request.getCode())
                && voucherRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Mã voucher '" + request.getCode() + "' đã tồn tại");
        }
        validateVoucherRequest(request);

        voucher.setCode(request.getCode().toUpperCase());
        voucher.setVoucherName(request.getVoucherName());
        voucher.setVoucherType(request.getVoucherType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setMinOrderAmount(request.getMinOrderAmount() != null
                ? request.getMinOrderAmount() : BigDecimal.ZERO);
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) voucher.setIsActive(request.getIsActive());
        if (request.getIsLoyalOnly() != null) voucher.setIsLoyalOnly(request.getIsLoyalOnly());

        return toVoucherResponse(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public void delete(Integer voucherId) {
        if (!voucherRepository.existsById(voucherId)) {
            throw new ResourceNotFoundException("Voucher", voucherId);
        }
        userVoucherRepository.deleteAllByUserId(voucherId); // xóa liên kết user
        voucherRepository.deleteById(voucherId);
        log.info("[Voucher] Xóa voucher id={}", voucherId);
    }

    @Override
    @Transactional
    public void assignToUsers(AssignVoucherRequest request) {
        Voucher voucher = voucherRepository.findById(request.getVoucherId())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", request.getVoucherId()));

        for (Integer userId : request.getUserIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            if (userVoucherRepository.existsByUserUserIdAndVoucherVoucherId(
                    userId, voucher.getVoucherId())) {
                log.warn("[Voucher] User {} đã có voucher {}, bỏ qua", userId, voucher.getCode());
                continue;
            }

            UserVoucher uv = UserVoucher.builder()
                    .user(user)
                    .voucher(voucher)
                    .isUsed(false)
                    .build();
            userVoucherRepository.save(uv);
            log.info("[Voucher] Cấp voucher {} cho user {}", voucher.getCode(), userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserVoucherResponse> getUsersOfVoucher(Integer voucherId, Pageable pageable) {
        if (!voucherRepository.existsById(voucherId)) {
            throw new ResourceNotFoundException("Voucher", voucherId);
        }
        Page<UserVoucher> page = userVoucherRepository
                .findByVoucherVoucherIdOrderByAssignedAtDesc(voucherId, pageable);
        List<UserVoucherResponse> content = page.getContent().stream()
                .map(this::toUserVoucherResponse).toList();
        return PageResponse.of(page, content);
    }

    // ── Validate ──────────────────────────────────────────────────────────────

    private void validateVoucherRequest(VoucherRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        if (request.getVoucherType() == VoucherType.PERCENT) {
            if (request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Phần trăm giảm tối đa là 100%");
            }
        }
    }
}