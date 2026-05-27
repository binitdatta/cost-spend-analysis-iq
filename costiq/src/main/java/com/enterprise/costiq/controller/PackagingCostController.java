package com.enterprise.costiq.controller;

import com.enterprise.costiq.dto.PackagingCostEntryForm;
import com.enterprise.costiq.entity.PackagingCostEntry;
import com.enterprise.costiq.repository.*;
import com.enterprise.costiq.service.PackagingCostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dashboard/packaging-costs")
@RequiredArgsConstructor
public class PackagingCostController {

    private final PackagingCostService packagingCostService;
    private final PackagingItemRepository packagingItemRepo;
    private final SupplierRepository supplierRepo;
    private final CostCenterRepository costCenterRepo;
    private final FiscalPeriodRepository periodRepo;
    private final CountryRepository countryRepo;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size) {
        Page<PackagingCostEntry> entries = packagingCostService.findPaged(
            PageRequest.of(page, size, Sort.by("entryDate").descending()));
        model.addAttribute("entries", entries);
        model.addAttribute("pageTitle", "Packaging Cost Entries");
        model.addAttribute("currentPage", page);
        return "dashboard/packaging-costs/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new PackagingCostEntryForm());
        model.addAttribute("pageTitle", "New Packaging Cost Entry");
        populateFormModel(model);
        return "dashboard/packaging-costs/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") PackagingCostEntryForm form,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            populateFormModel(model);
            model.addAttribute("pageTitle", "New Packaging Cost Entry");
            return "dashboard/packaging-costs/form";
        }
        PackagingCostEntry saved = packagingCostService.create(form);
        ra.addFlashAttribute("successMsg", "Packaging cost entry #" + saved.getId() + " created.");
        return "redirect:/dashboard/packaging-costs";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PackagingCostEntry entry = packagingCostService.findById(id);
        model.addAttribute("form", packagingCostService.toForm(entry));
        model.addAttribute("entry", entry);
        model.addAttribute("pageTitle", "Edit Packaging Cost Entry #" + id);
        populateFormModel(model);
        return "dashboard/packaging-costs/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") PackagingCostEntryForm form,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            populateFormModel(model);
            model.addAttribute("pageTitle", "Edit Packaging Cost Entry #" + id);
            return "dashboard/packaging-costs/form";
        }
        packagingCostService.update(id, form);
        ra.addFlashAttribute("successMsg", "Packaging cost entry #" + id + " updated.");
        return "redirect:/dashboard/packaging-costs";
    }

    @GetMapping("/{id}/view")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("entry", packagingCostService.findById(id));
        model.addAttribute("pageTitle", "Packaging Cost Entry #" + id);
        return "dashboard/packaging-costs/view";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        packagingCostService.delete(id);
        ra.addFlashAttribute("successMsg", "Packaging cost entry #" + id + " deleted.");
        return "redirect:/dashboard/packaging-costs";
    }

    private void populateFormModel(Model model) {
        model.addAttribute("packagingItems", packagingItemRepo.findAllActiveWithType());
        model.addAttribute("suppliers",      supplierRepo.findAllActiveWithCountry());
        model.addAttribute("costCenters",    costCenterRepo.findAllByOrderByDepartmentAscNameAsc());
        model.addAttribute("periods",        periodRepo.findAllByOrderByFiscalYearDescQuarterDesc());
        model.addAttribute("countries",      countryRepo.findAllWithRegion());
    }
}
