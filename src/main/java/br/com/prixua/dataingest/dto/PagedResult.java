package br.com.prixua.dataingest.dto;

import java.util.List;

public record PagedResult<T>(List<T> content, long total, int page, int size) {}
