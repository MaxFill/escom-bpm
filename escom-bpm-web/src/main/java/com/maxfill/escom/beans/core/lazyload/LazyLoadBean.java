package com.maxfill.escom.beans.core.lazyload;

import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.model.Dict;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.facade.BaseLazyLoadFacade;
import com.maxfill.model.BaseDict;
import java.util.ArrayList;
import java.util.Comparator;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.model.SortOrder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.faces.component.UIComponent;
import javax.faces.event.AjaxBehaviorEvent;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;

/**
 * Базовый бин реализующий методы ленивой загрузки списков данных из таблиц БД
 * с фильтрацией
 * @param <T>
 */
public abstract class LazyLoadBean<T extends Dict> extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = 8355055695644413851L;

    protected LazyLoadModel<T> lazyModel;
    protected List<T> checkedItems = new ArrayList<>();
    protected T selected;
    
    /* Атрибуты для фильтра */
    protected Date dateStart;
    protected Date dateEnd;
    protected Map<String,Object> filters = new HashMap <>();

    protected abstract BaseLazyLoadFacade getFacade();    
    
    protected final Map<String, Function<BaseDict, ?>> extractors = new HashMap<>();          

    protected final Map<String, Boolean> visibleColumns = new HashMap <>();
    protected final Map<Integer, String> columns = new HashMap <>();
    
    @Override
    protected void initBean() {
        extractors.put("name", BaseDict::getName);
        extractors.put("iconName", BaseDict::getIconName);   
        extractors.put("companyName", BaseDict::getCompanyName);
        extractors.put("docTypeName", BaseDict::getDocTypeName);
        extractors.put("regNumber", BaseDict::getRegNumber);
        extractors.put("itemDate", BaseDict::getItemDate);
        extractors.put("stateName", BaseDict::getStateName);
        extractors.put("dateChange", BaseDict::getDateChange);
        extractors.put("dateCreate", BaseDict::getDateCreate);
        extractors.put("authorName", BaseDict::getAuthorName);
        extractors.put("login", BaseDict::getLogin);
        extractors.put("email", BaseDict::getEmail);
        extractors.put("employeeFIO", BaseDict::getEmployeeFIO);
        extractors.put("postName", BaseDict::getPostName);
        extractors.put("planExecDate", BaseDict::getPlanExecDate);
        extractors.put("beginDate", BaseDict::getBeginDate);
        extractors.put("endDate", BaseDict::getEndDate);
        extractors.put("fullName", BaseDict::getFullName);
        extractors.put("code", BaseDict::getCode);
        
        columns.put(0, "colCheck");
        columns.put(1, "colIcon");        
        columns.put(2, "colName");
        initColumns();
        columns.put(7, "colStateIcon");
        columns.put(8, "colDateChange");
        columns.put(9, "colDateCreate");
        columns.put(10, "colAuthor");
        columns.put(11, "colButton");
        super.initBean(); 
    }    

    protected void initColumns(){
    }
    
    public List<BaseDict> sortDetails(List<BaseDict> items, String sortField, SortOrder sortOrder){
        if (extractors.containsKey(sortField)){            
            if (sortField.toLowerCase().contains("date")){
                Function<BaseDict, Date> extract = (Function<BaseDict, Date>)extractors.get(sortField);           

                    if (sortOrder.equals(SortOrder.ASCENDING)){
                        return items.stream()
                                .sorted(Comparator.comparing(extract, nullsFirst(naturalOrder())))
                                .collect(Collectors.toList());
                    } else {
                        return items.stream()
                                .sorted(Comparator.comparing(extract, nullsFirst(naturalOrder())).reversed())
                                .collect(Collectors.toList());
                    }
            } else {
                Function<BaseDict, String> extract = (Function<BaseDict, String>)extractors.get(sortField);

                    if (sortOrder.equals(SortOrder.ASCENDING)){
                        return items.stream()
                                .sorted(Comparator.comparing(extract, nullsFirst(naturalOrder())))
                                .collect(Collectors.toList());
                    } else {
                        return items.stream()
                                .sorted(Comparator.comparing(extract, nullsFirst(naturalOrder())).reversed())
                                .collect(Collectors.toList());
                    }
            }
        }
        return items;
    }
    
    public void onRowSelect(SelectEvent event){
        selected = (T) event.getObject();
    }
    
    public int countItems(){
        return getFacade().countItems(makeFilters(filters));
    }

    public int deleteItems(){
        return getFacade().deleteItems(makeFilters(filters));
    }

    public List<T> loadItems(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
        this.filters = filters;
        return getFacade().findItemsByFilters(first, pageSize, sortField, sortOrder.name(), makeFilters(filters));
    }

    public void refreshLazyData(){
        lazyModel = null;
    }

    public void removeItemFromData(T item){
        getLazyDataModel().removeItem(item);
        new LazyLoadModel<T>(null, this);
    }

    public boolean checkedItemsEmpty(){
        return CollectionUtils.isEmpty(checkedItems);
    }
    
    public LazyLoadModel getLazyDataModel(){
        if (lazyModel == null){
            lazyModel = new LazyLoadModel(null, this);
        }
        return lazyModel;
    }
    
    protected Map<String,Object> makeFilters(Map<String, Object> filters){
        return filters;
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
     * Возвращает флаг видимости столбца по его имени
     * @param column
     * @return
     */
    public boolean isVisibleColumn(String column){
        if (!visibleColumns.containsKey(column)) return true;
        return visibleColumns.get(column);
    }
    
    /* *** GETS & SETS *** */

    public Map<String, Function<BaseDict, ?>> getExtractors() {
        return extractors;
    }
    
    public T getSelected() {
        return selected;
    }
    public void setSelected(T selected) {
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

    public List <T> getCheckedItems() {
        return checkedItems;
    }
    public void setCheckedItems(List <T> checkedItems) {
        this.checkedItems = checkedItems;
    }
}
