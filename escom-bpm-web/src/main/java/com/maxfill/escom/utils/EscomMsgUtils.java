package com.maxfill.escom.utils;

import com.maxfill.dictionary.DictBundles;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Утилиты для работы с сообщениями JSF
 */
public final class EscomMsgUtils{
    private EscomMsgUtils() {}

    /**
     * Вывод сообщения об ошибке. Текст ошибки (error) уже ранее должен быть подготовлен!
     * @param error
     */
    public static void errorMessage(String error) {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, error, "");
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }

    public static void succesMessage(String strMsg) {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, strMsg, "");
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }

    public static void succesFormatMsg(String msg, Object[] msgParams) {
        addFacesMsg(FacesMessage.SEVERITY_INFO, msg, msgParams);
    }

    public static void warnFormatMsg(String msg, Object[] msgParams) {
        addFacesMsg(FacesMessage.SEVERITY_WARN, msg, msgParams);
    }

    public static void errorFormatMsg(String msg, Object[] msgParams) {
        addFacesMsg(FacesMessage.SEVERITY_ERROR, msg, msgParams);
    }

    /* Формирует и возвращает FacesMessage c текстом ошибки */
    public static FacesMessage prepFormatErrorMsg(String key, Object[] msgParams){
        return makeFacesMsg(FacesMessage.SEVERITY_ERROR, key, msgParams);
    }

    public static void showFacesMessages(Set<FacesMessage> messages){
        FacesContext ctx = FacesContext.getCurrentInstance();
        messages.stream().limit(10).forEach(message -> ctx.addMessage(null, message));
    }

    /**
     * Отображение 10-ти сообщений об ошибке
     * @param errors
     */
    public static void showErrorsMsg(Set<String> errors) {
        errors.stream().limit(10).forEach(error->errorMessage(error));
    }

    /**
     * Формирование FM сообщения
     * @param keyMsg - строка-ресурс из msg
     */
    public static void succesMsg(String keyMsg) {
        addFacesMsg(FacesMessage.SEVERITY_INFO, keyMsg, new Object[]{});
    }

    public static void warnMsg(String keyMsg) {
        addFacesMsg(FacesMessage.SEVERITY_WARN, keyMsg, new Object[]{});
    }

    /**
     * Вывод сообщения об ошибке
     * @param msg ключ ресурса
     */
    public static void errorMsg(String msg) {
        addFacesMsg(FacesMessage.SEVERITY_ERROR, msg, new Object[]{});
    }

    /* Возвращает значение из msg по ключу  */
    public static String getMessageLabel(String key) {
        return getFromBundle(key, DictBundles.MESSAGES_BUNDLE);
    }

    /* Возвращает значение из bundle по ключу */
    public static String getBandleLabel(String key) {
        return getFromBundle(key, DictBundles.LABELS_BUNDLE);
    }

    /* Возвращает значение из validate по ключу */
    public static String getValidateLabel(String key) {
        return getFromBundle(key, DictBundles.VALIDATOR_BUNDLE);
    }

    /**
     * Формирование и вывод FacesMessage сообщения
     */
    private static void addFacesMsg(FacesMessage.Severity type, String keyMsg, Object[] msgParams){
        addMessage(makeFacesMsg(type, keyMsg, msgParams));
    }

    /**
     * Вывод FacesMessage сообщения
     */
    private static void addMessage(FacesMessage message){
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    /**
     * Формирование FacesMessage сообщения
     */
    private static FacesMessage makeFacesMsg(FacesMessage.Severity type, String keyMsg, Object[] msgParams){
        ResourceBundle bundle = getResourceBundle("msg");
        String message = MessageFormat.format(bundle.getString(keyMsg), msgParams);
        return new FacesMessage(type, message, "");
    }

    public static String getFromBundle(String key, String bundleName) {
        ResourceBundle bundle = getResourceBundle(bundleName);
        return bundle.getString(key);
    }

    /* Возвращает запрощенный ресурс по имени */
    private static ResourceBundle getResourceBundle(String bundleName) {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getApplication().getResourceBundle(context, bundleName);
    }


    /* Формирование форматированной строки сообщения из ключей локали */
    /*
    public static String makeMessage(Map<String, Object[]> keys){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object[]> entry : keys.entrySet()){
            String msg = MessageFormat.format(getMessageLabel(entry.getKey()), entry.getValue());
            sb.append(msg);
        }
        return sb.toString();
    }
    */
}
