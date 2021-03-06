package com.maxfill.escom.beans.docs.docsTypes.docTypeGroups;

import com.maxfill.model.basedict.docTypeGroups.DocTypeGroupsFacade;
import com.maxfill.model.basedict.docTypeGroups.DocTypeGroups;
import com.maxfill.escom.beans.BaseCardTree;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.docs.docsTypes.DocTypeBean;
import com.maxfill.model.core.states.State;
import java.util.List;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/* Контролер формы "Группа видов документов" */
@Named
@ViewScoped
public class DocTypeGroupsCardBean extends BaseCardTree<DocTypeGroups>{
    private static final long serialVersionUID = -8530560023530152318L;    
    
    @Inject
    private DocTypeGroupsBean docTypeGroupsBean;
    @Inject
    private DocTypeBean docTypeBean;
    
    @EJB
    private DocTypeGroupsFacade itemsFacade;

    @Override
    public List<State> getStateForChild(){
        return docTypeBean.getMetadatesObj().getStatesList();
    }
    
    @Override
    public DocTypeGroupsFacade getFacade() {
        return itemsFacade;
    }

    @Override
    protected BaseTreeBean getTreeBean() {
        return docTypeGroupsBean;
    }
    
}