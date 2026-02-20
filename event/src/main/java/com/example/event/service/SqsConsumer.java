package com.example.event.service;

import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Service
public class SqsConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SqsConsumer.class);

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final EventService eventService;

    public SqsConsumer(
            @Value("${aws.sqs.queue-url:#{null}}") String queueUrl,
            @Value("${aws.sqs.region:eu-west-2}") String region,
            EventService eventService) {
        this.queueUrl = queueUrl;
        this.eventService = eventService;

        if (queueUrl != null && !queueUrl.isEmpty()) {
            this.sqsClient = SqsClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            logger.info("SQS consumer initialized, polling queue: {}", queueUrl);
        } else {
            this.sqsClient = null;
            logger.info("SQS queue not configured -- ticket updates received via HTTP only");
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void pollMessages() {
        if (sqsClient == null || queueUrl == null) {
            return;
        }

        try {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

            for (Message message : messages) {
                try {
                    processMessage(message);
                    deleteMessage(message);
                } catch (Exception e) {
                    logger.error("Error processing SQS message: {}", message.messageId(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Error polling SQS queue", e);
        }
    }

    private void processMessage(Message message) {
        JSONObject json = new JSONObject(message.body());
        int eventId = json.getInt("eventId");
        int ticketChange = json.getInt("ticketChange");

        logger.info("Processing ticket update from SQS: eventId={}, ticketChange={}", eventId, ticketChange);

        if (ticketChange < 0) {
            eventService.bookTickets(eventId, Math.abs(ticketChange));
        } else {
            eventService.returnTickets(eventId, ticketChange);
        }

        logger.info("Ticket update processed successfully for event: {}", eventId);
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();
        sqsClient.deleteMessage(deleteRequest);
    }
}
