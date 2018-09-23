package com.maxfill.model.messages;

import com.maxfill.Configuration;
import com.maxfill.services.mail.MailBoxFacade;
import com.maxfill.facade.BaseLazyFacade;
import com.maxfill.model.process.Process;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.DocFacade;
import com.maxfill.model.process.ProcessFacade;
import com.maxfill.model.task.Task;
import com.maxfill.model.task.TaskFacade;
import com.maxfill.model.users.User;
import com.maxfill.model.users.UserFacade;
import com.maxfill.utils.ItemUtils;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;

@Stateless
public class UserMessagesFacade extends BaseLazyFacade<UserMessages>{

    @EJB
    private Configuration conf;
    @EJB
    private DocFacade docFacade;
    @EJB
    private TaskFacade taskFacade;
    @EJB
    private ProcessFacade proccessFacade;
    @EJB
    private MailBoxFacade mailBoxFacade;    
    @EJB
    private UserFacade userFacade;
    
    public UserMessagesFacade() {
        super(UserMessages.class);
    }

    public List<UserMessages> findUnReadMessageByUser(User addressee){
        getEntityManager().getEntityManagerFactory().getCache().evict(UserMessages.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<UserMessages> cq = builder.createQuery(UserMessages.class);
        Root<UserMessages> root = cq.from(UserMessages.class);
        Predicate crit1 = builder.equal(root.get(UserMessages_.addressee), addressee);
        Predicate crit2 = builder.isNull(root.get(UserMessages_.dateReading));
        cq.select(root).where(builder.and(crit1, crit2));
        cq.orderBy(builder.asc(root.get(UserMessages_.dateSent)));
        Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    public List<UserMessages> findMessageByUser(User addressee){
        getEntityManager().getEntityManagerFactory().getCache().evict(UserMessages.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<UserMessages> cq = builder.createQuery(UserMessages.class);
        Root<UserMessages> c = cq.from(UserMessages.class);
        Predicate crit1 = builder.equal(c.get(UserMessages_.addressee), addressee);
        cq.select(c).where(builder.and(crit1));
        cq.orderBy(builder.asc(c.get(UserMessages_.dateSent)));
        Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    /**
     * Создание системного сообщения
     * @param addressee
     * @param subject
     * @param content
     * @param links
     */
    public void createSystemMessage(User addressee, String subject, StringBuilder content, List<BaseDict> links){        
        String senderName = ItemUtils.getBandleLabel("System", userFacade.getUserLocale(addressee));
        String senderEmail = conf.getDefaultSenderEmail();
        createMessage(addressee, senderName, senderEmail, subject, content, links);
    }

    /**
     * Создание нового сообщения
     * @param addressee
     * @param senderName
     * @param senderEmail
     * @param subject
     * @param content
     * @param links
     */
    public void createMessage(User addressee, String senderName, String senderEmail, String subject, StringBuilder content, List<BaseDict> links){
        UserMessages message = new UserMessages();
        message.setName(subject);
        message.setAddressee(addressee);
        message.setDateSent(new Date());
        Locale locale = userFacade.getUserLocale(addressee);
        links.forEach(item->{
            switch (item.getClass().getSimpleName().toLowerCase()){
                case "doc":{
                    Doc doc = (Doc)item;
                    message.setDocument(doc);
                    content.append("<br/>")
                            .append(ItemUtils.getBandleLabel("GoToDocument", locale))
                            .append(" ")
                            .append(docFacade.getItemHREF(doc));
                    break;
                }
                case "task":{
                    Task task = (Task)item;
                    message.setTask(task);
                    content.append("<br/>")
                            .append(ItemUtils.getBandleLabel("GoToTask", locale))
                            .append(" ")
                            .append(taskFacade.getItemHREF(task));
                    break;
                }
                case "process":{
                    Process process = (Process)item;
                    message.setProcess(process); 
                    content.append("<br/>")
                            .append(ItemUtils.getBandleLabel("GoToProcess", locale))
                            .append(" ")
                            .append(proccessFacade.getItemHREF(process));
                    break;
                }
            }
        });
        
        message.setSender(senderName);
        message.setImportance(1);
        create(message);
        if (addressee.isDuplicateMessagesEmail()){
            String emailAdress = addressee.getEmail();
            if (StringUtils.isNotBlank(emailAdress)){
                mailBoxFacade.createMailBox(subject, emailAdress, senderEmail, content.toString(), senderName);
            }
        }
    }    
    
    public Integer getCountUnReadMessage(User addressee){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery();
        Root<UserMessages> root = cq.from(UserMessages.class);        
        Predicate crit1 = builder.equal(root.get(UserMessages_.addressee), addressee);
        Predicate crit2 = builder.isNull(root.get(UserMessages_.dateReading));
        cq.select(builder.count(root));
        cq.where(builder.and(crit1, crit2));        
        Query query = getEntityManager().createQuery(cq);
        return ((Long) query.getSingleResult()).intValue();
    }

    /**
     * Удаление сообщений по пользователю
     * @param user
     * @return 
     */
    public int removeMessageByUser(User user){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<UserMessages> cd = builder.createCriteriaDelete(UserMessages.class);
        Root root = cd.from(UserMessages.class);
        Predicate crit1 = builder.equal(root.get(UserMessages_.addressee), user);
        cd.where(crit1);
        Query query = getEntityManager().createQuery(cd);
        return query.executeUpdate();
    }
    
    /**
     * Удаление сообщений по процессу
     * @param process
     * @return 
     */
    public int removeMessageByProcess(Process process){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<UserMessages> cd = builder.createCriteriaDelete(UserMessages.class);
        Root root = cd.from(UserMessages.class);
        Predicate crit1 = builder.equal(root.get(UserMessages_.process), process);
        cd.where(crit1);
        Query query = getEntityManager().createQuery(cd);
        return query.executeUpdate();
    }
    
    public int removeMessageByTask(Task task){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<UserMessages> cd = builder.createCriteriaDelete(UserMessages.class);
        Root root = cd.from(UserMessages.class);
        Predicate crit1 = builder.equal(root.get(UserMessages_.task), task);
        cd.where(crit1);
        Query query = getEntityManager().createQuery(cd);
        return query.executeUpdate();
    }
}