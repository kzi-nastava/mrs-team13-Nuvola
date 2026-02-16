package Nuvola.Projekatsiit2025.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class RideRepositoryTestEmbedded {

    @Autowired
    private RideRepository rideRepository;
}
