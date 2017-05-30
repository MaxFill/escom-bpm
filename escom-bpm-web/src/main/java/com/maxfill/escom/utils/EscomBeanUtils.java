package com.maxfill.escom.utils;

import com.maxfill.model.BaseDict;
import com.maxfill.model.folders.FolderNavigation;
import com.maxfill.model.states.State;
import com.maxfill.model.users.User;
import com.maxfill.utils.ItemUtils;
import com.maxfill.utils.Tuple;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.extensions.model.layout.LayoutOptions;
import org.primefaces.model.TreeNode;

/* Утилиты, вызываемые из бинов */
public final class EscomBeanUtils {

    private EscomBeanUtils() {
    }
    
    public static String rusToEngTranlit (String text){
        String[] abcCyr = {"a","б","в","г","д","е","ё","ж","з","и","й","к","л","м","н","п","о","р","с","т","у","ч","ф","х","ц","ш","щ","ы","э","ю","я"};
        String[] abcLat = {"a","b","v","g","d","e","jo","zh","z","i","j","k","l","m","n","o","p","r","s","t","u","ch","f","h","ts","sh","sch","","e","ju","ja"};
        return StringUtils.replaceEach(text, abcCyr, abcLat);
    }
    
    /* Инициализация областей обозревателя */
    public static void initLayoutOptions(LayoutOptions layoutOptions){
        LayoutOptions north = new LayoutOptions();
        north.addOption("resizable", false);
        north.addOption("closable", false);
        north.addOption("size", 40);
        layoutOptions.setNorthOptions(north);

        LayoutOptions south = new LayoutOptions();
        south.addOption("resizable", false);
        south.addOption("closable", false);
        south.addOption("size", 42);
        layoutOptions.setSouthOptions(south);

        LayoutOptions west = new LayoutOptions();
        west.addOption("size", 255);
        west.addOption("minSize", 150);
        west.addOption("maxSize", 500);
        west.addOption("resizable", true);
        west.addOption("initClosed", false);
        layoutOptions.setWestOptions(west);

        LayoutOptions east = new LayoutOptions();
        east.addOption("size", 300);
        east.addOption("minSize", 150);
        east.addOption("maxSize", 450);
        east.addOption("resizable", true);
        east.addOption("initClosed", false);
        layoutOptions.setEastOptions(east);

        LayoutOptions center = new LayoutOptions();
        center.addOption("resizable", true);
        center.addOption("closable", false);
        center.addOption("minSize", 400);
        center.addOption("size", 950);
        center.addOption("minWidth", 400);
        center.addOption("minHeight", 200);
        layoutOptions.setCenterOptions(center);        
    }
    
    public static void initCardLayout(LayoutOptions layoutOptions){
        LayoutOptions panes = new LayoutOptions();
        panes.addOption("slidable", false);
        layoutOptions.setPanesOptions(panes);

        LayoutOptions north = new LayoutOptions();
        north.addOption("resizable", false);
        north.addOption("closable", false);
        north.addOption("size", 38);
        layoutOptions.setNorthOptions(north);
        
        LayoutOptions south = new LayoutOptions();
        south.addOption("resizable", false);
        south.addOption("closable", false);
        south.addOption("size", 45);
        layoutOptions.setSouthOptions(south);

        LayoutOptions west = new LayoutOptions();
        west.addOption("size", 170);
        west.addOption("minSize", 150);
        west.addOption("maxSize", 250);
        west.addOption("resizable", true);
        layoutOptions.setWestOptions(west);

        LayoutOptions center = new LayoutOptions();
        center.addOption("resizable", true);
        center.addOption("closable", false);
        center.addOption("minWidth", 200);
        center.addOption("minHeight", 100);
        layoutOptions.setCenterOptions(center);
    }   
    
    public static void initAddLayoutOptions(LayoutOptions layoutOptions){
        /*
        LayoutOptions east = layoutOptions.getEastOptions();
        LayoutOptions childOptions = new LayoutOptions();
        east.setChildOptions(childOptions);

        LayoutOptions searcheABC = new LayoutOptions();
        searcheABC.addOption("size", 85);
        searcheABC.addOption("minSize", 85);
        searcheABC.addOption("maxSize", 85);
        childOptions.setWestOptions(searcheABC); 
        */
        LayoutOptions center = layoutOptions.getCenterOptions();
        LayoutOptions childCenterOptions = new LayoutOptions();
        center.setChildOptions(childCenterOptions);
        
        LayoutOptions centerSouth = new LayoutOptions();
        centerSouth.addOption("size", "15%");
        childCenterOptions = layoutOptions.getCenterOptions().getChildOptions();
        childCenterOptions.setSouthOptions(centerSouth);
    }    
    
    /* Получение bean по его имени */
    @SuppressWarnings("unchecked")
    public static <T> T findBean(String beanName, FacesContext context) {
        return (T) context.getApplication().evaluateExpressionGet(context, "#{" + beanName + "}", Object.class);
    }

    public static void WarnMsgDlg(String key1, String key2) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String msg1 = bundle.getString(key1);
        String msg2 = bundle.getString(key2);
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, msg1, msg2);
        RequestContext.getCurrentInstance().showMessageInDialog(msg);
    }

    //поиск позиции в навигационной цепочке папок
    public static BaseDict findUiNavigatorItem(Deque navigator, Integer keyRow) {
        BaseDict rez = null;
        int pos = 0;
        for (Object n : navigator) {
            if (pos == keyRow) {
                FolderNavigation fn = (FolderNavigation) n;
                rez = fn.getFolder();
                break;
            }
            pos++;
        }
        return rez;
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

    /* Формирование форматированной строки сообщения из ключей локали */
    public static String makeMessage(Map<String, Object[]> keys){
        StringBuilder sb = new StringBuilder(); 
        for (Entry<String, Object[]> entry : keys.entrySet()){           
            String msg = MessageFormat.format(getMessageLabel(entry.getKey()), entry.getValue());
            sb.append(msg);
        }
        return sb.toString();
    }
    
    /* Поиск позиции в дереве по значению объекта */
    public static TreeNode findTreeNode(TreeNode root, Object item) {
        TreeNode result = null;
        if (item == null) {
            result = root;
        } else {
            for (TreeNode child : root.getChildren()) {
                Object data = child.getData();
                if (Objects.equals(item, data)) {
                    result = child;
                    break;
                }
                result = findTreeNode(child, item);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    /* Выполняет поиск позиции в таблице контента */
    public static BaseDict findUITableContent(List<BaseDict> root, Integer keyRow) {
        BaseDict rez = null;
        int pos = 0;
        for (BaseDict fc : root) {
            if (pos == keyRow) {
                rez = fc;
                break;
            }
            pos++;
        }
        return rez;
    }

    public static void WarnMsgAdd(String key1, String key2) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String msg1 = bundle.getString(key1);
        String msg2 = bundle.getString(key2);
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, msg1, msg2));
    }

    public static void showErrorsMsg(Set<String> errors) {
        errors.stream().limit(10).forEach((String error) -> ErrorMsgAdd("Error", "", error));
    }

    public static void SuccesMsgAdd(String key1, String key2) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String msg1 = bundle.getString(key1);
        String msg2 = bundle.getString(key2);
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg1, msg2));
    }

    //поиск позиции в дереве
    public static TreeNode findUiTreeNode(TreeNode root, String rowKey) {
        TreeNode result = null;
        if (root.getRowKey().equals(rowKey)) {
            return root;
        }
        for (TreeNode child : root.getChildren()) {
            if (child.getRowKey().equals(rowKey)) {
                result = child;
                break;
            }
            result = findUiTreeNode(child, rowKey);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    public static void ErrorMsgAdd(String key1, String key2, String extString) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String msg1 = bundle.getString(key1);
        StringBuilder msg2 = new StringBuilder();
        if (StringUtils.isNotBlank(key2)) {
            msg2.append(bundle.getString(key2)).append(" ");
        }
        if (StringUtils.isNotBlank(extString)) {
            msg2.append(extString);
        }
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg1, msg2.toString()));
    }

    public static UIComponent findUIComponent(UIComponent root, String id) {
        UIComponent result = null;
        if (root.getId().equals(id)) {
            return root;
        }
        for (UIComponent child : root.getChildren()) {
            if (child.getId().equals(id)) {
                result = child;
                break;
            }
            result = findUIComponent(child, id);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    public static void ErrorFormatMessage(String key1, String key2, Object[] messageParameters) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        String titleError = bundle.getString(key1);
        String template = bundle.getString(key2);
        String msgError = MessageFormat.format(template, messageParameters);
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, titleError, msgError);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }

    /* Возвращает значение из msg по ключу  */
    public static String getMessageLabel(String key) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "msg");
        return bundle.getString(key);
    }

    /* Возвращает значение из bundel по ключу */
    public static String getBandleLabel(String key) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "bundle");
        return bundle.getString(key);
    }
    
    /* Открытие карточки объекта  */
    public static void openItemForm(String formName, String itemOpenKey,  Tuple<Double, Double> size) {
        Map<String, Object> options = new HashMap<>();
        //options.put("headerElement", formName + ":btnClose");
        options.put("resizable", true);
        options.put("modal", true);
        options.put("minWidth", 450);
        options.put("minHeight", 300);
        options.put("width", size.a);
        options.put("height", size.b);
        options.put("maximizable", true);
        options.put("closable", false);
        options.put("closeOnEscape", false);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> itemKeyList = new ArrayList<>();
        itemKeyList.add(itemOpenKey);
        paramMap.put("itemOpenKey", itemKeyList);
        RequestContext.getCurrentInstance().openDialog(formName + "-card", options, paramMap);
    }    
    
    /* Открытие карточки диалога */
    public static void openDlgFrm(String dlgName, Map<String, List<String>> paramMap, Tuple<Double, Double> size) {
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", size.a);
        options.put("height", size.b);
        options.put("minWidth", 600);
        options.put("minHeight", 400);
        options.put("maximizable", true);
        options.put("closable", true);
        options.put("closeOnEscape", true);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        RequestContext.getCurrentInstance().openDialog(dlgName, options, paramMap);
    }
    
    /* Открытие формы нового почтового сообщения  */
    public static void openMailMsgForm(String mode, List<BaseDict> docs){      
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", 700);
        options.put("height", 500);
        options.put("maximizable", true);
        options.put("closable", true);
        options.put("closeOnEscape", true);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        List<String> openModeList = new ArrayList<>();
        openModeList.add(mode); 
        List<Integer> idList = docs.stream().map(BaseDict::getId).collect(Collectors.toList());
        String docIds = org.apache.commons.lang3.StringUtils.join(idList, ",");
        List<String> docsList = new ArrayList<>();
        docsList.add(docIds); 
        Map<String, List<String>> paramMap = new HashMap<>();
        paramMap.put("modeSendAttache", openModeList);
        paramMap.put("docIds", docsList);
        RequestContext.getCurrentInstance().openDialog("/view/services/mail-message", options, paramMap);
    }
    
    /* Формирует ссылку URL для объекта  */
    public static String doGetItemURL(BaseDict item, String page, String openMode){
        ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) ectx.getRequest();
        
        String serverURL = null;
        String docURL = "";

        try {
            serverURL = new URL(request.getScheme(),
                    request.getServerName(),
                    request.getServerPort(),
                    request.getContextPath()).toString();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ItemUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!org.apache.commons.lang3.StringUtils.isEmpty(serverURL)){
            StringBuilder builder = new StringBuilder();
            builder.append(serverURL);

            builder.append("/faces/view/").append(page).append(".xhtml").append("?itemId=");
            builder.append(item.getId());
            builder.append("?openMode=");
            builder.append(openMode);
            docURL = builder.toString();
        }
        return docURL;
    }
    
    /* Формирует ключ открываемого объекта */
    public static String makeOpenItemKey(String itemKey, Integer openMode, User user){
        StringBuilder sb = new StringBuilder();
        sb.append(itemKey).append("_").append(openMode).append("_").append(user.getId());
        return sb.toString();
    }   

}
