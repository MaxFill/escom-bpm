package com.maxfill.escom.beans.folders;

import com.maxfill.facade.FoldersFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.escom.beans.BaseCardBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.DocFacade;
import com.maxfill.facade.DocTypeFacade;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.rights.Right;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.states.State;
import com.maxfill.utils.Tuple;
import org.primefaces.event.SelectEvent;

import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Папки
 *
 * @author Maxim
 */

@Named
@ViewScoped
public class FoldersCardBean extends BaseCardBean<Folder> {
    private static final long serialVersionUID = 1052362714114861680L;
    private static final int TYPE_RIGHT_FOLDER = 0;
    private static final int TYPE_RIGHT_DOC = 1;
    
    @EJB
    private FoldersFacade foldersFacade;
    @EJB
    private DocTypeFacade docTypeFacade;
    
    private Integer typeEditedRight;

    private List<DocType> docTypes;    
    
    /* ПРАВА ДОСТУПА: обработка при изменении опции "Наследование прав для документов" в карточке папки */
    public void onInheritsDocChange(ValueChangeEvent event) {
        if ((Boolean) event.getNewValue() == false) {    //если галочка снята, то нужно скопировать права документов от родителя           
            makeRightForChilds(getEditedItem());
            setIsItemChange(Boolean.TRUE);
            EscomBeanUtils.SuccesMsgAdd("Successfully", "RightIsParentCopy");
        }
    }
    
    /* ПРАВА ДОСТУПА: удаление права доступа к документам из списка прав папки  */
    public void onDeleteRightDoc(Right right) {
        getEditedItem().getRightForChild().getRights().remove(right);
        setIsItemChange(Boolean.TRUE);
    }    
    
    /* ПРАВА ДОСТУПА: добавление права для документа */
    public void onAddDocRight(State state){
        typeEditedRight = TYPE_RIGHT_DOC;
        super.onAddRight(state);
    }
    
    /* ПРАВА ДОСТУПА: добавление права для папки */
    @Override
    public void onAddRight(State state){
        typeEditedRight = TYPE_RIGHT_FOLDER;
        super.onAddRight(state);
    }
    
    /* ПРАВА ДОСТУПА: редактирование права для документа */
    public void onEditDocRight(Right right){
        typeEditedRight = TYPE_RIGHT_DOC;
        super.onEditRight(right);
    }
    
    /* ПРАВА ДОСТУПА: редактирование права для папки */
    @Override
    public void onEditRight(Right right){
        typeEditedRight = TYPE_RIGHT_FOLDER;
        super.onEditRight(right);
    }
    
    /* ПРАВА ДОСТУПА: событие закрытия карточки прав доступа */
    @Override
    public void onCloseRightCard(SelectEvent event) {
        switch (typeEditedRight){
            case TYPE_RIGHT_FOLDER:{
                super.onCloseRightCard(event);
                break;
            }
            case TYPE_RIGHT_DOC:{
                Tuple<Boolean, Right> tuple = (Tuple)event.getObject();
                Boolean isChange = tuple.a;
                if (isChange) {
                    onItemChange();                    
                    Right right = tuple.b;
                    if (right != null){
                        getEditedItem().getRightForChild().getRights().add(right);
                    }
                }            
                break;
            }
        }
    }        
    
    /* ПРАВА ДОСТУПА: Возвращает для папки список прав к документам в заданном состоянии */
    public List<Right> getRightDocsForState(Folder folder, State state) {
        List<Right> rights = folder.getRightForChild().getRights()
                .stream()
                .filter(right -> state.equals(right.getState()))
                .collect(Collectors.toList());
        return rights;
    }    
    
    /* Обработка события изменения типа документа на форме карточки папки  */
    public void onDocTypeDefaultSelected(SelectEvent event){
        List<DocType> items = (List<DocType>) event.getObject();
        if (items.isEmpty()){return;}
        DocType item = items.get(0);
        onItemChange();
        getEditedItem().setDocTypeDefault(item);
        if (docTypes.contains(item)){
            docTypes.add(item);
        }
    }

    public List<DocType> getDocTypes() {
        if (docTypes == null){
            docTypes = docTypeFacade.findAll().stream()
                    .filter(item -> preloadCheckRightView(item))
                    .collect(Collectors.toList());
        }
        return docTypes;
    }

    @Override
    public FoldersFacade getItemFacade() {
        return foldersFacade;
    }

    @Override
    protected void onBeforeSaveItem(Folder folder) {
        Rights rightDocs = folder.getRightForChild();
        folder.setXmlAccessChild(rightDocs.toString());
    }

    @Override
    protected void afterCreateItem(Folder item) {        
    }
    
    /* Подготовка прав доступа к визуализации */
    @Override
    protected void prepareRightsForView(Folder folder){
        super.prepareRightsForView(folder);
        makeRightForChilds(folder); 
        rightFacade.prepareRightsForView(folder.getRightForChild().getRights());
    }
    
    @Override
    public Class<Folder> getItemClass() {
        return Folder.class;
    }
}