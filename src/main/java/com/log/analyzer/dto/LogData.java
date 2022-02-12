package com.log.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogData {
    public String id;
    public String state;
    public String type;
    public String host;
    public String timestamp;
}
