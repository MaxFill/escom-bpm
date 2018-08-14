package com.maxfill.escom.beans.core;

import com.maxfill.Configuration;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.ApplicationBean;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.staffs.StaffFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.users.User;
import com.sun.faces.application.view.ViewScopeManager;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.codehaus.plexus.util.StringUtils;
import org.primefaces.PrimeFaces;

/**
 * Базовый бин для работы с формами и диалогами
 * Реализует методы обработки событий открытия и закрытия диалогов,
 * сохранения изменений размеров форм
 * @param <T>
 */
public abstract class BaseViewBean<T extends BaseView> implements Serializable, BaseView{
    protected static final Logger LOGGER = Logger.getLogger(BaseViewBean.class.getName());
    private static final long serialVersionUID = -255630654257638984L;

    @Inject
    protected ApplicationBean appBean;
    @Inject
    protected SessionBean sessionBean;
    @EJB
    protected Configuration conf;
    @EJB
    protected StaffFacade staffFacade;
    
    private BaseDict sourceItem;
    private int tabActiveIndex = 0;
    protected boolean isItemChange;               //признак изменения записи
        
    protected String beanId; //Faces id этого бина (актуально для ViewScopeBean) автоматически записывается в это поле из формы карточки   
    protected T sourceBean;  //Ссылка на бин источник, из которого был открыт этот бин (актуально для ViewScopeBean).    
    protected String sourceBeanId;
    
    /**
     * Возвращает имя этого бина. (Использется для передачи имени бина в качестве параметра в дочерний бин)
     * @return - String имя бина
     */
    @Override
    public String getBeanName(){
        return this.getClass().getSimpleName().substring(0, 1).toLowerCase() + this.getClass().getSimpleName().substring(1);        
    }
    
    @PostConstruct
    protected void init(){
        initBean();
    }

    @PreDestroy
    protected void destroy(){
        System.out.println("Bean [" + this.getClass().getSimpleName() + "] destroy!");
    }

    protected void initBean(){};

    @Override
    public SessionBean getSessionBean() {
        return sessionBean;
    }

    /**
     * Метод вызывается автоматически при открытии формы
     */
    public void onBeforeOpenCard(){
        beanId = this.toString();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();
        if (params.size() <= 1){
            sessionBean.killBean(getBeanName(), beanId);            
        } else  {            
            if (sourceBean == null && params.containsKey(SysParams.PARAM_BEAN_ID) && StringUtils.isNotEmpty(params.get(SysParams.PARAM_BEAN_ID))){                
                sourceBeanId = params.get(SysParams.PARAM_BEAN_ID);
                String beanName = params.get(SysParams.PARAM_BEAN_NAME);
                if (StringUtils.isNotEmpty(sourceBeanId) && StringUtils.isNotEmpty(beanName)){                 
                    HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                    Map map = (Map) session.getAttribute(ViewScopeManager.ACTIVE_VIEW_MAPS);          
                    for (Object entry : map.values()) { //поиск view бина
                      if (entry instanceof Map) {
                        Map viewScopes = (Map) entry;
                        if (viewScopes.containsKey(beanName)) {
                            setSourceBean((T) viewScopes.get(beanName));
                            String id = sourceBean.toString();
                            if (sourceBeanId.equals(id)) break;
                        }
                      }
                    }
                }
                if (sourceBean == null && StringUtils.isNotEmpty(beanName)){ //поиск session бина
                    BaseTableBean bean = EscomBeanUtils.findBean(beanName, facesContext);
                    setSourceBean((T) bean);
                }
            }
            doBeforeOpenCard(params);
        }
    }

    @Override
    public void doBeforeOpenCard(Map<String, String> params){};
    
    /**
     * Метод вызывается автоматически после загрузки формы диалога
     */
    public void onAfterFormLoad(){        
    }

    public String onCancelItemSave(){
        return onCloseCard();
    }
    
    /**
     * Обработка события закрытия формы
     * @return
     */
    public String onCloseCard(){
        return onCloseCard(SysParams.EXIT_NOTHING_TODO);
    }

    public String onCloseCard(Object result){        
        return finalCloseDlg(result);
    }

    /**
     * Завершающая стадия закрытия диалога с удалением view bean из viewMap 
     * @param exitParam
     * @return 
     */
    protected String finalCloseDlg(Object exitParam){        
        sessionBean.killBean(getBeanName(), beanId);
        PrimeFaces.current().dialog().closeDynamic(exitParam);
        return "";
    }        

    public void onFormSize(){
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();        
        if (params.containsKey("width") && params.containsKey("height")){
            Integer width = Integer.valueOf(params.get("width"));
            Integer height = Integer.valueOf(params.get("height"));
            sessionBean.saveFormSize(getFormName(), width, height);
        }
    }
    
    /* НАСТРОЙКИ ОТРИСОВКИ ФОРМЫ */

    public abstract String getFormName();
    
    public Boolean isWestShow(){
        return false;
    }
    public Boolean isEastShow(){
        return false;
    }    
    
    public abstract String getFormHeader();
    
    /* ПРОЧИЕ */
    
    public User getCurrentUser(){
        return sessionBean.getCurrentUser();
    }    
    
    public Staff getCurrentStaff(){
        return staffFacade.findStaffByUser(getCurrentUser());
    }
    
    public String getLabelFromBundle(String key){
        if (StringUtils.isEmpty(key)) return "";
        
        return MsgUtils.getBandleLabel(key);        
    }
    
    public String getFormatLabelFromBundle(String key, String param){
        if (StringUtils.isEmpty(key)) return "";
        Object[] params = new Object[]{param};        
        return MessageFormat.format(MsgUtils.getBandleLabel(key), params);                
    }
    
    /**
     * Формирует набор параметров для передачи его в открываемый бин
     * @return 
     */
    public Map<String, List<String>> getParamsMap(){
        Map<String, List<String>> paramsMap = new HashMap<>();
        List<String> itemIds = Collections.singletonList(beanId == null ? this.toString() : beanId);        
        List<String> beanNameList = Collections.singletonList(getBeanName());
        paramsMap.put(SysParams.PARAM_BEAN_ID, itemIds);
        paramsMap.put(SysParams.PARAM_BEAN_NAME, beanNameList);
        return paramsMap;
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
    
    /* GETS & SETS */
   
    public String getBeanId() {
        return beanId;
    }
    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }

    public T getSourceBean() {
        return sourceBean;
    }
    public void setSourceBean(T sourceBean) {
        this.sourceBean = sourceBean;
    }

    public int getTabActiveIndex() {
        return tabActiveIndex;
    }
    public void setTabActiveIndex(int tabActiveIndex) {
        this.tabActiveIndex = tabActiveIndex;
    }
    
    @Override
    public BaseDict getSourceItem() {
        return sourceItem;
    }
    public void setSourceItem(BaseDict sourceItem) {
        this.sourceItem = sourceItem;
    }

    public String getSourceBeanId() {
        return sourceBeanId;
    }      
}