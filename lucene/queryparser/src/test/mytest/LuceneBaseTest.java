package mytest;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.function.BiConsumer;

public abstract class LuceneBaseTest {

    protected int size() {
        return 50000;
    }

    protected abstract BiConsumer<Document, Long> getIndexBuilder();

    protected void write(String fileName) throws IOException {
        IndexWriter writer = LuceneBuilder.indexWriterBuilder()
                .topic(fileName)
                .cleanBeforeWrite(true)
                .build();

        BiConsumer<Document, Long> consumer = getIndexBuilder();
        for (long i = 0; i < size(); ++i) {
            Document document = new Document();
            consumer.accept(document, i);
            writer.addDocument(document);
        }
        writer.commit();
        writer.forceMerge(1);
        writer.close();
    }

    protected Query must() throws ParseException {
        QueryParser parser = new QueryParser("name", new StandardAnalyzer());
        Query query = parser.parse("city:hangzhou AND sex:male");
        return query;
    }

    protected Query should() throws ParseException {
        QueryParser parser = new QueryParser("name", new StandardAnalyzer());
        Query query = parser.parse("sex:male");
        return query;
    }

    protected Query matchAll() throws ParseException {
        return new MatchAllDocsQuery();
    }

    protected Query termQuery() throws ParseException {
        return new TermQuery(new Term("name", "jack"));
    }

    protected abstract Query getQuery() throws ParseException;

    protected void read(String fileName) {
        try {
            IndexSearcher searcher = LuceneBuilder.indexSearcherBuilder().topic(fileName).build();

            SortField sortField = getSortField();
            Sort sort = new Sort(sortField);
            TopDocs docs = searcher.search(getQuery(), 20, sort);
            System.out.println("totalHits: " + docs.totalHits);
            ScoreDoc[] scores = docs.scoreDocs;

            TextTable table = new TextTable("id,", "name", "age");
            //遍历结果
            for (ScoreDoc scoreDoc : scores) {
                Document document = searcher.doc(scoreDoc.doc);
                table.addRow(
                        String.valueOf(scoreDoc.doc),
                        document.get("name"),
                        document.get("age")
                );
            }
            System.out.println(table.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SortField getSortField() {
        return new SortField("age", SortField.Type.LONG, true);
    }

}