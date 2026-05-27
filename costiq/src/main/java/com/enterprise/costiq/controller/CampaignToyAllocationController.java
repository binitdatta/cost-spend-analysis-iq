package com.enterprise.costiq.controller;

import com.enterprise.costiq.dto.CampaignToyAllocationForm;
import com.enterprise.costiq.entity.CampaignToyAllocation;
import com.enterprise.costiq.repository.*;
import com.enterprise.costiq.service.CampaignCostService;
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
@RequestMapping("/dashboard/toy-costs")
@RequiredArgsConstructor
public class CampaignToyAllocationController {

    private final CampaignCostService service;
    private final CampaignRepository campaignRepo;
    private final ToyItemRepository toyItemRepo;
    private final CountryRepository countryRepo;
    private final SupplierRepository supplierRepo;
    private final FiscalPeriodRepository periodRepo;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size) {
        Page<CampaignToyAllocation> entries = service.findToyAllocationsPaged(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "entryDate")));
        model.addAttribute("entries", entries);
        model.addAttribute("pageTitle", "Campaign Toy Allocations");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", entries.getTotalPages());
        return "dashboard/toy-costs/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new CampaignToyAllocationForm());
        model.addAttribute("pageTitle", "New Toy Allocation");
        model.addAttribute("isEdit", false);
        model.addAttribute("channels", CampaignToyAllocation.DistributionChannel.values());
        populateDropdowns(model);
        return "dashboard/toy-costs/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") CampaignToyAllocationForm form,
                         BindingResult br, Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "New Toy Allocation");
            model.addAttribute("isEdit", false);
            model.addAttribute("channels", CampaignToyAllocation.DistributionChannel.values());
            populateDropdowns(model);
            return "dashboard/toy-costs/form";
        }
        service.createToyAllocation(form);
        ra.addFlashAttribute("successMsg", "Toy allocation created successfully.");
        return "redirect:/dashboard/toy-costs";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        CampaignToyAllocation entry = service.findToyAllocationById(id);
        model.addAttribute("form", service.toToyForm(entry));
        model.addAttribute("entry", entry);
        model.addAttribute("pageTitle", "Edit Toy Allocation #" + id);
        model.addAttribute("isEdit", true);
        model.addAttribute("channels", CampaignToyAllocation.DistributionChannel.values());
        populateDropdowns(model);
        return "dashboard/toy-costs/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") CampaignToyAllocationForm form,
                         BindingResult br, Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Toy Allocation #" + id);
            model.addAttribute("isEdit", true);
            model.addAttribute("channels", CampaignToyAllocation.DistributionChannel.values());
            populateDropdowns(model);
            return "dashboard/toy-costs/form";
        }
        service.updateToyAllocation(id, form);
        ra.addFlashAttribute("successMsg", "Toy allocation #" + id + " updated.");
        return "redirect:/dashboard/toy-costs";
    }

    @GetMapping("/{id}/view")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("entry", service.findToyAllocationById(id));
        model.addAttribute("pageTitle", "Toy Allocation #" + id);
        return "dashboard/toy-costs/view";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.deleteToyAllocation(id);
        ra.addFlashAttribute("successMsg", "Toy allocation #" + id + " deleted.");
        return "redirect:/dashboard/toy-costs";
    }

    private void populateDropdowns(Model model) {
        model.addAttribute("campaigns",  campaignRepo.findAllByOrderByStartDateDesc());
        model.addAttribute("toyItems",   toyItemRepo.findAllActiveWithCategory());
        model.addAttribute("countries",  countryRepo.findAllWithRegion());
        model.addAttribute("suppliers",  supplierRepo.findAllActiveWithCountry());
        model.addAttribute("periods",    periodRepo.findAllByOrderByFiscalYearDescQuarterDesc());
    }
}
