package com.maxfill;

import com.maxfill.model.licence.Licence;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;

/* Конфигурационные настройки программы */
@Singleton
@LocalBean
public class Configuration {
    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    private String uploadPath;
    private String encoding;
    private String defaultSenderEmail;
    private String ldapServer;
    private Licence licence;
            
    @PostConstruct
    private void init() {
        String propertyFile = System.getProperty("escom.properties");
        File file = new File(propertyFile);
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(file));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        uploadPath = (String) properties.get("UPLOAD_PATCH");
        encoding = (String) properties.get("ENCODING");
        defaultSenderEmail = (String) properties.get("DEFAULT_EMAIL_SENDER");
        ldapServer = (String) properties.get("LDAP_SERVER");
        licence = new Licence();
        licence.setLicenceNumber((String) properties.get("LICENCE_NUMBER"));
    }
    
    public String getUploadPath() {
        return uploadPath;
    }
    public String getEncoding() {
        return encoding;
    } 
    public String getDefaultSenderEmail() {
        return defaultSenderEmail;
    }
    public String getLdapServer() {
        return ldapServer;
    }
    public Licence getLicence() {
        return licence;
    }
}
