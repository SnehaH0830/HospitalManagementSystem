package com.hospital.appointment.controller;

import com.hospital.appointment.dto.QueueUpdateDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * WebSocket STOMP controller for broadcasting real-time queue updates.
 *
 * Clients subscribe to /topic/queue-updates and receive a QueueUpdateDto JSON payload
 * whenever the doctor advances the queue. The DoctorController broadcasts via
 * SimpMessagingTemplate on every call-next, complete, and no-show action.
 *
 * This endpoint also supports client-side pings: a patient page can send a message
 * to /app/queue/ping and will receive the echoed update (useful for reconnect scenarios).
 */
@Controller
public class QueueWebSocketController {

    /**
     * Simple echo endpoint for client-initiated pings.
     * Receives messages from clients via /app/queue/ping
     * and re-broadcasts them to all /topic/queue-updates subscribers.
     *
     * @param update the queue update DTO sent by the client
     * @return the same DTO, broadcast to all subscribers
     */
    @MessageMapping("/queue/ping")
    @SendTo("/topic/queue-updates")
    public QueueUpdateDto ping(QueueUpdateDto update) {
        return update;
    }
}
