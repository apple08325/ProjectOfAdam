/**
 * 
 */
package individual.adam.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Joyband
 * 
 */
public class TranslatorHelper {

	public static String[] METHOD_NAMES = { "Translator", "TranslatorString",
			"TranslatorReferString", "TranslatorSentenceString", "SuggestWord",
			"GetMp3" };

	private static final String TRANSLATOR = "Translator";
	private static final String TRANSLATOR_STRING = "TranslatorString";
	private static final String TRANSLATOR_REFER_STRING = "TranslatorReferString";
	private static final String TRANSLATOR_SENTENCE_STRING = "TranslatorSentenceString";
	private static final String SUGGEST_WORD = "SuggestWord";
	private static final String GETMP3 = "GetMp3";
	
	public static WordItem queryByStep(String wordKey) {
		wordKey = StringUtil.encodeString(wordKey, "UTF-8");
		WordItem word = new WordItem();
		try {
			// TranslatorString
			word = translatorString(wordKey);

			// TranslatorReferString
			String[] referArray = translatorReferString(wordKey);
			if (referArray != null && referArray.length > 0
					&& !referArray[0].equals("- Empty -")) {
				ArrayList<String> referList = new ArrayList<String>();
				for (String s : referArray)
					referList.add(s);
				word.setReferList(referList);
			}

			// TranslatorSentenceString
			referArray = translatorSentenceString(wordKey);
			if (referArray != null && referArray.length > 0
					&& !referArray[0].equals("- Empty -")) {
				ArrayList<Map<String, String>> sentences = new ArrayList<Map<String, String>>();
				for (String s : referArray) {
					Map<String, String> map = new HashMap<String, String>();
					StringTokenizer st = new StringTokenizer(s, "|");
					map.put("Orig", st.nextToken());
					map.put("Trans", st.nextToken());
					sentences.add(map);
				}
				word.setSentences(sentences);
			}

			// SuggestWord
			referArray = suggestWord(wordKey);
			if (referArray != null && referArray.length > 0
					&& !referArray[0].equals("- Empty -")) {
				ArrayList<String> suggestWords = new ArrayList<String>();
				for (String s : referArray)
					suggestWords.add(s);
				word.setSuggestWords(suggestWords);
			}

			// GetMp3
			// 这里暂不实现，到调用时机时直接调用返回byte数组的方法，或者直接访问URL

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return word;
	}
	
	/**
	 * Translator 方法
	 * 通过输入中文或英文单词进行双向翻译，输入参数 wordKey = 字符串，中文或英文单词， 返回名称为：“Dictionary”的 DataSet。
	 * 此 Dataset 包含三个 DataTable。
	 * DataTable(0) 表名称：Trans，中英文基本翻译，包含一个 Row，5 个 Item
	 * 
	 * @param wordKey
	 * @return
	 * @throws Exception
	 */
	public static WordItem translator(String wordKey) throws Exception {
		wordKey = StringUtil.encodeString(wordKey, "UTF-8");
		WordItem word = new WordItem();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("wordKey", wordKey);
		String returnString = WSUtil.callWebService(
				TranslatorHelper.TRANSLATOR, params);
		System.out.println("returnString=" + returnString);

		parseWordItem(returnString, word);
		
		// SuggestWord
		String[] referArray = suggestWord(wordKey);
		if (referArray != null && referArray.length > 0
				&& !referArray[0].equals("- Empty -")) {
			ArrayList<String> suggestWords = new ArrayList<String>();
			for (String s : referArray)
				suggestWords.add(s);
			word.setSuggestWords(suggestWords);
		}

		return word;
	}

	/**
	 * TranslatorString 方法 
	 * 通过输入中文或英文单词获得基本翻译，输入参数 wordKey = 字符串，中文或英文单词，
	 * 返回为：一维数组，String[0] ---- String[4]。  
	 * String[0] = 需要进行翻译的单词（输入的单词） 
	 * String[1] = 音标（英文）、拼音（中文字）  
	 * String[2] = 中文字的国标码、部首、笔画、笔顺信息  
	 * String[3] = 翻译、解释，多个翻译使用中文“；”分隔  
	 * String[4] = 英文单词朗读 Mp3 文件名
	 * 
	 * 如果没有发现单词翻译或出现错误，String[3] 会出现以下提示：  
	 * WordKey Empty ---- 没有输入单词  
	 * Not Found ---- 不能翻译  
	 * Error ---- 系统错误  
	 * Not Data ---- 没有输据
	 * 
	 * @param wordKey
	 * @return  返回为：一维数组，String[0] ---- String[4]
	 * @throws Exception
	 */
	public static WordItem translatorString(String wordKey) throws Exception {
		String[] resultArray = null;
		WordItem word = new WordItem();
		;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("wordKey", wordKey);
		String returnString = WSUtil.callWebService(
				TranslatorHelper.TRANSLATOR_STRING, params);
		//System.out.println("returnString=" + returnString);
		resultArray = parseResult(returnString);
		if (resultArray != null) {
			word.setWordKey(resultArray[0]);
			word.setPron(resultArray[1]);
			word.setInfo(resultArray[2]);
			word.setTranslation(resultArray[3]);
			if (resultArray[3].contains("WordKey Empty")
					|| resultArray[3].contains("Not Found")
					|| resultArray[3].contains("Error")
					|| resultArray[3].contains("Not Data")) {
				word.setErrorMsg("没有相关翻译内容");
			} else {
				String[] s3 = resultArray[3].split("；");
				ArrayList<String> translations = new ArrayList<String>();
				word.setTranslations(translations);
				for (int i = 0; i < s3.length; i++) {
					translations.add(s3[i]);
				}
			}
			word.setMp3Name(resultArray[4]);
		}
		return word;
	}

	/**
	 * TranslatorReferString 方法 
	 * 通过输入中文单词获得相关词条，输入参数 wordKey = 字符串，中文单词
	 * @param wordKey
	 * @return
	 * 返回为：一 维数组，String[0] ---- String[n]。 
	 * 没有内容或没有结果返回提示： String[0] =    - Empty -
	 * @throws Exception
	 */

	public static String[] translatorReferString(String wordKey)
			throws Exception {
		String[] resultArray = null;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("wordKey", wordKey);
		String returnString = WSUtil.callWebService(
				TranslatorHelper.TRANSLATOR_REFER_STRING, params);
		if (returnString != null && returnString.length() > 0) {
			resultArray = parseResult(returnString);
		}
		
		return resultArray;
	}

	/**
	 * TranslatorSentenceString 方法 
	 * 通过输入中文或英文单词获得中译英的例句，输入参数 wordKey = 字符串，中文单词， 返回为：一维数组，String[0] ---- String[n]。
	 * 结构为：正本|译本。 	（以“|”分隔符）
	 * 没有内容或没有结果返回提示： String[0] =    - Empty -
	 * @param wordKey
	 * @return
	 * @throws Exception
	 */
	public static String[] translatorSentenceString(String wordKey)
			throws Exception {
		String[] resultArray = null;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("wordKey", wordKey);
		String returnString = WSUtil.callWebService(
				TranslatorHelper.TRANSLATOR_SENTENCE_STRING, params);
		//System.out.println("returnString=" + returnString);
		// anyType{string=anyType{}; string=anyType{}; string=anyType{};
		// string=WordKey Empty; string=anyType{}; }

		if (returnString != null && returnString.length() > 0) {
			resultArray = parseResult(returnString);
		}

		return resultArray;
	}

	/**
	 * SuggestWord 方法
	 * 通过输入英文单词获得候选词，输入参数 wordKey = 字符串，英文单词，返回为：一维 数组，String[0] ---- String[n]。
	 * 没有内容或没有结果返回提示： String[0] =    - Empty -
	 * @param wordKey
	 * @return
	 * @throws Exception
	 */
	public static String[] suggestWord(String wordKey) throws Exception {
		String[] resultArray = null;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("wordKey", wordKey);
		String returnString = WSUtil.callWebService(
				TranslatorHelper.SUGGEST_WORD, params);
		//System.out.println("returnString=" + returnString);

		if (returnString != null && returnString.length() > 0) {
			resultArray = parseResult(returnString);
		}

		return resultArray;
	}

	/**
	 * GetMp3 方法
	 * 输入参数 Mp3 = 字符串，Mp3 文件名。返回为 Mp3 字节流 Byte[]。
	 * Mp3 文件名可通过 Translator方法 或 TranslatorString方法 获得。
	 * 如没有 Mp3 返回 Byte[] = {0,0,0,0,0,0,}	（开发可以通过 Byte.Length 判断是否有 Mp3 文件）
	 * 也可以通过访问： http://fy.webxml.com.cn/sound/Mp3 文件名      下载 Mp3 文件
	 * @param Mp3
	 * @return
	 * @throws Exception
	 */
	public static byte[] getMp3(String Mp3) throws Exception {
		byte[] mp3Byte = null;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("Mp3", Mp3);
		mp3Byte = WSUtil.callWebServiceByte(TranslatorHelper.GETMP3, params);
		System.out.println("returnString="+mp3Byte);
		
		return mp3Byte;
	}
	
	private static String[] parseResult(String returnString){
		//anyType{string=GOOGLE; string=gu:gl; string=anyType{}; 
		//string=1,在互联网上快速查找信息；2,1*10的一百次方；The largest search engine on the Web, launched in 1999. As of 2005, the Google index of Web pages points to more than eight ; 
		//string=anyType{}; }
		if(returnString == null || returnString.trim().length() == 0)
			return null;
		List<String> list = new ArrayList<String>();
		int firstIdx = returnString.indexOf("{");
		int endIdx = returnString.lastIndexOf("}");
		String tmp = returnString.substring(firstIdx+1, endIdx);
		System.out.println("tmp="+tmp);
		String[] ta = tmp.split("; ");
		if(ta == null || ta.length == 0)
			return null;
		for(String s:ta){
			if(s.endsWith("anyType{}")){
				list.add("");
			}else{
				list.add(s.substring("string=".length()));
			}
		}
		
		if(list.size() == 0)
			return null;
		
		return list.toArray(new String[0]);
	}
	
	
	/**
	 * 解析单词
	 * 
	 * @param str
	 * @param word
	 */
	private static void parseWordItem(String str,WordItem word){
		/*
		 * Dictionary=anyType{
			Trans=anyType{
				WordKey=good; Pron=g?d; Info=anyType{}; 
				Translation=n. 善行,好处；adj. 好的,优良的,上等的；[pl.]商品; Mp3=1033.mp3; 
			}; 
			Sentence=anyType{Orig=My one good suit is at the cleaner's.; Trans=我那套讲究的衣服还在洗衣店里呢。; }; 
			Sentence=anyType{Orig=He was very good to me when I was ill.; Trans=我生病时他帮了我的大忙。; }; 
			Sentence=anyType{Orig=This is a good place for a picnic.; Trans=这是一个野餐的好地方。; }; 
		}; 
		 */
		if(str == null || str.trim().length() == 0)
			return;
		int len = str.length();
		int p1 = str.indexOf("Dictionary=anyType{");
		int p2 = -1;
		int cnt = 0;
		List<String> list = new ArrayList<String>();
		for(int i=p1;i<len;i++){
			if(str.charAt(i) == '{'){
				cnt++;
			}else if(str.charAt(i) == '}'){
				cnt--;
				if(cnt == 0){
					p2 = i;
					break;
				}
			}
		}
		//System.out.println("p1="+p1+",p2="+p2);
		String tmp = str.substring(p1 + "Dictionary=anyType{".length(),p2);
		//System.out.println("\n\n tmp=\n\n"+tmp);
		/*
		 * tmp=
		 * Trans=anyType{WordKey=good; Pron=g?d; Info=anyType{}; Translation=n. 善行,好处；adj. 好的,优良的,上等的；[pl.]商品; Mp3=1033.mp3; }; 
			Sentence=anyType{Orig=My one good suit is at the cleaner's.; Trans=我那套讲究的衣服还在洗衣店里呢。; }; 
			Sentence=anyType{Orig=He was very good to me when I was ill.; Trans=我生病时他帮了我的大忙。; }; 
			Sentence=anyType{Orig=This is a good place for a picnic.; Trans=这是一个野餐的好地方。; }; 
		 */
		String Trans = tmp.substring("Trans=anyType{".length(), tmp.indexOf(" }; "));
		System.out.println("\n\n trans=\n\n"+Trans);
		
		String[] resultArray = Trans.split(";");
		if(resultArray == null || resultArray.length == 0)
			return ;
		for(String s:resultArray){
			System.out.println("s="+s);
			if(s.trim().length() == 0)
				continue;
			if(s.endsWith("anyType{}")){
				list.add("");
			}else{
				int n = s.indexOf("=");
				list.add(s.substring(n+1).trim());
			}
		}
		resultArray = list.toArray(resultArray);
		word.setWordKey(resultArray[0]);
		word.setPron(resultArray[1]);
		word.setInfo(resultArray[2]);
		word.setTranslation(resultArray[3]);
		if(resultArray[3].contains("WordKey Empty") || resultArray[3].contains("Not Found")
				||resultArray[3].contains("Error")||resultArray[3].contains("Not Data")){
			word.setErrorMsg("没有相关翻译内容");
		}else {
			String[] s3 = resultArray[3].split("；");
			ArrayList<String> translations = new ArrayList<String>();
			word.setTranslations(translations);
			for(int i=0;i<s3.length;i++){
				translations.add(s3[i]);
			}
		}
		word.setMp3Name(resultArray[4]);
		
		tmp = tmp.substring(Trans.length() + " }; ".length() + "Trans=anyType{".length());
		//System.out.println("\n\ntmp=\n\n"+tmp);
		String[] sentences = tmp.split("Sentence=anyType");
		ArrayList<Map<String, String>> slist = new ArrayList<Map<String, String>>();
		Map<String, String> sMap;
		word.setSentences(slist);
		for(String s:sentences){
			//System.out.println("\n\n sentences =\n\n"+s);
			if(s.trim().length() == 0)
				continue;
			sMap = new HashMap<String,String>();
			int n = s.indexOf("; ");
			String orig = s.substring(s.indexOf("Orig=")+"Orig=".length(), n);
			s = s.substring(n+1);
			n = s.indexOf("; ");
			String tmpTrans = s.substring("Trans=".length()+1, n);
			
			sMap.put("Orig", orig);
			sMap.put("Trans", tmpTrans);
			
			slist.add(sMap);
		}
	}
	public static void main(String[] args){
		try {
			WordItem item = TranslatorHelper.translator("basic");
			System.out.println("item.getPron():"+item.getPron());
			System.out.println("item.getTranslation():"+item.getTranslation());
			System.out.println("item.getSentences():"+item.getSentences());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
