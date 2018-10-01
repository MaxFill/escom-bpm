package com.maxfill.services.notification;

import com.maxfill.dictionary.SysParams;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.staff.StaffFacade;
import com.maxfill.model.core.states.StateFacade;
import com.maxfill.model.basedict.task.TaskFacade;
import com.maxfill.model.core.messages.UserMessagesFacade;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.process.timers.ProcTimerFacade;
import com.maxfill.model.core.states.State;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.basedict.user.UserFacade;
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
import java.util.concurrent.atomic.AtomicInteger;
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
    public void makeNotifications(StringBuilder detailInfo) {        
        List<State> states = new ArrayList<>();
        states.add(stateFacade.getRunningState());
        AtomicInteger countTask = new AtomicInteger(0);
        staffFacade.findActualStaff().stream()
                .forEach(staff->{
                    taskFacade.findTaskByStaffStates(staff, states)
                            .stream()
                            .filter(task->task.getNextReminder() != null && task.getNextReminder().before(new Date()))
                            .forEach(task->{
                                countTask.incrementAndGet();
                                notification(task);
                            });
                }
        );
        detailInfo.append("Send task notifications: ").append(countTask).append(SysParams.LINE_SEPARATOR);
        
        Set<String> errors = new HashSet<>();
        AtomicInteger countTimers = new AtomicInteger(0);
        procTimerFacade.findActualTimers().forEach(timer -> {
                countTimers.incrementAndGet();
                workflow.executeTimer(timer, errors);
            });
        detailInfo.append("Execute timers: ").append(countTimers).append(SysParams.LINE_SEPARATOR);
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
        List<BaseDict> links = new ArrayList<>();
        links.add(task);
        if (task.getScheme() != null && task.getScheme().getProcess() != null){
            Process process = task.getScheme().getProcess();
            links.add(process);
            List<Doc> docs = process.getDocs();
            if (CollectionUtils.isNotEmpty(docs)){
                Doc doc = docs.stream().findFirst().orElse(null);
                links.add(doc);
            }
        }
        messagesFacade.createSystemMessage(task.getOwner().getEmployee(), sb.toString(), new StringBuilder(), links);
    }    
    
}