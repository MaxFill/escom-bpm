package com.maxfill.facade;

import com.maxfill.model.users.UserLog;
import com.maxfill.model.users.User;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.model.departments.Department;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.posts.Post;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.utils.SysParams;
import com.maxfill.services.ldap.LdapUsers;
import com.maxfill.utils.EscomUtils;
import com.maxfill.utils.Tuple;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.StringUtils;

/* Пользователи */
@Stateless
public class UserFacade extends BaseDictFacade<User, UserGroups, UserLog> {
    protected static final Logger LOG = Logger.getLogger(UserFacade.class.getName());    
    @EJB    
    private PostFacade postFacade;
    @EJB    
    private StaffFacade staffFacade;
    @EJB    
    private CompanyFacade companyFacade;
    @EJB    
    private DepartmentFacade departmentFacade;
    @EJB    
    private UserGroupsFacade userGroupsFacade;

    public UserFacade() {
        super(User.class, UserLog.class);
    }
    
    /* Установка специфичных атрибутов при создании нового пользователя  */
    @Override
    public void setSpecAtrForNewItem(User user, Map<String, Object> params) {
    }
    
    @Override
    public String getFRM_NAME() {
        return DictObjectName.USER.toLowerCase();
    }        
    
    /* Ищет пользователя по login  */
    public List<User> findByLogin(String login){
        getEntityManager().getEntityManagerFactory().getCache().evict(User.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> cq = builder.createQuery(User.class);
        Root<User> c = cq.from(User.class);        
        Predicate crit1 = builder.equal(c.get("login"), login);
        cq.select(c).where(builder.and(crit1));        
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /* Возвращает обновлённый список контрагентов для группы контрагентов  */
    @Override
    public List<User> findDetailItems(UserGroups group){
        UserGroups freshGroup = userGroupsFacade.find(group.getId());
        List<User> detailItems = freshGroup.getDetailItems().stream().filter(user -> !user.isDeleted()).collect(Collectors.toList());
        return detailItems;
    }
    
    /* Ищет пользователя по login исключая ID указанного пользователя  */
    public List<User> findByLoginExcludeId(String login, Integer userId){
        getEntityManager().getEntityManagerFactory().getCache().evict(User.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> cq = builder.createQuery(User.class);
        Root<User> c = cq.from(User.class);        
        Predicate crit1 = builder.equal(c.get("login"), login);
        Predicate crit2 = builder.notEqual(c.get("id"), userId);
        cq.select(c).where(builder.and(crit1, crit2));        
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /* Ищет пользователей по Staff  */
    public List<User> findUsersByStaff(Staff staff){
        getEntityManager().getEntityManagerFactory().getCache().evict(User.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> cq = builder.createQuery(User.class);
        Root<User> c = cq.from(User.class);        
        Predicate crit1 = builder.equal(c.get("staff"), staff);
        cq.select(c).where(builder.and(crit1));        
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /* Создание пользователя из службы интеграции с LDAP  */
    public void createUserFromLDAP(LdapUsers ldapUser){
        Post post = postFacade.onGetPostByName(ldapUser.getPost());
        Company company = companyFacade.onGetCompanyByName(ldapUser.getCompany());
        Department department = departmentFacade.onGetDepartamentByName(company, ldapUser.getDepartament());        
        UserGroups mainGroup = userGroupsFacade.onGetGroupByName(ldapUser.getPrimaryGroupName());
        User user = doCreateUser(mainGroup, ldapUser.getName(), ldapUser.getLogin(), ldapUser.getPhone(), ldapUser.getMail(), ldapUser.getDistinguishedName());
        staffFacade.createStaff(department, post, user);
        
        for (String groupName : ldapUser.getGroups()){
            UserGroups userGroup = userGroupsFacade.onGetGroupByName(groupName);
            userGroup.getUsersList().add(user);
            user.getUsersGroupsList().add(userGroup);
            edit(user);
        } 
    }
    
    /* Обновление пользователя из службы интеграции с LDAP  */
    public void updateUserFromLDAP(User user, LdapUsers ldapUser){
        if (StringUtils.isNotBlank(ldapUser.getMail())){
            user.setEmail(ldapUser.getMail());
        }
        if (StringUtils.isNotBlank(ldapUser.getPhone())){
            user.setPhone(ldapUser.getPhone());
        }
        Post post = postFacade.onGetPostByName(ldapUser.getPost());
        Company company = companyFacade.onGetCompanyByName(ldapUser.getCompany());
        Department department = departmentFacade.onGetDepartamentByName(company, ldapUser.getDepartament());
        
        List<Staff> staffs = staffFacade.findStaffsByUser(user);        
        if (!staffs.isEmpty()){
            staffFacade.createStaff(department, post, user);
        } else {
            Staff staff = staffs.get(0);
            staffFacade.updateStaff(staff, department, post, user);
        }
                
        onUpdateUserFIO(user, ldapUser.getName());
        edit(user);  
    }  
    
    /* Создание пользователя из LDAP  */
    private User doCreateUser(UserGroups mainGroup, String name, String login, String phone, String email, String LDAPname){
        User user = createItem(getAdmin());
        onUpdateUserFIO(user, name);
        user.setLogin(login);
        user.setPhone(phone);
        user.setEmail(email);
        user.setLDAPname(LDAPname); 
        create(user);        
        LOG.log(Level.INFO, "Create user = {0}", login);
        return user;
    } 
    
    public User getAdmin(){
        return find(SysParams.ADMIN_ID);
    }
    
    /* Формирование значений для Фамилии Имени Отчества посредством разбивки строки на части  */
    private void onUpdateUserFIO(User user, String name){
        String firstName = "";
        String secondName = "";
        String lastName = "";
        ArrayList<String> names = EscomUtils.SplitString(name, " ");
        Iterator itr = names.iterator();
        if (itr.hasNext()){                    
            secondName = (String)itr.next(); //фамилия
        }
        if (itr.hasNext()){                    
            firstName = (String)itr.next(); //имя
        }
        if (itr.hasNext()){                    
            lastName = (String)itr.next();  //отчество
        }
        user.setName(name);
        user.setFirstName(firstName);
        user.setSecondName(secondName);
        user.setLastName(lastName);
    }
    
    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_USERS;
    }

    @Override
    public void replaceItem(User oldItem, User newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
