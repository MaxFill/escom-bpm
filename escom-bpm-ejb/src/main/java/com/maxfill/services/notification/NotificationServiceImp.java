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
import com.maxfill.utils.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
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
        List<State> states = Collections.singletonList(stateFacade.getRunningState());        
        AtomicInteger countTask = new AtomicInteger(0);
        final Date curDate = new Date();
        staffFacade.findActualStaff().stream()
                .forEach(staff->{
                    taskFacade.findTaskByStaffStates(staff, states)
                        .forEach(task->{                                                        
                            if (task.getNextReminder() != null && task.getNextReminder().before(new Date())){
                                notifReminder(task);
                                countTask.incrementAndGet();
                            }
                            if (task.getPlanExecDate().before(curDate)){
                                makeNotification(task, "ThisTaskOverdue");
                            }
                        });
                }
        );
        detailInfo.append("Send task notifications: ").append(countTask).append(SysParams.LINE_SEPARATOR);
        
        Set<Tuple> errors = new HashSet<>();
        AtomicInteger countTimers = new AtomicInteger(0);
        procTimerFacade.findActualTimers().forEach(timer -> {
                countTimers.incrementAndGet();
                workflow.executeTimer(timer, errors);
            });
        detailInfo.append("Execute timers: ").append(countTimers).append(SysParams.LINE_SEPARATOR);
    }

    /**
     * Напоминание о задаче
     * @param task 
     */
    private void notifReminder(Task task){        
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
        messagesFacade.createSystemMessage(resipient, sb.toString(), new StringBuilder(), links);        
        
        //дублирование уведомлений заместителям
        resipient.getAssistants().stream()
                .filter(assist->assist.getDuplicateChiefMessage() && assist.isActive())
                .forEach(assist->{
                    Locale assistlocale = userFacade.getUserLocale(assist.getUser());
                    StringBuilder dubleMsg = new StringBuilder();
                    dubleMsg.append(ItemUtils.getBandleLabel("DublicateMsg", assistlocale)).append(": ");
                    dubleMsg.append("[").append(sb.toString()).append("]");
                    StringBuilder content = new StringBuilder();
                    content.append(ItemUtils.getFormatMessage("ReceivedSubscribDuplicateMsgChief", assistlocale, new Object[]{resipient.getShortFIO()}));
                    messagesFacade.createSystemMessage(assist.getUser(), dubleMsg.toString(), content, links);
                });
    }
}