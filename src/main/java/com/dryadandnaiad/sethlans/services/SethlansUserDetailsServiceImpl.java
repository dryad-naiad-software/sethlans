/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * File created by Mario Estrella on 6/12/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansUserDetailsServiceImpl implements SethlansUserDetailsService {
    private final UserRepository userRepository;
    private final Converter<User, UserDetails> userDetailsConverter;

    public SethlansUserDetailsServiceImpl(UserRepository userRepository, Converter<User, UserDetails> userDetailsConverter) {
        this.userRepository = userRepository;
        this.userDetailsConverter = userDetailsConverter;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (userRepository.findUserByUsername(username.toLowerCase()).isPresent()) {
            return userDetailsConverter.convert(userRepository.findUserByUsername(username).get());
        }
        throw new UsernameNotFoundException(username + " does not exist in database.");
    }
}
