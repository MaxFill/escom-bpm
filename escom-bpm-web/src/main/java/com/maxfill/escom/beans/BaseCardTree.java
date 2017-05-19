package com.maxfill.escom.beans;

import com.maxfill.escom.utils.EscomBeanUtils;
import static com.maxfill.escom.utils.EscomBeanUtils.getMessageLabel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.rights.Right;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.states.State;
import com.maxfill.utils.Tuple;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.faces.event.ValueChangeEvent;
import org.primefaces.event.SelectEvent;

/* Базовый бин карточек древовидных объектов */
public abstract class BaseCardTree<T extends BaseDict> extends BaseCardBean<T>{
    private static final long serialVersionUID = 1711235249835675543L;
    private static final int TYPE_RIGHT_CHILDS = 1;
    private static final int TYPE_RIGHT_ITEM = 0; 
        
    private Integer typeEditedRight;
        
    protected abstract BaseTreeBean getTreeBean();
    
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
        Boolean inherit = (Boolean) event.getNewValue();
        if (inherit) {
            Set<String> errors = new LinkedHashSet<>();
            getEditedItem().setInheritsAccessChilds(inherit);
            checkRightsChilds(getEditedItem(), errors);
            if (!errors.isEmpty()){
                EscomBeanUtils.showErrorsMsg(errors);
                return;
            }
            getTreeBean().makeRightForChilds(getEditedItem());
            rightFacade.prepareRightsForView(getEditedItem().getRightForChild().getRights());
            onItemChange();
            EscomBeanUtils.SuccesMsgAdd("Successfully", "RightIsParentCopy");
        }
    }
    
    /* Проверка на наличие у объекта корректных прав доступа для дочерних объектов */
    @Override
    protected void checkRightsChilds(T item, Set<String> errors){ 
        if (item.isInheritsAccessChilds() && item.getParent() == null){
            errors.add(getMessageLabel("RightsChildInheritIncorrect"));
        }
    }
    
    /* Удаление права доступа к дочерним объеткам из списка  */
    public void onDeleteRightChild(Right right) {
        getEditedItem().getRightForChild().getRights().remove(right);
        onItemChange();
    }
    
    /* Редактирование права к дочерним объектам */
    public void onEditRightChild(Right right){
        typeEditedRight = TYPE_RIGHT_CHILDS;
        super.onEditRight(right);
    }
    
    /* Создание права к дочерним объектам */
    public void onAddRightChild(State state){
        typeEditedRight = TYPE_RIGHT_CHILDS;
        super.onAddRight(state);
    }
    
    /* Подготовка к визуализации прав доступа к дочерним объектам */
    @Override
    protected void prepareRightsForView(T item){
        super.prepareRightsForView(item);
        getTreeBean().makeRightForChilds(item); 
        rightFacade.prepareRightsForView(item.getRightForChild().getRights());
    }
    
    /* Добавление права для объекта в заданном состоянии */
    @Override
    public void onAddRight(State state){
        typeEditedRight = TYPE_RIGHT_ITEM;
        super.onAddRight(state);
    }
        
    /* Редактирование права объекта */
    @Override
    public void onEditRight(Right right){
        typeEditedRight = TYPE_RIGHT_ITEM;
        super.onEditRight(right);
    }
    
    /* Обработка события закрытия карточки прав доступа */
    @Override
    public void onCloseRightCard(SelectEvent event) {
        switch (typeEditedRight){
            case TYPE_RIGHT_ITEM:{
                super.onCloseRightCard(event);
                break;
            }
            case TYPE_RIGHT_CHILDS:{
                Tuple<Boolean, Right> tuple = (Tuple)event.getObject();
                Boolean isChange = tuple.a;
                if (isChange) {
                    onItemChange();                    
                    Right right = tuple.b;
                    if (right != null){
                        getEditedItem().getRightForChild().getRights().add(right);
                    }
                }            
                break;
            }
        }
    } 
    
    @Override
    protected void onBeforeSaveItem(T item) {
        getTreeBean().settingRightForChild(item, item.getRightForChild());
        super.onBeforeSaveItem(item);
    }    
}
