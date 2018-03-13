package com.maxfill.escom.utils;

import com.maxfill.dictionary.DictBundles;
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
    private EscomMsgUtils() {
    }

    /**
     * Вывод сообщения в диалоге
     */
    public static void warnMsgDlg(String title, String msg, Object[] msgParams) {
        FacesMessage message = makeFacesMsg(FacesMessage.SEVERITY_WARN, title, msg, msgParams);
        RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public static void errorMessage(String error) {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", error);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }

    public static void succesFormatMsg(String msg, Object[] msgParams) {
        addFacesMsg(FacesMessage.SEVERITY_INFO, "Successfully", msg, msgParams);
    }

    public static void warnFormatMsg(String msg, Object[] msgParams) {
        addFacesMsg(FacesMessage.SEVERITY_WARN, "Attention", msg, msgParams);
    }

    public static void errorFormatMsg(String msg, Object[] msgParams) {
        addFacesMsg(FacesMessage.SEVERITY_ERROR,  "Error", msg, msgParams);
    }

    /* Формирует и возвращает FacesMessage c текстом ошибки */
    public static FacesMessage prepFormatErrorMsg(String key, Object[] msgParams){
        return makeFacesMsg(FacesMessage.SEVERITY_ERROR, "Error", key, msgParams);
    }

    public static void showFacesMessages(Set<FacesMessage> messages){
        FacesContext ctx = FacesContext.getCurrentInstance();
        messages.stream().limit(10).forEach(message -> ctx.addMessage(null, message));
    }

    /**
     * Отображение 10-ти сообщений об ошибке
     */
    public static void showErrorsMsg(Set<String> errors) {
        errors.stream().limit(10).forEach(error -> errorMsg(error));
    }

    public static void succesMsg(String msg) {
        addFacesMsg(FacesMessage.SEVERITY_INFO, "Successfully", msg, new Object[]{});
    }

    public static void warnMsg(String msg) {
        addFacesMsg(FacesMessage.SEVERITY_WARN, "Attention", msg, new Object[]{});
    }

    public static void errorMsg(String msg) {
        addFacesMsg(FacesMessage.SEVERITY_ERROR,  "Error", msg, new Object[]{});
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
     * Вывод FacesMessage сообщения
     */
    private static void addFacesMsg(FacesMessage.Severity type, String keyTitle, String keyMsg, Object[] msgParams){
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.addMessage(null, makeFacesMsg(type, keyTitle, keyMsg, msgParams));
    }

    /**
     * Формирование FacesMessage сообщения
     */
    private static FacesMessage makeFacesMsg(FacesMessage.Severity type, String keyTitle, String keyMsg, Object[] msgParams){
        ResourceBundle bundle = getResourceBundle("msg");
        String message = MessageFormat.format(bundle.getString(keyMsg), msgParams);
        String title = bundle.getString(keyTitle);
        return new FacesMessage(type, title, message);
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
