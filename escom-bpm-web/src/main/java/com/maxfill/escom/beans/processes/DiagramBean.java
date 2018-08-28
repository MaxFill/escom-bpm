package com.maxfill.escom.beans.processes;

import com.maxfill.escom.beans.SessionBean;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;

/**
 *
 * @author maksim
 */
@Named
@RequestScoped
public class DiagramBean implements Serializable{    
    private static final long serialVersionUID = -4403976059082444626L;
    
    private DefaultDiagramModel model;
    
    @Inject
    private SessionBean sessionBean;    
    
    @PostConstruct
    private void init(){
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();        
        String beanId = (String) ec.getFlash().get("beanId");
        if (StringUtils.isNotEmpty(beanId)){
            model = sessionBean.getDiagrams().get(beanId);
        }
    }
    
    public DiagramModel getModel() {
        return model;
    }
}
