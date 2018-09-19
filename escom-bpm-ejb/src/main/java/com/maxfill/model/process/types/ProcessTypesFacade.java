package com.maxfill.model.process.types;

import com.maxfill.model.process.ProcessFacade;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.users.User;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * Фасад для сущности "Виды процессов"
 */
@Stateless
public class ProcessTypesFacade extends BaseDictFacade<ProcessType, ProcessType, ProcessTypeLog, ProcessTypeStates>{
    @EJB
    private ProcessFacade processFacade;

    public ProcessTypesFacade() {
        super(ProcessType.class, ProcessTypeLog.class, ProcessTypeStates.class);
    }

    @Override
    public int replaceItem(ProcessType oldItem, ProcessType newItem) {
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return 21;
    }

    /**
     * Формирование прав доступа объекта
     * @param item
     * @param user
     * @return 
     */
    @Override
    public Rights getRightItem(BaseDict item, User user) {
        if (item == null) return null;

        if (!item.isInherits()) {
            return getActualRightItem(item, user); //получаем свои права
        }

        if (item.getParent() != null) {
            return getRightItem(item.getParent(), user); //получаем права от родительской группы
        }

        return getDefaultRights(item);
    }

    @Override
    public Rights getRightForChild(BaseDict item){
        if (item == null) return null;

        if (!item.isInheritsAccessChilds()) { //если не наследует права
            return getActualRightChildItem((ProcessType) item);
        }

        if (item.getParent() != null) {
            return getRightForChild(item.getParent()); //получаем права от родителя
        }

        return processFacade.getDefaultRights(); //если иного не найдено, то берём дефолтные права справочника
    }
}
