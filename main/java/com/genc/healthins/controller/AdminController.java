package com.genc.healthins.controller;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.User;
import com.genc.healthins.service.PolicyService;
import com.genc.healthins.service.UserService;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// import java.util.List;

@Controller
public class AdminController {

    private final UserService userService;
    private final PolicyService policyService;
    private final com.genc.healthins.service.ClaimService claimService;
    private final com.genc.healthins.service.SupportService supportService;

    public AdminController(UserService userService, PolicyService policyService, com.genc.healthins.service.ClaimService claimService, com.genc.healthins.service.SupportService supportService) {
        this.userService = userService;
        this.policyService = policyService;
        this.claimService = claimService;
        this.supportService = supportService;
    }

    @GetMapping({"/admin/dashboard", "/admin/dashboard.html"})
    public String dashboard(Model model) {
        var users = userService.findAll();
        var policies = policyService.findAll();

        // Aggregate claims across policies
        java.util.List<com.genc.healthins.model.Claim> allClaims = new java.util.ArrayList<>();
        for (var p : policies) {
            allClaims.addAll(claimService.findByPolicy(p));
        }

        // compute KPI counts
        long activePolicies = policies.stream().filter(p -> "Active".equalsIgnoreCase(p.getPolicyStatus())).count();
        long openClaims = allClaims.stream().filter(c -> "PENDING".equalsIgnoreCase(c.getClaimStatus())).count();
        long tickets = supportService.findAll().size();

        // recent claims (sorted by date desc, take 3)
        java.util.List<com.genc.healthins.model.Claim> recentClaims = allClaims.stream()
                .sorted(java.util.Comparator.comparing((com.genc.healthins.model.Claim c) -> c.getClaimDate(),
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())).reversed())
                .limit(3)
                .toList();

        model.addAttribute("policies", policies);
        model.addAttribute("usersCount", users.size());
        model.addAttribute("users", users);
        model.addAttribute("activePolicies", activePolicies);
        model.addAttribute("openClaims", openClaims);
        model.addAttribute("ticketsCount", tickets);
        model.addAttribute("recentClaims", recentClaims);

        // build a small list of recent users with policy counts
        var recentUsers = users.stream().limit(5).map(u -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("username", u.getUsername());
            m.put("email", u.getEmail());
            m.put("policyCount", policyService.findByUser(u).size());
            return m;
        }).toList();
        model.addAttribute("recentUsers", recentUsers);

        return "admin/dashboard"; // templates/admin/dashboard.html
    }
    
    @GetMapping({"/admin/users", "/admin/users.html"})
    public String users(Model model) {
        var users = userService.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }
    // ADD THESE TO AdminController.java

// 1. Handle "Add New Account" Form Submission
    @PostMapping("/admin/users/add")
    public String addUser(@ModelAttribute User user, RedirectAttributes ra) {
        try {
            // Set the dynamic join date to right now
            user.setJoinDate(java.time.LocalDateTime.now());
            
            // Password is automatically bound from the form field name="password"
            userService.save(user); 
            ra.addFlashAttribute("success", "New account created successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to create account: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // 2. Handle "Edit User" Form Submission
@PostMapping("/admin/users/edit")
public String editUser(@RequestParam Long id, 
                       @RequestParam String username, 
                       @RequestParam String phone, 
                       @RequestParam String role, 
                       RedirectAttributes ra) {
    var opt = userService.findById(id);
    if (opt.isPresent()) {
        User user = opt.get();
        user.setUsername(username);
        user.setPhone(phone);
        user.setRole(role);
        userService.save(user); // Persists changes to the database
        ra.addFlashAttribute("success", "User updated!");
    }
    return "redirect:/admin/users";
}
    @GetMapping({"/admin/policies", "/admin/policies.html"})
    public String policies(Model model) {
        model.addAttribute("policies", policyService.findAll());
        model.addAttribute("users", userService.findAll());
        return "admin/policies";
    }

// 1. Handle creating a new Policy Plan for the Marketplace
@PostMapping("/admin/policies")
public String createPolicy(@RequestParam("policyName") String policyName, // Matches HTML name="policyName"
                           @RequestParam("premium") java.math.BigDecimal premium,
                           @RequestParam("coverage") java.math.BigDecimal coverage,
                           @RequestParam("startDate") String startDate,
                           @RequestParam("endDate") String endDate,
                           RedirectAttributes ra) {
    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminController.class);
    try {
        log.debug("Received createPolicy request: name={}, premium={}, coverage={}, start={}, end={}", policyName, premium, coverage, startDate, endDate);
        com.genc.healthins.model.Policy p = new com.genc.healthins.model.Policy();
        // Use a temporary, unique token for policyNumber to satisfy NOT NULL/UNIQUE constraint
        p.setPolicyNumber("TMP-" + java.util.UUID.randomUUID().toString());
        p.setCoverageType(policyName);
        p.setCoverageAmount(coverage);
        p.setPremiumAmount(premium);
        p.setPolicyStatus("ACTIVE");
        p.setStartDate(java.time.LocalDate.parse(startDate).atStartOfDay());
        p.setEndDate(java.time.LocalDate.parse(endDate).atStartOfDay());
        p.setCreatedAt(java.time.LocalDateTime.now());
        p.setUpdatedAt(java.time.LocalDateTime.now());

        // Save to obtain the generated ID, then set a human-friendly sequential policy number
        com.genc.healthins.model.Policy saved = policyService.save(p); // Persist to DB
        if (saved != null && saved.getId() != null) {
            String formatted = String.format("POL-%d-%03d", java.time.Year.now().getValue(), saved.getId());
            saved.setPolicyNumber(formatted);
            saved.setUpdatedAt(java.time.LocalDateTime.now());
            // Save again to persist policyNumber change
            saved = policyService.save(saved);
            log.info("Policy saved with id={} and policyNumber={}", saved.getId(), saved.getPolicyNumber());
            ra.addFlashAttribute("success", "New Plan published successfully!");
        } else {
            log.warn("Policy.save returned null id or saved is null");
            ra.addFlashAttribute("error", "Plan was not saved properly. Check logs.");
        }
    } catch (org.springframework.dao.DataIntegrityViolationException dive) {
        log.error("Data integrity violation while creating policy", dive);
        ra.addFlashAttribute("error", "Database constraint error: " + dive.getRootCause());
    } catch (Exception ex) {
        log.error("Error creating policy", ex);
        ra.addFlashAttribute("error", "Failed to publish plan: " + ex.toString());
    }
    return "redirect:/admin/policies";
}
// Updated Edit Policy logic to handle Optional
@PostMapping("/admin/policies/edit")
public String editPolicy(@RequestParam("id") Long id, 
                         @RequestParam("policyName") String policyName,
                         @RequestParam("premium") java.math.BigDecimal premium, 
                         @RequestParam("coverage") java.math.BigDecimal coverage,
                         @RequestParam("startDate") String startDate,
                         @RequestParam("endDate") String endDate,
                         RedirectAttributes ra) {
    java.util.Optional<com.genc.healthins.model.Policy> opt = policyService.findById(id);
    if (opt.isPresent()) {
        com.genc.healthins.model.Policy p = opt.get();
        p.setCoverageType(policyName);
        p.setPremiumAmount(premium);
        p.setCoverageAmount(coverage);
        p.setStartDate(java.time.LocalDate.parse(startDate).atStartOfDay());
        p.setEndDate(java.time.LocalDate.parse(endDate).atStartOfDay());
        policyService.save(p);
        ra.addFlashAttribute("success", "Plan updated successfully!");
    }
    return "redirect:/admin/policies";
}
// 2. Handle Deleting a Policy
@PostMapping("/admin/policies/delete/{id}")
public String deletePolicy(@PathVariable Long id, RedirectAttributes ra) {
    try {
        policyService.deleteById(id); // Physically removes from MySQL
        ra.addFlashAttribute("success", "Policy Plan removed from Marketplace.");
    } catch (Exception ex) {
        ra.addFlashAttribute("error", "Could not delete policy.");
    }
    return "redirect:/admin/policies";
}

@GetMapping({"/admin/policies/{id}", "/admin/policies/{id}.html"})
public String policyDetails(@PathVariable Long id, Model model, RedirectAttributes ra) {
    java.util.Optional<com.genc.healthins.model.Policy> opt = policyService.findById(id);
    if (opt.isEmpty()) {
        ra.addFlashAttribute("error", "Policy not found");
        return "redirect:/admin/policies";
    }
    model.addAttribute("policy", opt.get());
    return "admin/policy-details";
}

    @PostMapping({"/admin/users/delete/{id}", "/admin/users/delete/{id}.html"})
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.deleteById(id);
            ra.addFlashAttribute("success", "User deleted");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Could not delete user: " + ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping({"/admin/users/{id}", "/admin/users/{id}.html"})
    public String userDetails(@PathVariable Long id, Model model, RedirectAttributes ra) {
        var opt = userService.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "User not found");
            return "redirect:/admin/users";
        }
        var user = opt.get();
        model.addAttribute("user", user);
        model.addAttribute("policies", policyService.findByUser(user));
        return "admin/user-details";
    }
    @GetMapping({"/admin/claims", "/admin/claims.html"})
public String claims(Model model) {
    List<Claim> allClaims = claimService.findAll();
    model.addAttribute("claims", allClaims);
    
    // Dynamic KPI Stats
    model.addAttribute("pendingCount", allClaims.stream().filter(c -> "PENDING".equalsIgnoreCase(c.getClaimStatus())).count());
    model.addAttribute("approvedCount", allClaims.stream().filter(c -> "APPROVED".equalsIgnoreCase(c.getClaimStatus())).count());
    model.addAttribute("rejectedCount", allClaims.stream().filter(c -> "REJECTED".equalsIgnoreCase(c.getClaimStatus())).count());
    
    return "admin/claims";
}

@PostMapping("/admin/claims/process")
public String processClaim(@RequestParam Long claimId, 
                           @RequestParam String status, 
                           @RequestParam(required = false) java.math.BigDecimal approvedAmount, 
                           RedirectAttributes ra) {
    var opt = claimService.findById(claimId);
    if (opt.isPresent()) {
        Claim c = opt.get();
        c.setClaimStatus(status);
        if (approvedAmount != null) {
            c.setClaimAmount(approvedAmount); // Admin adjustment logic
        }
        claimService.save(c);
        ra.addFlashAttribute("success", "Claim " + status + " successfully!");
    }
    return "redirect:/admin/claims";
}
}
