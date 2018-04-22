package com.maxfill.escom.beans.task;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.facade.TaskFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.process.schemes.task.Task;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;

/**
 * Контролер формы "Поручение"
 */
@Named
@ViewScoped
public class TaskCardBean extends BaseCardBean<Task>{
    private static final long serialVersionUID = -2860068605023348908L;

    private Staff executor;
    
    @EJB
    private TaskFacade taskFacade;

    @Override
    protected BaseDictFacade getFacade() {
        return taskFacade;
    }
    
    /**
     * Обработка события выбора Исполнителя
     * @param event
     */
    public void onExecutorChanged(SelectEvent event){
        List<Staff> items = (List<Staff>) event.getObject();
        if (items.isEmpty()) return;
        executor = items.get(0);
        onItemChange();
    }
    public void onExecutorChanged(ValueChangeEvent event){
        executor = (Staff) event.getNewValue();
        onItemChange();
    }
    
    /* GETS & SETS */

    public Staff getExecutor() {
        return executor;
    }
    public void setExecutor(Staff executor) {
        this.executor = executor;
    }
    
}