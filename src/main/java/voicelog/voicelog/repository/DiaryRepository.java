package voicelog.voicelog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import voicelog.voicelog.domain.Diary;
import voicelog.voicelog.domain.User;

import java.time.LocalDate;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Optional<Diary> findByDateAndUser(LocalDate date, User user);
}
