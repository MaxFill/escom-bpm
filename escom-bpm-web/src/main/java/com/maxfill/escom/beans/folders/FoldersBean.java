package com.maxfill.escom.beans.folders;

import com.maxfill.dictionary.DictExplForm;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.folders.FoldersFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.model.docs.DocFacade;
import com.maxfill.model.BaseDict;
import java.text.MessageFormat;
import javax.ejb.EJB;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

/* Сервисный бин "Папки документов" */

@Named(value = "foldersBean")
@SessionScoped
public class FoldersBean extends BaseTreeBean<Folder, Folder> {
    private static final long serialVersionUID = 2678662239530806110L;
    
    @Inject
    private DocBean docBean;
    
    @EJB
    private FoldersFacade foldersFacade;
    @EJB
    private DocFacade docFacade;
    
    /* Формирование содержимого контента папки   */ 

    @Override
    public List<BaseDict> makeGroupContent(BaseDict folder, BaseTableBean tableBean, Integer viewMode, int first, int pageSize, String sortField, String sortOrder) {
        if (Objects.equals(viewMode, DictExplForm.SELECTOR_MODE) && tableBean == this){
            return getFacade().findActualChilds((Folder) folder, getCurrentUser()).collect(Collectors.toList());
        } else {
            return getDetailBean().getFacade().findActualDetailItems(folder, first, pageSize, sortField, sortOrder, getCurrentUser());
        }
    }

    /* Действия перед удалением папки  */
    @Override
    protected void preDeleteItem(Folder folder) {       
        folder.getDetailItems().clear();
        docFacade.deleteDocFromFolder(folder);        
    }

    /* Формирует число ссылок на folder в связанных объектах  */
    @Override
    public void doGetCountUsesItem(Folder folder,  Map<String, Integer> rezult){
        rezult.put("Documents", folder.getDetailItems().size());
        rezult.put("Folders", folder.getChildItems().size());
    }

    /**
     * Проверка возможности удаления Папки
     * Папку можно удалить всегда (при наличии прав) не зависимо от наличия дочерних папок и документов
     * @param folder
     * @param errors
     */
    @Override
    protected void checkAllowedDeleteItem(Folder folder, Set<String> errors){
        super.checkAllowedDeleteItem(folder, errors);
        if (!userFacade.findUsersByInbox(folder).isEmpty()){
            Object[] messageParameters = new Object[]{folder.getName()};
            String error = MessageFormat.format(MsgUtils.getMessageLabel("FolderUsedInUsers"), messageParameters);
            errors.add(error);
        }
    }

    /**
     * Возвращает списки зависимых объектов, необходимых для копирования папки
     * @param folder
     * @return
     */
    @Override
    public List<List<?>> doGetDependency(Folder folder){
        List<List<?>> dependency = new ArrayList<>();
        List detail = docFacade.findActualDetailItems(folder, 0, 0, "name", "ASCENDING", getCurrentUser());
        if (!detail.isEmpty()) {
            dependency.add(detail);
        }
        List<Folder> childs = foldersFacade.findActualChilds(folder, getCurrentUser()).collect(Collectors.toList());
        if (!childs.isEmpty()) {
            dependency.add(childs);
        }
        return dependency;
    }
    
    @Override
    public void preparePasteItem(Folder pasteItem, Folder sourceItem, BaseDict target){
        super.preparePasteItem(pasteItem, sourceItem, target);
        if (target == null){
            target = sourceItem.getParent();
        }
        pasteItem.setParent((Folder)target);
    } 
    
    public String getTypeName(Folder folder){
        return MsgUtils.getBandleLabel("FolderType");
    }        
    
    /* GETS & SETS */

    @Override
    public FoldersFacade getFacade() {
        return foldersFacade;
    }
    
    @Override
    public List<Folder> getGroups(Folder item) {
        return null;
    }

    @Override
    public Class<Folder> getOwnerClass() {
        return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return null;
    }

    @Override
    public BaseDetailsBean getDetailBean() {
        return docBean;
    }

}