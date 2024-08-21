package voicelog.voicelog.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import voicelog.voicelog.domain.RefreshToken;
import voicelog.voicelog.domain.User;

import java.util.Optional;

public interface RefreshTokenRepository  extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    Optional<RefreshToken> findByUserId(Long userId);

    @Transactional
    void deleteByUser(User user);
}
