package com.maxfill.escom.beans;

import com.maxfill.Configuration;
import com.maxfill.RightsDef;
import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.system.rights.RightsBean;
import com.maxfill.escom.utils.EscomFileUtils;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.services.attaches.AttacheService;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.services.numerators.NumeratorService;
import com.maxfill.services.print.PrintService;
import com.maxfill.facade.RightFacade;
import com.maxfill.model.users.User;
import com.maxfill.services.favorites.FavoriteService;
import com.maxfill.facade.UserFacade;
import com.maxfill.facade.UserGroupsFacade;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

/* Базовый бин */
public abstract class BaseBean <T extends BaseDict> implements Serializable{
    static final private long serialVersionUID = 3914263308813029722L;
    static final protected Logger LOGGER = Logger.getGlobal();

    @Inject
    protected ApplicationBean appBean;
    @Inject
    protected SessionBean sessionBean;
    @Inject
    protected RightsBean rightsBean; //используется в CardBean!
    @EJB
    protected Configuration conf;
    @EJB
    protected RightsDef rightsDef;
    @EJB
    protected PrintService printService;
    @EJB
    protected FavoriteService favoriteService;

    @EJB
    protected NumeratorService numeratorService;
    @EJB
    protected AttacheService attacheService;
    @EJB
    protected RightFacade rightFacade;
    @EJB
    protected UserFacade userFacade;
    @EJB
    protected UserGroupsFacade userGroupsFacade;
    
    private boolean isItemChange;               //признак изменения записи  

    protected User currentUser;
    private Metadates metadatesObj;             //объект метаданных
    
    public abstract BaseDictFacade getItemFacade(); //установка фасада объекта    

    @PostConstruct
    public void init() {
        //System.out.println("Создан бин =" + this.toString());
        setCurrentUser(sessionBean.getCurrentUser());
        onInitBean();
    }
    
    @PreDestroy
    private void destroy(){
        //System.out.println("Удалён бин =" + this.toString());
    }
    
    public abstract void onInitBean();
    
    /* СОЗДАНИЕ: создание нового объекта   */
    public T createItem(BaseDict owner) {
        return createItem(owner, currentUser, new HashMap<>());
    }
    
    /* СОЗДАНИЕ: cоздание объекта */
    public T createItem(BaseDict owner, User author, Map<String, Object> params) {
        return (T) getItemFacade().createItem(author, owner, params);        
    }
    
    /* *** *** *** */
    
    /* СПИСКИ ОБЪЕКТОВ: предварительная обработка списка объектов  */
    public List<BaseDict> prepareItems(List<T> sourceItems) {
        List<BaseDict> target = sourceItems.stream()
                .filter(item -> getItemFacade().preloadCheckRightView(item, currentUser))
                .collect(Collectors.toList());
        return target;
    }    
    
    /* ИЗБРАННОЕ: добавление объекта в избранное  */
    public void addInFavorites(BaseDict item){
        Object[] params = new Object[]{item.getName()};
        if (favoriteService.addInFavorites(item, metadatesObj, currentUser)){
            EscomMsgUtils.succesFormatMsg("ObjectAddedToFavorites", params);
        } else {
            EscomMsgUtils.warnFormatMsg("ObjectAlreadyAddedFavorites", params);
        }
    }    

    /* ВЛОЖЕНИЯ */
    
    /* Просмотр вложения  */
    public void onViewAttache(Attaches attache) {
        String path = conf.getUploadPath() + attache.getFullName();
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> pathList = new ArrayList<>();
        pathList.add(FilenameUtils.removeExtension(path) + ".pdf");
        paramMap.put("path", pathList);
        sessionBean.openDialogFrm(DictDlgFrmName.FRM_DOC_VIEWER, paramMap);
    }    
    
    public void onViewReport(String reportName){
        String pdfFile = new StringBuilder()
                    .append(conf.getTempFolder())
                    .append(reportName)
                    .append("_")
                    .append(currentUser.getLogin())
                    .append(".pdf").toString();
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> pathList = new ArrayList<>();
        pathList.add(pdfFile);
        paramMap.put("path", pathList);
        sessionBean.openDialogFrm(DictDlgFrmName.FRM_DOC_VIEWER, paramMap);
    }
    
    /* Скачивание вложения  */
    public void attacheDownLoad(Attaches attache){
        if (attache == null) return;
        String path = conf.getUploadPath() + attache.getFullName(); 
        EscomFileUtils.attacheDownLoad(path, attache.getName());
    }
        
    /* СЛУЖЕБНЫЕ МЕТОДЫ */    
    
    /* Установка признака изменения объекта  */
    public void onItemChange() {
        isItemChange = true;
    }    
    
    /* Признак изменения объекта  */
    public boolean isItemChange() {
        return isItemChange;
    }

    /* Получение ссылки на объект метаданных  */
    public Metadates getMetadatesObj() {
        if (metadatesObj == null) {
            metadatesObj = getItemFacade().getMetadatesObj();
        }
        return metadatesObj;
    }

    /* GET & SET */
                
    public Boolean getIsItemChange() {
        return isItemChange;
    }
    public void setIsItemChange(Boolean isItemChange) {
        this.isItemChange = isItemChange;
    }  
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    public User getCurrentUser() {
        return currentUser;
    }

}