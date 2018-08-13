package com.maxfill.escom.beans.docs.docsTypes.docTypeGroups;

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroupsFacade;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.docs.docsTypes.DocTypeBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.docsTypes.DocTypeFacade;

import javax.ejb.EJB;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import org.primefaces.model.TreeNode;

/* Бин для сущности "Группы видов документов" */
@Named
@SessionScoped
public class DocTypeGroupsBean extends BaseTreeBean<DocTypeGroups, DocTypeGroups>{
    private static final long serialVersionUID = -690060212424991825L;  
    
    @Inject
    private DocTypeBean docTypeBean;
    
    @EJB
    private DocTypeGroupsFacade itemsFacade;
    @EJB
    private DocTypeFacade docTypeFacade;        
    
    @Override
    public DocTypeGroupsFacade getFacade() {
        return itemsFacade;
    }
    
    @Override
    public void preparePasteItem(DocTypeGroups pasteItem, DocTypeGroups sourceItem, BaseDict target){
        super.preparePasteItem(pasteItem, sourceItem, target);
        pasteItem.setParent((DocTypeGroups)target);
    }
    
    @Override
    public List<DocTypeGroups> getGroups(DocTypeGroups item) {
        return null;
    }
    
    @Override
    public void doGetCountUsesItem(DocTypeGroups docTypeGroups,  Map<String, Integer> rezult){
        rezult.put("DocTypes", docTypeFacade.findItemByOwner(docTypeGroups).size());
    }

    @Override
    public Class<DocTypeGroups> getOwnerClass() {
        return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return null;
    }

    @Override
    public BaseDetailsBean getDetailBean() {
        return docTypeBean;
    }

    @Override
    protected void doExpandTreeNode(TreeNode node){
        node.setExpanded(true);
    }
}
