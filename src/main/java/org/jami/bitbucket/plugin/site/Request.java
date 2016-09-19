package org.jami.bitbucket.plugin.site;

public class Request {

    private String project;

    private String repository;

    private String commit;

    private String path;

    public Request(String project, String repository, String commit, String path) {
        this.project = project;
        this.repository = repository;
        this.commit = commit;
        this.path = path;
    }

    public String getProject() {
        return project;
    }

    public String getRepository() {
        return repository;
    }

    public String getCommit() {
        return commit;
    }

    public String getPath() {
        return path;
    }
}
