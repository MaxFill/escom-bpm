package com.maxfill.model.roles;

import com.maxfill.model.users.User;
import java.io.Serializable;
import java.util.Set;

public abstract class Roles implements Serializable{
    private static final long serialVersionUID = 5076062464471540016L;

    private String roleName;
    private Set<User> participant;

    public Roles() {
    }
    
    public Roles(String roleName, Set<User> participant) {
        this.roleName = roleName;
        this.participant = participant;
    }
    
    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Set<User> getParticipant() {
        return participant;
    }
    public void setParticipant(Set<User> participant) {
        this.participant = participant;
    }    
    
}
