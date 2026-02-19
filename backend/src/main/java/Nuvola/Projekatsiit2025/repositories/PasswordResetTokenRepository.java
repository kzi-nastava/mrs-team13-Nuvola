//package Nuvola.Projekatsiit2025.repositories;
//
//import Nuvola.Projekatsiit2025.model.PasswordResetToken;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
//    Optional<PasswordResetToken> findByToken(String token);
//    void deleteByUserId(Long userId);
//}

package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.PasswordResetToken;
import Nuvola.Projekatsiit2025.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("delete from PasswordResetToken t where t.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
