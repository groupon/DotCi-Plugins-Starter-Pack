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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.notifications.PostBuildNotifier;
import hudson.Extension;
import hudson.model.BuildListener;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class WebhookNotifier extends PostBuildNotifier {
	private static final Logger LOGGER = Logger.getLogger(WebhookNotifier.class.getName());

	public WebhookNotifier() {
		super("webhook");
	}

	@Override
	protected Type getType() {
		return PostBuildNotifier.Type.ALL;
	}

	@Override
	protected boolean notify(DynamicBuild build, BuildListener listener) {
		Map<String, ?> options = (Map<String, ?>) getOptions();
		HttpClient client = getHttpClient();
		String requestUrl = (String) options.get("url");
		PostMethod post = new PostMethod(requestUrl);

		Map<String, String> payload = (Map<String, String>) options.get("payload");
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			String payloadJson = objectMapper.writeValueAsString(payload);
			StringRequestEntity requestEntity = new StringRequestEntity(payloadJson, "application/json", "UTF-8");
			post.setRequestEntity(requestEntity);
			int statusCode = client.executeMethod(post);
			listener.getLogger().println("Posted Paylod " + payloadJson + " to " + requestUrl + " with response code " + statusCode);
		} catch (Exception e) {
			listener.getLogger().print("Failed to make a POST to webhook. Check Jenkins logs for exceptions.");
			LOGGER.log(Level.WARNING, "Error posting to webhook", e);
			return false;
		} finally {
			post.releaseConnection();
		}
		return false;
	}

	protected HttpClient getHttpClient() {
		return new HttpClient();
	}
}
