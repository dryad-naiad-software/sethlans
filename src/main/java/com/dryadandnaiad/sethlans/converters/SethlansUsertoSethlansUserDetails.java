package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.security.SethlansUserDetails;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created Mario Estrella on 2/25/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
public class SethlansUsertoSethlansUserDetails implements Converter<SethlansUser, UserDetails> {
    @Override
    public UserDetails convert(SethlansUser user) {
        SethlansUserDetails userDetails = new SethlansUserDetails();
        userDetails.setUsername(user.getUsername().toLowerCase());
        userDetails.setPassword(user.getPassword());
        userDetails.setEnabled(user.isActive());

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.toString()));
        });

        userDetails.setAuthorities(authorities);

        return userDetails;
    }
}
