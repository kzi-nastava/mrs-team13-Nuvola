export interface PageDTO<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // current page
  size: number;
}