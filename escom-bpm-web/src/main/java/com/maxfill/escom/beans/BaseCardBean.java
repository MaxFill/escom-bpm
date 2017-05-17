package com.maxfill.escom.beans;

import com.maxfill.model.BaseDict;
import com.maxfill.model.rights.Right;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.states.State;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.escom.utils.EscomBeanUtils;
import static com.maxfill.escom.utils.EscomBeanUtils.getBandleLabel;
import static com.maxfill.escom.utils.EscomBeanUtils.getMessageLabel;
import com.maxfill.utils.Tuple;
import org.primefaces.component.tabview.Tab;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.extensions.model.layout.LayoutOptions;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/* Базовый бин для карточек объектов */
public abstract class BaseCardBean<T extends BaseDict> extends BaseBean<T> {
    private static final long serialVersionUID = 6864719383155087328L;    
        
    private Boolean isItemRegisted;             //признак того что была выполнена регистрация (для отката при отказе)     
    private Integer rightPageIndex;
    private String itemOpenKey;      
    private Integer typeEdit;                   //режим редактирования записи
    private T editedItem;                       //редактируемый объект 
    private final LayoutOptions cardLayoutOptions = new LayoutOptions();
    
    /* Действия перед сохранением объекта  */
    protected void onBeforeSaveItem(T item) {
    }

    protected abstract void afterCreateItem(T item);

    @Override
    public void onInitBean(){
        EscomBeanUtils.initCardLayout(cardLayoutOptions);
    }
    
    /* При открытии карточки объекта */
    public void onOpenCard(){
        if (getEditedItem() == null){
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            itemOpenKey = params.get("itemOpenKey");
            Tuple<Integer, BaseDict> tuple = appBean.getOpenedItemTuple(itemOpenKey);
            T item = (T) tuple.b;
            setTypeEdit(tuple.a);
            setEditedItem(item);

            //если создание!
            if (getTypeEdit().equals(DictEditMode.INSERT_MODE)){
                addOwnerInGroups(item); //owner_а нужно добавить в группу
                afterCreateItem(item);
                checkCorrectItemRight(item);
            }

            prepareRightsForView(item);

            if (getEditedItem().getItemLogs() != null) {
                getEditedItem().getItemLogs().size();
            }
            doPrepareOpen(item);
        }
    }
    
    /* Подготовка прав доступа к визуализации */
    protected void prepareRightsForView(T item){
        List<Right> itemRights = item.getRightItem().getRights();
        rightFacade.prepareRightsForView(itemRights);
    }
    
    /* Специфические действия перед открытием карточки */
    protected void doPrepareOpen(T item) {
    }

    /* Добавление владельца объекта в группы объекта */
    protected void addOwnerInGroups(T item){ 
        //переопределяется в бине с группами
    }
    
    /* Подготовка к сохранению объекта  */
    public void prepSaveItemAndClose() {        
        Boolean isNeedUpdateExplore = false;
        if (!getTypeEdit().equals(DictEditMode.VIEW_MODE) && isItemChange()) {
            isNeedUpdateExplore = true;
            T item = getEditedItem();
            Set<String> errors = new LinkedHashSet<>();
            checkItemBeforeSave(item, errors);
            if (!errors.isEmpty()) {
                EscomBeanUtils.showErrorsMsg(errors);
                return; 
            }
            onBeforeSaveItem(item);
            switch (getTypeEdit()){
                case DictEditMode.EDIT_MODE: {
                    settingRightItem(item, item.getRightItem());
                    settingRightForChild(item, item.getRightForChild());
                    getItemFacade().addLogEvent(item, getBandleLabel(DictLogEvents.SAVE_EVENT), currentUser);        
                    getItemFacade().edit(item);
                    break;
                }
                case DictEditMode.INSERT_MODE:{
                    getItemFacade().create(item);
                    break;
                }
            }
            onAfterSaveItem(item);
        }
        closeItemForm(isNeedUpdateExplore);
    }
    
    /* Действия сразу после сохранения объекта перед закрытием его карточки */
    protected void onAfterSaveItem(T item){      
    }

    /* Проверка корректности полей объекта перед сохранением  */
    protected void checkItemBeforeSave(T item, Set<String> errors) {
        T parent = (T) item.getParent();
        Integer itemId = item.getId();
        String itemName = item.getName();
        //Проверка на дубль
        Tuple<Boolean, T> tuple = getItemFacade().findByNameExcludeId(itemId, parent, itemName);
        Boolean isFind = tuple.a;
        if (isFind) {
            T findItem = tuple.b;
            Object[] messageParameters = new Object[]{itemName, findItem.getId()};
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("ObjectIsExsist"), messageParameters);
            errors.add(error);
        }
        //Проверка на корректность прав доступа
        checkItemHaveRightEdit(item, errors);                    
    }

    /* Отмена изменений в объекте  */
    public String onCancelItemSave() {
        if (!getTypeEdit().equals(DictEditMode.VIEW_MODE) && isItemChange()) {
            RequestContext.getCurrentInstance().execute("PF('confirm').show();");
        } else {
            return doFinalCancelSave();
        }
        return "";
    }

    /* Отмена изменений в объекте, завершающие действия  */
    protected String doFinalCancelSave() {
        if (Boolean.TRUE.equals(isItemRegisted)) {
            numeratorService.doRollBackRegistred(getEditedItem(), getItemFacade().getFRM_NAME());
            isItemRegisted = false;
        }
        return closeItemForm(false);  //закрыть форму объекта
    }
        
    /* Отмена пользователем изменений из диалога сохранения*/
    public String onFinalCancelSave() {
        return doFinalCancelSave();
    }
    
    /* Закрытие формы карточки объекта */
    private String closeItemForm(Boolean isNeedUpdate) {
        clearLockItem();
        RequestContext.getCurrentInstance().closeDialog(new Tuple(isNeedUpdate, itemOpenKey));
        return "/view/index?faces-redirect=true";
    }

    /* Закрытие служебных диалогов, открываемых с формы */
    public String onClose() {
        RequestContext.getCurrentInstance().closeDialog(null);        
        return "/view/index?faces-redirect=true";
    }
    
    /* Получение и сохранение размеров формы */
    public void handleResize(org.primefaces.extensions.event.ResizeEvent event) { 
        Double x = event.getWidth() + 14;
        Double y = event.getHeight() + 14;
        sessionBean.saveFormSize(getItemObjName(), x, y);
    }
     
    /* Сброс блокировок объекта  */
    private void clearLockItem(){
        T item = getEditedItem();
        appBean.deleteOpenedItem(itemOpenKey);  //удаление из буфера открытых объектов
        String itemKey = item.getItemKey();
        appBean.deleteLockItem(itemKey);        //удаление из буфера заблокированных объектов
    }

    /* Изменение страницы аккардиона с правами доступа на карточке объекта */
    public void onRightTabChange(TabChangeEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        Tab tab = event.getTab();
        String activeIndexValue = params.get(tab.getParent().getClientId(context) + "_tabindex");
        Integer tabId = Integer.parseInt(activeIndexValue);
        setRightPageIndex(tabId);
    }

    /* ПЕЧАТЬ: Подготовка бланка объекта для печати. Вызов с печатной формы */
    public void onPreViewItemCard() {
        printService.doPrint(getEditedItem());
    }

    /* ПРАВА ДОСТУПА: открытие карточки для создание нового права к объекту  */
    public void onAddRight(State state) {
        //getSessionBean().addSourceBean(this.toString(), this);
        EscomBeanUtils.openRightCard(DictEditMode.INSERT_MODE, state, "");
    }
    
    /* ПРАВА ДОСТУПА: открытие карточки для редактирования права объекта  */
    public void onEditRight(Right right) {
        Integer hashCode = right.hashCode();
        String keyRight = hashCode.toString();
        sessionBean.addSourceRight(keyRight, right);
        EscomBeanUtils.openRightCard(DictEditMode.EDIT_MODE, right.getState(), keyRight);
    }      
    
    /* ПРАВА ДОСТУПА: удаление права из редактируемого объекта  */
    public void onDeleteRight(Right right) {
        getEditedItem().getRightItem().getRights().remove(right);
        setIsItemChange(true);
    }

    /* ПРАВА ДОСТУПА: Обработка события закрытия карточки редактирования права объекта  */
    public void onCloseRightCard(SelectEvent event) {
        Tuple<Boolean, Right> tuple = (Tuple)event.getObject();
        Boolean isChange = tuple.a;
        if (isChange) {
            setIsItemChange(true);
            Right right = tuple.b;
            if (right != null){
                getEditedItem().getRightItem().getRights().add(right);
            }
            checkCorrectItemRight(getEditedItem());
        }                
    }

    /* ПРАВА ДОСТУПА: Выполняет проверку на наличие у объекта права на редактирование */
    private void checkItemHaveRightEdit(T item, Set<String> errors){
        if (item.isInherits()) return;
        Rights rights = item.getRightItem();
        for (Right right : rights.getRights()){
            if (right.isUpdate()){
                return;
            }
        }        
        StringBuilder sb = new StringBuilder();
        sb.append(getMessageLabel("ObjectDontHaveRightEdit")).append(getMessageLabel("CheckRights"));
        errors.add(sb.toString());
    }
    
    private void checkCorrectItemRight(T item){
        Set<String> errors = new LinkedHashSet<>();
        checkItemHaveRightEdit(item, errors);
        if (!errors.isEmpty()) {
            EscomBeanUtils.showErrorsMsg(errors);
        }
    }
    
    /* ПРАВА ДОСТУПА: Возвращает список прав к объекту в конкретном состоянии
     * Права объекта, после того как они актуализированы, хранятся в поле rightItem объекта   */
    public List<Right> getRightItemForState(T item, State state) {
        List<Right> rights = item.getRightItem().getRights().stream()
                .filter(right -> state.equals(right.getState()))                
                .collect(Collectors.toList());
        return rights;
    }

    /* ПРАВА ДОСТУПА: */
    public boolean isHaveRightChangeRight() {
        return isHaveRightChangeRight(editedItem);                
    }
    
    /* ПРАВА ДОСТУПА: */ 
    public boolean isHaveRightEdit() {
        return isHaveRightEdit(editedItem);                
    }    
    
    /* ПРОЧИЕ МЕТОДЫ */

    /* При изменении в карточке объекта опции "Наследование прав"  */
    public void onInheritsChange(ValueChangeEvent event) {
        Boolean inherits = (Boolean) event.getNewValue();
        if (inherits) { //если галочка установлена, то нужно скопировать права от владельца              
            Rights rights = getRightItem(getEditedItem());
            settingRightItem(getEditedItem(), rights);
            rightFacade.prepareRightsForView(rights.getRights());
            EscomBeanUtils.SuccesMsgAdd("RightIsParentCopy", "RightIsParentCopy");
        }
    }

    /* Открытие окна сканирования */
    public void onShowScan(){
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", 900);
        options.put("height", 700);
        options.put("maximizable", true);
        options.put("closable", true);
        options.put("closeOnEscape", true);
        RequestContext.getCurrentInstance().openDialog("scanning", options, null);
    }
    
    public String getCancelBtnName() {
        return "itemCard:btnCancel";
    }

    /* Формирует строку заголовка карточки объекта. Вызов с экранной формы  */
    public String makeCardHeader() {
        StringBuilder sb = new StringBuilder(EscomBeanUtils.getBandleLabel(getItemFacade().getMetadatesObj().getBundleName()));
        sb.append(": ");
        switch (getTypeEdit()){
            case DictEditMode.VIEW_MODE:{
                sb.append(getEditedItem().getName());                
                sb.append(" <").append(EscomBeanUtils.getBandleLabel("ReadOnly")).append(">");;
                break;
            }
            case DictEditMode.EDIT_MODE:{
                sb.append(getEditedItem().getName());                
                sb.append(" <").append(EscomBeanUtils.getBandleLabel("Edited")).append(">");
                break;
            }
            case DictEditMode.INSERT_MODE:{
                sb.append(EscomBeanUtils.getBandleLabel("New"));                
                sb.append(" <").append(EscomBeanUtils.getBandleLabel("Create")).append(">");
                break;
            }
        }
        return sb.toString();
    }
    
    /**
     * Формирует текст сообщения о том, что редактируемый объект актуален или не
     * актуален
     *
     * @return
     */
    public String getActualInfo() {
        String msg;
        if (getEditedItem().isActual()) {
            msg = EscomBeanUtils.getBandleLabel("ActualInfo");
        } else {
            msg = EscomBeanUtils.getBandleLabel("NoActualInfo");
        }
        return msg;
    }

    public boolean isReadOnly(){
        return getTypeEdit().equals(DictEditMode.VIEW_MODE);
    }    

    public String getItemObjName(){
        return getItemFacade().getFRM_NAME().toLowerCase();
    }
    
    /* GET & SET  */
        
    public Integer getRightPageIndex() {
        return rightPageIndex;
    }
    public void setRightPageIndex(Integer rightPageIndex) {
        this.rightPageIndex = rightPageIndex;
    }

    public String getItemOpenKey() {
        return itemOpenKey;
    }
    public void setItemOpenKey(String itemOpenKey) {
        this.itemOpenKey = itemOpenKey;
    }
    
    public LayoutOptions getCardLayoutOptions() {
        return cardLayoutOptions;
    }
    
    public Integer getTypeEdit() {
        return typeEdit;
    }
    public void setTypeEdit(Integer typeEdit) {
        this.typeEdit = typeEdit;
    }
    
    public T getEditedItem() {
        return editedItem;
    }
    public void setEditedItem(T item) {
        this.editedItem = item;
    }
}