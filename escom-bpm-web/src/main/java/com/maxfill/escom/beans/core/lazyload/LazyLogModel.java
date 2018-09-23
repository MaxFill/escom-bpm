package com.maxfill.escom.beans.core.lazyload;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.model.BaseLogItems;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LazyLogModel<T extends BaseLogItems> extends LazyDataModel<T>{
    private static final long serialVersionUID = 7191520294306205834L;

    private List<T> datasource;
    private final BaseCardBean bean;

    public LazyLogModel(List <T> datasource, BaseCardBean bean) {
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

    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
        datasource = bean.loadItemLogs(first, pageSize, sortField, sortOrder);

        int dataSize = bean.getCountItemLogs();
        this.setRowCount(dataSize);

        return datasource;
    }
}
