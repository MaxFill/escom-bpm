package com.maxfill;

import com.maxfill.model.licence.Licence;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.ItemUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.jcr.Repository;

/* Конфигурационные настройки программы */
@Singleton
@LocalBean
public class Configuration {
    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());
    private static final String REPO_NAME = "artifacts";
    private static final String REPO_FULL_NAME = "modeshape-webdav/" + REPO_NAME + "/other/";
    
    @Resource(mappedName="java:/jcr/"+REPO_NAME)
    private javax.jcr.Repository repository;
    
    private String serverOS;
    private Locale serverLocale;
    private String serverURL;
    private String serverPath;
    private String uploadPath;
    private String encoding;
    private String defaultSenderEmail;
    private String defaultEmailServer;
    private String defaultEmailServerPort;
    private String ldapServer;
    private String tempFolder;
    private String jasperReports;
    private String convertorPDF;
    private Licence licence;
    private Integer serverId;        
    
    @PostConstruct
    private void init() {
        String propertyFile = System.getProperty("escom.properties");
        File file = new File(propertyFile);
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(file));
            serverOS = (String) properties.get("SERVER_OS");
            serverURL = (String) properties.get("SERVER_URL");
            serverPath = (String) properties.get("SERVER_PATH");
            uploadPath = (String) properties.get("UPLOAD_PATH");
            encoding = (String) properties.get("ENCODING");
            defaultSenderEmail = (String) properties.get("DEFAULT_EMAIL_SENDER");
            defaultEmailServer = (String) properties.get("DEFAULT_EMAIL_SERVER");
            defaultEmailServerPort = (String) properties.get("DEFAULT_EMAIL_SERVER_PORT");
            ldapServer = (String) properties.get("LDAP_SERVER");
            serverId = Integer.valueOf((String) properties.get("SERVER_ID"));
            tempFolder = (String) properties.get("TEMP_FOLDER");
            jasperReports = (String) properties.get("JASPER_REPORTS");
            convertorPDF = (String) properties.get("CONVERTOR_PDF");
            initLicense();
            initServerLocale((String) properties.get("SERVER_LOCALE"));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public Locale getServerLocale() {
        return serverLocale;
    }    
    public String getServerOS() {
        return serverOS;
    }
    public String getServerPath() {
        return serverPath;
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
    public Integer getServerId() {
        return serverId;
    }
    public String getTempFolder() {
        return tempFolder;
    }  
    public String getJasperReports() {
        return jasperReports;
    }
    public String getConvertorPDF() {
        return convertorPDF;
    }
    public String getServerURL() {
        return serverURL;
    }
    public String getDefaultEmailServer() {
        return defaultEmailServer;
    }
    public String getDefaultEmailServerPort() {
        return defaultEmailServerPort;
    }
    public String getRepositoryName(){
        return REPO_FULL_NAME;
    }
    
    public Repository getRepository() {
        return repository;
    }
 
    private void initServerLocale(String nameLocale){
        serverLocale = new Locale(nameLocale);
    }
    
    private void initLicense(){
        String propertyFile = System.getProperty("license.properties");
        File file = new File(propertyFile);
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file));            
            licence = new Licence((String)properties.get("LICENCE_TERM"),
                Integer.valueOf((String) properties.get("LICENCE_COUNT")),
                (String) properties.get("LICENCE_EDITION"),
                (String) properties.get("LICENCE_NUMBER"),
                (String) properties.get("LICENSOR"));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
