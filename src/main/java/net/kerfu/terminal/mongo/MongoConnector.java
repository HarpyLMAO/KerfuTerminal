package net.kerfu.terminal.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.Document;

@Getter
public class MongoConnector {

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    private final MongoCollection<Document> ips;

    public MongoConnector() {
        this.mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://harpylmao:1234asdf@privatedb.u5zb8.mongodb.net/?retryWrites=true&w=majority"));
        this.mongoDatabase = this.mongoClient.getDatabase("terminal");
        this.ips = this.mongoDatabase.getCollection("ips");
    }
}
