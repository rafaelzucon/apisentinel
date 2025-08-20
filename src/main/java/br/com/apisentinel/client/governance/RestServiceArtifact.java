package br.com.apisentinel.client.governance;

import java.util.List;

public class RestServiceArtifact {
    private String id;
    private String name;
    private String version;
    private String context;
    private String friendlyName;
    private String selfLink;
    private List<RestServiceArtifactRequest.Contact> contacts;
    private Integer rateLimitTps;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String v) {
        this.version = v;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String c) {
        this.context = c;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String f) {
        this.friendlyName = f;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String s) {
        this.selfLink = s;
    }

    public List<RestServiceArtifactRequest.Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<RestServiceArtifactRequest.Contact> c) {
        this.contacts = c;
    }

    public Integer getRateLimitTps() {
        return rateLimitTps;
    }

    public void setRateLimitTps(Integer r) {
        this.rateLimitTps = r;
    }
}
