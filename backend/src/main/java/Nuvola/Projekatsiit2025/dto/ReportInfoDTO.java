package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.Report;
import lombok.Data;

@Data
public class ReportInfoDTO {
    private Long id;
    private String reason;
    private String authorUsername;

    public ReportInfoDTO() {}

    public ReportInfoDTO(Report r) {
        this.id = r.getId();
        this.reason = r.getReason();
        this.authorUsername = r.getAuthor() != null ? r.getAuthor().getUsername() : null;
    }
}
