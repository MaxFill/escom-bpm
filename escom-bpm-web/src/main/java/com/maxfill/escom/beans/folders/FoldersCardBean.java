package com.maxfill.escom.beans.folders;

import com.maxfill.facade.FoldersFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.escom.beans.BaseCardTree;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.rights.Rights;
import org.primefaces.event.SelectEvent;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;
import javax.inject.Inject;

/* Карточка Папки */

@Named
@ViewScoped
public class FoldersCardBean extends BaseCardTree<Folder> {
    private static final long serialVersionUID = 1052362714114861680L;  
    
    @Inject
    private FoldersBean foldersBean;
    
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

    @Override
    public FoldersFacade getItemFacade() {
        return foldersFacade;
    }

    @Override
    protected void afterCreateItem(Folder item) {        
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