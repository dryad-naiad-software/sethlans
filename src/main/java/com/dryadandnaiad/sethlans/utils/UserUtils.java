package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.models.user.UserChallenge;
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
        userInDatabase.setEmail(updatedUser.getEmail());
        userInDatabase.setRoles(updatedUser.getRoles());
        userInDatabase.setActive(updatedUser.isActive());
        userInDatabase.setPromptPasswordChange(updatedUser.isPromptPasswordChange());
        userInDatabase.setNodeEmailNotifications(updatedUser.isNodeEmailNotifications());
        userInDatabase.setProjectEmailNotifications(updatedUser.isProjectEmailNotifications());
        userInDatabase.setSystemEmailNotifications(updatedUser.isSystemEmailNotifications());
        userInDatabase.setVideoEncodingEmailNotifications(updatedUser.isVideoEncodingEmailNotifications());

        return userInDatabase;
    }

}
