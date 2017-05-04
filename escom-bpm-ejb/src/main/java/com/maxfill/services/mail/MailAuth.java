/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.services.mail;

/**
 * Класс аутентификации для почтовой службы
 * @author Maxim
 */
public class MailAuth extends javax.mail.Authenticator { 
    private final String user; 
    private final String password; 

    public MailAuth(String user, String password) { 
        this.user = user; 
        this.password = password; 
    } 

    @Override
    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
            return new javax.mail.PasswordAuthentication(user, password);
        }
}
