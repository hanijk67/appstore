package com.fanap.midhco.appstore.service.engine;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.applicationUtils.WebSocketClient;
import com.fanap.midhco.appstore.applicationUtils.ZipUtil;
import com.fanap.midhco.appstore.applicationUtils.asyncMessages.*;
import com.fanap.midhco.appstore.entities.async.MessageVO;
import com.fanap.midhco.appstore.entities.async.MessageWrapperVO;
import com.fanap.midhco.appstore.restControllers.vos.OrganizationVO;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.xerces.impl.dv.util.Base64;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by admin123 on 1/21/2017.
 */
public class EngineOrganizationProviderService {
    public static final EngineOrganizationProviderService INSTANCE = new EngineOrganizationProviderService();

    static WebSocketClient webSocketClient;
    List<OrganizationVO> loadedOrganizations = new ArrayList<>();

    public CompletableFuture<List<OrganizationVO>> getOrganizations() {
        CompletableFuture completableFuture = new CompletableFuture();
        if (!loadedOrganizations.isEmpty()) {
            completableFuture.complete(loadedOrganizations);
        }

        webSocketClient = new WebSocketClient(URI.create(
                ConfigUtil.getProperty(ConfigUtil.ORGANIZATION_ASYNC_WEBSOCKET_URL)
        )) {
            String deviceId;
            String peerId;

            @Override
            public void onOpen(ServerHandshake handshakedata) {
            }

            @Override
            public void onMessage(String message) {
                try {
                    JSONObject jsonObject = new JSONObject(message);

                    int type = jsonObject.getInt("type");

                    if (type == 0) {
                        deviceId = ConfigUtil.getProperty(ConfigUtil.ORGANIZATION_DEVICEID);

                        PeerMessage peerInfoVO = new PeerMessage();
                        peerInfoVO.setAppId(ConfigUtil.getProperty(ConfigUtil.ORGANIZATION_APPID));
                        peerInfoVO.setDeviceId(deviceId);

                        String peerInfoVOJson = JsonUtil.getJson(peerInfoVO);

                        MessageWrapperVO messageWrapperVO = new MessageWrapperVO();
                        messageWrapperVO.setType((byte) 2);
                        messageWrapperVO.setContent(peerInfoVOJson);

                        String messageWrapperVOString = JsonUtil.getJson(messageWrapperVO);

                        EngineOrganizationProviderService.sendClient(messageWrapperVOString);

                    } else if (type == 2) {
                        JSONObject object = new JSONObject(message);
                        String content = object.getString("content");
                        peerId = content;

                        QeeMessage qeeMessage = new QeeMessage();
                        qeeMessage.setContent(peerId);
                        qeeMessage.setName(ConfigUtil.getProperty(ConfigUtil.ORGANIZATION_QUEUENAME));

                        String qeeMessageJson = JsonUtil.getJson(qeeMessage);
                        MessageWrapperVO messageWrapperVO = new MessageWrapperVO();
                        messageWrapperVO.setType((byte) 1);
                        messageWrapperVO.setContent(qeeMessageJson);

                        String messageWrapperVOString = JsonUtil.getJson(messageWrapperVO);

                        sendClient(messageWrapperVOString);

                    } else if (type == 1) {
                        MessageVO vo = new MessageVO();
                        vo.setMessageId(new Date().getTime());
                        vo.setPeerName(ConfigUtil.getProperty(ConfigUtil.ORGANIZATION_ENGINE_ENDPOINT_NAME));

                        Request request = new Request();
                        RequestHeader requestHeader = new RequestHeader();
                        request.setRequestHeader(requestHeader);
                        requestHeader.setId_token(ConfigUtil.getProperty(ConfigUtil.ORGANIZATION_SERVICE_IDTOKEN));

                        request.setMessageType(EngineMessageType.GET_OU_LIST);

                        vo.setContent(Base64.encode(ZipUtil.INSTANCE.compress(JsonUtil.getJson(request).getBytes())));
                        String messageVOString = JsonUtil.getJson(vo);

                        MessageWrapperVO messageWrapperVO = new MessageWrapperVO();
                        messageWrapperVO.setType((byte) 3);
                        messageWrapperVO.setContent(messageVOString);

                        String m_message = JsonUtil.getJson(messageWrapperVO);
                        sendClient(m_message);
                    } else if (type == 3) {
                        try {
                            String organizationContentAsString = new JSONObject(jsonObject.getString("content")).getString("content");
                            List<OrganizationVO> organizationList = JsonUtil.getObject(organizationContentAsString, new TypeReference<List<OrganizationVO>>() {
                            });
                            loadedOrganizations.clear();
                            loadedOrganizations.addAll(organizationList);
                            completableFuture.complete(organizationList);
                        } catch (Exception ex) {
                            completableFuture.completeExceptionally(ex);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onError(Exception ex) {
            }
        };

        webSocketClient.connect();

        return completableFuture;
    }

    private static void sendClient(String message) {
        try {
            webSocketClient.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
