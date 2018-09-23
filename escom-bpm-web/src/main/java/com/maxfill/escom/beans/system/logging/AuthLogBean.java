package com.maxfill.escom.beans.system.logging;

import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.lazyload.LazyLoadBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.authlog.AuthLogFacade;
import com.maxfill.facade.BaseLazyFacade;
import com.maxfill.model.authlog.Authlog;
import com.maxfill.utils.DateUtils;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/* Контролер формы журнала аутентификации пользователей */

@ViewScoped
@Named
public class AuthLogBean extends LazyLoadBean{
    private static final long serialVersionUID = -2035201127652612778L;

    private String orientationName = "Portret";
    private boolean orientation;
    private boolean onlyCurPageExp;
    private final Map<String, Boolean> visibleColumns = new HashMap <>();
    private final Map<Integer, String> columns = new HashMap <>();

    @EJB
    private AuthLogFacade authLogFacade;

    @Override
    protected void initBean(){
        columns.put(0, "colImg");
        columns.put(1, "colDate");
        columns.put(2, "colLogin");
        columns.put(3, "colEvent");
        columns.put(4, "colIP");
        columns.put(5, "colSMS");
        columns.entrySet().stream().forEach(col->visibleColumns.put(col.getValue(), true));
        dateEnd = DateUtils.clearDate(DateUtils.addDays(new Date(), 1));
        dateStart = DateUtils.addDays(dateEnd, -3);
    }

    @Override
    protected BaseLazyFacade getLazyFacade() {
        return authLogFacade;
    }

    @Override
    protected Map <String, Object> makeFilters(Map filters) {
        if (dateStart != null || dateEnd != null) {
            Map <String, Date> dateFilters = new HashMap<>();
            dateFilters.put("startDate", dateStart);
            dateFilters.put("endDate", dateEnd);
            filters.put("dateEvent", dateFilters);
        }
        return filters;
    }

    @Override
    public String getFormName(){
        return DictFrmName.FRM_AUTH_LOG;
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("AuthenticationLog");
    }

    public String getBundleName(String keyBundle){
        if (keyBundle == null) return null;
        return MsgUtils.getBandleLabel(keyBundle);
    }

    /**
     * Обработка команды очистки журнала
     */
    public void onClearData(){
        Integer countDelete = deleteItems();
        MsgUtils.succesFormatMsg("RemovedEntries", new Object[]{countDelete});
    }

    /**
     * Формирует сообщение для вывода в диалоге подтверждения очистки журнала
     * @return
     */
    public String clearEventsConfirmMsg(){
        Object[] params = new Object[]{countItems()};
        return MessageFormat.format(MsgUtils.getBandleLabel("WillBeDeleted"), params);
    }

    public void onChangeOrientation(){
        orientationName = orientation ? "Landscape" : "Portret";
    }

    /**
     * Установка шрифта для экспорта журнала в PDF
     * @param document
     * @throws IOException
     * @throws DocumentException
     */
    public void preProcessPDF(Object document) throws IOException, DocumentException {
        String fontUrl = conf.getJasperReports() + conf.getPdfFont();
        FontFactory.register(fontUrl);
    }

    /* gets & sets */

    public boolean isOnlyCurPageExp() {
        return onlyCurPageExp;
    }
    public void setOnlyCurPageExp(boolean onlyCurPageExp) {
        this.onlyCurPageExp = onlyCurPageExp;
    }

    public boolean isOrientation() {
        return orientation;
    }
    public void setOrientation(boolean orientation) {
        this.orientation = orientation;
    }

    public String getOrientationName() {
        return orientationName;
    }

    @Override
    public Authlog getSelected() {
        return (Authlog)selected;
    }
    public void setSelected(Authlog selected) {
        this.selected = selected;
    }

}
