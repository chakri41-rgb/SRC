package com.genc.healthins.controller;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.Policy;
import com.genc.healthins.model.SupportTicket;
import com.genc.healthins.model.User;
import com.genc.healthins.service.ClaimService;
import com.genc.healthins.service.PolicyService;
import com.genc.healthins.service.SupportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AgentController {

    private final PolicyService policyService;
    private final SupportService supportService;
    private final ClaimService claimService;
    private final com.genc.healthins.service.UserService userService;

    public AgentController(PolicyService policyService, SupportService supportService, ClaimService claimService, com.genc.healthins.service.UserService userService) {
        this.policyService = policyService;
        this.supportService = supportService;
        this.claimService = claimService;
        this.userService = userService;
    }

    @GetMapping({"/agent/dashboard", "/agent/dashboard.html"})
    public String dashboard(Model model) {
        List<Policy> policies = policyService.findAll();
        List<SupportTicket> tickets = supportService.findAll();

        // Customers count (unique users)
        Set<User> customers = policies.stream()
                .map(Policy::getUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Claims counts and lists
        List<Claim> pendingClaims = new ArrayList<>();
        int approvedToday = 0;
        int rejectedToday = 0;
        LocalDate today = LocalDate.now();

        for (Policy p : policies) {
            var claims = claimService.findByPolicy(p);
            for (Claim c : claims) {
                String status = c.getClaimStatus();
                if ("PENDING".equalsIgnoreCase(status)) pendingClaims.add(c);
                if ("APPROVED".equalsIgnoreCase(status) && c.getClaimDate() != null && c.getClaimDate().toLocalDate().equals(today)) approvedToday++;
                if ("REJECTED".equalsIgnoreCase(status) && c.getClaimDate() != null && c.getClaimDate().toLocalDate().equals(today)) rejectedToday++;
            }
        }

        model.addAttribute("policies", policies);
        model.addAttribute("tickets", tickets);
        model.addAttribute("pendingClaims", pendingClaims);
        model.addAttribute("pendingClaimsCount", pendingClaims.size());
        model.addAttribute("approvedToday", approvedToday);
        model.addAttribute("rejectedToday", rejectedToday);
        model.addAttribute("customersCount", customers.size());

        return "agent/dashboard";
    }

    // API endpoint to process (approve/reject) a claim
    @org.springframework.web.bind.annotation.PostMapping("/agent/claims/{id}/action")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> processClaim(@org.springframework.web.bind.annotation.PathVariable Long id,
                                                                   @org.springframework.web.bind.annotation.RequestParam String action) {
        var opt = claimService.findById(id);
        if (opt.isEmpty()) return org.springframework.http.ResponseEntity.status(404).body(java.util.Map.of("error", "Claim not found"));
        var claim = opt.get();
        var now = java.time.LocalDateTime.now();
        if ("approve".equalsIgnoreCase(action)) {
            claim.setClaimStatus("APPROVED");
            claim.setClaimDate(now);
            claimService.save(claim);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("status","approved"));
        } else if ("reject".equalsIgnoreCase(action)) {
            claim.setClaimStatus("REJECTED");
            claim.setClaimDate(now);
            claimService.save(claim);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("status","rejected"));
        }
        return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error","Unknown action"));
    }

    // API endpoint to delete a claim
    @org.springframework.web.bind.annotation.DeleteMapping("/agent/claims/{id}")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> deleteClaim(@org.springframework.web.bind.annotation.PathVariable Long id) {
        var opt = claimService.findById(id);
        if (opt.isEmpty()) return org.springframework.http.ResponseEntity.status(404).body(java.util.Map.of("error","Claim not found"));
        claimService.deleteById(id);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("status","deleted"));
    }

    @GetMapping({"/agent/policies", "/agent/policies.html"})
    public String policies(Model model) {
        model.addAttribute("policies", policyService.findAll());
        return "agent/policies";
    }

    @GetMapping({"/agent/claims", "/agent/claims.html"})
    public String claims(Model model) {
        // collect all claims across policies for the agent view
        List<Policy> policies = policyService.findAll();
        List<Claim> allClaims = new ArrayList<>();
        for (Policy p : policies) {
            allClaims.addAll(claimService.findByPolicy(p));
        }
        model.addAttribute("claims", allClaims);
        model.addAttribute("statPending", (int) allClaims.stream().filter(c -> "PENDING".equalsIgnoreCase(c.getClaimStatus())).count());
        model.addAttribute("statApproved", (int) allClaims.stream().filter(c -> "APPROVED".equalsIgnoreCase(c.getClaimStatus())).count());
        model.addAttribute("statRejected", (int) allClaims.stream().filter(c -> "REJECTED".equalsIgnoreCase(c.getClaimStatus())).count());
        return "agent/claims";
    }

    @GetMapping({"/agent/profile", "/agent/profile.html"})
    public String profile(jakarta.servlet.http.HttpServletRequest request, Model model) {
        var session = request.getSession(false);
        var user = session != null ? (com.genc.healthins.model.User) session.getAttribute("loggedInUser") : null;
        model.addAttribute("agent", user);
        return "agent/profile";
    }

    @GetMapping({"/agent/customers", "/agent/customers.html"})
    public String customers(Model model) {
        java.util.List<com.genc.healthins.model.User> users = userService.findAll().stream()
                .filter(u -> "USER".equalsIgnoreCase(u.getRole()))
                .toList();

        long active = users.stream().filter(u -> !policyService.findByUser(u).isEmpty()).count();
        long pending = users.size() - active;
        long inactive = 0; // no explicit status field on User yet

        model.addAttribute("customers", users);
        model.addAttribute("statActive", active);
        model.addAttribute("statPending", pending);
        model.addAttribute("statInactive", inactive);
        model.addAttribute("customersCount", users.size());
        return "agent/customers";
    }

    @GetMapping({"/agent/customers/{id}", "/agent/customers/{id}.html"})
    public String customerDetails(@org.springframework.web.bind.annotation.PathVariable Long id, Model model, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        var opt = userService.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Customer not found");
            return "redirect:/agent/customers";
        }
        var user = opt.get();
        model.addAttribute("customer", user);
        model.addAttribute("policies", policyService.findByUser(user));
        return "agent/customer-details";
    }
}
