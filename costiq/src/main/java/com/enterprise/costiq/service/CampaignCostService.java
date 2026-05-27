package com.enterprise.costiq.service;

import com.enterprise.costiq.dto.CampaignMarketingCostForm;
import com.enterprise.costiq.dto.CampaignToyAllocationForm;
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
public class CampaignCostService {

    private final CampaignToyAllocationRepository toyRepo;
    private final CampaignMarketingCostRepository marketingRepo;
    private final CampaignRepository campaignRepo;
    private final ToyItemRepository toyItemRepo;
    private final CountryRepository countryRepo;
    private final SupplierRepository supplierRepo;
    private final FiscalPeriodRepository periodRepo;
    private final CostCenterRepository costCenterRepo;

    // ---- TOY ALLOCATIONS ----

    @Transactional(readOnly = true)
    public List<CampaignToyAllocation> findAllToyAllocations() {
        return toyRepo.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Page<CampaignToyAllocation> findToyAllocationsPaged(Pageable pageable) {
        return toyRepo.findAllWithDetailsPaged(pageable);
    }

    @Transactional(readOnly = true)
    public CampaignToyAllocation findToyAllocationById(Long id) {
        return toyRepo.findByIdWithDetails(id)
            .orElseThrow(() -> new NoSuchElementException("Toy allocation not found: " + id));
    }

    @Transactional
    public CampaignToyAllocation createToyAllocation(CampaignToyAllocationForm form) {
        CampaignToyAllocation a = new CampaignToyAllocation();
        applyToyForm(a, form);
        a.setCreatedBy(currentUser());
        return toyRepo.save(a);
    }

    @Transactional
    public CampaignToyAllocation updateToyAllocation(Long id, CampaignToyAllocationForm form) {
        CampaignToyAllocation a = toyRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Toy allocation not found: " + id));
        applyToyForm(a, form);
        a.setUpdatedBy(currentUser());
        a.setUpdatedAt(LocalDateTime.now());
        return toyRepo.save(a);
    }

    @Transactional
    public void deleteToyAllocation(Long id) {
        toyRepo.deleteById(id);
    }

    public CampaignToyAllocationForm toToyForm(CampaignToyAllocation a) {
        CampaignToyAllocationForm form = new CampaignToyAllocationForm();
        form.setId(a.getId());
        form.setCampaignId(a.getCampaign().getId());
        form.setToyItemId(a.getToyItem().getId());
        form.setCountryId(a.getCountry().getId());
        form.setSupplierId(a.getSupplier().getId());
        form.setFiscalPeriodId(a.getFiscalPeriod().getId());
        form.setQuantity(a.getQuantity());
        form.setUnitCostUsd(a.getUnitCostUsd());
        form.setDistributionChannel(a.getDistributionChannel());
        form.setInvoiceRef(a.getInvoiceRef());
        form.setPoNumber(a.getPoNumber());
        form.setNotes(a.getNotes());
        form.setEntryDate(a.getEntryDate());
        return form;
    }

    private void applyToyForm(CampaignToyAllocation a, CampaignToyAllocationForm form) {
        a.setCampaign(campaignRepo.findById(form.getCampaignId())
            .orElseThrow(() -> new NoSuchElementException("Campaign not found")));
        a.setToyItem(toyItemRepo.findById(form.getToyItemId())
            .orElseThrow(() -> new NoSuchElementException("Toy item not found")));
        a.setCountry(countryRepo.findById(form.getCountryId())
            .orElseThrow(() -> new NoSuchElementException("Country not found")));
        a.setSupplier(supplierRepo.findById(form.getSupplierId())
            .orElseThrow(() -> new NoSuchElementException("Supplier not found")));
        a.setFiscalPeriod(periodRepo.findById(form.getFiscalPeriodId())
            .orElseThrow(() -> new NoSuchElementException("Fiscal period not found")));
        a.setQuantity(form.getQuantity());
        a.setUnitCostUsd(form.getUnitCostUsd());
        a.setDistributionChannel(
            form.getDistributionChannel() != null
                ? form.getDistributionChannel()
                : CampaignToyAllocation.DistributionChannel.RETAIL);
        a.setInvoiceRef(form.getInvoiceRef());
        a.setPoNumber(form.getPoNumber());
        a.setNotes(form.getNotes());
        a.setEntryDate(form.getEntryDate());
    }

    // ---- MARKETING COSTS ----

    @Transactional(readOnly = true)
    public List<CampaignMarketingCost> findAllMarketingCosts() {
        return marketingRepo.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Page<CampaignMarketingCost> findMarketingCostsPaged(Pageable pageable) {
        return marketingRepo.findAllWithDetailsPaged(pageable);
    }

    @Transactional(readOnly = true)
    public CampaignMarketingCost findMarketingCostById(Long id) {
        return marketingRepo.findByIdWithDetails(id)
            .orElseThrow(() -> new NoSuchElementException("Marketing cost not found: " + id));
    }

    @Transactional
    public CampaignMarketingCost createMarketingCost(CampaignMarketingCostForm form) {
        CampaignMarketingCost m = new CampaignMarketingCost();
        applyMarketingForm(m, form);
        m.setCreatedBy(currentUser());
        return marketingRepo.save(m);
    }

    @Transactional
    public CampaignMarketingCost updateMarketingCost(Long id, CampaignMarketingCostForm form) {
        CampaignMarketingCost m = marketingRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Marketing cost not found: " + id));
        applyMarketingForm(m, form);
        m.setUpdatedBy(currentUser());
        m.setUpdatedAt(LocalDateTime.now());
        return marketingRepo.save(m);
    }

    @Transactional
    public void deleteMarketingCost(Long id) {
        marketingRepo.deleteById(id);
    }

    public CampaignMarketingCostForm toMarketingForm(CampaignMarketingCost m) {
        CampaignMarketingCostForm form = new CampaignMarketingCostForm();
        form.setId(m.getId());
        form.setCampaignId(m.getCampaign().getId());
        form.setCostCenterId(m.getCostCenter().getId());
        form.setFiscalPeriodId(m.getFiscalPeriod().getId());
        form.setCostType(m.getCostType());
        form.setAmountUsd(m.getAmountUsd());
        form.setVendorName(m.getVendorName());
        form.setInvoiceRef(m.getInvoiceRef());
        form.setDescription(m.getDescription());
        form.setEntryDate(m.getEntryDate());
        return form;
    }

    private void applyMarketingForm(CampaignMarketingCost m, CampaignMarketingCostForm form) {
        m.setCampaign(campaignRepo.findById(form.getCampaignId())
            .orElseThrow(() -> new NoSuchElementException("Campaign not found")));
        m.setCostCenter(costCenterRepo.findById(form.getCostCenterId())
            .orElseThrow(() -> new NoSuchElementException("Cost center not found")));
        m.setFiscalPeriod(periodRepo.findById(form.getFiscalPeriodId())
            .orElseThrow(() -> new NoSuchElementException("Fiscal period not found")));
        m.setCostType(form.getCostType());
        m.setAmountUsd(form.getAmountUsd());
        m.setVendorName(form.getVendorName());
        m.setInvoiceRef(form.getInvoiceRef());
        m.setDescription(form.getDescription());
        m.setEntryDate(form.getEntryDate());
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
