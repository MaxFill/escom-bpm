
package com.maxfill.dictionary;

import javax.faces.context.FacesContext;

/**
 * Системные параметры
 * @author mfilatov
 */
public final class SysParams {

    private SysParams() {
    }
    
    public static final String ALL = "*";
    public static final Integer ADMIN_ID = 1;
    public static final String CODE_SEPARATOR = "_";
    public static final int LENGHT_NAME_ELIPSE = 40;   
        
    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String ENCODING = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("Encoding");
    public static final String DEFAULT_SENDER = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("DefSenderEmail");
    public static final String LDAP_SERVER = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("LdapServer");
    
    /**
     * путь для хранения шаблонов печатных форм и отчётов
     */
    //public static final String PRINT_FORM_TEMPLATE = "C:\\Work\\escombpm2\\src\\main\\resources\\print\\";
    public static final String PRINT_FORM_TEMPLATE = "print/";        

    /**
     * Путь загрузки файлов вложений
     */
    public static final String UPLOAD_PATCH = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("FilesUploadPath");

    //TODO Нужно получать из файла конфигурации и разделитель формировать через + File.separator    
     
    public static final String APP_NAME = "Escom3";
        
    public static final String MAIN_PAGE    = "/view/index.xhtml";
    public static final String LOGIN_PAGE   = "login.xhtml";
    public static final String LOGOUT_PAGE  = "logout.xhtml";
    public static final String EXPIRE_PAGE  = "view/errors/expire.xhtml";
    public static final String ERROR_PAGE   = "view/errors/505.xhtml";
    public static final String PRIME_URL    = "javax.faces.resource";
    public static final String RESOURCE_URL = "/faces/resources/";    
    
    public static final int DEFAULT_DOC_TYPE_ID = 1;
}
