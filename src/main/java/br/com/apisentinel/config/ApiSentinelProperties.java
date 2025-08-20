package br.com.apisentinel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apisentinel")
public class ApiSentinelProperties {

    private Output output = new Output();
    private Scheduling scheduling = new Scheduling();
    private Ssl ssl = new Ssl();
    private Csv csv = new Csv();
    private ExternalApi externalapi = new ExternalApi();
    private Governance governance = new Governance();
    private Gitlab gitlab = new Gitlab();
    private Sync sync = new Sync();

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output o) {
        this.output = o;
    }

    public Scheduling getScheduling() {
        return scheduling;
    }

    public Ssl getSsl() {
        return ssl;
    }

    public Csv getCsv() {
        return csv;
    }

    public ExternalApi getExternalApi() {
        return externalapi;
    }

    public Governance getGovernance() {
        return governance;
    }

    public Gitlab getGitlab() {
        return gitlab;
    }

    public Sync getSync() {
        return sync;
    }

    public static class Output {
        private String discovery = "build/discovery.csv";
        private String inconsistencies = "build/inconsistencies.csv";

        public String getDiscovery() {
            return discovery;
        }

        public void setDiscovery(String d) {
            this.discovery = d;
        }

        public String getInconsistencies() {
            return inconsistencies;
        }

        public void setInconsistencies(String i) {
            this.inconsistencies = i;
        }
    }

    public static class Scheduling {
        private boolean enabled = true;
        private String cron = resolveDefaultCron();

        private static String resolveDefaultCron() {
            String fromSys = System.getProperty("apisentinel.scheduling.cron");
            if (fromSys != null && !fromSys.isBlank()) return fromSys;

            String fromEnv = System.getenv("EXTRACTOR_SCHEDULING_CRON");
            if (fromEnv != null && !fromEnv.isBlank()) return fromEnv;

            String alt = System.getProperty("apisentinelCron");
            if (alt != null && !alt.isBlank()) return alt;

            return "0 */30 * * * *";
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean e) {
            enabled = e;
        }

        public String getCron() {
            return cron;
        }

        public void setCron(String c) {
            cron = c;
        }
    }

    public static class Ssl {
        private boolean insecure = false;

        public boolean isInsecure() {
            return insecure;
        }

        public void setInsecure(boolean i) {
            insecure = i;
        }
    }

    public static class Csv {
        private String input = resolveDefaultInput();

        private static String resolveDefaultInput() {

            String fromSys = System.getProperty("apisentinel.csv.input");
            if (fromSys != null && !fromSys.isBlank()) return fromSys;

            String fromEnv = System.getenv("EXTRACTOR_CSV_INPUT");
            if (fromEnv != null && !fromEnv.isBlank()) return fromEnv;

            String csvName = System.getProperty("csvFileName", System.getProperty("csvName"));
            if (csvName != null && !csvName.isBlank()) return "data/" + csvName;

            return "data/input.csv";
        }

        public String getInput() {
            return input;
        }

        public void setInput(String i) {
            input = i;
        }
    }

    public static class ExternalApi {
        private String baseUrl;
        private String token;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String b) {
            baseUrl = b;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String t) {
            token = t;
        }
    }

    public static class Governance {
        private String baseUrl;
        private String username;
        private String password;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String b) {
            baseUrl = b;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String u) {
            username = u;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String p) {
            password = p;
        }
    }

    public static class Gitlab {
        private String baseUrl;
        private String token;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String b) {
            baseUrl = b;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String t) {
            token = t;
        }
    }

    public static class Sync {
        private String hmacKey = "super-secret-key";
        private boolean enabled = true;

        public String getHmacKey() {
            return hmacKey;
        }

        public void setHmacKey(String k) {
            hmacKey = k;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean e) {
            enabled = e;
        }
    }
}
