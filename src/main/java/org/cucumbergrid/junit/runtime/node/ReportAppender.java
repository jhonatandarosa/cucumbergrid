package org.cucumbergrid.junit.runtime.node;

import java.io.Serializable;
import org.cucumbergrid.junit.runtime.common.FormatMessage;
import org.cucumbergrid.junit.runtime.common.FormatMessageID;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;

public class ReportAppender {

    private CucumberGridNodeRuntime runtime;

    public ReportAppender(CucumberGridNodeRuntime runtime) {
        this.runtime = runtime;
    }

    public final void addReportInfo(String key, Serializable value) {
        FormatMessage formatMsg = new FormatMessage(FormatMessageID.FEATURE_EXTRA_INFO, key, value);
        Message msg = new Message(MessageID.FORMAT, formatMsg);
        runtime.send(msg);
    }
}
