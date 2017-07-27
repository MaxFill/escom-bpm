package com.maxfill.services.webDav;

import com.maxfill.Configuration;
import com.maxfill.facade.AttacheFacade;
import com.maxfill.facade.DocFacade;
import com.maxfill.facade.UserMessagesFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.users.User;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.ItemUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;

@Stateless
public class WebDavRemainder {
    private static final Logger LOG = Logger.getLogger(WebDavRemainder.class.getName());
    private static final Integer COUNT_REMAINING_CYCLES = 3;
    
    @Resource
    TimerService timerService;
    
    @EJB
    private AttacheFacade attacheFacade;
    @EJB
    private WebDavService webDavService;
    @EJB
    private UserMessagesFacade messagesFacade;
    @EJB
    private Configuration conf;
    @EJB
    private DocFacade docFacade;
                
    public void createTimer(Attaches attache, User editor, Date lockDate){
        Timer timer = startTimer(attache, lockDate); 
        attache.setLockAuthor(editor);
        attache.setLockDate(new Date());
        attache.setPlanUnlockDate(lockDate);
        attache.setCountRemainingCycles(COUNT_REMAINING_CYCLES);
        attache.setTimeHandle(getTimerHandlerBytes(timer));
        attacheFacade.edit(attache);
        docFacade.doSetEditState(attache.getDoc(), editor);
        webDavService.uploadFile(attache);  // скопировать файл в хранилище                
        LOG.log(Level.INFO, "Successfully create remainder timer : {0}", attache.getName());        
    }        
    
    public void changeTimer(Attaches attache, User editor, Date lockDate){
        stopTimer(attache);
        startTimer(attache, lockDate);
        LOG.log(Level.INFO, "Successfully change remainder timer : {0}", attache.getName());
    }
    
    public void cancelTimer(Attaches attache){
        stopTimer(attache);
        attache.setLockAuthor(null);
        attache.setLockDate(null);
        attache.setPlanUnlockDate(null);
        attache.setTimeHandle(null);
        attache.setCountRemainingCycles(null);
        attacheFacade.edit(attache);
        docFacade.doRemoveEditState(attache.getDoc());
        webDavService.downloadFile(attache);    // забрать файл из хранилища 
        LOG.log(Level.INFO, "Remainder for attache {0} is cancelled!", attache.getName()); 
    }
    
    private Timer startTimer(Attaches attache, Date lockDate){      
        TimerConfig config = new TimerConfig(attache, true);
                
        Date remainDate = DateUtils.addDays(lockDate, -1);                        
        LocalDateTime localDateTime = DateUtils.toLocalDateTime(remainDate);
        
        ScheduleExpression schedule = new ScheduleExpression();
        schedule.start(remainDate);
        schedule.hour(makeHours(localDateTime.getHour()));
        schedule.minute(localDateTime.getMinute());
        schedule.dayOfWeek("Mon, Tue, Wed, Thu, Fri");                     
        schedule.timezone(TimeZone.getDefault().getID());
        Timer timer = timerService.createCalendarTimer(schedule, config);
        Date nextDate = timer.getNextTimeout();      
        LOG.log(Level.INFO, "Timer start! Next date {0}", nextDate);
        return timer;
    }
    
    private String makeHours(Integer firstHour){         
        int secondHour;        
        if (firstHour + 3 > 18){
            secondHour = 9;
        } else {
            secondHour = firstHour + 3;
        }
        int thirdHour;
        if (secondHour + 3 > 18) {
            thirdHour = 9;
        } else {
            thirdHour = secondHour + 3;
        }
        StringBuilder hours = new StringBuilder();    
        hours.append(firstHour).append(", ").append(secondHour).append(", ").append(thirdHour);
        return hours.toString();
    }
    
    private void stopTimer(Attaches attache){  
        if (attache.getTimeHandle() == null) return;
        ByteArrayInputStream bais;
        ObjectInputStream ins;
        try {
            bais = new ByteArrayInputStream(attache.getTimeHandle());
            ins = new ObjectInputStream(bais);
            TimerHandle timerHandle =(TimerHandle)ins.readObject();
            ins.close();
            timerHandle.getTimer().cancel();
                        
            LOG.log(Level.INFO, "Timer stop! ");
        }  catch (NoSuchObjectLocalException | IOException | ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    } 
      
    private byte[] getTimerHandlerBytes(Timer timer){        
        ObjectOutputStream oos = null;
        ByteArrayOutputStream bos = null;
        byte[] data = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(timer.getHandle()); 
            data = bos.toByteArray();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (oos != null){
                    oos.flush();                
                    oos.close();
                }
                if (bos != null) bos.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        return data;
    }    
    
    @Timeout
    public void doTimer(Timer timer) {      
        Attaches attache = (Attaches) timer.getInfo();
        if (attache == null){
            timer.cancel();
            LOG.log(Level.INFO, "The timer is stopped because феесфру is null ! ");
            return;
        }
        LOG.log(Level.INFO, "Start work remainder for attache: {0}", attache.getName());
        timerWork(attache);        
        LOG.log(Level.INFO, "Finish work remainder for attache: {0}", attache.getName());
    }
    
    private void timerWork(Attaches attache){
        Integer countRemainingCycles = attache.getCountRemainingCycles();
        User adressee = attache.getLockAuthor();
        Doc doc = attache.getDoc();
        
        StringBuilder docUrl = new StringBuilder(conf.getServerURL());
        docUrl.append("faces/view/").append("docs/").append("doc-card").append(".xhtml").append("?itemId=");
        docUrl.append(doc.getId());
        docUrl.append("&openMode=0");
        
        StringBuilder content = new StringBuilder();
        content.append(ItemUtils.getBandleLabel("Document")).append(": ");
        content.append("<a href=").append(docUrl).append(">").append(doc.getFullName()).append("</a>").append("<br />");        
        
        if (countRemainingCycles == 0){
            cancelTimer(attache);
            String subject = ItemUtils.getMessageLabel("DocumentWasAutoUnlocked");
            messagesFacade.createSystemMessage(adressee, subject, content.toString(), doc);
        } else {
            String dateUnlock = DateUtils.dateToString(attache.getPlanUnlockDate(), DateFormat.SHORT, DateFormat.MEDIUM, conf.getServerLocale());
            String msgError = ItemUtils.getFormatMessage("DocumentWilBeAutomaticallyUnlocked", new Object[]{dateUnlock});
            
            StringBuilder subject = new StringBuilder();
            subject.append(ItemUtils.getMessageLabel("YouNeedUnlockDocument")).append(" ").append(msgError);  
            messagesFacade.createSystemMessage(adressee, subject.toString(), content.toString(), doc);
            countRemainingCycles--;
            attache.setCountRemainingCycles(countRemainingCycles);
            attacheFacade.edit(attache);
        }
    }
    
    
}