package com.maxfill.model.basedict.processType;

import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.procTempl.ProcTempl;
import com.maxfill.model.basedict.procTempl.ProcessTemplFacade;
import com.maxfill.model.core.rights.Rights;
import com.maxfill.model.basedict.user.User;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * Фасад для сущности "Виды процессов"
 */
@Stateless
public class ProcessTypesFacade extends BaseDictFacade<ProcessType, ProcessType, ProcessTypeLog, ProcessTypeStates>{
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private ProcessTemplFacade processTemplFacade;
            
    public ProcessTypesFacade() {
        super(ProcessType.class, ProcessTypeLog.class, ProcessTypeStates.class);
    }

    @Override
    public void setSpecAtrForNewItem(ProcessType processType, Map<String, Object> params) {       
        processType.setNameReports("ApprovalSheet");
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
    
    /**
     * Возвращает дефолтный шаблон для вида процесса
     * @param processType
     * @param currentUser
     * @return 
     */
    public ProcTempl getDefaultTempl(ProcessType processType, User currentUser){        
        return processTemplFacade.findActualItemsByOwner(processType, currentUser)
                .stream()
                .filter(templ->templ.getIsDefault())
                .findFirst().orElse(null);
    }
    
    /**
     * Возвращает корневой ВидПроцесса, который не наследует параметры для задач
     * @param processType
     * @return 
     */
    public ProcessType getProcTypeForTasks(ProcessType processType){
        if (processType.isInheritTaskOptions() && processType.getParent() != null){
            return getProcTypeForTasks(processType.getParent());
        }
        return processType;
    }
    
    /**
     * Возвращает корневой ВидПроцесса, который не наследует параметры процесса
     * @param processType
     * @return 
     */
    public ProcessType getProcTypeForOpt(ProcessType processType){
        if (processType.isInheritRunOptions() && processType.getParent() != null){
            return getProcTypeForOpt(processType.getParent());
        }
        return processType;
    }
    
    @Override
    public void remove(ProcessType processType){
        processType.getTemplates().forEach(templ->processTemplFacade.remove(templ));
        super.remove(processType);
    }
}
