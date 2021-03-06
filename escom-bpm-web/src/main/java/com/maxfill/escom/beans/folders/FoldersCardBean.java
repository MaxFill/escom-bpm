package com.maxfill.escom.beans.folders;

import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.folder.FoldersFacade;
import com.maxfill.model.basedict.folder.Folder;
import com.maxfill.escom.beans.BaseCardTree;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.model.basedict.docType.DocType;
import com.maxfill.model.basedict.partner.Partner;
import com.maxfill.model.core.states.State;
import org.primefaces.event.SelectEvent;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import java.util.List;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

/* Контролер формы "Папка" */

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
        if (event.getObject() instanceof String) return;
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
        if (event.getObject() instanceof String) return;
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
            return MsgUtils.getMessageLabel("RightsInheritedForChildDocs");
        } else {
            return MsgUtils.getMessageLabel("DocumentsHaveSpecRights");
        }
    }

    /**
     * Формирует заголовок для карточки папки
     * @return
     */
    @Override
    public String makeCardHeader() {
        StringBuilder sb = new StringBuilder();
        if (getEditedItem().isCase()){
            sb.append(MsgUtils.getBandleLabel("Case"));
        } else {
            sb.append(MsgUtils.getBandleLabel("Folder"));
        }
        return makeHeader(sb);
    }

    public String getTypeName(){
        return foldersBean.getTypeName(getEditedItem());
    }

    /**
     * Возвращает список состояний для подчинённых объектов
     * @return
     */
    @Override
    public List<State> getStateForChild(){
        return docBean.getMetadatesObj().getStatesList();
    }
    
    @Override
    public FoldersFacade getFacade() {
        return foldersFacade;
    }

    @Override
    protected BaseTreeBean getTreeBean() {
        return foldersBean;
    }

}