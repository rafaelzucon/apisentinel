package br.com.apisentinel.repository;

import br.com.apisentinel.domain.AssetGW;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetGWRepository extends JpaRepository<AssetGW, Long> {

    Optional<AssetGW> findByGwAssetId(Long gwAssetId);

    Optional<AssetGW> findByNameAndVersion(String name, String version);

    List<AssetGW> findByName(String name);
}
