package com.maxfill.escom.system.services;

import static com.maxfill.escom.utils.MsgUtils.getBandleLabel;

import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.services.ServicesFacade;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.Services;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.services.common.sheduler.Sheduler;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import org.primefaces.event.SelectEvent;
import javax.ejb.EJB;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.faces.model.SelectItem;
import javax.xml.bind.JAXB;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Базовый бин для системных служб (сервисов)
 * @param <P> класс параметров службы
 */
public abstract class BaseServicesBean<P> extends BaseViewBean{    
    private static final long serialVersionUID = -4590499571847393975L;
    protected static final Logger LOG = Logger.getLogger(BaseServicesBean.class.getName());
    
    @EJB
    private ServicesFacade servicesFacade;    

    
    protected Services service;
    
    private P settings;
    private Sheduler scheduler;
    private ServicesEvents selectedEvent;
    private List<SelectItem> repeatTypes;
    private List<SelectItem> intervalTypes;
    private List<SelectItem> daysOfWeek;
        
    @Override
    public void initBean() {
        initSelectItems();
        service = getServicesFacade().find(getSERVICE_ID()); 
        settings = createSettings(); 
        scheduler = createSheduler();
    }
    
    protected abstract P createSettings();
    public abstract BaseTimer getTimerFacade();
    public abstract int getSERVICE_ID();
    
    /**
     * Создаёт объект Sheduler для службы
     * @return 
     */
    private Sheduler createSheduler(){
        byte[] compressXML = service.getSheduler();
        if (compressXML != null && compressXML.length >0){
            try {
                String settingsXML = EscomUtils.decompress(compressXML);
                scheduler = (Sheduler) JAXB.unmarshal(new StringReader(settingsXML), Sheduler.class);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        } else {
            scheduler = new Sheduler();
        }
        return scheduler;
    }
    
    /**
     * Запуск службы
     */
    public void onStartService(){
        try {
            doSaveSettings();
            Timer timer = getTimerFacade().createTimer(service, getScheduler());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(timer.getHandle());
            oos.flush();
            oos.close();
            bos.close();
            byte[] data = bos.toByteArray();
            service.setTimeHandle(data);
            service.setDateNextStart(timer.getNextTimeout());
            service.setStarted(Boolean.TRUE);
            servicesFacade.edit(service);
            MsgUtils.succesMsg("ServiceRunSchedule");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Немедленный запуск службы на выполнение
     */
    public abstract void doRunService();
    
    /**
     * Остановка службы
     */
    public void onStopService(){  
        if (service.getTimeHandle() != null){
            ByteArrayInputStream bais;
            ObjectInputStream ins;
            try {
                bais = new ByteArrayInputStream(service.getTimeHandle());
                ins = new ObjectInputStream(bais);
                TimerHandle timerHandle =(TimerHandle)ins.readObject();
                ins.close();
                timerHandle.getTimer().cancel();
                service.setStarted(Boolean.FALSE);
                service.setDateNextStart(null);
                servicesFacade.edit(service);
                MsgUtils.succesMsg("ServiceStopped");
                LOG.log(Level.INFO, "Timer for service {0} is cancelled!", service.getName()); 
            }  catch (NoSuchObjectLocalException | IOException | ClassNotFoundException exception) {
                MsgUtils.errorMessage(exception.getMessage());
                throw new RuntimeException(exception);
            }
        }
    }           
    
    /**
     * Действие по кнопке "Сохранить настройки"
     */
    public void onSaveSettings(){
        if (doSaveSettings()) {
            MsgUtils.succesFormatMsg("DataIsSaved", new Object[]{service.getName()});
        } else {
            MsgUtils.errorFormatMsg("ErrorSaveSettings", new Object[]{service.getName()});
        }
    }
    
    /**
     * Сохранение параметров службы
     */
    private boolean doSaveSettings(){
        try {
            service.setSettings(EscomUtils.compress(settings.toString()));
            service.setSheduler(EscomUtils.compress(scheduler.toString()));    
            servicesFacade.edit(service);
            return true;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return false;
    }            
    
    /**
     * Очистка журнала событий службы
     */
    public void clearLogEvents(){
        service.getServicesEventsList().clear();
        servicesFacade.edit(service);
    }

    /**
     * Удаление события
     * @param logEvent
     */
    public void deleteLogEvent(ServicesEvents logEvent){
        service.getServicesEventsList().remove(logEvent);
        servicesFacade.edit(service);
    }

    /**
     * Обновление журнала событий службы
     */
    public void refreshLogEvents(){
        service = servicesFacade.find(service.getId());
    }
    
    /**
     * Обработка двойного клика в журнале событий
     * @param event 
     */
    public void onRowDblClck(SelectEvent event) {
        selectedEvent = (ServicesEvents) event.getObject();
    }    
    
    @Override
    public Boolean isWestShow(){
        return true;
    }
    @Override
    public Boolean isEastShow(){
        return true;
    }
    
    @Override
    public String getMainGridColumnStyleClass() {
        return "ui-grid-col-3 col-grid-expl, ui-grid-col-6 col-grid-expl, ui-grid-col-3 col-grid-expl";
    }

    @Override
    public String getMainGridColumnCount() {
        return "3";
    }
    
    /**
     * Инициализация списковых значений для настройки расписания
     */
    private void initSelectItems(){
        repeatTypes = new ArrayList<>();
        repeatTypes.add(new SelectItem(DateUtils.DAILY_REPEAT, getBandleLabel("Daily")));
        repeatTypes.add(new SelectItem(DateUtils.WEEKLY_REPEAT, getBandleLabel("Weekly" )));
        //repeatTypes.add(new SelectItem(DateUtils.MONTHLY_REPEAT, EscomUtils.getBandleLabel("Monthly")));
        intervalTypes = new ArrayList<>();
        intervalTypes.add(new SelectItem(DateUtils.MINUTE_TYPE, getBandleLabel("Minute")));
        intervalTypes.add(new SelectItem(DateUtils.HOUR_TYPE, getBandleLabel("Hour")));
        daysOfWeek = new ArrayList<>();
        daysOfWeek.add(new SelectItem("Sun", getBandleLabel("SUNDAY")));
        daysOfWeek.add(new SelectItem("Mon", getBandleLabel("MONDAY")));
        daysOfWeek.add(new SelectItem("Tue", getBandleLabel("TUESDAY")));
        daysOfWeek.add(new SelectItem("Wed", getBandleLabel("WEDNESDAY")));
        daysOfWeek.add(new SelectItem("Thu", getBandleLabel("THURSDAY")));
        daysOfWeek.add(new SelectItem("Fri", getBandleLabel("FRIDAY")));
        daysOfWeek.add(new SelectItem("Sat", getBandleLabel("SATURDAY")));
    }

    public ServicesFacade getServicesFacade() {
        return servicesFacade;
    }

    public Sheduler getScheduler() {
        return scheduler;
    }

    public P getSettings() {
        return settings;
    }
    public void setSettings(P settings) {
        this.settings = settings;
    }

    public void setService(Services service) {
        this.service = service;
    }
    public Services getService() {
        return service;
    }

    public ServicesEvents getSelectedEvent() {
        return selectedEvent;
    }
    public void setSelectedEvent(ServicesEvents selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    public List<SelectItem> getRepeatTypes() {
        return repeatTypes;
    }

    public List<SelectItem> getIntervalTypes() {
        return intervalTypes;
    }

    public List<SelectItem> getDaysOfWeek() {
        return daysOfWeek;
    }
    
}
