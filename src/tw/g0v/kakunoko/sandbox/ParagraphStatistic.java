package tw.g0v.kakunoko.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import tw.g0v.kakunoko.types.TextBound;

public class ParagraphStatistic extends PDFTextStripper {
	
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
	
	// y1 統計數據
	private Map<Float,Integer> yCount = new TreeMap<Float,Integer>();
	
	// 水平鄰近程度判別值
	private static final float X_NEARBY_THRESHOLD = 10;
	
	/**
	 * 沒屁用
	 * 
	 * @throws IOException
	 */
	public ParagraphStatistic() throws IOException {
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
		
		// 統計
		for(TextBound tb2 : textBounds) {
			if(yCount.containsKey(tb2.getY1())) {
				yCount.put(tb2.getY1(), yCount.get(tb2.getY1())+1);
			} else {
				yCount.put(tb2.getY1(), 1);
			}
		}
		
		// 容許誤差處理 (Y值接近時，即使不相等也視為同一列)
		Float[] yKeys = new Float[yCount.size()];
		yCount.keySet().toArray(yKeys);
		float currY, prevY = yKeys[0];
		for(int i=1;i<yKeys.length;i++) {
			currY = yKeys[i];
			if(currY-prevY<3) {
				yCount.put(prevY,yCount.get(prevY)+yCount.get(currY));
				yCount.remove(currY);
			} else {
				prevY = currY;
			}
		}
		
		// 顯示統計值
		for(float y1 : yCount.keySet()) {
			if(yCount.get(y1)==1) {
				System.out.format("y1=%.2f 出現 %d 次\n", y1, yCount.get(y1));
			} else {
				System.out.format("y1=%.2f 出現 %d 次 (可能有表格)\n", y1, yCount.get(y1));
			}
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
				currTb.setX2(Math.max(currTb.getX2(), nextTb.getX2()));
				currTb.setY2(nextTb.getY2());
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
		ParagraphStatistic es = new ParagraphStatistic();
		es.processStream(page0, page0.getResources(), page0.getContents().getStream());
		es.end();
	}

}
