import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;


public class easySearch {


    public void term(int totalLength, String queryString, IndexReader reader, IndexSearcher searcher) {


        try {

            // As given by the assign, call the standard analyzer
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("TEXT", analyzer);
            Query query = parser.parse(queryString);
            Set<Term> queryTerms = new LinkedHashSet<Term>();

            // extract terms
            searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);

            for (Term t : queryTerms) {
                //System.out.println(t.text());


                // get frq for preparison of tfidf
                int df=reader.docFreq(new Term("TEXT", t.text()));

                //Use DefaultSimilarity.decodeNormValue(…) to decode normalized document length
                ClassicSimilarity dSimi = new ClassicSimilarity();

                // Get the segments of the index
                List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();

                HashMap storeMap = new HashMap();
                double termSocre=0;


                // Processing each segment
                for (int i = 0; i < leafContexts.size(); i++) {

                    LeafReaderContext leafContext = leafContexts.get(i);
                    int startDocNo = leafContext.docBase;
                    int numberOfDoc = leafContext.reader().maxDoc();

                    // store docnum,freq pair into a hashmap
                    PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(), "TEXT", new BytesRef(t.text()));
                    int doc;
                    if (de != null) {
                        while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
                            if(!storeMap.containsKey(de.docID()+startDocNo)){
                                storeMap.put(de.docID()+startDocNo, de.freq());
                            }
                        }
                    }

                    // count the occurance of terms in documents
                    // set the start docid to 0
                    for (int docId = 0; docId < numberOfDoc+0; docId++) {

                        //init occurance
                        int occurance=0;

                        // Get normalized length (1/sqrt(numOfTokens)) of the document
                        float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));

                        // Get the real length of the document
                        float realDocLength = 1 / (normDocLeng * normDocLeng);

                        if(storeMap.containsKey(docId + startDocNo)){

                            // convert to int for further tfidf caculation
                            occurance = (int) storeMap.get(docId + startDocNo);
                        }

                        //tfidf ranking function
                        termSocre = (occurance/realDocLength)*Math.log(1+(totalLength/df));

                        System.out.println( "For doc number " + searcher.doc(docId + startDocNo).get("DOCNO") + " : ");
                        System.out.println("Score on each term " + t.text() +" is " + termSocre);

                    }





                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void totalquery(int totalLength, String queryString, IndexReader reader, IndexSearcher searcher) {

        try {

            // similiar to the each term function
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("TEXT", analyzer);
            Query query;
            query = parser.parse(queryString);
            Set<Term> queryTerms = new LinkedHashSet<Term>();
            searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);

            ClassicSimilarity dSimi = new ClassicSimilarity();

            List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();


            double termSocre=0;


            for (int i = 0; i < leafContexts.size(); i++) {

                LeafReaderContext leafContext = leafContexts.get(i);
                int startDocNo = leafContext.docBase;
                int numberOfDoc = leafContext.reader().maxDoc();


                for (int docId = 0; docId < numberOfDoc; docId++) {

                    // each term occurance in each doc
                    int occurance=0;


                    float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));

                    float realDocLeng = 1/(normDocLeng * normDocLeng);


                    // whole query
                    double queryScore=0;
                    for (Term t : queryTerms) {
                        int docIncludeTerm=reader.docFreq(new Term("TEXT", t.text()));

                        PostingsEnum p = MultiFields.getTermDocsEnum(leafContext.reader(), "TEXT", new BytesRef(t.text()));

                        int doc;
                        if (p != null) {
                            while ((doc = p.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
                                if(p.docID()+startDocNo == docId + startDocNo){
                                    occurance = p.freq();
                                    break;
                                }
                            }
                        }

                        //TFIDF
                        termSocre = (occurance/realDocLeng)*Math.log(1+(totalLength/docIncludeTerm));

                        // sum on each term
                        queryScore+=termSocre;

                    }
                    System.out.println( "For doc number " + searcher.doc(docId + startDocNo).get("DOCNO") + " : ");
                    System.out.println("Score on query " + queryString +  " is " + queryScore);

                }
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws ParseException, IOException {

        String indexpath = "index";
        easySearch search = new easySearch();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexpath)));
        IndexSearcher searcher = new IndexSearcher(reader);


        String queryString = "the great wall";


        int totalLength = reader.maxDoc();
        // call the score on each term
        search.term(totalLength, queryString, reader, searcher);

        //call the score on total query
        search.totalquery(totalLength, queryString, reader, searcher);
    }
}