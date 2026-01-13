package es.jklabs.json.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubAsset {

    private String name;
    private String browserDownloadUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("browser_download_url")
    public String getBrowserDownloadUrl() {
        return browserDownloadUrl;
    }

    @JsonProperty("browser_download_url")
    public void setBrowserDownloadUrl(String browserDownloadUrl) {
        this.browserDownloadUrl = browserDownloadUrl;
    }
}
