package co.orffosoft.utill;

import java.awt.Graphics;
import java.awt.PrintJob;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.Sides;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import co.orffosoft.bean.ProductVarietyMasterBean;
import lombok.extern.log4j.Log4j2;

/**
 * @author Pratap Kumar Sahu
 *
 */
@Log4j2
@Service("printTest")
@Scope("session")
public class PrintTest implements Printable {

	private final String LIST_PRODUCT_VARITY_MASTER = "/pages/printingAndStationary/print.xhtml?faces-redirect=true;";

	public String listPage() {
		try {

			ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
			ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("javascript");
			scriptEngine.eval(new FileReader("script.js"));
//			scriptEngine.eval("window.print()");
			Invocable invocable = (Invocable) scriptEngine;
//			invocable.invokeFunction("printData", "This is Test Print");
			invocable.invokeFunction("printPreview");

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} 
		 catch (ScriptException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//
		return null;

	}

	public void printTest() {
		log.info("PrintTest Method Started >> ");

		PrinterJob pj = PrinterJob.getPrinterJob();
		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
		aset.add(OrientationRequested.PORTRAIT);
		aset.add(MediaSizeName.INVOICE);
		aset.add(new PageRanges(1, 1));
		aset.add(Sides.DUPLEX);
		// pj.setPrintable(new PrintTest());
		boolean pd = pj.printDialog();
		log.info("Before Printing dialogs>> ");
		if (pd) {
			try {
				log.info("Before Printing >> ");
				pj.print();
				log.info("Printed SuccessFully >> ");
			} catch (PrinterException pe) {
				log.error("Exception Occured on Printer Test getStackTrace " + pe.getStackTrace());
				log.error("Exception Occured on Printer Test getCause " + pe.getCause());
				log.error("Exception Occured on Printer Test getMessage " + pe.getMessage());
			}
		}
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		graphics.drawString("Printed Successfully", 10, 100);
		return 0;
	}

}
