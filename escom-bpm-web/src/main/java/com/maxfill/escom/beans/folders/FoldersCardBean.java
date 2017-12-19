package com.maxfill.escom.beans.folders;

import com.maxfill.facade.FoldersFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.escom.beans.BaseCardTree;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.partners.Partner;
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
    
    /* Обработка события изменения поля "Вид документа для новых документов"  */
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
        onItemChange();        
    }
    
    /* Обработка события изменения поля "Контрагент для новых документов"   */
    public void onPartnerSelected(SelectEvent event){
        List<Partner> items = (List<Partner>) event.getObject();
        if (items.isEmpty()){return;}
        Partner item = items.get(0);
        onItemChange();
        getEditedItem().setPartnerDefault(item);
    }
    public void onPartnerSelected(ValueChangeEvent event){
        Partner partner = (Partner) event.getNewValue();
        getEditedItem().setPartnerDefault(partner);
        onItemChange();
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
        return foldersBean.getTypeName(getEditedItem());
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
    protected BaseTreeBean getTreeBean() {
        return foldersBean;
    }
}