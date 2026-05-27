package com.enterprise.costiq.dto;

import com.enterprise.costiq.entity.CampaignMarketingCost;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignMarketingCostForm {

    private Long id;

    @NotNull(message = "Campaign is required")
    private Long campaignId;

    @NotNull(message = "Cost center is required")
    private Long costCenterId;

    @NotNull(message = "Fiscal period is required")
    private Long fiscalPeriodId;

    @NotNull(message = "Cost type is required")
    private CampaignMarketingCost.CostType costType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amountUsd;

    @Size(max = 200)
    private String vendorName;

    @Size(max = 50)
    private String invoiceRef;

    private String description;

    @NotNull(message = "Entry date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;
}
