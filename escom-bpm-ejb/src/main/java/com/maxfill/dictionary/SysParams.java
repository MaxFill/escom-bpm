package com.maxfill.dictionary;

public final class SysParams {

    private SysParams() {
    }    
      
    public static final String ALL = "*";
    public static final String CODE_SEPARATOR = "_";
    public static final int LENGHT_NAME_ELIPSE = 60;   
        
    public static final String LINE_SEPARATOR = System.lineSeparator();           

    //TODO Нужно получать из файла конфигурации и разделитель формировать через + File.separator    
     
    public static final String APP_NAME = "EscomBPM3";
        
    public static final String MAIN_PAGE     = "/view/index.xhtml";
    public static final String LOGIN_PAGE    = "login.xhtml";
    public static final String LOGOUT_PAGE   = "/faces/logout.xhtml";
    public static final String LOGIN_ERROR   = "/faces/view/errors/login-error.xhtml";
    public static final String EXPIRE_PAGE   = "/faces/view/errors/expire.xhtml";
    public static final String ERROR_PAGE    = "/faces/view/errors/505.xhtml";
    public static final String PRIME_URL     = "javax.faces.resource";
    public static final String RESOURCE_URL  = "/faces/resources/"; 
    public static final String AGREE_LICENSE = "/view/admin/license/license-view.xhtml";
    public static final String UPLOAD_PAGE   = "upload";
    
    public static final String OS_UNIX = "UNIX";
    public static final String OS_WIN = "WIN";
        
    public static final int DEFAULT_DOC_TYPE_ID = 1;
    
    public static final int MODE_UNLOCK_CREATE_VERSION = 0;
    public static final int MODE_UNLOCK_DONT_CREATE_VERSION = 1;
    public static final int MODE_UNLOCK_DONT_SAVE_CHANGE = 2;
}
