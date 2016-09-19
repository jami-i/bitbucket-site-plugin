package org.jami.bitbucket.plugin.site;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.content.ContentService;
import com.atlassian.bitbucket.content.NoSuchPathException;
import com.atlassian.bitbucket.io.TypeAwareOutputSupplier;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.repository.Branch;
import com.atlassian.bitbucket.repository.RefService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.user.ApplicationUser;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class SiteServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(SiteServlet.class);

    private static final Duration expires = Duration.standardMinutes(10);

    private final RepositoryService repositoryService;

    private final RefService refService;
    private final ContentService contentService;

    private final AuthenticationContext authenticationContext;
    private final PermissionService permissionService;

    public SiteServlet(RepositoryService repositoryService,
                       RefService refService,
                       ContentService contentService,
                       AuthenticationContext authenticationContext,
                       PermissionService permissionService)
    {
        this.repositoryService = repositoryService;
        this.refService = refService;

        this.contentService = contentService;

        this.authenticationContext = authenticationContext;
        this.permissionService = permissionService;
    }

    @Override
    protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        RequestCreator<String> creator = new RequestCreatorImpl();

        final Request request = creator.create(pathInfo);

        if(request == null) {
            response404(resp);
        } else {

            Repository repository;
            try {
                repository = repositoryService.getBySlug(request.getProject(), request.getRepository());
            } catch (Exception e) {
                response404(resp);
                return;
            }

            if (repository == null) {
                response404(resp);
            } else {
                try {

                    boolean canRead = true;

                    if(!permissionService.isPubliclyAccessible(repository)) {
                        ApplicationUser currentUser = authenticationContext.getCurrentUser();
                        canRead = permissionService.hasRepositoryPermission(currentUser, repository, Permission.REPO_READ);
                    }

                    if(!canRead) {
                        response404(resp);
                    } else {

                        String commit = resolveCommit(request, repository);

                        contentService.streamFile(repository, commit, request.getPath(), new TypeAwareOutputSupplier() {
                            @Nonnull
                            public OutputStream getStream(@Nonnull String s) throws IOException {
                                resp.setContentType(s);
                                resp.setDateHeader("Expires", getExpiresMillis());

                                return resp.getOutputStream();
                            }
                        });
                    }
                } catch (NoSuchPathException e) {
                    log.debug("target path not found", e);
                    response404(resp);
                }
            }
        }

    }

    private long getExpiresMillis() {
        return DateTime.now()
                .plus(expires)
                .withZone(DateTimeZone.UTC)
                .getMillis();
    }

    private String resolveCommit(Request request, Repository repository) {
        String commit;
        if(StringUtils.isEmpty(request.getCommit())) {
            Branch defaultBranch = refService.getDefaultBranch(repository);
            commit = defaultBranch.getId();
        } else {
            commit = request.getCommit();
        }
        return commit;
    }

    private void response404 (HttpServletResponse resp) throws IOException {
        resp.setStatus(404);
        resp.setContentType("text/html");
        resp.getWriter().write("<html><body><h1>File Not Found.</h1></body></html>");
    }

}
