package com.hapi.chargingsystem.common.http;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    
    private List<T> records;
    private long total;
    private long size;
    private long current;
    private long pages;
    
    public PageResult() {}
    
    public PageResult(List<T> records, long total, long size, long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = (total + size - 1) / size;
    }
    
    public static <T> PageResult<T> of(List<T> records, long total, long size, long current) {
        return new PageResult<>(records, total, size, current);
    }
}