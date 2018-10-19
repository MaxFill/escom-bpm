package com.maxfill.facade;

import com.google.gson.Gson;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.BaseLogItems;
import com.maxfill.model.core.messages.UserMessagesFacade;
import com.maxfill.model.core.states.BaseStateItem;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.basedict.userGroups.UserGroups;
import com.maxfill.utils.ItemUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * Класс определяет методы, для работы с ролями
 * @param <T>
 * @param <O>
 * @param <L>
 * @param <S>
 */
public abstract class BaseDictWithRolesFacade<T extends BaseDict, O extends BaseDict, L extends BaseLogItems, S extends BaseStateItem> extends BaseDictFacade<T, O, L, S>{

    @EJB
    private UserMessagesFacade messagesFacade;    

    public BaseDictWithRolesFacade(Class <T> itemClass, Class <L> logClass, Class <S> stateClass) {
        super(itemClass, logClass, stateClass);
    }

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
    
    /**
     * Сохранение ролей в строку для записи в базу
     * @param item 
     */
    public void doSaveRoleToJson(T item){
        Gson gson = new Gson();
        String attacheJson = gson.toJson(item.getRoles());
        item.setRoleJson(attacheJson);
    }

    /**
     * Проверка вхождения пользователя в роль c учётом заместителей
     * @param item
     * @param groupId
     * @param user
     * @return 
     */
    @Override
    public boolean checkUserInRole(T item, Integer groupId, User user){
        //получаем актуальную роль
        UserGroups group = roleFacade.find(groupId);
        if (group == null) return false; //если не нашли
        
        //получаем имя роли
        String roleName = group.getRoleFieldName();
        if (StringUtils.isBlank(roleName)) return false;
        roleName = roleName.toUpperCase();
        
        //получаем роли объекта
        Map<String, Set<Integer>> roles = item.getRoles();
        if (roles.isEmpty() || !roles.containsKey(roleName)) return false; //если роль не найдена среди ролей объекта
        
        //получаем роли состав роли
        Set<Integer> usersIds = (Set<Integer>)roles.get(roleName);
        if (usersIds.isEmpty()) return false; //если состав пустой 
        
        //проверяем состав роли
        if (usersIds.contains(user.getId())) return true; //если пользователь явно указан в роли
        
        //проверяем, не является ли пользователь замом когото из состава роли
        return usersIds.stream()
                .filter(id->userFacade.checkAssistant(id, user))
                .findFirst()
                .orElse(null) != null;
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
        if (CollectionUtils.isEmpty(usersId)) return null;
        StringBuilder names = new StringBuilder();
        usersId.stream().map((userId) -> userFacade.find(userId)).forEach((user) -> {
            if (names.length() > 0){
                names.append(", ");
            }
            names.append(user.getShortFIO());
        });
        return names.toString();
    }
    
    /**
     * Возвращает исполнителей указанной роли
     * @param item
     * @param roleName
     * @return 
     */
    public List<User> getActors(T item, String roleName){ 
        roleName = roleName.trim().toUpperCase();
        Map<String, Set<Integer>> roles = item.getRoles();
        if (roles.isEmpty() || !roles.containsKey(roleName)) return new ArrayList<>();
        Set<Integer> usersIds = roles.get(roleName);
        if (CollectionUtils.isEmpty(usersIds)) return new ArrayList<>();
        return userFacade.findByIds(usersIds, userFacade.getAdmin());                
    }
    
    /**
     * Возвращает исполнителя указанной роли
     * @param item
     * @param roleName
     * @return 
     */
    public User getActor(T item, String roleName){
        return getActors(item, roleName).stream()
                .filter(user->user.getStaff() != null)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Формирует список пользователей, входящих в роль
     * @param item - процесс или документ
     * @param roleName
     * @param currentUser
     * @return 
     */
    public List<User> getUsersFromRole(T item, String roleName, User currentUser){
        roleName = roleName.toUpperCase();
        Map<String, Set<Integer>> roles = item.getRoles();
        if (CollectionUtils.isEmpty(roles) || !roles.containsKey(roleName)) return new ArrayList<>();
        Set<Integer> usersIds = roles.get(roleName);
        return userFacade.findByIds(usersIds, currentUser);
    }     
    
    /**
     * Отправка сообщения ролям
     * @param item - документ или процесс
     * @param rolesJson
     * @param keyMsgSubject - ключ ресурса msg
     * @param sb
     * @param currentUser
     */
    public void sendRoleMessage(T item, String rolesJson, String keyMsgSubject, StringBuilder sb, User currentUser){
        Gson gson = new Gson();        
        List<String> roles = gson.fromJson(rolesJson, List.class);
        roles.forEach(role->{
            getUsersFromRole(item, role, currentUser)
                    .forEach(user -> { 
                        StringBuilder subject = new StringBuilder(ItemUtils.getMessageLabel(keyMsgSubject, userFacade.getUserLocale(user)));
                        subject.append(": ").append(item.getNameEndElipse());
                        messagesFacade.createSystemMessage(user, subject.toString(), sb, Collections.singletonList(item));
                    });
        });
    }
    
    /**
     * Возвращает список ролей объекта
     * @param item
     * @return 
     */
    public List<String> getRoles(T item){
         Map<String, Set<Integer>> roles = item.getRoles();
         return roles.entrySet().stream().map(rec -> rec.getKey()).collect(Collectors.toList());         
    }
    
    /**
     * Добавление роли в объект
     * @param roleName
     * @param item 
     */
    public void addRole(T item, String roleName){
        roleName = roleName.toUpperCase();
        Map<String, Set<Integer>> roles = item.getRoles();
        if (!roles.containsKey(roleName)){
            roles.put(roleName, new HashSet<>());
        }
    }
    
    /**
     * Очистка роли
     * @param item
     * @param roleName 
     */
    public void clearRole(T item, String roleName){
        item.clearRole(roleName);
    }
    
    /**
     * Очистка ролей
     * @param item 
     */
    public void clearRoles(T item){
        item.setRoles(null);
        item.setRoleJson(null);
    }
}