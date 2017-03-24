package mavenBigQuery.DSE;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;

import java.util.Iterator;
import java.util.List;

public class SimpleApp {
  public static void main(String... args) throws Exception {
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
    QueryRequest queryRequest =
        QueryRequest
            .newBuilder(
                "SELECT "
                    + "APPROX_TOP_COUNT(corpus, 10) as title, "
                    + "COUNT(*) as unique_words "
                    + "FROM `publicdata.samples.shakespeare`;")
            // Use standard SQL syntax for queries.
            // See: https://cloud.google.com/bigquery/sql-reference/
            .setUseLegacySql(false)
            .build();
    QueryResponse response = bigquery.query(queryRequest);

    QueryResult result = response.getResult();

    while (result != null) {
      Iterator<List<FieldValue>> iter = result.iterateAll();

      while (iter.hasNext()) {
        List<FieldValue> row = iter.next();
        List<FieldValue> titles = row.get(0).getRepeatedValue();
        System.out.println("titles:");

        for (FieldValue titleValue : titles) {
          List<FieldValue> titleRecord = titleValue.getRecordValue();
          String title = titleRecord.get(0).getStringValue();
          long uniqueWords = titleRecord.get(1).getLongValue();
          System.out.printf("\t%s: %d\n", title, uniqueWords);
        }

        long uniqueWords = row.get(1).getLongValue();
        System.out.printf("total unique words: %d\n", uniqueWords);
      }

      result = result.getNextPage();
    }
  }
}