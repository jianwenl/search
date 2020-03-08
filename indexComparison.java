import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.File;

public class indexComparison {


    public static void main(String[] args) throws Exception {

        // we clean the index directory each time after we
        // finished indexing for analyzer

        System.out.println("indexing for KeywordAnalyzer......");
        generateIndex KeyAnalyz = new generateIndex(new KeywordAnalyzer());
        KeyAnalyz.dataLoader();
        KeyAnalyz.printResandStats("result");
        System.out.println("Success for KeywordAnalyzer!");
        File mappings = new File("result");
        FileUtils.cleanDirectory(mappings);


        System.out.println("indexing for SimpleAnalyzer......");
        generateIndex SimpleAnalyz = new generateIndex(new SimpleAnalyzer());
        SimpleAnalyz.dataLoader();
        SimpleAnalyz.printResandStats("result");
        System.out.println("Success for SimpleAnalyzer!");

        FileUtils.cleanDirectory(mappings);

        System.out.println("indexing for StopAnalyzer......");
        generateIndex StopAnalyz = new generateIndex(new StopAnalyzer());
        StopAnalyz.dataLoader();
        StopAnalyz.printResandStats("result");
        System.out.println("Success for StopAnalyzer!");

        FileUtils.cleanDirectory(mappings);

        System.out.println("indexing for StandardAnalyzer......");
        generateIndex StandAnalyz = new generateIndex(new StandardAnalyzer());
        StandAnalyz.dataLoader();
        StandAnalyz.printResandStats("result");
        System.out.println("Success for StandardAnalyzer!");

        FileUtils.cleanDirectory(mappings);


    }
}