
package com.maxfill.escom.beans.users.sessions;

import com.maxfill.model.users.sessions.UsersSessions;
import com.maxfill.escom.beans.ApplicationBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.extensions.model.layout.LayoutOptions;

/**
 *
 * @author mfilatov
 */
@Named
@ViewScoped
public class UsersSessionsBean implements Serializable{
    private static final long serialVersionUID = 127067622604654493L;

    private UsersSessions selectedSession;
    private final LayoutOptions layoutOptions = new LayoutOptions();  
    private List<UsersSessions> listSessions;
            
    @Inject 
    private ApplicationBean appBean;
            
    @PostConstruct
    public void init(){
        initLayoutOptions();
    }        

    public List<UsersSessions> getListSessions() {           
        if (listSessions == null){
            listSessions = new ArrayList<>();
            appBean.getUserSessions().forEach(
                    (key, usSession) -> listSessions.add(usSession)
            );
        }
        return listSessions;
    } 
    
    public void refreshData(){
       listSessions = null; 
    }
    
    /**
     * Закрытие формы
     */
    public void onCloseForm(){
       RequestContext.getCurrentInstance().closeDialog(null); 
    }

    public UsersSessions getSelectedSession() {
        return selectedSession;
    }
    public void setSelectedSession(UsersSessions selectedSession) {
        this.selectedSession = selectedSession;
    }
    
    private void initLayoutOptions() {
        LayoutOptions panes = new LayoutOptions();
        panes.addOption("slidable", false);
        panes.addOption("resizable", true);
        layoutOptions.setPanesOptions(panes);

        LayoutOptions center = new LayoutOptions();
        center.addOption("resizable", true);
        center.addOption("closable", false);
        center.addOption("size", 600);
        center.addOption("minWidth", 300);
        center.addOption("minHeight", 300);
        layoutOptions.setCenterOptions(center);  
        
        LayoutOptions south = new LayoutOptions();
        south.addOption("resizable", false);
        south.addOption("closable", false);
        south.addOption("size", 38);
        layoutOptions.setSouthOptions(south);
        
        LayoutOptions west = new LayoutOptions();
        west.addOption("size", 150);
        west.addOption("minSize", 150);
        west.addOption("maxSize", 350);
        layoutOptions.setWestOptions(west);
        /*        
        LayoutOptions east = new LayoutOptions();;
        east.addOption("size", 300);
        east.addOption("minSize", 150);
        east.addOption("maxSize", 450);
        layoutOptions.setEastOptions(east);
        */
     
    }
    public LayoutOptions getLayoutOptions() {
        return layoutOptions;
    }
    
}
