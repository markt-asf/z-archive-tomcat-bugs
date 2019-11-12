package org.apache.tomcat.bug63898;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class MyTag extends TagSupport {

    private static final long serialVersionUID = 1L;

    private String data;

    public void setData(final String data) {
        this.data = "String [" + data + "]";
    }

    public void setData(final Object data) {
        this.data = "Object [" + data + "]";
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            pageContext.getOut().print("<p>" + data + "</p>");
        } catch (IOException e) {
            throw new JspException(e);
        }
        return super.doStartTag();
    }
}
