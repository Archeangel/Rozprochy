import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class Doc extends InfoReceiver implements Runnable {

    private final List<String> corrIds = new LinkedList<>();
    private String feedbackQueueName;

    @Override
    public void run() {
        try {
            System.out.println("Starting the Docter");
            final ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(Utils.HOST_NAME);
            final Connection connection = factory.newConnection();
            channel = connection.createChannel();

            startReceivingInfo();

            channel.exchangeDeclare(Utils.COMMON_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

            feedbackQueueName = channel.queueDeclare().getQueue();
            channel.queueBind(feedbackQueueName, Utils.COMMON_EXCHANGE_NAME, feedbackQueueName);

            channel.basicConsume(feedbackQueueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(final String consumerTag, final Envelope envelope,
                                           final AMQP.BasicProperties properties, final byte[] body) throws IOException {
                    if (corrIds.contains(properties.getCorrelationId())) {
                        corrIds.remove(properties.getCorrelationId());
                        System.out.println("Feedback: " + new String(body));
                    }
                }
            });

            final Scanner scanner = new Scanner(System.in);
            while (true) {
                final String injury = scanner.next();
                if (injury.equals("quit")) {
                    break;
                }

                final String name = scanner.next();

                send(Injury.valueOf(injury), name);
            }

            scanner.close();
            channel.close();
            connection.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void send(final Injury injury, final String name) throws IOException {
        final String corrId = UUID.randomUUID().toString();
        corrIds.add(corrId);

        final AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(corrId)
                .replyTo(feedbackQueueName).build();

        channel.basicPublish(Utils.COMMON_EXCHANGE_NAME, injury.toString(), props, name.getBytes());
        System.out.println("Send " + injury.toString() + " " + name);

    }

    public static void main(final String[] args) {
        new Doc().run();
    }
}
