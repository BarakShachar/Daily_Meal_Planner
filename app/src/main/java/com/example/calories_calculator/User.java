package com.example.calories_calculator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    protected String name;
    protected String email;
    protected int height;
    protected int weight;
    public boolean isAdmin;

    public User() {}

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWeight() {
        return this.weight;
    }
}
