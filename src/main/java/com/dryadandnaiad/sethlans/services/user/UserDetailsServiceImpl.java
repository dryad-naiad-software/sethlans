package com.dryadandnaiad.sethlans.services.user;

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.repositories.SethlansUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static java.util.Collections.emptyList;

/**
 * Created Mario Estrella on 2/16/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private SethlansUserRepository sethlansUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SethlansUser sethlansUser = sethlansUserRepository.findByUsername(username);
        if (sethlansUser == null) {
            throw new UsernameNotFoundException(username);
        }
        return new User(sethlansUser.getUsername(), sethlansUser.getPassword(), emptyList());
    }

    @Autowired
    public void setSethlansUserRepository(SethlansUserRepository sethlansUserRepository) {
        this.sethlansUserRepository = sethlansUserRepository;
    }
}
