package com.maxfill.services;

import com.maxfill.Configuration;
import com.maxfill.dictionary.SysParams;
import com.maxfill.facade.ServicesFacade;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.services.common.history.ServicesEventsFacade;
import com.maxfill.services.common.sheduler.Sheduler;
import com.maxfill.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.ejb.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseTimer<P> {
    protected static final Logger LOG = Logger.getLogger(BaseTimer.class.getName());
    protected static final String RESULT_FAIL = "Error";
    protected static final String RESULT_SUCCESSFULLY = "Ok";

    StringBuilder detailInfo = new StringBuilder("");

    @EJB
    private ServicesFacade servicesFacade;
    @EJB
    protected ServicesEventsFacade servicesEventsFacade;
    @EJB
    protected Configuration conf;
    
    @Resource
    TimerService timerService;
    
    /**
     * Создание таймера
     * @param services - ссылка на службу
     * @param sheduler
     * @return 
     */
    public Timer createTimer(Services services, Sheduler sheduler){
        Date startDate = sheduler.getStartDate();        
        TimerConfig config = new TimerConfig(services, true);
        Timer timer;
        if (DateUtils.DAILY_REPEAT.equals(sheduler.getRepeatType())){
            Long interval = doMakeInterval(sheduler);
            timer = timerService.createIntervalTimer(startDate, interval, config);
        } else {
            ScheduleExpression schedule = new ScheduleExpression();
            schedule.start(startDate);
            if (DateUtils.HOUR_TYPE.equals(sheduler.getRepeatEachType())){
                schedule.hour("*/" + sheduler.getInterval());
            }
            if (DateUtils.MINUTE_TYPE.equals(sheduler.getRepeatEachType())){
                schedule.minute("*/" + sheduler.getInterval());
            }       
            String dayOfWeek = StringUtils.join(sheduler.getDayOfWeek(),",");
            schedule.dayOfWeek(dayOfWeek);                     
            schedule.timezone(TimeZone.getDefault().getID());
            timer = timerService.createCalendarTimer(schedule, config);
        } 
        LOG.log(Level.INFO, "Successfully create timer for service: {0}", services.getName());
        return timer;
    }
        
    @Timeout
    public void doTimer(Timer timer) {      
        Services service = (Services) timer.getInfo();
        LOG.log(Level.INFO, "Start timer for service: {0}", service.getName()); 
        P settings = restoreSettings(service);
        doExecuteTask(service, settings);
        service.setDateNextStart(timer.getNextTimeout());
        servicesFacade.edit(service);
        LOG.log(Level.INFO, "Finish timer for service: {0}", service.getName());
    }

    protected abstract ServicesEvents doExecuteTask(Services service, P settings); 
    protected abstract P restoreSettings(Services service);
    
    /**
     * Добавление строки в журнал событий службы
     * @param addRow 
     */
    protected void detailInfoAddRow(String addRow){
        detailInfo.append(addRow).append(SysParams.LINE_SEPARATOR);
    }
    
    /**
     * Формирование интервала в миллисекундах
     * @return 
     */
    private Long doMakeInterval(Sheduler scheduler){
        Integer interval = scheduler.getInterval();
        if (DateUtils.MINUTE_TYPE.equals(scheduler.getRepeatEachType())){            
            interval = interval * 60000;
            return interval.longValue();
        }
        if (DateUtils.HOUR_TYPE.equals(scheduler.getRepeatEachType())){ 
            interval = interval * 60 * 60000;
            return interval.longValue();
        }
        return null;
    }

    /**
     * Завершающее действие в doExecuteTask
     */
    protected void finalAction(ServicesEvents event) {
        Date finishDate = new Date();
        detailInfoAddRow("The service finished in " + DateUtils.dateToString(finishDate, DateFormat.SHORT, DateFormat.MEDIUM, conf.getServerLocale()));
        event.setDetails(detailInfo.toString());
        event.setDateFinish(finishDate);
        servicesEventsFacade.create(event);
    }
}