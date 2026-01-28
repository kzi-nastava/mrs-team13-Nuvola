package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.ProfileChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileChangeRequestRepository
        extends JpaRepository<ProfileChangeRequest, Long> {
}