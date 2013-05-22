/**
 * 
 */
package individual.adam.util;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Joyband
 * 
 */
public class WordItem {

	/*
	 * DataTable(0) 表名称：Trans，中英文基本翻译，包含一个 Row，5 个 Item    
	 * Item(“WordKey”) = 需要进行翻译的单词（输入的单词）    
	 * Item(“Pron”) = 音标（英文）、拼音（中文字）    
	 * Item(“Info”) = 中文字的国标码、部首、笔画、笔顺信息    
	 * Item(“Translation”) = 翻译、解释，多个翻译使用中文“；”分隔    
	 * Item(“Mp3”) = 英文单词朗读 Mp3 文件名 
	 *（下载 Mp3 地址： http://fy.webxml.com.cn/sound/Mp3 文件名） 
	 * 如果没有发现单词翻译或出现错误，Item(“Translation”) 会出现以下提示：    
	 * WordKey Empty ---- 没有输入单词 
	 * Not Found ---- 不能翻译 
	 * Error ---- 系统错误 
	 * Not Data ---- 没有输据 
	 * 
	 * DataTable(1) 表名称：Refer，中译英的相关词条，包含多个 Row，
	 * 1 个 Item    Item(“Rel”) = 相关词条 
	 * DataTable(2) 表名称：Sentence，中译英的例句，包含多个 Row，
	 * 2 个 Item    
	 * 	Item(“Orig”) = 正本 
	 *    Item(“Trans”) = 译本
	 */
	private String wordKey;
	private String pron;
	private String info;
	private String translation;
	private String mp3Name;

	private String errorMsg;

	private ArrayList<String> referList;
	private ArrayList<Map<String, String>> sentences;
	
	private ArrayList<String> translations;
	
	private ArrayList<String> suggestWords;
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("[")
		.append(wordKey).append(",")
		.append(pron).append(",")
		.append(info).append(",")
		.append(translation).append(",")
		.append(mp3Name).append(",")
		.append(errorMsg).append(",")
		.append("referList.size="+(referList == null?0:referList.size())).append(",")
		.append("sentences.size="+(sentences==null?0:sentences.size())).append(",")
		.append("suggestWords.size="+(suggestWords==null?0:suggestWords.size()))
		.append("]");
		
		return sb.toString();
	}

	public String getWordKey() {
		return wordKey;
	}

	public void setWordKey(String wordKey) {
		this.wordKey = wordKey;
	}

	public String getPron() {
		return pron;
	}

	public void setPron(String pron) {
		this.pron = pron;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getTranslation() {
		return translation;
	}

	public void setTranslation(String translation) {
		this.translation = translation;
	}

	public String getMp3Name() {
		return mp3Name;
	}

	public void setMp3Name(String mp3Name) {
		this.mp3Name = mp3Name;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public ArrayList<String> getReferList() {
		return referList;
	}

	public void setReferList(ArrayList<String> referList) {
		this.referList = referList;
	}

	public ArrayList<Map<String, String>> getSentences() {
		return sentences;
	}

	public void setSentences(ArrayList<Map<String, String>> sentences) {
		this.sentences = sentences;
	}

	public ArrayList<String> getTranslations() {
		return translations;
	}

	public void setTranslations(ArrayList<String> translations) {
		this.translations = translations;
	}

	public ArrayList<String> getSuggestWords() {
		return suggestWords;
	}

	public void setSuggestWords(ArrayList<String> suggestWords) {
		this.suggestWords = suggestWords;
	}
}
