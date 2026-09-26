package com.example.BankManagement.Controller;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Let Spring Security's own filter-level handling (SecurityConfig's
	 * authenticationEntryPoint/accessDeniedHandler) produce the 401/403 response.
	 * Without this, the catch-all Exception handler below would swallow
	 * @PreAuthorize denials and return a 200 HTML error page instead.
	 */
	@ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
	public void rethrowSecurityExceptions(Exception e) throws Exception {
		throw e;
	}

	/**
	 * Let Spring's own static-resource handling produce a real 404 for a missing
	 * CSS/JS/image path. Without this, the catch-all Exception handler below caught
	 * it too and returned a 200 HTML error page whose Content-Type still matched the
	 * originally-requested extension (e.g. text/css) - a broken stylesheet request
	 * that looks like a successful 200 load until you actually read the response
	 * body, exactly what made the /main/create styling bug so confusing.
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	public void rethrowMissingStaticResource(NoResourceFoundException e) throws Exception {
		throw e;
	}

	@ExceptionHandler(Exception.class)
	public String handleError(Exception e , Model model)
	{
		model.addAttribute("errorMessage", e.getMessage());
		return "error-page";
	}
}