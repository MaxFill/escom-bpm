package com.maxfill.escom.beans.docs.docsTypes.docTypeGroups;

import com.maxfill.facade.DocTypeGroupsFacade;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.escom.beans.BaseCardBean;
import com.maxfill.dictionary.DictObjectName;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Бин для карточки Группы видов документов
 * @author mfilatov
 */
@Named
@ViewScoped
public class DocTypeGroupsCardBean extends BaseCardBean<DocTypeGroups>{
    private static final long serialVersionUID = -8530560023530152318L;    
    
    @EJB
    private DocTypeGroupsFacade itemsFacade;
        
    @Override
    public DocTypeGroupsFacade getItemFacade() {
        return itemsFacade;
    }

    @Override
    protected void onAfterCreateItem(DocTypeGroups item) {        
    }

}
