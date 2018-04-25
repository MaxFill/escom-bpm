package com.maxfill.escom.beans.task;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.facade.TaskFacade;
import com.maxfill.model.staffs.Staff;
import org.primefaces.event.SelectEvent;

import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;

/**
 * Контролер формы "Поручение"
 */
@Named
@ViewScoped
public class TaskCardBean extends BaseViewBean{
    private static final long serialVersionUID = -2860068605023348908L;

    private Staff executor;
    
    @EJB
    private TaskFacade taskFacade;

    /**
     * Обработка события выбора Исполнителя
     * @param event
     */
    public void onExecutorChanged(SelectEvent event){
        List<Staff> items = (List<Staff>) event.getObject();
        if (items.isEmpty()) return;
        executor = items.get(0);
    }
    public void onExecutorChanged(ValueChangeEvent event){
        executor = (Staff) event.getNewValue();
    }
    
    /* GETS & SETS */

    public Staff getExecutor() {
        return executor;
    }
    public void setExecutor(Staff executor) {
        this.executor = executor;
    }

    @Override
    public String getFormName() {
        return DictDlgFrmName.FRM_TASK;
    }
}