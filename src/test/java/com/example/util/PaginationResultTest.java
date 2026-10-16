package com.example.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginationResultTest {

    @Test
    void calculatesTotalPagesByRoundingUp() {
        PaginationResult<Integer> result = new PaginationResult<>(Arrays.asList(1, 2), 2, 2, 5);
        assertEquals(3, result.getTotalPages());
        assertEquals(5, result.getTotalItems());
    }
}
