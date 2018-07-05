package com.maxfill.facade;

import com.google.gson.Gson;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.model.BaseDict;
import com.maxfill.model.BaseLogItems;
import com.maxfill.model.states.BaseStateItem;
import com.maxfill.model.users.User;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Класс определяет методы, для работы с ролями
 * @param <T>
 * @param <O>
 * @param <S>
 */
public abstract class BaseDictWithRolesFacade<T extends BaseDict, O extends BaseDict, L extends BaseLogItems, S extends BaseStateItem> extends BaseDictFacade<T, O, L, S>{

    @Override
    public void edit(T item) {
        doSaveRoleToJson(item);
        super.edit(item);
    }

    @Override
    public void create(T item) {
        doSaveRoleToJson(item);
        super.create(item);
    }

    public BaseDictWithRolesFacade(Class <T> itemClass, Class <L> logClass, Class <S> stateClass) {
        super(itemClass, logClass, stateClass);
    }

    /**
     * Сохранение ролей в строку для записи в базу
     * @param item 
     */
    public void doSaveRoleToJson(T item){
        Gson gson = new Gson();
        String attacheJson = gson.toJson(item.getRoles());
        item.setRoleJson(attacheJson);
    }

    /* Проверка вхождения пользователя в роль документа */
    @Override
    public boolean checkUserInRole(T item, String roleName, User user){
        roleName = roleName.toLowerCase();
        Map<String, Set<Integer>> roles = item.getRoles();
        if (roles.isEmpty() || !roles.containsKey(roleName)) return false;
        Set<Integer> usersIds = (HashSet<Integer>)roles.get(roleName);
        if (usersIds.isEmpty()) return false;
        return usersIds.contains(user.getId());
    }

    /**
     * Возвращает имя пользователя - исполнителя указанной роли
     * @param item
     * @param roleName
     * @return 
     */
    public String getActorName(T item, String roleName){
        Map<String, Set<Integer>> roles = item.getRoles();
        if (roles.isEmpty() || !roles.containsKey(roleName)) return null;
        Set<Integer> usersId = roles.get(roleName);
        if (usersId == null || usersId.isEmpty()) return null;
        StringBuilder names = new StringBuilder();
        usersId.stream().map((userId) -> userFacade.find(userId)).forEach((user) -> {
            if (names.length() > 0){
                names.append(", ");
            }
            names.append(user.getShortFIO());
        });
        return names.toString();
    }
    
}
