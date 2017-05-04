/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.escom.beans.system.login;

import java.io.Serializable;

/**
 *
 * @author Filatov Maxim
 */
public class CountryFlags implements Serializable{    

    private static final long serialVersionUID = -5537548573980796679L;
  
    private String displayName; 
    private String name;
    private Integer id;
    
    public CountryFlags() {}
 
    public CountryFlags(Integer id, String name, String displayName) {
        this.id = id;
        this.displayName = displayName;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
 
    public String getDisplayName() {
        return displayName;
    }
 
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
 
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
     
    @Override
    public String toString() {
        return name;
    }
 
}
