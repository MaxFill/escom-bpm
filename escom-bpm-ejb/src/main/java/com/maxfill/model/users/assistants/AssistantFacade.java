package com.maxfill.model.users.assistants;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.users.User;
import java.util.List;
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
    public Class<Assistant> getItemClass() {
        return Assistant.class;
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
    public String getFRM_NAME() {
        return DictObjectName.ASSISTANT.toLowerCase();
    }
 
    @Override
    protected void dublicateCheckAddCriteria(CriteriaBuilder builder, Root<Assistant> root, List<Predicate> criteries, Assistant item){
       criteries.add(builder.equal(root.get("user"), item.getUser()));
       criteries.add(builder.equal(root.get("owner"), item.getOwner()));
    }

}
