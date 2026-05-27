package com.enterprise.costiq.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodCostEntryForm {

    private Long id;

    @NotNull(message = "Food item is required")
    private Long foodItemId;

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotNull(message = "Cost center is required")
    private Long costCenterId;

    @NotNull(message = "Fiscal period is required")
    private Long fiscalPeriodId;

    @NotNull(message = "Country is required")
    private Long countryId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotNull(message = "Unit cost is required")
    @DecimalMin(value = "0.0001", message = "Unit cost must be greater than 0")
    private BigDecimal unitCostUsd;

    @Size(max = 50, message = "Invoice ref max 50 characters")
    private String invoiceRef;

    @Size(max = 50, message = "PO number max 50 characters")
    private String poNumber;

    private String notes;

    @NotNull(message = "Entry date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;
}
