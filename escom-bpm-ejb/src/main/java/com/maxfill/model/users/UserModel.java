
package com.maxfill.model.users;

import com.maxfill.model.BaseDataModel;

/**
 *
 * @author mfilatov
 */
public class UserModel extends BaseDataModel{
    private static final long serialVersionUID = -5904920752878613676L;

    public UserModel() {
    }
    
    private String newPassword;
    private String oldPassword;
    private String repeatePassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getRepeatePassword() {
        return repeatePassword;
    }

    public void setRepeatePassword(String repeatePassword) {
        this.repeatePassword = repeatePassword;
    }
    
    
}
