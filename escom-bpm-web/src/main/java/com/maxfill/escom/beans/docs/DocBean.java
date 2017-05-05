package com.maxfill.escom.beans.docs;

import com.maxfill.facade.DocFacade;
import com.maxfill.model.docs.DocModel;
import com.maxfill.model.docs.Doc;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.folders.Folders;
import com.maxfill.escom.utils.FileUtils;
import com.maxfill.utils.ItemUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  Бизнес-логика для работы с документами
 * @author mfilatov
 */

@Named(value = "docsBean")
@ViewScoped
public class DocBean extends BaseExplBeanGroups<Doc, Folders>{
    private static final long serialVersionUID = 923378036800543406L;    
    private static final String BEAN_NAME = "docsBean";      
    
    @EJB
    private DocFacade docsFacade;  
    
    public DocBean() {
    }                          
     
    public void onUploadFileAndCreateDoc(FileUploadEvent event) throws IOException{ 
        if (isHaveRightCreate()){
            onUploadFile(event);
        } else {
            String objName = EscomBeanUtils.getBandleLabel(getItemFacade().getMetadatesObj().getBundleName());
            EscomBeanUtils.ErrorFormatMessage("AccessDenied", "RightCreateNo", new Object[]{objName});
        }
    }  
    
    /**
     * Загрузка файла документа
     * @param event
     * @throws java.io.IOException
     */
    public void onUploadFile(FileUploadEvent event) throws IOException{        
        UploadedFile uploadedFile = FileUtils.handleUploadFile(event);
        Attaches attache = FileUtils.doUploadAtache(uploadedFile, currentUser, conf.getUploadPath());
        explorerBean.getCreateParams().put("attache", attache);
    }
    
    /**
     * Формирование списка документов из отмеченных папок и документов
     */
    private List<BaseDict> prepareCheckedItems(){
        List<BaseDict> sourceItems = explorerBean.getCheckedItems(); 
        List<BaseDict> targetItems = new ArrayList<>();
        for (BaseDict content : sourceItems){
            if (content.getClass().getSimpleName().equals("Folders")){
                Folders folder = (Folders) content;
                folder.getDocsList().stream().forEach(doc -> targetItems.add(doc));
            } else {
                targetItems.add(content);
            }
        }
        return targetItems;
    }

    /**
     * Возвращает список папок, в которые входит документ
     * Примечание: документ может быть только в одной папке
     * @param item
     * @return 
     */
    @Override
    public List<Folders> getGroups(Doc item) {
        List<Folders> groups = new ArrayList<>();
        groups.add(item.getOwner());
        return groups;
    }  
    
    /* *** ВЛОЖЕНИЯ *** */    
    
    /**
     * Подготовка вложений документов для отправки на e-mail
     * @param mode 
     */
    public void prepareSendMailDocs(String mode){
        List<BaseDict> checked = prepareCheckedItems();
        if (!checked.isEmpty()){
            EscomBeanUtils.openMailMsgForm(mode, checked);
        } else {
            EscomBeanUtils.WarnMsgAdd("Error", "NO_SELECT_DOCS");
        }
    }    

    /* *** КОРЗИНА *** */ 
    
    /**
     * Удаление всех документов из папки
     * @param folder 
     */
    @Override
    protected void clearOwner(Folders folder){
        getItemFacade().deleteDocFromFolder(folder); //удаляем документы в папке из базы
        super.clearOwner(folder);
    }
             
    /* *** СПЕЦИФИЧНЫЕ Ф-ЦИИ РАБОТЫ С ДОКУМЕНТАМИ *** */
    
    /**
     * Показать документ в папке, вызов с экранной формы
     * Возвращает url по которому автоматически происходит переход на новую страницу
     * @param doc
     * @return URL страницы обозревателя документов
     */
    public String onShowDocInFolder(BaseDict doc){
        return EscomBeanUtils.doGetItemURL(doc, "folders/folder-explorer", "0");
    }
    
    /**
     * Показать документ в папке
     */
    public void doShowDocInFolder(){         
        if (getModel().getSelectedDocId() != null){
            Doc doc = getItemFacade().find(getModel().getSelectedDocId());
            if (doc != null){
                Folders owner = (Folders) doc.getOwner();
                TreeNode node = null;
                if (owner != null){
                    node = EscomBeanUtils.findTreeNode(explorerBean.getTree(), owner);
                }
                if (getOwnerBean().getSelectedNode() != null) {
                    getOwnerBean().getSelectedNode().setSelected(false);
                }
                getOwnerBean().setSelectedNode(node);
                getModel().setSelectedDocId(null);
                RequestContext.getCurrentInstance().execute("PF('accordion').select(0);");
            }
        }
    }
    
    /* *** СИСТЕМНЫЕ МЕТОДЫ *** */

    @Override
    public DocFacade getItemFacade() {
        return docsFacade;
    }
    @Override
    protected DocModel createModel() {
        return new DocModel();
    }

    @Override
    public BaseExplBean getDetailBean() {
        return null;
    }
    @Override
    public DocModel getModel(){
        return (DocModel) super.getModel();
    }
    @Override
    protected String getBeanName() {
        return BEAN_NAME;
    }    

    @Override
    public Class<Doc> getItemClass() {
        return Doc.class;
    }

    @Override
    public Class<Folders> getOwnerClass() {
        return Folders.class;
    }
  
}