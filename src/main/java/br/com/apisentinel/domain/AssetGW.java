package br.com.apisentinel.domain;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "asset_gw")
public class AssetGW {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(name = "friendly_name", length = 180)
    private String friendlyName;

    @Column(length = 32)
    private String version;

    @Column(length = 255)
    private String context;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 120)
    private String owner;

    @Column(name = "owner_email", length = 180)
    private String ownerEmail;

    @Column(name = "governed", nullable = false)
    @Builder.Default
    private Boolean governed = Boolean.FALSE;

    @Column(name = "change_hash", nullable = false, length = 64)
    private String changeHash;

    @Column(name = "created_in", nullable = false)
    private Timestamp createdIn;

    @Column(name = "updated_in")
    private Timestamp updatedIn;

    @Column(name = "gw_asset_id", unique = true)
    private Long gwAssetId;

    @Column(name = "gw_id", length = 64)
    private String gwId;

    @Column(name = "gw_version", length = 64)
    private String gwVersion;

    @Column(name = "gw_created_in")
    private Timestamp gwCreatedIn;

    @Column(name = "gw_exposure", length = 32)
    private String gwExposure;
}
