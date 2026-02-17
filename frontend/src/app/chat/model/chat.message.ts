export interface ChatMessageDTO {
  id: number;
  chatId: number;
  content: string;
  sentAt?: string;
  senderId: number;
  senderName: string;
}
