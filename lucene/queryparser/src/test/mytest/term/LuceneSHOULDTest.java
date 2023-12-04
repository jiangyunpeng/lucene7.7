package mytest.term;

import mytest.LuceneBaseTest;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.junit.Test;

import java.io.IOException;
import java.util.function.BiConsumer;

public class LuceneSHOULDTest extends LuceneBaseTest {
    @Override
    protected BiConsumer<Document, Long> getIndexBuilder() {
        return (document, i) -> {
            document.add(new StringField("city", "hangzhou", Store.YES));
            document.add(new StringField("city", "beijing", Store.YES));
            if (i % 3 == 0) {
                document.add(new StringField("sex", "male", Store.YES));
                //document.add(new TextField("message", "hello, i am from USA", Store.YES));
            } else {
                document.add(new StringField("sex", "female " + i, Store.YES));
                //document.add(new TextField("message", "nihao , i am from China", Store.YES));
            }
        };
    }

    @Override
    protected Query getQuery() throws ParseException {
        return should();
    }

    @Test
    public void testWrite() throws IOException {
        super.write("LongPoint");
    }

    @Test
    public void testRead() throws IOException {
        //super.write("LongPoint");
        super.read("LongPoint");
    }
}
