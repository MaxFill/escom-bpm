package com.maxfill;

import io.jsonwebtoken.impl.crypto.MacProvider;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
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
    
    //ToDo этот параметр вероятно нужно будет удалить ...
    private static AtomicInteger smsMaxCount; //максимальное число sms, которое можно отправить (для демо версии актуален)
    public static final Key SIGN_KEY = MacProvider.generateKey();    

/*
    @Resource(mappedName="java:/jcr/"+REPO_NAME)
    private javax.jcr.Repository repository;
*/
    private static String serverAppURL;
    private static String serverOS;
    private static Locale serverLocale;
    private static String serverURL;
    private static String serverPath;
    private static String uploadPath;
    private static String encoding;
    private static String defaultSenderEmail;
    private static String defaultSmtpServer;
    private static String defaultImapServer;
    private static String defaultSmtpPort;
    private static String defaultIMAPPort;
    private static String ldapServer;
    private static String tempFolder;
    private static String jasperReports;
    private static String convertorPDF;
    private static String convertorTXT;
    private static String pdfEncoding;
    private static String pdfFont;
    private static String fullSearcheConnect;
    private static String smsHostGate;   
    private static String smsHostProtocol;
    private static Integer smsHostPort;
    private static String smsLogin;
    private static String smsPwl;
    private static String smsSender;
    private static String smsCommand;   
    private static Integer maxFileSize;
    private static Integer diskQuote;
    private static Integer maxResultCount;            
    private static Boolean useMailSSLConnect;
    
    static {         
        String propertyFile = System.getProperty("escom.properties");
        File file = new File(propertyFile);
        Properties props = new Properties();
        try {
            LOGGER.log(Level.INFO, "ESCOM WEB APP server start load configuration from escom.properties"); 
            props.load(new FileInputStream(file)); 
            smsHostGate = loadParam(props, "SMS_HOST_GATE");     
            smsHostProtocol = loadParam(props,"SMS_HOST_PROTOCOL");
            smsHostPort = Integer.parseInt(loadParam(props, "SMS_HOST_PORT"));
            smsLogin = loadParam(props, "SMS_LOGIN");
            smsPwl = loadParam(props, "SMS_PWL");
            smsCommand = loadParam(props, "SMS_COMMAND");
            smsSender = loadParam(props, "SMS_SENDER");
            smsMaxCount = new AtomicInteger(Integer.parseInt(loadParam(props, "SMS_MAX_COUNT")));
            fullSearcheConnect = loadParam(props, "FULL_SEARCHE_CONNECT_URL");        
            serverOS = loadParam(props,"SERVER_OS");
            serverURL = loadParam(props,"SERVER_URL");            
            serverAppURL = serverURL + "escom-bpm-web/faces/view";
            serverPath = loadParam(props,"SERVER_PATH");            
            uploadPath = loadParam(props,"UPLOAD_PATH");            
            encoding = loadParam(props,"ENCODING");            
            defaultSenderEmail = loadParam(props,"DEFAULT_EMAIL_SENDER");            
            defaultImapServer = loadParam(props,"DEFAULT_IMAP_SERVER");            
            defaultSmtpServer = loadParam(props,"DEFAULT_SMTP_SERVER");            
            defaultSmtpPort = loadParam(props,"DEFAULT_SMTP_PORT");            
            defaultIMAPPort = loadParam(props,"DEFAULT_IMAP_PORT");            
            useMailSSLConnect = Boolean.valueOf(loadParam(props,"MAIL_SSL_CONNECT"));            
            ldapServer = loadParam(props,"LDAP_SERVER");            
            tempFolder = loadParam(props,"TEMP_FOLDER");
            jasperReports = loadParam(props,"JASPER_REPORTS");            
            convertorPDF = loadParam(props,"CONVERTOR_TO_PDF");            
            convertorTXT = loadParam(props,"CONVERTOR_TO_TXT");            
            pdfEncoding = loadParam(props,"DEFAULT_PDF_ENCODING");            
            pdfFont = loadParam(props,"DEFAULT_PDF_FONT");
            maxFileSize = Integer.parseInt(loadParam(props,"MAX_UPLOAD_SIZE")); 
            maxResultCount = Integer.parseInt(loadParam(props,"MAX_QUERY_RESULT_COUNT"));
            diskQuote = Integer.parseInt(loadParam(props,"DISK_SIZE"));
            serverLocale = new Locale(loadParam(props,"SERVER_LOCALE"));
            LOGGER.log(Level.INFO, "ESCOM WEB APP server load configuration successfully!"); 
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "An error occurred while loading the configuration escom.properties!", ex);
            throw new RuntimeException();
        }
    }
    
    private static String loadParam(Properties properties, String paramName){        
        if (properties.containsKey(paramName)){
            LOGGER.log(Level.INFO, "Load param [{0}] ... ok!", paramName);
            return (String)properties.get(paramName);
        } else {
            LOGGER.log(Level.SEVERE, "ERROR! Param [{0}] not found!", paramName);
            throw new RuntimeException();
        }        
    }    
    
    /* GETS & SETS */

    public Integer getDiskQuote() {
        return diskQuote;
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

    public String getServerAppURL() {
        return serverAppURL;
    }
    
}
