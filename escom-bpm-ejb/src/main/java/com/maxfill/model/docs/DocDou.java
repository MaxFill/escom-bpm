/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.model.docs;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Maxim
 */
@Entity
@Table(name = "docsDou")
public class DocDou implements Serializable {
    private static final long serialVersionUID = 1612388878385765739L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "DocId")
    private Integer docId;

    @Column(name = "FolderId")
    private Integer folderId;

    @Size(max = 50)
    @Column(name = "NumberOuter")
    private String numberOuter;

    @JoinColumn(name = "DocId", referencedColumnName = "Id", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Doc docs;

    public DocDou() {
    }

    public DocDou(Integer docId) {
        this.docId = docId;
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }

    public String getNumberOuter() {
        return numberOuter;
    }

    public void setNumberOuter(String numberOuter) {
        this.numberOuter = numberOuter;
    }

    public Doc getDocs() {
        return docs;
    }

    public void setDocs(Doc docs) {
        this.docs = docs;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (docId != null ? docId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DocDou)) {
            return false;
        }
        DocDou other = (DocDou) object;
        if ((this.docId == null && other.docId != null) || (this.docId != null && !this.docId.equals(other.docId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.datamodel.DocsDou[ docId=" + docId + " ]";
    }
    
}
