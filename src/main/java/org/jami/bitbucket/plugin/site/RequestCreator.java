package org.jami.bitbucket.plugin.site;

/**
 * Created by jami on 2016/09/19.
 */
public interface RequestCreator<T> {

    Request create(T t);
}
