package mytest.term;

import mytest.LuceneBaseTest;
import mytest.LuceneBuilder;
import mytest.TextTable;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;
import java.util.function.BiConsumer;

public class LuceneLogTest extends LuceneBaseTest {
    public static String generateRandomString() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            // 生成随机的小写字母 ASCII 码值，然后转换为字符添加到字符串中
            sb.append((char) (random.nextInt(26) + 'a'));
        }
        return sb.toString();
    }


    protected int size() {
        return 50000;
    }

    protected BiConsumer<Document, Long> getIndexBuilder() {
        return (document, i) -> {
            document.add(new StringField("input_type", "1", Store.YES));
            document.add(new TextField("offset", "GET"+i, Store.YES));
            document.add(new StringField("source", "http", Store.YES));
            document.add(new StringField("http_code", "200", Store.YES));
            document.add(new StringField("http_version", "1.1", Store.YES));
            String randomCode = generateRandomString();
            String message=  "user.ucenter.k"+i+".wacai.info:80 | "+randomCode+" | GET"+i+" /authorization/list?sourceBid=WC&otherBids= HTTP/1.1";
            //System.out.println(message);
            document.add(new TextField("message", message, Store.YES));
            document.add(new TextField("code", randomCode, Store.YES));

        };
    }

    @Override
    protected Query getQuery() throws ParseException {
        QueryParser parser = new QueryParser("name", new StandardAnalyzer());
//        Query query = parser.parse("message:GET490840");
//        Query query = parser.parse("code:qibeotkoek");
//        Query query = parser.parse("offset:GET490840");
        Query query = parser.parse("http_code:200");
        return query;
    }

    @Test
    public void testWrite() throws IOException {
        super.write("nginx-ingress");
    }

    @Test
    public void testRead() throws Exception {
        IndexSearcher searcher = LuceneBuilder.indexSearcherBuilder().topic("nginx-ingress").build();

        SortField sortField = getSortField();
        Sort sort = new Sort(sortField);
        TopDocs docs = searcher.search(getQuery(), 20, sort);
        System.out.println("totalHits: " + docs.totalHits);
        ScoreDoc[] scores = docs.scoreDocs;

        TextTable table = new TextTable("doc_id,", "http_code", "message");
        //遍历结果
        for (ScoreDoc scoreDoc : scores) {
            Document document = searcher.doc(scoreDoc.doc);
            table.addRow(
                    String.valueOf(scoreDoc.doc),
                    document.get("http_code"),
                    document.get("message")
            );
        }
        System.out.println(table.toString());
    }
}
