package com.ds.devsuaccount.infraestructure.valuestorage.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Idempotency {
    private String id;
    private Object value;
}
