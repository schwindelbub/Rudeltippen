package validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import models.Constants;
import services.DataService;
import validators.annotations.ValidUsername;

import com.google.inject.Inject;

public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {

    @Inject
    private DataService dataService;

    @Override
    public void initialize(ValidUsername constraintAnnotation) {
        //nothing to do
    }

    @Override
    public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
        final Pattern p = Pattern.compile(Constants.USERNAMEPATTERN.value());
        final Matcher m = p.matcher(object);

        if (dataService.findUserByUsername(object) == null && m.matches()) {
            return true;
        }

        return false;
    }
}