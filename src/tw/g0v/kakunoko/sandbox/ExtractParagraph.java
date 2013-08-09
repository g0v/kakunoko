package tw.g0v.kakunoko.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import tw.g0v.kakunoko.types.TextBound;

public class ExtractParagraph extends PDFTextStripper {
	
	// 文字範圍存放區
	private List<TextBound> textBounds = new ArrayList<TextBound>();
	
	// 字串緩衝
	private StringBuffer strbuf = new StringBuffer();
	private float strX1;
	private float strY1;
	private float strX2;
	private float strY2;
	
	// 第一個字元 & 上一個字元
	private TextPosition txpFirst = null;
	private TextPosition txpPrevious = null;
	
	private static final float X_NEARBY_THRESHOLD = 10;
	
	public ExtractParagraph() throws IOException {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 字元組合成字串
	 */
	@Override
	protected void processTextPosition(TextPosition txp) {
		if(!isNextChar(txp)) {
			if(!strbuf.toString().equals("")) {
				TextBound tb = new TextBound(strbuf.toString(),strX1,strY1,strX2,strY2);
				textBounds.add(tb);
			}
			strbuf.delete(0, strbuf.length());
		}
		strbuf.append(txp.getCharacter());
	}
	
	/**
	 * 合併字串成為段落
	 */
	public void end() {
		// 取得最後一個字串
		TextBound tb = new TextBound(strbuf.toString(),strX1,strY1,strX2,strY2);
		textBounds.add(tb);
		
		// 垂直合併
		mergeBound();
		
		// 顯示
		for(TextBound tb2 : textBounds) {
			System.out.println(tb2);
			System.out.println();
		}
	}

	/**
	 * 垂直偵測連續字串
	 */
	private void mergeBound() {
		for(int i=0;i<textBounds.size()-1;i++) {
			TextBound currTb = textBounds.get(i);
			TextBound nextTb = textBounds.get(i+1);
			
			while(currTb.getX1()==nextTb.getX1() && nextTb.getY1()-currTb.getY2()<15) {
				currTb.setString(currTb.getString()+"\n"+nextTb.getString());
				textBounds.remove(i+1);
				if(i+1<textBounds.size()) {
					nextTb = textBounds.get(i+1);
				} else {
					break;
				}
			}
		}
	}
	
	/**
	 * 水平偵測連續字元
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
				strX1 = txpFirst.getX();
				strY1 = txpFirst.getY();
				strX2 = txpPrevious.getX() + txpPrevious.getWidth();
				strY2 = txpPrevious.getY() + txpPrevious.getHeight();
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
		ExtractParagraph es = new ExtractParagraph();
		es.processStream(page0, page0.getResources(), page0.getContents().getStream());
		es.end();
	}

}
