package com.maxfill;

import io.jsonwebtoken.impl.crypto.MacProvider;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.jcr.Repository;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/* Конфигурационные настройки программы */
@Singleton
@LocalBean
public class Configuration {
    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());
    private static final String REPO_NAME = "artifacts";
    private static final String REPO_FULL_NAME = "modeshape-webdav/" + REPO_NAME + "/other/";
    private static AtomicInteger smsMaxCount;

    @Resource(mappedName="java:/jcr/"+REPO_NAME)
    private javax.jcr.Repository repository;

    private String serverOS;
    private Locale serverLocale;
    private String serverURL;
    private String serverPath;
    private String uploadPath;
    private String encoding;
    private String defaultSenderEmail;
    private String defaultSmtpServer;
    private String defaultImapServer;
    private String defaultSmtpPort;
    private String defaultIMAPPort;
    private String ldapServer;
    private String tempFolder;
    private String jasperReports;
    private String convertorPDF;
    private String convertorTXT;
    private String pdfEncoding;
    private String pdfFont;
    private String fullSearcheConnect;

    private String smsHostGate;
    private String smsHostProtocol;
    private Integer smsHostPort;
    private String smsLogin;
    private String smsPwl;
    private String smsSender;
    private String smsCommand;

    private Integer serverId;
    private Integer maxFileSize;

    private Integer maxResultCount;
    
    private Key signKey;
    private Boolean useMailSSLConnect;
    
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
            defaultImapServer = (String) properties.get("DEFAULT_IMAP_SERVER");
            defaultSmtpServer = (String) properties.get("DEFAULT_SMTP_SERVER");
            defaultSmtpPort = (String) properties.get("DEFAULT_SMTP_PORT");
            defaultIMAPPort = (String) properties.get("DEFAULT_IMAP_PORT");
            useMailSSLConnect = Boolean.valueOf((String) properties.get("MAIL_SSL_CONNECT"));
            ldapServer = (String) properties.get("LDAP_SERVER");
            serverId = Integer.valueOf((String) properties.get("SERVER_ID"));
            tempFolder = (String) properties.get("TEMP_FOLDER");
            jasperReports = (String) properties.get("JASPER_REPORTS");
            convertorPDF = (String) properties.get("CONVERTOR_TO_PDF");
            convertorTXT = (String) properties.get("CONVERTOR_TO_TXT");
            pdfEncoding = (String) properties.get("DEFAULT_PDF_ENCODING");
            pdfFont = (String) properties.get("DEFAULT_PDF_FONT");
            maxFileSize = Integer.valueOf((String) properties.get("MAX_UPLOAD_SIZE"));
            maxResultCount = Integer.valueOf((String) properties.get("MAX_QUERY_RESULT_COUNT"));
            smsHostGate = (String) properties.get("SMS_HOST_GATE");
            smsHostProtocol = (String) properties.get("SMS_HOST_PROTOCOL");
            smsHostPort = Integer.valueOf((String) properties.get("SMS_HOST_PORT"));
            smsLogin = (String) properties.get("SMS_LOGIN");
            smsPwl = (String) properties.get("SMS_PWL");
            smsCommand = (String) properties.get("SMS_COMMAND");
            smsSender = (String) properties.get("SMS_SENDER");
            Integer smsCount = Integer.valueOf((String) properties.get("SMS_MAX_COUNT"));
            smsMaxCount = new AtomicInteger(smsCount);
            initServerLocale((String) properties.get("SERVER_LOCALE"));
            signKey = MacProvider.generateKey();
            fullSearcheConnect = (String) properties.get("FULL_SEARCHE_CONNECT_URL");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public Integer getMaxResultCount() {
        return maxResultCount;
    }    
    public String getConvertorTXT() {
        return convertorTXT;
    }   
    public Integer getMaxFileSize() {
        return maxFileSize;
    }    
    public String getPdfFont() {
        return pdfFont;
    }    
    public String getPdfEncoding() {
        return pdfEncoding;
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
    public String getDefaultSmtpServer() {
        return defaultSmtpServer;
    }
    public String getDefaultSmtpPort() {
        return defaultSmtpPort;
    }
    public String getRepositoryName(){
        return REPO_FULL_NAME;
    }
    public Key getSignKey() {
        return signKey;
    }
    public Boolean getUseMailSSLConnect() {
        return useMailSSLConnect;
    }
    public String getDefaultIMAPPort() {
        return defaultIMAPPort;
    }
    public String getDefaultImapServer() {
        return defaultImapServer;
    }
    public String getFullSearcheConnect() {
        return fullSearcheConnect;
    }
    public String getSmsHostGate() {
        return smsHostGate;
    }
    public String getSmsHostProtocol() {
        return smsHostProtocol;
    }
    public Integer getSmsHostPort() {
        return smsHostPort;
    }
    public String getSmsLogin() {
        return smsLogin;
    }
    public String getSmsPwl() {
        return smsPwl;
    }
    public String getSmsCommand() {
        return smsCommand;
    }
    public String getSmsSender() {
        return smsSender;
    }

    public Integer changeSmsCount() {
        return smsMaxCount.decrementAndGet();
    }

    public Integer getSmsMaxCount() {
        return smsMaxCount.get();
    }

    public Repository getRepository() {
        return repository;
    }
 
    private void initServerLocale(String nameLocale){
        serverLocale = new Locale(nameLocale);
    }

}
