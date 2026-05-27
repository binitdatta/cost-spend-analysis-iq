package com.enterprise.costiq.dto;

import com.enterprise.costiq.entity.CampaignToyAllocation;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignToyAllocationForm {

    private Long id;

    @NotNull(message = "Campaign is required")
    private Long campaignId;

    @NotNull(message = "Toy item is required")
    private Long toyItemId;

    @NotNull(message = "Country is required")
    private Long countryId;

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotNull(message = "Fiscal period is required")
    private Long fiscalPeriodId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity;

    @NotNull(message = "Unit cost is required")
    @DecimalMin(value = "0.0001", message = "Unit cost must be greater than 0")
    private BigDecimal unitCostUsd;

    private CampaignToyAllocation.DistributionChannel distributionChannel;

    @Size(max = 50)
    private String invoiceRef;

    @Size(max = 50)
    private String poNumber;

    private String notes;

    @NotNull(message = "Entry date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;
}
