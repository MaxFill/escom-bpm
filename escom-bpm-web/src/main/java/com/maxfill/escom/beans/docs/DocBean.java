package com.maxfill.escom.beans.docs;

import com.maxfill.facade.DocFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.escom.beans.folders.FoldersBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.rights.Rights;
import javax.ejb.EJB;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

/* Сервисный бин "Документы" */

@Named(value = "docsBean")
@SessionScoped
public class DocBean extends BaseExplBeanGroups<Doc, Folder>{
    private static final long serialVersionUID = 923378036800543406L;         
    
    @Inject
    private FoldersBean ownerBean;
    
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
        if (item.getOwner() != null) {
            Rights childRight = ownerBean.getRightForChild(item.getOwner()); //получаем права из спец.прав 
            if (childRight != null){
                return childRight;
            }
        }
        return getDefaultRights(item);  
    }
    
    /* Подготовка к просмотру документа */
    @Override
    public Doc prepViewItem(Doc doc){
        if (doc.getAttache() != null){
            onViewAttache(doc.getAttache());
        }
        return doc;
    }
    
    /* Установка специфичных атрибутов документа при его создании */
    @Override
    public void setSpecAtrForNewItem(Doc doc, Map<String, Object> params){
        Folder folder = doc.getOwner();
        if (folder != null){
            DocType docType = folder.getDocTypeDefault();
            doc.setDocType(docType);
        }
        doc.setDateDoc(new Date());
        if (doc.getOwner().getId() == null){ //сброс owner если документ создаётся в корне архиа!
            doc.setOwner(null);
        }
        if (params != null && !params.isEmpty()){
            Attaches attache = (Attaches)params.get("attache");
            if (attache != null){
                Integer version = doc.getNextVersionNumber();            
                attache.setNumber(version);
                attache.setDoc(doc);
                String fileName = attache.getName();
                doc.setName(fileName);
                doc.getAttachesList().add(attache);
            }
        }
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
    public BaseExplBean getOwnerBean() {
        return ownerBean;
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