package com.maxfill.escom.beans.users;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.folders.FoldersFacade;
import com.maxfill.model.users.UserFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.users.User;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.escom.beans.core.interfaces.WithDetails;
import com.maxfill.escom.beans.staffs.StaffBean;
import com.maxfill.escom.beans.users.assistants.AssistantBean;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.states.State;
import com.maxfill.model.states.StateFacade;
import com.maxfill.model.users.assistants.Assistant;
import com.maxfill.model.users.assistants.AssistantFacade;
import com.maxfill.utils.EscomUtils;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import org.apache.commons.lang.WordUtils;
import org.primefaces.event.SelectEvent;

/* Контроллер формы "Карточка пользователя */
@Named
@ViewScoped
public class UserCardBean extends BaseCardBeanGroups<User, UserGroups> implements WithDetails{            
    private static final long serialVersionUID = 2031203859450836271L;

    private String password;

    @EJB
    private FoldersFacade folderFacade;
    @EJB
    private AssistantFacade assistantFacade;
    @EJB
    private StateFacade stateFacade;
    
    @Inject
    private AssistantBean assistantBean;
    @Inject
    private StaffBean staffBean;
    
    private List<Assistant> checkedDetails;
    private Assistant selectedDetail;
    private Staff oldStaffvalue;
    
    @Override
    public void doPrepareOpen(User user) {
        if (getTypeEdit().equals(DictEditMode.EDIT_MODE)){
            password = "**********";
        }
        oldStaffvalue = user.getStaff();       
    }

    @Override
    public void onAfterFormLoad(){
        validateStaff(oldStaffvalue);
    }
    
    /**
     * Обработка события изменения телефона
     * @param event
     */
    public void onChangePhone(ValueChangeEvent event){
        String newValue = (String) event.getNewValue();
        if (StringUtils.isNotBlank(newValue) && StringUtils.isBlank(getEditedItem().getMobilePhone())){
            getEditedItem().setMobilePhone(newValue);
            onItemChange();
        }
    }

    /**
     * Обработка события изменения подписи
     * @param event
     */
    public void onEmailSignChange(ValueChangeEvent event){
        onItemChange();
    }

    /**
     * Обработка события выбора дефолтной папки
     * @param event
     */
    public void onInboxSelected(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<Folder> items = (List<Folder>) event.getObject();
        if (items.isEmpty()) return;
        Folder folder = items.get(0);
        onItemChange();
        getEditedItem().setInbox(folder);
        checkFolder(folder);
    }

    /**
     * Проверка выбранной папки
     * @param folder
     */
    public void checkFolder(Folder folder){
        if(!folderFacade.checkRightAddDetail(folder, getEditedItem())) {
            String errMsg = MsgUtils.getMessageLabel("SelectedFolderCantNotAddDocs");
            String checkError = MsgUtils.getValidateLabel("CHECK_ERROR");
            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:folderPanel_item");
            input.setValid(false);
            context.addMessage(input.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR, errMsg, checkError));
            context.validationFailed();
        }
    }

    /**
     * Добавление пользователю прав на изменение папки
     */
    public void addRightChangeFolder(){
        Folder folder = folderFacade.find(getEditedItem().getInbox().getId());
        if (folder.isInherits()){
            MsgUtils.errorFormatMsg("CannotAddRightBecauseInheritsRights", new Object[]{folder.getNameEndElipse()});
            return;
        }
        State state = stateFacade.getValidState();
        folderFacade.addItemRightForUser(folder, getEditedItem(), state);
        MsgUtils.succesMsg("AccessRightsChanged");
    }
    
    /* Формирование отображаемого имени пользователя */    
    public void makeName(){
        getEditedItem().setName(getEditedItem().getShortFIO());
    }

    /* Формирование логина для пользователя */
    public void makeLogin(){ 
        String name = getEditedItem().getName();
        if (StringUtils.isBlank(name)){
            name = getEditedItem().getShortFIO();
            getEditedItem().setName(name);
        }
        String login = name.replace(" ", "").replace(".", "").replace(",", "").toLowerCase();
        login = EscomBeanUtils.rusToEngTranlit(login);        
        getEditedItem().setLogin(WordUtils.capitalize(login));
    }

    /**
     * Обработка события изменения пароля
     * @param event
     * @throws NoSuchAlgorithmException
     */
    public void onChangePassword(ValueChangeEvent event) throws NoSuchAlgorithmException{
        String newValue = (String) event.getNewValue();
        String newPwl = EscomUtils.encryptPassword(newValue);
        String oldPwl = (String) event.getOldValue();
        if (!Objects.equals(newPwl, oldPwl)){
            getEditedItem().setPassword(newPwl);
            getEditedItem().setPwl(newValue);
            onItemChange();
        }
    }
    
    @Override
    protected void addItemInGroup(User item, UserGroups group) {
        if (group == null || group.getId() == 0) return;
        super.addItemInGroup(item, group);
    }
    
    @Override
    public UserFacade getFacade() {
        return userFacade;
    }

    /**
     * Проверка корректности полей перед сохранением
     * @param user
     * @param errors
     */
    @Override
    protected void checkItemBeforeSave(User user, Set<String> errors) {       
        super.checkItemBeforeSave(user, errors);

        String login = user.getLogin();
        Integer userId = user.getId();
        List<User> existUsers = getFacade().findByLoginExcludeId(login, userId);
        if (!existUsers.isEmpty()) {
            errors.add(MessageFormat.format(MsgUtils.getMessageLabel("UserLoginIsExsist"), new Object[]{login}));
        }
        if (user.isDoubleFactorAuth() && StringUtils.isBlank(user.getMobilePhone())){
            errors.add(MessageFormat.format(MsgUtils.getMessageLabel("NeedSetMobilePhone"), new Object[]{}));
        }
    }

    /**
     * Действия перед сохранением пользователя
     * @param user
     */
    @Override
    protected void onBeforeSaveItem(User user){
        if (StringUtils.isBlank(user.getName())){
            user.setName(user.getShortFIO());
        }
        super.onBeforeSaveItem(user);
    }

    @Override
    protected void onAfterSaveItem(User user) {
        super.onAfterSaveItem(user);
        if (user.isNeedChangePwl()){
            String msg = MsgUtils.getMessageLabel("YouNeedChangePassword");
            getFacade().sendSystemMsg(user, msg);
        }
        User employee = getEditedItem();
        Staff staff = getEditedItem().getStaff();
        if (staff == null && oldStaffvalue != null){
            employee = null;
            staff = oldStaffvalue;
        }
        if (staff == null) return;
        staff = staffFacade.find(staff.getId());
        staff.setEmployee(employee);
        staffBean.makeName(staff);
        staffFacade.edit(staff);
    }

    @Override
    public List<UserGroups> getGroups(User item) {
        return item.getUsersGroupsList();
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Обработка события изменения на форме штатной единицы
     * @param event 
     */
    public void onChangeStaff(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<Staff> items = (List<Staff>)event.getObject();
        if (items.isEmpty()) return;
        Staff staff = items.get(0);        
        validateStaff(staff);
        onItemChange();
        getEditedItem().setStaff(staff);
    }    
    
    public void validateStaff(Staff staff){
        if (staff == null) return;
        
        if (staff.getEmployee() != null){
            if (!Objects.equals(staff.getEmployee(), getEditedItem())){
                FacesContext context = FacesContext.getCurrentInstance();
                UIInput input = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:staffPanel_item");            
                input.setValid(false);
                String errMsg = MsgUtils.getMessageLabel("StaffAlreadyAssociated");
                String checkError = MsgUtils.getValidateLabel("CHECK_ERROR");
                context.addMessage(input.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_WARN, errMsg, checkError));
                context.validationFailed();
                onItemChange();
            }
        }
    }
    
    /* Details implementation */
    
    @Override
    public List<Assistant> getDetails(){
        return getEditedItem().getAssistants();
    }

    @Override
    public List<Assistant> getCheckedDetails() {
        return checkedDetails;
    }
    @Override
    public void setCheckedDetails(List checkedDetails) {
        this.checkedDetails = checkedDetails;
    }

    @Override
    public void onDeleteDetail(BaseDict item) {
        getDetails().remove((Assistant)item);
        onItemChange();
    }    
    
    @Override
    public void onDeleteCheckedDetails(){
        getDetails().removeAll(checkedDetails);
        onItemChange();
    }
    
    @Override
    public void onCreateDetail(){
        selectedDetail = assistantFacade.createItem(getCurrentUser(), null, getEditedItem(), new HashMap<>());        
        onOpenDetail(selectedDetail);
    }
    
    @Override
    public void onOpenDetail(BaseDict item){
        setSourceItem(item);        
        assistantBean.prepEditChildItem((Assistant)item, getParamsMap());
    }
    
    @Override
    public void afterCloseDetailItem(SelectEvent event){
        if (event.getObject() == null) return;        
        switch ((String) event.getObject()){
            case SysParams.EXIT_NOTHING_TODO:{
                break;
            }
            case SysParams.EXIT_NEED_UPDATE:{                
                if (selectedDetail.getId() == null){
                    getDetails().add(selectedDetail);
                }
                onItemChange();
                break;
            }
        }         
    }           

    @Override
    public Assistant getSelectedDetail() {
        return selectedDetail;
    }
    @Override
    public void setSelectedDetail(BaseDict selectedDetail) {
        this.selectedDetail = (Assistant)selectedDetail;
    }
    
    /* *** *** */
}