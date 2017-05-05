/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;

/**
 *
 * @author Maxim
 */
@Singleton
@LocalBean
public class Configuration {

    @Resource(name="UPLOAD_PATCH")
    private String uploadPath;

    @Resource(name="ENCODING")
    private String encoding;
    
    public String getUploadPath() {
        return uploadPath;
    }
    public String getEncoding() {
        return encoding;
    }   
}
