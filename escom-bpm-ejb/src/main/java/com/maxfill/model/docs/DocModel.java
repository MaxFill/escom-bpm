
package com.maxfill.model.docs;

import com.maxfill.model.BaseDataModel;

/**
 *
 * @author Maxim
 * @param <Doc>
 */
public class DocModel<Doc> extends BaseDataModel{
    private static final long serialVersionUID = -4568284522033684930L;
    private String numberSearche;
    
    private Integer selectedDocId;
    
    public String getNumberSearche() {
        return numberSearche;
    }
    public void setNumberSearche(String numberSearche) {
        this.numberSearche = numberSearche;
    }
    
    public Integer getSelectedDocId() {
        return selectedDocId;
}
    public void setSelectedDocId(Integer selectedDocId) {
        this.selectedDocId = selectedDocId;
    }
        
}
