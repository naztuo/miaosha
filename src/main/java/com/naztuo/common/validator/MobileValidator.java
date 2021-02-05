package com.naztuo.common.validator;

import com.naztuo.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MobileValidator implements ConstraintValidator<MobileCheck,String> {

    private boolean require = false ;

    @Override
    public void initialize(MobileCheck constraintAnnotation) {
        require = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(require){
            return ValidatorUtil.isMobile(value) ;
        }else{
            if(StringUtils.isEmpty(value)){
                return true ;
            }else {
                return ValidatorUtil.isMobile(value) ;
            }
        }
    }
}
