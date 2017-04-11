import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class Tech extends InfoReceiver implements Runnable {

    private final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

    private final Consumer patient = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(final String consumerTag, final Envelope envelope,
                                   final AMQP.BasicProperties properties, final byte[] body) throws IOException {
            final AMQP.BasicProperties replyId = new AMQP.BasicProperties.Builder()
                    .correlationId(properties.getCorrelationId()).build();

            final String message = new String(body, "UTF-8");
            System.out.println("Examinating " + message + " for " + envelope.getRoutingKey());

            try {
                Thread.sleep(10000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            channel.basicPublish(Utils.COMMON_EXCHANGE_NAME, properties.getReplyTo(), replyId,
                    new String(message + " examination").getBytes());
            channel.basicAck(envelope.getDeliveryTag(), false);

            System.out.println("Examinating " + message + " for " + envelope.getRoutingKey() + " ended");
        }
    };

    @Override
    public void run() {
        try {
            System.out.println("Starting Tech");

            final ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            final Connection connection = factory.newConnection();
            channel = connection.createChannel();

            startReceivingInfo();

            channel.exchangeDeclare(Utils.COMMON_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
            prepareQueues();
            channel.basicQos(1);

            registerForInjuries();

            System.out.println("Waiting for patient");

            String line;
            while ((line = inputReader.readLine()) != null) {
                if (line.equals("quit")) {
                    break;
                }
            }

            channel.close();
            connection.close();
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void registerForInjuries() throws IOException {
        String line;
        line = inputReader.readLine();
        channel.basicConsume(Injury.valueOf(line).toString(), false, patient);

        line = inputReader.readLine();
        channel.basicConsume(Injury.valueOf(line).toString(), false, patient);
    }

    private void prepareQueues() throws IOException {
        prepareQueue(Injury.ANKLE.toString());
        prepareQueue(Injury.ELBOW.toString());
        prepareQueue(Injury.KNEE.toString());
    }

    private void prepareQueue(final String key) throws IOException {
        channel.queueDeclare(key, false, false, false, null);
        channel.queueBind(key, Utils.COMMON_EXCHANGE_NAME, key);
    }

    public static void main(final String[] args) {
        new Tech().run();
    }
}
