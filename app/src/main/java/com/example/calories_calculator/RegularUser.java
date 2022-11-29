package com.example.calories_calculator;

import java.util.HashMap;
import java.util.Map;

public class RegularUser extends User{
    private HashMap<String, HashMap<String, HashMap<String,Integer>>> menus;


    public RegularUser(){
    }

    public void set_user(Map<String, Object> user_data){
        this.name = (String) user_data.get("name");
        if (user_data.containsKey("height") && user_data.get("height")!= null) {
            this.height = (Integer) user_data.get("height");
        }
        if (user_data.containsKey("weight") && user_data.get("weight")!= null) {
            this.weight = (Integer) user_data.get("weight");
        }
        this.isAdmin = true;
        this.menus = (HashMap<String, HashMap<String, HashMap<String, Integer>>>) user_data.get("menus");
    }

    public HashMap<String, HashMap<String, HashMap<String, Integer>>> getMenus() {
        return menus;
    }

}
