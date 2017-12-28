package com.maxfill.escom.beans.system.statuses;

import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.StatusesDocFacade;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.escom.beans.BaseCardBean;
import com.maxfill.escom.beans.docs.docsTypes.DocTypeBean;
import com.maxfill.model.docs.docsTypes.DocType;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
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
   
    public void onAddStatusInTypeDocs(){
        docTypeBean.onManySelectItem();
    }
    
    public void doAddStatusInTypeDocs(SelectEvent event){
        List<DocType> docTypes = (List<DocType>) event.getObject();
        if (docTypes.isEmpty()) return;
        docTypes.stream().forEach(docType -> {
            if (!docType.getStatusDocList().contains(getEditedItem())){
                docType.getStatusDocList().add(getEditedItem());            
                docTypeBean.getItemFacade().edit(docType);
            }
        });
        EscomMsgUtils.succesMsg("AddStatusInSelectedDocumentTypes");
    }
    
    @Override
    public StatusesDocFacade getItemFacade() {
        return itemFacade;
    }

}