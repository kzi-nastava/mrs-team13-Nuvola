package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.ProfileChangeRequest;
import Nuvola.Projekatsiit2025.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileChangeRequestRepository
        extends JpaRepository<ProfileChangeRequest, Long> {
    List<ProfileChangeRequest> findByStatus(RequestStatus status);
}