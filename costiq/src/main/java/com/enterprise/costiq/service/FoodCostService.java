package com.enterprise.costiq.service;

import com.enterprise.costiq.dto.FoodCostEntryForm;
import com.enterprise.costiq.entity.*;
import com.enterprise.costiq.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FoodCostService {

    private final FoodCostEntryRepository entryRepo;
    private final FoodItemRepository foodItemRepo;
    private final SupplierRepository supplierRepo;
    private final CostCenterRepository costCenterRepo;
    private final FiscalPeriodRepository periodRepo;
    private final CountryRepository countryRepo;

    @Transactional(readOnly = true)
    public List<FoodCostEntry> findAll() {
        return entryRepo.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Page<FoodCostEntry> findPaged(Pageable pageable) {
        return entryRepo.findAllWithDetailsPaged(pageable);
    }

    @Transactional(readOnly = true)
    public FoodCostEntry findById(Long id) {
        return entryRepo.findByIdWithDetails(id)
            .orElseThrow(() -> new NoSuchElementException("Food cost entry not found: " + id));
    }

    @Transactional
    public FoodCostEntry create(FoodCostEntryForm form) {
        FoodCostEntry entry = new FoodCostEntry();
        applyForm(entry, form);
        entry.setCreatedBy(currentUser());
        return entryRepo.save(entry);
    }

    @Transactional
    public FoodCostEntry update(Long id, FoodCostEntryForm form) {
        FoodCostEntry entry = entryRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Food cost entry not found: " + id));
        applyForm(entry, form);
        entry.setUpdatedBy(currentUser());
        entry.setUpdatedAt(LocalDateTime.now());
        return entryRepo.save(entry);
    }

    @Transactional
    public void delete(Long id) {
        entryRepo.deleteById(id);
    }

    public FoodCostEntryForm toForm(FoodCostEntry entry) {
        FoodCostEntryForm form = new FoodCostEntryForm();
        form.setId(entry.getId());
        form.setFoodItemId(entry.getFoodItem().getId());
        form.setSupplierId(entry.getSupplier().getId());
        form.setCostCenterId(entry.getCostCenter().getId());
        form.setFiscalPeriodId(entry.getFiscalPeriod().getId());
        form.setCountryId(entry.getCountry().getId());
        form.setQuantity(entry.getQuantity());
        form.setUnitCostUsd(entry.getUnitCostUsd());
        form.setInvoiceRef(entry.getInvoiceRef());
        form.setPoNumber(entry.getPoNumber());
        form.setNotes(entry.getNotes());
        form.setEntryDate(entry.getEntryDate());
        return form;
    }

    private void applyForm(FoodCostEntry entry, FoodCostEntryForm form) {
        entry.setFoodItem(foodItemRepo.findById(form.getFoodItemId())
            .orElseThrow(() -> new NoSuchElementException("Food item not found")));
        entry.setSupplier(supplierRepo.findById(form.getSupplierId())
            .orElseThrow(() -> new NoSuchElementException("Supplier not found")));
        entry.setCostCenter(costCenterRepo.findById(form.getCostCenterId())
            .orElseThrow(() -> new NoSuchElementException("Cost center not found")));
        entry.setFiscalPeriod(periodRepo.findById(form.getFiscalPeriodId())
            .orElseThrow(() -> new NoSuchElementException("Fiscal period not found")));
        entry.setCountry(countryRepo.findById(form.getCountryId())
            .orElseThrow(() -> new NoSuchElementException("Country not found")));
        entry.setQuantity(form.getQuantity());
        entry.setUnitCostUsd(form.getUnitCostUsd());
        entry.setInvoiceRef(form.getInvoiceRef());
        entry.setPoNumber(form.getPoNumber());
        entry.setNotes(form.getNotes());
        entry.setEntryDate(form.getEntryDate());
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
