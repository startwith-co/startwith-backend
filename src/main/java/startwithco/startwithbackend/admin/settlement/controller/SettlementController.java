package startwithco.startwithbackend.admin.settlement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import startwithco.startwithbackend.admin.settlement.dto.SettlementDto;
import startwithco.startwithbackend.admin.settlement.service.SettlementService;

import java.util.List;

@Controller
@RequestMapping("/admin/settlement")
@RequiredArgsConstructor
public class SettlementController {
    private final SettlementService settlementService;

    @GetMapping()
    public String settlement(@RequestParam(defaultValue = "0") int page, @ModelAttribute String errorMessage, Model model) {
        int pageSize = 20;
        int start = page * pageSize;
        int end = start + pageSize;

        try {
            List<SettlementDto> payments = settlementService.getAllSettlementPayments(start, end);
            model.addAttribute("payments", payments);
            model.addAttribute("hasNext", payments.size() == pageSize);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("payments", List.of());
            model.addAttribute("hasNext", false);
        }

        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);

        return "settlement";
    }

    @PostMapping("/approve")
    public String approveSettlement(@RequestParam int page, @RequestParam String orderId, RedirectAttributes redirectAttributes) {
        try {
            settlementService.approveSettlement(orderId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/settlement?page=" + page;
        }

        return "redirect:/admin/settlement?page=" + page;
    }

    @PostMapping("/cancel")
    public String cancelSettlement(@RequestParam int page, @RequestParam String orderId, RedirectAttributes redirectAttributes) {
        try {
            settlementService.cancelSettlement(orderId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/settlement?page=" + page;
        }

        return "redirect:/admin/settlement?page=" + page;
    }
}
