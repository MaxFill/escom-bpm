package com.maxfill.escom.system;

import com.maxfill.escom.beans.ApplicationBean;
import com.sun.faces.application.view.ViewScopeManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {
    protected static final Logger LOG = Logger.getLogger(SessionListener.class.getName());
    @Inject
    private ApplicationBean appBean;
     
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        LOG.log(Level.INFO, "Session is created!");
        event.getSession().setAttribute(ViewScopeManager.ACTIVE_VIEW_MAPS_SIZE, 50);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se){
        HttpSession httpSession = se.getSession();
        LOG.log(Level.INFO, "Session is closed.");
        if (httpSession != null){
            String login = (String) httpSession.getAttribute("UserLogin");
            if (login != null && appBean != null){
                appBean.clearBasyLicence(login);
                LOG.log(Level.INFO, "Session closed for user {0}", login);
            }
        }
    }   
}