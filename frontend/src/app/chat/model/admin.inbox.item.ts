export interface AdminInboxItemDTO {
  chatId: number;
  userId: number;
  ownerName: string;
  lastMessageContent: string;
  lastMessageTime: string; // LocalDateTime string
  lastMessageSenderName: string;
}
