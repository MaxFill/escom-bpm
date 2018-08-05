package com.maxfill.escom.beans.system.admObj;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.BaseDict;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* Контролер формы администрирования объекта */
@Named
@ViewScoped
public class AdmObjectBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = -7160350484665477991L;    
    
    private BaseDict replaceItem;

    private Map<String, Integer> rezultUpdate;

    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceBean != null){
            setSourceItem(sourceBean.getSourceItem());
        }
    }
    
    /* Вычисление числа ссылок на объект в связанных объектах */
    public Set<Map.Entry<String, Integer>> countUsesItem() {
        Map<String, Integer> rezult = new HashMap<>();
        getItemBean().doGetCountUsesItem(getSourceItem(), rezult);
        return rezult.entrySet();
    }
    
    /* Выбор в селекторе элемента для замены   */
    public void onSelectChangeItem(SelectEvent event) {
        if (event.getObject() == null) return;
        List<BaseDict> selectedItems = (List<BaseDict>) event.getObject();
        if (!selectedItems.isEmpty()){
            replaceItem = selectedItems.get(0);
        }
    }
    
    /* Обработка события замены объекта в связанных объектах  */
    public void onReplaceItem(){ 
        if (replaceItem != null) {
            int count = getItemBean().replaceItem(getSourceItem(), replaceItem);
            MsgUtils.succesFormatMsg("ReplaceCompleted", new Object[]{count});
            PrimeFaces.current().ajax().update("mainFRM:centerFRM");
        } else {
            MsgUtils.errorMsg("DoNotSpecifyValueReplacement");
        }
    }

    /* Gets & Sets */

    public BaseTableBean getItemBean() {        
        return (BaseTableBean)getSourceBean();
    }

    public BaseDict getReplaceItem() {
        return replaceItem;
    }

    @Override
    public String getFormName() {
        return DictFrmName.FRM_OBJECT_ADMIN;
    }
    
    public Map<String, Integer> getRezultUpdate() {
        return rezultUpdate;
    }
    public void setRezultUpdate(Map<String, Integer> rezultUpdate) {
        this.rezultUpdate = rezultUpdate;
    }

}
