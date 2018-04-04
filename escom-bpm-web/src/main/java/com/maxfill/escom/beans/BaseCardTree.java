package com.maxfill.escom.beans;

import com.maxfill.dictionary.DictRights;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.model.rights.Right;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.states.State;
import com.maxfill.utils.Tuple;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.faces.event.ValueChangeEvent;
import org.apache.commons.beanutils.BeanUtils;
import org.primefaces.event.SelectEvent;

/* Базовый бин карточек древовидных объектов */
public abstract class BaseCardTree<T extends BaseDict> extends BaseCardBean<T>{
    private static final long serialVersionUID = 1711235249835675543L;
    private static final int TYPE_RIGHT_CHILDS = 1; //права для дочерних
    private static final int TYPE_RIGHT_ITEM = 0;   //права для самого объекта
        
    protected abstract BaseTreeBean getTreeBean();
    private List<Right> rightsChilds;

    /* Возвращает список прав к дочерним объектам в заданном состоянии */
    public List<Right> getRightChildsForState(T item, State state) {
        List<Right> rights = item.getRightForChild().getRights()
                .stream()
                .filter(right -> state.equals(right.getState()))
                .collect(Collectors.toList());
        return rights;
    }

    /* Обработка события изменения наследования дочерних прав объекта */
    public void onInheritsChildsRightChange(ValueChangeEvent event) {
        onItemChange();
        Set<String> errors = new LinkedHashSet<>();
        Boolean inherit = (Boolean) event.getNewValue();
        //ToDo может быть это нужно?
        //checkRightsChilds(getEditedItem(), inherit, errors);
        if (!errors.isEmpty()){
            EscomMsgUtils.showErrorsMsg(errors);
            return;
        }
        if (Boolean.FALSE.equals(inherit)) { // если галочка снята, значит права не наследуются и нужно скопировать права 
            try {
                Rights childRights = new Rights();
                Rights childDef = getTreeBean().getItemFacade().getRightForChild(getEditedItem());
                for(Right rightDef : childDef.getRights()){
                    Right right = new Right();
                    BeanUtils.copyProperties(right, rightDef); 
                    childRights.getRights().add(right);
                }
                getEditedItem().setRightForChild(childRights);
                rightsBean.prepareRightsForView(childRights.getRights());
                EscomMsgUtils.succesMsg("RightIsParentCopy");
            } catch (IllegalAccessException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }                         
        }
        rightsChilds = null;
    }


    /* Проверка на наличие у объекта корректных прав доступа для дочерних объектов */
    /*
    @Override
    protected void checkRightsChilds(T item, Boolean isInheritsAccessChilds, Set<String> errors){
        if (isInheritsAccessChilds && item.getParent() == null){
            errors.add(getMessageLabel("RightsChildInheritIncorrect"));
        }
    }
    */

    /**
     * Возвращает список состояний для подчинённых объектов
     * @return
     */
    protected abstract List<State> getStateForChild();
    
    /* Удаление права доступа к дочерним объектам из списка  */
    public void onDeleteRightChild(Right right) {
        getEditedItem().getRightForChild().getRights().remove(right);
        onItemChange();
    }

    /**
     * Обработка события добавления права в права дочерних объектов
     */
    public void onAddRightChild(){
        Set<String> errors = new HashSet<>();
        BaseDict obj = null;
        switch(typeAddRight){
            case DictRights.TYPE_GROUP :{
                if (selUsGroup == null){
                    errors.add(EscomMsgUtils.getMessageLabel("UserGroupNotSet"));
                } else {
                    obj = selUsGroup;
                }
                break;
            }
            case DictRights.TYPE_USER :{
                if (selUser == null){
                    errors.add(EscomMsgUtils.getMessageLabel("UserNotSet"));
                } else {
                    obj = selUser;
                }
                break;
            }
            case DictRights.TYPE_ROLE :{
                if (selUserRole == null){
                    errors.add(EscomMsgUtils.getMessageLabel("RoleNotSet"));
                } else {
                    obj = selUserRole;
                }
                break;
            }
        }
        if (!errors.isEmpty()) {
            EscomMsgUtils.showErrorsMsg(errors);
        } else {
            Right right = rightsBean.createRight(typeAddRight, obj.getId(), obj.getName(), selState, null);
            rightsChilds.add(right);
            onItemChange();
        }
    }
    
    /* Подготовка к визуализации прав доступа к дочерним объектам */
    @Override
    protected void prepareRightsForView(T item){
        super.prepareRightsForView(item);
        getTreeBean().getItemFacade().makeRightForChilds(item);
        rightsBean.prepareRightsForView(item.getRightForChild().getRights());
    }
    
    @Override
    protected void onBeforeSaveItem(T item) {
        Rights newChildsRight = item.getRightForChild();
        if (item.isInheritsAccessChilds()) { //если галочка установлена, значит права наследуются                         
            getItemFacade().saveAccessChild(getEditedItem(), "");
        } else {
            getItemFacade().saveAccessChild(getEditedItem(), newChildsRight.toString()); //сохраняем права в XML
        }
        super.onBeforeSaveItem(item);
    }

    @Override
    public Integer getRightColSpan(){
        return 8;
    }

    public List <Right> getRightsChilds() {
        if (rightsChilds==null){
            rightsChilds = getEditedItem().getRightForChild().getRights();
        }
        return rightsChilds;
    }
    public void setRightsChilds(List <Right> rightsChilds) {
        this.rightsChilds = rightsChilds;
    }
}
