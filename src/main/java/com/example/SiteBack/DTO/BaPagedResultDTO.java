package com.example.SiteBack.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BaPagedResultDTO<T> {
    private List<T> items;  // 当前页数据
    private long totalCount; // 总条数
}
