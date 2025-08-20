package br.com.apisentinel.client.governance;

import java.util.List;

public class RestServiceArtifactRequest {
    public String id;
    public String type;
    public String name;
    public String friendlyName;
    public String context;
    public String version;
    public String description;
    public String application;
    public String accessScope;
    public String host;
    public String apiGatewayExposure;
    public String communicationChannel;
    public String communicationProtocol;
    public String messageFormat;
    public String authenticationType;
    public String authorizationType;
    public String secureCommunicationChannel;
    public String credentialVaultLocation;
    public String securityTestType;
    public String usesSensitiveData;
    public Integer rateLimitTps;
    public List<Contact> contacts;

    public static class Contact {
        public String name;
        public String type;
    }
}
