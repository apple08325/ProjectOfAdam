package individual.adam.util;

import java.net.URLEncoder;

/**
 * @author Joyband
 * 
 */
public class StringUtil {
	/**
	 * 按照指定的要求分割字符串
	 * 
	 * @param src
	 *            被分割字符串
	 * @param sp
	 *            分隔符
	 * @param noEmpty
	 *            是否包含null和空字符串
	 * 
	 * @return 分割后的字符串数组
	 */
	public static String[] splitString(String src, String sp, boolean noEmpty) {
		if (src == null || src.trim().length() == 0)
			return null;
		String[] array = src.split(sp);
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				array[i] = array[i].trim();
			}
		}

		return array;
	}

	/**
	 * 为防止音标/拼音显示乱码，需要进行 Asc 编码转换，下面是在网页上使用编码后显示文字 的方法
	 * 
	 * @param src
	 * @return
	 */
	public static String ascEncode(String src) {
		if (src == null || src.trim().length() == 0) {
			return null;
		} else {
			StringBuffer sb = new StringBuffer();
			char[] chars = src.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				int j = chars[i];
				//除ASCII  31-127以外进行编码,以防止网页乱码
				if (j > 31 && j < 127) {
					sb.append(chars[i]);
				} else {
					sb.append("&#" + j + ";");
				}
			}
			return sb.toString();
		}
	}
	
	public static void main(String[] args){
		System.out.println("gʊd");
	}
	
	public static String encodeString(String src,String enc){
		try{
			return URLEncoder.encode(src, enc);
		}catch(Exception ex){
			return src;
		}
	}

}
