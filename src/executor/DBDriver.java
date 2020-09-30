package executor;

import com.mongodb.*;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.operation.UpdateOperation;
import org.bson.Document;
import org.bson.conversions.Bson;
import utils.SysUtils;

import java.util.ArrayList;

public class DBDriver {
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;

    public static boolean startConnection(String collectionName) {
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        boolean load = false;
        DBDriver.database = mongoClient.getDatabase("CORANA");
        boolean collectionExists = database.listCollectionNames()
                .into(new ArrayList<String>()).contains(collectionName);
        if (!collectionExists) {
            database.createCollection(collectionName);
            load = true;
        }
        DBDriver.collection = database.getCollection(collectionName);
        return load;
    }

    public static String getValue(String address) {
        Document result = DBDriver.collection.find(eq("address",address)).limit(1).first();
        return (result == null) ? "#x0" : result.getString("value");
    }

    public static void addMemoryDocument(String address, String value) {
        Document doc = new Document("address", address).append("value", value);
        collection.insertOne(doc);
    }

    public static void updateMemoryDocument(String address, String value) {
        Bson filter = eq("address", SysUtils.getAddressValue(address));
        Bson updateOperation = set("value", SysUtils.getAddressValue(value));
        UpdateOptions options = new UpdateOptions().upsert(true);
        collection.updateOne(filter, updateOperation, options);
    }

    public static void main(String[] args) {
        //startConnection("test");
        //addDocument("0x01", "0xff");
        String result = "x000008e";

        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        DBDriver.database = mongoClient.getDatabase("CORANA");
        String collectionName = "Test";
        boolean collectionExists = database.listCollectionNames()
                .into(new ArrayList<String>()).contains(collectionName);
        if (!collectionExists) {
            database.createCollection(collectionName);
        }
        DBDriver.collection = database.getCollection("test");
        //updateMemoryDocument("#xfefffff4", "r0");
        System.out.println(getValue("fefffff4"));
    }
}
