package com.example.calories_calculator;

import java.util.HashMap;
import java.util.Map;

public class RegularUser extends User{
    private String admin_mail;
    private HashMap<String, Object> menus;


    public RegularUser(){
    }

    public void set_user(Map<String, Object> user_data){
        this.name = (String) user_data.get("name");
        if (user_data.containsKey("height")) {
            this.height = (Integer) user_data.get("height");
        }
        if (user_data.containsKey("weight")) {
            this.weight = (Integer) user_data.get("weight");
        }
        if (user_data.containsKey("admin_mail")) {
            this.admin_mail = (String) user_data.get("admin_mail");
        }
        this.isAdmin = false;
        this.menus = (HashMap<String, Object>) user_data.get("menus");
        this.admin_mail = (String) user_data.get("admin_mail");
    }

    public HashMap<String, Object> getMenus() {
        return menus;
    }

}
