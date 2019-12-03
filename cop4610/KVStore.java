package nachos.cop4610;
import java.util.*;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
 

/**
 * This is a dummy KeyValue Store. Ideally this would go to disk, 
 * or some other backing store. For this project, we simulate the disk like 
 * system using a manual delay.
 *
 */
public class KVStore implements KeyValueInterface {
	private Dictionary<String, String> store 	= null;
	
	public KVStore() {
		resetStore();
	}

	private void resetStore() {
		store = new Hashtable<String, String>();
	}
	
	public void put(String key, String value) throws KVException {
		AutoGrader.agStorePutStarted(key, value);
		
		try {
			putDelay();
			store.put(key, value);
		} finally {
			AutoGrader.agStorePutFinished(key, value);
		}
	}
	
	public String get(String key) throws KVException {
		AutoGrader.agStoreGetStarted(key);
		
		try {
			getDelay();
			String retVal = this.store.get(key);
			if (retVal == null) {
			    KVMessage msg = new KVMessage("resp", "key \"" + key + "\" does not exist in store");
			    throw new KVException(msg);
			}
			return retVal;
		} finally {
			AutoGrader.agStoreGetFinished(key);
		}
	}
	
	public void del(String key) throws KVException {
		AutoGrader.agStoreDelStarted(key);

		try {
			delDelay();
			if(key != null)
				this.store.remove(key);
		} finally {
			AutoGrader.agStoreDelFinished(key);
		}
	}
	
	private void getDelay() {
		AutoGrader.agStoreDelay();
	}
	
	private void putDelay() {
		AutoGrader.agStoreDelay();
	}
	
	private void delDelay() {
		AutoGrader.agStoreDelay();
	}
	
    public String toXML() {
    	try {
    		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    		Document doc = docBuilder.newDocument();
    		doc.setXmlStandalone(true);
    		
    		// build DOM
    		Element kvStoreNode = doc.createElement("KVStore");
    		doc.appendChild(kvStoreNode);
    		
    		List<String> keyList = Collections.list(store.keys());
    		java.util.Collections.sort(keyList);
    		
    		for (String key : keyList) {
//    			String key = keys.nextElement();
    			String value = store.get(key);
    			
    			Element kvPairNode = doc.createElement("KVPair");
    			kvStoreNode.appendChild(kvPairNode);
    			
    			Element keyNode = doc.createElement("Key");
    			keyNode.setTextContent(key);
    			kvPairNode.appendChild(keyNode);
    			
    			Element valueNode = doc.createElement("Value");
    			valueNode.setTextContent(value);
    			kvPairNode.appendChild(valueNode);
    		}
    		
    		// output to string
    		TransformerFactory transformerFactory = TransformerFactory.newInstance();
    		Transformer transformer = transformerFactory.newTransformer();
    	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    		StringWriter writer = new StringWriter();
    		transformer.transform(new DOMSource(doc), new StreamResult(writer));
    		return writer.getBuffer().toString();
    	} catch (Exception e) {
    		System.err.println("KVStore::dumpToString: Exception building DOM: " + e);
    	}
		return "";
    }        

    public void dumpToFile(String fileName) {
        String xmlContent = toXML();
        
        try {
	    	FileWriter fw = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(xmlContent);
			bw.close();
        } catch (IOException e) {
        	System.err.println("KVStore::dumpToFile: file output exception: " + e);
        }
    }
    
    public void restoreFromFile(String fileName) {
    	BufferedReader br = null;
        try {
        	StringBuilder sb = new StringBuilder();
        	br = new BufferedReader(new FileReader(fileName));
        	String line = br.readLine();
        	
        	while (line != null) {
        		sb.append(line);
        		sb.append("\n");
        		
        		line = br.readLine();
        	}
        	
        	restoreFromString(sb.toString());
        } catch (IOException e) {
        	System.err.println("KVStore::dumpToString: exception reading file: " + e);
        } finally {
        	if (br != null) {
        		try {
        			br.close();
	        	} catch (IOException e) {        		
	        	}
        	}
        }
    }
    
    public void restoreFromString(String xmlContent) {
    	try {
	    	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	
	        Document doc = docBuilder.parse(new InputSource(new StringReader(xmlContent)));
	        
	        resetStore();
	        
	        Node root = doc.getFirstChild();
	        if (root.getNodeName() != "KVStore") {
	        	System.err.println("KVStore::restoreFromString: input DOM structure error: root not KVStore");
	        	return;
	        }
	        
	        for (Node kvPairNode=root.getFirstChild(); kvPairNode!=null; kvPairNode=kvPairNode.getNextSibling()) {
	        	String nodeKey = null, nodeValue = null;

	        	if (kvPairNode.getNodeName().startsWith("#")) {
	        		continue;
	        	} else if (kvPairNode.getNodeName() != "KVPair") {
	        		System.err.println("KVStore::restoreFromString: input DOM structure error: expected KVPair, got " + kvPairNode.getNodeName());
		        	return;	        		
	        	}
	        	
	        	for (Node kvAttrNode=kvPairNode.getFirstChild(); kvAttrNode!=null; kvAttrNode=kvAttrNode.getNextSibling()) {
	        		String attrType = kvAttrNode.getNodeName();
	        		if (attrType.startsWith("#")) {
	        			continue;
	        		} else if (attrType == "Key") {
	        			if (nodeKey != null) {
	        				System.err.println("KVStore::restoreFromString: input DOM structure error: duplicate Key");
				        	return;	        				
	        			} else {
	        				nodeKey = kvAttrNode.getTextContent();
	        			}
	        		} else if (attrType == "Value") {
	        			if (nodeValue != null) {
	        				System.err.println("KVStore::restoreFromString: input DOM structure error: duplicate Value");
				        	return;	        				
	        			} else {
	        				nodeValue = kvAttrNode.getTextContent();
	        			}
	        		} else {
	        			System.err.println("KVStore::restoreFromString: input DOM structure error: unexpected KVPair attr");
			        	return;	        			
	        		}
	        	}
	        	
        		if (nodeKey == null || nodeValue == null) {
        			System.err.println("KVStore::restoreFromString: input DOM structure error: missing key or value");
		        	return;
        		}
        		
        		store.put(nodeKey, nodeValue);
	        }
	        
	        
    	} catch (Exception e) {
    		System.err.println("KVStore::restoreFromString: Exception building DOM: " + e);
    	}
	}
}
