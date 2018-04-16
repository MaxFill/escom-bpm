package com.maxfill.escom.beans.system.logging;

import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.maxfill.Configuration;
import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.system.lazyload.LazyLoadDialogBean;
import com.maxfill.escom.beans.system.lazyload.LazyLoadModel;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.AuthLogFacade;
import com.maxfill.facade.base.BaseLazyLoadFacade;
import com.maxfill.model.authlog.Authlog;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;

import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/* Контролер формы журнала аутентификации пользователей */

@ViewScoped
@Named
public class AuthLogBean extends LazyLoadDialogBean {
    private static final long serialVersionUID = -2035201127652612778L;

    private Authlog selected;

    private String orientationName = "Portret";
    private boolean orientation;
    private boolean onlyCurPageExp;
    private final Map<String, Boolean> visibleColumns = new HashMap <>();
    private final Map<Integer, String> columns = new HashMap <>();

    private final LazyLoadModel<Authlog> lazyModel = new LazyLoadModel(null, this);

    @EJB
    private Configuration conf;
    @EJB
    private AuthLogFacade authLogFacade;

    @Override
    protected void initBean(){
        super.initBean();
        columns.put(0, "colImg");
        columns.put(1, "colDate");
        columns.put(2, "colLogin");
        columns.put(3, "colEvent");
        columns.put(4, "colIP");
        columns.put(5, "colSMS");
        columns.entrySet().stream().forEach(col->visibleColumns.put(col.getValue(), true));
    }

    @Override
    protected BaseLazyLoadFacade getFacade() {
        return authLogFacade;
    }

    @Override
    public LazyLoadModel<Authlog> getLazyDataModel() {
        return lazyModel;
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

    /**
     * Обработка команды очистки журнала
     */
    public void onClearData(){
        Integer countDelete = authLogFacade.clearEvents(dateStart, dateEnd, filters);
        EscomMsgUtils.succesFormatMsg("RemovedEntries", new Object[]{countDelete});
    }

    /**
     * Формирует сообщение для вывода в диалоге подтверждения очистки журнала
     * @return
     */
    public String clearEventsConfirmMsg(){
        Object[] params = new Object[]{countItems(filters)};
        return MessageFormat.format(EscomMsgUtils.getBandleLabel("WillBeDeleted"), params);
    }

    public void onChangeOrientation(){
        orientationName = orientation ? "Landscape" : "Portret";
    }

    /**
     * Установка шрифта для экспорта журнала в PDF
     * @param document
     * @throws IOException
     * @throws BadElementException
     * @throws DocumentException
     */
    public void preProcessPDF(Object document) throws IOException, BadElementException, DocumentException {
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

    public Authlog getSelected() {
        return selected;
    }
    public void setSelected(Authlog selected) {
        this.selected = selected;
    }

}
