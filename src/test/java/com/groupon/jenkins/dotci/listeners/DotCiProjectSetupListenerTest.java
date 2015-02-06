package com.groupon.jenkins.dotci.listeners;

import hudson.plugins.ansicolor.AnsiColorBuildWrapper;
import hudson.plugins.build_timeout.BuildTimeoutWrapper;
import hudson.plugins.build_timeout.impl.AbsoluteTimeOutStrategy;
import hudson.util.DescribableList;

import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.groupon.jenkins.dynamic.build.DynamicProject;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DotCiProjectSetupListenerTest {

	@Test
	public void should_set_build_timeout_to_60_mins_and_adds_ansi_color_xterm() throws Exception {
		DynamicProject project = mock(DynamicProject.class);
		DescribableList wrapperList = mock(DescribableList.class);
		when(project.getBuildWrappersList()).thenReturn(wrapperList);
		DotCiProjectSetupListener dotCiProjectSetupListener = new DotCiProjectSetupListener();
		dotCiProjectSetupListener.onCreated(project);

		ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
		verify(wrapperList, atMost(2)).add(argument.capture());
		List wrappers = argument.getAllValues();

        BuildTimeoutWrapper timeoutWrapper = (BuildTimeoutWrapper) wrappers.get(0);
        AbsoluteTimeOutStrategy absoluteTimeOutStrategy = (AbsoluteTimeOutStrategy)timeoutWrapper.getStrategy();
		assertEquals("60", absoluteTimeOutStrategy.getTimeoutMinutes());
		assertEquals("xterm", ((AnsiColorBuildWrapper) wrappers.get(1)).getColorMapName());

	}
}
