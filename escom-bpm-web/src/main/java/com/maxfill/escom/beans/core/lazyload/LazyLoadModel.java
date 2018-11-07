package com.maxfill.escom.beans.core.lazyload;

import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.Dict;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LazyLoadModel<T extends Dict> extends LazyDataModel<T>{
    private static final long serialVersionUID = 7191520294306205834L;

    private List<T> datasource;
    private final LazyLoadBean bean;

    public LazyLoadModel(List <T> datasource, LazyLoadBean bean) {
        this.datasource = datasource;
        this.bean = bean;
    }

    @Override
    public T getRowData(String rowKey) {
        Integer id = Integer.valueOf(rowKey);
        return datasource.stream().filter(row->Objects.equals(row.getId(), id)).findFirst().orElse(null);
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
        datasource = bean.onLoadItems(first, pageSize, sortField, sortOrder, filters);

        int dataSize = bean.countItems();
        this.setRowCount(dataSize);
        return datasource;
    }
}
