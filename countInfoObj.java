import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import java.io.StringReader;
import java.util.*;

// we create a class
// to store the normalized freq of each token in the doc, and use this weights for further Rocchio
class countInfoObj {

    // field and output
    private HashMap<String, Double> tokenFreqMap;
    private LinkedList<String> storedText;
    private int documentlength;
    private boolean adv;

    // constructor
    public countInfoObj(boolean adv, LinkedList<String> storedText){

        this.storedText = storedText;
        this.tokenFreqMap = null;
        this.documentlength = storedText.size();
        this.adv = adv;
    }

    // caculation of mnormalized weights
    // with token freq and the hyperparameters
    // this is like the feature map
    public double normalizedTokenFreqFeature(String token){
        if (this.tokenFreqMap.containsKey(token)) {
            return this.tokenFreqMap.get(token);
        } else { return 0.0000;}
    }

    // method to extract tokens
    public Set tokeninDoc() {
        return this.tokenFreqMap.keySet();
    }

    // method to extract tokens
    public int lengthofDocs() {
        return this.documentlength;
    }

    // iterate on doc and token, get the count
    public void countRecorder(IndexReader reader) throws Exception{

        LinkedList<String> docText = this.storedText;
        HashMap<String, Double> normCountValueE = new HashMap();
        HashMap<String, Integer> freqinDocE = new HashMap();

        // for each document
        for (String doct: docText){
            Analyzer analyzerdoc = new StandardAnalyzer();
            TokenStream tokenstreamDoc = analyzerdoc.tokenStream(null,new StringReader(doct));
            CharTermAttribute tokenstreamwithAttrDoc = tokenstreamDoc.addAttribute(CharTermAttribute.class);
            tokenstreamDoc.reset();
            int wordCountinDoc = 0;
            while (tokenstreamDoc.incrementToken()) {
                String tsStringE = tokenstreamwithAttrDoc.toString();
                if (!freqinDocE.containsKey(tsStringE)) {
                    freqinDocE.put(tsStringE, 1);
                }
                else { freqinDocE.put(tsStringE, freqinDocE.get(tsStringE)+1); }
                wordCountinDoc += 1;
            }
            RC(wordCountinDoc,reader, normCountValueE, freqinDocE,adv);
            freqinDocE = new HashMap();
            tokenstreamDoc.end();
            tokenstreamDoc.close();
            analyzerdoc.close();
        }
        this.tokenFreqMap = normCountValueE;
    }

    public void RC( int wordNumberinDoc,IndexReader reader, HashMap<String, Double> normalizeCountValue, HashMap<String, Integer> freqinCount, boolean adv) throws Exception {

        for (Map.Entry<String, Integer> freqCountSet: freqinCount.entrySet()) {
            String token = freqCountSet.getKey();
            double idf;
            if (adv) {
                //https://zhuanlan.zhihu.com/p/31197209
                int numberofDocswithToken = reader.docFreq(new Term("TEXT", token));
                int numberofDocAll = reader.numDocs();
                idf = Math.log(numberofDocAll / (numberofDocswithToken + 1));
            }
            else {
                idf = 1.0;
            }
            double termcountValue = (double) freqCountSet.getValue() / wordNumberinDoc * idf;
            if (!normalizeCountValue.containsKey(token)) {
                normalizeCountValue.put(token, termcountValue); }
            else {
                normalizeCountValue.put(token, normalizeCountValue.get(token) + termcountValue); }
        }
    }
}