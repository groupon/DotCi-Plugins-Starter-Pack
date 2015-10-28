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

import hudson.Extension;
import hudson.model.BuildListener;
import hudson.model.Result;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.notifications.PostBuildNotifier;

@Extension
public class HipchatNotifier extends PostBuildNotifier {
    private static final Logger LOGGER = Logger.getLogger(HipchatNotifier.class.getName());

    public HipchatNotifier() {
        super("hipchat");
    }

    @Override
    public boolean notify(DynamicBuild build, BuildListener listener) {
        List rooms = getRooms();
        listener.getLogger().println("sending hipchat notifications");
        for (Object roomId : rooms) {
            HttpClient client = getHttpClient();
            String url = "https://api.hipchat.com/v1/rooms/message?auth_token=" + getHipchatConfig().getToken();
            PostMethod post = new PostMethod(url);
            String urlMsg = " (<a href='" + build.getFullUrl() + "'>Open</a>)";

            try {
                post.addParameter("from", "CI");
                post.addParameter("room_id", roomId.toString());
                post.addParameter("message", getNotificationMessage(build, listener)+ " " + urlMsg);
                post.addParameter("color", getColor(build, listener));
                post.addParameter("notify", shouldNotify(getColor(build, listener)));
                post.getParams().setContentCharset("UTF-8");
                client.executeMethod(post);
            } catch (Exception e) {
                listener.getLogger().print("Failed to send hipchat notifications. Check Jenkins logs for exceptions.");
                LOGGER.log(Level.WARNING, "Error posting to HipChat", e);
            } finally {
                post.releaseConnection();
            }
        }
        return true;
    }

    @Override
    protected String getNotificationMessage(DynamicBuild build, BuildListener listener) {
        if(getOptions() instanceof  Map && ((Map)getOptions()).containsKey("message")){
            return (String) ((Map)getOptions()).get("message");
        }
        return super.getNotificationMessage(build, listener);
    }

    protected HipchatConfig getHipchatConfig() {
        return HipchatConfig.get();
    }

    protected HttpClient getHttpClient() {
        return new HttpClient();
    }

    private List getRooms() {
       Object rooms = getRoomsConfig();
        return (List) (rooms instanceof List ? rooms: Arrays.asList(rooms));
    }

    private Object getRoomsConfig() {
        if( getOptions() instanceof Map ){
            Map options = (Map) getOptions();
            return options.containsKey("room")? options.get("room"): options.get("rooms");
        }
        return getOptions();
    }


    private String getColor(DynamicBuild build, BuildListener listener) {
        return Result.FAILURE.equals(build.getResult()) ? "red" : "green";
    }

    private String shouldNotify(String color) {
        return color.equalsIgnoreCase("green") ? "0" : "1";
    }

    @Override
    protected Type getType() {
        if(getOptions() instanceof  Map){
            Map options = (Map) getOptions();
            if(options.containsKey("notify_on")){
                return PostBuildNotifier.Type.valueOf(((String)options.get("notify_on")).toUpperCase());
            }
        }
        return PostBuildNotifier.Type.FAILURE_AND_RECOVERY;
    }

}
