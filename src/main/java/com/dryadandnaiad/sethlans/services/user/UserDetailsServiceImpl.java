package com.dryadandnaiad.sethlans.services.user;

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Created Mario Estrella on 2/16/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private SethlansUserDatabaseService userDatabaseService;
    private Converter<SethlansUser, UserDetails> sethlansUserDetailsConverter;

    @Autowired
    public void setUserUserDetailsConverter(Converter<SethlansUser, UserDetails> userUserDetailsConverter) {
        this.sethlansUserDetailsConverter = userUserDetailsConverter;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return sethlansUserDetailsConverter.convert(userDatabaseService.findByUserName(username.toLowerCase()));
    }

    @Autowired
    public void setUserDatabaseService(SethlansUserDatabaseService userDatabaseService) {
        this.userDatabaseService = userDatabaseService;
    }
}
