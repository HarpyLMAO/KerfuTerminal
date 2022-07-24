package net.kerfu.terminal.rabbit;

import com.mongodb.client.FindIterable;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.kerfu.terminal.Terminal;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static com.mongodb.client.model.Filters.eq;

public class RabbitService {

    public void start() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("34.118.86.66");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("bT8upZTH2Qqv2DDf38TcdmxEsG3LuPPeXET9i7L11cS81C5Kk952LL9yTQ4s7v9r");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare("default", "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, "default", "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            String[] login = message.split(";");

            System.out.println("------------------------------------------------");
            System.out.println(" [x] New Login");
            System.out.println(" [x]   - IP: '" + login[0] + "'");
            System.out.println(" [x]   - Plugin: '" + login[1] + "'");
            System.out.println(" [x]   - License: '" + login[2] + "'");
            System.out.println("------------------------------------------------");


            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();

            TextChannel textChannel = Terminal.getInstance().getJda().getTextChannelById("1000142916666269816");

            assert textChannel != null;
            MessageAction unkownLogin = textChannel.sendMessageEmbeds(
                    new EmbedBuilder()
                            .setColor(Color.CYAN)
                            .setImage("https://cdn.discordapp.com/attachments/696944554640670731/944464364956704798/LOGO-SHARK.png")
                            .setTitle("Unknown Login!")
                            .setDescription("Be careful with this message! Don't do a miss click!")
                            .addField("IP", login[0], false)
                            .addField("Plugin", login[1], false)
                            .addField("License", login[2], false)
                            .setFooter(formatter.format(date), "https://cdn.discordapp.com/attachments/696944554640670731/944464364956704798/LOGO-SHARK.png")
                            .build()
            );

            MessageAction knownLogin = textChannel.sendMessageEmbeds(
                    new EmbedBuilder()
                            .setColor(Color.CYAN)
                            .setImage("https://cdn.discordapp.com/attachments/696944554640670731/944464364956704798/LOGO-SHARK.png")
                            .setTitle("Known Login!")
                            .setDescription("Be careful with this message! Don't do a miss click!")
                            .addField("IP", login[0], false)
                            .addField("Plugin", login[1], false)
                            .addField("License", login[2], false)
                            .setFooter(formatter.format(date), "https://cdn.discordapp.com/attachments/696944554640670731/944464364956704798/LOGO-SHARK.png")
                            .build()
            );

            Bson filter = eq("_id", login[0]);
            FindIterable<Document> iterable = Terminal.getInstance().getMongoConnector().getIps().find(filter);

            if (iterable.first() == null) {
                Button button = Button.success("button:add_ip", "Add access");

                unkownLogin.setActionRow(button).queue(lol -> {
                    Terminal.getInstance()
                            .getEventWaiter()
                            .waitForEvent(
                                    ButtonClickEvent.class,
                                    event -> {
                                        if (
                                                Objects.requireNonNull(event.getMember()).getUser().isBot()
                                        ) return false;
                                        return event.getMessageId().equalsIgnoreCase(lol.getId());
                                    },
                                    event -> {
                                        event.getInteraction().deferEdit().complete();

                                        Document document = new Document("_id", login[0]);

                                        Terminal
                                                .getInstance()
                                                .getMongoConnector()
                                                .getIps()
                                                .insertOne(document);

                                        lol.reply("Access added successfully!").queue();
                                    });

                });
            } else {
                Button button = Button.danger("button:revoke_ip", "Revoke access");
                knownLogin.setActionRow(button).queue(lol -> {
                    Terminal.getInstance()
                            .getEventWaiter()
                            .waitForEvent(
                                    ButtonClickEvent.class,
                                    event -> {
                                        if (
                                                Objects.requireNonNull(event.getMember()).getUser().isBot()
                                        ) return false;
                                        return event.getMessageId().equalsIgnoreCase(lol.getId());
                                    },
                                    event -> {
                                        event.getInteraction().deferEdit().complete();

                                        Terminal
                                                .getInstance()
                                                .getMongoConnector()
                                                .getIps()
                                                .deleteOne(filter);

                                        lol.reply("Access revoked successfully!").queue();
                                    });
                });
            }
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }
}
