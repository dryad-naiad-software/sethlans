package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.models.query.UserQuery;
import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.models.user.UserChallenge;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToUserQuery implements Converter<User, UserQuery> {
    @Override
    public UserQuery convert(User user) {
        for (UserChallenge challenge: user.getChallengeList()) {
            challenge.setResponse("");
        }
        var userQuery = UserQuery.builder()
                .userID(user.getUserID())
                .active(user.isActive())
                .username(user.getUsername())
                .email(user.getEmail())
                .challengeList(user.getChallengeList())
                .nodeEmailNotifications(user.isNodeEmailNotifications())
                .projectEmailNotifications(user.isProjectEmailNotifications())
                .systemEmailNotifications(user.isSystemEmailNotifications())
                .videoEncodingEmailNotifications(user.isVideoEncodingEmailNotifications())
                .roles(user.getRoles())
                .build();
        return userQuery;
    }
}
