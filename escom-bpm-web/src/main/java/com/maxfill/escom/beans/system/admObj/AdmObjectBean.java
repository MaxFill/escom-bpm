
package com.maxfill.escom.beans.system.admObj;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.BaseDict;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.context.FacesContext;

/* Контролер формы администрирования объекта */
@Named
@ViewScoped
public class AdmObjectBean extends BaseViewBean{
    private static final long serialVersionUID = -7160350484665477991L;    
    
    private BaseDict replaceItem;
    private BaseDict sourceItem;
    private Map<String, Integer> rezultUpdate;
    private BaseTableBean itemBean;

    @Inject
    private SessionBean sessionBean;

    @Override
    public void onBeforeOpenCard(){
        if (sourceItem == null){
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            String beanName = params.get("beanName");
            itemBean = sessionBean.getItemBeanByClassName(beanName);
            Integer itemId = Integer.valueOf(params.get("itemId"));
            sourceItem = itemBean.findItem(itemId);
        }
    }

    /* Вычисление числа ссылок на объект в связанных объектах */
    public Set<Map.Entry<String, Integer>> countUsesItem() {
        Map<String, Integer> rezult = new HashMap<>();
        itemBean.doGetCountUsesItem(sourceItem, rezult);
        return rezult.entrySet();
    }
    
    /* Выбор в селекторе элемента для замены   */
    public void onSelectChangeItem(SelectEvent event) {
        if (event.getObject() == null){ return;}
        List<BaseDict> selectedItems = (List<BaseDict>) event.getObject();
        if (!selectedItems.isEmpty()){
            replaceItem = selectedItems.get(0);
        }
    }
    
    /* Обработка события замены объекта в связанных объектах  */
    public void onReplaceItem(){ 
        if (replaceItem != null) {
            int count = itemBean.replaceItem(sourceItem, replaceItem);
            EscomMsgUtils.succesFormatMsg("ReplaceCompleted", new Object[]{count});
            PrimeFaces.current().ajax().update("centerFRM");
        } else {
            EscomMsgUtils.errorMsg("DoNotSpecifyValueReplacement");
        }
    }

    /* gets & sets */

    public BaseTableBean getItemBean() {
        return itemBean;
    }

    public BaseDict getReplaceItem() {
        return replaceItem;
    }

    public BaseDict getSourceItem() {
        return sourceItem;
    }

    @Override
    public String getFormName() {
        return DictDlgFrmName.FRM_OBJECT_ADMIN;
    }
    
    public Map<String, Integer> getRezultUpdate() {
        return rezultUpdate;
    }
    public void setRezultUpdate(Map<String, Integer> rezultUpdate) {
        this.rezultUpdate = rezultUpdate;
    }

}
