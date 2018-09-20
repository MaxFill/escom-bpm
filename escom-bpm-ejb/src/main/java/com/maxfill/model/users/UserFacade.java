package com.maxfill.model.users;

import com.google.gson.Gson;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.companies.CompanyFacade;
import com.maxfill.model.departments.DepartmentFacade;
import com.maxfill.model.users.groups.UserGroupsFacade;
import com.maxfill.model.companies.Company;
import com.maxfill.model.departments.Department;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.posts.Post;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.dictionary.DictRights;
import com.maxfill.model.posts.PostFacade;
import com.maxfill.model.staffs.StaffFacade;
import com.maxfill.model.messages.UserMessagesFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.users.assistants.Assistant;
import com.maxfill.services.ldap.LdapUsers;
import com.maxfill.services.ldap.LdapUtils;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import com.maxfill.utils.ItemUtils;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.AuthenticationException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;

/* Пользователи */
@Stateless
public class UserFacade extends BaseDictFacade<User, UserGroups, UserLog, UserStates>{
 
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
    private UserMessagesFacade messagesFacade;

    public UserFacade() {
        super(User.class, UserLog.class, UserStates.class);
    }

    @Override
    public void create(User user) {
        updateUserInfoInRealm(user);
        super.create(user);
        String welcomMsg = ItemUtils.getMessageLabel("Welcom", configuration.getServerLocale());
        sendSystemMsg(user, welcomMsg);
    }

    @Override
    public void edit(User user) {
        updateUserInfoInRealm(user);
        super.edit(user);
    }
    
    @Override
    protected void detectParentOwner(User user, BaseDict parent, BaseDict owner){
        user.setOwner(null);
        user.setParent(null);
        if (owner == null) return;
        if (!user.getUsersGroupsList().contains((UserGroups)owner)){
            user.getUsersGroupsList().add((UserGroups)owner);            
        } 
    }
    
    private void updateUserInfoInRealm(User user){
        if (user == null) return;
        String login = user.getLogin();
        String pwl = user.getPwl();
        if (StringUtils.isNotBlank(login) && StringUtils.isNotBlank(pwl)){
            //usersService.addUserInRealm(login, pwl);
        }
    }

    /**
     * Ищет пользователей по e-mail
     * @param email
     * @return 
     */
    public List<User> findUserByEmail(String email){
        if (StringUtils.isBlank(email)) return null;
        getEntityManager().getEntityManagerFactory().getCache().evict(User.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> cq = builder.createQuery(User.class);
        Root<User> c = cq.from(User.class);
        Predicate crit1 = builder.equal(c.get(User_.email), email);
        cq.select(c).where(builder.and(crit1));
        TypedQuery<User> q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    /**
     * Поиск пользователей по Main Staff    
     * @param mainStaff
     * @return 
     */
    public List<User> findUserByMainStaff(Staff mainStaff){        
        getEntityManager().getEntityManagerFactory().getCache().evict(User.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> cq = builder.createQuery(User.class);
        Root<User> c = cq.from(User.class);
        Predicate crit1 = builder.equal(c.get(User_.staff), mainStaff);
        cq.select(c).where(builder.and(crit1));
        TypedQuery<User> q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }
    
    /**
     * Ищет пользователя по login
     * @param login
     * @return 
     */
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

    /**
     * Ищет пользователей по дефолтной папке
     * @param folder
     * @return 
     */
    public List<User> findUsersByInbox(Folder folder){
        getEntityManager().getEntityManagerFactory().getCache().evict(User.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> cq = builder.createQuery(User.class);
        Root<User> root = cq.from(User.class);        
        Predicate crit1 = builder.equal(root.get(User_.inbox), folder);
        Predicate crit2 = builder.equal(root.get("deleted"), false);
        cq.select(root).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /**
     * Проверка e-mail пользователя на дубликат
     * Возвращает TRUE если в базе уже есть пользователель с указанным e-mail
     * exclUserId - исключить из проверки id пользователя
     * @param exclUserId
     * @param email
     * @return 
     */
    public boolean checkEmailDuplicate(Integer exclUserId, String email){
        getEntityManager().getEntityManagerFactory().getCache().evict(User.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> cq = builder.createQuery(User.class);
        Root<User> c = cq.from(User.class);
        Predicate crit1 = builder.equal(c.get(User_.email), email);
        Predicate crit2 = builder.notEqual(c.get("id"), exclUserId);
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);
        return !q.getResultList().isEmpty();
    }

    @Override
    public List<User> findActualDetailItems(UserGroups group, int first, int pageSize, String sortField, String sortOrder, User currentUser){
        //TODO нужно сделать сортировку
        //slist = list.stream().sorted(Comparator.comparing(Student::getAge)).collect(Collectors.toList());
        UserGroups freshGroup = userGroupsFacade.find(group.getId());
        return freshGroup.getDetailItems().stream().filter(user -> !user.isDeleted() && user.isActual()).collect(Collectors.toList());        
    }
    
    @Override
    public Long findCountActualDetails(UserGroups group){
        UserGroups freshGroup = userGroupsFacade.find(group.getId());
        return freshGroup.getDetailItems().stream().filter(user -> !user.isDeleted()).count();        
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
        User admin = getAdmin();
        Post post = postFacade.onGetPostByName(ldapUser.getPost(), admin);
        Company company = companyFacade.onGetCompanyByName(ldapUser.getCompany(), admin);
        Department department = departmentFacade.onGetDepartamentByName(company, ldapUser.getDepartament());        
        UserGroups mainGroup = userGroupsFacade.onGetGroupByName(ldapUser.getPrimaryGroupName(), admin);
        User user = doCreateUser(mainGroup, ldapUser.getName(), ldapUser.getLogin(), ldapUser.getPhone(), ldapUser.getMail(), ldapUser.getDistinguishedName());
        staffFacade.createStaff(department, post, user);
        
        for (String groupName : ldapUser.getGroups()){
            UserGroups userGroup = userGroupsFacade.onGetGroupByName(groupName, admin);
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
        Post post = postFacade.onGetPostByName(ldapUser.getPost(), getAdmin());
        Company company = companyFacade.onGetCompanyByName(ldapUser.getCompany(), getAdmin());
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
        User user = createItem(getAdmin(), null, null, null);
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
    
    public boolean isAdmin(User user){
        return Objects.equals(user, getAdmin());
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
    
    /* Проверка JWT token */
    public User tokenCorrect(String token){
        try {
            Key key = configuration.getSignKey();
            /*
             info: ключ меняется автоматически при перезапуске сервера,
             при этом все ранее выданные токены становятся недействительными!
             */
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

    /**
     * Создание токена JWT для пользователя
     * @param loginMap
     * @return 
     * @throws java.io.UnsupportedEncodingException
     */
    public String makeJsonToken(Map<String, String> loginMap) throws UnsupportedEncodingException{
        String login = loginMap.get("login");
        String password = loginMap.get("pwl");
        User user = checkUserLogin(login, password.toCharArray());
        if (user == null) return null;

        Map<String,String> tokenMap = new HashMap<>();
        tokenMap.put("token", "");

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

    /**
     * Отправка сообщения пользователю
     * @param receiver
     * @param msg
     */
    public void sendSystemMsg(User receiver, String msg) {
        messagesFacade.createSystemMessage(receiver, msg, new StringBuilder(), new HashMap<>());
    }

    /* Дополнения при выполнении поиска пользователей через форму поиска */
    @Override
    protected void addLikePredicates(Root root, List<Predicate> predicates,  CriteriaBuilder cb, Map<String, Object> paramLIKE){
        String param = (String) paramLIKE.get("name");
        if (StringUtils.isNotBlank(param)) {
            predicates.add(
                    cb.or(
                            cb.like(root.<String>get("name"), param),
                            cb.like(root.<String>get("login"), param)
                    )
            );
            paramLIKE.remove("name");
        }
        super.addLikePredicates(root, predicates, cb, paramLIKE);
    }

    /* Дополнения при выполнении поиска через форму поиска */
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder cb, Map<String, Object> addParams){
        String loginName = (String) addParams.get("login");
        if (StringUtils.isNotBlank(loginName)){
            predicates.add(cb.like(root.<String>get("login"), loginName));
        }
    }

    /**
     * Замена пользователя на другого в связанных объектах
     * @param oldItem
     * @param newItem
     * @return
     */
    @Override
    public int replaceItem(User oldItem, User newItem) {
        //TODO не реализовано!!!
        int count = 0;
        return 0;
    }
    
    /**
     * Проверка того, что пользователь является замом руководителя
     * @param chiefId -  id руководителя
     * @param checkUser - проверяемый сотрудник
     * @return - true если сотрудник является замом руководителя и false, если не является таковым 
     */
    public boolean checkAssistant(Integer chiefId, User checkUser){
        User chief = find(chiefId);
        if (chief == null || checkUser == null) return false;
        Assistant assistant = chief.getAssistants().stream()
                .filter(assist->checkUser.equals(assist.getUser()))
                .findFirst().orElse(null);
        return assistant != null;
    }
    
    @Override
    public void remove(User user){
        messagesFacade.removeMessageByUser(user);
        super.remove(user);
    }
    
    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_USERS;
    }

    /**
     * Возвращает локаль пользователя
     * @param user
     * @return 
     */
    public Locale getUserLocale(User user){                    
        if (StringUtils.isNotBlank(user.getLocale())){
            return LocaleUtils.toLocale(user.getLocale());
        } else {
            return configuration.getServerLocale();
        }      
    }
}