package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.models.user.SethlansUser;
import com.dryadandnaiad.sethlans.models.user.UserChallenge;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserUtils {

    public static SethlansUser updateDatabaseUser(SethlansUser updatedSethlansUser, SethlansUser sethlansUserInDatabase, BCryptPasswordEncoder bCryptPasswordEncoder) {
        var passwordPresent = !updatedSethlansUser.getPassword().isEmpty();
        var challengePresent = !updatedSethlansUser.getChallengeList().isEmpty();
        if (passwordPresent) {
            sethlansUserInDatabase.setPassword(bCryptPasswordEncoder.encode(updatedSethlansUser.getPassword()));
        }
        if (challengePresent) {
            for (UserChallenge challenge : updatedSethlansUser.getChallengeList()) {
                challenge.setResponse(bCryptPasswordEncoder.encode(challenge.getResponse()));
            }
            sethlansUserInDatabase.setChallengeList(updatedSethlansUser.getChallengeList());
        }
        if (EmailValidator.getInstance().isValid(updatedSethlansUser.getEmail())) {
            sethlansUserInDatabase.setEmail(updatedSethlansUser.getEmail());
        }
        if (updatedSethlansUser.getRoles() != null) {
            sethlansUserInDatabase.setRoles(updatedSethlansUser.getRoles());
        }
        sethlansUserInDatabase.setActive(updatedSethlansUser.isActive());
        sethlansUserInDatabase.setPromptPasswordChange(updatedSethlansUser.isPromptPasswordChange());
        sethlansUserInDatabase.setNodeEmailNotifications(updatedSethlansUser.isNodeEmailNotifications());
        sethlansUserInDatabase.setProjectEmailNotifications(updatedSethlansUser.isProjectEmailNotifications());
        sethlansUserInDatabase.setSystemEmailNotifications(updatedSethlansUser.isSystemEmailNotifications());
        sethlansUserInDatabase.setVideoEncodingEmailNotifications(updatedSethlansUser.isVideoEncodingEmailNotifications());

        return sethlansUserInDatabase;
    }

}
