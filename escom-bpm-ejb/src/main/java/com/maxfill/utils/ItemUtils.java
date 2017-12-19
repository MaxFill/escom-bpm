package com.maxfill.utils;

import com.maxfill.model.BaseDict;
import java.text.MessageFormat;
import java.util.*;

public final class ItemUtils {  

    private ItemUtils() {
    }
    
    public static String getBandleLabel(String key, Locale locale){
        ResourceBundle bundle = ResourceBundle.getBundle("locale.Bundle", locale);
        return bundle.getString(key);
    }
    
    public static String getMessageLabel(String key, Locale locale){
        ResourceBundle bundle = ResourceBundle.getBundle("locale.messages", locale);
        return bundle.getString(key);
    }
    
    public static String getFormatMessage(String msgKey, Locale locale, Object[] messageParameters ){
        return MessageFormat.format(getMessageLabel(msgKey, locale), messageParameters);    
    }
    
    /* Поиск объекта в списке объектов по его позиции (поиска в таблице обозревателя)  */ 
    public static BaseDict findItemInDetailByKeyRow(Integer keyRow, List<BaseDict> detailItems){
        BaseDict rez = null;
        Integer pos = 0;
        for (BaseDict fc : detailItems){
            if (Objects.equals(pos, keyRow)){
               rez = fc;
               break;
            }
            pos ++;
        }
        return rez; 
    }
        
    /* Формирование пути вложенности для дочерних объектов  */
    public static String makePath(BaseDict item){
        StringBuilder sb = new StringBuilder();
        if (item.getParent() != null){
            sb.append(makePath(item.getParent()));
        }
        if (sb.length() > 0){
            sb.append("->");
        }
        sb.append(item.getName());
        return sb.toString();
    }    

    /* Проверяет нахождение объекта в списке details владельца (owner) для каждой его группы и если находит, то удаляет объект  */
    public static void removeItemFromGroup(BaseDict item, BaseDict owner, List<BaseDict> itemGroups){
        itemGroups.stream()
            .filter(group -> Objects.equals(owner, group))
                .map(_item -> owner.getDetailItems())
                    .filter(details -> (details.contains(item)))
                    .forEach(details -> details.remove(item));
    }
    
    /* Проверяет нахождение объекта в списке details владельца (owner) для каждой его группы и если нет, то добавляет объект в него   */
    public static void checkAndAddItemInGroup(BaseDict item, BaseDict owner, List<BaseDict> itemGroups){
        itemGroups.stream()
            .filter(group -> Objects.equals(owner, group))
                .map(_item -> owner.getDetailItems())
                    .filter(details -> (!details.contains(item)))
                    .forEach(details -> details.add(item));        
    }
    
    /* Возвращает список Id всех дочерних объектов в структуре начиная от заданного объекта */ 
    public static List<BaseDict> getChildsItems(BaseDict item){
        List<BaseDict> childItems = new ArrayList<>();
        addChildInList(item, childItems);
        return childItems;
    }
    
    private static void addChildInList(BaseDict item, List<BaseDict> childItems){
        childItems.add(item);
        if (item.getChildItems() != null ){
            Integer s = item.getChildItems().size();
            item.getChildItems().stream().forEach(child -> addChildInList((BaseDict) child, childItems));
        }
    }
}
