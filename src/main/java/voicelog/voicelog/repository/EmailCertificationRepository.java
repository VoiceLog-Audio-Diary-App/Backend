package voicelog.voicelog.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import voicelog.voicelog.domain.EmailCertification;

@Repository
public interface EmailCertificationRepository extends JpaRepository<EmailCertification, String> {
    EmailCertification findByEmail(@Param("email") String email);

    @Transactional
    void deleteByEmail(@Param("email") String email);
}
