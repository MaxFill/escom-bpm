
package com.maxfill.escom.beans.system.admObj;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.BaseDict;
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
public class AdmObjectBean extends BaseDialogBean{    
    private static final long serialVersionUID = -7160350484665477991L;    
    
    private BaseDict replaceItem;
    private BaseDict sourceItem;
    private Map<String, Integer> rezultUpdate;
    private BaseExplBean itemBean;

    @Inject
    private SessionBean sessionBean;

    @Override
    protected void initBean(){        
    }
    
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
    
    @Override
    public String onCloseCard(){
        return super.onFinalCloseCard(null);
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
            itemBean.replaceItem(sourceItem, replaceItem);
            EscomMsgUtils.succesMsg("ReplaceCompleted");
        } else {
            EscomMsgUtils.errorMsg("DoNotSpecifyValueReplacement");
        }
    }

    /* gets & sets */

    public BaseExplBean getItemBean() {
        return itemBean;
    }

    public BaseDict getReplaceItem() {
        return replaceItem;
    }

    public BaseDict getSourceItem() {
        return sourceItem;
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
