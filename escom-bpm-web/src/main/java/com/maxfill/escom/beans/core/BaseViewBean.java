package com.maxfill.escom.beans.core;

import com.maxfill.Configuration;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.ApplicationBean;
import com.maxfill.escom.beans.SessionBean;
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
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
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
        
    @PostConstruct
    protected void init(){
        initBean();
        System.out.println("Create  " + this.getClass().getSimpleName());
    }

    @PreDestroy
    protected void destroy(){
        System.out.println("Destroy " + this.getClass().getSimpleName());
    }

    protected void initBean(){};

    @Override
    public SessionBean getSessionBean() {
        return sessionBean;
    }
    public Locale getLocale(){
        return sessionBean.getLocale();
    }
    
    /**
     * Метод вызывается автоматически при открытии формы
     */
    public void onBeforeOpenCard(){
        beanId = this.toString();                
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();
        
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Map map = (Map) session.getAttribute(ViewScopeManager.ACTIVE_VIEW_MAPS);
        
        //Map<String, Object> viewMap = facesContext.getViewRoot().getViewMap();        
        //String viewId = Faces.getViewRoot().getViewId();
        //ResponseStateManager manager = getRenderKit(facesContext).getResponseStateManager();
        //Object state = manager.getState(facesContext, viewId);
        
        if (sourceBean == null && params.containsKey(SysParams.PARAM_BEAN_ID) && StringUtils.isNotEmpty(params.get(SysParams.PARAM_BEAN_ID))){            
            sourceBeanId = params.get(SysParams.PARAM_BEAN_ID);
            sourceBean = (T)sessionBean.getOpenedBeans().get(sourceBeanId);
        } else {            
            //sessionBean.getKillBeans().add(viewId);
            //viewMap.entrySet().stream().forEach(rec-> sessionBean.getKillBeans().add((String)rec.getValue()));            
        }
        doBeforeOpenCard(params);
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
        sessionBean.getOpenedBeans().remove(beanId);
        PrimeFaces.current().dialog().closeDynamic(exitParam);
        return "";
    }
    
    /* НАСТРОЙКИ ОТРИСОВКИ ФОРМЫ */

    public abstract String getFormName();
    
    public Boolean isWestShow(){
        return false;
    }
    public Boolean isEastShow(){
        return false;
    }    
    public Boolean isSouthShow(){
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
        if (beanId == null){
            beanId = this.toString();
        }        
        paramsMap.put(SysParams.PARAM_BEAN_ID, Collections.singletonList(beanId));
        sessionBean.getOpenedBeans().put(beanId, this);
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
    
    public boolean isFullPageMode(){
        return sourceBeanId == null;
    }
}