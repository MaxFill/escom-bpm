
package com.maxfill.escom.beans;

import com.maxfill.escom.utils.EscomBeanUtils;
import org.primefaces.context.RequestContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Map;

/* Базовый бин для служебных диалогов  */
public abstract class BaseDialogBean implements Serializable{    
    private static final long serialVersionUID = 3712139345846276454L;
    
    @Inject
    protected SessionBean sessionBean;

    private BaseBean sourceBean;
    private boolean itemChange;       //признак изменения записи 
    private String beanName;

    public SessionBean getSessionBean() {
        return sessionBean;
    }
    
    protected void onOpenCard(){
         if (getSourceBean() == null){
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            beanName = params.get("beanName");
            sourceBean = getSessionBean().getSourceBean(getBeanName());
         }
    }
    
    protected abstract String onCloseCard();
     
    protected String onFinalCloseCard(Object param){
        getSessionBean().removeSourceBean(beanName);
        RequestContext.getCurrentInstance().closeDialog(param);
        return "/view/index?faces-redirect=true";
    }
        
    public void onItemChange() {
        setItemChange(Boolean.TRUE);
    }

    /* GETS & SETS */
    
    public boolean isItemChange() {
        return itemChange;
    }
    public void setItemChange(boolean itemChange) {
        this.itemChange = itemChange;
    }

    public String getBeanName() {
        return beanName;
    }
    public void setSourceBean(BaseBean sourceBean) {
        this.sourceBean = sourceBean;
    }

    public String getLabelFromBundle(String key){
        return EscomBeanUtils.getBandleLabel(key);
    }
            
    public BaseBean getSourceBean() {
        return sourceBean;
    }
}
