package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.models.query.UserQuery;
import com.dryadandnaiad.sethlans.models.settings.NotificationSettings;
import com.dryadandnaiad.sethlans.models.user.SethlansUser;
import com.dryadandnaiad.sethlans.models.user.UserChallenge;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToUserQuery implements Converter<SethlansUser, UserQuery> {
    @Override
    public UserQuery convert(SethlansUser sethlansUser) {
        for (UserChallenge challenge : sethlansUser.getChallengeList()) {
            challenge.setResponse("");
        }
        var notificationSettings = NotificationSettings.builder()
                .nodeEmailNotifications(sethlansUser.isNodeEmailNotifications())
                .projectEmailNotifications(sethlansUser.isProjectEmailNotifications())
                .videoEncodingEmailNotifications(sethlansUser.isVideoEncodingEmailNotifications())
                .systemEmailNotifications(sethlansUser.isSystemEmailNotifications()).build();
        return UserQuery.builder()
                .userID(sethlansUser.getUserID())
                .active(sethlansUser.isActive())
                .username(sethlansUser.getUsername())
                .email(sethlansUser.getEmail())
                .challengeList(sethlansUser.getChallengeList())
                .notificationSettings(notificationSettings)
                .roles(sethlansUser.getRoles())
                .build();
    }
}
