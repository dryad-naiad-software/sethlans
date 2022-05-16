package com.dryadandnaiad.sethlans.models.query;

import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.models.settings.NotificationSettings;
import com.dryadandnaiad.sethlans.models.user.UserChallenge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserQuery {
    private String userID;
    private String username;
    private List<UserChallenge> challengeList;
    private Set<Role> roles;
    private String email;
    private boolean active;
    private NotificationSettings notificationSettings;



}
