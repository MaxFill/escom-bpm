package com.maxfill.facade;

import com.google.gson.Gson;
import com.maxfill.model.users.UserLog;
import com.maxfill.model.users.User;
import com.maxfill.model.companies.Company;
import com.maxfill.model.departments.Department;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.posts.Post;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.dictionary.DictRights;
import com.maxfill.model.BaseDict;
import com.maxfill.model.users.UserStates;
import com.maxfill.services.ldap.LdapUsers;
import com.maxfill.services.ldap.LdapUtils;
import com.maxfill.services.users.UsersService;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.AuthenticationException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.StringUtils;

/* Пользователи */
@Stateless
public class UserFacade extends BaseDictFacade<User, UserGroups, UserLog, UserStates> {
 
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
    @EJB    
    private UsersService usersService;
    
    public UserFacade() {
        super(User.class, UserLog.class, UserStates.class);
    }

    @Override
    public Class<User> getItemClass() {
        return User.class;
    }

    @Override
    public void create(User user) {
        updateUserInfoInRealm(user);
        super.create(user);
    }

    @Override
    public void edit(User user) {
        updateUserInfoInRealm(user);
        super.edit(user);
    }
    
    @Override
    protected void detectParentOwner(User user, BaseDict owner){
        user.setOwner(null);
        user.setParent(null);
        if (owner == null) return;
        if (!user.getUsersGroupsList().contains((UserGroups)owner)){
            user.getUsersGroupsList().add((UserGroups)owner);            
        } 
    }
    
    private void updateUserInfoInRealm(User user){
        String login = user.getLogin();
        String pwl = user.getPwl();
        if (StringUtils.isNotBlank(login) && StringUtils.isNotBlank(pwl)){
            usersService.addUserInRealm(login, pwl);
        }
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
    public List<User> findActualDetailItems(UserGroups group){
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
        User user = createItem(getAdmin(), null, null);
        onUpdateUserFIO(user, name);
        user.setLogin(login);
        user.setPhone(phone);
        user.setEmail(email);
        user.setLDAPname(LDAPname); 
        create(user);        
        return user;
    }     
    
    public User getAdmin(){
        return find(DictRights.USER_ADMIN_ID);
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
     
    /** 
     * Выполняет проверку логина и пароля пользователя 
     * Если учётные данные корректны, то возвращает User иначе null 
     * @param login
     * @param password
     * @return 
     **/
    public User checkUserLogin(String login, char[] password){        
        List<User> users = findByLogin(login);
        if (users.isEmpty()) return null;
        User user = users.get(0);
        String pwl = EscomUtils.encryptPassword(String.valueOf(password));
        if (Objects.equals(pwl, user.getPassword())){
            return user;
        }
        if (StringUtils.isNotBlank(user.getLDAPname()) && checkLdapUser(login, password)){
            return user;            
        } 
        return null;
    }

    /* Проверка подключения к LDAP серверу  */
    public boolean checkLdapUser(String userName, char[] password){
        boolean loginCorrect = false;
        try {      
            LdapUtils.initLDAP(userName, String.valueOf(password), configuration.getLdapServer());
            loginCorrect = true;
        } catch (AuthenticationException ex){
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (Exception ex){
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return loginCorrect;
    }
    
    /* Проверка token */
    public User tokenCorrect(String token){
        try {
            Key key = configuration.getSignKey();
            Claims claims = Jwts.parser()         
                .setSigningKey(key)
                .parseClaimsJws(token).getBody();
            Integer uesrId = Integer.valueOf(claims.getId());
            String subject = claims.getSubject();
            Date dateExpir = claims.getExpiration();
            if (dateExpir.before(new Date())) return null; //token просрочен
            User user = find(uesrId);
            if (user == null) return null; //пользователь не найден
            String login = user.getLogin() + "/" + user.getPassword();
            if (!Objects.equals(login, subject)){
                user = null;
            }
            return user;
        } catch (MalformedJwtException | ClaimJwtException | SignatureException | UnsupportedJwtException ex){
            LOGGER.log(Level.SEVERE, null, ex);
        } 
        return null;
    }
    
    public String makeJsonToken(Map<String, String> loginMap) throws UnsupportedEncodingException{        
        Map<String,String> tokenMap = new HashMap<>();
        tokenMap.put("token", "");
        String login = loginMap.get("login");
        String password = loginMap.get("pwl");
        User user = checkUserLogin(login, password.toCharArray());
        if (user == null) return null;

        Date expirationDate = DateUtils.addMounth(new Date(), 1);
        Key key = configuration.getSignKey();
        String jwt = Jwts.builder()
                .setId(user.getId().toString())
                .setIssuer("http://localhost/")
                .setSubject(login + "/" + EscomUtils.encryptPassword(password))
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
        tokenMap.put("token", jwt);
        Gson gson = new Gson();
        return gson.toJson(tokenMap, Map.class);
    }
}