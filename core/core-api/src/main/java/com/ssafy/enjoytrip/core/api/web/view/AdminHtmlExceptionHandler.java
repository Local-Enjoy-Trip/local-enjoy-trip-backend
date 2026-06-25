package com.ssafy.enjoytrip.core.api.web.view;

import com.ssafy.enjoytrip.core.api.web.view.controller.AdminPlaceController;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(assignableTypes = {
        AdminPlaceController.class
})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminHtmlExceptionHandler {
    @ExceptionHandler(CoreException.class)
    public String handleCoreException(
            CoreException exception,
            HttpServletResponse response,
            Model model
    ) {
        response.setStatus(exception.errorType().status().value());
        model.addAttribute("message", exception.errorType().message());
        model.addAttribute("code", exception.errorType().code());
        return "admin/error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDeniedException(AccessDeniedException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        model.addAttribute("code", "S403");
        return "admin/error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        model.addAttribute("code", "C400");
        return "admin/error";
    }
}
