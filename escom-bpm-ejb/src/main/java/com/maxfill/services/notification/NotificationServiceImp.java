package com.maxfill.services.notification;

import com.maxfill.model.BaseDict;
import com.maxfill.model.process.Process;
import com.maxfill.model.staffs.StaffFacade;
import com.maxfill.model.states.StateFacade;
import com.maxfill.model.task.TaskFacade;
import com.maxfill.model.messages.UserMessagesFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.process.timers.ProcTimerFacade;
import com.maxfill.model.states.State;
import com.maxfill.model.task.Task;
import com.maxfill.model.users.User;
import com.maxfill.model.users.UserFacade;
import com.maxfill.services.Services;
import com.maxfill.services.workflow.Workflow;
import com.maxfill.utils.ItemUtils;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.collections.CollectionUtils;

/**
 * Сервис формирования системных уведомлений
 */
@Stateless
public class NotificationServiceImp implements NotificationService{

    @EJB
    private UserFacade userFacade;
    @EJB
    private TaskFacade taskFacade;
    @EJB
    private StaffFacade staffFacade;
    @EJB
    private StateFacade stateFacade;
    @EJB
    private UserMessagesFacade messagesFacade;  
    @EJB
    private ProcTimerFacade procTimerFacade;
    @EJB
    private Workflow workflow;
    
    @Override
    public NotificationSettings createSettings(Services service) {
        return new NotificationSettings();
    }

    @Override
    public void makeNotifications() {
        List<State> states = new ArrayList<>();
        states.add(stateFacade.getRunningState());
        staffFacade.findActualStaff().stream()
                .forEach(staff->{
                    taskFacade.findTaskByStaffStates(staff, states)
                            .stream()
                            .filter(task->task.getNextReminder() != null && task.getNextReminder().before(new Date()))
                            .forEach(task->notification(task));
                }
        );
        Set<String> errors = new HashSet<>();
        procTimerFacade.findActualTimers().forEach(timer -> workflow.executeTimer(timer, errors));
    }

    private void notification(Task task){        
        makeNotification(task, "Reminder");
        taskFacade.makeNextReminder(task);
        taskFacade.edit(task);
    }
    
    /**
     * Формирование напоминания по задаче
     * Вызывается так же из маршрута процесса
     * @param task 
     * @param msgKeySubject - ключ ресурса для сообщения 
     */
    @Override
    public void makeNotification(Task task, String msgKeySubject){        
        User resipient = task.getOwner().getEmployee();
        if (resipient == null) return;
        Locale locale = userFacade.getUserLocale(resipient);
        
        StringBuilder sb = new StringBuilder();
        sb.append(ItemUtils.getMessageLabel(msgKeySubject, locale));
        sb.append(" <").append(task.getName()).append(">");
        
        Map<String, BaseDict> links = new HashMap<>();
        links.put("task", task);
        if (task.getScheme() != null && task.getScheme().getProcess() != null){
            Process process = task.getScheme().getProcess();
            links.put("process", process);
            List<Doc> docs = process.getDocs();
            if (CollectionUtils.isNotEmpty(docs)){
                Doc doc = docs.stream().findFirst().orElse(null);
                links.put("doc", doc);
            }
        }
        messagesFacade.createSystemMessage(task.getOwner().getEmployee(), sb.toString(), new StringBuilder(), links);
    }    
    
}