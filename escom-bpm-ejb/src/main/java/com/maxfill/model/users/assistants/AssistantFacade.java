package com.maxfill.model.users.assistants;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.users.User;
import javax.ejb.Stateless;

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
        return DictObjectName.USER.toLowerCase();
    }
    
}
