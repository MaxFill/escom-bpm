package com.maxfill.escom.system.validator;

import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;

import com.maxfill.escom.utils.MsgUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Проверка корректности email
 */
@FacesValidator(EmailValidator.VALIDATOR_ID)
public class EmailValidator extends AbstractValidator {

    public static final String VALIDATOR_ID = "escom.emailValidator";

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    @Override
    public String getValidatorId() {
        return VALIDATOR_ID;
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (StringUtils.isBlank((String)value)) {
            return;
        }

        if (!pattern.matcher(value.toString()).matches()) {
            String invalidEmail = MsgUtils.getValidateLabel("INVALID_EMAIL");
            String checkError = MsgUtils.getValidateLabel("CHECK_ERROR");
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, invalidEmail, checkError));
        }
    }
}