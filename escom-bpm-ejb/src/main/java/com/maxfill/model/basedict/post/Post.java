package com.maxfill.model.basedict.post;

import com.maxfill.model.basedict.BaseDict;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Справочник "Должности"
 */
@Entity
@Table(name = "posts")
@DiscriminatorColumn(name = "REF_TYPE")
public class Post extends BaseDict<Post, Post, Post, PostLog, PostStates> {
    private static final long serialVersionUID = 9047767804115998647L;

    @TableGenerator(
            name = "idPostGen",
            table = "SYS_ID_GEN",
            pkColumnName = "GEN_KEY",
            valueColumnName = "GEN_VALUE",
            pkColumnValue = "POST_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = TABLE, generator = "idPostGen")
    @Column(name = "Id")
    private Integer id;

    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private PostStates state;
        
    public Post() {
    }

    @Override
    public PostStates getState() {
        return state;
    }
    @Override
    public void setState(PostStates state) {
        this.state = state;
    }

    @Override
    public String getIconName() {
        return "doc16";
    }

    @Override
    public List<Post> getDetailItems() {
        return null;
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }    
    
    /* *** *** */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Post)) {
            return false;
        }
        Post other = (Post) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Posts[ id=" + id + " ] [" + getName() + "]";
    }

}
