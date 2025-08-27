package com.raczkowski.app.admin.operator.subscription;

import com.raczkowski.app.accountPremium.entity.ChangePriceRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/operator")
public class OperatorSubscriptionController {

    private final OperatorSubscriptionService operatorSubscriptionService;

    @PutMapping("/change/plan/price")
    void changePlanPrice(@RequestBody ChangePriceRequest changePriceRequest) {
        operatorSubscriptionService.changePlanPrice(changePriceRequest);
    }
}
