package net.kerfu.terminal;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.kerfu.terminal.mongo.MongoConnector;
import net.kerfu.terminal.rabbit.RabbitService;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Getter
public class Terminal {

    @Getter private static Terminal instance;

    private final EventWaiter eventWaiter;
    private final MongoConnector mongoConnector;
    private final JDA jda;
    private final RabbitService rabbitService;

    public Terminal() throws LoginException, InterruptedException, IOException, TimeoutException {
        instance = this;

        JDABuilder jdaBuilder = JDABuilder
                .createDefault("OTc2NDgyMzMzNjA1NjMwMDAz.GhZ8fH.4lObICo_WGxdt4W42zfl2o4hsCydw3AuNTBoAI")
                .setActivity(Activity.playing("Watching all plugins!"));

        this.eventWaiter = new EventWaiter();

        this.mongoConnector = new MongoConnector();

        jdaBuilder.addEventListeners(
                eventWaiter
        );

        this.jda = jdaBuilder.build().awaitReady();

        this.rabbitService = new RabbitService();
        this.rabbitService.start();

    }

    public static void main(String[] args) throws IOException, TimeoutException, LoginException, InterruptedException {
        new Terminal();
    }
}
