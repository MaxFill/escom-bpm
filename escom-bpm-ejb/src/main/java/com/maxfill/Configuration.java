package com.maxfill;

import com.maxfill.model.licence.Licence;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.bouncycastle.crypto.CryptoException;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.jcr.Repository;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

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

    private Licence licence;
    private Integer serverId;
    private Integer maxFileSize;

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
            convertorPDF = (String) properties.get("CONVERTOR_PDF");
            pdfEncoding = (String) properties.get("DEFAULT_PDF_ENCODING");
            pdfFont = (String) properties.get("DEFAULT_PDF_FONT");
            maxFileSize = Integer.valueOf((String) properties.get("MAX_UPLOAD_SIZE"));
            smsHostGate = (String) properties.get("SMS_HOST_GATE");
            smsHostProtocol = (String) properties.get("SMS_HOST_PROTOCOL");
            smsHostPort = Integer.valueOf((String) properties.get("SMS_HOST_PORT"));
            smsLogin = (String) properties.get("SMS_LOGIN");
            smsPwl = (String) properties.get("SMS_PWL");
            smsCommand = (String) properties.get("SMS_COMMAND");
            smsSender = (String) properties.get("SMS_SENDER");
            Integer smsCount = Integer.valueOf((String) properties.get("SMS_MAX_COUNT"));
            smsMaxCount = new AtomicInteger(smsCount);
            initLicense();
            initServerLocale((String) properties.get("SERVER_LOCALE"));
            signKey = MacProvider.generateKey();
            fullSearcheConnect = (String) properties.get("FULL_SEARCHE_CONNECT_URL");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
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

    private void initLicense(){
        final byte[] keyValue = new byte[]{'L', '1', '_', 'D', '2', '-', 'z', 'O', 'j', 'w', 'e', 'c', '4', 'L', '4', '!'};

        String propertyFile = System.getProperty("license-info");
        File inputFile = new File(propertyFile);

        try {
            Key secretKey = new SecretKeySpec(keyValue, "AES");;
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(outputBytes));

            licence = new Licence((String)properties.get("LICENCE_TERM"),
                    Integer.valueOf((String) properties.get("LICENCE_COUNT")),
                    (String) properties.get("LICENCE_EDITION"),
                    (String) properties.get("LICENCE_NUMBER"),
                    (String) properties.get("LICENSOR"));

            inputStream.close();

        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
