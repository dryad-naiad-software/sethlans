/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

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
