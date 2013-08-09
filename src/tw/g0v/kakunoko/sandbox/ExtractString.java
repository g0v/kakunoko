package tw.g0v.kakunoko.sandbox;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

/**
 * 字串偵測練習
 * 
 * @author raymond
 */
public class ExtractString extends PDFTextStripper {

	// 字串緩衝
	private StringBuffer strbuf = new StringBuffer();
	private float strX;
	private float strY;
	
	// 第一個字元 & 上一個字元
	private TextPosition txpFirst = null;
	private TextPosition txpPrevious = null;
	
	private static final float X_NEARBY_THRESHOLD = 10;
	
	/**
	 * 用不到的建構式
	 * 
	 * @throws IOException
	 */
	public ExtractString() throws IOException {
		// TODO
	}

	/**
	 * 每次處理字元時，攔截字元資訊
	 */
	@Override
	protected void processTextPosition(TextPosition txp) {
		if(!isNextChar(txp)) {
			if(!strbuf.toString().equals("")) {
				System.out.format("(%.2f, %.2f) %s\n",strX,strY,strbuf);
			}
			strbuf.delete(0, strbuf.length());
		}
		
		strbuf.append(txp.getCharacter());
	}
	
	/**
	 * 比對目前的字元和上一個字元是不是落在同一個字串
	 *  
	 * @param txpCurrent 目前的字元
	 * @return
	 */
	private boolean isNextChar(TextPosition txpCurrent) {
		boolean is_next = false;

		if(txpPrevious!=null) {
			if(txpPrevious.getY()==txpCurrent.getY()) {
				float prevX = txpPrevious.getX() + txpPrevious.getWidth();
				float currX = txpCurrent.getX();
				if(currX-prevX<X_NEARBY_THRESHOLD) {
					is_next = true;
				}
			}
		}
		
		if(!is_next) {
			if(txpFirst!=null) {
				strX = txpFirst.getX();
				strY = txpFirst.getY();
			}
			txpFirst = txpCurrent;
		}
		
		txpPrevious = txpCurrent;
		return is_next;
	}
	
	/**
	 * 簡單練習一下 PDF 解文字的內部構造
	 * 
	 * @param args 不可能會用到的參數
	 */
	public static void main(String[] args) throws Exception {
		String docname = "samples/難民手續認定指南.pdf";
		int    pagenum = 13;
		
		PDDocument pd = PDDocument.load(new File(docname));
		PDPage page0 = (PDPage)pd.getDocumentCatalog().getAllPages().get(pagenum);
		ExtractString es = new ExtractString();
		es.processStream(page0, page0.getResources(), page0.getContents().getStream());
	}

}
