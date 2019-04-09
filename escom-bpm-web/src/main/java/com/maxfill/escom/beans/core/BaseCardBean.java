package com.maxfill.escom.beans.core;

import com.maxfill.dictionary.*;
import com.maxfill.escom.beans.core.lazyload.LazyLogModel;
import com.maxfill.escom.beans.system.rights.RightsBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.user.UserFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.BaseLogItems;
import com.maxfill.model.core.rights.Right;
import com.maxfill.model.core.rights.Rights;
import com.maxfill.model.core.states.State;
import com.maxfill.model.core.metadates.Metadates;
import com.maxfill.model.core.metadates.MetadatesStates;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.basedict.userGroups.UserGroups;
import com.maxfill.services.attaches.AttacheService;
import com.maxfill.services.numerators.NumeratorService;
import com.maxfill.services.print.PrintService;
import com.maxfill.utils.EscomUtils;
import com.maxfill.utils.Tuple;
import java.lang.reflect.InvocationTargetException;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.faces.context.FacesContext;
import org.apache.commons.beanutils.BeanUtils;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.SortOrder;

/**
 * Базовый бин для работы с единичными объектами
 * Реализует бызовые функции создания, редактирования, валидации и сохранения объектов на карточках
 * @param <T>
 * @param <L>
 */
public abstract class BaseCardBean<T extends BaseDict> extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = 6864719383155087328L;

    @Inject
    protected RightsBean rightsBean;
    @EJB
    protected PrintService printService;
    @EJB
    protected NumeratorService numeratorService;
    @EJB
    protected AttacheService attacheService;
    @EJB
    protected UserFacade userFacade;
    
    private Metadates metadatesObj;             //объект метаданных
    protected Boolean isItemRegisted;           //признак того что была выполнена регистрация (для отката при отказе)     
    private String itemOpenKey;
    private Integer typeEdit;                   //режим редактирования записи
    private T editedItem;                       //редактируемый объект 
    private User owner;
    private State itemCurrentState;
    private List<Right> rights;
    protected Integer typeAddRight = DictRights.TYPE_GROUP;
    protected State selState;
    protected User selUser;
    protected UserGroups selUsGroup;
    protected UserGroups selUserRole;

    protected abstract BaseDictFacade getFacade();    
    
    private final LazyLogModel<BaseLogItems> itemLogs = new LazyLogModel<>(null, this);                
    
    /* *** ОТКРЫТИЕ КАРТОЧКИ *** */
    
    /**
     * Общий метод для всех бинов карточек, вызываемый автоматически перед открытием карточки
     * @param params 
     */
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (params.containsKey("itemId") && getEditedItem() == null){
            itemOpenKey = params.get("itemId");
            T item;
            if (params.containsKey("openMode")){ //only if enter from url
               item = (T) getFacade().find(Integer.valueOf(itemOpenKey));
               if (item == null) return;
               getFacade().makeRightItem(item, getCurrentUser());
               typeEdit = Integer.valueOf(params.get("openMode"));
               if (typeEdit.equals(DictEditMode.EDIT_MODE)){
                   String itemKey = item.getItemKey();
                   User user = appBean.whoLockedItem(itemKey); //узнаём, заблокирован ли уже объект
                   if (user != null){
                       typeEdit = DictEditMode.VIEW_MODE;
                   } else {
                       itemOpenKey = appBean.addLockedItem(itemKey, DictEditMode.EDIT_MODE, item, getCurrentUser());
                   }
               }
            } else { //only if enter from bean
                Tuple<Integer, BaseDict> tuple = appBean.getOpenedItemTuple(itemOpenKey); //получаем объект для редактирования из буфера
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
                   MsgUtils.showErrors(errors);
                }                
            }

            prepareRightsForView(item);
            doPrepareOpen(item);
        }
    }    
    
    /* Подготовка прав доступа к визуализации */
    protected void prepareRightsForView(T item) {
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
    
    /** 
     * Начало сохранения объекта
     * @return 
     */
    public String prepSaveItemAndClose() { 
        if (getTypeEdit().equals(DictEditMode.INSERT_MODE)){
            onItemChange();
        }
        if (!doSaveItem()){
            return "";
        }    
        return closeItemForm(SysParams.EXIT_NEED_UPDATE);
    }
    
    public String prepSaveItemAndPublic(){
       return  prepSaveItemAndClose(); 
    }
    
    public boolean doSaveItem(){
        if (isItemChange) {
            T item = getEditedItem();
            onBeforeSaveItem(item);
            Set<String> errors = new HashSet<>();
            FacesContext context = FacesContext.getCurrentInstance();
            checkItemBeforeSave(item, context, errors);
            if (!errors.isEmpty()) {                
                context.validationFailed();
                MsgUtils.showErrors(errors);
                return Boolean.FALSE; 
            }
            
            owner = item.getAuthor();
            switch (getTypeEdit()){
                case DictEditMode.EDIT_MODE: {                    
                    getFacade().addLogEvent(item, DictLogEvents.CHANGE_EVENT, getCurrentUser());
                    getFacade().edit(item);
                    break;
                }
                case DictEditMode.INSERT_MODE:{
                    getFacade().create(item); 
                    getFacade().addLogEvent(item, DictLogEvents.CREATE_EVENT, getCurrentUser());
                    setTypeEdit(DictEditMode.EDIT_MODE);                    
                    break;
                }
                case DictEditMode.CHILD_MODE:{
                    try { 
                        BaseDict sourceItem = sourceBean.getSourceItem();                        
                        BeanUtils.copyProperties(sourceItem, item);                        
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        errors.add("InternalErrorSavingTask");
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                    break;
                }
            }
            onAfterSaveItem(item);
            isItemChange = false;
        }
        //MsgUtils.succesMsg("ChangesSaved");
        return Boolean.TRUE;
    }
    
    /* Действия перед сохранением объекта  */
    protected void onBeforeSaveItem(T item) {
        Rights newRight = getFacade().makeRightItem(item, getCurrentUser());
        if (newRight == null) return;
        if (item.isInherits()) { //если галочка установлена, значит права наследуются                         
            getFacade().saveAccess(editedItem, "");
        } else {
            getFacade().saveAccess(editedItem, newRight.toString()); //сохраняем права в XML
        }
    }

    /* Действия сразу после сохранения объекта перед закрытием его карточки */
    protected void onAfterSaveItem(T item){}
    
    /**
     * Проверка корректности полей объекта перед сохранением
     * @param item
     * @param context
     * @param errors
     */
    protected void checkItemBeforeSave(T item, FacesContext context, Set<String> errors) {
        checkCorrectItemRight(item, errors);                
        
        //Проверка на дубль
        Tuple<Boolean, T> tuple = getFacade().findDublicateExcludeItem(item);
        Boolean isFind = tuple.a;
        if (isFind) {
            T findItem = tuple.b;            
            Object[] messageParameters = new Object[]{item.getName(), findItem.getId()};
            String error = MessageFormat.format(MsgUtils.getMessageLabel("ObjectIsExsist"), messageParameters);
            errors.add(error);
        }
    }
            
    /* Отмена изменений в объекте  */
    @Override
    public String onCancelItemSave() {
        if (!getTypeEdit().equals(DictEditMode.VIEW_MODE) && isItemChange()) {
            PrimeFaces.current().executeScript("PF('confirmDLG').show();");
        } else {
            return doFinalCancelSave();
        }
        return "";
    }

    /**
     * Обработка события отмены изменений по кнопке в диалоге предупреждения о сохранении изменений
     * @return 
     */ 
    public String onFinalCancelSave() {
        return doFinalCancelSave();
    }
    
    /* Отмена изменений в объекте, завершающие действия  */
    protected String doFinalCancelSave() {
        if (Boolean.TRUE.equals(isItemRegisted)) {
            getNumerator().doRollBackRegistred(getEditedItem());
            isItemRegisted = false;
        }        
        return closeItemForm(SysParams.EXIT_NOTHING_TODO);  //закрыть форму объекта
    }        
        
    /* Закрытие формы карточки объекта */
    protected String closeItemForm(Object exits) {
        attacheService.deleteTmpFiles(getCurrentUser().getLogin());
        clearLockItem();
        return finalCloseDlg(exits);        
    }        
    
    /* Сброс блокировок объекта  */
    private void clearLockItem(){        
        appBean.deleteOpenedItem(itemOpenKey);                  //удаление из буфера открытых объектов        
        appBean.deleteLockItem(getEditedItem().getItemKey());   //удаление из буфера заблокированных объектов
    }

    /* Обработка события изменения состояния */
    public void onStateChange(){
        getEditedItem().getState().setPreviousState(itemCurrentState);
    }
    
    /* Обработка события изменения Владельца на карточке объекта */
    public void onChangeOwner(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<User> users = (List<User>) event.getObject();
        if (users.isEmpty()) return;
        User user = users.get(0);
        getEditedItem().doSetSingleRole(DictRoles.ROLE_OWNER, user.getId());
        getEditedItem().setAuthor(user);
        onItemChange();        
    }
    public void onChangeOwner(ValueChangeEvent event){
        User user = (User) event.getNewValue();
        getEditedItem().setAuthor(user);
        getEditedItem().doSetSingleRole(DictRoles.ROLE_OWNER, user.getId());
    }
    
    public boolean lockChangeOwner(){
        if (isReadOnly()) return true;
        if (sessionBean.isUserAdmin()) return false;
        return !getCurrentUser().equals(owner);
    }
    
    /* *** ПЕЧАТЬ *** */
    
    /* ПЕЧАТЬ: Подготовка бланка карточки объекта для печати */
    public void onPreViewItemCard() {
        Map<String, Object> params = prepareReportParams();
        ArrayList<Object> dataReport = new ArrayList<>();
        dataReport.add(editedItem);
        doPreViewItemCard(dataReport, params, DictPrintTempl.REPORT_ITEM_CARD);        
    }

    protected void doPreViewItemCard(ArrayList<Object> dataReport, Map<String, Object> parameters, String reportName){
        printService.doPrint(dataReport, parameters, reportName);
        sessionBean.onViewReport(reportName);
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
        sessionBean.onViewReport(reportName);
    }
    
    /* ПЕЧАТЬ: Подготовка параметров отчёта */
    @Override
    protected Map<String, Object> prepareReportParams(){
        Map<String, Object> parameters = new HashMap<>();        
        parameters.put("BARCODE", getBarCode());
        parameters.put("USER_LOGIN", getCurrentUser().getLogin());
        String key = getMetadatesObj().getBundleName();
        parameters.put("REPORT_TITLE", MsgUtils.getBandleLabel(key));
        return parameters;
    }

   /* *** ПРАВА ДОСТУПА *** */    
    
    /**
     * Обработка события добавления права в права объекта
     */
    public void onAddRight(){
        Set<String> errors = new HashSet <>();
        BaseDict obj = null;
        switch(typeAddRight){
            case DictRights.TYPE_GROUP :{
                if (selUsGroup == null){
                    errors.add(MsgUtils.getMessageLabel("UserGroupNotSet"));
                } else {
                    obj = selUsGroup;
                }
                break;
            }
            case DictRights.TYPE_USER :{
                if (selUser == null){
                    errors.add(MsgUtils.getMessageLabel("UserNotSet"));
                } else {
                    obj = selUser;
                }
                break;
            }
            case DictRights.TYPE_ROLE :{
                if (selUserRole == null){
                    errors.add(MsgUtils.getMessageLabel("RoleNotSet"));
                } else {
                    obj = selUserRole;
                }
                break;
            }
        }
        if (!errors.isEmpty()) {
            MsgUtils.showErrors(errors);
        } else {
            Right right = rightsBean.createRight(typeAddRight, obj.getId(), obj.getName(), selState, null);
            rights.add(right);
            onItemChange();
        }
    }
    
    /* ПРАВА ДОСТУПА: удаление права из редактируемого объекта  */
    public void onDeleteRight(Right right) {
        rights.remove(right);
        onItemChange();
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
        sb.append(MsgUtils.getMessageLabel("ObjectDontHaveRightEdit")).append(MsgUtils.getMessageLabel("CheckRights"));
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
        sb.append(MsgUtils.getMessageLabel("ObjectDontHaveRightView")).append(MsgUtils.getMessageLabel("CheckRights"));
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
        return getFacade().isHaveRightChangeRight(editedItem);
    }
    
    /* ПРАВА ДОСТУПА: */ 
    public boolean isHaveRightEdit() {
        return getFacade().isHaveRightEdit(editedItem);
    }        
    
    /**
     * Проверяет наличие права на выполнение (нажатие на кнопку)
     * @return 
     */
    public boolean isHaveRightExec() {
        return getFacade().isHaveRightExec(editedItem);
    }  
    
    /* Возвращает название для заголовка наследования дефолтных прав дочерних объектов */
    public String getInheritsAccessChildName(){
        if (editedItem.isInheritsAccessChilds()){
            return MsgUtils.getMessageLabel("RightsInheritedForChilds");
        } else {
            return MsgUtils.getMessageLabel("RightsNotInheritedForChilds");
        }
    }
    
    public String getInheritsRightName(){
        String inheritsRightName = MsgUtils.getMessageLabel("RightIsInherits");
        if (!editedItem.isInherits()){
            inheritsRightName = MsgUtils.getMessageLabel("RightNotInherits");
        }
        return inheritsRightName;
    }
        
    /* ПРОЧИЕ МЕТОДЫ */

    public void addInFavorites(){
        sessionBean.addInFavorites(getEditedItem(), getMetadatesObj());
    }
    
    /* При изменении в карточке объекта опции "Наследование прав"  */
    public void onInheritsChange(ValueChangeEvent event) {
        onItemChange();
        if (Boolean.FALSE.equals((Boolean) event.getNewValue())) { //если галочка снята, значит права не наследуются и нужно скопировать права             
            try {                    
                Rights newRight = new Rights();
                Rights rightDefs = getFacade().getRightItem(editedItem, getCurrentUser());
                for(Right rightDef : rightDefs.getRights()){
                    Right right = new Right();
                    BeanUtils.copyProperties(right, rightDef); 
                    newRight.getRights().add(right);
                }
                editedItem.setInherits(Boolean.FALSE);
                editedItem.setRightItem(newRight);
                rightsBean.prepareRightsForView(newRight.getRights());
                MsgUtils.succesMsg("RightIsParentCopy");
            } catch (IllegalAccessException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        rights = null;
    }    

    /* Формирует заголовок карточки объекта. Вызов с экранной формы  */
    public String makeCardHeader() {
        StringBuilder sb = new StringBuilder(MsgUtils.getBandleLabel(getFacade().getMetadatesObj().getBundleName()));
        return makeHeader(sb);
    }

    protected String makeHeader(StringBuilder sb){
        if (getEditedItem() == null) return "";
        sb.append(": ");
        switch (getTypeEdit()){
            case DictEditMode.VIEW_MODE:{
                sb.append(getEditedItem().getCaption().toUpperCase());
                sb.append(". <").append(MsgUtils.getBandleLabel("ReadOnly").toUpperCase()).append(">");
                break;
            }
            case DictEditMode.CHILD_MODE:{
                sb.append(getEditedItem().getCaption().toUpperCase());
                sb.append(". <").append(MsgUtils.getBandleLabel("Correction").toUpperCase()).append(">");
                break;
            }
            case DictEditMode.EDIT_MODE:{
                sb.append(getEditedItem().getCaption().toUpperCase());
                sb.append(". <").append(MsgUtils.getBandleLabel("Correction").toUpperCase()).append(">");
                break;
            }
            case DictEditMode.INSERT_MODE:{
                sb.append(MsgUtils.getBandleLabel("New").toUpperCase());
                sb.append(". ").append(MsgUtils.getBandleLabel("Mode")).append(": ").append(MsgUtils.getBandleLabel("Create").toUpperCase());
                break;
            }
        }
        return sb.toString();
    }

    /* Формирует текст сообщения о том, что редактируемый объект актуален или не актуален  */
    public String getActualInfo() {
        if (getEditedItem() == null) return "";
        String msg;
        if (getEditedItem().isActual()) {
            msg = MsgUtils.getBandleLabel("ActualInfo");
        } else {
            msg = MsgUtils.getBandleLabel("NoActualInfo");
        }
        return msg;
    }

    /**
     * Определяет дефолтный доступ ко всем полям карточки объекта
     * @return 
     */
    @Override
    public boolean isReadOnly(){
        boolean result = Objects.equals(DictEditMode.VIEW_MODE, getTypeEdit());
        return result;
    }
    
    /**
     * Определяет доступ к полям на вкладке "Информация"
     * @return 
     */
    public boolean isInfoReadOnly(){
        return isReadOnly();
    }
    
    /* Возвращает название для заголовка для вкладки "Права доступа к объекту" */
    public String getRightsForObjectTitle(){
        return MsgUtils.getBandleLabel("Rights");
    }

    /**
     * Формирование штрих-кода 
     * @param item
     * @return 
     */
    public String getBarCode(){
        return EscomUtils.getBarCode(editedItem.getId());         
    }
    
    /**
     * Возвращает признак использования простых карточек
     * @return 
     */
    public boolean simpleCard(){
        boolean flag = sessionBean.getUserSettings().isSimpleCard();
        return flag;
    }
    
    /* *** ЛОГИРОВАНИЕ *** */
    
    public List<BaseLogItems> loadItemLogs(int first, int pageSize, String sortField, SortOrder sortOrder){        
        return getFacade().getItemLogs(getEditedItem(), first, pageSize, sortField, sortOrder.name());
    }
    
    public int getCountItemLogs(){        
        return getFacade().getCountItemLogs(getEditedItem());
    }

    protected Map<String,Object> makeFilters(Map<String, Object> filters){
        filters.put("item", getEditedItem());
        return filters;
    }

    public LazyLogModel<BaseLogItems> getItemLogs() {
        return itemLogs;
    }
               
    
    /* СЛУЖЕБНЫЕ МЕТОДЫ */

    /**
     * Возвращает число столбцов в групповой строке таблицы прав доступа
     * @return
     */
    public Integer getRightColSpan(){
        return 7;
    }

    /**
     * Возвращает список доступных состояний объекта
     * @return 
     */
    public Set<State> getAvailableStates(){         
        List<MetadatesStates> metadatesStates = getMetadatesObj().getMetadatesStates();
        Set<State> result = metadatesStates.stream()               
                .map(state -> state.getStateSource())
                .collect(Collectors.toSet());
        if (!result.contains(itemCurrentState)){
            result.add(itemCurrentState);
        }
        return result;
    }    
    
    /* GETS & SETS */

    public Boolean getIsItemRegisted() {
        return isItemRegisted;
    }
    
    public NumeratorService getNumerator(){
        return numeratorService;
    }
    
    @Override
    public String getFormName() {
        return getFacade().getFRM_NAME().toLowerCase();
    }

    @Override
    public String getFormHeader() {
        return makeCardHeader();
    }
    
    /* Возвращает пользователя, владельца объекта */
    public User getOwner() { return owner; }

    public State getSelState() {
        return selState;
    }
    public void setSelState(State selState) {
        this.selState = selState;
    }

    public List<Right> getRights() {
        if (rights == null){
            rights = getEditedItem().getRightItem().getRights();
        }
        return rights;
    }
    public void setRights(List <Right> rights) {
        this.rights = rights;
    }

    public String getItemOpenKey() {
        return itemOpenKey;
    }
    public void setItemOpenKey(String itemOpenKey) {
        this.itemOpenKey = itemOpenKey;
    }    
    
    public Integer getTypeEdit() {
        return typeEdit;
    }
    public void setTypeEdit(Integer typeEdit) {
        this.typeEdit = typeEdit;
    }

    public Integer getTypeAddRight() {
        return typeAddRight;
    }
    public void setTypeAddRight(Integer typeAddRight) {
        this.typeAddRight = typeAddRight;
    }

    public User getSelUser() {
        return selUser;
    }
    public void setSelUser(User selUser) {
        this.selUser = selUser;
    }

    public UserGroups getSelUsGroup() {
        return selUsGroup;
    }
    public void setSelUsGroup(UserGroups selUsGroup) {
        this.selUsGroup = selUsGroup;
    }

    public UserGroups getSelUserRole() {
        return selUserRole;
    }
    public void setSelUserRole(UserGroups selUserRole) {
        this.selUserRole = selUserRole;
    }

    public T getEditedItem() {
        return editedItem;
    }
    public void setEditedItem(T item) {
        this.editedItem = item;
    }

    public State getItemCurrentState() {
        return itemCurrentState;
    }
    public void setItemCurrentState(State itemCurrentState) {
        this.itemCurrentState = itemCurrentState;
    }

    public void onTabChange(TabChangeEvent event){
    }
    
    /* Получение ссылки на объект метаданных  */
    public Metadates getMetadatesObj() {
        if (metadatesObj == null) {
            metadatesObj = getFacade().getMetadatesObj();
        }
        return metadatesObj;
    }
}