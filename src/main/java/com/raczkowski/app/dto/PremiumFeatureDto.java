package com.raczkowski.app.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PremiumFeatureDto {
    private Long id;
    private String featureName;
    private int value;
}