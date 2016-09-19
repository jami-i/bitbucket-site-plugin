package org.jami.bitbucket.plugin.site;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestCreatorImpl implements RequestCreator<String> {

    private static final Pattern p = Pattern.compile("^/([^/]+)/([^/]+)(/(.+))?/-/(.+)");

    public Request create(String s) {

        Matcher matcher = p.matcher(s);

        if(matcher.find()) {

            String project = matcher.group(1);
            String repository = matcher.group(2);
            String branch = matcher.group(4);
            String path = matcher.group(5);

            return new Request(project, repository, branch, path);

        }

        return null;
    }
}
