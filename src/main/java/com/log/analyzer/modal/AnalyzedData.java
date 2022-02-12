package com.log.analyzer.modal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyzedData {

    @Id
    private String id;

    private int duration;
    private String type;
    private String host;
    private boolean alert;


}
