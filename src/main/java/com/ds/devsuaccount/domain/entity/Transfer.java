package com.ds.devsuaccount.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class Transfer {
    private String id;
    @JsonProperty("origin_id")
    private String originId;
    private String type;
    @JsonProperty("payment_date")
    private String paymentDate;
    private String concept;
    private Double amount;
    @JsonProperty("additional_info")
    private Map<Object,Object> additionalInfo;
    private Origin origin;
    private Destination destination;
    private String status;
    @JsonProperty("status_detail")
    private String statusDetail;
    @JsonProperty("failed_reason")
    private String failedReason;

}
