package com.maxfill.escom.beans.folders;

import com.maxfill.facade.FoldersFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.escom.beans.BaseCardTree;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.states.State;
import org.primefaces.event.SelectEvent;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

/* Карточка Папки */

@Named
@ViewScoped
public class FoldersCardBean extends BaseCardTree<Folder> {
    private static final long serialVersionUID = 1052362714114861680L;  
    
    @Inject
    private FoldersBean foldersBean;
    @Inject
    private DocBean docBean;
    
    @EJB
    private FoldersFacade foldersFacade;                     
    
    /* Обработка события изменения типа документа на форме карточки папки  */
    public void onDocTypeDefaultSelected(SelectEvent event){
        List<DocType> items = (List<DocType>) event.getObject();
        if (items.isEmpty()){return;}
        DocType item = items.get(0);
        onItemChange();
        getEditedItem().setDocTypeDefault(item);
    }
    public void onDocTypeDefaultSelected(ValueChangeEvent event){
        DocType docType = (DocType) event.getNewValue();
        getEditedItem().setDocTypeDefault(docType);
    }
    
    /* Возвращает название для заголовка наследования прав к документам  */
    @Override
    public String getInheritsAccessChildName(){
        if (getEditedItem().isInheritsAccessChilds()){
            return EscomBeanUtils.getMessageLabel("RightsInheritedForChildDocs");
        } else {
            return EscomBeanUtils.getMessageLabel("DocumentsHaveSpecRights");
        }
    }
    
    public String getTypeName(){
        return foldersBean.getTypeName();
    }
        
    @Override
    public List<State> getStateForChild(){
        return docBean.getMetadatesObj().getStatesList();
    }
    
    @Override
    public FoldersFacade getItemFacade() {
        return foldersFacade;
    }  
    
    @Override
    public Class<Folder> getItemClass() {
        return Folder.class;
    }

    @Override
    protected BaseTreeBean getTreeBean() {
        return foldersBean;
    }
}