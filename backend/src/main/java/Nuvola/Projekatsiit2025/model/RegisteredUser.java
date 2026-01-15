package Nuvola.Projekatsiit2025.model;

import jakarta.persistence.Entity;

@Entity
public class RegisteredUser extends User {
    private boolean isActivated;
}
