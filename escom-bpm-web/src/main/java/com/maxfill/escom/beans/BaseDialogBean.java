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

import org.primefaces.event.CloseEvent;
import org.primefaces.extensions.component.layout.LayoutPane;
import org.primefaces.extensions.model.layout.LayoutOptions;

/* Базовый бин для диалогов  */
public abstract class BaseDialogBean implements Serializable{    
    private static final long serialVersionUID = 3712139345846276454L;
    protected static final Logger LOGGER = Logger.getLogger(BaseDialogBean.class.getName());
    
    @Inject
    protected SessionBean sessionBean;
    
    @EJB
    protected Configuration conf;
            
    private boolean itemChange;       //признак изменения записи

    private Integer centerHight = 0;
    private Integer northHight = 0;
    private Integer southHight = 0;

    private Integer westWidth = 0;
    private Integer centerWidth = 0;
    private Integer eastWidth = 0;

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
    public void onBeforeOpenCard(){
    }

    /**
     * Метод вызывается автоматически после загрузки формы диалога
     */
    public void onAfterFormLoad(){
    }

    protected abstract String onCloseCard();
     
    protected String onFinalCloseCard(Object param){
        return sessionBean.closeDialog(param);
    }

    /**
     * Обработка события изменения чего-либо на карточке
     */
    public void onItemChange() {
        setItemChange(Boolean.TRUE);
    }

    /* Обработка cобытия изменения размеров формы */
    public void handleResize(org.primefaces.extensions.event.ResizeEvent event) { 
        LayoutPane o = (LayoutPane)event.getSource();
        Double width = event.getWidth();
        Double height = event.getHeight();
        switch(o.getId()){
            case "center":{
                centerHight = height.intValue();
                centerWidth = width.intValue();
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

    protected abstract String getFormName();
    
    protected void initLayotOptions(){        
        LayoutOptions panes = new LayoutOptions();
        panes.addOption("slidable", false);
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

        if (isWestShow()) {
            LayoutOptions west = new LayoutOptions();
            west.addOption("size", 170);
            west.addOption("minSize", 150);
            west.addOption("maxSize", 250);
            west.addOption("resizable", true);
            west.addOption("initClosed", isWestInitClosed());
            layoutOptions.setWestOptions(west);
        }

        LayoutOptions center = new LayoutOptions();
        center.addOption("resizable", true);
        center.addOption("closable", false);
        center.addOption("minWidth", 200);
        center.addOption("minHeight", 100);
        layoutOptions.setCenterOptions(center);
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

    protected boolean isSouthInitClosed(){
        return false;
    }

    protected boolean isWestInitClosed(){
        return true;
    }

    protected boolean isNorthInitClosed(){
        return false;
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
