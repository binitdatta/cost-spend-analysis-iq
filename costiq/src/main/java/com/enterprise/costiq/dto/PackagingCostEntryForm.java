package com.enterprise.costiq.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackagingCostEntryForm {

    private Long id;

    @NotNull(message = "Packaging item is required")
    private Long packagingItemId;

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotNull(message = "Cost center is required")
    private Long costCenterId;

    @NotNull(message = "Fiscal period is required")
    private Long fiscalPeriodId;

    @NotNull(message = "Country is required")
    private Long countryId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity;

    @NotNull(message = "Unit cost is required")
    @DecimalMin(value = "0.0001", message = "Unit cost must be greater than 0")
    private BigDecimal unitCostUsd;

    @Size(max = 50)
    private String invoiceRef;

    @Size(max = 50)
    private String poNumber;

    private String notes;

    @NotNull(message = "Entry date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;
}
