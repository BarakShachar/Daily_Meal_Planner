package com.example.calories_calculator;

import java.util.HashMap;
import java.util.Map;

public class AdminUser extends User {
    private HashMap<String, HashMap<String, HashMap<String,Integer>>> suggestions;

    public AdminUser(){
    }

    public void set_admin(Map<String, Object> user_data){
        this.name = (String) user_data.get("name");
        if (user_data.containsKey("height") && user_data.get("height")!= null) {
            this.height = (Integer) user_data.get("height");
        }
        if (user_data.containsKey("weight") && user_data.get("weight")!= null) {
            this.weight = (Integer) user_data.get("weight");
        }
        this.isAdmin = true;
        this.suggestions = (HashMap<String, HashMap<String, HashMap<String, Integer>>>) user_data.get("menus");
    }

    public HashMap<String, HashMap<String, HashMap<String, Integer>>> getSuggestions() {
        return suggestions;
    }
}
