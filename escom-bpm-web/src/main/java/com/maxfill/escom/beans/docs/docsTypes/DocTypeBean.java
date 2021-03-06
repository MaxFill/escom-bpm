package com.maxfill.escom.beans.docs.docsTypes;

import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.docType.DocTypeFacade;
import com.maxfill.model.basedict.docType.DocType;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.docs.docsTypes.docTypeGroups.DocTypeGroupsBean;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.docTypeGroups.DocTypeGroups;
import com.maxfill.model.basedict.folder.FoldersFacade;
import com.maxfill.model.basedict.BaseDict;
import javax.ejb.EJB;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

/* Сервисный бин "Виды документов" */

@Named
@SessionScoped
public class DocTypeBean extends BaseExplBeanGroups<DocType, DocTypeGroups>{
    private static final long serialVersionUID = -4625860665708197046L; 
    
    @Inject
    private DocTypeGroupsBean ownerBean;
            
    @EJB 
    private DocTypeFacade itemsFacade;    
    @EJB 
    private DocFacade docFacade;
    @EJB 
    private FoldersFacade foldersFacade;


    /* Специфичные действия перед вставкой Вида документа объекта */
    @Override
    public void preparePasteItem(DocType pasteItem, DocType sourceItem, BaseDict owner) {
        super.preparePasteItem(pasteItem, sourceItem, owner);
        if (owner == null) {
            owner = sourceItem.getOwner();
        }
        pasteItem.setOwner((DocTypeGroups) owner);
    }

    @Override
    public DocTypeFacade getLazyFacade() {
        return itemsFacade;
    }   

    @Override
    public BaseDetailsBean getDetailBean() {
        return null;
    }

    @Override
    public List<DocTypeGroups> getGroups(DocType item) {
        List<DocTypeGroups> groups = null;
        if (item.getOwner() != null){
            groups = item.getOwner().getChildItems();
        }
        return groups;
    }
    
    @Override
    protected void checkAllowedDeleteItem(DocType docType, Set<String> errors){
        super.checkAllowedDeleteItem(docType, errors);
        if (!docFacade.findDocsByDocTyper(docType).isEmpty()){
            Object[] messageParameters = new Object[]{docType.getName()};
            String error = MessageFormat.format(MsgUtils.getMessageLabel("DocTypeUsedInDocs"), messageParameters);
            errors.add(error);
        }
        if (!foldersFacade.findFoldersByDocTyper(docType).isEmpty()){
            Object[] messageParameters = new Object[]{docType.getName()};
            String error = MessageFormat.format(MsgUtils.getMessageLabel("DocTypeUsedInFolders"), messageParameters);
            errors.add(error);
        }
    }
    
    @Override
    public void doGetCountUsesItem(DocType docType,  Map<String, Integer> rezult){
        rezult.put("Documents", docFacade.findDocsByDocTyper(docType).size());
    }
        
    @Override
    public boolean addItemToGroup(DocType docType, BaseDict targetGroup){
        if (docType == null || targetGroup == null) return false;
        docType.setOwner((DocTypeGroups)targetGroup);
        getLazyFacade().edit(docType);
        return true;
    }     
    
    @Override
    public Class<DocTypeGroups> getOwnerClass() {
        return DocTypeGroups.class;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return ownerBean;
    }

    @Override
    public BaseDetailsBean getGroupBean() {
        return ownerBean;
    }

}