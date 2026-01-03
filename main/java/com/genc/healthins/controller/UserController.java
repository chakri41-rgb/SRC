package com.genc.healthins.controller;

import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;
import com.genc.healthins.service.PolicyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class UserController {

    private final PolicyService policyService;
    private final com.genc.healthins.service.ClaimService claimService;
    private final com.genc.healthins.service.PaymentService paymentService;
    private final com.genc.healthins.service.SupportService supportService;

    public UserController(PolicyService policyService,
                          com.genc.healthins.service.ClaimService claimService,
                          com.genc.healthins.service.PaymentService paymentService,
                          com.genc.healthins.service.SupportService supportService) {
        this.policyService = policyService;
        this.claimService = claimService;
        this.paymentService = paymentService;
        this.supportService = supportService;
    }

    @GetMapping({"/user/dashboard", "/user/dashboard.html"})
    public String dashboard(Model model, jakarta.servlet.http.HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        List<Policy> policies = user != null ? policyService.findByUser(user) : java.util.Collections.emptyList();

        // Aggregate claims and payments for the logged-in user
        int claimsCount = 0;
        int pendingClaimsCount = 0;
        int paymentsDue = 0;
        java.util.List<com.genc.healthins.model.Claim> allClaims = new java.util.ArrayList<>();

        for (Policy p : policies) {
            var claims = claimService.findByPolicy(p);
            allClaims.addAll(claims);
            claimsCount += claims.size();
            for (var c : claims) {
                if ("PENDING".equalsIgnoreCase(c.getClaimStatus())) pendingClaimsCount++;
            }

            var payments = paymentService.findByPolicy(p);
            for (var pay : payments) {
                if (pay.getPaymentStatus() == null || "PENDING".equalsIgnoreCase(pay.getPaymentStatus())) paymentsDue++;
            }
        }

        java.util.List<com.genc.healthins.model.SupportTicket> tickets = user != null ? supportService.findByUser(user) : java.util.Collections.<com.genc.healthins.model.SupportTicket>emptyList();
        int ticketsOpen = (int) tickets.stream().filter(t -> t.getTicketStatus() == null || !"CLOSED".equalsIgnoreCase(t.getTicketStatus())).count();

        model.addAttribute("policies", policies);
        model.addAttribute("claimsCount", claimsCount);
        model.addAttribute("pendingClaimsCount", pendingClaimsCount);
        model.addAttribute("paymentsDue", paymentsDue);
        model.addAttribute("ticketsOpen", ticketsOpen);
        model.addAttribute("userName", user != null ? user.getUsername() : "Guest");

        return "user/dashboard";
    }

    @GetMapping({"/user/policies", "/user/policies.html"})
    public String policies(Model model, jakarta.servlet.http.HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        java.util.List<Policy> policies = user != null ? policyService.findByUser(user) : java.util.Collections.emptyList();

        // compute last payment per policy
        java.util.Map<Long, com.genc.healthins.model.Payment> lastPayments = new java.util.HashMap<>();
        for (Policy p : policies) {
            var pays = paymentService.findByPolicy(p);
            if (!pays.isEmpty()) {
                var last = pays.stream().max(java.util.Comparator.comparing(com.genc.healthins.model.Payment::getPaymentDate)).get();
                lastPayments.put(p.getId(), last);
            }
        }

        model.addAttribute("policies", policies);
        model.addAttribute("lastPayments", lastPayments);
        return "user/policies";
    }

    @GetMapping({"/user/policy-details", "/user/policy-details.html"})
    public String policyDetails(@org.springframework.web.bind.annotation.RequestParam(required = false) Long id, Model model, jakarta.servlet.http.HttpServletRequest request, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        if (id == null) {
            ra.addFlashAttribute("error", "Policy not specified");
            return "redirect:/user/policies";
        }
        var opt = policyService.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Policy not found");
            return "redirect:/user/policies";
        }
        model.addAttribute("policy", opt.get());
        return "user/policy-details";
    }

    @GetMapping({"/user/claims", "/user/claims.html"})
    public String claims(Model model, jakarta.servlet.http.HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        java.util.List<com.genc.healthins.model.Claim> allClaims = new java.util.ArrayList<>();
        if (user != null) {
            for (Policy p : policyService.findByUser(user)) {
                allClaims.addAll(claimService.findByPolicy(p));
            }
        }
        model.addAttribute("claims", allClaims);
        model.addAttribute("statPending", (int) allClaims.stream().filter(c -> "PENDING".equalsIgnoreCase(c.getClaimStatus())).count());
        model.addAttribute("statApproved", (int) allClaims.stream().filter(c -> "APPROVED".equalsIgnoreCase(c.getClaimStatus())).count());
        model.addAttribute("statRejected", (int) allClaims.stream().filter(c -> "REJECTED".equalsIgnoreCase(c.getClaimStatus())).count());
        return "user/claims";
    }

    @GetMapping({"/user/payments", "/user/payments.html"})
    public String payments(Model model, jakarta.servlet.http.HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        java.util.List<com.genc.healthins.model.Payment> payments = new java.util.ArrayList<>();
        java.util.List<Policy> policies = user != null ? policyService.findByUser(user) : java.util.Collections.emptyList();
        if (user != null) {
            for (Policy p : policies) {
                payments.addAll(paymentService.findByPolicy(p));
            }
        }
        model.addAttribute("payments", payments);
        // provide user's policies to populate the "Select Policy" dropdown
        model.addAttribute("policies", policies);
        return "user/payments";
    }

    @PostMapping("/user/payments/pay")
    public String makePayment(@org.springframework.web.bind.annotation.RequestParam Long policyId,
                              @org.springframework.web.bind.annotation.RequestParam java.math.BigDecimal amount,
                              jakarta.servlet.http.HttpServletRequest request,
                              org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        var opt = policyService.findById(policyId);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Policy not found");
            return "redirect:/user/payments";
        }
        Policy policy = opt.get();
        // ensure user owns this policy
        User user = (User) request.getSession().getAttribute("loggedInUser");
        if (user == null || policy.getUser() == null || !policy.getUser().getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "You are not authorized to pay for this policy");
            return "redirect:/user/payments";
        }

        try {
            com.genc.healthins.model.Payment p = new com.genc.healthins.model.Payment();
            p.setPaymentAmount(amount);
            p.setPaymentDate(java.time.LocalDateTime.now());
            p.setPaymentStatus("COMPLETED");
            p.setPolicy(policy);
            paymentService.save(p);
            ra.addFlashAttribute("success", "Payment recorded successfully");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Could not process payment: " + ex.getMessage());
        }
        return "redirect:/user/payment-history";
    }

    @GetMapping({"/user/payment-history", "/user/payment-history.html"})
    public String paymentHistory(Model model, jakarta.servlet.http.HttpServletRequest request) {
        return payments(model, request);
    }

    @GetMapping({"/user/profile", "/user/profile.html"})
    public String profile(Model model, jakarta.servlet.http.HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        model.addAttribute("user", user);
        return "user/profile";
    }

@GetMapping({"/user/submit-claim", "/user/submit-claim.html"})
public String submitClaimForm(Model model, jakarta.servlet.http.HttpServletRequest request) {
    User user = (User) request.getSession().getAttribute("loggedInUser");
    model.addAttribute("policies", user != null ? policyService.findByUser(user) : java.util.Collections.emptyList());
    return "user/submit-claim";
}

// Method 2: Process the data and SAVE it to DB
@PostMapping("/user/claims/submit")
public String submitClaim(@org.springframework.web.bind.annotation.RequestParam Long policyId,
                          @org.springframework.web.bind.annotation.RequestParam java.math.BigDecimal amount,
                          org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
    var opt = policyService.findById(policyId);
    if (opt.isPresent()) {
        com.genc.healthins.model.Claim c = new com.genc.healthins.model.Claim();
        c.setPolicy(opt.get());
        c.setClaimAmount(amount); // Setting the requested amount from the user
        c.setClaimDate(java.time.LocalDateTime.now());
        c.setClaimStatus("PENDING"); // New claims start as PENDING for Admin review
        
        claimService.save(c); // This line sends it to MySQL
        ra.addFlashAttribute("success", "Claim submitted for approval!");
    }
    return "redirect:/user/claims";
}
    @GetMapping({"/user/create-ticket", "/user/create-ticket.html"})
    public String createTicketForm(Model model, jakarta.servlet.http.HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("loggedInUser");
        model.addAttribute("user", user);
        return "user/create-ticket";
    }
@GetMapping({"/user/marketplace", "/user/marketplace.html"})
public String marketplace(Model model) {
    // Only show policies that haven't been bought yet (user_id is null)
    List<Policy> availablePlans = policyService.findAll().stream()
            .filter(p -> p.getUser() == null)
            .toList();
    model.addAttribute("plans", availablePlans);
    return "user/marketplace";
}
@PostMapping("/user/policies/enroll")
public String enrollInPolicy(@RequestParam("planId") Long planId, 
                             jakarta.servlet.http.HttpServletRequest request, 
                             RedirectAttributes ra) {
    User user = (User) request.getSession().getAttribute("loggedInUser");
    if (user == null) return "redirect:/login";

    var opt = policyService.findById(planId);
    if (opt.isPresent()) {
        com.genc.healthins.model.Policy plan = opt.get();
        
        // Associate the user with this policy
        plan.setUser(user); 
        plan.setPolicyStatus("Active");
        
        policyService.save(plan); // Persist change to MySQL
        ra.addFlashAttribute("success", "Successfully enrolled in " + plan.getCoverageType() + "!");
    }
    return "redirect:/user/policies";
}
@PostMapping("/user/policies/join")
public String joinPolicy(@RequestParam Long id, 
                         jakarta.servlet.http.HttpServletRequest request, 
                         RedirectAttributes ra) {
    User user = (User) request.getSession().getAttribute("loggedInUser");
    var opt = policyService.findById(id);
    
    if (opt.isPresent() && user != null) {
        Policy planTemplate = opt.get();
        
        // Logic: When a user joins, we assign their ID to the policy row
        planTemplate.setUser(user); 
        planTemplate.setPolicyStatus("Active");
        policyService.save(planTemplate); 
        
        ra.addFlashAttribute("success", "Successfully enrolled in " + planTemplate.getCoverageType());
    }
    return "redirect:/user/policies";
}
}
