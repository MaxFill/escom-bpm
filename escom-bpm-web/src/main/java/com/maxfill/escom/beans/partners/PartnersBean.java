package com.maxfill.escom.beans.partners;

import com.maxfill.facade.PartnersFacade;
import com.maxfill.model.partners.Partner;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.BaseDict;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.numPuttern.NumeratorPattern;
import org.primefaces.model.TreeNode;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.SessionScoped;
import org.apache.commons.lang3.StringUtils;

/* Сервисный бин "Контрагенты" */
@Named
@SessionScoped
public class PartnersBean extends BaseExplBeanGroups<Partner, PartnerGroups>{
    private static final long serialVersionUID = -6099934518557372507L;
    
    @EJB 
    private PartnersFacade itemsFacade;    
    @EJB 
    private DocFacade docFacade;     
    
    @Override
    public SearcheModel initSearcheModel() {
        return new PartnersSearche();
    }
    
    @Override
    public PartnersFacade getItemFacade() {
        return itemsFacade;
    }

    @Override
    public BaseExplBean getDetailBean(){
        return null;
    }      
    
    /* Установка специфичных атрибутов контрагента при его создании */
    @Override
    public void setSpecAtrForNewItem(Partner item, Map<String, Object> params){
        NumeratorPattern numeratorPattern = getMetadatesObj().getNumPattern();
        String number = numeratorService.doRegistrNumber(item, numeratorPattern, null, new Date());
        item.setCode(number);
    }
    
    /* Контрагента нужно копировать в случае если он вставляется не в группу или если в ту же группу. В других случаях только добавление ссылки */
    @Override
    public boolean isNeedCopyOnPaste(Partner sourceItem, BaseDict recipient){
        if (!(recipient instanceof PartnerGroups)){
            return true;
        }
        if (sourceItem.getPartnersGroupsList().contains((PartnerGroups)recipient)){
            return true;
        }
        return false;
    }
    
    @Override
    public void preparePasteItem(Partner pasteItem, Partner sourceItem, BaseDict recipient){
        super.preparePasteItem(pasteItem, sourceItem, recipient);
        if (!isNeedCopyOnPaste(pasteItem, recipient)){
            addItemToGroup(pasteItem, recipient);
        }
    } 
    
    @Override
    public boolean addItemToGroup(Partner partner, BaseDict targetGroup){
        if (partner == null || targetGroup == null || targetGroup.getId().equals(0)) return false;
        
        PartnerGroups group = (PartnerGroups)targetGroup;
        if (!partner.getPartnersGroupsList().contains(group)){
            partner.getPartnersGroupsList().add(group);
            getItemFacade().edit(partner);            
            group.getPartnersList().add(partner);
            return true;
        }
        return false;
    }    

    @Override
    protected void detectParentOwner(Partner partner, BaseDict owner){
        partner.setOwner(null);
        partner.setParent(null);
        if (owner == null || owner.getId().equals(0)) return;
        if (!partner.getPartnersGroupsList().contains((PartnerGroups)owner)){
            partner.getPartnersGroupsList().add((PartnerGroups)owner);            
        } 
    }
    
    @Override
    public List<PartnerGroups> getGroups(Partner partner){
        return partner.getPartnersGroupsList();
    } 
    
    @Override
    public void doGetCountUsesItem(Partner partner,  Map<String, Integer> rezult){
        rezult.put("Documents", docFacade.findDocsByPartner(partner).size());
    }    
    
    @Override
    protected void checkAllowedDeleteItem(Partner partner, Set<String> errors){
        super.checkAllowedDeleteItem(partner, errors);
        if (!docFacade.findDocsByPartner(partner).isEmpty()){
            Object[] messageParameters = new Object[]{partner.getName()};
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("PartnerUsedInDocs"), messageParameters);
            errors.add(error);
        }
    }           
    
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
    public Class<Partner> getItemClass() {
        return Partner.class;
    }

    @Override
    public Class<PartnerGroups> getOwnerClass() {
        return PartnerGroups.class;
    }    

    @Override
    public BaseExplBean getOwnerBean() {
        return null;
    }
    
    public String getTitleName(Partner partner){
        StringBuilder builder = new StringBuilder();        
        if (partner.getType() != null){
           builder.append(partner.getType().getName()).append(" ");
        }
        if (StringUtils.isNotBlank(partner.getName())){
            builder.append(partner);
        }
        if (builder.length() == 0){
            builder.append(EscomBeanUtils.getBandleLabel("NewPartner"));
        }
        return builder.toString();
    }
        
    @FacesConverter("partnersConvertor")
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