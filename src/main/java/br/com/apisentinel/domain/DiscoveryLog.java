package br.com.apisentinel.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import jakarta.persistence.PrePersist;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "discovery_log")
public class DiscoveryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64, nullable = false)
    private String status;

    @Column(length = 64)
    private String type;

    @Column(length = 256)
    private String governanceId;

    @Column(length = 512)
    private String name;

    @Column(length = 64)
    private String version;

    @Column(length = 512)
    private String context;

    @Column(length = 32, nullable = false)
    private String source;

    @Column(nullable = false)
    private Timestamp createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = new Timestamp(System.currentTimeMillis());
    }

}
