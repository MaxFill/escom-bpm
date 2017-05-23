
package com.maxfill.escom.beans.system.admObj;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.BaseDict;
import org.primefaces.event.SelectEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.context.FacesContext;

/*  Диалог администрирования объекта */
 
@Named
@ViewScoped
public class AdmObjectBean extends BaseDialogBean{    
    private static final long serialVersionUID = -7160350484665477991L;    
    
    private BaseDict replaceItem;
    private BaseDict sourceItem;
    private Map<String, Integer> rezultUpdate;
    
    @Override
    protected void initBean(){        
    }
    
    @Override
    public void onOpenCard(){
        if (sourceItem == null){
            super.onOpenCard(); 
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            Integer itemId = Integer.valueOf(params.get("itemId"));
            sourceItem = getSourceBean().findItem(itemId);
        }
    }
    
    @Override
    public String onCloseCard(){
        return super.onFinalCloseCard(null);
    }

    /* Вычисление числа ссылок на объект в связанных объектах */
    public Set<Map.Entry<String, Integer>> countUsesItem() {
        Map<String, Integer> rezult = new HashMap<>();
        getSourceBean().doGetCountUsesItem(sourceItem, rezult);
        return rezult.entrySet();
    }
    
    /* Выбор в селекторе элемента для замены   */
    public void onSelectChangeItem(SelectEvent event) {
        if (event.getObject() == null){ return;}
        List<BaseDict> selectedItems = (List<BaseDict>) event.getObject();
        if (selectedItems != null){
            replaceItem = selectedItems.get(0);
        }
    }
    
    /* Обработка события замены объекта в связанных объектах  */
    public void onReplaceItem(){ 
        if (replaceItem != null) {
            getSourceBean().replaceItem(sourceItem, replaceItem);            
            EscomBeanUtils.SuccesMsgAdd("Successfully", "ReplaceCompleted");
        } else {
            EscomBeanUtils.ErrorMsgAdd("Error", "DoNotSpecifyValueReplacement", "");
        }
    }

    public BaseDict getReplaceItem() {
        return replaceItem;
    }

    public BaseDict getSourceItem() {
        return sourceItem;
    }

    @Override
    public BaseExplBean getSourceBean() {
        return (BaseExplBean) super.getSourceBean(); 
    }    

    @Override
    protected String getFormName() {
        return DictDlgFrmName.FRM_OBJECT_ADMIN;
    }
    
    public Map<String, Integer> getRezultUpdate() {
        return rezultUpdate;
    }
    public void setRezultUpdate(Map<String, Integer> rezultUpdate) {
        this.rezultUpdate = rezultUpdate;
    }

}
