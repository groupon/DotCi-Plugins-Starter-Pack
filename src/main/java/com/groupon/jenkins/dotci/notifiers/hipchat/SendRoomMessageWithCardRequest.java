package com.groupon.jenkins.dotci.notifiers.hipchat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.Consts;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;

public class SendRoomMessageWithCardRequest{
    private final BuildCause.CommitInfo commitInfo;
    private String idOrName;
    private String accessToken;
    private MessageColor color;
    private String notificationMessage;
    private Boolean notify;
    private MessageFormat messageFormat;
    private String buildLink;
    private final ObjectMapper objectMapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    protected final ObjectWriter objectWriter = objectMapper.writer();

    public SendRoomMessageWithCardRequest(String idOrName, String accessToken, MessageColor color, boolean notify,String notificationMessage, String buildLink, BuildCause.CommitInfo commitInfo) {
        this.idOrName = idOrName;
        this.accessToken = accessToken;
        this.color = color;
        this.notificationMessage = notificationMessage;
        this.buildLink = buildLink;
        this.notify = notify;
        this.messageFormat = MessageFormat.HTML;
        this.commitInfo = commitInfo;
    }



    protected String getPath() {
        return "/v2/room/" + idOrName + "/notification";
    }

    protected Map<String, Object> toQueryMap() {
        Map<String, Object> params = new HashMap();
        if (color != null) {
            params.put("color", color.name().toLowerCase());
        }
        params.put("message", notificationMessage);
        if (notify != null) {
            params.put("notify", notify);
        }
        if (messageFormat != null) {
            params.put("message_format", "text");
        }
        if(commitInfo != null){
            params.put("card",getCard());
        }
        return params;
    }

    private Object getCard() {
        HashMap<String, Object> card = new HashMap<>();
        card.put( "style", "application");
        card.put("url", this.commitInfo.getCommitUrl());
        card.put("title", this.commitInfo.getBranch()+ "@"+this.commitInfo.getShortSha());
        card.put( "description",   of("value",   this.commitInfo.getMessage(),"format","html"));
        card.put("format","medium");
        card.put("id",new Date().getTime() + "");
        card.put("icon", of("url",this.commitInfo.getAvatarUrl()));
        card.put("activity", of("html", String.format("<a href=\"%s\">%s: %s</a>",this.buildLink,this.commitInfo.getCommitterName(), this.notificationMessage)));
        return card;
    }



    public int execute() throws IOException {
        org.apache.commons.httpclient.HttpClient client = getHttpClient();
        Map<String, Object> params = toQueryMap();
        String body = objectWriter.writeValueAsString(params);
        String encodedPath = getEncodedPath();
        PostMethod post = new PostMethod(encodedPath);
        post.addRequestHeader("Authorization", "Bearer " + accessToken);
        post.addRequestHeader("Content-Type", "application/json");
        post.setRequestEntity(new StringRequestEntity(body,"application/json",Consts.UTF_8.toString()));
        int responseCode = client.executeMethod(post);
        if(responseCode != 200 && responseCode != 204){
            throw new RuntimeException("Could not post notification: "+ new String(post.getResponseBody()));
        }
        return  responseCode;
    }

    private HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        HostConfiguration hostConfig = new HostConfiguration();
        hostConfig.setHost("https://api.hipchat.com");
        client.setHostConfiguration(hostConfig);
        return client;
    }

    protected String getEncodedPath() {
        String path = getPath();
        String[] tokens = path.split("/");
        String encodedPath = "";
        URLCodec urlCodec = new URLCodec();
        try {
            for (String token : tokens) {
                if (!token.isEmpty()) {
                    //replace + to %20
                    encodedPath += "/" + urlCodec.encode(token).replace("+", "%20");
                }
            }
        } catch (EncoderException e) {
           throw new RuntimeException(e);
        }
        return encodedPath;
    }
}
