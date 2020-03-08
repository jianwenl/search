import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;



public class compareAlgorithms {

    // compare main function
    public static void main (String args[])  throws IOException, ParseException {


        compareAlgorithms compareAlgoVSM = new compareAlgorithms();
        compareAlgoVSM.VSM();
        System.out.println("Success for Vector Space Model");

        compareAlgorithms compareAlgoBM = new compareAlgorithms();
        compareAlgoBM.BM25();
        System.out.println("Success for Language Model with BM25");
//
//
        compareAlgorithms compareAlgoLMD = new compareAlgorithms();
        compareAlgoLMD.LMDirichlet();
        System.out.println("Success for Language Model with Dirichlet Smoothing");

        compareAlgorithms compareAlgoLMJ = new compareAlgorithms();
        compareAlgoLMJ.LMJelinekMercer();
        System.out.println("Success for Language Model with Jelinek Mercer Smoothing");


    }



    public void VSM(){
        try {

            String index = "index";
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            int totalDocLength = reader.maxDoc();
            String s = new String(Files.readAllBytes(Paths.get("topics.51-100")));


            Pattern patTopic = Pattern.compile("<top>(.+?)</top>", Pattern.DOTALL);
            Matcher matTopic = patTopic.matcher(s);

            PrintWriter printShortQuery = new PrintWriter(new BufferedWriter(new FileWriter("VSMShortQuery", true)));
            PrintWriter printLongQuery = new PrintWriter(new BufferedWriter(new FileWriter("VSMLongQuery", true)));

            int shortTopicID = 51;
            int longTopicID = 51;

            List<String> list1 = new ArrayList<String>();

            while(matTopic.find()) {

                //String s1 = matTopic.group(shortTopicID - 50).toString().replaceAll("Topic:", "").trim();

//                System.out.println(shortTopicID);

                //top_line = ??
                //top line responding to each content of <top>...</top>
                //then do your tregular expression from each top line


                list1.add(matTopic.group(1));


                //System.out.println("cHECK WHAT IS I"+i);
            }

            for (int j = 0; j < list1.size(); j++){
                String s1 = list1.get(j);
                Pattern patShortQuery = Pattern.compile("<title>(.+?)<desc>", Pattern.DOTALL);
                Matcher matShortQuery = patShortQuery.matcher(s1);

                Pattern patLongQuery = Pattern.compile("<desc>(.+?)<smry>", Pattern.DOTALL);
                Matcher matchLongQuery = patLongQuery.matcher(s1);


                while (matShortQuery.find()) {
                    String queryString = list1.get(j);
                    //assume in each time you only operate on one query element(short or long)
                    //then you try to operate on each this specific query
                    queryString = matShortQuery.toString().replaceAll("Topic:", "").trim();
                    IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
                    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                    Analyzer analyzer = new StandardAnalyzer();

                    //score
                    indexSearcher.setSimilarity(new ClassicSimilarity());
                    QueryParser queryParser = new QueryParser("TEXT", analyzer);

                    Query query = queryParser.parse(QueryParser.escape(queryString));

                    TopDocs topDocs = indexSearcher.search(query, 1000);
                    int noOfHits = topDocs.totalHits;

                    int count = 1;
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;

                    StringBuilder stringBuilder = new StringBuilder();
                    for (ScoreDoc scoreDoc : scoreDocs) {
                        //                    System.out.println("Document is = " + scoreDoc.doc + " score = " + scoreDoc.score);
                        Document document = indexSearcher.doc(scoreDoc.doc);
                        stringBuilder.append(shortTopicID).append(" Q0 ").append(document.get("DOCNO")).append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-VSMShort");
                        printShortQuery.println(stringBuilder.toString());
                        count += 1;
                        stringBuilder.setLength(0);
                        stringBuilder.trimToSize();
                    }

                }
                shortTopicID++;
                //add another loop here which is

                //loop over all topics to find your Top1000 topics(or docs whatever you like) for this specific query
                //and try to add this query q's Top1000 to your overall file
                //your overall file should be like
                //for each query you have 1000 corresponding topic scores
                //in this way, you should have 50 ge(pinyin) 1000


                while (matchLongQuery.find()) {

                    String queryString = matchLongQuery.group(1).toString().replaceAll("Description:", "").trim();
                    IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
                    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                    Analyzer analyzer = new StandardAnalyzer();
                    indexSearcher.setSimilarity(new BM25Similarity());
                    QueryParser queryParser = new QueryParser("TEXT", analyzer);

                    Query query = queryParser.parse(QueryParser.escape(queryString));

                    TopDocs topDocs = indexSearcher.search(query, 1000);

                    int count = 1;
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                    StringBuilder writerCarry = new StringBuilder();

                    for (ScoreDoc scoreDoc : scoreDocs) {
                        Document document = indexSearcher.doc(scoreDoc.doc);
                        writerCarry.append(longTopicID).append(" Q0 ").append(document.get("DOCNO")).append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-VSMLong");
                        printLongQuery.println(writerCarry.toString());
                        count += 1;
                        writerCarry.setLength(0);
                        writerCarry.trimToSize();
                    }


                }
                longTopicID++;

            }
            printShortQuery.close();
            printLongQuery.close();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void BM25(){
        try {

            String index = "index";
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            int totalDocLength = reader.maxDoc();
            String s = new String(Files.readAllBytes(Paths.get("topics.51-100")));


            Pattern patTopic = Pattern.compile("<top>(.+?)</top>", Pattern.DOTALL);
            Matcher matTopic = patTopic.matcher(s);

            PrintWriter printShortQuery = new PrintWriter(new BufferedWriter(new FileWriter("BM25ShortQuery", true)));
            PrintWriter printLongQuery = new PrintWriter(new BufferedWriter(new FileWriter("BM25LongQuery", true)));

            int shortTopicID = 51;
            int longTopicID = 51;

            List<String> list1 = new ArrayList<String>();

            while(matTopic.find()) {

                list1.add(matTopic.group(1));

            }

            for (int j = 0; j < list1.size(); j++){
                String s1 = list1.get(j);
                Pattern patShortQuery = Pattern.compile("<title>(.+?)<desc>", Pattern.DOTALL);
                Matcher matShortQuery = patShortQuery.matcher(s1);

                Pattern patLongQuery = Pattern.compile("<desc>(.+?)<smry>", Pattern.DOTALL);
                Matcher matchLongQuery = patLongQuery.matcher(s1);


                while (matShortQuery.find()) {
                    String queryString = list1.get(j);
                    queryString = matShortQuery.toString().replaceAll("Topic:", "").trim();
                    IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
                    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                    Analyzer analyzer = new StandardAnalyzer();

                    //score
                    indexSearcher.setSimilarity(new LMDirichletSimilarity());
                    QueryParser queryParser = new QueryParser("TEXT", analyzer);

                    Query query = queryParser.parse(QueryParser.escape(queryString));

                    TopDocs topDocs = indexSearcher.search(query, 1000);
                    int noOfHits = topDocs.totalHits;

                    int count = 1;
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;

                    StringBuilder stringBuilder = new StringBuilder();
                    for (ScoreDoc scoreDoc : scoreDocs) {
                        Document document = indexSearcher.doc(scoreDoc.doc);
                        stringBuilder.append(shortTopicID).append(" Q0 ").append(document.get("DOCNO")).append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-BM25Short");
                        printShortQuery.println(stringBuilder.toString());
                        count += 1;
                        stringBuilder.setLength(0);
                        stringBuilder.trimToSize();
                    }

                }
                shortTopicID++;



                while (matchLongQuery.find()) {

                    String queryString = matchLongQuery.group(1).toString().replaceAll("Description:", "").trim();
                    IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
                    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                    Analyzer analyzer = new StandardAnalyzer();
                    indexSearcher.setSimilarity(new BM25Similarity());
                    QueryParser queryParser = new QueryParser("TEXT", analyzer);

                    Query query = queryParser.parse(QueryParser.escape(queryString));

                    TopDocs topDocs = indexSearcher.search(query, 1000);

                    int count = 1;
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                    StringBuilder writerCarry = new StringBuilder();

                    for (ScoreDoc scoreDoc : scoreDocs) {
                        Document document = indexSearcher.doc(scoreDoc.doc);
                        writerCarry.append(longTopicID).append(" Q0 ").append(document.get("DOCNO")).append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-BM25Long");
                        printLongQuery.println(writerCarry.toString());
                        count += 1;
                        writerCarry.setLength(0);
                        writerCarry.trimToSize();
                    }


                }
                longTopicID++;

            }
            printShortQuery.close();
            printLongQuery.close();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }





    public void LMDirichlet(){
        try {

            String index = "index";
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            int totalDocLength = reader.maxDoc();
            String s = new String(Files.readAllBytes(Paths.get("topics.51-100")));


            Pattern patTopic = Pattern.compile("<top>(.+?)</top>", Pattern.DOTALL);
            Matcher matTopic = patTopic.matcher(s);

            PrintWriter printShortQuery = new PrintWriter(new BufferedWriter(new FileWriter("LMDirichletShortQuery", true)));
            PrintWriter printLongQuery = new PrintWriter(new BufferedWriter(new FileWriter("LMDirichletLongQuery", true)));

            int shortTopicID = 51;
            int longTopicID = 51;

            List<String> list1 = new ArrayList<String>();

            while(matTopic.find()) {
                list1.add(matTopic.group(1));
            }

            for (int j = 0; j < list1.size(); j++){
                String s1 = list1.get(j);
                Pattern patShortQuery = Pattern.compile("<title>(.+?)<desc>", Pattern.DOTALL);
                Matcher matShortQuery = patShortQuery.matcher(s1);

                Pattern patLongQuery = Pattern.compile("<desc>(.+?)<smry>", Pattern.DOTALL);
                Matcher matchLongQuery = patLongQuery.matcher(s1);


                while (matShortQuery.find()) {
                    String queryString = list1.get(j);
                    queryString = matShortQuery.toString().replaceAll("Topic:", "").trim();
                    IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
                    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                    Analyzer analyzer = new StandardAnalyzer();

                    //score
                    indexSearcher.setSimilarity(new LMDirichletSimilarity());
                    QueryParser queryParser = new QueryParser("TEXT", analyzer);

                    Query query = queryParser.parse(QueryParser.escape(queryString));

                    TopDocs topDocs = indexSearcher.search(query, 1000);
                    int noOfHits = topDocs.totalHits;

                    int count = 1;
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;

                    StringBuilder stringBuilder = new StringBuilder();
                    for (ScoreDoc scoreDoc : scoreDocs) {
                        Document document = indexSearcher.doc(scoreDoc.doc);
                        stringBuilder.append(shortTopicID).append(" Q0 ").append(document.get("DOCNO")).
                                append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-LMDirichletShort");
                        printShortQuery.println(stringBuilder.toString());
                        count += 1;
                        stringBuilder.setLength(0);
                        stringBuilder.trimToSize();
                    }

                }
                shortTopicID++;

                while (matchLongQuery.find()) {

                    String queryString = matchLongQuery.group(1).toString().replaceAll("Description:", "").trim();
                    IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
                    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                    Analyzer analyzer = new StandardAnalyzer();
                    indexSearcher.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7));
                    QueryParser queryParser = new QueryParser("TEXT", analyzer);

                    Query query = queryParser.parse(QueryParser.escape(queryString));

                    TopDocs topDocs = indexSearcher.search(query, 1000);
                    int count = 1;
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                    StringBuilder writerCarry = new StringBuilder();

                    for (ScoreDoc scoreDoc : scoreDocs) {
                        Document document = indexSearcher.doc(scoreDoc.doc);
                        writerCarry.append(longTopicID).append(" Q0 ").append(document.get("DOCNO")).append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-LMDirichletLong");
                        printLongQuery.println(writerCarry.toString());
                        count += 1;
                        writerCarry.setLength(0);
                        writerCarry.trimToSize();
                    }


                }
                longTopicID++;

            }
            printShortQuery.close();
            printLongQuery.close();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }




    public void LMJelinekMercer(){
        try {

            String index = "index";
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            int totalDocLength = reader.maxDoc();
            String s = new String(Files.readAllBytes(Paths.get("topics.51-100")));


            Pattern patTopic = Pattern.compile("<top>(.+?)</top>", Pattern.DOTALL);
            Matcher matTopic = patTopic.matcher(s);

            PrintWriter printShortQuery = new PrintWriter(new BufferedWriter(new FileWriter("LMJelinekMercerShortQuery", true)));
            PrintWriter printLongQuery = new PrintWriter(new BufferedWriter(new FileWriter("LMJelinekMercerLongQuery", true)));

            int shortTopicID = 51;
            int longTopicID = 51;

            List<String> list1 = new ArrayList<String>();

            while(matTopic.find()) {

                //String s1 = matTopic.group(shortTopicID - 50).toString().replaceAll("Topic:", "").trim();

//                System.out.println(shortTopicID);

                //top_line = ??
                //top line responding to each content of <top>...</top>
                //then do your tregular expression from each top line


                list1.add(matTopic.group(1));


                //System.out.println("cHECK WHAT IS I"+i);
            }

            for (int j = 0; j < list1.size(); j++){
                    String s1 = list1.get(j);
                    Pattern patShortQuery = Pattern.compile("<title>(.+?)<desc>", Pattern.DOTALL);
                    Matcher matShortQuery = patShortQuery.matcher(s1);

                    Pattern patLongQuery = Pattern.compile("<desc>(.+?)<smry>", Pattern.DOTALL);
                    Matcher matchLongQuery = patLongQuery.matcher(s1);


                    while (matShortQuery.find()) {
                        String queryString = list1.get(j);
                        //assume in each time you only operate on one query element(short or long)
                        //then you try to operate on each this specific query
                        queryString = matShortQuery.toString().replaceAll("Topic:", "").trim();
                        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
                        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                        Analyzer analyzer = new StandardAnalyzer();

                        //score
                        indexSearcher.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7));
                        QueryParser queryParser = new QueryParser("TEXT", analyzer);

                        Query query = queryParser.parse(QueryParser.escape(queryString));

                        TopDocs topDocs = indexSearcher.search(query, 1000);
                        int noOfHits = topDocs.totalHits;

                        int count = 1;
                        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

                        StringBuilder stringBuilder = new StringBuilder();
                        for (ScoreDoc scoreDoc : scoreDocs) {
                            //                    System.out.println("Document is = " + scoreDoc.doc + " score = " + scoreDoc.score);
                            Document document = indexSearcher.doc(scoreDoc.doc);
                            stringBuilder.append(shortTopicID).append(" Q0 ").append(document.get("DOCNO")).
                                    append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-LMJelinekMercerShort");
                            printShortQuery.println(stringBuilder.toString());
                            count += 1;
                            stringBuilder.setLength(0);
                            stringBuilder.trimToSize();
                        }

                    }
                shortTopicID++;
                //add another loop here which is

                //loop over all topics to find your Top1000 topics(or docs whatever you like) for this specific query
                //and try to add this query q's Top1000 to your overall file
                //your overall file should be like
                //for each query you have 1000 corresponding topic scores
                //in this way, you should have 50 ge(pinyin) 1000


                while (matchLongQuery.find()) {

                    String queryString = matchLongQuery.group(1).toString().replaceAll("Description:", "").trim();
                    IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
                    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                    Analyzer analyzer = new StandardAnalyzer();
                    indexSearcher.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7));
                    QueryParser queryParser = new QueryParser("TEXT", analyzer);

                    Query query = queryParser.parse(QueryParser.escape(queryString));

                    TopDocs topDocs = indexSearcher.search(query, 1000);

//                    int noOfHits = topDocs.totalHits;
//                    System.out.println("Total number of matching documents: " + noOfHits);

                    int count = 1;
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                    StringBuilder writerCarry = new StringBuilder();

                    for (ScoreDoc scoreDoc : scoreDocs) {
//                    System.out.println("Document is = " + scoreDoc.doc + " score = " + scoreDoc.score);

                        Document document = indexSearcher.doc(scoreDoc.doc);
                        writerCarry.append(longTopicID).append(" Q0 ").append(document.get("DOCNO")).append(" ").append(count).append(" ").append(scoreDoc.score).append(" run-1-LMJelinekMercerLong");
                        printLongQuery.println(writerCarry.toString());
                        count += 1;
                        writerCarry.setLength(0);
                        writerCarry.trimToSize();
                    }


                }
                longTopicID++;

            }
                printShortQuery.close();
                printLongQuery.close();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }


}