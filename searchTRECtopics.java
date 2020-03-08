import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;


public class searchTRECtopics extends easySearch {


    private static HashMap sortonMap(HashMap mapinput) {

        List list = new LinkedList(mapinput.entrySet());

        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        HashMap sortedMap = new LinkedHashMap();
        for (Iterator sca = list.iterator(); sca.hasNext();) {
            Map.Entry entry = (Map.Entry) sca.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public static void main(String[] args) throws ParseException, IOException {

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index")));
        IndexSearcher searcher = new IndexSearcher(reader);
        int totalDocLength = reader.maxDoc();
        String s = new String(Files.readAllBytes(Paths.get("topics.51-100")));

        Pattern patTopic = Pattern.compile("<top>(.+?)</top>", Pattern.DOTALL);
        Matcher matTopic = patTopic.matcher(s);


//        Pattern patTitle = Pattern.compile("<title>(.+?)<desc>", Pattern.DOTALL);
//        Matcher matchTitle = patTitle.matcher(s);
        PrintWriter writeCarryTitle = new PrintWriter(new BufferedWriter(new FileWriter("ShortQuery", true)));
        PrintWriter writeCarryDescription = new PrintWriter(new BufferedWriter(new FileWriter("LongQuery", true)));


        int shortQueryTopic = 51;
        int longQueryTopic = 51;

        List<String> list1 = new ArrayList<String>();


        while (matTopic.find()) {

            //String s1 = matTopic.group(shortTopicID - 50).toString().replaceAll("Topic:", "").trim();

//                System.out.println(shortTopicID);

            //top_line = ??
            //top line responding to each content of <top>...</top>
            //then do your tregular expression from each top line


            list1.add(matTopic.group(1));


            //System.out.println("cHECK WHAT IS I"+i);
        }



        for (int j = 0; j < list1.size(); j++) {

            String s1 = list1.get(j);



            Pattern patShortQuery = Pattern.compile("<title>(.+?)<desc>", Pattern.DOTALL);
            Matcher matchShortQuery = patShortQuery.matcher(s1);

            Pattern patLongQuery = Pattern.compile("<desc>(.+?)<smry>", Pattern.DOTALL);
            Matcher matchLongQuery = patLongQuery.matcher(s1);

//            while (matShortQuery.find()) {
//
//                try {
//
//                    String queryString = list1.get(j);
//
//                    //assume in each time you only operate on one query element(short or long)
//                    //then you try to operate on each this specific query
//                    queryString = matShortQuery.toString().replaceAll("Topic:", "").trim();
//
////                    String queryString = matShortQuery.group(1).replaceAll("Topic:", "").trim();
//                    PriorityQueue<Double> queueTitle = new PriorityQueue<Double>(10, Collections.reverseOrder());
//
//                    HashMap storedMap = new HashMap();
//                    StringBuilder writeCarryBuilder = new StringBuilder();
//
//                    Analyzer analyzer = new StandardAnalyzer();
//                    QueryParser parser = new QueryParser("TEXT", analyzer);
//                    Query query;
//                    query = parser.parse(queryString);
//                    Set<Term> queryTerms = new LinkedHashSet<Term>();
//                    searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
//
//                    ClassicSimilarity dSimi = new ClassicSimilarity();
//                    List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
//
//                    for (int i = 0; i < leafContexts.size(); i++) {
//                        LeafReaderContext leafContext = leafContexts.get(i);
//                        int startDocNo = leafContext.docBase;
//                        int numberOfDoc = leafContext.reader().maxDoc();
//
//                        for (int docId = 0; docId < numberOfDoc; docId++) {
//
//                            int occuranceTitle = 0;
//                            double queryScore = 0;
//
//                            float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));
//                            float realdocLength = 1 / (normDocLeng * normDocLeng);
//
//                            for (Term t : queryTerms) {
//
//                                int df = reader.docFreq(new Term("TEXT", t.text()));
//                                PostingsEnum p = MultiFields.getTermDocsEnum(leafContext.reader(), "TEXT", new BytesRef(t.text()));
//
//                                int doc;
//                                if (p != null) {
//                                    while ((doc = p.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
//                                        if (p.docID() + startDocNo == docId + startDocNo) {
//                                            occuranceTitle = p.freq();
//                                            break;
//                                        }
//                                    }
//                                }
//                                // tfidf
//                                double termScore = (occuranceTitle / realdocLength) * Math.log(1 + (totalDocLength / df));
//                                queryScore += termScore;
//                            }
//
//                            queueTitle.offer(queryScore);
//                            if (!storedMap.containsKey(docId + startDocNo)) {
//                                storedMap.put(searcher.doc(docId + startDocNo).get("DOCNO"), queryScore);
//                            }
//                        }
//                    }
//
//                    HashMap sortedMap = sortonMap(storedMap);
//                    Set mapSet = sortedMap.entrySet();
//                    Iterator scanner = mapSet.iterator();
//
//
//                    int scanCount = 0;
//                    while (scanner.hasNext() && scanCount < 1000) {
//
//                        Map.Entry mapAccess = (Map.Entry) scanner.next();
//                        System.out.println(shortQueryTopic + "\t" + "Q0" + "\t" + mapAccess.getKey() + "\t" + (scanCount + 1) + "\t" + mapAccess.getValue() + "\t" + "run-1-short");
//                        writeCarryBuilder.append(shortQueryTopic).append(" Q0 ").append(mapAccess.getKey()).append(" ").append((scanCount + 1)).append(" ").append(mapAccess.getValue()).append(" run-1-short");
//                        writeCarryTitle.println(writeCarryBuilder.toString());
//                        writeCarryBuilder.setLength(0);
//                        writeCarryBuilder.trimToSize();
//                        scanCount++;
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//
//            }
//            shortQueryTopic++;



            while (matchShortQuery.find()) {

                try {

                    String queryString = matchShortQuery.group(1).replaceAll("Topic:", "").trim();
                    PriorityQueue<Double> pq1 = new PriorityQueue<Double>(10, Collections.reverseOrder());

                    HashMap storedMap = new HashMap();
                    StringBuilder writeQueryBuilder = new StringBuilder();
                    Analyzer analyzer = new StandardAnalyzer();
                    QueryParser parser = new QueryParser("TEXT", analyzer);
                    Query query;
                    query = parser.parse(queryString);
                    Set<Term> queryTerms = new LinkedHashSet<Term>();
                    searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
                    ClassicSimilarity dSimi = new ClassicSimilarity();
                    List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();

                    for (int i = 0; i < leafContexts.size(); i++) {

                        LeafReaderContext leafContext = leafContexts.get(i);
                        int startDocNo = leafContext.docBase;
                        int numberOfDoc = leafContext.reader().maxDoc();

                        for (int docId = 0; docId < numberOfDoc; docId++) {
                            int occurance = 0;
                            double queryScore = 0;

                            float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));
                            float realDocLength = 1 / (normDocLeng * normDocLeng);

                            for (Term t : queryTerms) {


                                int df = reader.docFreq(new Term("TEXT", t.text()));
                                PostingsEnum p = MultiFields.getTermDocsEnum(leafContext.reader(), "TEXT", new BytesRef(t.text()));

                                int doc;
                                if (p != null) {
                                    while ((doc = p.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {

                                        if (p.docID() + startDocNo == docId + startDocNo) {
                                            occurance = p.freq();
                                            break;
                                        }
                                    }
                                }

                                double termScore = (occurance / realDocLength) * Math.log(1 + (totalDocLength / df));
                                queryScore += termScore;
                            }

                            pq1.offer(queryScore);
                            if (!storedMap.containsKey(docId + startDocNo)) {
                                storedMap.put(searcher.doc(docId + startDocNo).get("DOCNO"), queryScore);
                            }
                        }
                    }

                    HashMap sortedMapShortQuery = sortonMap(storedMap);
                    Set setShortQuery = sortedMapShortQuery.entrySet();
                    Iterator scannerShortQuery = setShortQuery.iterator();

                    int scanCount = 0;
                    while (scannerShortQuery.hasNext() && scanCount < 1000) {
                        Map.Entry mapAccess = (Map.Entry) scannerShortQuery.next();
                        System.out.println(shortQueryTopic + "\t" + "Q0" + "\t" + mapAccess.getKey() + "\t" + (scanCount + 1) + "\t" + mapAccess.getValue() + "\t" + "run-1-short");
                        writeQueryBuilder.append(shortQueryTopic).append(" Q0 ").append(mapAccess.getKey()).append(" ").append((scanCount + 1)).append(" ").append(mapAccess.getValue()).append(" run-1-short");
                        writeCarryTitle.println(writeQueryBuilder.toString());
                        writeQueryBuilder.setLength(0);
                        writeQueryBuilder.trimToSize();
                        scanCount++;
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            shortQueryTopic++;

            // long query
//        Pattern patDescription = Pattern.compile("<desc>(.+?)<smry>", Pattern.DOTALL);
//        Matcher matchDescription = patDescription.matcher(s);

            while (matchLongQuery.find()) {

                try {

                    String queryString = matchLongQuery.group(1).replaceAll("Description:", "").trim();
                    PriorityQueue<Double> pq1 = new PriorityQueue<Double>(10, Collections.reverseOrder());

                    HashMap storedMap = new HashMap();
                    StringBuilder writeQueryBuilder = new StringBuilder();
                    Analyzer analyzer = new StandardAnalyzer();
                    QueryParser parser = new QueryParser("TEXT", analyzer);
                    Query query;
                    query = parser.parse(queryString);
                    Set<Term> queryTerms = new LinkedHashSet<Term>();
                    searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
                    ClassicSimilarity dSimi = new ClassicSimilarity();
                    List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();

                    for (int i = 0; i < leafContexts.size(); i++) {

                        LeafReaderContext leafContext = leafContexts.get(i);
                        int startDocNo = leafContext.docBase;
                        int numberOfDoc = leafContext.reader().maxDoc();

                        for (int docId = 0; docId < numberOfDoc; docId++) {
                            int occurance = 0;
                            double queryScore = 0;

                            float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));
                            float realDocLength = 1 / (normDocLeng * normDocLeng);

                            for (Term t : queryTerms) {


                                int df = reader.docFreq(new Term("TEXT", t.text()));
                                PostingsEnum p = MultiFields.getTermDocsEnum(leafContext.reader(), "TEXT", new BytesRef(t.text()));

                                int doc;
                                if (p != null) {
                                    while ((doc = p.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {

                                        if (p.docID() + startDocNo == docId + startDocNo) {
                                            occurance = p.freq();
                                            break;
                                        }
                                    }
                                }

                                double termScore = (occurance / realDocLength) * Math.log(1 + (totalDocLength / df));
                                queryScore += termScore;
                            }

                            pq1.offer(queryScore);
                            if (!storedMap.containsKey(docId + startDocNo)) {
                                storedMap.put(searcher.doc(docId + startDocNo).get("DOCNO"), queryScore);
                            }
                        }
                    }

                    HashMap sortedMapLongQuery = sortonMap(storedMap);
                    Set setLongQuery = sortedMapLongQuery.entrySet();
                    Iterator scannerLongQuery = setLongQuery.iterator();

                    int scanCount = 0;
                    while (scannerLongQuery.hasNext() && scanCount < 1000) {
                        Map.Entry mapAccess = (Map.Entry) scannerLongQuery.next();
                        System.out.println(longQueryTopic + "\t" + "Q0" + "\t" + mapAccess.getKey() + "\t" + (scanCount + 1) + "\t" + mapAccess.getValue() + "\t" + "run-1-long");
                        writeQueryBuilder.append(longQueryTopic).append(" Q0 ").append(mapAccess.getKey()).append(" ").append((scanCount + 1)).append(" ").append(mapAccess.getValue()).append(" run-1-long");
                        writeCarryDescription.println(writeQueryBuilder.toString());
                        writeQueryBuilder.setLength(0);
                        writeQueryBuilder.trimToSize();
                        scanCount++;
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            longQueryTopic++;
//            writeCarryTitle.close();
//            writeCarryDescription.close();
        }
    }
}
