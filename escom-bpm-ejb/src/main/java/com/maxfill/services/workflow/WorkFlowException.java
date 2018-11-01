package com.maxfill.services.workflow;

import javax.ejb.ApplicationException;

/**
 *
 * @author maksim
 */
@ApplicationException(rollback = true)
public class WorkFlowException extends Exception{    
    private static final long serialVersionUID = -854185543831700276L;

    public WorkFlowException() {
    }

    public WorkFlowException(String message) {
        super(message);
    }
    
}
