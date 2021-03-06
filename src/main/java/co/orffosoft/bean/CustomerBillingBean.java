package co.orffosoft.bean;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppPreference;
import co.orffosoft.core.util.AppUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.CustomerBillingDTO;
import co.orffosoft.entity.Bill_D;
import co.orffosoft.entity.Bill_H;
import co.orffosoft.entity.CustomerMaster;
import co.orffosoft.entity.ProductVarietyMaster;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("customerBillingBean")
@Scope("session")
public class CustomerBillingBean implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -3927409251841374148L;

	private final String BILLING_CB_PAGE = "/pages/billing/createCustomerBilling.xhtml?faces-redirect=true;";

	@Getter
	@Setter
	List<CustomerMaster> customerMasterlist = new ArrayList<>();

	@Getter
	@Setter
	List<Date> expire;

	@Getter
	@Setter
	String barcodeType;

	@Getter
	@Setter
	String paymentmode;

	@Getter
	@Setter
	boolean barcode;

	@Getter
	@Setter
	String customerBillingType;

	@Getter
	@Setter
	boolean customer = false;

	@Getter
	@Setter
	CustomerMaster customerMaster = new CustomerMaster();

	@Getter
	@Setter
	List<CustomerBillingDTO> customerBillingDTOList;

	@Getter
	@Setter
	List<CustomerBillingDTO> customerBillingDTOItemList;

	@Getter
	@Setter
	Bill_H bill_H = new Bill_H();

	@Getter
	@Setter
	Bill_D bill_D = new Bill_D();

	@Getter
	@Setter
	CustomerMaster selectedcustomerMaster = new CustomerMaster();

	@Getter
	@Setter
	CustomerBillingDTO customerBillingDTO = new CustomerBillingDTO();

	@Autowired
	HttpService httpService;

	@Autowired
	AppPreference appPreference;

	@Getter
	@Setter
	Date billDate;

	public static final String SERVER_URL = AppUtil.getPortalServerURL();

	@Getter
	@Setter
	ProductVarietyMaster productVarietyMaster;

	@Getter
	@Setter
	List<ProductVarietyMaster> productVarietyMasterList;

	@Getter
	@Setter
	String pageAction, customerName, mobileNumber;

	@Getter
	@Setter
	Boolean disabledCustomer = true;

	@Getter
	@Setter
	String totalNetAmount;

	@Getter
	@Setter
	Double reciveamnt;

	@Getter
	@Setter
	String content = "''";

	DecimalFormat df = new DecimalFormat("0.00");

	int index;

	Double netamt;

	/**
	 * This Method Calls When 1st Request Getting On Customer Billing Page
	 * 
	 * @return
	 */
	public String showBillingPage() {
		reciveamnt = 0.0;
		barcodeType = "withoutbarcode";
		barcode = true;
		customerMasterlist = new ArrayList<>();
		totalNetAmount = "0";
		customerBillingDTOList = new ArrayList<>();
		customerBillingDTOItemList = new ArrayList<>();
		customerMaster = new CustomerMaster();
		selectedcustomerMaster = null;
		productVarietyMaster = new ProductVarietyMaster();
		customerBillingDTO = new CustomerBillingDTO();
		customerBillingDTO.setBarcodebool(false);
		customerBillingDTO.setProductVarietyMaster(new ProductVarietyMaster());
		customerBillingDTOList.add(customerBillingDTO);
		billDate = new Date();
		bill_H = new Bill_H();
		bill_D = new Bill_D();
		customerName = null;
		mobileNumber = null;
		customerBillingType = "WITH_CUSTOMER";
		return BILLING_CB_PAGE;

	}

	/**
	 * This Method Used To Clear Talbe Values
	 */
	public void clear() {
		customerMasterlist = new ArrayList<>();
		customerMaster = new CustomerMaster();
		selectedcustomerMaster = new CustomerMaster();
	}

	/**
	 * This Method Used To Load Customer Information Based On Mobile Search
	 * 
	 * @param mobileNo
	 * @return
	 */
	public List<CustomerMaster> mobileAutocomplete(String mobileNo) {

		log.info(":::<- Load mobileAutocomplete TypeStart ->::: ");
		BaseDTO baseDTO = null;
		try {
			if (mobileNo.trim() != null && !mobileNo.isEmpty()) {
				mobileNumber = mobileNo;
				String url = SERVER_URL + "/customerbillingcontroller/autocompletemobile/" + mobileNo;
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

	public void printData(BaseDTO baseDTO) throws IOException {
		log.info("CustomerBillingBean PrintDate Started");
		customerBillingDTO = new CustomerBillingDTO();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YY");
		DateFormat timeformat = new SimpleDateFormat("HH:mm");

		String Date = sdf.format(new Date());
		String Time = timeformat.format(new Date());

		if (baseDTO != null && baseDTO.getResponseContent() != null && baseDTO.getStatusCode() == 0) {
			ObjectMapper mapper = new ObjectMapper();
			String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContent());
			customerBillingDTO = mapper.readValue(jsonResponse, CustomerBillingDTO.class);
		}
		List<CustomerBillingDTO> cst = customerBillingDTO.getCustomerBillingDTOList();

		String printData = "'<center><b>" + customerBillingDTO.getStoreName()
				+ "</b></center> nextLine <center> BillNo: " + customerBillingDTO.getBill_H_BillNo() + " nextLine D: "
				+ Date + " T:" + Time + "  </center> ---------------------------------- "
				+ "nextLine <b> Desc. &nbsp; | Qty | Rate | Amt </b> nextLine ---------------------------------- nextLine "
				+ printItemName(cst) + " ---------------------------------- nextLine <b> total Qnty=" + TotalQty(cst)
				+ " nextLine TotalAmount = " + customerBillingDTO.getTotalRate1() + " nextLine " + "Total GST Amount="
				+ customerBillingDTO.getTotalGst() + " nextLine Discount Amount = "
				+ cst.stream().mapToDouble(x -> x.getBill_D_DiscountValue()).sum() + " nextLine TotalNet Amount = "
				+ customerBillingDTO.getNetAmount1()
				+ "</b> nextLine ---------------------------------- nextLine <small> THANK YOU VISIT AGAIN </small> ';";

		content = printData;
		log.info("Print " + content);
		/*
		 * this is unuse code for bill print.........
		 * 
		 * String shopName = "" + customerBillingDTO.getStoreName() + "\n"; String line
		 * = "-------------------------------\n"; String dt = "D: " + Date + "  T:" +
		 * Time + "\n"; String head = "Desc.   | Qyt | Rate  | Amt\n"; // 8,5,8,7 String
		 * items = "" + printItemName(cst) + ""; String amt = "Total Qyt=" +
		 * TotalQty(cst) + "\nTotal Amount=" + customerBillingDTO.getTotalRate1() +
		 * "\nTotal GST Amount=" + customerBillingDTO.getTotalGst() +
		 * "\nTotal Net Amount=" + customerBillingDTO.getNetAmount1() + "\n";
		 * 
		 * String end = "THANK YOU--VISIT AGAIN \n\n\n\n\n\n"; String bln = "BillNo: " +
		 * customerBillingDTO.getBill_H_BillNo() + "\n";
		 * log.info("Bill Input String Store Name  :: "+shopName+
		 * "And BillgNo :: "+items);
		 * 
		 * 
		 * PrintRequestAttributeSet patts = new HashPrintRequestAttributeSet();
		 * patts.add(Sides.DUPLEX); //PrintService[] PrintServices =
		 * PrintServiceLookup.lookupPrintServices(null, null);
		 * 
		 * PrintService PrintService = PrintServiceLookup.lookupDefaultPrintService();
		 * PrintService mysrvice = null;
		 * 
		 * if (PrintService!=null) { mysrvice = PrintService; }
		 * 
		 * for (PrintService printService : PrintServices) {
		 * System.out.println(printService.getName()); if
		 * (printService.getName().equals("Everycom-58-Series")) { mysrvice =
		 * printService; break; } } if (mysrvice == null) {
		 * log.error("Printer Not Availble To Print"); return; }
		 * log.info("Available Printers Is :: "+mysrvice.getName());
		 * ByteArrayOutputStream expected = new ByteArrayOutputStream();
		 * 
		 * expected.write(POS.POSPrinter.Justification(POS.Justifications.Center));
		 * expected.write(POS.POSPrinter.CharSize.DoubleHeight3());
		 * expected.write(shopName.getBytes());
		 * expected.write(POS.POSPrinter.CharSize.Normal());
		 * expected.write(line.getBytes()); expected.write(bln.getBytes());
		 * expected.write(dt.getBytes());
		 * expected.write(POS.POSPrinter.Justification(POS.Justifications.Left));
		 * expected.write(POS.POSPrinter.SetStyles(POS.PrintStyle.Bold));
		 * expected.write(head.getBytes());
		 * expected.write(POS.POSPrinter.SetStyles(POS.PrintStyle.None));
		 * expected.write(line.getBytes()); expected.write(items.getBytes());
		 * expected.write(line.getBytes());
		 * expected.write(POS.POSPrinter.SetStyles(POS.PrintStyle.Bold));
		 * expected.write(amt.getBytes());
		 * expected.write(POS.POSPrinter.SetStyles(POS.PrintStyle.None));
		 * expected.write(POS.POSPrinter.Justification(POS.Justifications.Center));
		 * expected.write(line.getBytes());
		 * expected.write(POS.POSPrinter.CharSize.DoubleHeight2());
		 * expected.write(end.getBytes());
		 * expected.write(POS.POSPrinter.CharSize.Normal());
		 * 
		 * DocPrintJob job = mysrvice.createPrintJob(); Doc doc = new
		 * SimpleDoc(expected.toByteArray(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
		 * 
		 * try { log.info(" >> Before Printing >>> "); job.print(doc, new
		 * HashPrintRequestAttributeSet()); log.info(" >> After Printing >>> "); } catch
		 * (PrintException e) {
		 * log.error(" >> Exception Occured on CustomerBilling >> printData "+e); }
		 */
	}

	private Double TotalQty(List<CustomerBillingDTO> cst) {

		Double l = 0.0;
		double totalrate = 0.0;
		double sgst = 0.0;
		double cgst = 0.0;
		double netamount;
		DecimalFormat df2 = new DecimalFormat("#.##");

		if (cst != null) {
			for (CustomerBillingDTO dto : cst) {
				if (dto.getItemId() == null) {
					break;
				}
				Double Qnt = dto.getBilledQnty();
				l = l + Qnt;

				double tnp = dto.getTotalRate();
				totalrate = totalrate + tnp;
				if (dto.getSgstAmount() == null) {
					dto.setSgstAmount(0D);
				}
				double sgctamt = dto.getSgstAmount();
				sgst = sgst + sgctamt;
				if (dto.getCgstAmount() == null) {
					dto.setCgstAmount(0D);
				}
				double cgctamt = dto.getCgstAmount();
				cgst = cgst + cgctamt;
			}
			netamount = totalrate + sgst + cgst;

			customerBillingDTO.setTotalRate1(Double.parseDouble(df2.format(totalrate).toString()));
			customerBillingDTO.setTotalGst(Double.parseDouble(df2.format(sgst + cgst).toString()));
			customerBillingDTO.setNetAmount1(Double.parseDouble(df2.format(netamount).toString()));
		}
		return l;
	}

	private String TotalAmt(List<CustomerBillingDTO> cst) {
		double d = 0.0;
		if (cst != null) {

			for (CustomerBillingDTO dto : cst) {
				double tnp = dto.getTotalRate();
				d = d + tnp;

			}
		}
		String s = "" + d;
		if (s.length() > 10) {
			s = s.substring(0, 10) + "";
			// s=s.

		}
		return s;
	}

	private String printItemName(List<CustomerBillingDTO> cst) {

		String input = "";
		if (cst != null) {

			for (CustomerBillingDTO dto : cst) {
				String name = "" + dto.getItemCodeName();
				String qnty = " " + dto.getBilledQnty();
				String urt = " " + dto.getUnitRate();
				String tnp = " " + dto.getTotalRate();

				if (name.length() >= 8) {
					name = name.substring(0, 8) + "";

				} else {
					int i = name.length();
					if (i < 8) {

						for (int j = i; j < 8; j++)
							name = name + " &nbsp;";
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
							urt = urt + " &nbsp; ";
					}
				}
				if (tnp.length() > 7) {
					tnp = tnp.substring(0, 7) + "";
				} else {
					int i = tnp.length();
					for (int j = i; j < 7; j++)
						tnp = tnp + " &nbsp; ";
				}

				String item = name + "|" + qnty + "|" + urt + "|" + tnp + " nextLine ";
				input = input + item;
			}
			// input = input + "\n";

		}
		return input;
	}

	/**
	 * This Method Used To Save Billing Information And used for Print Bill Receipt
	 */
	public void billPrint() {

		log.info(":::<- Load billPrint Start ->::: ");
		BaseDTO baseDTO = null;
		try {
			content = "''";
			if (customerBillingDTOList != null && customerBillingDTOList.size() > 0) {
				if (appendCustomerNameAndMob()) {

					String url = SERVER_URL + "/customerbillingcontroller/billadd";
					log.info("::: billPrint Controller calling  :::");
					baseDTO = httpService.post(url, customerBillingDTO);
					log.info("::: get itemAutocomplete Response :");
					if (baseDTO.getStatusCode() == 0) {
						printData(baseDTO);
						AppUtil.addInfo("Bill Printed Successfully (Bill No:" + baseDTO.getGeneralContent() + ")");
						showBillingPage();
					} else {
						if (baseDTO.getMessage() != null) {
							log.warn(baseDTO.getMessage());
						} else {
							log.warn("billPrint Error ");
						}
					}
				}
			} else {
				AppUtil.addInfo("Please Add Item");
			}
		} catch (Exception ee) {
			log.error("Exception Occured in billPrint load ", ee);
		}

	}

	private boolean appendCustomerNameAndMob() {
		boolean valid = true;

		customerBillingDTOList.get(0).setCustomerBillingType(customerBillingType);
		if (customerBillingType.equalsIgnoreCase("WITH_CUSTOMER")) {
			if (customerName == null || customerName.isEmpty()) {
				AppUtil.addWarn("Customer Name And Mobile Number Is mandatory");
				return false;
			}
		}
		CustomerBillingDTO dto = customerBillingDTOList.get(customerBillingDTOList.size() - 1);
		if (dto.getItemCodeName() != null && !(dto.getItemCodeName().isEmpty())) {
			Double billedQnty = customerBillingDTOList.get(customerBillingDTOList.size() - 1).getBilledQnty();
			if (billedQnty == null) {
				AppUtil.addWarn("Enter Billed Qnty");
				return false;
			}
		}

		if (reciveamnt > netamt) {
			AppUtil.addWarn("Enter Exact Amount");
			return false;
		} else {
			if (reciveamnt < netamt) {
				if (!customerBillingType.equalsIgnoreCase("WITH_CUSTOMER")) {
					AppUtil.addWarn("Please Print Bill With Saved Custmer");
					return false;
				}
			}
		}

		customerBillingDTO = new CustomerBillingDTO();
		customerBillingDTO.setReciveAmount(reciveamnt);
		customerBillingDTO.setTotalNetPrice(netamt);
		customerBillingDTO.setPaymentmode(paymentmode);
		customerBillingDTO.setBillingDtoList(customerBillingDTOList);

		if (selectedcustomerMaster == null) {
			selectedcustomerMaster = new CustomerMaster();
			selectedcustomerMaster.setName(customerName);
			selectedcustomerMaster.setPrimaryContactNumber(mobileNumber);
		} else {
			selectedcustomerMaster.setName(customerName);
		}
		customerBillingDTOList.get(0).setCustomerMaster(selectedcustomerMaster);
		return valid;
	}

	/**
	 * This Method Used To Remove Added Item In Table For Billing
	 * 
	 * @param customerBillingDto
	 */
	public void removeItem(CustomerBillingDTO customerBillingDto) {

		if (customerBillingDto != null) {
			DecimalFormat df = new DecimalFormat("0.00");

			customerBillingDTOList.remove(customerBillingDto);
			Double n = netamt - customerBillingDto.getTotalNetPrice();
			totalNetAmount = df.format(n);
			netamt = Double.valueOf(totalNetAmount);
			reciveamnt = Double.valueOf(totalNetAmount);
		}

		// DecimalFormat df = new DecimalFormat("0.00");
		// totalNetAmount = df.format(totalNetAmt);
		// netamt=totalNetAmt;

	}

	public List<ProductVarietyMaster> itemAutoSearch(String itemName) {

		log.info(":::<- Load itemAutocomplete TypeStart ->::: ");
		BaseDTO baseDTO = null;
		try {
			if (itemName.trim() != null && !itemName.isEmpty()) {
				String url = SERVER_URL + "/customerbillingcontroller/autocompleteitemName/" + itemName + "/"
						+ barcodeType + "/" + false;
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

	public void setValuesToTableList(CustomerBillingDTO customerbillingDto) {
		customerbillingDto.setItemId(productVarietyMasterList.get(0).getId());
		customerbillingDto.setItemCodeName(productVarietyMasterList.get(0).getName());
		customerbillingDto.setCgstPercent(productVarietyMasterList.get(0).getCgst_percentage());
		customerbillingDto.setSgstPercent(productVarietyMasterList.get(0).getSgst_percentage());
		customerbillingDto.setHsnCode(productVarietyMasterList.get(0).getHsnCode());

		customerbillingDto.setUnitRate(productVarietyMasterList.get(0).getCustomerBillingDTO().getSellingPrice());
		customerbillingDto.setRateQTY(productVarietyMasterList.get(0).getCustomerBillingDTO().getRateQTY());
		customerbillingDto.setItemPricePk(productVarietyMasterList.get(0).getCustomerBillingDTO().getItemPricePk());
		customerbillingDto
				.setList_expiryDate(productVarietyMasterList.get(0).getCustomerBillingDTO().getList_expiryDate());
		customerbillingDto
				.setBill_D_PurchasePrice(productVarietyMasterList.get(0).getCustomerBillingDTO().getPurchasePrice());
		customerbillingDto.setBarcode(productVarietyMasterList.get(0).getBarcodenumber());
		customerbillingDto.setUomName(productVarietyMasterList.get(0).getUomname());
		if (productVarietyMasterList.get(0).getDiseabledItem()) {
			AppUtil.addWarn("No Stock Available");
		}
		expire = customerbillingDto.getList_expiryDate();
		itemPk = customerbillingDto.getItemId();

	}

	Long itemPk;

	/**
	 * This Method Used For Select Item Select
	 * 
	 * @param customerbillingDto
	 */
	public void creatRow() {
		CustomerBillingDTO dto = new CustomerBillingDTO();
		dto.setProductVarietyMaster(new ProductVarietyMaster());
		if (barcodeType.equalsIgnoreCase("withoutbarcode")) {
			dto.setBarcodebool(false);
		}
		customerBillingDTOList.add(dto);

	}

	public void getItemBasedOnBarCode(CustomerBillingDTO customerbillingDto, int index) {
		try {
			String url = SERVER_URL + "/customerbillingcontroller/autocompleteitemName/"
					+ customerbillingDto.getItemCodeName() + "/" + "withbarcode" + "/" + true;
			log.info("::: loadProductVarietyDetails Controller calling  1 :::");

			BaseDTO baseDTO = httpService.get(url);
			log.info("::: get itemAutocomplete Response :");
			if (baseDTO.getStatusCode() == 0) {
				ObjectMapper mapper = new ObjectMapper();
				productVarietyMasterList = new ArrayList<ProductVarietyMaster>();
				String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
				productVarietyMasterList = mapper.readValue(jsonResponse,
						new TypeReference<List<ProductVarietyMaster>>() {
						});
				if (productVarietyMasterList != null && productVarietyMasterList.size() > 0) {
					customerbillingDto.setBarcodebool(true);
					setValuesToTableList(customerbillingDto);
					// customerBillingDTOList.set(index, customerbillingDto);
					if (customerBillingDTOList.get(customerBillingDTOList.size() - 1).getItemPricePk() != null) {
						creatRow();
					}
				} else {
					AppUtil.addWarn("BarCode not Availble In Store");
					customerbillingDto.setItemCodeName("");
				}

			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void ajaxItemSelect(CustomerBillingDTO customerbillingDto) {
		if (customerbillingDto.getProductVarietyMaster() != null
				&& customerbillingDto.getProductVarietyMaster().getId() != null) {
			ProductVarietyMaster pvm = customerbillingDto.getProductVarietyMaster();
			customerbillingDto.setItemId(pvm.getId());
			customerbillingDto.setItemCodeName(pvm.getName());
			customerbillingDto.setCgstPercent(pvm.getCgst_percentage());
			customerbillingDto.setSgstPercent(pvm.getSgst_percentage());
			customerbillingDto.setHsnCode(pvm.getHsnCode());

			// newly added
			// dto.setStockQnty(customerDTO.getStockQnty());
			customerbillingDto.setUnitRate(pvm.getCustomerBillingDTO().getSellingPrice());
			customerbillingDto.setRateQTY(pvm.getCustomerBillingDTO().getRateQTY());
			customerbillingDto.setItemPricePk(pvm.getCustomerBillingDTO().getItemPricePk());
			customerbillingDto.setList_expiryDate(pvm.getCustomerBillingDTO().getList_expiryDate());
			customerbillingDto.setBill_D_PurchasePrice(pvm.getCustomerBillingDTO().getPurchasePrice());
			customerbillingDto.setBarcode(pvm.getBarcodenumber());
			customerbillingDto.setUomName(pvm.getUomname());
			if (pvm.getDiseabledItem()) {
				AppUtil.addWarn("No Stock Available");
			}
			expire = customerbillingDto.getList_expiryDate();
			itemPk = customerbillingDto.getItemId();
		}

	}

	// public void updatediscountvalue(CustomerBillingDTO customerdto) {
	//
	// if(customerdto.getBill_D_DiscountValue()!=null && customerdto.getTotalRate()
	// != null) {
	//
	//
	// customerdto.setDiscountPercent(
	// customerdto.getBill_D_DiscountValue() * 100 / customerdto.getTotalRate());
	// }
	//
	// }
	
	public void updatediscountper(CustomerBillingDTO customerdto) {
		if(customerdto!=null && customerdto.getDiscountPercent()!=null) {
			Double discountAmount = customerdto.getTotalRate() * customerdto.getDiscountPercent() / 100;
			customerdto.setBill_D_DiscountValue(discountAmount);
			updateUnitRateWithGST( customerdto);
		} else {
			customerdto.setBill_D_DiscountValue(0D);
			updateUnitRateWithGST( customerdto);
		}
	}
	
	public void updatediscountval(CustomerBillingDTO customerdto) {
		if(customerdto!=null && customerdto.getBill_D_DiscountValue()!=null) {
			customerdto.setDiscountPercent(customerdto.getBill_D_DiscountValue() * 100 / customerdto.getTotalRate());
			updateUnitRateWithGST( customerdto);
		} else {
			customerdto.setDiscountPercent(0D);
			updateUnitRateWithGST(customerdto);
		}
	}

	public void updateUnitRateWithGST(CustomerBillingDTO customerdto) {

		if (customerdto.getCgstPercent() == null) {
			customerdto.setSgstAmount(0d);
			customerdto.setSgstPercent(0d);
		}
		if (customerdto.getSgstPercent() == null) {
			customerdto.setCgstAmount(0d);
			customerdto.setCgstPercent(0d);
		}

		if (customerdto.getTotalgstamount() == null) {
			customerdto.setTotalgstamount(0d);
		}
	   Double discountTotalRate=0D;

		if (customerdto.getBilledQnty() != null && customerdto.getBilledQnty() > 0 && customerdto.getUnitRate() != null
				&& customerdto.getUnitRate() > 0) {
			if (customerdto.getRateQTY() < customerdto.getBilledQnty()) {
				AppUtil.addWarn(" Billed Qnty Is More Than Stock Qnty ");
				customerdto.setBilledQnty(0.0);
				return;
			}

			customerdto.setTotalRate(customerdto.getUnitRate() * customerdto.getBilledQnty());
			discountTotalRate = customerdto.getUnitRate() * customerdto.getBilledQnty() - customerdto.getBill_D_DiscountValue();
		}

		if (customerdto.getTotalRate() != null && customerdto.getTotalRate() > 0) {

			if (customerdto.getCgstPercent() != null && customerdto.getCgstPercent() > 0) {

				customerdto.setCgstAmount(
						Double.valueOf(df.format(discountTotalRate * customerdto.getCgstPercent() / 100)));
			} else {

				customerdto.setCgstAmount(0.0);
			}

			if (customerdto.getSgstPercent() != null && customerdto.getSgstPercent() > 0) {

				customerdto.setSgstAmount(
						Double.valueOf(df.format(discountTotalRate * customerdto.getSgstPercent() / 100)));
			} else {
				customerdto.setSgstAmount(0.0);
			}

		}

		if (customerdto.getUnitRate() != null) {
			if (customerdto.getTotalRate() == null) {
				customerdto.setTotalRate(0D);
			}

			if (customerdto.getCgstAmount() > 0 || customerdto.getSgstAmount() > 0) {
				customerdto.setTotalgstamount(customerdto.getCgstAmount() + customerdto.getSgstAmount());
			} else {
				customerdto.setTotalgstamount(0.0);
			}

			if (customerdto.getTotalRate() != null && customerdto.getSgstAmount() != null
					&& customerdto.getCgstAmount() != null) {
				customerdto.setTotalNetPrice(Double.parseDouble(df.format(discountTotalRate + customerdto.getTotalgstamount())));
			}

			if (customerdto.getTotalNetPrice() != null) {
				Double totalNetAmt = customerBillingDTOList.stream()
						.mapToDouble(x -> x.getTotalNetPrice() != null ? x.getTotalNetPrice() : 0.0D).sum();
				totalNetAmount = df.format(totalNetAmt);
				netamt = totalNetAmt;
				reciveamnt = netamt;
			}
		}

	}


	public void updateBarCodeRadio() {
		if (barcodeType.equalsIgnoreCase("withoutbarcode")) {
			customerBillingDTOList.get(customerBillingDTOList.size() - 1).setBarcodebool(false);
		} else {
			customerBillingDTOList.get(customerBillingDTOList.size() - 1).setBarcodebool(true);
		}
	}

	public void updateMobileNumber() {

		if (selectedcustomerMaster != null && selectedcustomerMaster.getId() != null) {
			customerName = selectedcustomerMaster.getName();
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
