package org.jami.bitbucket.plugin.site;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jami on 2016/09/19.
 */
public class RequestCreatorImplTest {
    @Test
    public void create() throws Exception {

        RequestCreator<String> c = new RequestCreatorImpl();

        c.create("/project/repo/master/-/nested/file.html");


    }

}