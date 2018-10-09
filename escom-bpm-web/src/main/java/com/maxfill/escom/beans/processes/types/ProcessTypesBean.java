package com.maxfill.escom.beans.processes.types;

import com.maxfill.dictionary.DictExplForm;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.processType.ProcessTypesFacade;
import com.maxfill.model.basedict.processType.ProcessType;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Сервисный бин для работы с сущностью "Виды Процессов" 
 * @author maksim
 */
@Named
@SessionScoped
public class ProcessTypesBean extends BaseTreeBean<ProcessType, ProcessType> {
    private static final long serialVersionUID = -6084661748115239310L;

    @Inject
    private ProcessBean processBean;
    @EJB
    private ProcessTypesFacade processTypesFacade;
      
    @Override
    public List<BaseDict> makeGroupContent(BaseDict item, BaseTableBean tableBean, Integer viewMode, int first, int pageSize, String sortField, String sortOrder) {
        if (Objects.equals(viewMode, DictExplForm.SELECTOR_MODE) && tableBean == this){
            return getLazyFacade().findActualChilds((ProcessType) item, getCurrentUser()).collect(Collectors.toList());
        } else {
            return getDetailBean().getLazyFacade().findActualDetailItems(item, first, pageSize, sortField, sortOrder, getCurrentUser());
        }
    }
    
    @Override
    public BaseDetailsBean getDetailBean() {
        return processBean;
    }

    @Override
    public ProcessTypesFacade getLazyFacade() {
        return processTypesFacade;
    }

    @Override
    public List<ProcessType> getGroups(ProcessType item) {
        return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return null;
    }

    @Override
    public Class <ProcessType> getOwnerClass() {
        return null;
    }

}
