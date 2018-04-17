package com.maxfill.escom.beans.system.lazyload;

import com.maxfill.dictionary.Dict;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class LazyLoadModel<T extends Dict> extends LazyDataModel<T>{

    private List<T> datasource;
    private LazyLoadDialogBean bean;

    public LazyLoadModel(List <T> datasource, LazyLoadDialogBean bean) {
        this.datasource = datasource;
        this.bean = bean;
    }

    @Override
    public T getRowData(String rowKey) {
        for(T item : datasource) {
            if(item.getId().equals(rowKey))
                return item;
        }
        return null;
    }

    @Override
    public Object getRowKey(T item) {
        return item.getId();
    }

    public boolean isDataEmpty(){
        return datasource == null || datasource.isEmpty();
    }

    public void removeItem(T item){
        this.datasource.remove(item);
    }

    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
        datasource = bean.loadItems(first, pageSize, sortField, sortOrder, filters);

        int dataSize = bean.countItems();
        this.setRowCount(dataSize);

        return datasource;
    }
}
