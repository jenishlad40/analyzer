package com.log.analyzer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LogAnalyzerException extends Exception {
    private final String errorMessage;
}
