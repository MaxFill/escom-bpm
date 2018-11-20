/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.escom.beans.system.eventfeed;

import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.model.core.eventfeed.EventFeed;
import com.maxfill.model.core.eventfeed.EventFeedFacade;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import org.omnifaces.cdi.ViewScoped;

/**
 * Контролер ленты событий
 * @author maksim
 */
@Named
@ViewScoped
public class EventFeedBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = 355565612596749892L;

    @EJB
    private EventFeedFacade eventFeedFacade;
    
    private List<EventFeed> events;
    
    public void onDeleteEvent(EventFeed event){
        events.remove(event);
        eventFeedFacade.remove(event);        
    }
    
    public void onRefresh(){
        events = null;
    }           

    @Override
    public String getFormName() {
        return "";
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("EventFeed");
    }

    /* GETS & SETS */
    
    public List<EventFeed> getEvents() {
        if (events == null){
            events = eventFeedFacade.findLasts();
        }
        return events;
    }
    public void setEvents(List<EventFeed> events) {
        this.events = events;
    }
}
