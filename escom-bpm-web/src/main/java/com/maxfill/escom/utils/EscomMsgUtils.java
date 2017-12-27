package com.maxfill.escom.utils;

import com.maxfill.dictionary.DictBundles;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Утилиты для работы с сообщениями JSF
 */
public final class EscomMsgUtils{
    private EscomMsgUtils() {
    }

    public static void WarnMsgDlg(String key1, String key2) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String msg1 = bundle.getString(key1);
        String msg2 = bundle.getString(key2);
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, msg1, msg2);
        RequestContext.getCurrentInstance().showMessageInDialog(msg);
    }

    public static void ErrorMessage(String error) {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", error);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }

    public static void SuccesFormatMessage(String key1, String key2, Object[] messageParameters) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String titleError = bundle.getString(key1);
        String template = bundle.getString(key2);
        String msgError = MessageFormat.format(template, messageParameters);
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, titleError, msgError);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }

    public static void WarnFormatMessage(String key1, String key2, Object[] messageParameters) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String titleError = bundle.getString(key1);
        String template = bundle.getString(key2);
        String msgError = MessageFormat.format(template, messageParameters);
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, titleError, msgError);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }

    public static void ErrorFormatMessage(String key1, String key2, Object[] messageParameters) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String titleError = bundle.getString(key1);
        String template = bundle.getString(key2);
        String msgError = MessageFormat.format(template, messageParameters);
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, titleError, msgError));
    }

    public static FacesMessage prepFormatErrorMsg(String bundleKey, Object[] messageParameters){
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String titleError = bundle.getString("Error");
        String template = bundle.getString(bundleKey);
        String message = MessageFormat.format(template, messageParameters);
        return new FacesMessage(FacesMessage.SEVERITY_ERROR, titleError, message);
    }

    public static FacesMessage prepErrorMsg(String bundleKey){
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String titleError = bundle.getString("Error");
        String message = bundle.getString(bundleKey);
        return new FacesMessage(FacesMessage.SEVERITY_ERROR, titleError, message);
    }

    /* Формирование форматированной строки сообщения из ключей локали */
    public static String makeMessage(Map<String, Object[]> keys){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object[]> entry : keys.entrySet()){
            String msg = MessageFormat.format(getMessageLabel(entry.getKey()), entry.getValue());
            sb.append(msg);
        }
        return sb.toString();
    }

    public static void showFacesMessages(Set<FacesMessage> messages){
        FacesContext ctx = FacesContext.getCurrentInstance();
        messages.stream().limit(10).forEach(message -> ctx.addMessage(null, message));
    }

    public static void showErrorsMsg(Set<String> errors) {
        errors.stream().limit(10).forEach((String error) -> errorMsgAdd("Error", error, ""));
    }

    public static void succesMsgAdd(String key1, String key2) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String msg1 = bundle.getString(key1);
        String msg2 = bundle.getString(key2);
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg1, msg2));
    }

    public static void warnMsgAdd(String key1, String key2) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String msg1 = bundle.getString(key1);
        String msg2 = bundle.getString(key2);
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, msg1, msg2));
    }

    public static void errorMsgAdd(String key1, String key2, String extString) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String msg1 = bundle.getString(key1);
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(key2)) {
            sb.append(bundle.getString(key2)).append(" ");
        }
        if (StringUtils.isNotBlank(extString)) {
            sb.append(extString);
        }
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg1, sb.toString()));
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

    private static String getFromBundle(String key, String bundleName) {
        ResourceBundle bundle = getResourceBundle(bundleName);
        return bundle.getString(key);
    }

    private static ResourceBundle getResourceBundle(String bundleName) {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getApplication().getResourceBundle(context, bundleName);
    }
}
