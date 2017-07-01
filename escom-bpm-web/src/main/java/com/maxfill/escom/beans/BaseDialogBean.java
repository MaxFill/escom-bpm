package com.maxfill.escom.beans;

import com.maxfill.Configuration;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.users.User;
import org.primefaces.context.RequestContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import org.primefaces.extensions.model.layout.LayoutOptions;

/* Базовый бин для служебных диалогов  */
public abstract class BaseDialogBean implements Serializable{    
    private static final long serialVersionUID = 3712139345846276454L;
    
    @Inject
    protected SessionBean sessionBean;
    
    @EJB
    protected Configuration conf;
            
    private BaseBean sourceBean;
    private boolean itemChange;       //признак изменения записи 
    private String beanName;
    
    protected final LayoutOptions layoutOptions = new LayoutOptions();

    @PostConstruct
    protected void init(){
        initLayotOptions();
        initBean();
    }
    
    protected abstract void initBean();
    
    public SessionBean getSessionBean() {
        return sessionBean;
    }
    
    protected void onOpenCard(){
        if (getSourceBean() == null){
           Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
           beanName = params.get("beanName");
           sourceBean = getSessionBean().getSourceBean(getBeanName());
        }
    }
    
    protected abstract String onCloseCard();
     
    protected String onFinalCloseCard(Object param){
        getSessionBean().removeSourceBean(beanName);
        RequestContext.getCurrentInstance().closeDialog(param);
        return "/view/index?faces-redirect=true";
    }
        
    public void onItemChange() {
        setItemChange(Boolean.TRUE);
    }

    /* Получение и сохранение размеров формы */
    public void handleResize(org.primefaces.extensions.event.ResizeEvent event) { 
        Double width = event.getWidth();
        Double height = event.getHeight();
        Integer x = width.intValue() + 14;
        Integer y = height.intValue() + 14;
        sessionBean.saveFormSize(getFormName(), x, y);
    }
    
    protected abstract String getFormName();
    
    protected void initLayotOptions(){        
        LayoutOptions panes = new LayoutOptions();
        panes.addOption("slidable", false);
        layoutOptions.setPanesOptions(panes);
        
        LayoutOptions south = new LayoutOptions();
        south.addOption("resizable", false);
        south.addOption("closable", false);
        south.addOption("size", 45);
        layoutOptions.setSouthOptions(south);

        LayoutOptions west = new LayoutOptions();
        west.addOption("size", 170);
        west.addOption("minSize", 150);
        west.addOption("maxSize", 250);
        west.addOption("resizable", true);
        layoutOptions.setWestOptions(west);

        LayoutOptions center = new LayoutOptions();
        center.addOption("resizable", true);
        center.addOption("closable", false);
        center.addOption("minWidth", 200);
        center.addOption("minHeight", 100);
        layoutOptions.setCenterOptions(center);
    }
    
    protected User getCurrentUser(){
        return sessionBean.getCurrentUser();
    }    
    
    /* GETS & SETS */

    public LayoutOptions getLayoutOptions() {
        return layoutOptions;
    }
        
    public boolean isItemChange() {
        return itemChange;
    }
    public void setItemChange(boolean itemChange) {
        this.itemChange = itemChange;
    }

    public String getBeanName() {
        return beanName;
    }
    public void setSourceBean(BaseBean sourceBean) {
        this.sourceBean = sourceBean;
    }

    public String getLabelFromBundle(String key){
        return EscomBeanUtils.getBandleLabel(key);
    }
            
    public BaseBean getSourceBean() {
        return sourceBean;
    }
}
