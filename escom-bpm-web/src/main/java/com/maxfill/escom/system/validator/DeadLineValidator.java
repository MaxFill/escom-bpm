package com.maxfill.escom.system.validator;

import com.maxfill.escom.utils.EscomMsgUtils;
import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;

/**
 * Валидатор проверки срока исполнения
 * @author maksim
 */
@FacesValidator(DeadLineValidator.VALIDATOR_ID)
public class DeadLineValidator extends AbstractValidator{
    public static final String VALIDATOR_ID = "deadlineValidator";

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) return;

        Date checkDate = (Date) value;
        
        if (checkDate.before(new Date())) {
            String msg = EscomMsgUtils.getMessageLabel("DeadlineSpecifiedInPastTime");            
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, ""));
        }
    }

    @Override
    public String getValidatorId() {
        return VALIDATOR_ID;
    }
    
}
