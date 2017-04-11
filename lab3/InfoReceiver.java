import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public abstract class InfoReceiver {
    protected Channel channel;

    protected void startReceivingInfo() throws IOException {
        channel.exchangeDeclare(Utils.ALL_EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        final String infoQueue = channel.queueDeclare().getQueue();
        channel.queueBind(infoQueue, Utils.ALL_EXCHANGE_NAME, Utils.ALL_ROUTING_KEY);
        channel.basicConsume(infoQueue, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(final String consumerTag, final Envelope envelope,
                                       final AMQP.BasicProperties properties, final byte[] body) throws IOException {
                System.out.println("info: " + new String(body));
            }
        });
    }

}
