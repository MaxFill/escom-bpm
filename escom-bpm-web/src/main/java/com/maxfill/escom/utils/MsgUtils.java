package com.maxfill.escom.utils;

import com.maxfill.dictionary.DictBundles;
import com.maxfill.utils.Tuple;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Утилиты для работы с сообщениями JSF
 */
public final class MsgUtils{
    private MsgUtils() {}

    /**
     * Вывод сообщения об ошибке. Текст ошибки (error) уже ранее должен быть подготовлен!
     * @param error
     */
    public static void errorMessage(String error) {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, error, "");
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }

    /**
     * Вывод сообщения об успехе
     * @param strMessage - готовая к показу строка
     */
    public static void succesMessage(String strMessage) {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, strMessage, "");
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }

    /**
     * Формирование успешного форматированного сообщения.
     * @param keyMsg - ключ для ресурса msg, содержащий вставки типа {0}, {1}
     * @param msgParams - new Object[]{param1, param2}
     */
    public static void succesFormatMsg(String keyMsg, Object[] msgParams) {
        addFacesMsg(FacesMessage.SEVERITY_INFO, keyMsg, msgParams);
    }

    public static void warnFormatMsg(String msg, Object[] msgParams) {
        addFacesMsg(FacesMessage.SEVERITY_WARN, msg, msgParams);
    }

    /**
     * Вывод сообщения об ошибке.
     * @param keyMsg - ключ ресурса msg
     * @param msgParams 
     */
    public static void errorFormatMsg(String keyMsg, Object[] msgParams) {
        addFacesMsg(FacesMessage.SEVERITY_ERROR, keyMsg, msgParams);
    }

    /* Формирует и возвращает FacesMessage c текстом ошибки */
    public static FacesMessage prepFormatErrorMsg(String key, Object[] msgParams){
        return makeFacesMsg(FacesMessage.SEVERITY_ERROR, key, msgParams);
    }

    /**
     * Вывод нескольких сообщений об успешном выполнении.
     * @param messages - Set(FacesMessage)
     */
    public static void showFacesMessages(Set<FacesMessage> messages){
        FacesContext ctx = FacesContext.getCurrentInstance();
        messages.stream().limit(10).forEach(message -> ctx.addMessage(null, message));
    }

    /**
     * Отображение 10-ти сообщений об ошибке. На входе список строк готовых к выводу
     * @param errors
     */
    public static void showErrors(Set<String> errors) {
        errors.stream().limit(10).forEach(error->errorMessage(error));
    }

    /**
     * Отображение 10-ти сообщений об ошибке. На входе список ключей для ресурса msg
     * @param errorKeys список ключей для ресурса msg
     */
    public static void showErrorsMsg(Set<String> errorKeys){
        errorKeys.stream().limit(10).forEach(error->errorMsg(error));
    }
    
    /**
     * Отображение 10-ти сообщений об ошибке. На входе список ключей для ресурса msg
     * @param tuples - список ключей для ресурса msg и значения new Object[]{}
     */
    public static void showTupleErrsMsg(Set<Tuple> tuples){
        tuples.stream().limit(10).forEach(tuple->errorFormatMsg((String)tuple.a, (Object[])tuple.b));
    }
    
    /**
     * Формирование успешного сообщения. На входе ключ для ресурса msg
     * @param keyMsg - ключ для ресурса msg
     */
    public static void succesMsg(String keyMsg) {
        addFacesMsg(FacesMessage.SEVERITY_INFO, keyMsg, new Object[]{});
    }

    /**
     * Формирование предупредительного сообщения. На входе ключ для ресурса msg
     * @param keyMsg - ключ для ресурса msg
     */    
    public static void warnMsg(String keyMsg) {
        addFacesMsg(FacesMessage.SEVERITY_WARN, keyMsg, new Object[]{});
    }

    /**
     * Отображение 10-ти предупреждающих сообщений. На входе список ключей для ресурса msg
     * @param warningKeys - список ключей для ресурса msg     
     */
    public static void showWarningMsg(Set<String> warningKeys){
        warningKeys.stream().limit(10).forEach(warning->warnMsg(warning));
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
     * Формирование и вывод сообщения заданного типа.
     * @param type
     * @param keyMsg - ключ для ресурса msg
     * @param msgParams 
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
     * Формирование FacesMessage сообщения. 
     * @param type
     * @param keyMsg - ключ для ресурса msg
     * @param msgParams
     * @return 
     */
    private static FacesMessage makeFacesMsg(FacesMessage.Severity type, String keyMsg, Object[] msgParams){        
        return new FacesMessage(type, makeFormatMsg(keyMsg, msgParams), "");
    }

    public static String makeFormatMsg(String keyMsg, Object[] msgParams){
        ResourceBundle bundle = getResourceBundle("msg");
        return MessageFormat.format(bundle.getString(keyMsg), msgParams);        
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
