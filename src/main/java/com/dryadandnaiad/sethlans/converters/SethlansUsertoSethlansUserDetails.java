/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.domains.users.SethlansUser;
import com.dryadandnaiad.sethlans.security.SethlansUserDetails;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created Mario Estrella on 9/21/17.
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
        userDetails.setPassword(user.getEncryptedPassword());
        userDetails.setEnabled(user.isEnabled());

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

        user.getSethlansRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getRole()));
        });

        userDetails.setAuthorities(authorities);

        return userDetails;
    }
}
