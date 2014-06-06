/*
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.groupon.jenkins.dotci.notifiers;

import hudson.model.BuildListener;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.ImmutableMap;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebhookNotifierTest {

	@Test
	public void should_make_a_post_to_web_hook_url_with_payload() throws HttpException, IOException {

		final HttpClient httpClient = mock(HttpClient.class);
		ImmutableMap<String, String> payload = ImmutableMap.of("option1", "value1");
		final Map<String, Serializable> options = ImmutableMap.of("url", "http://example.com/", "payload", payload);
		WebhookNotifier webhookNotifier = new WebhookNotifier() {
			@Override
			protected HttpClient getHttpClient() {
				return httpClient;
			};

			@Override
			public Object getOptions() {
				return options;
			}
		};

		webhookNotifier.notify(null, getMockListener());

		ArgumentCaptor<PostMethod> argument = ArgumentCaptor.forClass(PostMethod.class);
		verify(httpClient).executeMethod(argument.capture());
		PostMethod post = argument.getValue();

		assertEquals("http://example.com/", post.getURI().toString());
		assertEquals("{\"option1\":\"value1\"}", ((StringRequestEntity) post.getRequestEntity()).getContent());

	}

	private BuildListener getMockListener() {
		BuildListener buildListener = mock(BuildListener.class);
		PrintStream logger = mock(PrintStream.class);
		when(buildListener.getLogger()).thenReturn(logger);
		return buildListener;
	}
}
