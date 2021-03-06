package com.maxfill.escom.utils;

import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.folder.FolderNavigation;
import com.maxfill.model.basedict.user.User;
import java.time.Duration;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
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
        if (item == null) return root;
        TreeNode result = null;        
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
        Duration duration;
        if (dateStart.before(datePlan)){
            sb.append(MsgUtils.getBandleLabel("Remained"));    //осталось ...
            duration = Duration.between(dateStart.toInstant(), datePlan.toInstant());
        } else {
            sb.append(MsgUtils.getBandleLabel("Overdue")).append(" ").append(MsgUtils.getBandleLabel("On")); //просрочено на ...
            duration = Duration.between(datePlan.toInstant(), dateStart.toInstant());
        }

        StringBuilder dr = new StringBuilder();
        dr.append("d").append("д.").append("H").append("ч.").append("mm").append("м.");

        String delta = DurationFormatUtils.formatDuration(duration.toMillis(), dr.toString(), true);
        sb.append(" ").append(delta);
        return sb.toString();
    }  
    
    /**
     * Формирует информацию о длительности
     * @param dateStart
     * @param dateEnd
     * @return 
     */
    public static String makeDuration(Date dateStart, Date dateEnd){
        if (dateStart == null || dateEnd == null) return "";
        if (dateStart.after(dateEnd)) return "";
        Duration duration = Duration.between(dateStart.toInstant(), dateEnd.toInstant());
        StringBuilder dr = new StringBuilder();
        dr.append("d").append("д.").append("H").append("ч.").append("mm").append("м.");
        return DurationFormatUtils.formatDuration(duration.toMillis(), dr.toString(), true);
    }
}