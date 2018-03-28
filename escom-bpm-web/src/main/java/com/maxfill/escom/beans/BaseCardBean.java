package com.maxfill.escom.beans;

import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.model.rights.Right;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.states.State;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.dictionary.DictPrintTempl;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.dictionary.DictStates;
import com.maxfill.escom.utils.EscomBeanUtils;
import static com.maxfill.escom.utils.EscomMsgUtils.getBandleLabel;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.metadates.MetadatesStates;
import com.maxfill.model.users.User;
import com.maxfill.utils.EscomUtils;
import com.maxfill.utils.Tuple;
import java.lang.reflect.InvocationTargetException;

import org.primefaces.PrimeFaces;
import org.primefaces.component.tabview.Tab;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.extensions.model.layout.LayoutOptions;

import javax.annotation.PreDestroy;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.BeanUtils;

/* Базовый бин для карточек объектов */
public abstract class BaseCardBean<T extends BaseDict> extends BaseBean<T> {
    private static final long serialVersionUID = 6864719383155087328L;    
    
    private Boolean isItemRegisted;             //признак того что была выполнена регистрация (для отката при отказе)     
    private Boolean openInDialog;
    private Integer rightPageIndex;
    private String itemOpenKey;      
    private Integer typeEdit;                   //режим редактирования записи
    private T editedItem;                       //редактируемый объект 
    private User owner;
    private State itemCurrentState;
    private final LayoutOptions cardLayoutOptions = new LayoutOptions();

    @Override
    public void onInitBean(){
        EscomBeanUtils.initCardLayout(cardLayoutOptions);
    }

    @PreDestroy
    protected void destroy(){
        System.out.println("card view bean destroy!");
    }

    /**
     * При открытии карточки объекта
     */
    public void onOpenCard(){
        if (getEditedItem() == null){
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            openInDialog = params.containsKey("openInDialog");
            itemOpenKey = params.get("itemId");
            T item;
            if (params.containsKey("openMode")){ //only if enter from url
               item = (T)getItemFacade().find(Integer.valueOf(itemOpenKey));
               if (item == null) return;
               getItemFacade().makeRightItem(item, currentUser);
               typeEdit = Integer.valueOf(params.get("openMode"));
               if (typeEdit.equals(DictEditMode.EDIT_MODE)){
                   String itemKey = item.getItemKey();
                   User user = appBean.whoLockedItem(itemKey); //узнаём, заблокирован ли уже объект
                   if (user != null){
                       typeEdit = DictEditMode.VIEW_MODE;
                   } else {
                       itemOpenKey = appBean.addLockedItem(itemKey, DictEditMode.EDIT_MODE, item, currentUser);
                   }
               }
            } else { //only if enter from bean
                Tuple<Integer, BaseDict> tuple = appBean.getOpenedItemTuple(itemOpenKey);
                item = (T) tuple.b;
                typeEdit = tuple.a;     
            }
            
            setEditedItem(item);
            owner = item.getAuthor();
            itemCurrentState = item.getState().getCurrentState();
            
            //если создание!
            if (getTypeEdit().equals(DictEditMode.INSERT_MODE)){
                addOwnerInGroups(item); //owner_а нужно добавить в группу
                Set<String> errors = new LinkedHashSet<>();
                checkCorrectItemRight(item, errors);
                item.setDateCreate(new Date());
                if (!errors.isEmpty()) {
                   EscomMsgUtils.showErrorsMsg(errors);
                }
                getItemFacade().addLogEvent(item, getBandleLabel(DictLogEvents.CREATE_EVENT), currentUser);
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
        rightsBean.prepareRightsForView(itemRights);
    }
    
    /* Специфические действия перед открытием карточки */
    protected void doPrepareOpen(T item) {
    }

    /* Добавление владельца объекта в группы объекта */
    protected void addOwnerInGroups(T item){ 
        //переопределяется в бине с группами
    }
    
    /* Подготовка к сохранению объекта  */
    public String prepSaveItemAndClose() { 
        if (!doSaveItem()){
            return "";
        }
        return closeItemForm(Boolean.TRUE);
    }
    
    public String prepSaveItemAndPublic(){
       return  prepSaveItemAndClose(); 
    }
    
    public boolean doSaveItem(){
        if (!getTypeEdit().equals(DictEditMode.VIEW_MODE) && isItemChange()) {
            T item = getEditedItem();
            Set<String> errors = new LinkedHashSet<>();
            checkItemBeforeSave(item, errors);
            if (!errors.isEmpty()) {
                EscomMsgUtils.showErrorsMsg(errors);
                return Boolean.FALSE; 
            }
            onBeforeSaveItem(item);
            owner = item.getAuthor();
            switch (getTypeEdit()){
                case DictEditMode.EDIT_MODE: {                    
                    getItemFacade().addLogEvent(item, EscomMsgUtils.getBandleLabel(DictLogEvents.SAVE_EVENT), currentUser);
                    getItemFacade().edit(item);
                    break;
                }
                case DictEditMode.INSERT_MODE:{
                    getItemFacade().create(item);
                    break;
                }
            }
            onAfterSaveItem(item);
            setIsItemChange(Boolean.FALSE);
        }
        EscomMsgUtils.succesMsg("Saved");
        return Boolean.TRUE;
    }
    
    /* Действия перед сохранением объекта  */
    protected void onBeforeSaveItem(T item) {
        Rights newRight = getItemFacade().makeRightItem(item, currentUser);
        if (newRight == null) return;
        if (item.isInherits()) { //если галочка установлена, значит права наследуются                         
            getItemFacade().saveAccess(editedItem, "");
        } else {
            getItemFacade().saveAccess(editedItem, newRight.toString()); //сохраняем права в XML
        }
    }

    /* Действия сразу после сохранения объекта перед закрытием его карточки */
    protected void onAfterSaveItem(T item){      
    }

    /**
     * Проверка корректности полей объекта перед сохранением
     * @param item
     * @param errors
     */
    protected void checkItemBeforeSave(T item, Set<String> errors) {
        checkCorrectItemRight(item, errors);                
        
        //Проверка на дубль
        Tuple<Boolean, T> tuple = getItemFacade().findDublicateExcludeItem(item);
        Boolean isFind = tuple.a;
        if (isFind) {
            T findItem = tuple.b;            
            Object[] messageParameters = new Object[]{item.getName(), findItem.getId()};
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("ObjectIsExsist"), messageParameters);
            errors.add(error);
        }                           
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
            numeratorService.doRollBackRegistred(getEditedItem());
            isItemRegisted = false;
        }
        return closeItemForm(false);  //закрыть форму объекта
    }
        
    /* Отмена пользователем изменений из диалога сохранения*/
    public String onFinalCancelSave() {
        return doFinalCancelSave();
    }
    
    /* Закрытие формы карточки объекта */
    protected String closeItemForm(Boolean isNeedUpdate) {
        attacheService.deleteTmpFiles(currentUser.getLogin());
        clearLockItem();
        if (openInDialog){
            PrimeFaces.current().dialog().closeDynamic(new Tuple(isNeedUpdate, itemOpenKey));
        }
        return "/view/index?faces-redirect=true";
    }

    /* Закрытие служебных диалогов, открываемых с формы */
    public String onClose() {
        RequestContext.getCurrentInstance().closeDialog(null);        
        return "/view/index?faces-redirect=true";
    }
    
    /* Получение и сохранение размеров формы */
    public void handleResize(org.primefaces.extensions.event.ResizeEvent event) { 
        Double width = event.getWidth();
        Double height = event.getHeight();
        Integer x = width.intValue() + 14;
        Integer y = height.intValue() + 14;
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

    /* Обработка события изменения состояния */
    public void onStateChange(){
        getEditedItem().getState().setPreviousState(itemCurrentState);
    }
    
    /* Обработка события изменения Владельца на карточке объекта */
    public void onChangeOwner(SelectEvent event){
        List<User> users = (List<User>) event.getObject();
        if (users.isEmpty()) return;
        User user = users.get(0);
        getEditedItem().doSetSingleRole(DictRoles.ROLE_OWNER, user);
        getEditedItem().setAuthor(user);
        onItemChange();        
    }
    public void onChangeOwner(ValueChangeEvent event){
        User user = (User) event.getNewValue();
        getEditedItem().setAuthor(user);
        getEditedItem().doSetSingleRole(DictRoles.ROLE_OWNER, user);
    }
    
    public boolean lockChangeOwner(){
        if (isReadOnly()) return true;
        if (sessionBean.isUserAdmin()) return false;
        return !currentUser.equals(owner);
    }
    
    /* ПЕЧАТЬ: Подготовка бланка карточки объекта для печати */
    public void onPreViewItemCard() {
        Map<String, Object> params = prepareReportParams();
        ArrayList<Object> dataReport = new ArrayList<>();
        dataReport.add(editedItem);
        doPreViewItemCard(dataReport, params, DictPrintTempl.REPORT_ITEM_CARD);        
    }

    protected void doPreViewItemCard(ArrayList<Object> dataReport, Map<String, Object> parameters, String reportName){
        printService.doPrint(dataReport, parameters, reportName);
        onViewReport(reportName);
    }
    
    /* ПЕЧАТЬ: Подготовка бланка этикетки штрихкода для печати */
    public void onPreViewBarcode() {
        Map<String, Object> params = prepareReportParams();
        ArrayList<Object> dataReport = new ArrayList<>();
        dataReport.add(editedItem);
        doPreViewBarcode(dataReport, params, DictPrintTempl.REPORT_BARCODE);        
    }
    
    protected void doPreViewBarcode(ArrayList<Object> dataReport, Map<String, Object> parameters, String reportName){
        printService.doPrint(dataReport, parameters, reportName);
        onViewReport(reportName);
    }
    
    /* ПЕЧАТЬ: Подготовка параметров отчёта */
    private Map<String, Object> prepareReportParams(){
        Map<String, Object> parameters = new HashMap<>();        
        parameters.put("BARCODE", getBarCode(editedItem));
        parameters.put("USER_LOGIN", currentUser.getLogin());
        String key = getMetadatesObj().getBundleName();
        parameters.put("REPORT_TITLE", EscomMsgUtils.getBandleLabel(key));
        return parameters;
    }
            
    /* ПРАВА ДОСТУПА: открытие карточки для создание нового права к объекту  */
    public void onAddRight(State state) {
        //getSessionBean().addSourceBean(this.toString(), this);
        sessionBean.openRightCard(DictEditMode.INSERT_MODE, state, "", false);
    }
    
    /* ПРАВА ДОСТУПА: открытие карточки для редактирования права объекта  */
    public void onEditRight(Right right) {
        Integer hashCode = right.hashCode();
        String keyRight = hashCode.toString();
        sessionBean.addSourceRight(keyRight, right);
        sessionBean.openRightCard(DictEditMode.EDIT_MODE, right.getState(), keyRight, false);
    }      
    
    /* ПРАВА ДОСТУПА: удаление права из редактируемого объекта  */
    public void onDeleteRight(Right right) {
        getEditedItem().getRightItem().getRights().remove(right);
        onItemChange();
    }

    /* ПРАВА ДОСТУПА: Обработка события закрытия карточки редактирования права объекта  */
    public void onCloseRightCard(SelectEvent event) {
        Tuple<Boolean, Right> tuple = (Tuple)event.getObject();
        Boolean isChange = tuple.a;
        if (isChange) {
            onItemChange();
            Right right = tuple.b;
            if (right != null){
                getEditedItem().getRightItem().getRights().add(right);
            }
            Set<String> errors = new LinkedHashSet<>();
            checkCorrectItemRight(getEditedItem(), errors);
            if (!errors.isEmpty()) {
                EscomMsgUtils.showErrorsMsg(errors);
            }
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
        sb.append(EscomMsgUtils.getMessageLabel("ObjectDontHaveRightEdit")).append(EscomMsgUtils.getMessageLabel("CheckRights"));
        errors.add(sb.toString());
    }    
    
    /* ПРАВА ДОСТУПА: Выполняет проверку на наличие у объекта права на просмотр */
    private void checkItemHaveRightView(T item, Set<String> errors){
        if (item.isInherits()) return;
        Rights rights = item.getRightItem();
        for (Right right : rights.getRights()){
            if (right.isRead()){
                return;
            }
        }        
        StringBuilder sb = new StringBuilder();
        sb.append(EscomMsgUtils.getMessageLabel("ObjectDontHaveRightView")).append(EscomMsgUtils.getMessageLabel("CheckRights"));
        errors.add(sb.toString());
    }
    
    private void checkCorrectItemRight(T item, Set<String> errors){
        checkItemHaveRightEdit(item, errors);
        checkItemHaveRightView(item, errors);
        //checkRightsChilds(item, item.isInheritsAccessChilds(), errors);
    }
    
    /*
    protected void checkRightsChilds(T item, Boolean isInheritsAccessChilds, Set<String> errors){        
    }
    */
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
        return getItemFacade().isHaveRightChangeRight(editedItem);
    }
    
    /* ПРАВА ДОСТУПА: */ 
    public boolean isHaveRightEdit() {
        return getItemFacade().isHaveRightEdit(editedItem);
    }        
    
    /* Возвращает название для заголовка наследования дефолтных прав дочерних объектов */
    public String getInheritsAccessChildName(){
        if (editedItem.isInheritsAccessChilds()){
            return EscomMsgUtils.getMessageLabel("RightsInheritedForChilds");
        } else {
            return EscomMsgUtils.getMessageLabel("RightsNotInheritedForChilds");
        }
    }
    
    public String getInheritsRightName(){
        String inheritsRightName = EscomMsgUtils.getMessageLabel("RightIsInherits");
        if (!editedItem.isInherits()){
            inheritsRightName = EscomMsgUtils.getMessageLabel("RightNotInherits");
        }
        return inheritsRightName;
    }
        
    /* ПРОЧИЕ МЕТОДЫ */

    /* При изменении в карточке объекта опции "Наследование прав"  */
    public void onInheritsChange(ValueChangeEvent event) {
        onItemChange();
        if (Boolean.FALSE.equals((Boolean) event.getNewValue())) { //если галочка снята, значит права не наследуются и нужно скопировать права             
            try {                    
                Rights newRight = new Rights();
                Rights rightDefs = getItemFacade().getRightItem(editedItem, currentUser);
                for(Right rightDef : rightDefs.getRights()){
                    Right right = new Right();
                    BeanUtils.copyProperties(right, rightDef); 
                    newRight.getRights().add(right);
                }
                editedItem.setInherits(Boolean.FALSE);
                editedItem.setRightItem(newRight);
                rightsBean.prepareRightsForView(newRight.getRights());                
                EscomMsgUtils.succesMsg("RightIsParentCopy");
            } catch (IllegalAccessException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public String getCancelBtnName() {
        return "itemCard:btnCancel";
    }

    /* Формирует заголовок карточки объекта. Вызов с экранной формы  */
    public String makeCardHeader() {
        StringBuilder sb = new StringBuilder(EscomMsgUtils.getBandleLabel(getItemFacade().getMetadatesObj().getBundleName()));
        return makeHeader(sb);
    }

    protected String makeHeader(StringBuilder sb){
        sb.append(": ");
        switch (getTypeEdit()){
            case DictEditMode.VIEW_MODE:{
                sb.append(getEditedItem().getCaption());
                sb.append(" <").append(EscomMsgUtils.getBandleLabel("ReadOnly")).append(">");;
                break;
            }
            case DictEditMode.EDIT_MODE:{
                sb.append(getEditedItem().getCaption());
                sb.append(" <").append(EscomMsgUtils.getBandleLabel("Correction")).append(">");
                break;
            }
            case DictEditMode.INSERT_MODE:{
                sb.append(EscomMsgUtils.getBandleLabel("New"));
                sb.append(" <").append(EscomMsgUtils.getBandleLabel("Create")).append(">");
                break;
            }
        }
        return sb.toString();
    }

    /* Формирует текст сообщения о том, что редактируемый объект актуален или не актуален  */
    public String getActualInfo() {
        String msg;
        if (getEditedItem().isActual()) {
            msg = EscomMsgUtils.getBandleLabel("ActualInfo");
        } else {
            msg = EscomMsgUtils.getBandleLabel("NoActualInfo");
        }
        return msg;
    }

    public boolean isReadOnly(){
        return Objects.equals(DictEditMode.VIEW_MODE, getTypeEdit());
    }

    public String getItemObjName(){
        return getItemFacade().getFRM_NAME().toLowerCase();
    }
    
    public String getBarCode(T item){
        Integer serverId = conf.getServerId();
        String barcode = EscomUtils.getBarCode(editedItem, getMetadatesObj(), serverId); 
        return barcode;
    }

    /* GETS & SETS  */
    public User getOwner() {
        return owner;  //пользователь, владелец объекта
    }

    /* Возвращает список состояний доступных объекту из его текущего состояния */
    public List<State> getAvailableStates(){        
        Metadates metaObj = getMetadatesObj();
        List<MetadatesStates> metadatesStates = metaObj.getMetadatesStates();
        List<State> result = metadatesStates.stream()
                .filter(metadatesState -> Objects.equals(itemCurrentState, metadatesState.getStateSource()) 
                        && !DictStates.MOVED_AUTO.equals(metadatesState.getMoveType()))
                .map(metadatesState -> metadatesState.getStateTarget())
                .collect(Collectors.toList());
        if (!result.contains(itemCurrentState)){
            result.add(itemCurrentState);
        }
        return result;
    }
    
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