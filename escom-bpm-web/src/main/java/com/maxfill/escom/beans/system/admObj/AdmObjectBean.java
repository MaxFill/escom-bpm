
package com.maxfill.escom.beans.system.admObj;

import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.utils.EscomUtils;
import org.primefaces.event.SelectEvent;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mfilatov
 */
@Named
@ViewScoped
public class AdmObjectBean extends BaseDialogBean{    
    private static final long serialVersionUID = -7160350484665477991L;    
    
    private BaseDict replaceItem;
    private BaseDict sourceItem;
    private Map<String, Integer> rezultUpdate;
    
    /**
     * При открытии карточки объекта
     */
    @Override
    public void onOpenCard(){
        if (sourceItem == null){
            super.onOpenCard(); 
            sourceItem = getSourceBean().getExplorerBean().getCurrentItem();
        }
    }
    
    /**
     * Закрытие карточки
     * @return 
     */
    @Override
    public String onCloseCard(){
        return super.onFinalCloseCard(null);
    }

    /**
     * Вычисление числа ссылок на объект в связанных
     * объектах

     * @return
     */
    public Set<Map.Entry<String, Integer>> countUsesItem() {
        Map<String, Integer> rezult = new HashMap<>();
        getSourceBean().doGetCountUsesItem(sourceItem, rezult);
        return rezult.entrySet();
    }
    
    /**
     * Выбор в селекторе элемента для замены 
     *
     * @param event
     */
    public void onSelectChangeItem(SelectEvent event) {
        if (event.getObject() == null){ return;}
        List<BaseDict> selectedItems = (List<BaseDict>) event.getObject();
        if (selectedItems != null){
            replaceItem = selectedItems.get(0);
        }
    }
    
    /**
     * АДМИНИСТРИРОВАНИЕ ОБЪЕКТОВ: Обработка события замены объекта в связанных
     * объектах
     * 
     */
    public void onReplaceItem(){ 
        if (replaceItem != null) {
            Map<String, Integer> rezult = getSourceBean().getItemFacade().replaceItem(sourceItem, replaceItem);            
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
    
    public Map<String, Integer> getRezultUpdate() {
        return rezultUpdate;
    }
    public void setRezultUpdate(Map<String, Integer> rezultUpdate) {
        this.rezultUpdate = rezultUpdate;
    }
}
