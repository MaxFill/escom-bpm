package com.maxfill.services.notification;

import com.maxfill.facade.StaffFacade;
import com.maxfill.facade.StateFacade;
import com.maxfill.facade.TaskFacade;
import com.maxfill.facade.UserMessagesFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.states.State;
import com.maxfill.model.task.Task;
import com.maxfill.services.Services;
import com.maxfill.utils.Tuple;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * Сервис формирования системных уведомлений
 */
@Stateless
public class NotificationServiceImp implements NotificationService{

    @EJB
    private TaskFacade taskFacade;
    @EJB
    private StaffFacade staffFacade;
    @EJB
    private StateFacade stateFacade;
    @EJB
    private UserMessagesFacade messagesFacade;
    
    @Override
    public NotificationSettings createSettings(Services service) {
        return new NotificationSettings();
    }

    @Override
    public void makeNotifications() {
        List<State> states = new ArrayList<>();
        states.add(stateFacade.getRunningState());
        staffFacade.findActualStaff().parallelStream().forEach(staff->{
            taskFacade.findTaskByStaffStates(staff, states).forEach(task->makeNotification(task));
        });
    }

    private void makeNotification(Task task){        
        Doc doc = null;
        if (task.getScheme() != null && task.getScheme().getProcess() != null){
           doc = task.getScheme().getProcess().getDoc();
        }
        messagesFacade.createSystemMessage(task.getOwner().getEmployee(), task.getName(), "", new Tuple(doc, task));
    }
}