import java.nio.file.Paths;
import java.io.File;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;


public class generateIndex {

    // set the path
    final String corpurspath = "corpus";
    final String resultpath = "result";


    //get the analyzer
    public generateIndex(Analyzer ChosenAnalyzer) throws Exception {

        // Direct to the path to store the index
        // Given in Assignment for indexing in Lucene
        Directory dir = FSDirectory.open(Paths.get(resultpath));
        Analyzer analyzer = ChosenAnalyzer;
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        writer = new IndexWriter(dir, iwc);
    }
    IndexWriter writer;


    // load the data
    public  void dataLoader() throws Exception{

        final File fileCollect = new File(corpurspath);
        for (final File fileEntry : fileCollect.listFiles()) {
            File file = new File(corpurspath+"/"+fileEntry.getName()+"");
            // if the file with a trectext extends, we add the file for parser
            if(FilenameUtils.getExtension(file.getName()).equals("trectext")){
                // call the parse function for parsing
                dataParser(file);
            }
        }
        writer.forceMerge(1);
        writer.commit();
        writer.close();
    }

    // for parse and store data
    public void dataParser(File file) throws Exception{

        // convert to string
        String fileToString = FileUtils.readFileToString(file,(String)null);

        // seperate by DOC tag
        String lineDoc = System.getProperty("line.separator");
        String individuleDocument[] = fileToString.split("</DOC>"+lineDoc+"<DOC>");

        // for each document
        for(int i=0; i<individuleDocument.length; i++){
            Document luceneDoc = new Document();

            // for each field
            // we set a container stringbuffer to store string
            // we only need one container and clear it every time after loading for each field

            StringBuffer container = new StringBuffer();


            // locate
            // for docno
            String docno = StringUtils.substringBetween(individuleDocument[i], "<DOCNO>", "</DOCNO>");
            // insert into the container
            container.append(docno);
            luceneDoc.add(new StringField("DOCNO", container.toString(),Field.Store.YES));
            //clear stringbuffer
            container.setLength(0);


            //for head
            String head = StringUtils.substringBetween(individuleDocument[i], "<HEAD>", "</HEAD>");
            container.append(head);
            luceneDoc.add(new TextField("HEAD", container.toString(),Field.Store.YES));
            container.setLength(0);
            // for byline
            String byline = StringUtils.substringBetween(individuleDocument[i], "<BYLINE>", "</BYLINE>");
            container.append(byline);
            luceneDoc.add(new TextField("BYLINE", container.toString(),Field.Store.YES));
            container.setLength(0);

            // for dateline
            String dateline = StringUtils.substringBetween(individuleDocument[i], "<DATELINE>", "</DATELINE>");
            container.append(dateline);
            luceneDoc.add(new TextField("DATELINE", container.toString(),Field.Store.YES));
            container.setLength(0);

            // for text
            String testsString = individuleDocument[i];
            String text = StringUtils.substringBetween(testsString, "<TEXT>", "</TEXT>");
            container.append(text);
            container.append("\\s+");
            testsString = testsString.substring(testsString.indexOf("</TEXT>")+7);
            while(true){
                text = StringUtils.substringBetween(testsString, "<TEXT>", "</TEXT>");
                if(testsString.indexOf("</TEXT>")==-1){
                    break;
                }
                container.append(text);
                container.append("\\s+");
                testsString = testsString.substring(testsString.indexOf("</TEXT>")+7);
            }
            luceneDoc.add(new TextField("TEXT", container.toString(),Field.Store.YES));

            writer.addDocument(luceneDoc);
        }
    }

    //This function displays the statistics
    public void printResandStats(String path) throws Exception{


        // Given in the Assignment Requirement for useful statistics and some results

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(resultpath+'/')));

        //Print the total number of documents in the corpus
        System.out.println("Total number of documents in the corpus:"+reader.maxDoc());

        //Print the number of documents containing the term "new" in <field>TEXT</field>.
        System.out.println("Number of documents containing the term \"new\" for field \"TEXT\": "+reader.docFreq(new Term("TEXT", "new")));

        //Print the total number of occurrences of the term "new" across all documents for <field>TEXT</field>.
        System.out.println("Number of occurences of \"new\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","new")));




        //Print the size of the vocabulary for <field>content</field>, only available per-segment.
        Terms vocabulary = MultiFields.getTerms(reader, "TEXT");
        System.out.println("Size of the vocabulary for this field: "+vocabulary.size());

        //Print the total number of documents that have at least one term for <field>TEXT</field>
        System.out.println("Number of documents that have at least one term for this field: "+vocabulary.getDocCount());

        //Print the total number of tokens for <field>TEXT</field>
        System.out.println("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());

        //Print the total number of postings for <field>TEXT</field>
        System.out.println("Number of postings for this field: "+vocabulary.getSumDocFreq());

		//Print the vocabulary for <field>TEXT</field>
//		TermsEnum iterator = vocabulary.iterator();
//		System.out.println("\n*******Vocabulary-Start**********");
//		BytesRef byteRef;
//		while((byteRef = iterator.next()) != null) {
//			String term = byteRef.utf8ToString();
//			System.out.print(term+"\n");
//			}
//		System.out.println("\n*******Vocabulary-End**********");

        reader.close();
    }
    public static void main(String[] args) throws Exception {

        generateIndex StandAnalyz = new generateIndex(new StandardAnalyzer());
        System.out.println("load the data......");
        StandAnalyz.dataLoader();
        System.out.println("Indexing......");
        StandAnalyz.printResandStats("result");
        System.out.println("Success for StandardAnalyzer!");

    }
}
