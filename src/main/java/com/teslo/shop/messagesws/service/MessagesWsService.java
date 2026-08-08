package com.teslo.shop.messagesws.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.teslo.shop.auth.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class MessagesWsService {

    private record ConnectedClient(SocketIOClient socket, User user) {
    }

    private final Map<String, ConnectedClient> connectedClients = new ConcurrentHashMap<>();

    public void registerClient(SocketIOClient client, User user) {
        checkUserConnection(user);
        connectedClients.put(client.getSessionId().toString(), new ConnectedClient(client, user));
    }

    public void removeClient(String clientId) {
        connectedClients.remove(clientId);
    }

    public List<String> getConnectedClients() {
        return new ArrayList<>(connectedClients.keySet());
    }

    public String getUserFullName(String socketId) {
        ConnectedClient connectedClient = connectedClients.get(socketId);
        return connectedClient != null ? connectedClient.user().getFullName() : null;
    }

    private void checkUserConnection(User user) {
        for (ConnectedClient connectedClient : connectedClients.values()) {
            if (connectedClient.user().getId().equals(user.getId())) {
                connectedClient.socket().disconnect();
                break;
            }
        }
    }
}
