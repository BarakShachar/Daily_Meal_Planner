package com.example.calories_calculator;

import java.util.HashMap;

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
        this.password = password;
        this.height = height;
        this.weight = weight;
        this.isAdmin = false;
        this.menus = new HashMap<>();
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
