package co.orffosoft.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Sides;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppPreference;
import co.orffosoft.core.util.AppUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.DirectBillingDTO;
import co.orffosoft.dto.StockAdjustmentDTO;
import co.orffosoft.entity.CustomerMaster;
import co.orffosoft.entity.ProductVarietyMaster;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * @author PRATAP
 *
 */
@Log4j2
@Service("directBillingBean")
@Scope("session")
public class DirectBillingBean {

	@Getter
	@Setter
	String action;
	
	@Getter
	@Setter
	Boolean disabledCustomer = true;

	@Getter
	@Setter
	DirectBillingDTO directBillingDTO;
	
	@Getter
	@Setter
	List<CustomerMaster> customerMasterlist = new ArrayList<>();

	@Autowired
	HttpService httpService;

	@Autowired
	AppPreference appPreference;

	@Getter
	@Setter
	Date billDate;
	
	@Getter
	@Setter
	Double totalnetamount=0.0;

	public static final String SERVER_URL = AppUtil.getPortalServerURL();

	private final String DIRECT_BILLING_LIST = "/pages/billing/directBillingList.xhtml?faces-redirect=true;";

	private final String DIRECT_BILLING_ADD = "/pages/billing/directBillingAdd.xhtml?faces-redirect=true;";

	@Getter
	@Setter
	List<DirectBillingDTO> directBillingDTOList;

	@Getter
	@Setter
	ProductVarietyMaster productVarietyMaster;

	@Getter
	@Setter
	List<ProductVarietyMaster> productVarietyMasterList;
	
	@Getter
	@Setter
	String customerName;
	
	@Getter
	@Setter
	Long mobileNumber;
	
	@Getter
	@Setter
	String content;
	
	@Getter
	@Setter
	CustomerMaster selectedcustomerMaster = new CustomerMaster();

	/**
	 * @return
	 */
	public String getListPage() {
		log.info("Start getListPage call ");
		totalnetamount=0.0;
		productVarietyMaster = new ProductVarietyMaster();
		directBillingDTO = new DirectBillingDTO();
		directBillingDTOList = new ArrayList<>();
		customerName = null;
		mobileNumber = null;
//		directBillingDTOList.add(directBillingDTO);

		log.info("End getListPage call ");
		return DIRECT_BILLING_ADD;

	}

	/**
	 * @return
	 */
	public String addPage() {
		log.info("Start addPage call ");

		log.info("End addPage call ");
		return DIRECT_BILLING_ADD;

	}

	public void print(BaseDTO baseDTO) throws IOException {

		try {
		// directBillingDTO = new DirectBillingDTO();
		UserManagementBean ub = new UserManagementBean();

		DateFormat dateformat = new SimpleDateFormat("dd/mm/yy");
		DateFormat timeformat = new SimpleDateFormat("HH:mm");
		Date date = new Date();
		Date time = new Date();
		String name = "";

		String Date = dateformat.format(date);
		String Time = timeformat.format(time);
		
		String printData = "'<center><b>"+directBillingDTO.getStoreName()+"</b></center> nextLine <center> BillNo: " + directBillingDTO.getBillNo() + " nextLine D: " + Date + " T:"+Time+"  </center> ---------------------------------- "
				+ "nextLine <b> Desc. &nbsp; | Qty | Rate | Amt </b> nextLine ---------------------------------- nextLine " + printItemName(directBillingDTOList) + " ---------------------------------- nextLine <b> total Qnty=" + TotalQty(directBillingDTOList) + " nextLine TotalAmount = " + directBillingDTO.getSellingPrice()+" nextLine "
				+ "Total GST Amount=0.0 nextLine TotalNet Amount = "+ directBillingDTO.getTotalNetPrice() +"</b> nextLine ---------------------------------- nextLine <small> THANK YOU VISIT AGAIN </small> ';";

		content = printData;
		log.info("Print "+content);
		
//		String shopName = "" + directBillingDTO.getStoreName() + "\n";
//		String line = "-------------------------------\n";
//		String dt = "D: " + Date + "  T:" + Time + "\n";
//		String head = "Desc.   | Qyt | Rate  | Amt\n"; // 8,5,8,7
//		String items = "" + printItemName(directBillingDTOList) + "";
//		String amt = "Total Qyt=" + TotalQty(directBillingDTOList) + "\nTotal Amount=" + directBillingDTO.getSellingPrice()
//				+ "\nTotal Net Amount=" + directBillingDTO.getTotalNetPrice() + "\n";
//
//		String end = "THANK YOU--VISIT AGAIN\n\n\n";
//		String bln = "BillNo: " + directBillingDTO.getBillNo() + "\n";
//
//		PrintRequestAttributeSet patts = new HashPrintRequestAttributeSet();
//		patts.add(Sides.DUPLEX);
//		PrintService[] PrintServices = PrintServiceLookup.lookupPrintServices(null, null);
//		PrintService mysrvice = null;
//		for (PrintService printService : PrintServices) {
//			System.out.println(printService.getName());
//			if (printService.getName().equals("Everycom-58-Series")) {
//				mysrvice = printService;
//				break;
//			}
//		}
//		ByteArrayOutputStream expected = new ByteArrayOutputStream();
//
//		expected.write(POS.POSPrinter.Justification(POS.Justifications.Center));
//		expected.write(POS.POSPrinter.CharSize.DoubleHeight3());
//		expected.write(shopName.getBytes());
//		expected.write(POS.POSPrinter.CharSize.Normal());
//		expected.write(line.getBytes());
//		expected.write(bln.getBytes());
//		expected.write(dt.getBytes());
//		expected.write(POS.POSPrinter.Justification(POS.Justifications.Left));
//		expected.write(POS.POSPrinter.SetStyles(POS.PrintStyle.Bold));
//		expected.write(head.getBytes());
//		expected.write(POS.POSPrinter.SetStyles(POS.PrintStyle.None));
//		expected.write(line.getBytes());
//		expected.write(items.getBytes());
//		expected.write(line.getBytes());
//		expected.write(POS.POSPrinter.SetStyles(POS.PrintStyle.Bold));
//		expected.write(amt.getBytes());
//		expected.write(POS.POSPrinter.SetStyles(POS.PrintStyle.None));
//		expected.write(POS.POSPrinter.Justification(POS.Justifications.Center));
//		expected.write(line.getBytes());
//		expected.write(POS.POSPrinter.CharSize.DoubleHeight2());
//		expected.write(end.getBytes());
//		expected.write(POS.POSPrinter.CharSize.Normal());
//
//		DocPrintJob job = mysrvice.createPrintJob();
//		Doc doc = new SimpleDoc(expected.toByteArray(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
//
//		
//
//			job.print(doc, new HashPrintRequestAttributeSet());
		} catch (Exception e) {
			log.error(" Exception Occured On print call " + e);
		}

	}

	private String printItemName(List<DirectBillingDTO> directBillingDTOList) {

		String input = "";
		if (directBillingDTOList != null) {

			for (DirectBillingDTO dto : directBillingDTOList) {
				String name = "" + dto.getItemName();
				String qnty = " " + dto.getBilledQnty();
				String urt = " " + dto.getSellingPrice();
				String tnp = " " + dto.getTotalNetPrice();

				if (name.length() >= 8) {
					name = name.substring(0, 8) + "";

				} else {
					int i = name.length();
					if (i < 8) {

						for (int j = i; j < 8; j++)
							name = name + "&nbsp;";
					}
				}
				if (qnty.length() > 5) {

				} else {
					int i = qnty.length();
					if (i < 5) {
						for (int j = i; j < 5; j++)
							qnty = qnty + "&nbsp;";
					}
				}

				if (urt.length() > 8) {
					urt = urt.substring(0, 8) + "";

				} else {
					int i = urt.length();
					if (i < 8) {
						for (int j = i; j < 8; j++)
							urt = urt + "&nbsp;";
					}
				}
				if (tnp.length() > 7) {
					tnp = tnp.substring(0, 7) + "";
				} else {
					int i = tnp.length();
					for (int j = i; j < 7; j++)
						tnp = tnp + "&nbsp;";
				}

				String item = name + "|" + qnty + "|" + urt + "|" + tnp + "nextLine";
				input = input + item;
			}
		}
		return input;
	}

	private long TotalQty(List<DirectBillingDTO> cst) {

		long l = 0;
		double totalrate = 0.0;
		double sgst = 0.0;
		double cgst = 0.0;
		double netamount = 0.0;
		DecimalFormat df2 = new DecimalFormat("#.##");

		if (cst != null) {
			for (DirectBillingDTO dto : cst) {
				long Qnt = dto.getBilledQnty();
				l = l + Qnt;

				double tnp = dto.getTotalNetPrice();
				totalrate = totalrate + tnp;

				double sgctamt = 0;
				sgst = sgst + sgctamt;

				double cgctamt = 0;
				cgst = cgst + cgctamt;
			}
			netamount = totalrate + sgst + cgst;

			
		}
		//directBillingDTO.setTotalRate1(Double.parseDouble(df2.format(totalrate).toString()));
		//directBillingDTO.setTotalGst(Double.parseDouble(df2.format(sgst+cgst).toString()));
		directBillingDTO.setSellingPrice(Double.parseDouble(df2.format(netamount).toString()));
		directBillingDTO.setTotalNetPrice(Double.parseDouble(df2.format(netamount).toString()));
		return l;
	}

	/**
	 * This Method Used To Remove Added Item In Table For Billing
	 * 
	 * @param customerBillingDto
	 */
	public void removeItem(DirectBillingDTO directBillingDTO) {

		if (directBillingDTO != null) {
			
			directBillingDTOList.remove(directBillingDTO);
			totalnetamount=totalnetamount-directBillingDTO.getTotalNetPrice();
		}

	}

	/**
	 *
	 * 
	 * @param customerbillingDto
	 */
	public void creatRow() {
		
		if (directBillingDTO != null) {
			if (directBillingDTO.getDiscountAmt() == null) {
				directBillingDTO.setDiscountAmt(0D);
			}
			if (directBillingDTO.getSellingPrice() != null && directBillingDTO.getSellingPrice() > 0) {
				directBillingDTO
						.setTotalSellingPrice(directBillingDTO.getSellingPrice() * directBillingDTO.getBilledQnty());
				directBillingDTO.setTotalNetPrice(
						directBillingDTO.getTotalSellingPrice() - directBillingDTO.getDiscountAmt());
			}
		}
		directBillingDTO.setCustomerName(customerName);
		directBillingDTO.setMobileNumber(mobileNumber);
		directBillingDTO.setProductVarietyId(productVarietyMaster.getId());
		directBillingDTO.setItemName(productVarietyMaster.getName());
		directBillingDTOList.add(directBillingDTO);
		totalnetamount=directBillingDTOList.stream().mapToDouble(x->x.getTotalNetPrice()).sum();
		directBillingDTO = new DirectBillingDTO();
		productVarietyMaster = new ProductVarietyMaster();
	}

	public List<ProductVarietyMaster> itemAutoSearch(String itemName) {

		log.info(":::<- Load itemAutocomplete TypeStart ->::: ");
		BaseDTO baseDTO = null;
		try {
			if (itemName.trim() != null && !itemName.isEmpty()) {
				String url = SERVER_URL + "/stockuploadcontroller/autocompleteitemName/" + itemName;
				log.info("::: loadProductVarietyDetails Controller calling  1 :::");
				baseDTO = httpService.get(url);
				log.info("::: get itemAutocomplete Response :");
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					productVarietyMasterList = new ArrayList<ProductVarietyMaster>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					productVarietyMasterList = mapper.readValue(jsonResponse,
							new TypeReference<List<ProductVarietyMaster>>() {
							});
					log.info("itemAutocomplete load Successfully " + baseDTO.getTotalRecords());
					log.info("itemAutocomplete Page Convert Succes -->");
				} else {
					log.warn("itemAutocomplete Error ");
				}
			}
		} catch (Exception ee) {
			log.error("Exception Occured in itemAutocomplete load ", ee);
		}

		return productVarietyMasterList;

	}

	/**
	 * @param directBillingDTO
	 */
	public void ajaxItemSelect(DirectBillingDTO directBillingDTO) {

		if (productVarietyMaster != null) {
			directBillingDTO.setProductVarietyId(productVarietyMaster.getId());
			directBillingDTO.setItemName(productVarietyMaster.getName());
		}

	}

	/**
	 * @param customerdto
	 */
	public void updateUnitRateWithGST(DirectBillingDTO directBillingDTO) {

		if (directBillingDTO != null) {
			if (directBillingDTO.getDiscountAmt() == null) {
				directBillingDTO.setDiscountAmt(0D);
			}
			if (directBillingDTO.getSellingPrice() != null && directBillingDTO.getSellingPrice() > 0) {
				directBillingDTO
						.setTotalSellingPrice(directBillingDTO.getSellingPrice() * directBillingDTO.getBilledQnty());
				
				totalnetamount= totalnetamount-	directBillingDTO.getTotalNetPrice();
				directBillingDTO.setTotalNetPrice(
						directBillingDTO.getTotalSellingPrice() - directBillingDTO.getDiscountAmt());
				
				totalnetamount= totalnetamount+	directBillingDTO.getTotalNetPrice();
				
			}
		}
	}

	/**
	 * This Method Used To Save Billing Information And used for Print Bill Receipt
	 */
	/**
	 * 
	 */
	public void billPrint() {

		log.info(":::<- Load billPrint Start ->::: ");
		BaseDTO baseDTO = null;
		try {
			if (directBillingDTOList != null && directBillingDTOList.size() > 0) {
				directBillingDTO.setDirectBillingDTOList(directBillingDTOList);
				String url = SERVER_URL + "/directBillingController/save/";
				log.info("::: billPrint Controller calling  :::");
				baseDTO = httpService.post(url, directBillingDTOList);
				log.info("::: get itemAutocomplete Response :");
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					directBillingDTO=new DirectBillingDTO();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContent());
					directBillingDTO = mapper.readValue(jsonResponse, new TypeReference<DirectBillingDTO>() {
					});
					print(baseDTO);
					AppUtil.addInfo(" Bill Printed Successfully and Bill No: "+directBillingDTO.getBillNo());
					getListPage();
				} else {
					log.warn("billPrint Error ");
				}
			} else {
				AppUtil.addInfo("Please Add Item");
			}
		} catch (Exception ee) {
			log.error("Exception Occured in billPrint load ", ee);
		}
	}
	
	public List<CustomerMaster> mobileAutocomplete(String mobileNo) {

		log.info(":::<- Load mobileAutocomplete TypeStart ->::: ");
		BaseDTO baseDTO = null;
		try {
			if (mobileNo.trim() != null && !mobileNo.isEmpty()) {
				//mobileNumber =Long.parseLong(mobileNo) ;
				String url = SERVER_URL + "/directBillingController/autocompletemobile/" + mobileNo;
				baseDTO = httpService.get(url);
				selectedcustomerMaster = new CustomerMaster();
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					customerMasterlist = new ArrayList<CustomerMaster>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					customerMasterlist = mapper.readValue(jsonResponse, new TypeReference<List<CustomerMaster>>() {
					});
				} else {
					log.warn("mobileAutocomplete Error ");
				}
			}
		} catch (Exception ee) {
			log.error("Exception Occured in supplierAutocomplete load ", ee);
		}

		return customerMasterlist;

	}
	
	public void updateMobileNumber() {

		if (selectedcustomerMaster != null && selectedcustomerMaster.getId() != null) {
			customerName = selectedcustomerMaster.getName();
			mobileNumber= Long.parseLong(selectedcustomerMaster.getPrimaryContactNumber()) ;
			disabledCustomer = true;
		} else {
			disabledCustomer = false;
		}
		// if (selectedcustomerMaster == null) {
		// disabledCustomer = false;
		// } else if (selectedcustomerMaster.getId() == null) {
		// disabledCustomer = false;
		// } else {
		// disabledCustomer = true;
		// }
	}

}
