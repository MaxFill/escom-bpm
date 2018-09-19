package com.maxfill.model.users.assistants;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.users.User;
import java.util.List;
import java.util.Objects;
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
}
