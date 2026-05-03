package com.example.webbanhang.controller;

import com.example.webbanhang.dto.request.MomoCreatePaymentRequest;
import com.example.webbanhang.dto.response.ApiResponse;
import com.example.webbanhang.dto.response.MomoCreatePaymentResponse;
import com.example.webbanhang.service.MomoPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final MomoPaymentService momoPaymentService;

    @PostMapping("/momo/create")
    public ResponseEntity<ApiResponse<MomoCreatePaymentResponse>> createMomoPayment(
            @RequestBody MomoCreatePaymentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Tạo thanh toán MoMo thành công",
                        momoPaymentService.createPayment(request.getOrderId()))
        );
    }

    @PostMapping("/momo/ipn")
    public ResponseEntity<Void> momoIpn(@RequestBody String body) {
        momoPaymentService.handleIpn(body);
        return ResponseEntity.noContent().build();
    }
}