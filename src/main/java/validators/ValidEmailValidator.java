package validators;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import models.Confirmation;
import services.AuthService;
import services.DataService;
import validators.annotations.ValidEmail;

import com.google.inject.Inject;

public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {

    @Inject
    private DataService dataService;

    @Inject
    private AuthService authService;

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        //nothing to do
    }

    @Override
    public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
        boolean valid = true;

        final List<Confirmation> confirmations = dataService.findAllConfirmation();
        for (final Confirmation confirmation : confirmations) {
            String value = confirmation.getConfirmValue();
            value = authService.decryptAES(value);

            if (value.equalsIgnoreCase(object)) {
                valid = false;
            }
        }

        if (dataService.findUserByEmail(object) != null) {
            valid = false;
        }

        return valid;
    }
}