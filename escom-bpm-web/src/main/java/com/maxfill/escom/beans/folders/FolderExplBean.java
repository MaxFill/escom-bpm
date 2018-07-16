package com.maxfill.escom.beans.folders;

import com.maxfill.dictionary.ProcTypesDict;
import com.maxfill.escom.beans.explorer.ExplorerTreeBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.filters.Filter;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.process.types.ProcessType;
import com.maxfill.model.process.types.ProcessTypesFacade;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/* Расширение контролёра обозревателя архива */
@Named
@ViewScoped
public class FolderExplBean extends ExplorerTreeBean{
    private static final long serialVersionUID = 1718197265045722509L;
    
    @Inject
    private ProcessBean processBean;
    @EJB
    private ProcessTypesFacade processTypeFacade;
    
    /* Расширение для поиска в дереве папок по индексу дела */
    @Override
    protected boolean extTreeSearche(String searche, BaseDict item){
        Folder folder = (Folder) item;
        if (StringUtils.isBlank(folder.getFolderNumber())) return false;
        String folderNumber = folder.getFolderFullNumber();
        return folderNumber.equalsIgnoreCase(searche);
    }

    @Override
    public void onAfterFormLoad(String beanId){
        super.onAfterFormLoad(beanId);
        if (getFilterId() != null) {
            Filter filter = filtersFacade.find(getFilterId());
            if (filter != null) {
                makeSelectedFilter(filter);
                PrimeFaces.current().ajax().update("explorer_west");
                PrimeFaces.current().ajax().update("explorer");
            }
        }
    }
        
    /**
     * Открытие формы выбора шаблона процесса согласования и прикрепление к нему документа
     * @param doc 
     */
    public void onCreateConcorderProc(Doc doc){
        Map<String, Object> params = new HashMap<>();
        params.put("document", doc);
        ProcessType processType = processTypeFacade.find(ProcTypesDict.CONCORDER_TYPE);
        if (processType == null){
            MsgUtils.errorFormatMsg("ObjectWithIDNotFound", new Object[]{ProcessType.class.getSimpleName(), ProcTypesDict.CONCORDER_TYPE});
            return;
        } 
        processBean.createItemAndOpenCard(null, processType, params);
    }
}