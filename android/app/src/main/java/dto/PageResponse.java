package dto;

import java.util.List;

public class PageResponse<T> {
    public List<T> content;
    public int number;
    public int size;
    public long totalElements;
    public int totalPages;
}
