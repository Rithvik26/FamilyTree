package com.ase.service;

import com.ase.dto.FPUpdatePInput;
import com.ase.dto.LoginInput;
import com.ase.dto.RegisterInput;
import com.ase.dto.Result;
import com.ase.model.ForgetPasswordToken;
import com.ase.model.User;
import com.ase.repo.ForgetPasswordTokenRepo;
import com.ase.repo.UserRepo;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ForgetPasswordTokenRepo forgetPasswordTokenRepo;


    public boolean isValidName(String name) {
        String regex = "^[a-zA-Z0-9]{3,20}$";
        return name.matches(regex);
    }
    public static boolean isValidPassword(String password) {
        String regex = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[@#$%^&+=])"
                + "(?=\\S+$).{8,20}$";

        Pattern p = Pattern.compile(regex);
        Matcher pm = p.matcher(password);
        return pm.matches();
    }

    public User getUserByEmail(String email) {
        Criteria criteria = Criteria.where("email").is(email);
        Query query = new Query(criteria);
        List<User> emailList = mongoTemplate.find(query, User.class);
        return emailList.size() > 0 ? emailList.get(0) : null;
    }
    public Result addNewUser(RegisterInput input){
        Result result = new Result();

        //validating if email is already taken
        Criteria criteria = Criteria.where("email").is(input.getEmail());
        Query query = new Query(criteria);
        List<User> emailList = mongoTemplate.find(query, User.class);
        if(!emailList.isEmpty()){
            result.setStatus("ERROR");
            result.setResult("Email is already Taken");
            return result;
        }
        else if(!isValidName(input.getName())){
            result.setStatus("ERROR");
            result.setResult("Name Should Contain only Alphabets");
            return result;
        }
        else if(!input.getPassword().equals(input.getCpassword())){
            result.setStatus("ERROR");
            result.setResult("Password and Confirm Password did Not Match!");
            return result;
        }
        else if(!isValidPassword(input.getPassword())){
            result.setStatus("ERROR");
            result.setResult("Password should contain atleast one number,lower and uppercase character, special symbol among @#$% and length between 8 and 20");
            return result;
        }
        else{
            User newUser = new User();
            newUser.setName(input.getName());
            newUser.setPassword(passwordEncoder.encode(input.getPassword()));
            newUser.setEmail(input.getEmail());
            newUser.setRole("ROLE_USER");
            userRepo.save(newUser);
            result.setStatus("SUCCESS");
            result.setResult("User Registered SuccessFully!");
            return result;
        }
    }

    public Result login(LoginInput input){
        Result result = new Result();

        Criteria criteria = Criteria.where("email").is(input.getEmail());
        Query query = new Query(criteria);
        List<User> userList = mongoTemplate.find(query, User.class);
        if(userList.size()==0){
            result.setStatus("ERROR");
            result.setResult("User is Not Registered");
            return result;
        }
        else if(!passwordEncoder.matches(input.getPassword(),userList.get(0).getPassword())){
            result.setStatus("ERROR");
            result.setResult("Entered Incorrect Password!");
            return result;
        }
        else{
            result.setStatus("SUCCESS");
            result.setResult("Successfully Logged-In!");
            return result;
        }
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public String generateToken(int length) {

        boolean useLetters = false;
        boolean useNumbers = true;
        String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);

        return generatedString;
    }

    public Result forgotPasswordSendEmail(String email){
        Result result = new Result();
        Criteria criteria = Criteria.where("email").is(email);
        Query query = new Query(criteria);
        List<User> emailList = mongoTemplate.find(query, User.class);
        if(emailList.isEmpty()){
            result.setStatus("ERROR");
            result.setResult("User does not Exist with this Email");
            return result;
        }
        else{
            String token = generateToken(8);
            try {
                sendEmail(email,"Token For Resetting Password",token);
            }
            catch (Exception e){
                result.setStatus("ERROR");
                result.setResult("This Email is not Found to send Token for updating Password!");
                return result;
            }

            Criteria c1 = Criteria.where("email").is(email);
            Query q1 = new Query(criteria);
            List<ForgetPasswordToken> list = mongoTemplate.find(query, ForgetPasswordToken.class);
            if(list.size()==0){
                ForgetPasswordToken forgetPasswordToken = new ForgetPasswordToken();
                forgetPasswordToken.setEmail(email);
                forgetPasswordToken.setToken(token);
                forgetPasswordTokenRepo.save(forgetPasswordToken);
            }
            else{
                ForgetPasswordToken forgetPasswordToken = list.get(0);
                forgetPasswordToken.setEmail(email);
                forgetPasswordToken.setToken(token);
                forgetPasswordTokenRepo.save(forgetPasswordToken);
            }
            result.setStatus("Success");
            result.setResult("Email is sent to user with a token, use it for changing the password");
            return result;
        }

    }


    public Result fpUpdatePassword(FPUpdatePInput input){
        Result result = new Result();
        Criteria criteria = Criteria.where("email").is(input.getEmail());
        Query query = new Query(criteria);
        List<ForgetPasswordToken> emailList = mongoTemplate.find(query, ForgetPasswordToken.class);
        if(emailList.size()>0 && !emailList.get(0).getToken().equals(input.getEmailtoken())){
            result.setStatus("ERROR");
            result.setResult("Token Did not Match!");
            return result;
        }
        else if(!input.getPassword().equals(input.getCpassword())){
            result.setStatus("ERROR");
            result.setResult("Password and Confirm Password did Not Match!");
            return result;
        }
        else if(!isValidPassword(input.getPassword())){
            result.setStatus("ERROR");
            result.setResult("Password should contain atleast one number,lower and uppercase character, special symbol among @#$% and length between 8 and 20");
            return result;
        }
        else{
            //fetch user from db and update password and delete token.
            Criteria c2 = Criteria.where("email").is(input.getEmail());
            Query q2 = new Query(criteria);
            User updateU = mongoTemplate.find(query, User.class).get(0);
            updateU.setPassword(passwordEncoder.encode(input.getPassword()));
            userRepo.save(updateU);
            forgetPasswordTokenRepo.delete(emailList.get(0));
            result.setStatus("SUCCESS");
            result.setResult("Updated Password, please Continue to Login!");
            return result;
        }
    }


}
