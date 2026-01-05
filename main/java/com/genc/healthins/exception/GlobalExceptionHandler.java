package com.genc.healthins.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        logger.error("CRITICAL SYSTEM ERROR: ", ex);
        model.addAttribute("errorMessage", "An internal error occurred: " + ex.getMessage());
        return "error"; // Make sure you have an error.html in templates, or it will fallback to whitelabel
    }
}