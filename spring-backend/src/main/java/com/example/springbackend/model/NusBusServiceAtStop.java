package com.example.springbackend.model;

import java.util.Objects;

public class NusBusServiceAtStop {
    private String name;
    
    
    public NusBusServiceAtStop() {}
    
    
    public NusBusServiceAtStop(String name) {
        this.name = name;
    }
    
  
    public String getName() {
        return name;
    }
    
   
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "NusBusServiceAtStop{name='" + name + "'}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NusBusServiceAtStop that = (NusBusServiceAtStop) obj;
        return Objects.equals(name, that.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}