package org.jsystemtest.bean;

/**
 * Created with IntelliJ IDEA.
 * User: mgoldyan
 * Date: 1/27/14
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Page {
    private int pageCount = 220;

    public Page() {}

    /*public Page(int pageCount) {
        this.pageCount = pageCount;
    }*/

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}
