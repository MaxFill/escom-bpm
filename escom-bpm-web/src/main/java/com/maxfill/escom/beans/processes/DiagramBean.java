package com.maxfill.escom.beans.processes;

import com.maxfill.model.process.schemes.Scheme;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.primefaces.model.diagram.DefaultDiagramModel;

/**
 *
 * @author maksim
 */
@Named
@SessionScoped
public class DiagramBean implements Serializable{    
    private static final long serialVersionUID = -4403976059082444626L;
    
    private ConcurrentHashMap<String, DiagramData> diagrams = new ConcurrentHashMap<>();
    
    public class DiagramData {
        private Scheme scheme;
        private final DefaultDiagramModel model = new DefaultDiagramModel(); 
        
    }
    
}
