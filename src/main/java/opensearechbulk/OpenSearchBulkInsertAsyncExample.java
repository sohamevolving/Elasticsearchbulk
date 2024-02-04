package opensearechbulk;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

public class OpenSearchBulkInsertAsyncExample {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Set up the OpenSearch client using the Elasticsearch High-Level REST client
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")
                        ));

        // Set up the BulkProcessor
        BulkProcessor bulkProcessor = BulkProcessor.builder(
                (request, bulkListener) ->
                        client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId, BulkRequest request) {
                        // Executed before the bulk is executed
                        System.out.println("Executing bulk with " + request.numberOfActions() + " actions");
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                        // Executed after the bulk is executed
                        System.out.println("Bulk executed successfully");
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                        // Executed if the bulk fails
                        System.err.println("Bulk execution failed: " + failure.getMessage());
                    }
                })
                .setBulkActions(1000)
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .build();

        // Prepare your documents as JSON strings
        String document1 = "{\"field1\": \"value1\"}";
        String document2 = "{\"field1\": \"value2\"}";

        // Add documents to the BulkProcessor
        bulkProcessor.add(new IndexRequest("index1")
                .id("1")
                .source(document1, XContentType.JSON));
        bulkProcessor.add(new IndexRequest("index900")
                .id("2")
                .source(document2, XContentType.JSON));

        // Optionally, wait for the bulk processing to complete
        bulkProcessor.awaitClose(10, TimeUnit.MINUTES);

        // Close the client when done
        client.close();
    }
}
