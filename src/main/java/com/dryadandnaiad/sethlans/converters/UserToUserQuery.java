package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.models.query.UserQuery;
import com.dryadandnaiad.sethlans.models.settings.NotificationSettings;
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
        var notificationSettings = NotificationSettings.builder()
                .nodeEmailNotifications(user.isNodeEmailNotifications())
                .projectEmailNotifications(user.isProjectEmailNotifications())
                .videoEncodingEmailNotifications(user.isVideoEncodingEmailNotifications())
                .systemEmailNotifications(user.isSystemEmailNotifications()).build();
        return UserQuery.builder()
                .userID(user.getUserID())
                .active(user.isActive())
                .username(user.getUsername())
                .email(user.getEmail())
                .challengeList(user.getChallengeList())
                .notificationSettings(notificationSettings)
                .roles(user.getRoles())
                .build();
    }
}
