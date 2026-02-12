export interface NotificationDTO {
  title: string;
  message: string;
  type: string; // npr: "INFO" | "SUCCESS" | "WARNING" | "ERROR"
}