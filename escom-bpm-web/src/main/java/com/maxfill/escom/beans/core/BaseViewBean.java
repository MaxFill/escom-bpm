package com.maxfill.escom.beans.core;

import com.maxfill.Configuration;
import com.maxfill.escom.beans.ApplicationBean;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.users.User;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;

import org.primefaces.extensions.component.layout.LayoutPane;
import org.primefaces.extensions.model.layout.LayoutOptions;

/**
 * Базовый бин для работы с формами и диалогами
 * Реализует методы обработки событий открытия и закрытия диалогов,
 * сохранения изменений размеров форм
 */
public abstract class BaseViewBean implements Serializable{
    protected static final Logger LOGGER = Logger.getLogger(BaseViewBean.class.getName());

    @Inject
    protected ApplicationBean appBean;
    @Inject
    protected SessionBean sessionBean;
    @EJB
    protected Configuration conf;

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
        System.out.println("Bean [" + this.getClass().getSimpleName() + "] destroy!");
    }

    protected void initBean(){};

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

    public abstract String getFormName();
    
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

    public User getCurrentUser(){
        return sessionBean.getCurrentUser();
    }    
    
    /* GETS & SETS */

    public LayoutOptions getLayoutOptions() {
        return layoutOptions;
    }

    public String getLabelFromBundle(String key){
        return EscomMsgUtils.getBandleLabel(key);
    }

}
