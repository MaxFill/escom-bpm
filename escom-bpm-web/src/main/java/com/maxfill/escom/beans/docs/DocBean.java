package com.maxfill.escom.beans.docs;

import com.maxfill.facade.DocFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.rights.Rights;
import javax.ejb.EJB;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;

/* Сервисный бин "Документы" */

@Named(value = "docsBean")
@SessionScoped
public class DocBean extends BaseExplBeanGroups<Doc, Folder>{
    private static final long serialVersionUID = 923378036800543406L;         
    
    @EJB
    private DocFacade docsFacade;    
    
    @Override
    public void preparePasteItem(Doc pasteItem, BaseDict target){        
        pasteItem.setOwner((Folder)target);
    }

    @Override
    public Rights getRightItem(BaseDict item) {
        if (item == null) return null;
        
        if (!item.isInherits()) {
            return getActualRightItem(item);
        } 
        return getRightForChild(item.getOwner());  
    }
    
    @Override
    public SearcheModel initSearcheModel() {
        return new DocsSearche();
    }    
    
    @Override
    public List<Folder> getGroups(Doc item) {
        List<Folder> groups = new ArrayList<>();
        groups.add(item.getOwner());
        return groups;
    }                                     

    @Override
    public DocFacade getItemFacade() {
        return docsFacade;
    }
    
    @Override
    public BaseExplBean getDetailBean() {
        return null;
    }

    @Override
    public Class<Doc> getItemClass() {
        return Doc.class;
    }

    @Override
    public Class<Folder> getOwnerClass() {
        return Folder.class;
    }
 
}