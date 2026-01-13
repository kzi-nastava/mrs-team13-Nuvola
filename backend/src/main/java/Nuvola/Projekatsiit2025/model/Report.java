package Nuvola.Projekatsiit2025.model;

import lombok.Data;

@Data
public class Report {
    private Long id;
    private String reason;
    private RegisteredUser author;
    private Ride ride; // maybe it's not necessary for this to be bidirectional?
}
