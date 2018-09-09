package com.maxfill.services.ldap;

import com.maxfill.model.users.User;
import com.maxfill.model.users.UserFacade;
import com.maxfill.services.*;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.utils.DateUtils;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import org.apache.commons.lang3.StringUtils;

@Stateless
public class LdapTimer extends BaseTimer<LdapSettings>{    
    @EJB 
    private UserFacade userFacade;

    public LdapTimer() {
        super(LdapSettings.class);
    }
    
    @Override
    public ServicesEvents doExecuteTask(Services service, LdapSettings settings){
        LOG.log(Level.INFO, "Executing LDAP task!");
        StringBuilder detailInfo = new StringBuilder("");        
        Date startDate = new Date();
        detailInfoAddRow("The service started in " + DateUtils.dateToString(startDate, DateFormat.SHORT, DateFormat.MEDIUM, conf.getServerLocale()));

        ServicesEvents selectedEvent = new ServicesEvents();
        selectedEvent.setServiceId(service);
        selectedEvent.setDateStart(startDate);
        selectedEvent.setResult(RESULT_FAIL);
        try {
            if (settings.getUpdateUsers()){
                detailInfoAddRow("The mode is set to: ADD_AND_UPDATE_ALL");
            } else {
                detailInfoAddRow( "The mode is set to: ONLY_ADD_NEW_USERS");
            }
            List<LdapUsers> ldapUsers = doLoadUsers(detailInfo, Boolean.FALSE, settings);
            doUpdateUser(ldapUsers, detailInfo, settings); 
            selectedEvent.setResult(RESULT_SUCCESSFULLY);
        } finally{
            Date finishDate = new Date();
            detailInfoAddRow("The service finished in " + DateUtils.dateToString(finishDate, DateFormat.SHORT, DateFormat.MEDIUM, conf.getServerLocale() ));
            selectedEvent.setDetails(detailInfo.toString());
            selectedEvent.setDateFinish(finishDate);
            servicesEventsFacade.create(selectedEvent);
            service.getServicesEventsList().add(selectedEvent); 
            return selectedEvent;           
        }        
    }
    
    /**
     * Получение пользователей из LDAP
     * @param detailInfo
     * @param isManualStart
     * @param settings
     * @return 
     */
    public List<LdapUsers> doLoadUsers(StringBuilder detailInfo, Boolean isManualStart, LdapSettings settings){
        List<LdapUsers> ldapUsers = new ArrayList<>();
        try {
            LdapContext ctx = LdapUtils.initLDAP(settings.getLdapUsername(), settings.getLdapPassword(), settings.getLdapAdServer());
            if (ctx == null){
                detailInfoAddRow("Connect LDAP is fail!");
                return null;
            }
            detailInfoAddRow("Connect LDAP is completed!");
            List<SearchResult> srLdapUsers = LdapUtils.findAccounts(ctx, settings.getLdapSearchBase(), settings.getLdapSearcheGroup());
            detailInfoAddRow("Load users from LDAP is completed!");
            for (SearchResult result : srLdapUsers){
                String sAMAccountName = (String) result.getAttributes().get("sAMAccountName").get();
                LOG.log(Level.INFO, "Load from AD sAMAccountName = {0}", sAMAccountName);
                
                String login = (String) result.getAttributes().get("cn").get();
                LOG.log(Level.INFO, "Load from AD cn = {0}", login);
                
                String userPrincipalName = "";
                if (result.getAttributes().get("userPrincipalName") != null){
                    userPrincipalName = (String) result.getAttributes().get("userPrincipalName").get();
                }
                LOG.log(Level.INFO, "Load from AD userPrincipalName = {0}", userPrincipalName);
                
                String memberOf = (String) result.getAttributes().get("memberOf").get();
                LOG.log(Level.INFO, "Load from AD memberOf = {0}", memberOf);
                
                String distinguishedName = (String) result.getAttributes().get("distinguishedName").get();
                LOG.log(Level.INFO, "Load from AD distinguishedName = {0}", distinguishedName);
                
                String name = "";
                if (result.getAttributes().get("displayName") != null){
                    name = (String) result.getAttributes().get("displayName").get();
                }    
                String department = "";
                if (result.getAttributes().get("department") != null){
                    department = (String) result.getAttributes().get("department").get();
                }
                String company = "";
                if (result.getAttributes().get("company") != null){
                    company = (String) result.getAttributes().get("company").get();
                }
                String mail = "";
                if (result.getAttributes().get("mail") != null){
                    mail = (String) result.getAttributes().get("mail").get();
                }
                String phone = "";
                if (result.getAttributes().get("telephoneNumber") != null){
                    phone = (String) result.getAttributes().get("telephoneNumber").get();
                }
                String post = "";
                if (result.getAttributes().get("title") != null){
                    post = (String) result.getAttributes().get("title").get();
                }
                String primaryGroupSID = LdapUtils.getPrimaryGroupSID(result);
                String primaryGroupName = LdapUtils.findGroupBySID(ctx, settings.getLdapSearchBase(), primaryGroupSID);
                if (name.trim().isEmpty()){
                    name = login;
                }
                LdapUsers ldapUser = new LdapUsers();
                ldapUser.setLogin(login);
                ldapUser.setName(name);
                ldapUser.setUserPrincipalName(userPrincipalName);
                ldapUser.setsAMAccountName(sAMAccountName); 
                ldapUser.setGroups(LdapUtils.makeUserGroups(memberOf));
                ldapUser.setDistinguishedName(distinguishedName);
                ldapUser.setDepartament(department);
                ldapUser.setCompany(company);
                ldapUser.setMail(mail);
                ldapUser.setPhone(phone);
                ldapUser.setPost(post);
                ldapUser.setPrimaryGroupName(primaryGroupName);
                ldapUsers.add(ldapUser);
            }
        } catch (NamingException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return ldapUsers;
    }
    
    /**
     * Обновление пользователей, полученными данными из LDAP
     * @param detailInfo 
     */    
    private void doUpdateUser(List<LdapUsers> ldapUsers, StringBuilder detailInfo, LdapSettings settings){
        List<User> users = userFacade.findAll(userFacade.getAdmin());        
        for(LdapUsers ldapUser : ldapUsers){
            User user = isNewUser(ldapUser, users);
            if (user == null){
                detailInfoAddRow("Create new user=" + ldapUser.getLogin());
                userFacade.createUserFromLDAP(ldapUser);
            } else {
                if (settings.getUpdateUsers()){
                    detailInfoAddRow("Update user=" + ldapUser.getLogin());
                    userFacade.updateUserFromLDAP(user, ldapUser);
                }
            }
        }
    }
    
    /**
     * Определяет нужно добавлять или обновлять пользователя
     * @param ldapUser
     * @param users
     * @return 
     */
    private User isNewUser(LdapUsers ldapUser, List<User> users){
        for(User user : users){
            String ldapLogin = ldapUser.getLogin().trim();
            String userLogin = user.getLogin().trim();
            if (Objects.equals(user.getLDAPname(), ldapUser.getDistinguishedName())) {
                return user;
            }
            if (StringUtils.isBlank(user.getLDAPname()) && Objects.equals(ldapLogin, userLogin)){
                return user;
            }
        }
        return null;
    }

}
