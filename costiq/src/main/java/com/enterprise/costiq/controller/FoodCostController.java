package com.enterprise.costiq.controller;

import com.enterprise.costiq.dto.FoodCostEntryForm;
import com.enterprise.costiq.entity.FoodCostEntry;
import com.enterprise.costiq.repository.*;
import com.enterprise.costiq.service.FoodCostService;
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
@RequestMapping("/dashboard/food-costs")
@RequiredArgsConstructor
public class FoodCostController {

    private final FoodCostService foodCostService;
    private final FoodItemRepository foodItemRepo;
    private final SupplierRepository supplierRepo;
    private final CostCenterRepository costCenterRepo;
    private final FiscalPeriodRepository periodRepo;
    private final CountryRepository countryRepo;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size) {
        Page<FoodCostEntry> entries = foodCostService.findPaged(
            PageRequest.of(page, size, Sort.by("entryDate").descending()));
        model.addAttribute("entries", entries);
        model.addAttribute("pageTitle", "Food Cost Entries");
        model.addAttribute("currentPage", page);
        return "dashboard/food-costs/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new FoodCostEntryForm());
        model.addAttribute("pageTitle", "New Food Cost Entry");
        populateFormModel(model);
        return "dashboard/food-costs/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") FoodCostEntryForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            populateFormModel(model);
            model.addAttribute("pageTitle", "New Food Cost Entry");
            return "dashboard/food-costs/form";
        }
        FoodCostEntry saved = foodCostService.create(form);
        ra.addFlashAttribute("successMsg", "Food cost entry #" + saved.getId() + " created successfully.");
        return "redirect:/dashboard/food-costs";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        FoodCostEntry entry = foodCostService.findById(id);
        model.addAttribute("form", foodCostService.toForm(entry));
        model.addAttribute("entry", entry);
        model.addAttribute("pageTitle", "Edit Food Cost Entry #" + id);
        populateFormModel(model);
        return "dashboard/food-costs/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") FoodCostEntryForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            populateFormModel(model);
            model.addAttribute("pageTitle", "Edit Food Cost Entry #" + id);
            return "dashboard/food-costs/form";
        }
        foodCostService.update(id, form);
        ra.addFlashAttribute("successMsg", "Food cost entry #" + id + " updated successfully.");
        return "redirect:/dashboard/food-costs";
    }

    @GetMapping("/{id}/view")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("entry", foodCostService.findById(id));
        model.addAttribute("pageTitle", "Food Cost Entry #" + id);
        return "dashboard/food-costs/view";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        foodCostService.delete(id);
        ra.addFlashAttribute("successMsg", "Food cost entry #" + id + " deleted.");
        return "redirect:/dashboard/food-costs";
    }

    private void populateFormModel(Model model) {
        model.addAttribute("foodItems",   foodItemRepo.findAllActiveWithCategory());
        model.addAttribute("suppliers",   supplierRepo.findAllActiveWithCountry());
        model.addAttribute("costCenters", costCenterRepo.findAllByOrderByDepartmentAscNameAsc());
        model.addAttribute("periods",     periodRepo.findAllByOrderByFiscalYearDescQuarterDesc());
        model.addAttribute("countries",   countryRepo.findAllWithRegion());
    }
}
