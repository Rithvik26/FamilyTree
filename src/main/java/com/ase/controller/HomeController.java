package com.ase.controller;

import com.ase.config.UserDetailsServiceImpl;
import com.ase.dto.*;
import com.ase.model.User;
import com.ase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/v1")
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    AuthenticationManager authenticationManager;


    @GetMapping("/homePage")
    public String homePage(){
        return "home";
    }
    @GetMapping("/registerPage")
    public String registerPage(){
        return "register";
    }
    @GetMapping("/loginPage")
    public String loginPage(){
        return "login";
    }
    @GetMapping("/forgotPasswordPage")
    public String forgotPasswordPage(){
        return "forgotpassword";
    }

    @GetMapping("/fp_updatepasswordPage")
    public String fp_updatepasswordPage(){
        return "fp_updatepassword";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("input") RegisterInput input, Model model,
                           HttpSession session){

       Result result =  userService.addNewUser(input);
       if(result.getStatus().equals("ERROR")){
           session.setAttribute("err",(String)result.getResult());
           return "redirect:/v1/registerPage";
       }
       else {
           session.setAttribute("msg","Successfully Registered, Please Login");
           return "redirect:/v1/loginPage";
       }
    }

    @GetMapping("/login")
    public String login(@ModelAttribute("input") LoginInput input, Model model,
                        HttpSession session, HttpServletRequest request){
        String email = input.getEmail();
        String password = input.getPassword();
        User user = userService.getUserByEmail(email);
        if(user == null){
            session.setAttribute("err","User does not exist");
            return "redirect:/v1/loginPage";
        }
        if(!passwordEncoder.matches(password,user.getPassword())){
            session.setAttribute("err","Password is incorrect");
            return "redirect:/v1/loginPage";
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        session.setAttribute("user",SecurityContextHolder.getContext().getAuthentication().getName());
        session.setAttribute("email",email);
        session.setAttribute("role",userDetails.getAuthorities().toString());
        return "redirect:/user/home";
    }

    @PostMapping("/forgotPassword")
    public String forgotPassword(@ModelAttribute("input") ForgotPasswordInput input, HttpSession session){

        Result result =  userService.forgotPasswordSendEmail(input.getEmail());
        if(result.getStatus().equals("ERROR")){
            session.setAttribute("err",(String)result.getResult());
            return "redirect:/v1/forgotPasswordPage";
        }
        else {
            session.setAttribute("nextButton",(String)result.getResult());
            session.setAttribute("email",input.getEmail());
            return "redirect:/v1/fp_updatepasswordPage";
        }
    }

    @PostMapping("/fp/updatePassword")
    public String fp_forgotPassword(@ModelAttribute("input") FPUpdatePInput input, HttpSession session){

        Result result =  userService.fpUpdatePassword(input);
        if(result.getStatus().equals("ERROR")){
            session.setAttribute("err",(String)result.getResult());
            return "redirect:/v1/fp_updatepasswordPage";
        }
        else {
            session.setAttribute("msg",(String)result.getResult());
            return "redirect:/v1/fp_updatepasswordPage";
        }
    }





}

