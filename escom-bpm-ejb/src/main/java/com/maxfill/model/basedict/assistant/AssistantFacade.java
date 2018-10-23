package com.maxfill.model.basedict.assistant;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.basedict.user.User;
import java.util.Comparator;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Фасад для сущности "Заместитель"
 */
@Stateless
public class AssistantFacade extends BaseDictFacade<Assistant, User, AssistantLog, AssistantStates >{

    public AssistantFacade() {
        super(Assistant.class, AssistantLog.class, AssistantStates.class);
    }

    @Override
    public int replaceItem(Assistant oldItem, Assistant newItem) {
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_ASSISTANT;
    }
 
    @Override
    protected void dublicateCheckAddCriteria(CriteriaBuilder builder, Root<Assistant> root, List<Predicate> criteries, Assistant item){
       criteries.add(builder.equal(root.get("user"), item.getUser()));
       criteries.add(builder.equal(root.get("owner"), item.getOwner()));
    }

    /**
     * Определяет, является пользователь slave заместителем для chief
     * @param chief
     * @param slave
     * @return 
     */
    public boolean isAssistant(User chief, User slave){
        return chief.getAssistants().stream()
                .filter(assist->Objects.equals(slave, assist.getUser()))
                .findFirst()
                .orElse(null) != null;
    }
    
    /**
     * Определяет, является ли user руководителем для slave
     * @param user
     * @param slave
     * @return 
     */    
    public boolean isChief(User user, User slave){
        return user.getAssistants().stream()
                .filter(assist->Objects.equals(assist.getUser(), slave))
                .findFirst()
                .orElse(null) != null;
    }
    
    /**
     * Формирует список штатных единиц из заместителей, указанного пользователя
     * @param chief
     * @return 
     */
    public List<Staff> findAssistByUser(User chief){
        return chief.getAssistants().stream()
                .filter(assist-> !assist.getUser().isDeleted() 
                        && assist.getUser().isActual() 
                        && assist.getUser().getStaff() != null)
                .map(assist->assist.getUser().getStaff())                
                .collect(Collectors.toList());
    }
}
