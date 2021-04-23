package co.orffosoft.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.Setter;

@Service("printTestBean")
@Scope("session")
public class PrintTestBean implements Serializable {
	/**
	* 
	*/
	
	private static final long serialVersionUID = -3927409251841374148L;
	
	private final String BILLING_CB_PAGE = "/pages/printingAndStationary/printTest.xhtml?faces-redirect=true;";
	
	@Getter
	@Setter
	List<String> names;
	
	@Getter
	@Setter
	String content;
	
	public String listPage() {
		names = new ArrayList<String>();
		names.add("Pratap");
		names.add("Sahu");
		names.add("Laptop");
		
//		String nextLine = "\x0A";
//		String shopName = "" + "'Demo';" + "\n";
//		String bln = "'BillNo: " + "SAL/120';" + "\n";
//		String line = "'-------------------------------';\n";
//		String dt = "'D: " + "13-02-2021" + "  T:" + "11:00';" + "\n";
//		String head = "'Desc.   | Qyt | Rate  | Amt';\n"; // 8,5,8,7
//		String items = "' kurkure |  2  | 12  |  24 " + "';";
//		String amt = "'Total Qyt=" + "1';" + "\n'Total Amount=" + "24';"
//				+ "\n'Total GST Amount=" +"0';" + "\n'Total Net Amount= 24';"
//				+ "\n";
//
//		String end = "'THANK YOU--VISIT AGAIN';";
//		String space = "\n\n\n'';";
		
		
//		String printData = "'<center><b>Demo</b></center> nextLine <center> BillNo: SAL/120 nextLine D: 13-02-2021 / T:11.00 </center> ----------------------------- "
//				+ "nextLine Desc. &nbsp | Qty | Rate | Amt nextLine ----------------------------- nextLine kurkure  | 2 | 12 | 24 nextLine ----------------------------- nextLine total Qnty=1 nextLine TotalAmount = 24 nextLine "
//				+ "Total GST Amount=0 nextLine TotalNet Amount = 24 nextLine ----------------------------- nextLine <center><small> THANK YOU nextLine VISIT AGAIN </small></center> ';";
		
		String printData = "'Printed Successfully';";
				
		content = printData;
		
		return BILLING_CB_PAGE;
	}

}
