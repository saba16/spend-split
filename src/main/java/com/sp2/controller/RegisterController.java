package com.sp2.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import com.sp2.model.User;
import com.sp2.service.EmailService;
import com.sp2.service.UserService;

@Controller
public class RegisterController {
	
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	private UserService userService;
	private EmailService emailService;
	
	@Autowired
	public RegisterController(BCryptPasswordEncoder bCryptPasswordEncoder,
			UserService userService, EmailService emailService) {
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.userService = userService;
		this.emailService = emailService;
	}
	
	// Return registration form template
	@RequestMapping(value="/index", method = RequestMethod.GET)
	public ModelAndView login(ModelAndView modelAndView, User user){
		modelAndView.addObject("user", user);
		modelAndView.setViewName("index");
		return modelAndView;
	}
	
	@RequestMapping(value="/login", method = RequestMethod.POST)
	public ModelAndView loginUser(ModelAndView modelAndView,ModelMap model, @Valid User user,
			BindingResult bindingResult,@RequestParam Map<String, String> requestParams,
			 HttpServletRequest request){
		User userExists = userService.findByEmail(user.getEmail());
		if (userExists != null) {
			String pass=userExists.getPassword();
			String pass1=requestParams.get("password");
			if(bCryptPasswordEncoder.matches(pass1, pass))
					{
				modelAndView.addObject("confirmationMessage",
		                userExists.getFirstName());
				String name=userExists.getFirstName();
				
				 modelAndView.setViewName("/success");
				 modelAndView.addObject("username",name);
				 modelAndView.addObject("user", user.getFirstName());
					List<User> userExists1 = userService.splitlist();
					System.out.println(userExists1.size());
					modelAndView.addObject("user",userExists1);
				 System.out.println("vakue:"+user.getFirstName());
				 return modelAndView;
					}
			else
			{
				modelAndView.addObject("alreadyRegisteredMessage", "Oops! password Invalid");
				modelAndView.setViewName("index");
				return modelAndView;
			}
		}
		
			modelAndView.addObject("alreadyRegisteredMessage", "Oops! User was not Present.Please Register");
			modelAndView.setViewName("index");
			return modelAndView;
		
		
	
	}
	
	@RequestMapping(value="/success", method = RequestMethod.GET)
	public ModelAndView showSplitsPage(ModelAndView modelAndView, User user){
		modelAndView.addObject("user", user.getFirstName());
		
		List<User> userExists = userService.splitlist();
		System.out.println(userExists.size());
		modelAndView.addObject("user",userExists);
		modelAndView.setViewName("success");
		return modelAndView;
	}

	
	
	@RequestMapping(value="/register", method = RequestMethod.GET)
	public ModelAndView showRegistrationPage(ModelAndView modelAndView, User user){
		modelAndView.addObject("user", user);
		
		modelAndView.setViewName("register");
		return modelAndView;
	}
	
	// Process form input data
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ModelAndView processRegistrationForm(ModelAndView modelAndView, @Valid User user, BindingResult bindingResult,@RequestParam Map<String, String> requestParams,RedirectAttributes redir, HttpServletRequest request) {
				
		// Lookup user in database by e-mail
		User userExists = userService.findByEmail(user.getEmail());
		
		System.out.println(userExists);
               Zxcvbn passwordCheck = new Zxcvbn();
		
		Strength strength = passwordCheck.measure(requestParams.get("password"));
		
		if (strength.getScore() < 3) {
			//modelAndView.addObject("errorMessage", "Your password is too weak.  Choose a stronger one.");
			bindingResult.reject("password");
			
			redir.addFlashAttribute("errorMessage", "Your password is too weak.  Choose a stronger one.");
            return modelAndView;
		}
		
		if (userExists != null) {
			modelAndView.addObject("alreadyRegisteredMessage", "Oops!  There is already a user registered with the email provided.");
			modelAndView.setViewName("register");
			bindingResult.reject("email");
		}
			
		if (bindingResult.hasErrors()) { 
			modelAndView.setViewName("register");		
		} else { // new user so we create user and send confirmation e-mail
					
			/*// Disable user until they click on confirmation link in email
		    user.setEnabled(false);*/
		      
		    // Generate random 36-character string token for confirmation link
		    /*user.setConfirmationToken(UUID.randomUUID().toString());*/
			
			
			
			// Set new password
			user.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));

			// Set user to enabled
			/*user.setEnabled(true);*/
			
		        
		    userService.saveUser(user);
				
			String appUrl = request.getScheme() + "://" + request.getServerName();
			
			SimpleMailMessage registrationEmail = new SimpleMailMessage();
			registrationEmail.setTo(user.getEmail());
			registrationEmail.setSubject("Registration Confirmation");
			registrationEmail.setText("Welcome to our Community");
			registrationEmail.setFrom("noreply@domain.com");
			
			emailService.sendEmail(registrationEmail);
			
			modelAndView.addObject("confirmationMessage", "Register Success And A  confirmation e-mail has been sent to " + user.getEmail()+" Please login Again to continue");
			modelAndView.setViewName("index");
		}
			
		return modelAndView;
	}
	
/*	// Process confirmation link
	@RequestMapping(value="/confirm", method = RequestMethod.GET)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, @RequestParam("token") String token) {
			
		User user = userService.findByConfirmationToken(token);
			
		if (user == null) { // No token found in DB
			modelAndView.addObject("invalidToken", "Oops!  This is an invalid confirmation link.");
		} else { // Token found
			modelAndView.addObject("confirmationToken", user.getConfirmationToken());
		}
			
		modelAndView.setViewName("confirm");
		return modelAndView;		
	}
	
	// Process confirmation link
	@RequestMapping(value="/confirm", method = RequestMethod.POST)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, BindingResult bindingResult, @RequestParam Map<String, String> requestParams, RedirectAttributes redir) {
				
		modelAndView.setViewName("confirm");
		
		Zxcvbn passwordCheck = new Zxcvbn();
		
		Strength strength = passwordCheck.measure(requestParams.get("password"));
		
		if (strength.getScore() < 3) {
			//modelAndView.addObject("errorMessage", "Your password is too weak.  Choose a stronger one.");
			bindingResult.reject("password");
			
			redir.addFlashAttribute("errorMessage", "Your password is too weak.  Choose a stronger one.");

			modelAndView.setViewName("redirect:confirm?token=" + requestParams.get("token"));
			System.out.println(requestParams.get("token"));
			return modelAndView;
		}
	
		// Find the user associated with the reset token
		User user = userService.findByConfirmationToken(requestParams.get("token"));

		// Set new password
		user.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));

		// Set user to enabled
		user.setEnabled(true);
		
		// Save user
		userService.saveUser(user);
		
		modelAndView.addObject("successMessage", "Your password has been set!");
		return modelAndView;		
	}*/
	
}