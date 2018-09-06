package com.maxfill.escom.beans.core.lazyload;

import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.model.Dict;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.facade.BaseLazyLoadFacade;
import com.maxfill.model.BaseDict;
import java.util.ArrayList;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.model.SortOrder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.primefaces.event.SelectEvent;

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
    
    protected final Map<String, Function<T, String>> extractors = new HashMap<>();          
    
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

    /* *** GETS & SETS *** */

    public Map<String, Function<T, String>> getExtractors() {
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
