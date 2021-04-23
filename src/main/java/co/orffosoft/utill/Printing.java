package co.orffosoft.utill;

import java.io.FileInputStream;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j;

@Service("printing")
@Scope("session")
public class Printing {

	public static void main(String args[]) throws Exception {
		//log.info("Printing started");
		String filename = "Everycom-58-Seriess";
		PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
		DocFlavor flavor = DocFlavor.INPUT_STREAM.PNG;
		PrintService printService[] = PrintServiceLookup.lookupPrintServices(flavor, pras);
		for (int i = 0; i < printService.length; i++) {
			//log.info(printService[i].toString());
		}
		PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
		PrintService service = ServiceUI.printDialog(null, 200, 200, printService, defaultService, flavor, pras);
		//log.info(" PrintService >> defaultService ==== "+defaultService);
		//log.info(" PrintService >> service ==== "+service);
		if (service != null) {
			DocPrintJob job = service.createPrintJob();
			FileInputStream fis = new FileInputStream(filename);
			DocAttributeSet das = new HashDocAttributeSet();
			Doc doc = new SimpleDoc(fis, flavor, das);
			//log.info(" PrintService Before Print ==== ");
			job.print(doc, pras);
			//log.info(" PrintService Before Printed successfully ==== ");
			Thread.sleep(10000);
		}

		System.exit(0);
	}

}
