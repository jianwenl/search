import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;

public class Rocchio {

    private static HashMap sortingMap(HashMap mapinput) {

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

    // main function
    public static void main(String[] args) {

        try {
            String indexPath = "index";
            String queryRelevantDocPath = "topic judgement for feedback.txt";
            String queryTopicPath = "topics.51-100";

            // choose input for advanced Rochio or not
            boolean adv;
            Scanner in = new Scanner(System.in);
            System.out.print("do you want to use advanced Rocchio instead of normal Rocchio? true/false");
            adv = in.nextBoolean();
            System.out.println(adv);

            // search on the index
            // similar to hw2 and hw1
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new ClassicSimilarity());

            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("TEXT", analyzer);

            // trigger obj
            HashMap<Integer, HashMap<Integer, countInfoObj>> tokensofCompenDocs = relatedDocFeatureMap(reader,queryRelevantDocPath,adv);
            // use regex to get the number and topics
            Pattern titlePattern = Pattern.compile("<title>[ ]*Topic:[ ]*(.+)");
            Pattern numberPattern = Pattern.compile("<num>\\s*Number:\\s*(\\d+)");
            // main logic
            // topic text and search pattern
            String[] topicText = new String(Files.readAllBytes(Paths.get(queryTopicPath))).split("(?=</top>\\s+<top>)");


            // association of two parameters, use a hashmap to store this association
            HashMap<String, double[]> paraPairs = new HashMap();
            double[] gammalist = new double[] {
                    1.0, 0.8,
                    0.6, 0.4,
                    0.2, 0.0};
            double[] betalist = new double[] {
                    1.0, 0.8,
                    0.6, 0.4,
                    0.2};
            for(int i = 0; i<betalist.length;i++) {
                double beta = betalist[i];
                for (int j = 0; j<gammalist.length;j++) {
                    double gamma = gammalist[j];
                    paraPairs.put(String.format("%02.0f*%02.0f", beta*10, gamma*10),
                            new double[]{beta,gamma}); } }

            System.out.println(paraPairs);

            // iterate on params
            for(Map.Entry<String, double[]> storedParas: paraPairs.entrySet()) {
                String CombinedParameters = storedParas.getKey();
                double beta = storedParas.getValue()[0];double gamma = storedParas.getValue()[1];boolean sameToicMark = true;

                // iterate on topics
                for (String indivTopic: topicText) {
                    // find the number
                    Matcher matcherN = numberPattern.matcher(indivTopic);
                    matcherN.find();


                    // get the topic number
                    int topicNumber = Integer.parseInt(matcherN.group(1).trim());

                    // find the title
                    Matcher matcherT = titlePattern.matcher(indivTopic);
                    matcherT.find();
                    // extract the totken list from each title or short queries
                    Set<String> shortQueryTokens = new LinkedHashSet();
                    Analyzer tokenanalyzer = new StandardAnalyzer();
                    TokenStream titletokenstream = tokenanalyzer.tokenStream(null, new StringReader(matcherT.group(1).trim()));
                    CharTermAttribute tokenstreamwithAttr = titletokenstream.addAttribute(CharTermAttribute.class);
                    titletokenstream.reset();
                    while (titletokenstream.incrementToken())
                    {shortQueryTokens.add(tokenstreamwithAttr.toString());}
                    titletokenstream.end();
                    titletokenstream.close();
                    tokenanalyzer.close();
//                  serrch on the new compensated queries and store into socredocumnents as result
                    String compensatedQuery = rocchioExpending(shortQueryTokens, tokensofCompenDocs.get(topicNumber), beta, gamma);
//                    System.out.println(compensatedQuery);
                    Query query = parser.parse(compensatedQuery);
                    ScoreDoc[] scoredocuments = searcher.search(query, 1000).scoreDocs;

                    // outpput result
                    try {
                        String namingIter = "RC_" + CombinedParameters;
                        if (sameToicMark) { Files.deleteIfExists(Paths.get(namingIter));Files.createFile(Paths.get(namingIter)); }
                        List<String> storedWritter = new ArrayList();
                        for (int i = 0; i < scoredocuments.length; i++) {
                            int docID = scoredocuments[i].doc;
                            Document doc = searcher.doc(docID);
                            storedWritter.add(
                                    String.format("%d 0 %s %d %.5f %s%n", topicNumber, doc.get("DOCNO"), i+1,
                                    scoredocuments[i].score, namingIter)); }
                        String resultString = String.join("", storedWritter);
                        Files.write(Paths.get(namingIter), resultString.getBytes(), StandardOpenOption.APPEND);
                    } catch (Exception e) {
                        System.err.println("can not save.");
                        e.printStackTrace(System.err);
                        System.exit(-1);
                    }
                    String monit = String.format("ID is %d with beta is %.2f and gamma is %.2f", topicNumber,
                            Double.parseDouble(CombinedParameters.substring(0, 2))*0.1, Double.parseDouble(CombinedParameters.substring(3, 5))*0.1);
                    System.out.println(monit);
                    sameToicMark = false;
                }
            }
        } catch (Exception e) {
            System.err.println("fault.");
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }

    // core logic of Rocchio, compensate the query
    // we need four inputs to get back a Rocchio query:
    // hyperparas beta,gamma, original shortQuery(title), its corresponding relevant and irrelevant document
    public static String rocchioExpending(Set<String> tokeninQuery, HashMap<Integer, countInfoObj> ObjMap, double beta, double gamma){

        HashMap <String, Double> storedWeights = new HashMap();

        countInfoObj relObj = ObjMap.get(1);
        countInfoObj irrelObj = ObjMap.get(0);

        // store in the hashset of tokens
        // this is to create a vocabulary
        Set<String> vocablarySet = new HashSet();
        vocablarySet.addAll(tokeninQuery);
        vocablarySet.addAll(relObj.tokeninDoc());
        vocablarySet.addAll(irrelObj.tokeninDoc());

        for(String token : vocablarySet) {
            double alpha = 1.0;
            if (! tokeninQuery.contains(token))
            {alpha = 0.0;}

            double relFreqFeature = relObj.normalizedTokenFreqFeature(token)*beta;
            int rlenth = relObj.lengthofDocs();
            double RoRelWeights = (relFreqFeature/rlenth)*beta;

            double irrelFreqFeature = irrelObj.normalizedTokenFreqFeature(token)*beta;
            int irrlenth = relObj.lengthofDocs();
            double RoIRRelWeights = (irrelFreqFeature/irrlenth)*gamma;

            double summaofWeights = alpha + RoRelWeights - RoIRRelWeights;
            if (summaofWeights<0) { }else{ storedWeights.put(token, summaofWeights); }
        }

        Map<String, Double> sortedTokenWeights = sortingMap(storedWeights);
        List<String> tokeninShortQueries = new LinkedList();
        Integer counter = 1;
        for (Map.Entry<String, Double> weights: sortedTokenWeights.entrySet()) {
            Double processedWeight = weights.getValue();
            if (processedWeight.equals(0.0000))
                break;
            tokeninShortQueries.add(String.format("%s^%.4f", weights.getKey(), processedWeight));counter +=1;
            // req of lucene
            if (counter.equals(1024)){break;}
        }
        return String.join(" ", tokeninShortQueries);
    }

    // use obj to store the counting info of docs
    public static HashMap relatedDocFeatureMap(IndexReader reader, String queryRelevantDocPath, boolean adv) throws Exception{

        HashMap<Integer, HashMap<Integer, countInfoObj>> storedTermsinDoc = new HashMap();
        String alltext = new String(Files.readAllBytes(Paths.get(queryRelevantDocPath)));
        String[] documentText = alltext.split("(?=</top>\\s+<top>)");
        Pattern numberPattern = Pattern.compile("<num>\\s*(\\d+)");
        for (String stringinDocs: documentText) {
            // find topic nums
            Matcher matcherT = numberPattern.matcher(stringinDocs);
            matcherT.find();
            int topNums = Integer.parseInt(matcherT.group(1).trim());

            // noting that each paragraph changes with \n
            // not lines
            String [] paragraphinDoc = stringinDocs.split("\n");
            LinkedList<String> storedrText = new LinkedList();
            LinkedList<String> storedirText = new LinkedList();
//            int rTextLength = 0;int irTextLength = 0;
            for (int i =0;i<paragraphinDoc.length;i++) {
                String text = paragraphinDoc[i];

                // Noting we can not use replace string instead of substring method
                if (text.startsWith("<relevant>")){ storedrText.add(text.substring(10));}
                else if(text.startsWith("<irrelevant>")){storedirText.add(text.substring(12));}
            }
            // store relevant 1:relevantObj,
            // relevantObj is a hashmap with topicnum:obj, where obj stores feature of relevant docs
            countInfoObj relevantObj = new countInfoObj(adv,storedrText);
            relevantObj.countRecorder(reader);
            HashMap<Integer, countInfoObj> objMap = new HashMap();
            objMap.put(1, relevantObj);

            // store non relevant
            countInfoObj irrelevantObj = new countInfoObj(adv,storedirText);
            irrelevantObj.countRecorder(reader);
            objMap.put(0, irrelevantObj);
            storedTermsinDoc.put(topNums, objMap);
        }
            return storedTermsinDoc;
    }
}