package com.maxfill.escom.beans.system.statuses;

import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.docStatuses.StatusesDocFacade;
import com.maxfill.model.basedict.statusesDoc.StatusesDoc;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.beans.docs.docsTypes.DocTypeBean;
import com.maxfill.model.basedict.docType.DocType;
import java.util.List;

import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;

/* Бин для карточки "Статус документа" */
@ViewScoped
@Named
public class StatusesDocCardBean extends BaseCardBean<StatusesDoc>{
    private static final long serialVersionUID = 3106765045991539220L;
    
    @Inject
    private DocTypeBean docTypeBean;
    @EJB
    private StatusesDocFacade itemFacade;

    @Override
    public String getRightsForObjectTitle() {
        return MsgUtils.getBandleLabel("RightsForObject");
    }

    public void onAddStatusInTypeDocs(){
        docTypeBean.onManySelectItem();
    }
    
    public void doAddStatusInTypeDocs(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<DocType> docTypes = (List<DocType>) event.getObject();
        if (docTypes.isEmpty()) return;
        docTypes.stream().forEach(docType -> {
            if (!docType.getStatusDocList().contains(getEditedItem())){
                docType.getStatusDocList().add(getEditedItem());            
                docTypeBean.getLazyFacade().edit(docType);
            }
        });
        MsgUtils.succesMsg("AddStatusInSelectedDocumentTypes");
    }
    
    @Override
    public StatusesDocFacade getFacade() {
        return itemFacade;
    }

}