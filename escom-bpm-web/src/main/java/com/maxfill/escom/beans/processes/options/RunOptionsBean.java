package com.maxfill.escom.beans.processes.options;

import com.maxfill.model.basedict.process.options.RunOptions;
import com.maxfill.model.basedict.process.options.RunOptionsFacade;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * Сервисный бин "Результаты выполнения задачи" 
 * @author maksim
 */
@Named
@SessionScoped
public class RunOptionsBean implements Serializable{    
    private static final long serialVersionUID = -3352797318187287815L;

    @EJB
    private RunOptionsFacade runOptionsFacade;
    
    public List<RunOptions> findAll(){
        return runOptionsFacade.findAll();
    } 
    
    public RunOptionsFacade getItemFacade(){
        return runOptionsFacade;
    }
}
