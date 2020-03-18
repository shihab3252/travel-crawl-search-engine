package com.mishkat;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LuceneIndexing {
    public static String[] snipps = {"My new wife and I chose this beautiful hotel for our first two nights married after our Feb. 29 wedding at nearby Swedenborgian Church. Everything about out stay in a city view suite at the Drisco was perfect for us: luxurious comfort, spacious sparkling clean suite, gorgeous views, quiet location, and friendly/helpful staff.",
            "I recently visited Hotel Drisco for one night and it won’t be my last. I stayed in a lovely city view suite. The accommodations and service are a step above the rest. The hotel is located in the peaceful Pacific Heights neighborhood with wonderful views of the city and marina. Restaurants on Fillmore street are a 10 minute walk away. I will definitely return on a future San Francisco trip.",
            "Just spent the most amazing 2 nights in the hotel's city view suite. The hotel is located by the Presido, so it really felt above the fray of the city below. The morning and evening views were stunning. Everyone from the front desk to the bellman were super friendly and you can tell that their customer service training was spot on.",
            "This hotel was so beautiful. We had one of the ground rooms and it was spacious and gorgeously decorated. All of the extra details were so appreciated -- turn down service, pillows, wine hour, breakfast, sleep machine. Would definitely stay here again.",
            "Such a beautiful hotel, we chose to stay here as it was in the nicer part of SF and we were visiting from London for three nights. The surrounding areas have some beautiful houses and feel very safe. The service in the hotel was fabulous from start to finish and nothing was too difficult for the staff.",
            "We just returned from our new favorite San Francisco hotel. Living in Southern California, we have enjoyed plenty of weekends here. However, we have only stayed in the downtown areas of the city and we were so happy to find this exceptional hotel. Located in the beautiful tree-lined streets of Pacific Heights, it felt like a completely different city.",
            "Our reservations were made easily by phone and follow up email communication was great. Upon arrival, we received a gracious, friendly smile and a warm washcloth. Even though we arrived an hour earlier than expected, our room was ready and our bags carried up for us.",
            "This room had absolutely everything you could wish for in a hotel. We were touched by the champagne, the olives and nuts and the kind note in our room (for a special birthday celebration!) upon our arrival. There were lots of hanging spaces and soft-close drawers--in the nightstands, the dresser, the armoire and the bathroom.",
            "The morning buffet included is more than a buffet: hard boiled eggs, oatmeal, yogurts, cereals, juices, breads, jams, coffee drinks, smoked salmon, protein drinks, parfaits, pastries, avocados toast and more! The dining staff was so nice and quick to clear a table or bring more cappuccino!",
            "Upon our departure, the staff gladly stored our luggage until we needed to get to the airport. We shared with the hotel and now share with all the TripAdvisor readers; we will never stay anywhere else in San Francisco!! Hotel Drisco is our new go-to!"};
//    private static int createIndex(String fileDirectoryPath, String indexDirectoryPath, int indexedPostCount) throws IOException, ParseException {
//        Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath).toPath());
//        Analyzer analyzer = new StandardAnalyzer();
//
//        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
//        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
//        IndexWriter indexWriter = new IndexWriter(indexDirectory, writerConfig);
//
//        File[] files = new File(fileDirectoryPath).listFiles();
//        int actualIndexedPosts = 0;
//        if(files!=null) {
//            for (File file : files) {
//                int indexed = indexPostsAsDocuments(indexedPostCount, indexWriter, file);
//                actualIndexedPosts += indexed;
//                if(indexed == indexedPostCount)
//                    break;
//                indexedPostCount -= indexed;
//            }
//            indexWriter.close();
//        }
//        return actualIndexedPosts;
//    }
//
//    private static int indexPostsAsDocuments(int indexedPostCount, IndexWriter writer, File file) throws IOException, ParseException {
//        int indexedPosts = 0;
//
//        //Parsing the input JSON files and adding posts as documents
//        JSONParser jsonParser = new JSONParser();
//        Object object = jsonParser.parse(new FileReader(file));
//        JSONArray allPosts = (JSONArray) object;
//        indexedPosts = (indexedPostCount < allPosts.size())? indexedPostCount:allPosts.size();
//        Iterator itr = allPosts.iterator();
//        for(int i=0; i<indexedPosts; i++){
//            JSONObject postObject = (JSONObject) itr.next();
//            Document document = new Document();
//
//            String title = (String) postObject.get("title");
//            String body = (String) postObject.get("body");
//            String id = (String) postObject.get("id");
//            String image = (String) postObject.get("image");
//            long numComments = (long) postObject.get("num_comments");
//            String link = (String) postObject.get("link");
//            long up_votes = (long) postObject.get("upvotes");
//
//            StringBuilder comments = new StringBuilder();
//            JSONArray comment_array = (JSONArray) postObject.get("comments");
//            Iterator it2 = comment_array.iterator();
//            while(it2.hasNext()){
//                comments.append(it2.next());
//            }
//
//            document.add(new TextField("title", title, Field.Store.YES));
//            document.add(new TextField("body", body, Field.Store.YES));
//            document.add(new StringField("id", id, Field.Store.YES));
//            document.add(new StringField("image", image, Field.Store.NO));
//            document.add(new StoredField("num_comments", numComments));
//            document.add(new StringField("link", link, Field.Store.YES));
//            document.add(new StoredField("upvotes", up_votes));
//            document.add(new TextField("comments", comments.toString(), Field.Store.NO));
//            writer.addDocument(document);
//        }
//        return indexedPosts;
//
//    }

    private static List<String> searchIndex(String indexDirectoryPath, String searchQuery) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
        Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath).toPath());
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        StandardAnalyzer analyzer = new StandardAnalyzer();

        String[] fields = {"title", "comments"};
        QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
        Query query = queryParser.parse(searchQuery);
//        long startTime = System.currentTimeMillis();
        int maxToRetrieve = 200;
        int topHitCount = 10;
        TopDocs queryResults = indexSearcher.search(query, maxToRetrieve);
//        long endTime = System.currentTimeMillis();
//        System.out.println("Results found: "+ queryResults.totalHits + ", Search time: "+(endTime-startTime) +" ms");
        ScoreDoc[] hits = queryResults.scoreDocs;
        List<String> tops = new ArrayList<>();

        // Iterate through the results:
        int count = 0;
        for (int rank = 0; rank < hits.length; ++rank) {
            Document document = indexSearcher.doc(hits[rank].doc);
            if(tops.size() < topHitCount){
                String snippetbody = snipps[count++];
//                String body = document.get("body");
//                if (body.length() > 3) {
//                    snippetbody = body;
//                }
                tops.add((rank + 1) + " (score:" + hits[rank].score + ")--> " + document.get("title")+" -- "+document.get("link") + " |" + snippetbody + "");
            }
            else {
                break;
            }
        }

        indexReader.close();

        return tops;
    }

    public static void main(String[] args) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
        String pwd = args[0];//record present directory, where all files are
        String queryString = args[1]; //record user-supplied query
        String indexDirectoryPath = pwd + "/index";
        List<String> tops = searchIndex(indexDirectoryPath, queryString);

        for(String top : tops) {
            System.out.println(top);
        }
    }
}
