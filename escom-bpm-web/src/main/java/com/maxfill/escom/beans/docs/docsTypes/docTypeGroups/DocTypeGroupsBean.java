package com.maxfill.escom.beans.docs.docsTypes.docTypeGroups;

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroupsFacade;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.docs.docsTypes.DocTypeBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.docs.docsTypes.DocTypeFacade;
import java.util.ArrayList;

import javax.ejb.EJB;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

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
    public DocTypeGroupsFacade getLazyFacade() {
        return itemsFacade;
    }
    
    @Override
    public void preparePasteItem(DocTypeGroups pasteItem, DocTypeGroups sourceItem, BaseDict target){
        super.preparePasteItem(pasteItem, sourceItem, target);
        pasteItem.setParent((DocTypeGroups)target);
        //pasteItem.setDetailItems(sourceItem.getDetailItems()); //копируем только ссылки!
    }
    
      /* Возвращает списки зависимых объектов, необходимых для копирования */
    @Override
    public List<List<?>> doGetDependency(DocTypeGroups group){
        List<List<?>> dependency = new ArrayList<>();
        List<DocTypeGroups> childs = itemsFacade.findActualChilds(group, getCurrentUser()).collect(Collectors.toList());
        if (!childs.isEmpty()) {
            dependency.add(childs);
        }
        List<DocType> details =  docTypeFacade.findActualDetailItems(group, 0, 0, "name", "ASCENDING", getCurrentUser());
        if (!details.isEmpty()) {
            dependency.add(details);
        }
        return dependency;
    }
    
    @Override
    public List<DocTypeGroups> getGroups(DocTypeGroups item) {
        return null;
    }
    
    @Override
    public void doGetCountUsesItem(DocTypeGroups docTypeGroups,  Map<String, Integer> rezult){
        Long count =docTypeFacade.getCountDetails(docTypeGroups);
        rezult.put("DocTypes", count.intValue());
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
}
