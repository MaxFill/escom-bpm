package com.maxfill.escom.utils;

import com.maxfill.model.BaseDict;
import com.maxfill.model.folders.FolderNavigation;
import com.maxfill.model.users.User;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.ItemUtils;
import com.maxfill.utils.Tuple;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.extensions.model.layout.LayoutOptions;
import org.primefaces.model.TreeNode;

/* Утилиты, вызываемые из CDI бинов */
public final class EscomBeanUtils {

    private EscomBeanUtils() {
    }
    
    public static String rusToEngTranlit (String text){
        String[] abcCyr = {"а","б","в","г","д","е","ё", "ж", "з","и","й","к","л","м","н","о","п","р","с","т","у","ч","ф","х","ц","ш","щ","ы","э","ю","я"};
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
    
    /* Открытие карточки объекта  */
    public static void openItemForm(String formName, String itemOpenKey,  Tuple<Integer, Integer> size) {
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("minWidth", 450);
        options.put("minHeight", 300);
        options.put("width", size.a);
        options.put("height", size.b);
        options.put("maximizable", true);
        options.put("minimizable", true);
        options.put("closable", false);
        options.put("closeOnEscape", false);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        //options.put("headerElement", formName + ":customheader");
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> itemKeyList = new ArrayList<>();
        List<String> openInDialogList = new ArrayList<>();
        openInDialogList.add("true");
        itemKeyList.add(itemOpenKey);
        paramMap.put("itemId", itemKeyList);
        paramMap.put("openInDialog", openInDialogList);
        PrimeFaces.current().dialog().openDynamic(formName + "-card", options, paramMap);
    }    
    
    /* Открытие карточки диалога */
    public static void openDlgFrm(String dlgName, Map<String, List<String>> paramMap, Tuple<Integer, Integer> size) {
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", size.a);
        options.put("height", size.b);
        options.put("minWidth", 600);
        options.put("minHeight", 400);
        options.put("maximizable", true);
        options.put("closable", false);
        options.put("closeOnEscape", false);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        //options.put("headerElement", "centerFRM:customheader");
        PrimeFaces.current().dialog().openDynamic(dlgName, options, paramMap);
    }    
    
    /* Формирует ссылку URL для объекта  */
    public static String doGetItemURL(BaseDict item, String page){
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
    
    /**
     * Формирует информацию о том сколько осталось или на сколько просрочено
     * @param dateStart
     * @param datePlan
     * @return 
     */
    public static String makeDateDiffStatus(Date dateStart, Date datePlan){        
        StringBuilder sb = new StringBuilder();
        if (dateStart.before(datePlan)){
            sb.append(EscomMsgUtils.getBandleLabel("Remained"));    //осталось ...
        } else {
            sb.append(EscomMsgUtils.getBandleLabel("Overdue")).append(" ").append(EscomMsgUtils.getBandleLabel("On")); //просрочено на ...
        }

        Duration duration = Duration.between(dateStart.toInstant(), datePlan.toInstant());              

        StringBuilder dr = new StringBuilder();
        dr.append("d").append("дн.").append("H").append("ч.").append("mm").append("м.");

        String delta = DurationFormatUtils.formatDuration(duration.toMillis(), dr.toString(), true);
        sb.append(" ").append(delta);
        return sb.toString();
    }    
}