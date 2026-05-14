package com.autovalor.api.websocket;

import com.autovalor.api.dto.chatDTO.MessageResponse;
import com.autovalor.api.repository.ConversationRepository;
import com.autovalor.security.JwtService;
import com.autovalor.user.User;
import com.autovalor.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;
    private final Map<Long, List<WebSocketSession>> sessionsByConversation = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(
            JwtService jwtService,
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            ObjectMapper objectMapper
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            URI uri = session.getUri();
            if (uri == null) {
                session.close(CloseStatus.BAD_DATA);
                return;
            }

            Map<String, String> params = parseQuery(uri.getRawQuery());
            Long conversationId = Long.valueOf(params.getOrDefault("conversationId", ""));
            String token = params.get("token");

            if (token == null || token.isBlank()) {
                session.close(CloseStatus.POLICY_VIOLATION.withReason("Missing token"));
                return;
            }

            User user = authenticate(token);
            conversationRepository.findForUser(conversationId, user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

            session.getAttributes().put("conversationId", conversationId);
            session.getAttributes().put("userId", user.getId());
            sessionsByConversation.computeIfAbsent(conversationId, key -> new ArrayList<>()).add(session);
        } catch (Exception ex) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Unauthorized"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object conversationIdValue = session.getAttributes().get("conversationId");
        if (!(conversationIdValue instanceof Long conversationId)) {
            return;
        }

        List<WebSocketSession> sessions = sessionsByConversation.get(conversationId);
        if (sessions == null) {
            return;
        }

        sessions.removeIf(existing -> existing.getId().equals(session.getId()) || !existing.isOpen());
        if (sessions.isEmpty()) {
            sessionsByConversation.remove(conversationId);
        }
    }

    public void broadcastMessage(MessageResponse message) {
        List<WebSocketSession> sessions = sessionsByConversation.get(message.conversationId());
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        sessions.removeIf(session -> !session.isOpen());

        for (WebSocketSession session : sessions) {
            sendMessage(session, message);
        }
    }

    private void sendMessage(WebSocketSession session, MessageResponse message) {
        Object currentUserIdValue = session.getAttributes().get("userId");
        Long currentUserId = currentUserIdValue instanceof Long value ? value : null;

        MessageResponse payload = new MessageResponse(
                message.id(),
                message.conversationId(),
                message.senderId(),
                message.senderName(),
                message.content(),
                message.createdAt(),
                message.readAt(),
                currentUserId != null && currentUserId.equals(message.senderId())
        );

        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        } catch (IOException ignored) {
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignoredAgain) {
                // Ignore close failure.
            }
        }
    }

    private User authenticate(String token) {
        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!jwtService.isTokenValid(token, user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        return user;
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new ConcurrentHashMap<>();
        if (query == null || query.isBlank()) {
            return result;
        }

        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            result.put(key, value);
        }
        return result;
    }
}
