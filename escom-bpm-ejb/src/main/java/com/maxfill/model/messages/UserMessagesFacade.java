package com.maxfill.model.messages;

import com.maxfill.Configuration;
import com.maxfill.services.mail.MailBoxFacade;
import com.maxfill.facade.BaseLazyLoadFacade;
import com.maxfill.model.process.Process;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.task.Task;
import com.maxfill.model.users.User;
import com.maxfill.utils.ItemUtils;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
public class UserMessagesFacade extends BaseLazyLoadFacade<UserMessages>{

    @EJB
    private Configuration conf;
    
    @EJB
    private MailBoxFacade mailBoxFacade;    
    
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
    public void createSystemMessage(User addressee, String subject, StringBuilder content, Map<String, BaseDict> links){        
        String senderName = ItemUtils.getBandleLabel("System", conf.getServerLocale());
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
    public void createMessage(User addressee, String senderName, String senderEmail, String subject, StringBuilder content, Map<String, BaseDict> links){
        UserMessages message = new UserMessages();
        message.setName(subject);
        message.setAddressee(addressee);
        message.setDateSent(new Date());
        links.entrySet().forEach(row->{
            switch (row.getKey()){
                case "doc":{
                    message.setDocument((Doc)row.getValue());
                    break;
                }
                case "task":{
                    message.setTask((Task)row.getValue()); 
                    break;
                }
                case "process":{
                    message.setProcess((Process)row.getValue()); 
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
    
    /**
     * Отправка сообщения пользователям из списка
     * @param recipients
     * @param sender
     * @param subject
     * @param content
     * @param subject
     * @param links
     */
    public void sendMessageUsers(List<User> recipients, User sender, String subject, StringBuilder content, Map<String, BaseDict> links){
        recipients.forEach(recipient->createMessage(recipient, sender.getName(), sender.getEmail(), subject, content, links));
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

    public int removeMessageByUser(User user){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<UserMessages> cd = builder.createCriteriaDelete(UserMessages.class);
        Root root = cd.from(UserMessages.class);
        Predicate crit1 = builder.equal(root.get(UserMessages_.addressee), user);
        cd.where(crit1);
        Query query = getEntityManager().createQuery(cd);
        return query.executeUpdate();
    }
}