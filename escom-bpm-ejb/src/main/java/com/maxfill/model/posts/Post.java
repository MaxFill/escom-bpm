package com.maxfill.model.posts;

import com.maxfill.model.BaseDict;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;

/* Должность */
@Entity
@Table(name = "posts")
@DiscriminatorColumn(name = "REF_TYPE")
public class Post extends BaseDict<Post, Post, Post, PostLog> {
    private static final long serialVersionUID = 9047767804115998647L;

    @TableGenerator(
            name = "idPostGen",
            table = "SYS_ID_GEN",
            pkColumnName = "GEN_KEY",
            valueColumnName = "GEN_VALUE",
            pkColumnValue = "POST_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy = TABLE, generator = "idPostGen")
    @Column(name = "Id")
    private Integer id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<PostLog> itemLogs = new ArrayList<>();

    public Post() {
    }

    @Override
    public List<PostLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<PostLog> itemLogs) {
        this.itemLogs = itemLogs;
    }

    @Override
    public String getIconName() {
        return "doc16";
    }

    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }    
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
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
        return "com.maxfill.escombpm2.datamodel.docs.Posts[ id=" + id + " ]";
    }

}
