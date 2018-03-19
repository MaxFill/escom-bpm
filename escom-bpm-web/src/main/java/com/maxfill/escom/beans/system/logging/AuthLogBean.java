package com.maxfill.escom.beans.system.logging;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.maxfill.Configuration;
import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.AuthLogFacade;
import com.maxfill.model.authlog.Authlog;
import com.maxfill.utils.DateUtils;
import org.primefaces.component.column.Column;
import org.primefaces.component.columntoggler.ColumnToggler;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.export.ExporterType;
import org.primefaces.event.ToggleEvent;
import org.primefaces.extensions.model.layout.LayoutOptions;
import org.primefaces.model.Visibility;

import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/* Контролер формы журнала аутентификации пользователей */

@ViewScoped
@Named
public class AuthLogBean extends BaseDialogBean{
    private static final long serialVersionUID = -2035201127652612778L;

    private Authlog selected;
    private List<Authlog> authlogs;
    private Date dateStart;
    private Date dateEnd;

    private String orientationName = "Portret";
    private boolean orientation;
    private boolean onlyCurPageExp;
    private final Map<String, Boolean> visibleColumns = new HashMap <>();
    private final Map<Integer, String> columns = new HashMap <>();

    @EJB
    private Configuration conf;
    @EJB
    private AuthLogFacade authLogFacade;

    @Override
    protected void initBean(){
        dateEnd = DateUtils.clearDate(DateUtils.addDays(new Date(), 1));
        dateStart = DateUtils.addDays(dateEnd, -3);

        columns.put(0, "colImg");
        columns.put(1, "colDate");
        columns.put(2, "colLogin");
        columns.put(3, "colEvent");
        columns.put(4, "colIP");
        columns.put(5, "colSMS");

        columns.entrySet().stream().forEach(col->visibleColumns.put(col.getValue(), true));
    }

    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);
    }

    @Override
    public String getFormName(){
        return DictDlgFrmName.FRM_AUTH_LOG;
    }

    public String getBundleName(String keyBundle){
        if (keyBundle == null) return null;
        return EscomMsgUtils.getBandleLabel(keyBundle);
    }


    /**
     * Обработка события перемещения столбцов в таблице
     * @param event
     */
    public void onColumnReorder(AjaxBehaviorEvent event){
        DataTable table = (DataTable) event.getSource();
        columns.clear();
        for (int i = 0; i < table.getColumns().size(); i++) {
            UIComponent col = (UIComponent) table.getColumns().get(i);
            columns.put(i, col.getId());
        }
    }

    /**
     * Обработка события скрытия/отображения колонок таблицы
     * @param event
     */
    public void onToggle(ToggleEvent event){
        Integer columnIndex = (Integer) event.getData();
        String column = columns.get(columnIndex);
        visibleColumns.replace(column, event.getVisibility() == Visibility.VISIBLE);
    }

    /**
     * Возвращает флаг видимости столбца по его имени
     * @param column
     * @return
     */
    public boolean isVisibleColumn(String column){
        return visibleColumns.get(column);
    }

    public void refreshData(){
        authlogs = null;
    }

    public void clearData(){
        Integer countDelete = authLogFacade.clearEvents(dateStart, dateEnd);
        authlogs = null;
        EscomMsgUtils.succesFormatMsg("RemovedEntries", new Object[]{countDelete});
    }

    public List <Authlog> getAuthlogs() {
        if (authlogs == null){
            authlogs = authLogFacade.findEventsByPeriod(dateStart, dateEnd);
        }
        return authlogs;
    }

    public void onChangeOrientation(){
        orientationName = orientation ? "Landscape" : "Portret";
    }

    public void preProcessPDF(Object document) throws IOException, BadElementException, DocumentException {
        String fontUrl = conf.getJasperReports() + conf.getPdfFont();
        //BaseFont.createFont(fontUrl, "CP1251", BaseFont.EMBEDDED);
        FontFactory.register(fontUrl);
    }

    protected void initLayotOptions() {
        super.initLayotOptions();
        LayoutOptions west = layoutOptions.getWestOptions();
        west.addOption("initClosed", true);
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

    public Authlog getSelected() {
        return selected;
    }
    public void setSelected(Authlog selected) {
        this.selected = selected;
    }

    public Date getDateStart() {
        return dateStart;
    }
    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }
    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }
}
