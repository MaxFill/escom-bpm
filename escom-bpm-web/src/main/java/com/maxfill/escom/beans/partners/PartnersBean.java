package com.maxfill.escom.beans.partners;

import com.maxfill.facade.PartnersFacade;
import com.maxfill.model.partners.Partner;
import com.maxfill.model.partners.PartnerModel;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.EscomUtils;
import org.primefaces.model.TreeNode;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  Контрагенты
 * @author mfilatov
 */
@Named
@ViewScoped
public class PartnersBean extends BaseExplBeanGroups<Partner, PartnerGroups>{
    private static final long serialVersionUID = -6099934518557372507L;
    private static final String BEAN_NAME = "partnersBean";
    
    @EJB 
    private PartnersFacade itemsFacade;    
    @EJB 
    private DocFacade docFacade;
        
    public PartnersBean() {
    }    

    @Override
    protected String getBeanName() {
        return BEAN_NAME;
    }

    @Override
    public PartnersFacade getItemFacade() {
        return itemsFacade;
    }

    @Override
    public BaseExplBean getDetailBean(){
        return null;
    }      

    /**
     * Возвращает список групп контрагента
     * @param partner
     * @return 
     */
    @Override
    public List<PartnerGroups> getGroups(Partner partner){
        return partner.getPartnersGroupsList();
    } 
    
    /**
     * Формирует число ссылок на объект в связанных объектах 
     * @param partner
     * @param rezult 
     */
    @Override
    public void doGetCountUsesItem(Partner partner,  Map<String, Integer> rezult){
        rezult.put("Documents", docFacade.findDocsByPartner(partner).size());
    }    
    
    /**
     * Проверка возможности удаления Контрагента
     * @param partner
     */
    @Override
    protected void checkAllowedDeleteItem(Partner partner, Set<String> errors){
        super.checkAllowedDeleteItem(partner, errors);
        if (!docFacade.findDocsByPartner(partner).isEmpty()){
            Object[] messageParameters = new Object[]{partner.getName()};
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("PartnerUsedInDocs"), messageParameters);
            errors.add(error);
        }
    }           
    
    /**
     * Перемещение контрагента из одной группы в другую
     * 
     * @param targetGroup (dropItem)
     * @param partner (dragItem)
     */
    @Override
    public void moveItemToGroup(BaseDict targetGroup, Partner partner, TreeNode sourceNode) {        
        if (sourceNode != null){
            PartnerGroups sourceGroup = (PartnerGroups)sourceNode.getData();
            partner.getPartnersGroupsList().remove(sourceGroup);
        }                             
        partner.getPartnersGroupsList().add((PartnerGroups)targetGroup);
        getItemFacade().edit(partner);
    }    
    
    @Override
    protected BaseDataModel createModel() {
        return new PartnerModel();
    }

    @Override
    public Class<Partner> getItemClass() {
        return Partner.class;
    }

    @Override
    public Class<PartnerGroups> getOwnerClass() {
        return PartnerGroups.class;
    }
    
    @FacesConverter("partnersConvertors")
    public static class partnersConvertors implements Converter {
    
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
         if(value != null && value.trim().length() > 0) {
             try {
                 PartnersBean bean = EscomBeanUtils.findBean("partnersBean", fc);
                 Object searcheObj = bean.getItemFacade().find(Integer.parseInt(value));
                 return searcheObj;
             } catch(NumberFormatException e) {
                 throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not valid"));
             }
         }
         else {
             return null;
         }
        }

        @Override
        public String getAsString(FacesContext fc, UIComponent uic, Object object) {
            if(object != null) {
                return String.valueOf(((Partner)object).getId());
            }
            else {
                return "";
            }
        }      
    }
}