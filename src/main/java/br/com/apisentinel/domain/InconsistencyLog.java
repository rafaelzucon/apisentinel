package br.com.apisentinel.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inconsistency_log")
public class InconsistencyLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128, nullable = false)
    private String code;

    @Column(length = 2000)
    private String payload;

    @Column(nullable = false)
    private Timestamp createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = new Timestamp(System.currentTimeMillis());
    }

}
