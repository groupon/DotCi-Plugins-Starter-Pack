package com.groupon.jenkins.dotci.notifiers;

import hudson.model.BuildListener;
import hudson.model.Result;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicBuild;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HipchatNotifierTest {

    @Test
    public void should_post_failure_message_on_failed_build_in_specified_hipchat_room() throws HttpException, IOException {
        HipchatNotifier hipchatNotifier = spy(new HipchatNotifier());
        HttpClient httpClient = mock(HttpClient.class);
        doReturn(httpClient).when(hipchatNotifier).getHttpClient();
        HipchatConfig hipchatConfig = mock(HipchatConfig.class);
        when(hipchatConfig.getToken()).thenReturn("hipchat_token");
        doReturn(hipchatConfig).when(hipchatNotifier).getHipchatConfig();
        DynamicBuild build = getFailedBuild();
        hipchatNotifier.setOptions(Arrays.asList("room1"));

        hipchatNotifier.notify(build, getMockListener());

        ArgumentCaptor<PostMethod> argument = ArgumentCaptor.forClass(PostMethod.class);
        verify(httpClient).executeMethod(argument.capture());
        PostMethod post = argument.getValue();
        assertEquals("https://api.hipchat.com/v1/rooms/message?auth_token=hipchat_token", post.getURI().toString());
        assertEquals("CI", post.getParameter("from").getValue());
        assertEquals("red", post.getParameter("color").getValue());
    }

    protected DynamicBuild getFailedBuild() {
        DynamicBuild build = mock(DynamicBuild.class);
        when(build.getResult()).thenReturn(Result.FAILURE);
        when(build.getParent()).thenReturn(mock(DynamicProject.class));
        return build;
    }

    private BuildListener getMockListener() {
        BuildListener buildListener = mock(BuildListener.class);
        PrintStream logger = mock(PrintStream.class);
        when(buildListener.getLogger()).thenReturn(logger);
        return buildListener;
    }

}
