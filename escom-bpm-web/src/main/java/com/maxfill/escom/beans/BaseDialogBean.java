package com.maxfill.escom.beans;

import com.maxfill.Configuration;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.users.User;
import org.primefaces.PrimeFaces;
import org.primefaces.context.RequestContext;

import javax.annotation.PreDestroy;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import org.primefaces.extensions.model.layout.LayoutOptions;

/* Базовый бин для служебных диалогов  */
public abstract class BaseDialogBean implements Serializable{    
    private static final long serialVersionUID = 3712139345846276454L;
    protected static final Logger LOGGER = Logger.getLogger(BaseDialogBean.class.getName());
    
    @Inject
    protected SessionBean sessionBean;
    
    @EJB
    protected Configuration conf;
            
    private boolean itemChange;       //признак изменения записи

    protected final LayoutOptions layoutOptions = new LayoutOptions();

    @PostConstruct
    protected void init(){
        initLayotOptions();
        initBean();
    }

    @PreDestroy
    protected void destroy(){
        System.out.println("view bean destroy!");
    }

    protected abstract void initBean();
    
    public SessionBean getSessionBean() {
        return sessionBean;
    }

    /**
     * Метод вызывается автоматически при открытии формы диалога
     */
    protected void onOpenCard(){
    }
    
    protected abstract String onCloseCard();
     
    protected String onFinalCloseCard(Object param){
        return sessionBean.closeDialog(param);
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

    public String getLabelFromBundle(String key){
        return EscomMsgUtils.getBandleLabel(key);
    }

}
