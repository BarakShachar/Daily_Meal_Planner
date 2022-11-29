package com.example.calories_calculator;

import java.util.HashMap;
import java.util.Map;

public class Create_user {
    private String name;
    private String email;
    private String password;
    private int height;
    private int weight;
    boolean isAdmin;
    private HashMap<String, HashMap<String, HashMap<String,Integer>>> menus;

    public Create_user() {}

    public Create_user(String name, String email, String password, int height, int weight) {
        this.name = name;
        this.email = email;
        this.height = height;
        this.weight = weight;
        this.isAdmin = false;
        this.menus = new HashMap<>();
    }

    public void set_user(Map<String, Object> user_data){
        System.out.println(user_data);
        this.name = (String) user_data.get("name");
        if (user_data.containsKey("height") && user_data.get("height")!= null) {
            this.height = (Integer) user_data.get("height");
        }
        if (user_data.containsKey("weight") && user_data.get("weight")!= null) {
            this.weight = (Integer) user_data.get("weight");
        }
        this.isAdmin = (Boolean) user_data.get("is_admin");
        this.menus = (HashMap<String, HashMap<String, HashMap<String, Integer>>>) user_data.get("menus");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public HashMap<String, HashMap<String, HashMap<String, Integer>>> getMenus() {
        return menus;
    }

    public void setMenus(HashMap<String, HashMap<String, HashMap<String, Integer>>> menus) {
        this.menus = menus;
    }
}
