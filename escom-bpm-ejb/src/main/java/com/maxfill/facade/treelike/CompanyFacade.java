package com.maxfill.facade.treelike;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.facade.UserFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.model.companies.CompanyLog;
import com.maxfill.model.companies.CompanyStates;
import com.maxfill.model.departments.Department;
import com.maxfill.model.departments.Department_;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.folders.Folder_;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.staffs.Staff_;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.logging.Logger;

@Stateless
public class CompanyFacade extends BaseDictFacade<Company, Company, CompanyLog, CompanyStates>{
    protected static final Logger LOG = Logger.getLogger(CompanyFacade.class.getName());
    
    @EJB
    private UserFacade userFacade;
    @EJB
    private DepartmentFacade departmentFacade;
    
    public CompanyFacade() {
        super(Company.class, CompanyLog.class, CompanyStates.class);
    }

    /**
     * Отбирает все подчинённые объекты для владельца.
     * @param owner
     * @return
     */
    public List<BaseDict> findAllDetailItems(Company owner){
        getEntityManager().getEntityManagerFactory().getCache().evict(Department.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Department> cq = builder.createQuery(Department.class);
        Root<Department> c = cq.from(Department.class);
        Predicate crit = builder.equal(c.get(Department_.owner), owner);
        cq.select(c).where(builder.and(crit));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    @Override
    public Class<Company> getItemClass() {
        return Company.class;
    }

    @Override
    public String getFRM_NAME() {
        return Company.class.getSimpleName().toLowerCase();
    }            

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_COMPANY;
    }       
    
    /* Ищет компанию с указанным названием, и если не найдена то создаёт новую  */
    public Company onGetCompanyByName(String companyName){
        if (StringUtils.isBlank(companyName)){
            return null;
        }
        for (Company company : findAll()){
            if (Objects.equals(company.getName(), companyName)){
                return company;
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put("name", companyName);
        Company company = createItem(userFacade.getAdmin(), null, params);
        create(company);
        return company;
    }

    @Override
    public Rights getRightForChild(BaseDict item){
        if (item == null) return null;

        if (!item.isInheritsAccessChilds()) { //если не наследует права
            return getActualRightChildItem((Company)item);
        }

        if (item.getParent() != null) {
            return getRightForChild(item.getParent()); //получаем права от родителя
        }

        return departmentFacade.getDefaultRights(); //если иного не найдено, то берём дефолтные права справочника
    }

    /**
     * Отбор всех штатных единиц, принадлежащих напрямую указанной компании, c учётом не актуальных и удалённых в корзину
     * @param company
     * @return
     */
    public List<Staff> findStaffByCompany(Company company){
        getEntityManager().getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);
        Predicate crit1 = builder.equal(c.get(Staff_.company), company);
        Predicate crit2 = builder.isNull(c.get(Staff_.owner));
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    /**
     * Отбор всех подразделений, относящихся к указанной компании, c учётом не актуальных и удалённых в корзину
     * @param company
     * @return
     */
    public List<Department> findDepartmentByCompany(Company company){
        getEntityManager().getEntityManagerFactory().getCache().evict(Department.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Department> cq = builder.createQuery(Department.class);
        Root<Department> root = cq.from(Department.class);
        Predicate crit1 = builder.equal(root.get("owner"), company);
        cq.select(root).where(builder.and(crit1));
        Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    /**
     * Замена компании на другую
     * @param oldItem
     * @param newItem
     */
    @Override
    public int replaceItem(Company oldItem, Company newItem) {
        int count = 0;
        count = replaceCompanyInDepartments(oldItem, newItem);
        count = count + replaceCompanyInStaffs(oldItem, newItem);
        count = count + replaceCompanyInFolders(oldItem, newItem);
        return count;
    }

    /**
     * Выполняет замену компании в штатных единицах, напрямую с ней связанных
     * @param oldItem
     * @param newItem
     */
    private int replaceCompanyInStaffs(Company oldItem, Company newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Staff> update = builder.createCriteriaUpdate(Staff.class);
        Root root = update.from(Staff.class);
        update.set(Staff_.company, newItem);
        Predicate predicate = builder.equal(root.get(Staff_.company), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }

    /**
     * Выполняет замену компании в подразделениях
     * @param oldItem
     * @param newItem
     */
    private int replaceCompanyInDepartments(Company oldItem, Company newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Department> update = builder.createCriteriaUpdate(Department.class);
        Root root = update.from(Department.class);
        update.set(Department_.owner, newItem);
        Predicate predicate = builder.equal(root.get(Department_.owner), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }

    /**
     * Выполняет замену компании в папках
     * @param oldItem
     * @param newItem
     */
    private int replaceCompanyInFolders(Company oldItem, Company newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Folder> update = builder.createCriteriaUpdate(Folder.class);
        Root root = update.from(Folder.class);
        update.set(Folder_.companyDefault, newItem);
        Predicate predicate = builder.equal(root.get(Folder_.companyDefault), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
}
