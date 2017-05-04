
package com.maxfill.utils;

import com.maxfill.model.BaseDict;
import java.util.*;

/**
 *
 * @author mfilatov
 */
public final class ItemUtils {  

    private ItemUtils() {
    }       
    
    public static String getBandleLabel(String key){
        ResourceBundle bundle = ResourceBundle.getBundle("locale.Bundle");
        return bundle.getString(key);
    }
    
    public static String getMessageLabel(String key){
        ResourceBundle bundle = ResourceBundle.getBundle("locale.messages");
        return bundle.getString(key);
    }
    
    /**
     * Поиск объекта в списке объектов по его позиции (поиска в таблице обозревателя)
     * @param keyRow
     * @param detailItems
     * @return 
     */ 
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
        
    /**
     * Формирование пути вложенности для дочерних объектов 
     * @param item
     * @return 
     */
    public static String makePath(BaseDict item){
        String name = "";
        if (item.getParent() != null){
            name = makePath(item.getParent());
        }
        if (!name.isEmpty()){
            name = name + "->";
        }
        name = name + item.getName();        
        return name;
    }
    

    /**
     * Проверяет нахождение объекта в списке details владельца (owner) для каждой его группы и если находит, то удаляет объект
     * @param item
     * @param owner
     * @param itemGroups - группы объекта
     */
    public static void removeItemFromGroup(BaseDict item, BaseDict owner, List<BaseDict> itemGroups){
        itemGroups.stream()
            .filter(group -> Objects.equals(owner, group))
                .map(_item -> owner.getDetailItems())
                    .filter(details -> (details.contains(item)))
                    .forEach(details -> details.remove(item));
        /*
        for (O group : itemGroups) {
            if (Objects.equals(owner, group)) {
                List<T> details = owner.getDetailItems();
                if (details.contains(item)){
                    details.remove(item);
                }
            }
        }
        */
    }
    
    /**
     * Проверяет нахождение объекта в списке details владельца (owner) для каждой его группы и если нет, то добавляет объект в него
     * @param item
     * @param owner
     * @param itemGroups - группы объекта
     */
    public static void checkAndAddItemInGroup(BaseDict item, BaseDict owner, List<BaseDict> itemGroups){
        itemGroups.stream()
            .filter(group -> Objects.equals(owner, group))
                .map(_item -> owner.getDetailItems())
                    .filter(details -> (!details.contains(item)))
                    .forEach(details -> details.add(item));
        /*
        for (O group : itemGroups) {
            if (Objects.equals(owner, group)) {
                List<T> details = owner.getDetailItems();
                if (!details.contains(item)){
                    details.add(item);
                }
            }
        }
        */
    }
    
}
