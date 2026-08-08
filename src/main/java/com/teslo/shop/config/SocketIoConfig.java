package com.teslo.shop.config;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.teslo.shop.auth.entity.User;
import com.teslo.shop.auth.jwt.JwtService;
import com.teslo.shop.auth.repository.UserRepository;
import com.teslo.shop.messagesws.dto.NewMessageDto;
import com.teslo.shop.messagesws.service.MessagesWsService;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @Configuration
public class SocketIoConfig {

    // @Bean(destroyMethod = "stop")
    public SocketIOServer socketIOServer(
        AppProperties properties,
        JwtService jwtService,
        UserRepository userRepository,
        MessagesWsService messagesWsService
    ) {
        com.corundumstudio.socketio.Configuration config =
            new com.corundumstudio.socketio.Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(properties.getSocketio().getPort());
        config.setOrigin("*");

        SocketIOServer server = new SocketIOServer(config);

        server.addConnectListener((SocketIOClient client) -> {
            String token = client
                .getHandshakeData()
                .getHttpHeaders()
                .get("authentication");
            try {
                String userId = jwtService.extractUserId(token);
                User user = userRepository
                    .findById(UUID.fromString(userId))
                    .orElse(null);
                if (user == null || !user.isActive()) {
                    client.disconnect();
                    return;
                }
                messagesWsService.registerClient(client, user);
            } catch (Exception e) {
                client.disconnect();
                return;
            }
            server
                .getBroadcastOperations()
                .sendEvent(
                    "clients-updated",
                    messagesWsService.getConnectedClients()
                );
        });

        server.addDisconnectListener((SocketIOClient client) -> {
            messagesWsService.removeClient(client.getSessionId().toString());
            server
                .getBroadcastOperations()
                .sendEvent(
                    "clients-updated",
                    messagesWsService.getConnectedClients()
                );
        });

        server.addEventListener(
            "message-from-client",
            NewMessageDto.class,
            (client, data, ackSender) -> {
                String message =
                    data != null &&
                    data.message() != null &&
                    !data.message().isBlank()
                        ? data.message()
                        : "no-message!!";
                String fullName = messagesWsService.getUserFullName(
                    client.getSessionId().toString()
                );
                server
                    .getBroadcastOperations()
                    .sendEvent(
                        "message-from-server",
                        Map.of("fullName", fullName, "message", message)
                    );
            }
        );

        server.start();
        return server;
    }
}
