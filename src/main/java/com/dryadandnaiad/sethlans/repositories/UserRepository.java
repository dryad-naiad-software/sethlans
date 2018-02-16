package com.dryadandnaiad.sethlans.repositories;

import com.dryadandnaiad.sethlans.domains.database.users.SethlansUser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Created Mario Estrella on 2/16/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Repository
public interface UserRepository extends JpaRepository<SethlansUser, Long> {

    String USERS_BY_LOGIN_CACHE = "usersByLogin";

    String USERS_BY_EMAIL_CACHE = "usersByEmail";

    Optional<SethlansUser> findOneByActivationKey(String activationKey);

    List<SethlansUser> findAllByActivatedIsFalseAndCreatedDateBefore(Instant dateTime);

    Optional<SethlansUser> findOneByResetKey(String resetKey);

    Optional<SethlansUser> findOneByEmailIgnoreCase(String email);

    Optional<SethlansUser> findOneByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    Optional<SethlansUser> findOneWithAuthoritiesById(Long id);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_LOGIN_CACHE)
    Optional<SethlansUser> findOneWithAuthoritiesByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_EMAIL_CACHE)
    Optional<SethlansUser> findOneWithAuthoritiesByEmail(String email);

    Page<SethlansUser> findAllByLoginNot(Pageable pageable, String login);
}
