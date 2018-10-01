package com.maxfill.escom.beans.processes.elements;

import com.maxfill.model.basedict.process.procedures.Procedure;
import com.maxfill.model.basedict.process.procedures.ProcedureFacade;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * Сервисный бин сущности "Процедуры процесса"
 */
@SessionScoped
@Named
public class ProcedureBean implements Serializable{
    private static final long serialVersionUID = 3800339371613792171L;
    
    @EJB
    private ProcedureFacade procedureFacade;
    
    public ProcedureFacade getItemFacade(){
        return procedureFacade;
    }
    
    /**
     * Возвращает список всех доступных процедур для добавления их в схему процесса
     * @return 
     */
    public List<Procedure> findAll(){
        return procedureFacade.findAll();
    } 
}
