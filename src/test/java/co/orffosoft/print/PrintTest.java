package co.orffosoft.print;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.Sides;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import co.orffosoft.bean.ProductVarietyMasterBean;
import lombok.extern.log4j.Log4j2;

/**
 * @author Pratap Kumar Sahu
 *
 */
@Log4j2
public class PrintTest implements Printable {

	public static void main(String arg[]){
		PrinterJob pj = PrinterJob.getPrinterJob();
		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
		aset.add(OrientationRequested.PORTRAIT);
		aset.add(MediaSizeName.INVOICE);
		aset.add(new PageRanges(1, 1));
		aset.add(Sides.DUPLEX);
		pj.setPrintable(new PrintTest());
		// boolean pd = pj.printDialog(aset);
		// if(pd) {
		try {
			log.info("Before Printing >> ");
			pj.print(aset);
			log.info("Printed SuccessFully >> ");
		} catch (PrinterException pe) {
			log.error("Exception Occured on Printer Test" + pe);
		}
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		graphics.drawString("AAAAA", 50, 100);
		return 0;
	}

}
