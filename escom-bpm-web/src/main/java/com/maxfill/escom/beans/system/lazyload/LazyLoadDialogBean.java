package com.maxfill.escom.beans.system.lazyload;

import com.maxfill.dictionary.Dict;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.facade.base.BaseLazyLoadFacade;
import com.maxfill.utils.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.model.SortOrder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LazyLoadDialogBean<T extends Dict> extends BaseDialogBean {

    protected Map<String,Object> filters;
    protected Date dateStart;
    protected Date dateEnd;
    protected LazyLoadModel<T> lazyModel;
    protected List<T> checkedItems;

    @Override
    protected void initBean() {
        dateEnd = DateUtils.clearDate(DateUtils.addDays(new Date(), 1));
        dateStart = DateUtils.addDays(dateEnd, -3);
    }

    public int countItems(){
        return getFacade().countItems(makeFilters(filters));
    }

    public int deleteItems(){
        return getFacade().deleteItems(makeFilters(filters));
    }

    public List<T> loadItems(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
        this.filters = filters;
        return getFacade().findItemsByPeriod(first, pageSize, sortField, sortOrder.name(), makeFilters(filters));
    }

    public void refreshData(){
        lazyModel = null;
    }

    public void removeItemFromData(T item){
        getLazyDataModel().removeItem(item);
        new LazyLoadModel<T>(null, this);
    }

    protected abstract BaseLazyLoadFacade getFacade();

    public boolean checkedItemsEmpty(){
        return CollectionUtils.isEmpty(checkedItems);
    }

    public LazyLoadModel getLazyDataModel(){
        if (lazyModel == null){
            lazyModel = new LazyLoadModel(null, this);
        }
        return lazyModel;
    }

    protected abstract String getFieldDateCrit();

    protected Map<String,Object> makeFilters(Map<String, Object> filters){
        if (filters == null){
            filters = new HashMap <>();
            this.filters = filters;
        }
        if (dateStart != null || dateEnd != null) {
            Map <String, Date> dateFilters = new HashMap <>();
            dateFilters.put("startDate", dateStart);
            dateFilters.put("endDate", dateEnd);
            filters.put(getFieldDateCrit(), dateFilters);
        }
        return filters;
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
