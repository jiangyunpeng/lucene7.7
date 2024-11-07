package mytest;


import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * 封装了一些助手方法
 *
 * @author bairen
 * @description
 **/
public class LuceneBuilder {


    /**
     * 返回 IndexWriterBuilder
     *
     */
    public static IndexWriterBuilder indexWriterBuilder() {
        return new IndexWriterBuilder();
    }

    /**
     * 返回 IndexSearcherBuilder
     *
     */
    public static IndexSearcherBuilder indexSearcherBuilder() {
        return new IndexSearcherBuilder();
    }


    public static class IndexWriterBuilder {
        private String topic = "default";
        private StandardAnalyzer analyzer = new StandardAnalyzer();
        private boolean simpleTextCodec;
        private boolean cleanBeforeWrite = false;

        public IndexWriterBuilder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public IndexWriterBuilder simpleTextCodec(boolean simpleTextCodec) {
            this.simpleTextCodec = simpleTextCodec;
            return this;
        }

        public IndexWriterBuilder cleanBeforeWrite(boolean cleanBeforeWrite) {
            this.cleanBeforeWrite = cleanBeforeWrite;
            return this;
        }

        public IndexWriter build() throws IOException {
            Path path = FileSystems.getDefault().getPath("/data/tmp/lucene-data", topic);
            if (cleanBeforeWrite) {
                File file = path.toFile();
                if (file.exists()) {
                    Stream.of(file.listFiles()).forEach(File::delete);
                    System.out.println("clean file " + file.getPath() + " " + file.delete());
                }
            }
            Directory memoryIndex = new MMapDirectory(path);
            IndexWriterConfig config = new IndexWriterConfig(analyzer);

            config.setUseCompoundFile(false);//是否CompoundFile
            if (simpleTextCodec) {
                config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
                //config.setCodec(new SimpleTextCodec());
            }
            return new IndexWriter(memoryIndex, config);
        }
    }

    public static class IndexSearcherBuilder {
        private IndexWriter writer;
        private String topic = "default";

        public IndexSearcherBuilder writer(IndexWriter writer) {
            this.writer = writer;
            return this;
        }

        public IndexSearcherBuilder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public IndexSearcher build() throws IOException {
            Path path = Paths.get("/data/tmp/lucene-data", topic);
            Directory directory = new MMapDirectory(path);
            IndexReader indexReader;
            if (writer != null) {
                indexReader = DirectoryReader.open(writer);
            } else {
                indexReader = DirectoryReader.open(directory);
            }
            return new IndexSearcher(indexReader);
        }
    }
}