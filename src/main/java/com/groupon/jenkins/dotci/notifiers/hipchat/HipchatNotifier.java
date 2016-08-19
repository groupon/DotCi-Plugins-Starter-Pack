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
package com.groupon.jenkins.dotci.notifiers.hipchat;

import com.groupon.jenkins.dotci.notifiers.HipchatConfig;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.notifications.PostBuildNotifier;
import hudson.Extension;
import hudson.model.BuildListener;
import hudson.model.Result;
import org.apache.commons.httpclient.HttpClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Extension
public class HipchatNotifier extends PostBuildNotifier {

    public HipchatNotifier() {
        super("hipchat");
    }

    @Override
    public boolean notify(DynamicBuild build, BuildListener listener) {
        List rooms = getRooms();
        listener.getLogger().println("sending hipchat notifications");
        String token = getHipchatConfig().getToken();
        for (Object roomId : rooms) {

            BuildCause.CommitInfo commitInfo = null;
            BuildCause cause = build.getCause(BuildCause.class);
            if(cause != null){
                 commitInfo = cause.getCommitInfo();
            }
            try {
                new SendRoomMessageWithCardRequest(roomId.toString(),token,getColor(build),shouldNotify(build), getNotificationMessage(build,listener),build.getFullUrl(),commitInfo).execute();
            } catch (Exception e) {
               e.printStackTrace(listener.getLogger());
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


    private MessageColor getColor(DynamicBuild build) {
        return Result.FAILURE.equals(build.getResult()) ? MessageColor.RED : MessageColor.GREEN;
    }

    private boolean shouldNotify(DynamicBuild build) {
        return Result.FAILURE.equals(build.getResult())? true : false;
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
