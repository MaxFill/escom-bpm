package com.maxfill.escom.beans.system.lazyload;

import com.maxfill.dictionary.Dict;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.facade.base.BaseLazyLoadFacade;
import com.maxfill.utils.DateUtils;
import org.primefaces.model.SortOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class LazyLoadDialogBean<T extends Dict> extends BaseDialogBean {

    protected Map<String,Object> filters;
    protected Date dateStart;
    protected Date dateEnd;

    @Override
    protected void initBean() {
        dateEnd = DateUtils.clearDate(DateUtils.addDays(new Date(), 1));
        dateStart = DateUtils.addDays(dateEnd, -3);
    }

    public int countItems(Map<String,Object> filters){
        this.filters = filters;
        return getFacade().countEvents(dateStart, dateEnd, filters);
    }

    public List<T> loadItems(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
        this.filters = filters;
        return getFacade().findItemsByPeriod(dateStart, dateEnd, first, pageSize, sortField, sortOrder.name(), filters);
    }

    public void refreshData(){
        getLazyDataModel().refresh();
    }

    public void removeItemFromData(T item){
        getLazyDataModel().removeItem(item);
    }

    protected abstract BaseLazyLoadFacade getFacade();

    public abstract LazyLoadModel getLazyDataModel();

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
