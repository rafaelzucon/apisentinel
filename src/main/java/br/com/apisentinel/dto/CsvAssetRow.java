package br.com.apisentinel.dto;

import lombok.Data;

@Data
public class CsvAssetRow {
    private String type;
    private String name;
    private String friendlyName;
    private String context;
    private String version;
    private String description;
    private String application;
    private String timeModelState;
    private String accessScope;
    private String host;
    private String apiGatewayExposure;
    private String communicationChannel;
    private String communicationProtocol;
    private String messageFormats;
    private String authenticationType;
    private String authorizationType;
    private String secureCommunicationChannel;
    private String credentialVaultLocation;
    private String securityTestType;
    private String usesSensitiveData;
    private String rateLimitTps;
    private String team;
    private String technicalOwner;
    private String technicalEmail;
    private String businessOwner;
    private String businessEmail;
}
