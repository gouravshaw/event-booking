package com.example.booking.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class SqsPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SqsPublisher.class);

    private final SqsClient sqsClient;
    private final String queueUrl;

    public SqsPublisher(
            @Value("${aws.sqs.queue-url:#{null}}") String queueUrl,
            @Value("${aws.sqs.region:eu-west-2}") String region) {
        this.queueUrl = queueUrl;

        if (queueUrl != null && !queueUrl.isEmpty()) {
            this.sqsClient = SqsClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            logger.info("SQS publisher initialized with queue: {}", queueUrl);
        } else {
            this.sqsClient = null;
            logger.info("SQS queue not configured -- using direct HTTP calls to event-service");
        }
    }

    public boolean isEnabled() {
        return sqsClient != null && queueUrl != null;
    }

    /**
     * Publish a ticket update message to SQS.
     * @param eventId the event ID
     * @param ticketChange negative for booking, positive for cancellation/return
     */
    public boolean publishTicketUpdate(int eventId, int ticketChange) {
        if (!isEnabled()) {
            return false;
        }

        try {
            JSONObject message = new JSONObject();
            message.put("eventId", eventId);
            message.put("ticketChange", ticketChange);
            message.put("timestamp", System.currentTimeMillis());

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(message.toString())
                    .build();

            sqsClient.sendMessage(request);
            logger.info("Published ticket update to SQS: eventId={}, ticketChange={}", eventId, ticketChange);
            return true;
        } catch (Exception e) {
            logger.error("Failed to publish to SQS, falling back to HTTP", e);
            return false;
        }
    }
}
