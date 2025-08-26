package com.raczkowski.app.admin.operator.subscription;

import com.raczkowski.app.accountPremium.entity.PlanPriceDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/operator")
public class OperatorSubscriptionController {

    private final OperatorSubscriptionService operatorSubscriptionService;

    @PutMapping("/change/plan/price")
    void changePlanPrice(@RequestBody PlanPriceDto.ChangePriceRequest changePriceRequest) {
        operatorSubscriptionService.changePlanPrice(changePriceRequest);
    }
}
