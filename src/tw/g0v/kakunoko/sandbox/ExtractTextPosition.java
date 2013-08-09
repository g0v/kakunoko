package tw.g0v.kakunoko.sandbox;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

public class ExtractTextPosition extends PDFTextStripper {

	/**
	 * 用不到的建構式
	 * 
	 * @throws IOException
	 */
	public ExtractTextPosition() throws IOException {
		// TODO
	}
	
	/**
	 * 每次處理字元時，攔截字元資訊
	 */
	@Override
	protected void processTextPosition(TextPosition txp) {
		System.out.printf(
			"字元: \"%s\", 位置: (%.3f, %.3f)\n",
			txp.getCharacter(),
			txp.getX(),
			txp.getY()
		);
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
		ExtractTextPosition exp = new ExtractTextPosition();
		exp.processStream(page0, page0.getResources(), page0.getContents().getStream());
	}

}
