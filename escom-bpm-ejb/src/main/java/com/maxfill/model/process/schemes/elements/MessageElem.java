package com.maxfill.model.process.schemes.elements;

import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.utils.EscomUtils;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *  Сущность "Элемент схемы процесса "Сообщение"
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MessageElem extends WFConnectedElem{
    private static final long serialVersionUID = -4406617528890972722L;

    /**
     * Получатели (роли процесса) в JSON
     */
    @XmlElement(name = "recipients")
    private String recipientsJSON;
    
    /**
     * Текст сообщения (caption не подходит, т.к. будет отображаться на диаграмме в компоненте и его расколбасит)
     */
    @XmlElement(name = "content")
    private String content;

    public MessageElem() {
        this.uid = EscomUtils.generateGUID();
    }
    
    public MessageElem(String content, int x, int y) {
        this.content = content;        
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }
    
    /* GETS & SETS */

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
        
    @Override
    public String getImage() {
        return "msg-32";
    }

    @Override
    public String getStyle() {
        return DictWorkflowElem.STYLE_MESSAGE;
    }

    @Override
    public String getBundleKey() {
        return "Message";
    }

    public String getRecipientsJSON() {
        return recipientsJSON;
    }
    public void setRecipientsJSON(String recipientsJSON) {
        this.recipientsJSON = recipientsJSON;
    }
     
    /* *** *** */
    
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        MessageElem elem = (MessageElem) o;

        return uid.equals(elem.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "MessageElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}
