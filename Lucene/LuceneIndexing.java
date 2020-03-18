import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class LuceneIndexing {
    public static int TOTAL_POST = 6; //500000;
    private static String indexDirectoryPath = "D:\\Lucene\\Index_Directory";
    private static String fileDirectoryPath = "D:\\Lucene\\Crawler_Output";

    private static void createIndex(int indexedPostCount) throws IOException, ParseException {
        Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath).toPath());
        Analyzer analyzer = new StandardAnalyzer();

        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter indexWriter = new IndexWriter(indexDirectory, writerConfig);

        File[] files = new File(fileDirectoryPath).listFiles();
        if(files!=null) {
            for (File file : files) {
                int indexed = indexPostsAsDocuments(indexedPostCount, indexWriter, file);
                if(indexed == indexedPostCount)
                    break;
                indexedPostCount -= indexed;
            }
            indexWriter.close();
        }
    }

    private static int indexPostsAsDocuments(int indexedPostCount, IndexWriter writer, File file) throws IOException, ParseException {
        int indexedPosts = 0;
        Document document = new Document();

        //Parsing the input JSON files and adding posts as documents
        JSONParser jsonParser = new JSONParser();
        Object object = jsonParser.parse(new FileReader(file));
        JSONArray allPosts = (JSONArray) object;
        indexedPosts = (indexedPostCount < allPosts.size())? indexedPostCount:allPosts.size();
        Iterator itr = allPosts.iterator();
        for(int i=0; i<indexedPosts; i++){
            JSONObject postObject = (JSONObject) itr.next();

            String title = (String) postObject.get("title");
            String body = (String) postObject.get("body");
            String id = (String) postObject.get("id");
            String image = (String) postObject.get("image");
            long numComments = (long) postObject.get("num_comments");
            String link = (String) postObject.get("link");
            long up_votes = (long) postObject.get("upvotes");

            StringBuilder comments = new StringBuilder();
            JSONArray comment_array = (JSONArray) postObject.get("comments");
            Iterator it2 = comment_array.iterator();
            while(it2.hasNext()){
                comments.append(it2.next());
            }

            document.add(new TextField("title", title, Field.Store.YES));
            document.add(new TextField("body", body, Field.Store.YES));
            document.add(new StringField("id", id, Field.Store.YES));
            document.add(new StringField("image", image, Field.Store.NO));
            document.add(new StoredField("num_comments", numComments));
            document.add(new StringField("link", link, Field.Store.YES));
            document.add(new StoredField("upvotes", up_votes));
            document.add(new TextField("comments", comments.toString(), Field.Store.NO));
            writer.addDocument(document);
        }
        return indexedPosts;
    }

    private static void searchIndex(String searchQuery) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
        Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath).toPath());
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Analyzer analyzer = new StandardAnalyzer();

        String[] fields = {"title"};
        QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
        Query query = queryParser.parse(searchQuery);
        long startTime = System.currentTimeMillis();
        TopDocs queryResults = indexSearcher.search(query, 10);
        long endTime = System.currentTimeMillis();
        System.out.println("Results found: "+ queryResults.totalHits + ", Search time: "+(endTime-startTime) +" ms");

        ScoreDoc[] resultHits = queryResults.scoreDocs;
        for (ScoreDoc hit : resultHits) {
            Document document = indexSearcher.doc(hit.doc);
            String title = document.get("title");
            String id = document.get("id");
            //String image = document.get("image");
            String link = document.get("link");
            String up_votes = document.get("upvotes");
            long numComments = Long.parseLong(document.get("num_comments"));
            System.out.println("id: " +id+ "\ntitle: " + title + "\nup_votes: " + up_votes+ "\nnum of comments: "+numComments+ "\nlink: "+link);
        }
        indexReader.close();
    }

    public static void main(String[] args) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
        int postsCount = 5;//10000;
        while (postsCount<TOTAL_POST) {
            long startTime = System.currentTimeMillis();
            createIndex(postsCount);
            long endTime = System.currentTimeMillis();
            long execTime = endTime - startTime;
            System.out.println("Number of posts indexed: " +postsCount+ " Execution Time: " + execTime + "ms");
            postsCount += 1;//10000;
        }
        searchIndex("place Patagonia");
    }
}
