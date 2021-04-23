package co.orffosoft.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import co.orffosoft.core.util.AppPreference;
import co.orffosoft.core.util.AppUtil;
import co.orffosoft.core.util.RestException;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.InitialStockUploadDTO;
import co.orffosoft.enums.TemplateCode;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("initialStockUploadBean")
@Scope("session")
public class InitialStockUploadBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8267829780984422492L;

	private final String STOCKUPLOAD_PAGE = "/pages/billing/initialStockUpload.xhtml?faces-redirect=true;";

	@Getter
	@Setter
	String fileName;

	@Getter
	@Setter
	Date stockUploadDate = null;

	@Getter
	@Setter
	InitialStockUploadDTO initialStockUploadDTO = new InitialStockUploadDTO();

	@Getter
	@Setter
	List<InitialStockUploadDTO> initialStockUploadDTOList = new ArrayList<>();

	@Autowired
	LoginBean loginBean;

	@Getter
	@Setter
	FileOutputStream fos;

	@Getter
	@Setter
	byte[] downloadName = null;

	@Getter
	@Setter
	StreamedContent file;

	@Getter
	@Setter
	private UploadedFile uploadedFile;

	@Getter
	@Setter
	File files = null;
	
//	 @Getter
//	 @Setter
//	 String filePathName ="/Data/PROGRAMS/ORFFOSOFT_RETAIL/INITIAL_STOCK/DOWNLOAD/InitialStockUpload.xls";
//
//	 @Getter
//	 @Setter
//	 String uploadFilePath ="/Data/PROGRAMS/ORFFOSOFT_RETAIL/INITIAL_STOCK/UPLOADED";

//	@Getter
//	@Setter
//	String filePathName = "D:/InitialStockUpload.xlsx";

//	 @Getter
//	 @Setter
//	 String uploadFilePath ="D:/Development_Practice/file_upload_path/uploaded/excel";

	 @Getter
	 @Setter
	 String filePathName = "E:/InitialStockUpload.xlsx";
	
	 @Getter
	 @Setter
	 String uploadFilePath = "E:/";

	public static final String SERVER_URL = AppUtil.getPortalServerURL();

	@Autowired
	HttpService httpService;

	@Autowired
	AppPreference appPreference;

	public String showListPage() {
		stockUploadDate = new Date();
		initialStockUploadDTO = new InitialStockUploadDTO();
		initialStockUploadDTOList = new ArrayList<>();
		return STOCKUPLOAD_PAGE;

	}

	/**
	 * This Method will use to create upload Stock
	 * 
	 * @param files
	 */
	@SuppressWarnings("unlikely-arg-type")
	public void uploadInitialStock(FileUploadEvent event) {
		initialStockUploadDTOList = new ArrayList<>();
		List<String>itemNames = new ArrayList<>();
		List<Long>itemCode = new ArrayList<>();
		int cellCount = 1;
		int rowCount = 0;
		log.info(":::<- uploadInitialStock Start ->::: ");
		try {
			InputStream excelFile = null;
			if (event == null || event.getFile() == null) {
				log.error(" Upload document is null ");
				AppUtil.addError("Uploaded Document Is Empty ");
				return;
			}
			uploadedFile = event.getFile();
			long size = uploadedFile.getSize();
			log.info(" size is : " + size);
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh-mm-ss");
			String fileName = loginBean.getEntityMaster().getCode() +"-"+ sdf.format(new Date()) +"-"+ event.getFile().getFileName();
			
			boolean fileCopied = AppUtil.copyFile(uploadedFile.getInputstream(), fileName, uploadFilePath);
			if (!fileCopied) {
				log.info("Initial Stock Upload File not copied.");
				throw new RestException("Initial Stock Upload File not copied.");
			}
			String absoluteFilePathForStockUpdate = uploadFilePath + "/" + fileName;
			File fileForUpdateStock = new File(absoluteFilePathForStockUpdate);
			excelFile = new FileInputStream(fileForUpdateStock);
			Workbook workbook = new XSSFWorkbook(excelFile);
			Sheet sheet = workbook.getSheetAt(0);
			Row row = null;
			boolean exit = false;
			for (Row rows : sheet) {
				if (exit) {
					return;
				}
				for (Cell cellValue : rows) {
					initialStockUploadDTO = new InitialStockUploadDTO();
					++rowCount;
					row = sheet.getRow(rowCount);
					
					if(row!=null) {
						cellCount = 0;
						cellValue = row.getCell(cellCount);
					}
					if (cellValue == null || row == null || cellValue.getStringCellValue().isEmpty()){
						updateStock(initialStockUploadDTOList);
						exit = true;
						return;
					}
					
					initialStockUploadDTO.setItemName(cellValue.getStringCellValue());
					

					cellValue = row.getCell(++cellCount);
					if (cellValue == null) {
						throw new IllegalStateException();
					}
					initialStockUploadDTO.setItemCode((long) cellValue.getNumericCellValue());

					cellValue = row.getCell(++cellCount);
					if (cellValue == null) {
						throw new IllegalStateException();
					}
					initialStockUploadDTO.setQuantity( cellValue.getNumericCellValue());
					cellValue = row.getCell(++cellCount);
					if (cellValue == null) {
						initialStockUploadDTO.setCgstPercentage(0d);
					} else {
						initialStockUploadDTO.setCgstPercentage((Double) cellValue.getNumericCellValue());
					}
					

					cellValue = row.getCell(++cellCount);
					if (cellValue == null) {
						initialStockUploadDTO.setSgstPercentage(0d);
					} else {
						initialStockUploadDTO.setSgstPercentage((Double) cellValue.getNumericCellValue());
					}

					cellValue = row.getCell(++cellCount);
					if (cellValue == null) {
						initialStockUploadDTO.setHsncode(0L);
					} else {
						initialStockUploadDTO.setHsncode((long) cellValue.getNumericCellValue());
					}

					cellValue = row.getCell(++cellCount);
					if (cellValue == null) {
						throw new IllegalStateException();
					}
					initialStockUploadDTO.setPurchaseAmout(cellValue.getNumericCellValue());

					cellValue = row.getCell(++cellCount);
					if (cellValue == null) {
						throw new IllegalStateException();
					}
					initialStockUploadDTO.setSellingAmount(cellValue.getNumericCellValue());
					initialStockUploadDTO.setStatus(TemplateCode.Active.toString());
				
//					initialStockUploadDTO.setTotalPurchaseAmt(
//							(initialStockUploadDTO.getQuantity() * (initialStockUploadDTO.getPurchaseAmout()
//									+ initialStockUploadDTO.getCgstAmount() + initialStockUploadDTO.getSgstAmount())));
					
					initialStockUploadDTO.setTotalPurchaseAmt(
							initialStockUploadDTO.getQuantity() * initialStockUploadDTO.getPurchaseAmout());
					
					if (initialStockUploadDTO.getCgstPercentage() != null) {
						initialStockUploadDTO.setCgstAmount((initialStockUploadDTO.getTotalPurchaseAmt() * initialStockUploadDTO.getCgstPercentage())/100);
					}
					if (initialStockUploadDTO.getCgstAmount() == null) {
						initialStockUploadDTO.setCgstAmount(0D);
					}

					if (initialStockUploadDTO.getSgstPercentage() != null) {
						initialStockUploadDTO.setSgstAmount((initialStockUploadDTO.getTotalPurchaseAmt() * initialStockUploadDTO.getSgstPercentage())/100);
					}
					if (initialStockUploadDTO.getSgstAmount() == null) {
						initialStockUploadDTO.setSgstAmount(0D);
					}
					
					initialStockUploadDTO.setTotalSellingAmt(
							(initialStockUploadDTO.getQuantity() * (initialStockUploadDTO.getSellingAmount()
									+ initialStockUploadDTO.getCgstAmount() + initialStockUploadDTO.getSgstAmount())));
					// initialStockUploadDTO.setCgstAmount(
					// (initialStockUploadDTO.getTotalPurchaseAmt() *
					// initialStockUploadDTO.getCgstPercentage())
					// / 100);
					// initialStockUploadDTO.setSgstAmount(
					// (initialStockUploadDTO.getTotalPurchaseAmt() *
					// initialStockUploadDTO.getSgstPercentage())
					// / 100);

					initialStockUploadDTO.setNetAmount((initialStockUploadDTO.getTotalPurchaseAmt()
							- initialStockUploadDTO.getDiscountAmount())
							+ (initialStockUploadDTO.getCgstAmount() + initialStockUploadDTO.getSgstAmount()));
					
					String itemName = initialStockUploadDTO.getItemName();
					if(itemNames.contains(itemName)) {
						throw new IllegalStateException("Item Name Duplicate");
						
					}
					if(itemCode.contains(initialStockUploadDTO.getItemCode())){
						throw new IllegalStateException("Item Code Duplicate");
						
					}
					itemCode.add(initialStockUploadDTO.getItemCode());
					itemNames.add(initialStockUploadDTO.getItemName());
					initialStockUploadDTOList.add(initialStockUploadDTO);

				}
			}

		} catch (IllegalStateException ie) {
			String errorMsg = handelException(cellCount, rowCount, ie.getMessage());
			AppUtil.addWarn(errorMsg + " Please Correct And Reupload");
			log.error(" IllegalStateException Occured On InitialStockUploadBean === "+errorMsg);
			log.error(" IllegalStateException Occured On InitialStockUploadBean === "+ie);
		} catch (Exception e) {
			log.error("Exception Occured on uploadSheet ==== " + e);
		}

	}

	private String handelException(int validateColumnData, int validateRowData, String errormsg) {
		String msg = "";

		if (validateColumnData == 0) {
			msg = "Internal Error";
		}else if (errormsg.equalsIgnoreCase("Item Code Duplicate")) {
			msg = "Item Code Duplicate On Row " + (++validateRowData);
		}
		else  if (errormsg.equalsIgnoreCase("Item Name Duplicate")) {
			msg = "Item Name Duplicate On Row \n" + (++validateRowData);
		}  else  if (validateColumnData == 1) {
				msg = "Item Name Not Valid On Row Number " + (++validateRowData);
			} else if (validateColumnData == 2) {
				msg = "Item Code Not Valid On Row Number " + (++validateRowData);
			} else if (validateColumnData == 3) {
				msg = "Qnty Not Valid On Row Number >> " + (++validateRowData);
			} else if (validateColumnData == 4) {
				msg = "CGST Not Valid On Row Number " + (++validateRowData);
			} else if (validateColumnData == 5) {
				msg = "SGST Not Valid On Row Number " + (++validateRowData);
			} else if (validateColumnData == 6) {
				msg = "HSN Code Not Valid On Row Number " + (++validateRowData);
			} else if (validateColumnData == 7) {
				msg = "Purchase Amount Not Valid On Row Number " + (++validateRowData);
			} else {
				if (validateColumnData == 8)
					msg = "Selling Amount Not Valid On Row Number " + (++validateRowData);
			}
			 
		

		return msg;

	}


	private void updateStock(List<InitialStockUploadDTO> dtoList) {
		if (dtoList != null && dtoList.size() > 0) {
			String url = SERVER_URL + "/initialStockUploadController/saveinitialstockupload";
			log.info("::: updateStock Method In InitialStockUpload:::");
			BaseDTO baseDTO = httpService.post(url, dtoList);
			if (baseDTO.getStatusCode() == 1) {
				
				AppUtil.addWarn(" Stock Uploaded Failed :: " +baseDTO.getMessage());
			} else {
				AppUtil.addInfo("Stock Has Been Successfully Uploaded");
			}
		} else {
			AppUtil.addInfo("Please Add Item");
		}

	}

	/**
	 * This method is used to .
	 *
	 */
	public void getDownloadFile() {
		InputStream input = null;
		try {
			files = new File(filePathName);
			Path path = Paths.get(filePathName);
			if (Files.notExists(path)) {
				Files.createDirectories(path);
			}
			input = new FileInputStream(files);
			if (input != null) {
				FacesContext context = FacesContext.getCurrentInstance();
				ExternalContext externalContext = context.getExternalContext();
				file = (new DefaultStreamedContent(input, externalContext.getMimeType(files.getName()),
						files.getName()));
			}
		} catch (Exception ex) {
			log.error(" Error in while getting getDownloadFile", ex);
			AppUtil.addError("Download Error");
		}
	}

	
}
