package Nuvola.Projekatsiit2025.dto;

public class RideCancelResponseDTO {
    private Long id;
    private String status;
    private String message;

    public RideCancelResponseDTO() {}

    public RideCancelResponseDTO(Long id, String status, String message) {
        this.id = id;
        this.status = status;
        this.message = message;
    }

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }

    public void setId(Long id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
}
