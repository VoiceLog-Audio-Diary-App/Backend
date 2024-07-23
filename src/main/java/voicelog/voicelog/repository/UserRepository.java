package voicelog.voicelog.repository;

import voicelog.voicelog.domain.RefreshToken;
import voicelog.voicelog.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String email);

    boolean existsByUsername(String email);
}
