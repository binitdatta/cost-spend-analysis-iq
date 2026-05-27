package com.enterprise.costiq.service;

import com.enterprise.costiq.dto.PackagingCostEntryForm;
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
public class PackagingCostService {

    private final PackagingCostEntryRepository entryRepo;
    private final PackagingItemRepository packagingItemRepo;
    private final SupplierRepository supplierRepo;
    private final CostCenterRepository costCenterRepo;
    private final FiscalPeriodRepository periodRepo;
    private final CountryRepository countryRepo;

    @Transactional(readOnly = true)
    public List<PackagingCostEntry> findAll() {
        return entryRepo.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Page<PackagingCostEntry> findPaged(Pageable pageable) {
        return entryRepo.findAllWithDetailsPaged(pageable);
    }

    @Transactional(readOnly = true)
    public PackagingCostEntry findById(Long id) {
        return entryRepo.findByIdWithDetails(id)
            .orElseThrow(() -> new NoSuchElementException("Packaging cost entry not found: " + id));
    }

    @Transactional
    public PackagingCostEntry create(PackagingCostEntryForm form) {
        PackagingCostEntry entry = new PackagingCostEntry();
        applyForm(entry, form);
        entry.setCreatedBy(currentUser());
        return entryRepo.save(entry);
    }

    @Transactional
    public PackagingCostEntry update(Long id, PackagingCostEntryForm form) {
        PackagingCostEntry entry = entryRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Packaging cost entry not found: " + id));
        applyForm(entry, form);
        entry.setUpdatedBy(currentUser());
        entry.setUpdatedAt(LocalDateTime.now());
        return entryRepo.save(entry);
    }

    @Transactional
    public void delete(Long id) {
        entryRepo.deleteById(id);
    }

    public PackagingCostEntryForm toForm(PackagingCostEntry entry) {
        PackagingCostEntryForm form = new PackagingCostEntryForm();
        form.setId(entry.getId());
        form.setPackagingItemId(entry.getPackagingItem().getId());
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

    private void applyForm(PackagingCostEntry entry, PackagingCostEntryForm form) {
        entry.setPackagingItem(packagingItemRepo.findById(form.getPackagingItemId())
            .orElseThrow(() -> new NoSuchElementException("Packaging item not found")));
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
