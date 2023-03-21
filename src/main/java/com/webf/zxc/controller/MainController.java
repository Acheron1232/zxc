package com.webf.zxc.controller;

import com.webf.zxc.entity.EntityUser;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.management.Query;

@Controller
public class MainController {
    @Autowired
    private EntityManager entityManager;
    @RequestMapping("/hello")
    public String hello(HttpServletRequest request, Model model){

        return "index";
    }
    @RequestMapping ("/gg")
    public String gg(@RequestParam("firstName") String firstName,@RequestParam("lastName") String lastName,@RequestParam("email") String email,@RequestParam("loginName") String loginName,@RequestParam("password") String password,@RequestParam("age")int age, Model model){
        Session session =entityManager.unwrap(Session.class);
        EntityUser user = new EntityUser(firstName,lastName,email,loginName,password,age);
        model.addAttribute("name",user);
        session.save(user);
        return "hello";
    }
}
