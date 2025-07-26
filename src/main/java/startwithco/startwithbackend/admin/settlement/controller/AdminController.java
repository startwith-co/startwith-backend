package startwithco.startwithbackend.admin.settlement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import startwithco.startwithbackend.admin.settlement.dto.SettlementDto;
import startwithco.startwithbackend.admin.settlement.dto.VendorDto;
import startwithco.startwithbackend.admin.settlement.service.SettlementService;
import startwithco.startwithbackend.b2b.vendor.service.VendorService;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final SettlementService settlementService;
    private final VendorService vendorService;

    @GetMapping("/settlement")
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

    @PostMapping("/settlement/approve")
    public String approveSettlement(@RequestParam int page, @RequestParam String orderId, RedirectAttributes redirectAttributes) {
        try {
            settlementService.approveSettlement(orderId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/settlement?page=" + page;
        }

        return "redirect:/admin/settlement?page=" + page;
    }

    @PostMapping("/settlement/cancel")
    public String cancelSettlement(@RequestParam int page, @RequestParam String orderId, RedirectAttributes redirectAttributes) {
        try {
            settlementService.cancelSettlement(orderId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/settlement?page=" + page;
        }

        return "redirect:/admin/settlement?page=" + page;
    }

    @GetMapping("/vendor")
    public String getAllVendors(@RequestParam(defaultValue = "0") int page, @ModelAttribute String errorMessage, Model model) {
        int pageSize = 20;
        int start = page * pageSize;
        int end = start + pageSize;

        try {
            List<VendorDto> vendors = vendorService.getAllVendorEntity(start, end);
            model.addAttribute("vendors", vendors);
            model.addAttribute("hasNext", vendors.size() == pageSize);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("vendors", List.of());
            model.addAttribute("hasNext", false);
        }

        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);

        return "vendor";
    }

    @PostMapping("/vendor/approve")
    public String approveVendorAudit(@RequestParam int page, @RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            vendorService.approveVendorEntity(email);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/vendor?page=" + page;
        }

        return "redirect:/admin/vendor?page=" + page;
    }

    @PostMapping("/vendor/cancel")
    public String cancelVendorAudit(@RequestParam int page, @RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            vendorService.cancelVendorEntity(email);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/vendor?page=" + page;
        }

        return "redirect:/admin/vendor?page=" + page;
    }
}
