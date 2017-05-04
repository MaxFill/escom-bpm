
package com.maxfill.model.staffs;

import com.maxfill.model.BaseDataModel; 

/**
 * Модель для бина Staff
 * @author Maxim
 * @param <Staff>
 */
public class StaffModel<Staff> extends BaseDataModel{
    private static final long serialVersionUID = 6995242584907118189L;
 
    private String postSearche = "";
    private String secondNameSearche = "";
    
    public String getPostSearche() {
        return postSearche;
    }

    public void setPostSearche(String postSearche) {
        this.postSearche = postSearche;
    }

    public String getSecondNameSearche() {
        return secondNameSearche;
    }

    public void setSecondNameSearche(String secondNameSearche) {
        this.secondNameSearche = secondNameSearche;
    }
    
}
