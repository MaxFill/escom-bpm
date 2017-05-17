package com.maxfill.escom.beans.partners.groups;

import com.maxfill.dictionary.DictExplForm;
import com.maxfill.facade.PartnersGroupsFacade;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.partners.Partner;
import com.maxfill.facade.PartnersFacade;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.rights.Rights;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;

/* Сервисный бин "Группы контрагентов" */
@Named
@SessionScoped
public class PartnersGroupsBean extends BaseTreeBean<PartnerGroups, PartnerGroups> {
    private static final long serialVersionUID = 6220113121230925868L;
    
    @EJB
    private PartnersGroupsFacade itemsFacade;
    @EJB
    private PartnersFacade partnersFacade;
    
    /* Получение прав доступа для иерархического справочника */
    @Override
    public Rights getRightItem(BaseDict item) {
        if (item == null) return null;
        
        if (!item.isInherits()) {
            return getActualRightItem(item); //получаем свои права 
        }
        
        if (item.getParent() != null) {
            return getRightItem(item.getParent()); //получаем права от родительской группы
        }                     
        
        return getDefaultRights(item);
    }
    
    /* Формирование контента группы контрагента */     
    @Override
    public List<BaseDict> makeGroupContent(PartnerGroups partnerGroup, Integer viewMode) {
        List<BaseDict> cnt = new ArrayList();
        //загружаем в контент группы контрагента
        List<PartnerGroups> groups = itemsFacade.findChilds(partnerGroup);
        groups.stream()
                .forEach(group -> addGroupInCnt(group, cnt)
        );
        if (Objects.equals(viewMode, DictExplForm.EXPLORER_MODE)){
            //загружаем в контент контрагентов 
            List<Partner> partners = partnersFacade.findDetailItems(partnerGroup);
            partners.stream().
                    forEach(staff -> addPartnerInCnt(staff, cnt)
            );
        }
        return cnt;
    }
    
    /* Добавляет группу в контент   */ 
    private void addGroupInCnt(BaseDict group, List<BaseDict> cnts) {
        //Rights rights = makeRightChild(folder, defDocRight);
        //settingRightForChild(folder, rights); //сохраняем права к документам
        cnts.add(group);
    }

    /* Добавляет контрагента в контент   */ 
    public void addPartnerInCnt(BaseDict partner, List<BaseDict> cnts) {
        /*
        Rights rd = defDocRight;
        if (doc.isInherits() && doc.getAccess() != null) { //установлены специальные права и есть в базе данные по правам
            rd = (Rights) JAXB.unmarshal(new StringReader(doc.getAccess()), Rights.class); //Демаршаллинг прав из строки! 
        }
        doc.setRightItem(rd);
        doc.setRightMask(rightService.getAccessMask(doc.getState(), rd, getCurrentUser())); //получаем маску доступа для текущего пользователя  
        */
        cnts.add(partner);
    }

    @Override
    public PartnersGroupsFacade getItemFacade() {
        return itemsFacade;
    }

    @Override
    public List<PartnerGroups> getGroups(PartnerGroups item) {
        return null;
    }
                
    @Override
    public void preparePasteItem(PartnerGroups pasteItem, BaseDict target){        
        pasteItem.setParent((PartnerGroups)target);
    }
    
    /* Возвращает списки зависимых объектов, необходимых для копирования */
    @Override
    public List<List<?>> doGetDependency(PartnerGroups group){
        List<List<?>> dependency = new ArrayList<>();
        dependency.add(group.getChildItems());
        return dependency;
    }
    
    /* Формирует число ссылок на partnerGroups в связанных объектах   */
    @Override
    public void doGetCountUsesItem(PartnerGroups partnerGroups,  Map<String, Integer> rezult){
        rezult.put("Partners", partnerGroups.getDetailItems().size());
        rezult.put("PartnersGroups", partnerGroups.getChildItems().size());
    }    
    
    /* Проверка возможности удаления partnerGroups */
    @Override
    protected void checkAllowedDeleteItem(PartnerGroups partnerGroups, Set<String> errors){
        // Группу контрагентов можно удалить без ограничений, т.к. контрагенты не удаляются, а только ссылки на них
    }

    /* Обработка события перемещения подчинённых объектов при перемещение группы контрагента в корзину */
    @Override
    protected void moveDetailItemsToTrash(BaseDict ownerItem, Set<String> errors) {          
        // При перемещение группы контрагента в корзину ничего с контрагентами делать не нужно 
    }
    
    /* Обработка события удаление подчинённых объектов при удалении группы контрагента*/
    @Override
    protected void deleteDetails(PartnerGroups partnerGroups) {
        // При удалении группы удалять контрагентов не нужно!
    }
    
    @Override
    public Class<PartnerGroups> getItemClass() {
        return PartnerGroups.class;
    }

    @Override
    public Class<PartnerGroups> getOwnerClass() {
        return null;
    }
    
    @FacesConverter("groupsPartnerConvertor")
    public static class groupsPartnersConvertors implements Converter {
    
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
         if(value != null && value.trim().length() > 0) {
             try {         
                 PartnersGroupsBean bean = EscomBeanUtils.findBean("partnersGroupsBean", fc);
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
                return String.valueOf(((PartnerGroups)object).getId());
            }
            else {
                return "";
            }
        }      
    }
}