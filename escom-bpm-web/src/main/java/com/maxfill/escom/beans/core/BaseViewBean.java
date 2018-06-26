package com.maxfill.escom.beans.core;

import com.maxfill.Configuration;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.ApplicationBean;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.StaffFacade;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.users.User;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.codehaus.plexus.util.StringUtils;

import org.primefaces.extensions.component.layout.LayoutPane;
import org.primefaces.extensions.model.layout.LayoutOptions;

/**
 * Базовый бин для работы с формами и диалогами
 * Реализует методы обработки событий открытия и закрытия диалогов,
 * сохранения изменений размеров форм
 * @param <T>
 */
public abstract class BaseViewBean<T extends BaseView> implements Serializable{
    protected static final Logger LOGGER = Logger.getLogger(BaseViewBean.class.getName());
    private static final long serialVersionUID = -255630654257638984L;

    @Inject
    protected ApplicationBean appBean;
    @Inject
    protected SessionBean sessionBean;
    @EJB
    protected Configuration conf;
    @EJB
    private StaffFacade staffFacade;
    
    private Integer centerHight = 0;
    private Integer northHight = 0;
    private Integer southHight = 0;

    private Integer westWidth = 0;
    private Integer centerWidth = 0;
    private Integer eastWidth = 0;
    
    protected String beanId; //Faces id этого бина (актуально для ViewScopeBean) автоматически записывается в это поле из формы карточки
    
    protected T sourceBean;  //Ссылка на бин источник, из которого был открыт этот бин (актуально для ViewScopeBean).
    
    protected final LayoutOptions layoutOptions = new LayoutOptions();

    /**
     * Возвращает имя этого бина. (Использется для передачи имени бина в качестве параметра в дочерний бин)
     * @return - String имя бина
     */
    public String getBeanName(){
        return this.getClass().getSimpleName().substring(0, 1).toLowerCase() + this.getClass().getSimpleName().substring(1);        
    }
    
    @PostConstruct
    protected void init(){
        initLayotOptions();
        initBean();
    }

    @PreDestroy
    protected void destroy(){
        //System.out.println("Bean [" + this.getClass().getSimpleName() + "] destroy!");
    }

    protected void initBean(){};

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    /**
     * Метод вызывается автоматически при открытии формы диалога
     */
    public void onBeforeOpenCard(){        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();        
        if (sourceBean == null && params.containsKey(SysParams.PARAM_BEAN_ID)){ 
            String sourceBeanId = params.get(SysParams.PARAM_BEAN_ID);
            String beanName = params.get(SysParams.PARAM_BEAN_NAME);
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
            Map map = (Map) session.getAttribute("com.sun.faces.application.view.activeViewMaps");          
            for (Object entry : map.values()) {
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
        doBeforeOpenCard(params);
    }

    public void doBeforeOpenCard(Map<String, String> params){};
    
    /**
     * Метод вызывается автоматически после загрузки формы диалога
     * @param beanId
     */
    public void onAfterFormLoad(String beanId){
        this.beanId = beanId;
    }

    /**
     * Обработка события закрытия формы
     * @return
     */
    public String onCloseCard(){
        return onFinalCloseCard(null);
    }

    public String onCloseCard(String param){
        return onFinalCloseCard(param);
    }

    private String onFinalCloseCard(Object param){
        return sessionBean.closeDialog(param);
    }

    /* Обработка cобытия изменения размеров формы */
    public void handleResize(org.primefaces.extensions.event.ResizeEvent event) { 
        LayoutPane o = (LayoutPane)event.getSource();
        Double width = event.getWidth();
        Double height = event.getHeight();
        switch(o.getId()){
            case "center":{
                centerHight = northHight + southHight + height.intValue();
                centerWidth = westWidth + eastWidth + width.intValue();
                westWidth = 0;
                eastWidth = 0;
                northHight = 0;
                southHight = 0;
                break;
            }
            case "north":{
                northHight = height.intValue();
                break;
            }
            case "south":{
                southHight = height.intValue();
                break;
            }
            case "west":{
                westWidth = width.intValue();
                break;
            }
            case "east":{
                eastWidth = width.intValue();
                break;
            }
        }
        Integer heightSum = centerHight + southHight + northHight;
        Integer widthSum = centerWidth + westWidth + eastWidth;
        sessionBean.saveFormSize(getFormName(), widthSum + 25, heightSum + 25);
    }

    public abstract String getFormName();
    
    protected void initLayotOptions(){        
        LayoutOptions panes = new LayoutOptions();
        panes.addOption("slidable", false);
        panes.addOption("resizable", true);
        layoutOptions.setPanesOptions(panes);

        if (isSouthShow()) {
            LayoutOptions south = new LayoutOptions();
            south.addOption("resizable", false);
            south.addOption("closable", false);
            south.addOption("size", 45);
            south.addOption("initClosed", isSouthInitClosed());
            layoutOptions.setSouthOptions(south);
        }

        if (isNorthShow()) {
            LayoutOptions north = new LayoutOptions();
            north.addOption("resizable", false);
            north.addOption("closable", false);
            north.addOption("size", 45);
            north.addOption("initClosed", isNorthInitClosed());
            layoutOptions.setNorthOptions(north);
        }

        initWestLayout(layoutOptions);                

        if (isEastShow()) {
            LayoutOptions east = new LayoutOptions();;
            east.addOption("size", "20%");
            east.addOption("minSize", 150);
            east.addOption("maxSize", 450);
            east.addOption("resizable", true);
            east.addOption("initClosed", isEastInitClosed());
            layoutOptions.setEastOptions(east);
        }
        
        LayoutOptions center = new LayoutOptions();        
        center.addOption("resizable", true);
        center.addOption("closable", false);
        center.addOption("minWidth", 200);
        center.addOption("minHeight", 100);
        layoutOptions.setCenterOptions(center);
    }

    protected void initWestLayout(LayoutOptions layoutOptions){
        if (isWestShow()) {
            LayoutOptions west = new LayoutOptions();
            west.addOption("size", "20%");
            west.addOption("resizable", true);            
            west.addOption("initClosed", isWestInitClosed());
            layoutOptions.setWestOptions(west);
        }
    }
    
    public Boolean isSouthShow(){
        return false;
    }
    public Boolean isWestShow(){
        return false;
    }
    public Boolean isNorthShow(){
        return false;
    }
    public Boolean isEastShow(){
        return false;
    }
    
    protected boolean isSouthInitClosed(){
        return false;
    }
    protected boolean isEastInitClosed(){
        return false;
    }
    protected boolean isWestInitClosed(){
        return true;
    }
    protected boolean isNorthInitClosed(){
        return false;
    }
    
    public User getCurrentUser(){
        return sessionBean.getCurrentUser();
    }    
    
    public Staff getCurrentStaff(){
        return staffFacade.findStaffByUser(getCurrentUser());
    }
    
    /* *** ПРОЧИЕ *** */
    
    public String getLabelFromBundle(String key){
        if (StringUtils.isEmpty(key)) return "";
        
        return EscomMsgUtils.getBandleLabel(key);        
    }
    
    public String getFormatLabelFromBundle(String key, String param){
        if (StringUtils.isEmpty(key)) return "";
        Object[] params = new Object[]{param};        
        return MessageFormat.format(EscomMsgUtils.getBandleLabel(key), params);                
    }
    
    /**
     * Формирует набор параметров для передачи его в открываемый бин
     * @return 
     */
    public Map<String, List<String>> getParamsMap(){
        Map<String, List<String>> paramsMap = new HashMap<>();
        List<String> itemIds = new ArrayList<>();
        itemIds.add(beanId);
        paramsMap.put(SysParams.PARAM_BEAN_ID, itemIds);
        List<String> beanNameList = new ArrayList<>();
        beanNameList.add(getBeanName());
        paramsMap.put(SysParams.PARAM_BEAN_NAME, beanNameList);
        return paramsMap;
    }
    
    /* GETS & SETS */

    public LayoutOptions getLayoutOptions() {
        return layoutOptions;
    }    
   
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
    
}
