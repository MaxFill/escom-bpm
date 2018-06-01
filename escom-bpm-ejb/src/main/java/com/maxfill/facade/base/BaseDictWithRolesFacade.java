package com.maxfill.facade.base;

import com.google.gson.Gson;
import com.maxfill.model.BaseDict;
import com.maxfill.model.BaseLogItems;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.process.Process;
import com.maxfill.model.states.BaseStateItem;
import com.maxfill.model.users.User;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Класс определяет методы, для работы с ролями
 * @param <O>
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
        HashSet<Integer> usersId = (HashSet<Integer>)roles.get(roleName);
        if (usersId.isEmpty()) return false;
        return usersId.contains(user.getId());
    }

    /* Возвращает имя исполнителя роли */
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
