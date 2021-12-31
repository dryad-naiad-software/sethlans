package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.models.user.UserChallenge;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserUtils {

    public static User updateDatabaseUser(User updatedUser, User userInDatabase, BCryptPasswordEncoder bCryptPasswordEncoder) {
        var passwordPresent = !updatedUser.getPassword().isEmpty();
        var challengePresent = !updatedUser.getChallengeList().isEmpty();
        if (passwordPresent) {
            userInDatabase.setPassword(bCryptPasswordEncoder.encode(updatedUser.getPassword()));
        }
        if(challengePresent) {
            for (UserChallenge challenge: updatedUser.getChallengeList()) {
                challenge.setResponse(bCryptPasswordEncoder.encode(challenge.getResponse()));
            }
            userInDatabase.setChallengeList(updatedUser.getChallengeList());
        }
        if(EmailValidator.getInstance().isValid(updatedUser.getEmail())) {
            userInDatabase.setEmail(updatedUser.getEmail());
        }
        if(updatedUser.getRoles() != null) {
            userInDatabase.setRoles(updatedUser.getRoles());
        }
        userInDatabase.setActive(updatedUser.isActive());
        userInDatabase.setPromptPasswordChange(updatedUser.isPromptPasswordChange());
        userInDatabase.setNodeEmailNotifications(updatedUser.isNodeEmailNotifications());
        userInDatabase.setProjectEmailNotifications(updatedUser.isProjectEmailNotifications());
        userInDatabase.setSystemEmailNotifications(updatedUser.isSystemEmailNotifications());
        userInDatabase.setVideoEncodingEmailNotifications(updatedUser.isVideoEncodingEmailNotifications());

        return userInDatabase;
    }

}
