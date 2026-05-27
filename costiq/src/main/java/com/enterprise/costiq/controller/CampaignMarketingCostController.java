package com.enterprise.costiq.controller;

import com.enterprise.costiq.dto.CampaignMarketingCostForm;
import com.enterprise.costiq.entity.CampaignMarketingCost;
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
@RequestMapping("/dashboard/marketing-costs")
@RequiredArgsConstructor
public class CampaignMarketingCostController {

    private final CampaignCostService service;
    private final CampaignRepository campaignRepo;
    private final CostCenterRepository costCenterRepo;
    private final FiscalPeriodRepository periodRepo;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size) {
        Page<CampaignMarketingCost> entries = service.findMarketingCostsPaged(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "entryDate")));
        model.addAttribute("entries", entries);
        model.addAttribute("pageTitle", "Campaign Marketing Costs");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", entries.getTotalPages());
        return "dashboard/marketing-costs/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new CampaignMarketingCostForm());
        model.addAttribute("pageTitle", "New Marketing Cost");
        model.addAttribute("isEdit", false);
        model.addAttribute("costTypes", CampaignMarketingCost.CostType.values());
        populateDropdowns(model);
        return "dashboard/marketing-costs/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") CampaignMarketingCostForm form,
                         BindingResult br, Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "New Marketing Cost");
            model.addAttribute("isEdit", false);
            model.addAttribute("costTypes", CampaignMarketingCost.CostType.values());
            populateDropdowns(model);
            return "dashboard/marketing-costs/form";
        }
        service.createMarketingCost(form);
        ra.addFlashAttribute("successMsg", "Marketing cost entry created.");
        return "redirect:/dashboard/marketing-costs";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        CampaignMarketingCost entry = service.findMarketingCostById(id);
        model.addAttribute("form", service.toMarketingForm(entry));
        model.addAttribute("entry", entry);
        model.addAttribute("pageTitle", "Edit Marketing Cost #" + id);
        model.addAttribute("isEdit", true);
        model.addAttribute("costTypes", CampaignMarketingCost.CostType.values());
        populateDropdowns(model);
        return "dashboard/marketing-costs/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") CampaignMarketingCostForm form,
                         BindingResult br, Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Marketing Cost #" + id);
            model.addAttribute("isEdit", true);
            model.addAttribute("costTypes", CampaignMarketingCost.CostType.values());
            populateDropdowns(model);
            return "dashboard/marketing-costs/form";
        }
        service.updateMarketingCost(id, form);
        ra.addFlashAttribute("successMsg", "Marketing cost #" + id + " updated.");
        return "redirect:/dashboard/marketing-costs";
    }

    @GetMapping("/{id}/view")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("entry", service.findMarketingCostById(id));
        model.addAttribute("pageTitle", "Marketing Cost #" + id);
        return "dashboard/marketing-costs/view";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.deleteMarketingCost(id);
        ra.addFlashAttribute("successMsg", "Marketing cost #" + id + " deleted.");
        return "redirect:/dashboard/marketing-costs";
    }

    private void populateDropdowns(Model model) {
        model.addAttribute("campaigns",   campaignRepo.findAllByOrderByStartDateDesc());
        model.addAttribute("costCenters", costCenterRepo.findAllByOrderByDepartmentAscNameAsc());
        model.addAttribute("periods",     periodRepo.findAllByOrderByFiscalYearDescQuarterDesc());
    }
}
