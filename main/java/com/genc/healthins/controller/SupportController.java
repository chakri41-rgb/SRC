package com.genc.healthins.controller;

import com.genc.healthins.model.SupportResponse;
import com.genc.healthins.model.SupportTicket;
import com.genc.healthins.model.User;
import com.genc.healthins.service.SupportResponseService;
import com.genc.healthins.service.SupportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class SupportController {

    private final SupportService ticketService;
    private final SupportResponseService responseService;

    public SupportController(SupportService ticketService, SupportResponseService responseService) {
        this.ticketService = ticketService;
        this.responseService = responseService;
    }

    @GetMapping({"/user/support","/user/support.html"})
    public String userSupport(Model model, HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("loggedInUser") == null) {
        return "redirect:/login"; // Force login if session is missing
    }
    
    User user = (User) session.getAttribute("loggedInUser");
    model.addAttribute("tickets", ticketService.findByUser(user));
    return "user/support";
}

    // @PostMapping("/user/support/create")
    // public String createTicket(@RequestParam String issue_description, HttpServletRequest request, RedirectAttributes ra) {
    //     HttpSession session = request.getSession(false);
    //     User user = session != null ? (User) session.getAttribute("loggedInUser") : null;
    //     if (user == null) {
    //         ra.addFlashAttribute("error", "Please login to create a ticket");
    //         return "redirect:/login";
    //     }
    //     SupportTicket ticket = new SupportTicket();
    //     ticket.setIssueDescription(issue_description);
    //     ticket.setCreatedDate(LocalDateTime.now());
    //     ticket.setTicketStatus("OPEN");
    //     ticket.setUser(user);
    //     ticketService.save(ticket);
    //     ra.addFlashAttribute("success", "Ticket created successfully");
    //     return "redirect:/user/support";
    // }
//     @PostMapping("/user/support/create")
//     public String createTicket(@RequestParam String issue_description, HttpServletRequest request, RedirectAttributes ra) {
//     HttpSession session = request.getSession(false);
    
//     // DEBUG: Check if session exists
//     if (session == null) {
//         System.out.println("DEBUG: Session is null");
//         return "redirect:/login";
//     }

//     User user = (User) session.getAttribute("loggedInUser");
    
//     // DEBUG: Check if user is in session
//     if (user == null) {
//         System.out.println("DEBUG: LoggedInUser not found in session");
//         ra.addFlashAttribute("error", "Please login to create a ticket");
//         return "redirect:/login";
//     }

//     try {
//         SupportTicket ticket = new SupportTicket();
//         ticket.setIssueDescription(issue_description);
//         ticket.setCreatedDate(LocalDateTime.now());
//         ticket.setTicketStatus("OPEN");
//         ticket.setUser(user); // Links ticket to user_id
        
//         ticketService.save(ticket);
//         System.out.println("DEBUG: Ticket saved successfully for user: " + user.getUsername());
        
//         ra.addFlashAttribute("success", "Ticket created successfully");
//     } catch (Exception e) {
//         System.err.println("DEBUG: Error saving ticket: " + e.getMessage());
//         ra.addFlashAttribute("error", "Database error occurred.");
//     }

//     return "redirect:/user/support";
// }

@PostMapping("/user/support/create")
public String createTicket(
    @RequestParam String subject,           // Added to match Photo 4
    @RequestParam String category,
    @RequestParam String priority,
    @RequestParam String issue_description,
    HttpServletRequest request, 
    RedirectAttributes ra) {
    
    HttpSession session = request.getSession(false);
    User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

    if (user == null) return "redirect:/login";

    try {
        SupportTicket ticket = new SupportTicket();
        ticket.setSubject(subject);         // Save Subject
        ticket.setCategory(category); 
        ticket.setPriority(priority); 
        ticket.setIssueDescription(issue_description);
        ticket.setCreatedDate(LocalDateTime.now());
        ticket.setTicketStatus("OPEN");
        ticket.setUser(user);
        
        ticketService.save(ticket);
        ra.addFlashAttribute("success", "Ticket created successfully!");
    } catch (Exception e) {
        ra.addFlashAttribute("error", "Database error: " + e.getMessage());
    }

    return "redirect:/user/support";
}
    @GetMapping({"/admin/support","/admin/support.html"})
    public String adminSupport(Model model, HttpServletRequest request) {
    // 1. Fetch all tickets for the admin list
    model.addAttribute("tickets", ticketService.findAll());
    
    // 2. Add KPI stats (Missing in your current code)
    model.addAttribute("openCount", ticketService.countByStatus("OPEN"));
    model.addAttribute("highPriorityCount", ticketService.countByPriority("High"));
    model.addAttribute("resolvedCount", ticketService.countByStatus("RESOLVED"));
    
    return "admin/support";
}

    // @GetMapping({"/support/view/{id}", "/support/view/{id}.html"})
    // public String viewTicket(@PathVariable Long id, Model model) {
    //     var opt = ticketService.findById(id);
    //     if (opt.isEmpty()) return "not-found";
    //     SupportTicket ticket = opt.get();
    //     model.addAttribute("ticket", ticket);
    //     model.addAttribute("responses", responseService.findByTicket(ticket));
    //     return "support/view"; // templates/support/view.html
    // }

@GetMapping("/support/view/{id}")
public String viewTicket(@PathVariable Long id, Model model) {
    var opt = ticketService.findById(id);
    if (opt.isEmpty()) return "not-found";
    
    SupportTicket ticket = opt.get();
    model.addAttribute("ticket", ticket);
    model.addAttribute("responses", responseService.findByTicket(ticket));
    
    // Points to src/main/resources/templates/user/view.html
    return "user/view"; 
}
//     @PostMapping("/support/{id}/resolve")
//     public String resolveTicket(@PathVariable Long id, HttpServletRequest request, RedirectAttributes ra) {
//     var opt = ticketService.findById(id);
//     if (opt.isEmpty()) {
//         ra.addFlashAttribute("error", "Ticket not found");
//         return "redirect:/admin/support";
//     }

//     SupportTicket ticket = opt.get();
//     ticket.setTicketStatus("RESOLVED");
//     ticket.setResolvedDate(LocalDateTime.now());
//     ticketService.save(ticket);

//     ra.addFlashAttribute("success", "Ticket marked as resolved");
    
//     // Redirect logic: if admin/agent, stay in admin view; if user, go to support list
//     HttpSession session = request.getSession(false);
//     User loggedInUser = (User) session.getAttribute("loggedInUser");
    
//     if (loggedInUser != null && ("ADMIN".equals(loggedInUser.getRole()) || "AGENT".equals(loggedInUser.getRole()))) {
//         return "redirect:/admin/support";
//     }
//     return "redirect:/user/support";
// }
// Add these methods to your existing SupportController.java

// 1. Display the Agent Support Page
@GetMapping("/agent/support")
public String agentSupport(Model model, HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

    if (user == null || (!"AGENT".equals(user.getRole()) && !"ADMIN".equals(user.getRole()))) {
        return "redirect:/login";
    }

    model.addAttribute("tickets", ticketService.findAll());
    
    // These match the th:text variables we will add to the HTML
    model.addAttribute("openCount", ticketService.countByStatus("OPEN"));
    model.addAttribute("highPriorityCount", ticketService.countByPriority("High"));
    model.addAttribute("resolvedCount", ticketService.countByStatus("RESOLVED"));

    return "agent/support";
}

// 2. Handle Agent Response (from Modal)
@PostMapping("/agent/support/response")
public String agentResponse(@RequestParam Long ticketId, 
                            @RequestParam String message, 
                            HttpServletRequest request, 
                            RedirectAttributes ra) {
    
    HttpSession session = request.getSession(false);
    User agent = (User) session.getAttribute("loggedInUser");

    try {
        SupportTicket ticket = ticketService.findById(ticketId).orElseThrow();
        
        SupportResponse response = new SupportResponse();
        response.setSupportTicket(ticket);
        response.setResponder(agent); // The Agent is the responder
        response.setMessage(message);
        response.setCreatedAt(LocalDateTime.now());
        
        responseService.save(response);
        ra.addFlashAttribute("success", "Response sent to customer!");
    } catch (Exception e) {
        ra.addFlashAttribute("error", "Error sending response.");
    }

    return "redirect:/agent/support";
}
    @PostMapping("/support/{id}/resolve")
    public String resolveTicket(@PathVariable Long id, HttpServletRequest request, RedirectAttributes ra) {
    var opt = ticketService.findById(id);
    if (opt.isEmpty()) {
        ra.addFlashAttribute("error", "Ticket not found.");
        return "redirect:/user/support";
    }

    SupportTicket ticket = opt.get();
    ticket.setTicketStatus("RESOLVED");
    ticket.setResolvedDate(LocalDateTime.now());
    ticketService.save(ticket);

    ra.addFlashAttribute("success", "Ticket resolved successfully.");
    
    // Logic: If Admin resolves, go to admin list. If User resolves, go to user list.
    HttpSession session = request.getSession(false);
    User user = (User) session.getAttribute("loggedInUser");
    if (user != null && "ADMIN".equals(user.getRole())) {
        return "redirect:/admin/support";
    }
    return "redirect:/user/support";
}
@PostMapping("/support/{id}/response")
public String addResponse(@PathVariable Long id, @RequestParam("message") String message, HttpServletRequest request, RedirectAttributes ra) {
    // Log to console so you can see if the button click reached Java
    System.out.println("DEBUG: Send Reply clicked for Ticket: " + id);

    var opt = ticketService.findById(id);
    if (opt.isEmpty()) return "redirect:/user/support";

    HttpSession session = request.getSession(false);
    User responder = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

    // If this prints in your console, your session has expired
    if (responder == null) {
        System.out.println("DEBUG: Save failed because NO USER found in session");
        return "redirect:/login"; 
    }

    try {
        SupportResponse response = new SupportResponse();
        response.setSupportTicket(opt.get());
        response.setResponder(responder);
        response.setMessage(message);
        response.setCreatedAt(LocalDateTime.now());
        
        responseService.save(response);
        System.out.println("DEBUG: Successfully saved to database!"); 
    } catch (Exception e) {
        System.out.println("DEBUG: Error saving: " + e.getMessage());
    }

    return "redirect:/support/view/" + id;
}
}
