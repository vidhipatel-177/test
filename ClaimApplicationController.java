package com.sttl.hrms.claimMatrix.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import com.mchange.v2.cfg.PropertiesConfigSource.Parse;
import com.sttl.hrms.HR.empmgmt.empinfo.model.*;
import com.sttl.hrms.HR.empmgmt.empinfo.repository.*;
import com.sttl.hrms.bean.*;
import com.sttl.hrms.claimMatrix.model.*;
import com.sttl.hrms.claimMatrix.repository.*;
import com.sttl.hrms.masters.model.GradeMaster;
import com.sttl.hrms.masters.repository.GradeMasterRepository;
import com.sttl.hrms.masters.repository.HolidayConfigurationMasterDTLRepository;
import com.sttl.hrms.payroll.repository.ElementOfPaySystemMasterRepository;
import com.sttl.hrms.payroll.repository.EmployeeProvidentFundDtlRepo;
import com.sttl.hrms.tour.repository.SanctionToTourRepository;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sttl.hrms.HR.empmgmt.empinfo.model.EmpFamilyDtl;
import com.sttl.hrms.HR.empmgmt.empinfo.model.EmpReportingOfficer;
import com.sttl.hrms.HR.empmgmt.empinfo.model.EmpSalaryDtl;
import com.sttl.hrms.HR.empmgmt.empinfo.model.Employee;
import com.sttl.hrms.HR.empmgmt.empinfo.repository.EmpFamilyRepository;
import com.sttl.hrms.HR.empmgmt.empinfo.repository.EmpReportingOfficerRepository;
import com.sttl.hrms.HR.empmgmt.empinfo.repository.EmpSalaryDtlRepository;
import com.sttl.hrms.HR.empmgmt.empinfo.repository.EmployeeRepository;

import com.sttl.hrms.claimMatrix.enums.ExpenseType;
import com.sttl.hrms.claimMatrix.enums.FuelType;

import com.sttl.hrms.claimMatrix.model.ClaimApplication;
import com.sttl.hrms.claimMatrix.model.ClaimApplicationExpenseDetails;
import com.sttl.hrms.claimMatrix.model.ClaimApplicationExpenseDetailstmp;
import com.sttl.hrms.claimMatrix.model.ClaimApprovedSum;
import com.sttl.hrms.claimMatrix.model.ClaimCategoryModel;
import com.sttl.hrms.claimMatrix.model.ClaimConfiguration;
import com.sttl.hrms.claimMatrix.model.ClaimItemConfiguration;
import com.sttl.hrms.claimMatrix.model.FurnitureItems;
import com.sttl.hrms.claimMatrix.model.ItemMasterForClaimMatrix;
import com.sttl.hrms.claimMatrix.model.VehicleRunningMaintenanceExpense;
import com.sttl.hrms.claimMatrix.repository.ClaimApplicationExpenseDetailsRepository;
import com.sttl.hrms.claimMatrix.repository.ClaimApplicationExpenseDetailsTmpRepository;
import com.sttl.hrms.claimMatrix.repository.ClaimApplicationRepository;
import com.sttl.hrms.claimMatrix.repository.ClaimCategoryMasterRepository;
import com.sttl.hrms.claimMatrix.repository.ClaimConfigurationRepository;
import com.sttl.hrms.claimMatrix.repository.ClaimItemConfigurationRepository;
import com.sttl.hrms.claimMatrix.repository.FurnitureItemsRepository;
import com.sttl.hrms.claimMatrix.repository.ItemMasterRepositoryForClaimMatrix;
import com.sttl.hrms.claimMatrix.repository.VehicleRunningMaintenanceExpenseRepository;
import com.sttl.hrms.claimMatrix.service.ClaimApplicationService;
import com.sttl.hrms.claimMatrix.service.ClaimConfigurationService;
import com.sttl.hrms.leaveattendance.model.LeaveManualAttendanceMaster;
import com.sttl.hrms.leaveattendance.repository.LeaveManualAttendanceMasterRepository;
import com.sttl.hrms.login.repository.UserMasterRepository;
import com.sttl.hrms.masters.model.RoleMaster;
import com.sttl.hrms.masters.model.UserMaster;
import com.sttl.hrms.masters.repository.CompanyBranchMasterRepository;
import com.sttl.hrms.masters.repository.CompanyMasterRepository;
import com.sttl.hrms.masters.repository.RoleMasterRepository;
import com.sttl.hrms.model.CompanyBranchMaster;
import com.sttl.hrms.model.CompanyMaster;
import com.sttl.hrms.model.FileMaster;
import com.sttl.hrms.model.HrmsCode;
import com.sttl.hrms.notification.service.AuditTrailService;
import com.sttl.hrms.notification.service.NotificationMasterService;
import com.sttl.hrms.payroll.repository.PayStructureMasterRepository;
import com.sttl.hrms.repository.FileMasterRepository;
import com.sttl.hrms.security.util.SecurityChecker;
import com.sttl.hrms.service.HrmsCodeService;
import com.sttl.hrms.statemachine.workflow.data.enums.Pair;
import com.sttl.hrms.statemachine.workflow.data.enums.WorkflowType;
import com.sttl.hrms.statemachine.workflow.data.model.repository.WorkflowEventLogRepository;
import com.sttl.hrms.statemachine.workflow.data.model.repository.WorkflowInstanceRepository;
import com.sttl.hrms.statemachine.workflow.data.model.entity.WorkflowEventLogEntity;
import com.sttl.hrms.statemachine.workflow.data.model.entity.ClaimMatrixAppWorkFlowInstanceEntity;
import com.sttl.hrms.statemachine.workflow.data.model.entity.WorkflowRoleMaster;
import com.sttl.hrms.statemachine.workflow.data.model.entity.WorkflowRuleConfigurationMaster;
import com.sttl.hrms.statemachine.workflow.data.model.repository.ClaimMatrixAppWorkFlowInstanceRepository;
import com.sttl.hrms.statemachine.workflow.data.model.repository.WorkflowRolesMasterRepository;
import com.sttl.hrms.statemachine.workflow.data.model.repository.WorkflowRuleConfiguratiionMasterRepository;
import com.sttl.hrms.statemachine.workflow.resource.dto.ClaimMatrixWFInstanceDto;
import com.sttl.hrms.statemachine.workflow.resource.dto.EventResponseDto;
import com.sttl.hrms.statemachine.workflow.resource.dto.PassEventDto;
import com.sttl.hrms.statemachine.workflow.service.ClaimMatrixAppService;
import com.sttl.hrms.util.CommonConstant;
import com.sttl.hrms.util.CommonUtil;
import com.sttl.hrms.util.CommonUtility;
import com.sttl.hrms.util.DateUtil;
import com.sttl.hrms.util.NotificationAction;
import com.sttl.hrms.util.NotificationModule;
import com.sttl.hrms.util.PaginationUtil;
import com.sttl.hrms.util.SilverUtil;
import com.sttl.hrms.util.StringUtil;
import com.sttl.hrms.statemachine.workflow.data.model.entity.WorkflowInstanceEntity;
import com.sttl.hrms.workflow.widget.controller.WorkflowUtils;

@Controller
@RequestMapping(value = "/hrms/claimMatrix")
public class ClaimApplicationController {

	private static final Logger logger = LoggerFactory.getLogger(ClaimApplicationController.class);

	@Autowired
	private EmpPersonalInfoRepository empPersonalInfoRepository;

	@Autowired
	private ClaimItemConfigurationRepository claimItemConfigurationRepository;

	@Autowired
	private ClaimApplicationExpenseDetailsRepository claimApplicationExpenseDetailsRepository;

	@Autowired
	private RoleMasterRepository roleMasterRepo;
	
	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private ClaimCategoryMasterRepository claimCategoryMasterRepo;

	@Autowired
	private ItemMasterRepositoryForClaimMatrix itemMasterRepository;

	@Autowired
	private EmpFamilyRepository empFamilyRepository;

	@Autowired
	private HrmsCodeService hrmsCodeService;

	@Autowired
	private CompanyMasterRepository companyMasterRepository;

	@Autowired
	private CompanyBranchMasterRepository companyBranchMasterRepository;

	@Autowired
	private CommonUtility commonUtility;

	@Autowired
	private ClaimApplicationExpenseDetailsRepository claimApplicationExpenseDetailsRepo;
	
	@Autowired
	private ClaimApplicationExpenseDetailsTmpRepository claimApplicationExpenseDetailsTmpRepo;

	@Autowired
	SecurityChecker securityChecker;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	AuditTrailService auditTrailService;

	@Autowired
	private ClaimApplicationService claimApplicationService;

	@Autowired
	private UserMasterRepository userMasterRepository;

	@Autowired
	private WorkflowRolesMasterRepository workflowRolesMasterRepository;

	@Autowired
	private NotificationMasterService notificationMasterService;

	@Autowired
	private ClaimMatrixAppService claimMatrixAppService;

	@Autowired
	private WorkflowRuleConfiguratiionMasterRepository configuratiionMasterRepository;

	@Autowired
	private EmpReportingOfficerRepository reportingOfficerRepository;

	@Autowired
	private Environment environment;

	@Autowired
	private ClaimMatrixAppWorkFlowInstanceRepository claimMatrixAppWorkFlowInstanceRepository;

	@Autowired
	private FileMasterRepository fileMasterRepository;

	@Autowired
	private LeaveManualAttendanceMasterRepository leaveManualAttendanceMasterRepository;

	@Autowired
	private EmpSalaryDtlRepository empSalaryDtlRepository;

	@Autowired
	private ClaimItemConfigurationRepository claimconfigurationrepo;

	@Autowired
	private ClaimConfigurationService claimConfigurationService;

	@Autowired
	private ClaimConfigurationRepository claimConfigurationRepository;

	@Autowired
	private ClaimApplicationRepository claimApplicationRepository;

	@Autowired
	private ClaimApplicationExpenseDetailsRepository applicationExpenseDetailsRepository;
	
	@Autowired
	private WorkflowEventLogRepository eventLogRepository;
	
	@Autowired
	private WorkflowUtils workflowUtils;
	

	@Autowired
	private WorkflowInstanceRepository workflowinstancerepo;
	
	@Autowired
	private VehicleRunningMaintenanceExpenseRepository vehicleRepo;
	
	@Autowired
	private WorkflowInstanceRepository workflowInstanceRepository;
	
	@Autowired
	private FurnitureItemsRepository furnitureItemsrepository;

	@Autowired
	private FurnitureItemSubCategoryRepository furnitureItemSubCategoryRepository;

	@Autowired
	private GradeMasterRepository gradeMasterRepository;

	@Autowired
	private ClaimGradeDetailsRepository claimGradeDetailsRepository;

	@Autowired
	private ClaimAllowanceBasicDetailsRepository claimAllowanceBasicDetailsRepository;

	@Autowired
	private ClaimItemDetailsForAllowanceRepository claimItemDetailsForAllowanceRepository;
	
	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private PayStructureMasterRepository payStructureMasterRepository;
	
	@Autowired
	private HolidayConfigurationMasterDTLRepository holidayConfigurationMasterDTLRepository;

	@Autowired
	private EmployeeProvidentFundDtlRepo employeeProvidentFundDtlRepo;

	@Autowired
	private ElementOfPaySystemMasterRepository elementOfPaySystemMasterRepository;

	Map<String, List<FileMaster>> objectMap = new HashMap<>();

	@RequestMapping("/viewclaimApplication")
	public String widgetClaimApplicationView(@RequestParam(value = "id") Long id, HttpServletRequest request,
			Model model, HttpServletResponse response, HttpSession session) throws JSONException {

		try {

			logger.info("ClaimApplicationController:viewclaimApplication");

			UserMaster um = (UserMaster) session.getAttribute("usermaster");

			Long companyId = (Long) session.getAttribute("companyId");
			Long companyBranchId = (Long) session.getAttribute("companyBranchId");

			ClaimMatrixAppWorkFlowInstanceEntity claimMatrixAppWorkFlowInstanceEntity = claimMatrixAppWorkFlowInstanceRepository
					.findById(id).orElse(null);
			
			System.out.println("---------------------->>>>>>>>>>>>>>>>>>>>>");
			System.out.println(id);

			if (um == null || companyId == null || companyBranchId == null) {
				return "redirect:/signin";
			} else {

				model.addAttribute("claimMatrix", claimMatrixAppWorkFlowInstanceEntity);

				// ClaimApplication claimApplication
				// =claimApplicationRepository.findById(claimMatrixAppWorkFlowInstanceEntity.getClaimApplication().getId()).orElse(null);
				ClaimApplication claimApplication = claimApplicationRepository.findByApplicationId(id);

				List<ClaimApplicationExpenseDetails> claimApplicationExpenseDetails = claimApplicationExpenseDetailsRepo
						.findAllByClaimApplication2(claimApplication);
				
				double suml1approval = 0.0;
			    double suml2approval= 0.0;
			    for (ClaimApplicationExpenseDetails details : claimApplicationExpenseDetails) {
			     if(details.getApprovedAmount()!=null) {
			    	 suml1approval += details.getApprovedAmount();
			     }
			     if(details.getApprovedamountl2()!=null) {
			    	 suml2approval += details.getApprovedamountl2();
			     }  
		            
		        }
			    if(suml2approval== 0.0) {
			    	model.addAttribute("suml2approval", "-");
			    }
			    else {
			    	model.addAttribute("suml2approval", suml2approval);
			    }
			    if(suml1approval== 0.0) {
			    	model.addAttribute("suml1approval", "-");
			    }
			    else {
			    	model.addAttribute("suml1approval", suml1approval);
			    }
				
				System.out.println("id is=-------------->>>>>>>>>>> "+id);
				List<WorkflowEventLogEntity> logs = eventLogRepository.findWfdtlByclaimId(id);
				
				 System.out.println("Sum of approved amounts by l1: " + suml1approval);
			     System.out.println("Sum of approved amounts by l2: " + suml2approval);
			    
				
				
				model.addAttribute("claimApplication", claimApplication);
				model.addAttribute("claimApplicationExpenseDetails", claimApplicationExpenseDetails);
				model.addAttribute("logs",logs);
				model.addAttribute("expenseType", ExpenseType.values());
				model.addAttribute("id",id);

				// System.out.println("claimapplicationmodel>>>>>>>>>>" + claimApplication);
				// System.out.println("claimapplicationexpense>>>>>>>" + claimApplicationExpenseDetails);
				// System.out.println("eventLogRepository>>>>>>>>>>" + logs);
				List<WorkflowEventLogEntity> entityLogs;
				for (WorkflowEventLogEntity log : logs) {
				    //System.out.println("Log comment:---------------------- " + log.getComment());
				    entityLogs = workflowUtils.getLogsByApplicationId(log.getInstanceId(), request, session);
				    model.addAttribute("entityLogs", entityLogs);			
			    }

				System.err.println(um);

				model.addAttribute("user", um);

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in View Claim Application");
		}
		return "hrms/claimMatrix/viewclaimApplication";
//		return "hrms/claimMatrix/claimApplicationView";
	}

	@GetMapping(value = "/downloadFile-{id}")
	public void downloadFile(@PathVariable Long id, HttpServletResponse response, HttpServletRequest request,
			HttpSession session) {
		try {
			Long companyId = (Long) session.getAttribute("companyId");
			Long companyBranchId = (Long) session.getAttribute("companyBranchId");
			FileMaster fileMaster1 = fileMasterRepository.findById(id).orElse(null);
			
			if(fileMaster1 != null) {
				File file = new File(
						environment.getProperty("file.repository.hrms.path").concat(companyId.toString() + File.separator)
								.concat(companyBranchId.toString() + File.separator).concat(CommonConstant.EMP_FILES) + "/"
								+ fileMaster1.getFileName());
				
				if (file.exists()) {
					byte[] fileInByteArray = toByteArray(file);
					if (fileInByteArray != null) {
						response.setContentType(new MimetypesFileTypeMap().getContentType(environment
								.getProperty("file.repository.hrms.path").concat(companyId.toString() + File.separator)
								.concat(companyBranchId.toString() + File.separator).concat(CommonConstant.CLAIM_FILES)
								+ "/" + fileMaster1.getFileName()));
						response.setContentLength(fileInByteArray.length);
						response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
								"attachment; filename=" + fileMaster1.getActualFileName());
						response.getOutputStream().write(fileInByteArray);
						response.getOutputStream().flush();
					}
				} else {
//					byte[] fileInByteArray = ReportUtil.emptyPdfResponse(genFile.getFileName(),"Error: File not found.");
//					ReportUtil.getPdfReportResponse(fileInByteArray, response, genFile.getFileName());
				}
			}


		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	private static byte[] toByteArray(File file) throws IOException {
		byte[] bytesArray = new byte[(int) file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(bytesArray); // read file into bytes[]
		fis.close();
		return bytesArray;
	}

//	@RequestMapping(value = "/claimApplicationList")
//	public String getClaimApplicationList(@RequestParam(value = "rowItems", defaultValue = "10") int rowItems,
//			@RequestParam(value = "pageId", defaultValue = "0") int ipageId,
//			@RequestParam(value = "opageId", defaultValue = "0") int opageId, Model model, HttpServletRequest request,
//			final RedirectAttributes redirectAttributes, HttpSession session, HttpServletResponse response, Object defaultValue) {
//		logger.info("Inside claimApplicationList page");
//		try {
//
//			// securityChecker.checkMenuAccessPermit(
//			// "/hrms/claimMatrix/claimApplicationList", request, response,
//			// request.getSession(), CommonConstant.VIEW);
//			
//			if(rowItems == 0) {
//				if(session.getAttribute("defaultValue") != null) {
//					rowItems = Integer.parseInt((String) session.getAttribute("defaultValue"));
//					System.out.println("defaultValue in session =======================================>" + defaultValue);
//				}else {
////					rowItems = 10;
//					session.setAttribute("defaultValue", request.getParameter("rowItems"));
//				}
//			}
//			else {
////				session.setAttribute("defaultValue", request.getParameter("rowItems"));
//				if(session.getAttribute("defaultValue") != null) {
//					rowItems = Integer.parseInt((String) session.getAttribute("defaultValue"));
//					System.out.println("defaultValue in session =======================================>" + defaultValue);
//				}else {
////					rowItems = 10;
//					session.setAttribute("defaultValue", request.getParameter("rowItems"));
//				}
//				System.out.println("defaultValue in else =======================================>" + defaultValue);
//			}
//			
////			session.setAttribute("theme", userMaster.getTheme());
//
//			UserMaster um = (UserMaster) session.getAttribute("usermaster");
//			Long roleId = (Long) session.getAttribute("roleId");
//			Long companyId = (Long) session.getAttribute("companyId");
//			Long companyBranchId = (Long) session.getAttribute("companyBranchId");
//
//			if (um == null || companyId == null || companyBranchId == null) {
//				return "redirect:/login";
//			}
//
//			List<ListItems> actionList = SilverUtil.getBulkActionList();
//			model.addAttribute("actionList", actionList);
//
//			int start = 0;
//			int end = 0;
//			Pageable pageable = PageRequest.of(ipageId, rowItems, Sort.by("id").descending());
//			if (opageId == 0) {
//				pageable = PageRequest.of(0, rowItems, Sort.by(Sort.Direction.DESC, "id"));
//				start = 1;
//				end = rowItems;
//			} else {
//				pageable = PageRequest.of(opageId - 1, rowItems, Sort.by(Sort.Direction.DESC, "id"));
//				start = (rowItems * (opageId - 1) + 1);
//				end = rowItems * opageId;
//			}
//
//			request = commonUtil.setMenuPermissionsInRequest(request, CommonConstant.CLAIM_APPLICATION);
//
//			model.addAttribute("isAdd", request.getAttribute("addPermission"));
//			// model.addAttribute("isEdit", request.getAttribute("editPermission"));
//			// model.addAttribute("isView", request.getAttribute("viewPermission"));
//			// model.addAttribute("isDelete", request.getAttribute("deletePermission"));
//			Page<ClaimApplicationExpenseDetails> claimConfigurationList = null;
//			List<ClaimApplicationExpenseDetails> claimConfigurationList1=null;
//			RoleMaster role =  roleMasterRepo.findByIdAndIsDelete(roleId, false);
//			if (role.getIsAdmin()) {
//				claimConfigurationList1 = claimApplicationExpenseDetailsRepo
//						.findAllByIsDeleteFalseAndCompanyIdAndCompanyBranchIdOrderByCreatedDateDesc1(
//								companyId, companyBranchId);
//				claimConfigurationList = claimApplicationExpenseDetailsRepo
//						.findAllByIsDeleteFalseAndCompanyIdAndCompanyBranchIdOrderByCreatedDateDesc1(pageable,
//								companyId, companyBranchId);
//
//
////				System.out.println("claimConfigurationList: " + claimConfigurationList);
////				System.out.println("claimConfigurationList with: " + claimConfigurationList.getContent());
//
//			} else {
//				claimConfigurationList = claimApplicationExpenseDetailsRepo
//						.findAllByIsDeleteFalseAndCompanyIdAndCompanyBranchIdOrderByCreatedDateDesc(pageable, companyId,
//								companyBranchId, um.getEmpId().getId());
//
//				claimConfigurationList1 = claimApplicationExpenseDetailsRepo
//						.findAllByIsDeleteFalseAndCompanyIdAndCompanyBranchIdOrderByCreatedDateDesc(companyId,
//								companyBranchId, um.getEmpId().getId());
//			}
//			for (ClaimApplicationExpenseDetails claimApplicationExpenseDetails : claimConfigurationList) {
//				if(claimApplicationExpenseDetails.getClaimApplication().getConfirmStatus() != null && claimApplicationExpenseDetails.getClaimApplication().getConfirmStatus() != "") {
//					 if(claimApplicationExpenseDetails.getClaimApplication().getConfirmStatus().equals("UNDER_REVIEW")) {
//						 List<WorkflowEventLogEntity> pendingWithUsername = workflowUtils.getLogsByApplicationId(claimApplicationExpenseDetails.
//									getClaimApplication().getClaimMatrixAppWorkFlowInstanceEntity(), request, session);
//						 List<WorkflowEventLogEntity> pendingWithUsername1=new ArrayList<WorkflowEventLogEntity>();
//						 pendingWithUsername1.add(pendingWithUsername.get(0));
//						 claimApplicationExpenseDetails.setPendingWithUser(String.join(", ", pendingWithUsername1.get(0).getDisplay_name().toString()));
//					 }
//				}
//			}
//			
//			for (ClaimApplicationExpenseDetails claimApplicationExpenseDetails : claimConfigurationList1) {
//				if(claimApplicationExpenseDetails.getClaimApplication().getConfirmStatus() != null && claimApplicationExpenseDetails.getClaimApplication().getConfirmStatus() != "") {
//					 if(claimApplicationExpenseDetails.getClaimApplication().getConfirmStatus().equals("UNDER_REVIEW")) {
//						 List<WorkflowEventLogEntity> pendingWithUsername = workflowUtils.getLogsByApplicationId(claimApplicationExpenseDetails.
//									getClaimApplication().getClaimMatrixAppWorkFlowInstanceEntity(), request, session);
//						 List<WorkflowEventLogEntity> pendingWithUsername1=new ArrayList<WorkflowEventLogEntity>();
//						 pendingWithUsername1.add(pendingWithUsername.get(0));
//						 claimApplicationExpenseDetails.setPendingWithUser(String.join(", ", pendingWithUsername1.get(0).getDisplay_name().toString()));
//					 }
//				}
//			}
//			
//			List<Long> uniqueApplicationIds = claimConfigurationList1.stream()
//				    .map(detail -> detail.getClaimApplication().getId()) 
//				    .distinct() 
//				    .collect(Collectors.toList());
//			
//			 List<Object[]> result = claimApplicationExpenseDetailsRepository.getSumApprovedAmount(uniqueApplicationIds);
//			    List<ClaimApprovedSum> claimApprovedSums = new ArrayList<>();
//
//			    for (Object[] row : result) {
//			        Long claimApplicationId = ((Number) row[0]).longValue(); // Assuming the claim_application is Long
//			        Double approvedAmountL2 = 0D;
//			        if(row[1] != null)
//			        	approvedAmountL2=((Number) row[1]).doubleValue(); // Assuming approved_amount_l2 is Double
//
//			        ClaimApprovedSum claimApprovedSum = new ClaimApprovedSum(claimApplicationId, approvedAmountL2);
//			        claimApprovedSums.add(claimApprovedSum);
//			    }
//
//			
//			Map<Long,String> m=new HashMap<>();
//			for(ClaimApprovedSum c:claimApprovedSums) {
//				m.put(c.getClaimApplication(), c.getApprovedAmountL2() == 0D ?"-":c.getApprovedAmountL2()+"");
//			}
//			model.addAttribute("approvedamountl2map",m);
//			
//			System.out.println("approvedamountl2map is "+m);
//			
//			if (end > claimConfigurationList.getTotalElements()) {
//				end = (int) claimConfigurationList.getTotalElements();
//			}
//
//
//				model.addAttribute("claimApplicationList1", claimConfigurationList1);
//				model.addAttribute("claimApplicationList", claimConfigurationList.getContent());
//				model.addAttribute("oTotalPages", claimConfigurationList.getTotalPages());
//				model.addAttribute("oTotalElements", claimConfigurationList.getTotalElements());
//				model.addAttribute("orowItems", rowItems);
//				model.addAttribute("opageId", opageId);
//				model.addAttribute("ostartPage", (opageId == 0) ? 1 : opageId);
//				model.addAttribute("oStart", start);
//				model.addAttribute("oEnd", end);
//				model.addAttribute("listSizeDropDown", PaginationUtil.getShowPageList());
//				model.addAttribute("rowItems", rowItems);
//
////				if(role.getIsAdmin()) {
////					model.addAttribute("isDelete", true);
////				}else{
////					model.addAttribute("isDelete", false);
////				}
//
//
//			
//			//Added to load default claims ln list page
//			List<ClaimCategoryModel> claims = claimCategoryMasterRepo
//					.findAllByCompanyIdAndCompanyBranchIdAndIsDeleteFalse(companyId, companyBranchId);
//			model.addAttribute("claims", claims);
//			
//			// UNDER_REVIEW,APPROVED,REJECTED,CANCELED
//			List<HrmsCode> applicationStatus = hrmsCodeService.findByFieldName("APPLICATION_STATUS");
//			model.addAttribute("applicationStatus", applicationStatus);
//			model.addAttribute("status", request.getParameter("status") == null ? "status" : request.getParameter("status") );
//			
//			Long userId = (long) session.getAttribute("userId");
//			auditTrailService.saveAuditTrailData("ClaimMatrix", "ListPage", "Admin",
//					NotificationModule.CLAIM_APPLICATION, NotificationAction.LIST, "/claimApplicationList", userId);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return "hrms/claimMatrix/claimApplicationList";
//	}
	
	@RequestMapping(value = "/claimApplicationList")
	public String getClaimApplicationList(
			@RequestParam(value = "range", required = false, defaultValue = "last 3 months") String timeRangeParam,
			 Model model, HttpServletRequest request,
			final RedirectAttributes redirectAttributes, HttpSession session, HttpServletResponse response, Object defaultValue) {
		logger.info("Inside claimApplicationList page");
		try {

			// securityChecker.checkMenuAccessPermit(
			// "/hrms/claimMatrix/claimApplicationList", request, response,
			// request.getSession(), CommonConstant.VIEW);
			
			
			
//			session.setAttribute("theme", userMaster.getTheme());

			UserMaster um = (UserMaster) session.getAttribute("usermaster");
			Long roleId = (Long) session.getAttribute("roleId");
			Long companyId = (Long) session.getAttribute("companyId");
			Long companyBranchId = (Long) session.getAttribute("companyBranchId");

			if (um == null || companyId == null || companyBranchId == null) {
				return "redirect:/login";
			}

			List<ListItems> actionList = SilverUtil.getBulkActionList();
			model.addAttribute("actionList", actionList);

			int start = 0;
			int end = 0;
			

			request = commonUtil.setMenuPermissionsInRequest(request, CommonConstant.CLAIM_APPLICATION);

			model.addAttribute("isAdd", request.getAttribute("addPermission"));
			model.addAttribute("isEdit", request.getAttribute("editPermission"));
			model.addAttribute("isView", request.getAttribute("viewPermission"));
			model.addAttribute("isDelete", request.getAttribute("deletePermission"));
//			Page<ClaimApplicationExpenseDetails> claimConfigurationList = null;
//			List<ClaimApplicationExpenseDetails> claimConfigurationList1=null;
			List<Object[]> claimConfigurationList = null;
			List<ClaimApplicationExpenseDetails> claimConfigurationList1=null;
			RoleMaster role =  roleMasterRepo.findByIdAndIsDelete(roleId, false);
			if (role.getIsAdmin()) {
				
				claimConfigurationList = claimApplicationExpenseDetailsRepository.getAllClaimDetailsWithPandingWith(companyId, companyBranchId, 0L, timeRangeParam);
				



			} else {
				claimConfigurationList = claimApplicationExpenseDetailsRepository.getAllClaimDetailsWithPandingWith(companyId, companyBranchId, um.getEmpId().getId(), timeRangeParam);
			}
			
			
//			if (end > claimConfigurationList.getTotalElements()) {
//				end = (int) claimConfigurationList.getTotalElements();
//			}


				//model.addAttribute("claimApplicationList1", claimConfigurationList1);
				model.addAttribute("claimApplicationList", claimConfigurationList);
//				model.addAttribute("oTotalPages", claimConfigurationList.getTotalPages());
//				model.addAttribute("oTotalElements", claimConfigurationList.getTotalElements());
				
				model.addAttribute("oStart", start);
				model.addAttribute("oEnd", end);
				model.addAttribute("listSizeDropDown", PaginationUtil.getShowPageList());
				


				Long userId = (long) session.getAttribute("userId");
			auditTrailService.saveAuditTrailData("ClaimMatrix", "ListPage", "Admin",
					NotificationModule.CLAIM_APPLICATION, NotificationAction.LIST, "/claimApplicationList", userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "hrms/claimMatrix/claimApplicationList";
	}
	
	@RequestMapping(value = "/viewEmployeeExpenseList")
	public String viewEmployeeExpenseList(@RequestParam(value = "rowItems", defaultValue = "10") int rowItems,
			@RequestParam(value = "pageId", defaultValue = "0") int ipageId,
			@RequestParam(value = "opageId", defaultValue = "0") int opageId, Model model, HttpServletRequest request,
			final RedirectAttributes redirectAttributes, HttpSession session, HttpServletResponse response) {
		logger.info("Inside viewEmployeeExpenseList page");
		try {

			// securityChecker.checkMenuAccessPermit(
			// "/hrms/claimMatrix/claimApplicationList", request, response,
			// request.getSession(), CommonConstant.VIEW);

			UserMaster um = (UserMaster) session.getAttribute("usermaster");

			Long companyId = (Long) session.getAttribute("companyId");
			Long companyBranchId = (Long) session.getAttribute("companyBranchId");

			if (um == null || companyId == null || companyBranchId == null) {
				return "redirect:/login";
			}

			List<ListItems> actionList = SilverUtil.getBulkActionList();
			model.addAttribute("actionList", actionList);

			/*
			 * int start = 0; int end = 0; Pageable pageable = PageRequest.of(ipageId,
			 * rowItems, Sort.by("id").descending()); if (opageId == 0) { pageable =
			 * PageRequest.of(0, rowItems, Sort.by(Sort.Direction.DESC, "id")); start = 1;
			 * end = rowItems; } else { pageable = PageRequest.of(opageId - 1, rowItems,
			 * Sort.by(Sort.Direction.DESC, "id")); start = (rowItems * (opageId - 1) + 1);
			 * end = rowItems * opageId; }
			 */

			model.addAttribute("listSizeDropDown", PaginationUtil.getShowPageList());
			
			//Added to load default claims ln list page
			List<ClaimCategoryModel> claims = claimCategoryMasterRepo
					.findAllByCompanyIdAndCompanyBranchIdAndIsDeleteFalse(companyId, companyBranchId);
			model.addAttribute("claims", claims);
			
			Long userId = (long) session.getAttribute("userId");
			auditTrailService.saveAuditTrailData("ClaimMatrix", "ListPage", "Admin",
					NotificationModule.CLAIM_APPLICATION, NotificationAction.LIST, "/claimApplicationList", userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "hrms/claimMatrix/viewEmployeeExpense";
	}

	@RequestMapping("/addClaimApplication")
	public String addClaimApplication(HttpServletRequest request, Model model, HttpServletResponse response,
			HttpSession session) throws JSONException {

		try {
			// securityChecker.checkMenuAccessPermit(
			// "/hrms/claimMatrix/addClaimApplication", request, response,
			// request.getSession(), CommonConstant.VIEW);

			logger.info("LeaveEncashmentController.addLeaveEncashment");

			UserMaster um = (UserMaster) session.getAttribute("usermaster");

			Long companyId = (Long) session.getAttribute("companyId");
			Long companyBranchId = (Long) session.getAttribute("companyBranchId");
			Long roleId = (Long) session.getAttribute("roleId");
			if (um == null || companyId == null || companyBranchId == null) {
				return "redirect:/login";
			} else {
				RoleMaster role =  roleMasterRepo.findByIdAndIsDelete(roleId, false);
				if (role.getRoleName().equalsIgnoreCase(CommonConstant.EMP_ROLE)) {

					model.addAttribute("empRole", role.getIsAdmin());
				} else {
					model.addAttribute("Employee", um.getEmpId());
					model.addAttribute("empRole", role.getIsAdmin());
					// model.addAttribute("empStatus", um.getStatusMaster().getStatusName());
				}

				logger.info("Role:: " + role.getRoleName());
				List<Employee> employeeList = null;
				if (role.getIsAdmin()) {
					employeeList = employeeRepository
							.findAllByIsDeleteFalseAndCompanyIdAndCompanyBranchIdOrderByCreatedDateDesc(companyId,
									companyBranchId);
					model.addAttribute("employeeList", employeeList);
				} else
					model.addAttribute("Employee", um.getEmpId());

				List<ClaimCategoryModel> claims = claimCategoryMasterRepo
						.findAllByCompanyIdAndCompanyBranchIdAndIsDeleteFalse(companyId, companyBranchId);
				model.addAttribute("claims", claims);

				List<HrmsCode> monthList = hrmsCodeService.findByFieldName("MONTHS");
				model.addAttribute("months", monthList);
				
//				List<FurnitureItems> FurnitureItem= furnitureItemsrepository.findAllByCompanyIdAndCompanyBranchIdAndIsDeleteFalse(companyId,companyBranchId);
//				model.addAttribute("FurnitureItem", FurnitureItem);
				//System.out.println("FurnitureItemis++++++++++"+FurnitureItem);

				model.addAttribute("expenseType", ExpenseType.values());
				model.addAttribute("fuelType", FuelType.values());

				model.addAttribute("isAdd", true);
				model.addAttribute("claimappisAdd", true);
				model.addAttribute("claimappisEdit", false);
				model.addAttribute("claimappisView", false);

				model.addAttribute("claimApplicationObj", new ClaimApplication());
				model.addAttribute("claimApplicationExpenseDetails", new ClaimApplicationExpenseDetailstmp());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "hrms/claimMatrix/claimApplication";
	}

	@RequestMapping("/getFamilyDetails")
	public @ResponseBody List<EmpFamilyDtl> getFamilyDetails(@RequestParam("empId") Long empId, HttpSession session) {
		logger.info("Employee Id:: " + empId);
		List<EmpFamilyDtl> empFamilyList = new ArrayList<>();
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		try {
//			empFamilyList = claimApplicationService.getFamilyDetails(companyId, companyBranchId, empId);
			empFamilyList = 	empFamilyRepository.findByIsDeleteFalseAndCompanyIdAndCompanyBranchIdAndEmpIdWithYearRange(companyId, companyBranchId, empId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return empFamilyList;
	}

	@RequestMapping("/getClaimItems")
	public @ResponseBody List<ItemMasterForClaimMatrix> getClaimItems(@RequestParam("claimTypeId") String claimTypeId,
			HttpSession session) {
		logger.info("Claim Type Id" + claimTypeId);
		List<ItemMasterForClaimMatrix> claimItemList = new ArrayList<>();
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		// Long itemconfigId = request.getParameter("relationship");
		try {

			claimItemList = claimApplicationService.getClaimItems(Long.parseLong(claimTypeId), companyId,
					companyBranchId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return claimItemList;
	}

	@GetMapping("/eligibleForOvertimeAllowance")
	@ResponseBody
	public ResponseEntity<String> eligibleForOvertimeAllowance(@RequestParam("empId") String empId,
			HttpSession session) {
		logger.info("empId: " + empId);
		List<Object> overtimeList = new ArrayList<>();
		Long comId = (Long) session.getAttribute("companyId");

		try {
			overtimeList = jobRepository.findByComIdAndEmpIdAndGradeName(comId,Long.parseLong(empId));
			
			System.out.println("overtimeList: " + overtimeList.size() );
			if(overtimeList.size() <= 0) {
				System.out.println("not eligible");
				 return ResponseEntity.ok("NOTELIGIBLE");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok("SUCCESS");
	}
	
	@GetMapping("/checkMonthAndYearOvertimeAllowance")
	@ResponseBody
	public ResponseEntity<String> checkMonthAndYearOvertimrAllowance(Model model, HttpServletRequest request,HttpServletResponse response,
			@RequestParam("empId") String empId, @RequestParam("yearPeriod") String yearPeriod,@RequestParam("month") String month,
			HttpSession session) {
		
		HrmsCode months = hrmsCodeService.findByFieldNameAndCode("MONTHS",
				month);
		
		Long count = claimApplicationRepository.findByEmpIdAndMonthAndYearAndExpenseItemId(months.getDescription(), yearPeriod, Long.parseLong(empId));		
		System.out.println("Count: " + count);
		if(count > 0) {
			return ResponseEntity.ok("ALREADYEXISTS");
		}
		return ResponseEntity.ok("SUCCESS");
	}
	
	@GetMapping("/calculateOverTimeAllowance")
	@ResponseBody
	public ResponseEntity<Map<String, Double>> calculateOverTimeAllowance(Model model, HttpServletRequest request,HttpServletResponse response,
			@RequestParam("empId") String empId, @RequestParam("yearPeriod") String yearPeriod,@RequestParam("month") String month,@RequestParam("overtimeHours") String overtimeHours,
			HttpSession session) {
		logger.info("empId: " + empId);
		Long comId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
//		String overtimeHours = request.getParameter("overtimeHours");
		
		System.out.println("empId: " +empId); 
		System.out.println("yearPeriod: "+ yearPeriod);
		System.out.println("month: " + month);
		
		Double salaryPerMonth = payStructureMasterRepository.findSumofBasicAndDa(Long.parseLong(empId), Long.parseLong(yearPeriod), Long.parseLong(month));
		Long publicHolidayCount = holidayConfigurationMasterDTLRepository.findPublicHolidays(comId, companyBranchId, Long.parseLong(yearPeriod), Long.parseLong(month));
		Long TotalMonthMinusSundays = holidayConfigurationMasterDTLRepository.findTotalMonthDaysMinusSundays(Long.parseLong(month),Long.parseLong(yearPeriod));
		
		System.out.println("salaryPerMonth: " +salaryPerMonth); 
		System.out.println("publicHolidayCount: "+ publicHolidayCount);
		System.out.println("TotalMonthMinusSundays: " + TotalMonthMinusSundays);
		
		Long TotalDays = TotalMonthMinusSundays - publicHolidayCount;
		System.out.println("TotalDays: " +TotalDays);
		
		Long TotalHours = TotalDays * 8;
		Double ratePerHour = salaryPerMonth / TotalHours ;
		Double totalAmount = ratePerHour * Double.parseDouble(overtimeHours);
		totalAmount = (double) Math.round(totalAmount);
		
		System.out.println("TotalHours: " +TotalHours);
		System.out.println("ratePerHour: " +ratePerHour);
		System.out.println("totalAmount: " +totalAmount);
		
		Double oneThirdofSalary = salaryPerMonth/3;
		System.out.println("oneThirdofSalary: " +oneThirdofSalary);
		
		if(totalAmount > oneThirdofSalary) {
			totalAmount = oneThirdofSalary;
		}
		
		Map<String, Double> responseMap = new HashMap<>();
	    responseMap.put("ratePerHour", ratePerHour);
	    responseMap.put("totalAmount", totalAmount);

	    return ResponseEntity.ok(responseMap);
	}
	
	@RequestMapping("/getAdvance")
	public @ResponseBody List<ClaimApplication> getAdvance(@RequestParam("expenseCategory") Long expenseCategory,
			@RequestParam("expenseItem") Long expenseItemId, @RequestParam("empId") Long empId, HttpSession session) {
		logger.info("Expense Category Id:: " + expenseCategory);
		logger.info("Expense Item Id:: " + expenseItemId);
		List<ClaimApplication> claimApplication = null;
		try {
			claimApplication = claimApplicationService.getAdvance(expenseCategory, expenseItemId, empId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return claimApplication;
	}

	@RequestMapping("/getAdvanceByChance")
	public @ResponseBody List<ClaimApplication> getAdvanceByChance(
			@RequestParam("expenseCategory") Long expenseCategory, @RequestParam("expenseItem") Long expenseItemId,
			@RequestParam("empId") Long empId, HttpSession session) {
		logger.info("Expense Category Id:: " + expenseCategory);
		logger.info("Expense Item Id:: " + expenseItemId);
		List<ClaimApplication> claimApplication = null;
		try {
			claimApplication = claimApplicationService.getAdvanceByChance(expenseCategory, expenseItemId, empId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return claimApplication;
	}

	@PostMapping("/setClaimApplicationExpenseDetails")
	@ResponseBody
	public Object setClaimApplicationExpenseDetails(Model model, HttpServletRequest request,
			@RequestParam(value = "file", required = false) MultipartFile file, HttpServletResponse response,
			HttpSession session, RedirectAttributes redirectAttributes) throws Exception {
		// logger.info("leaveApplicationDetails::"+claimApplicationExpenseDetails.toString());
		ClaimApplicationExpenseDetails claimApplicationExpenseDetails = new ClaimApplicationExpenseDetails();
		ClaimApplicationExpenseDetailstmp claimApplicationExpenseDetailstemp = new ClaimApplicationExpenseDetailstmp();
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		String expid=request.getParameter("Id");
		System.out.println("im here=====>"+expid);
		String claimappidedit=request.getParameter("claimappidedit");

		ItemMasterForClaimMatrix claimItem = itemMasterRepository
				  .findById(Long.parseLong(request.getParameter("expenseItem"))).get();

		System.out.println("Hereddddddddddsfc"+ request.getParameter("year"));

		 
		  //Long expensedttmpid=Long.parseLong(expid);
		  //ClaimApplicationExpenseDetailstmp cexp = 
				  //claimApplicationExpenseDetailsTmpRepo.findByClaimApplicationExpenseDetailstmpId(expensedttmpid);
		try {
	  if((expid.equalsIgnoreCase("undefined") || expid ==null || expid.equalsIgnoreCase("")) ) {	
		  

			if (request.getParameter("expenseItem") != null) {

//				ItemMasterForClaimMatrix claimItem = itemMasterRepository
//						.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
				System.out.println(" result *****" + claimItem);
				claimApplicationExpenseDetails.setItemMasterForClaimMatrix(claimItem);
				claimApplicationExpenseDetailstemp.setExpenseItem(claimItem);
			}

			if (request.getParameter("advanceId") != null) {
				ClaimApplication claimApplication = claimApplicationRepository
						.findByIdAndIsDeleteFalse(Long.parseLong(request.getParameter("expenseItem")));
				claimApplicationExpenseDetails.setAdvanceId(claimApplication);
				claimApplicationExpenseDetailstemp.setAdvanceId(claimApplication);
			}

			if (!"".equals(request.getParameter("expenseBillPeriod"))) {
				if (!request.getParameter("expenseBillPeriod").equals("CalenderBased")) {
					HrmsCode month = hrmsCodeService.findByFieldNameAndCode("MONTHS",
							request.getParameter("expenseBillPeriod"));
					if(claimItem.getItemName().equals("Over Time Allowance") || claimItem.getItemName().equals("Entertainment Expenses")){
						claimApplicationExpenseDetails.setExpenseBillPeriod(month.getDescription());
						claimApplicationExpenseDetailstemp.setExpenseBillPeriod(month.getDescription());
					}
					else {
						claimApplicationExpenseDetails.setExpenseBillPeriod(null);
						claimApplicationExpenseDetailstemp.setExpenseBillPeriod(null);
					}
					
					
				} else {
					claimApplicationExpenseDetails.setExpenseBillPeriod("Caldendar Based");
					claimApplicationExpenseDetailstemp.setExpenseBillPeriod("Caldendar Based");
				}
			}

		  if (!"".equals(request.getParameter("year"))) {

			  

			  if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")){
				  claimApplicationExpenseDetails.setYear(Integer.parseInt(request.getParameter("year")));
				  claimApplicationExpenseDetailstemp.setYear(Integer.parseInt(request.getParameter("year")));
			  }



		  }

			/*
			 * if (!"".equals(request.getParameter("incurredFor")) &&
			 * !request.getParameter("relationship").equals("self")) {
			 * 
			 * // EmpFamilyDtl empFamilyList =
			 * empFamilyRepository.findByEmployeeIdAndCompanyIdAndCompanyBranchId( //
			 * Long.parseLong(request.getParameter("empClaim")), companyId, companyBranchId,
			 * // request.getParameter("relationship").trim());
			 * System.out.println(" incurredFor *****" +
			 * request.getParameter("incurredFor"));
			 * System.out.println(" relationship *****" +
			 * request.getParameter("relationship"));
			 * 
			 * EmpFamilyDtl empFamilyList =
			 * empFamilyRepository.findByEmployeeIdAndCompanyIdAndCompanyBranchId(
			 * Long.parseLong(request.getParameter("empClaim")), companyId, companyBranchId,
			 * request.getParameter("relationship").trim(),
			 * request.getParameter("incurredFor").trim());
			 * claimApplicationExpenseDetails.setIncurredFor(empFamilyList);
			 * claimApplicationExpenseDetails
			 * .setRelationship(empFamilyList.getFamilyRelationId().getFamilyRelationName())
			 * ; }
			 */ /*
				 * else { //EmpFamilyDtl empFamilyList =
				 * empFamilyRepository.findByEmployeeIdAndCompanyIdAndCompanyBranchId(Long.
				 * parseLong(request.getParameter("empClaim")), companyId,companyBranchId);
				 * //claimApplicationExpenseDetails.setIncurredFor(empFamilyList);
				 * claimApplicationExpenseDetails.setRelationship("self"); }
				 */
			if (!"".equals(request.getParameter("incurredFor")) && !request.getParameter("relationship").equals("self")) {

//				EmpFamilyDtl empFamilyList = empFamilyRepository.findByEmployeeIdAndCompanyIdAndCompanyBranchId(
//						Long.parseLong(request.getParameter("empClaim")), companyId, companyBranchId,
//						request.getParameter("relationship").trim());
				System.out.println(" incurredFor *****" + request.getParameter("incurredFor"));
				System.out.println(" relationship *****" + request.getParameter("relationship"));
				String str = request.getParameter("relationship");
				String result = str.split("\\(")[0].trim();
				
				EmpFamilyDtl empFamilyList = empFamilyRepository.findByEmployeeIdAndCompanyIdAndCompanyBranchId(
						Long.parseLong(request.getParameter("empClaim")), companyId, companyBranchId,
						result,request.getParameter("incurredFor").trim());
				
				if(claimItem.getItemName().equals("Over Time Allowance") || claimItem.getItemName().equals("Conveyance")){
					claimApplicationExpenseDetails.setIncurredFor(null);
					
					claimApplicationExpenseDetailstemp.setIncurredFor(null);
					claimApplicationExpenseDetails
							.setRelationship(null);
					
					claimApplicationExpenseDetailstemp
					.setRelationship(null);
				}
				else {
					claimApplicationExpenseDetails.setIncurredFor(empFamilyList);
					
					claimApplicationExpenseDetailstemp.setIncurredFor(empFamilyList);
					claimApplicationExpenseDetails
							.setRelationship(empFamilyList.getFamilyRelationId().getFamilyRelationName());
					
					claimApplicationExpenseDetailstemp
					.setRelationship(empFamilyList.getFamilyRelationId().getFamilyRelationName());
				}
				
				
				
			} 
			else {
				claimApplicationExpenseDetailstemp.setIncurredFor(null);
			}
			if (request.getParameter("relationship") == "" || request.getParameter("relationship").equalsIgnoreCase("self")) {
				
				if(claimItem.getItemName().equals("Over Time Allowance") || claimItem.getItemName().equals("Conveyance")){
					claimApplicationExpenseDetails.setRelationship(null);
					claimApplicationExpenseDetailstemp.setRelationship(null);
				}
				else {
					claimApplicationExpenseDetails.setRelationship("self");
					claimApplicationExpenseDetailstemp.setRelationship("self");
				}
				
			}

			if (request.getParameter("billNumber") != null) {
				claimApplicationExpenseDetails.setBillNumber(request.getParameter("billNumber").trim());
				claimApplicationExpenseDetailstemp.setBillNumber(request.getParameter("billNumber").trim());

			}

		  System.out.println("claimMonth if======>>>>>>"+request.getParameter("claimMonth")+"claimYear if=======>"+request.getParameter("claimYear"));

			if (request.getParameter("eventName") != null) {
				claimApplicationExpenseDetails.setEventName(request.getParameter("eventName").trim());
				claimApplicationExpenseDetailstemp.setEventName(request.getParameter("eventName").trim());
			}
			if (request.getParameter("claimMonth") != null && !request.getParameter("claimMonth").isBlank() && request.getParameter("claimMonth")!="") {
				claimApplicationExpenseDetails.setClaimMonth(request.getParameter("claimMonth").trim());
				claimApplicationExpenseDetailstemp.setClaimMonth(request.getParameter("claimMonth").trim());
			}
			if (request.getParameter("claimYear") != null && !request.getParameter("claimYear").isBlank() && request.getParameter("claimYear")!="") {
				claimApplicationExpenseDetails.setClaimYear(Long.parseLong(request.getParameter("claimYear").trim()));
				claimApplicationExpenseDetailstemp.setClaimYear(Long.parseLong(request.getParameter("claimYear").trim()));
			}

			if (request.getParameter("billDate") != null && !request.getParameter("billDate").isBlank() && !request.getParameter("billDate").equalsIgnoreCase("")) {
				
				if(claimItem.getItemName().equals("Over Time Allowance")){
					claimApplicationExpenseDetails.setBillDate(null);
					claimApplicationExpenseDetailstemp.setBillDate(null);
				}
				else {
					claimApplicationExpenseDetails.setBillDate(
							DateUtil.convertStringToDate(request.getParameter("billDate"), DateUtil.IST_DATE_FORMATE));
					claimApplicationExpenseDetailstemp.setBillDate(
							DateUtil.convertStringToDate(request.getParameter("billDate"), DateUtil.IST_DATE_FORMATE));
				}
				
			}else {
				
				if(claimItem.getItemName().equals("Over Time Allowance") || claimItem.getItemName().equals("Conveyance")){
					claimApplicationExpenseDetails.setBillDate(null);
					claimApplicationExpenseDetailstemp.setBillDate(null);
				}
				else {
						org.joda.time.LocalDate billdate = new org.joda.time.LocalDate();
						claimApplicationExpenseDetails.setBillDate(
								DateUtil.convertStringToDate(billdate.getDayOfMonth() + "/" + billdate.getMonthOfYear() + "/" + billdate.getYear(), DateUtil.IST_DATE_FORMATE));
						claimApplicationExpenseDetailstemp.setBillDate(
								DateUtil.convertStringToDate(billdate.getDayOfMonth() + "/" + billdate.getMonthOfYear() + "/" + billdate.getYear(), DateUtil.IST_DATE_FORMATE));

				}
				
			}
			
			

			if (request.getParameter("fromDate") != null) {
				System.out.println("From Date:: " + request.getParameter("fromDate"));

//				ItemMasterForClaimMatrix claimItem = itemMasterRepository
//						.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
		
				if(claimItem.getItemName().equals("Entertainment Expenses")|| claimItem.getItemName().equals("Over Time Allowance")){
					claimApplicationExpenseDetails.setFromDate(null);
					claimApplicationExpenseDetailstemp.setFromDate(null);
				}else{

				claimApplicationExpenseDetails.setFromDate(
						DateUtil.convertStringToDate(request.getParameter("fromDate"), DateUtil.IST_DATE_FORMATE));
				claimApplicationExpenseDetailstemp.setFromDate(
						DateUtil.convertStringToDate(request.getParameter("fromDate"), DateUtil.IST_DATE_FORMATE));}
			}

			if (request.getParameter("toDate") != null) {
				String date = request.getParameter("toDate");

//				ItemMasterForClaimMatrix claimItem = itemMasterRepository
//						.findById(Long.parseLong(request.getParameter("expenseItem"))).get();

				if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")){
					claimApplicationExpenseDetails.setToDate(null);
					claimApplicationExpenseDetailstemp.setToDate(null);
				}else{
					claimApplicationExpenseDetails.setToDate(DateUtil.convertStringToDate(date, DateUtil.IST_DATE_FORMATE));
					claimApplicationExpenseDetailstemp.setToDate(DateUtil.convertStringToDate(date, DateUtil.IST_DATE_FORMATE));
				}


			}

			if (!"".equals(request.getParameter("expenseIncurredAt"))) {
				claimApplicationExpenseDetails.setExpenseIncurredAt(request.getParameter("expenseIncurredAt").trim());
				claimApplicationExpenseDetailstemp.setExpenseIncurredAt(request.getParameter("expenseIncurredAt").trim());

			}
			
			if (!"".equals(request.getParameter("billValue")) && !request.getParameter("billValue").equals("undefined")  ) {
				System.out.println("request.getParameter(\"billValue\") "+request.getParameter("billValue"));
				claimApplicationExpenseDetails.setBillValue(Double.parseDouble(request.getParameter("billValue")));
				claimApplicationExpenseDetailstemp.setBillValue(Double.parseDouble(request.getParameter("billValue")));

			}
//			ItemMasterForClaimMatrix claimItem = itemMasterRepository
//					.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
			if(claimItem.getItemName().equals("Vehicle Running & Maintenance Expense Four Wheeler") || claimItem.getItemName().equals("Vehicle Running & Maintenance Expense Two Wheeler")) {
				if(request.getParameter("fromDate") != null || request.getParameter("toDate") != null) {
					Date fromDate = DateUtil.convertStringToDate(request.getParameter("fromDate"), DateUtil.IST_DATE_FORMATE);
					Date toDate = DateUtil.convertStringToDate(request.getParameter("toDate"), DateUtil.IST_DATE_FORMATE);
					Calendar calendarfrom = Calendar.getInstance();
		            calendarfrom.setTime(fromDate);
		            int monthfrom = calendarfrom.get(Calendar.MONTH) + 1;
		            String monthfromNumber = String.format("%02d", monthfrom);
		            Calendar calendarto = Calendar.getInstance();
		            calendarto.setTime(toDate);
		            int monthto = calendarto.get(Calendar.MONTH) + 1;
		            String monthtoNumber = String.format("%02d", monthto);
		            System.out.println("monthfrom "+monthfromNumber + "monthto " + monthtoNumber + request.getParameter("fuelType").trim().toUpperCase());
		            VehicleRunningMaintenanceExpense rate=  vehicleRepo.findAllBetweenStartMonthAndEndMonthAndIsDeleteFalse(monthfromNumber,monthtoNumber,request.getParameter("fuelType").trim().toUpperCase());
		            Double requestedvalue = Double.parseDouble(request.getParameter("claimedLtr")) * rate.getRate();
					Double requestedroundedValue = (double) Math.round(requestedvalue);
					claimApplicationExpenseDetails.setRequestedValue(requestedroundedValue);
		            claimApplicationExpenseDetailstemp.setRequestedValue(requestedroundedValue);


					Double expenseAmt = Double.parseDouble(request.getParameter("incurredLtr"))* rate.getRate();
					Double expenseAmtroundedValue = (double) Math.round(expenseAmt);
		            System.out.println("expenseAmt is -----------------------"+expenseAmtroundedValue);
		            claimApplicationExpenseDetails.setTotalBillValue(expenseAmtroundedValue);
		            claimApplicationExpenseDetailstemp.setTotalBillValue(expenseAmtroundedValue);

				}
			}else {
			
			if (!"".equals(request.getParameter("requestedValue")) && !request.getParameter("requestedValue").equals("undefined")) {
				//System.out.println("request.getParameter(\"requestedValue\") "+request.getParameter("requestedValue"));
				claimApplicationExpenseDetails
						.setRequestedValue(Double.parseDouble(request.getParameter("requestedValue")));
				claimApplicationExpenseDetailstemp
				.setRequestedValue(Double.parseDouble(request.getParameter("requestedValue")));
			}

			if (!"".equals(request.getParameter("totalBillValue"))
					&& !request.getParameter("totalBillValue").equals("-")) {
				
				
				claimApplicationExpenseDetailstemp
				.setTotalBillValue(Double.parseDouble(request.getParameter("totalBillValue")));
			}	
			
			if (!"".equals(request.getParameter("requestedValue")) && !request.getParameter("requestedValue").equals("undefined")) {
				//System.out.println("request.getParameter(\"requestedValue\") "+request.getParameter("requestedValue"));
				claimApplicationExpenseDetails
						.setTotalBalancePending(Double.parseDouble(request.getParameter("requestedValue")));
				claimApplicationExpenseDetailstemp
				.setTotalBalancePending(Double.parseDouble(request.getParameter("requestedValue")));
			}
			
			}

			if (!"".equals(request.getParameter("currency"))) {
				claimApplicationExpenseDetails.setCurrency(request.getParameter("currency").trim());
				claimApplicationExpenseDetailstemp.setCurrency(request.getParameter("currency").trim());

			}
			
			String financialYear = request.getParameter("financialYear");
			System.out.println("financial year is " + financialYear);

			if (financialYear != null && !"".equals(financialYear) && !financialYear.equalsIgnoreCase("null")) {
			    String startYear = financialYear.split("-")[0];
			    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			    
			    // Create the from date
			    String fromDateString = "01/04/" + startYear;
			    Date fromDate = dateFormat.parse(fromDateString);
			    
			    claimApplicationExpenseDetails.setFromDate(fromDate);
			    claimApplicationExpenseDetailstemp.setFromDate(fromDate);
			    
			    // Create the to date
			    String toDateString = "31/03/" + (Integer.parseInt(startYear) + 1);
			    Date toDate = dateFormat.parse(toDateString);
			    
			    claimApplicationExpenseDetails.setToDate(toDate);
			    claimApplicationExpenseDetailstemp.setToDate(toDate);
			    
			    claimApplicationExpenseDetails.setFinancialYear(request.getParameter("financialYear").trim());
			    claimApplicationExpenseDetailstemp.setFinancialYear(request.getParameter("financialYear").trim());
			}

			
			Optional<CompanyMaster> cm = companyMasterRepository.findById(companyId);
			Optional<CompanyBranchMaster> cbm = companyBranchMasterRepository.findById(companyBranchId);
			//System.out.println("request.getParameter(\"file\") : " + request.getParameter("file"));
			System.out.println("fileobjectis"+request.getParameter("fileIde"));
			System.out.println("file is +++++++++++ "+request.getParameter("file"));
            if(request.getParameter("file")==null) {
            	//MultipartFile file="undefined";
            }
			if (request.getParameter("uploadDocument") != null && !request.getParameter("uploadDocument").equals("") && request.getParameter("fileIde")==null  
					)  {
				
			  if(file!=null ) {
				  System.out.println(request.getParameter("uploadDocument").trim());
					FileMaster fileMaster = new FileMaster();
					if (file.getOriginalFilename() != "") {
						fileMaster.setFileName(file.getOriginalFilename());
						fileMaster.setContentType(file.getContentType());
						String fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'),
								file.getOriginalFilename().length());
						fileMaster.setFileType(fileType);
						fileMaster.setCompany(cm.get());
						fileMaster.setCompanyBranch(cbm.get());
						String name = file.getOriginalFilename();
						FileMaster fileMasterClaimApplication = commonUtility.saveFileObject(name, file,
								"/hrms/claimMatrix/ClaimApplication", companyId, companyBranchId);
						System.out.println("fileMasterClaimApplication "+fileMasterClaimApplication.getId());
						claimApplicationExpenseDetails.setAttachDocument(fileMasterClaimApplication);
						
						claimApplicationExpenseDetailstemp.setAttachDocument(fileMasterClaimApplication);

						//addValueToMap(objectMap, "fileMaster", fileMasterClaimApplication);

					}
					else {
						System.out.println("no action required");
					}

			  }
				

			}
			

			// claimApplicationExpenseDetails.setAttachDocumentnew(objectMap.get("fileMaster"));
			System.out.println("request.getParameter(\"exception\")" + request.getParameter("exception"));
//			if (!request.getParameter("exception").equals("undefined")) {
//				claimApplicationExpenseDetails.setException(request.getParameter("exception"));
//				claimApplicationExpenseDetailstemp.setException(request.getParameter("exception"));
//
//			}
			
				claimApplicationExpenseDetailstemp.setException("No");

			

			if (!"".equals(request.getParameter("comments"))) {
				claimApplicationExpenseDetails.setClaimComments(request.getParameter("comments").trim());
				claimApplicationExpenseDetailstemp.setClaimComments(request.getParameter("comments").trim());

			}
			if (!"".equals(request.getParameter("expenseAmount"))) {
				claimApplicationExpenseDetails
						.setExpenseAmount(Double.parseDouble(request.getParameter("expenseAmount")));
				claimApplicationExpenseDetailstemp
				.setExpenseAmount(Double.parseDouble(request.getParameter("expenseAmount")));
			}

			if (!"".equals(request.getParameter("billPaymentRequired"))) {
				claimApplicationExpenseDetails
						.setBillPaymentRequired(request.getParameter("billPaymentRequired").trim());
				claimApplicationExpenseDetailstemp
				.setBillPaymentRequired(request.getParameter("billPaymentRequired").trim());
			}

			if (!"".equals(request.getParameter("quantityOrAmount"))) {
				claimApplicationExpenseDetails
						.setQuantityOrAmount(Double.parseDouble(request.getParameter("quantityOrAmount")));
				
				claimApplicationExpenseDetailstemp
				.setQuantityOrAmount(Double.parseDouble(request.getParameter("quantityOrAmount")));
			}

			if (!"".equals(request.getParameter("pendingAmountForApproval"))) {
				claimApplicationExpenseDetails.setPendingAmountForApproval(
						Double.parseDouble(request.getParameter("pendingAmountForApproval")));
				
				claimApplicationExpenseDetailstemp.setPendingAmountForApproval(
						Double.parseDouble(request.getParameter("pendingAmountForApproval")));
			}

			if (request.getParameter("unitOfMeasurement") != null) {
				claimApplicationExpenseDetails.setUnitOfMeasurement(request.getParameter("unitOfMeasurement").trim());
				claimApplicationExpenseDetailstemp.setUnitOfMeasurement(request.getParameter("unitOfMeasurement").trim());

			}

			if (request.getParameter("natureOfTravel") != null) {
				claimApplicationExpenseDetails.setNatureOfTravel(request.getParameter("natureOfTravel").trim());
				claimApplicationExpenseDetailstemp.setNatureOfTravel(request.getParameter("natureOfTravel").trim());

			}

			if (!"".equals(request.getParameter("numberOfDays"))) {

			claimItem = itemMasterRepository
						.findById(Long.parseLong(request.getParameter("expenseItem"))).get();

				if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")){
					claimApplicationExpenseDetails.setNumberOfDays(null);
					claimApplicationExpenseDetailstemp.setNumberOfDays(null);
				}
				else {
					claimApplicationExpenseDetails.setNumberOfDays(Long.parseLong(request.getParameter("numberOfDays")));
					claimApplicationExpenseDetailstemp.setNumberOfDays(Long.parseLong(request.getParameter("numberOfDays")));
				}

			}

			if (request.getParameter("timeFrom") != null) {

				claimItem = itemMasterRepository
						.findById(Long.parseLong(request.getParameter("expenseItem"))).get();

				if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")){
					claimApplicationExpenseDetails.setTimeFrom(null);
					claimApplicationExpenseDetailstemp.setTimeFrom(null);
				}else{

				claimApplicationExpenseDetails.setTimeFrom(
						DateUtil.convertStringToDate(request.getParameter("timeFrom"), DateUtil.IST_DATE_FORMATE));
				claimApplicationExpenseDetailstemp.setTimeFrom(
						DateUtil.convertStringToDate(request.getParameter("timeFrom"), DateUtil.IST_DATE_FORMATE));}
			}

			if (request.getParameter("timeTo") != null) {


				claimItem = itemMasterRepository
						.findById(Long.parseLong(request.getParameter("expenseItem"))).get();

				if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")) {
					claimApplicationExpenseDetails.setTimeTo(null);
					claimApplicationExpenseDetailstemp.setTimeTo(null);

				}else{


				claimApplicationExpenseDetails.setTimeTo(
						DateUtil.convertStringToDate(request.getParameter("timeTo"), DateUtil.IST_DATE_FORMATE));
				claimApplicationExpenseDetailstemp.setTimeTo(
						DateUtil.convertStringToDate(request.getParameter("timeTo"), DateUtil.IST_DATE_FORMATE));}
			}

			if (request.getParameter("fuelType") != null) {
				claimApplicationExpenseDetails.setFuelType(request.getParameter("fuelType").trim());
				claimApplicationExpenseDetailstemp.setFuelType(request.getParameter("fuelType").trim());

			}

//			if (!"".equals(request.getParameter("vehicleRegistrationNo"))) {
//				claimApplicationExpenseDetails
//						.setVehicleRegistrationNo(Long.parseLong(request.getParameter("vehicleRegistrationNo")));
//				claimApplicationExpenseDetailstemp
//				.setVehicleRegistrationNo(Long.parseLong(request.getParameter("vehicleRegistrationNo")));
//			}
			
			if (!"".equals(request.getParameter("vehicleRegistrationNo"))) {
			    claimApplicationExpenseDetails
			        .setVehicleRegistrationNo(request.getParameter("vehicleRegistrationNo"));
			    claimApplicationExpenseDetailstemp
			        .setVehicleRegistrationNo(request.getParameter("vehicleRegistrationNo"));
			}


			if (!"".equals(request.getParameter("advanceTaken")) && !request.getParameter("advanceTaken").equals("-")) {
				claimApplicationExpenseDetails
						.setAdvanceTaken(Double.parseDouble(request.getParameter("advanceTaken")));
				claimApplicationExpenseDetailstemp
				.setAdvanceTaken(Double.parseDouble(request.getParameter("advanceTaken")));
			}

			if (!"".equals(request.getParameter("netClaimedAmount"))
					&& !request.getParameter("netClaimedAmount").equals("-")) {
				claimApplicationExpenseDetails
						.setNetClaimedAmount(Double.parseDouble(request.getParameter("netClaimedAmount")));
				claimApplicationExpenseDetailstemp
				.setNetClaimedAmount(Double.parseDouble(request.getParameter("netClaimedAmount")));
			}

			if (request.getParameter("purchasedFrom") != null && !request.getParameter("purchasedFrom").equals("-")) {
				claimApplicationExpenseDetails.setPurchasedFrom(request.getParameter("purchasedFrom").trim());
				claimApplicationExpenseDetailstemp.setPurchasedFrom(request.getParameter("purchasedFrom").trim());

			}

			if (request.getParameter("accountNo") != null && !request.getParameter("accountNo").equals("-")) {
				claimApplicationExpenseDetails.setAccountNo(request.getParameter("accountNo").trim());
				claimApplicationExpenseDetailstemp.setAccountNo(request.getParameter("accountNo").trim());

			}

			if (request.getParameter("vehicleType") != null && !request.getParameter("vehicleType").equals("-")) {
				claimApplicationExpenseDetails.setVehicleType(request.getParameter("vehicleType").trim());
				claimApplicationExpenseDetailstemp.setVehicleType(request.getParameter("vehicleType").trim());

			}

			if (!"".equals(request.getParameter("incurredLtr")) && !request.getParameter("incurredLtr").equals("-")) {
				claimApplicationExpenseDetails.setIncurredLtr(Double.parseDouble(request.getParameter("incurredLtr")));
				claimApplicationExpenseDetailstemp.setIncurredLtr(Double.parseDouble(request.getParameter("incurredLtr")));

			}

			if (!"".equals(request.getParameter("claimedLtr")) && !request.getParameter("claimedLtr").equals("-")) {
				claimApplicationExpenseDetails.setClaimedLtr(Double.parseDouble(request.getParameter("claimedLtr")));
				claimApplicationExpenseDetailstemp.setClaimedLtr(Double.parseDouble(request.getParameter("claimedLtr")));

			}

			if (!"".equals(request.getParameter("billValueGst")) && !request.getParameter("billValueGst").equals("-")) {
				claimApplicationExpenseDetails
						.setBillValueGst(Double.parseDouble(request.getParameter("billValueGst")));
				claimApplicationExpenseDetailstemp
				.setBillValueGst(Double.parseDouble(request.getParameter("billValueGst")));
			}

			// if (!"".equals(request.getParameter("totalBillValue"))
			// 		&& !request.getParameter("totalBillValue").equals("-")) {
			// 	claimApplicationExpenseDetails
			// 			.setTotalBillValue(Double.parseDouble(request.getParameter("totalBillValue")));
				
			// 	claimApplicationExpenseDetailstemp
			// 	.setTotalBillValue(Double.parseDouble(request.getParameter("totalBillValue")));
			// }
			
			if (!"".equals(request.getParameter("hospitalId")) && !request.getParameter("hospitalId").equals("-")) {
				claimApplicationExpenseDetails.setHospitalName(request.getParameter("hospitalId"));
				claimApplicationExpenseDetailstemp.setHospitalName(request.getParameter("hospitalId"));

			}
			
			if (!"".equals(request.getParameter("DoctorId")) && !request.getParameter("DoctorId").equals("-")) {
				claimApplicationExpenseDetails.setDoctorName(request.getParameter("DoctorId"));
				claimApplicationExpenseDetailstemp.setDoctorName(request.getParameter("DoctorId"));

			}
			
			if (request.getParameter("taxableformedical").equalsIgnoreCase("true")) {
				claimApplicationExpenseDetailstemp.setTaxableformedical(true);
			} else {
				claimApplicationExpenseDetailstemp.setTaxableformedical(false);
			}
			
			
			if (request.getParameter("furnitureItem") != null && !"".equals(request.getParameter("furnitureItem")) && !request.getParameter("furnitureItem").equals("-")) {
				claimApplicationExpenseDetails.setFurnitureItem(request.getParameter("furnitureItem"));
				claimApplicationExpenseDetailstemp.setFurnitureItem(request.getParameter("furnitureItem"));

			}

		  if (request.getParameter("furnitureSubItem") != null && !"".equals(request.getParameter("furnitureSubItem")) && !request.getParameter("furnitureSubItem").equals("-")) {
			  claimApplicationExpenseDetails.setFurnitureSubItem(request.getParameter("furnitureSubItem"));
			  claimApplicationExpenseDetailstemp.setFurnitureSubItem(request.getParameter("furnitureSubItem"));

		  }
		  if (request.getParameter("membershipType") != null && !"".equals(request.getParameter("membershipType")) && !request.getParameter("membershipType").equals("-")) {
			  claimApplicationExpenseDetails.setMembershipType(request.getParameter("membershipType"));
			  claimApplicationExpenseDetailstemp.setMembershipType(request.getParameter("membershipType"));

		  }
			if (!"".equals(request.getParameter("overtimeHours")) && !request.getParameter("overtimeHours").equals("-")) {
				claimApplicationExpenseDetails.setOvertimeHours(Double.parseDouble(request.getParameter("overtimeHours")));
				claimApplicationExpenseDetailstemp.setOvertimeHours(Double.parseDouble(request.getParameter("overtimeHours")));
			}
			
			if (!"".equals(request.getParameter("ratePerHour")) && !request.getParameter("ratePerHour").equals("-")) {
				claimApplicationExpenseDetails.setRatePerHour(Double.parseDouble(request.getParameter("ratePerHour")));
				claimApplicationExpenseDetailstemp.setRatePerHour(Double.parseDouble(request.getParameter("ratePerHour")));
			}
			if (!"".equals(request.getParameter("overtimeAmount")) && !request.getParameter("overtimeAmount").equals("-")) {
				claimApplicationExpenseDetails.setOvertimeAmount(Double.parseDouble(request.getParameter("overtimeAmount")));
				claimApplicationExpenseDetailstemp.setOvertimeAmount(Double.parseDouble(request.getParameter("overtimeAmount")));
			}
			if (!"".equals(request.getParameter("overtimeRequestAmount")) && !request.getParameter("overtimeRequestAmount").equals("-")) {
				claimApplicationExpenseDetails.setOvertimeRequestAmount(Double.parseDouble(request.getParameter("overtimeRequestAmount")));
				claimApplicationExpenseDetailstemp.setOvertimeRequestAmount(Double.parseDouble(request.getParameter("overtimeRequestAmount")));
			}
			claimApplicationExpenseDetailstemp.setTempis(true);
			
			

			// claimApplicationExpenseDetails =
			// claimApplicationExpenseDetailsRepo.save(claimApplicationExpenseDetails);
			claimApplicationExpenseDetailstemp = claimApplicationExpenseDetailsTmpRepo.save(claimApplicationExpenseDetailstemp);

	    }
	  
	  
	  
	  
	 
	  
	 //for edit conditon in temp table update 
	  else if((request.getParameter("claimappidedit").equalsIgnoreCase("") ||(request.getParameter("claimappidedit").equalsIgnoreCase("-")))&& expid !=null) {
		  String expidt=request.getParameter("Id");
		  Long expensedttmpidt=Long.parseLong(expidt);
		   claimApplicationExpenseDetailstemp = 
				  claimApplicationExpenseDetailsTmpRepo.findByClaimApplicationExpenseDetailstmpId(expensedttmpidt);
		  
		  if (request.getParameter("expenseItem") != null) {

//				ItemMasterForClaimMatrix claimItem = itemMasterRepository
//						.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
				System.out.println(" result *****" + claimItem);
				claimApplicationExpenseDetailstemp.setExpenseItem(claimItem);
			}

			if (request.getParameter("advanceId") != null) {
				ClaimApplication claimApplication = claimApplicationRepository
						.findByIdAndIsDeleteFalse(Long.parseLong(request.getParameter("expenseItem")));
				claimApplicationExpenseDetailstemp.setAdvanceId(claimApplication);
			}

			if (!"".equals(request.getParameter("expenseBillPeriod"))) {
				if (!request.getParameter("expenseBillPeriod").equals("CalenderBased")) {
					HrmsCode month = hrmsCodeService.findByFieldNameAndCode("MONTHS",
							request.getParameter("expenseBillPeriod"));
					claimApplicationExpenseDetailstemp.setExpenseBillPeriod(month.getDescription());
				} else {
					claimApplicationExpenseDetailstemp.setExpenseBillPeriod("Caldendar Based");
				}
			}

			
			if (!"".equals(request.getParameter("incurredFor")) && !request.getParameter("relationship").equals("self")) {


				System.out.println(" incurredFor *****" + request.getParameter("incurredFor"));
				System.out.println(" relationship *****" + request.getParameter("relationship"));
				String str = request.getParameter("relationship");
				String result = str.split("\\(")[0].trim();
				
				EmpFamilyDtl empFamilyList = empFamilyRepository.findByEmployeeIdAndCompanyIdAndCompanyBranchId(
						Long.parseLong(request.getParameter("empClaim")), companyId, companyBranchId,
						result,request.getParameter("incurredFor").trim());
				
				if(claimItem.getItemName().equals("Over Time Allowance") || claimItem.getItemName().equals("Conveyance")){
					claimApplicationExpenseDetailstemp.setIncurredFor(null);
					
					claimApplicationExpenseDetailstemp
					.setRelationship(null);
				}
				else {
					claimApplicationExpenseDetailstemp.setIncurredFor(empFamilyList);
					
					
					claimApplicationExpenseDetailstemp
					.setRelationship(empFamilyList.getFamilyRelationId().getFamilyRelationName());
				}
				
				
				
			}
			else {
				claimApplicationExpenseDetailstemp.setIncurredFor(null);
			}

			System.out.println("claimMonth if======>>>>>>"+request.getParameter("claimMonth")+"claimYear if=======>"+request.getParameter("claimYear"));


			if (request.getParameter("eventName") != null) {
				claimApplicationExpenseDetails.setEventName(request.getParameter("eventName").trim());
				claimApplicationExpenseDetailstemp.setEventName(request.getParameter("eventName").trim());
			}
		  	if (request.getParameter("claimMonth") != null && !request.getParameter("claimMonth").isBlank() && request.getParameter("claimMonth")!="" && !Objects.equals(request.getParameter("claimMonth"), "null")) {
				claimApplicationExpenseDetails.setClaimMonth(request.getParameter("claimMonth").trim());
				claimApplicationExpenseDetailstemp.setClaimMonth(request.getParameter("claimMonth").trim());
			}
			if (request.getParameter("claimYear") != null && !request.getParameter("claimYear").isBlank() && request.getParameter("claimYear")!="" && !Objects.equals(request.getParameter("claimYear"), "null")) {
				claimApplicationExpenseDetails.setClaimYear(Long.parseLong(request.getParameter("claimYear").trim()));
				claimApplicationExpenseDetailstemp.setClaimYear(Long.parseLong(request.getParameter("claimYear").trim()));
			}
			if (request.getParameter("relationship") == "" || request.getParameter("relationship").equalsIgnoreCase("self")) {
				claimApplicationExpenseDetailstemp.setRelationship("self");
			}

			if (request.getParameter("billNumber") != null) {
				claimApplicationExpenseDetailstemp.setBillNumber(request.getParameter("billNumber").trim());

			}

			if (request.getParameter("billDate") != null && !request.getParameter("billDate").isBlank()  && !request.getParameter("billDate").equalsIgnoreCase("") && !request.getParameter("billDate").equalsIgnoreCase("-")) {
				if(claimItem.getItemName().equals("Over Time Allowance")){
					claimApplicationExpenseDetails.setBillDate(null);
					claimApplicationExpenseDetailstemp.setBillDate(null);
				}
				else {
					claimApplicationExpenseDetails.setBillDate(
							DateUtil.convertStringToDate(request.getParameter("billDate"), DateUtil.IST_DATE_FORMATE));
					claimApplicationExpenseDetailstemp.setBillDate(
							DateUtil.convertStringToDate(request.getParameter("billDate"), DateUtil.IST_DATE_FORMATE));
				}
				
			}else {
				if(claimItem.getItemName().equals("Over Time Allowance") || claimItem.getItemName().equals("Conveyance")){
					claimApplicationExpenseDetails.setBillDate(null);
					claimApplicationExpenseDetailstemp.setBillDate(null);

				}
				else {
					org.joda.time.LocalDate billdate= new org.joda.time.LocalDate();
					claimApplicationExpenseDetails.setBillDate(
							DateUtil.convertStringToDate(billdate.getDayOfMonth()+"/"+billdate.getMonthOfYear()+"/"+billdate.getYear(), DateUtil.IST_DATE_FORMATE));
					claimApplicationExpenseDetailstemp.setBillDate(
							DateUtil.convertStringToDate(billdate.getDayOfMonth()+"/"+billdate.getMonthOfYear()+"/"+billdate.getYear(), DateUtil.IST_DATE_FORMATE));
				}
				
			}

			if ( !"".equals(request.getParameter("fromDate"))  &&   request.getParameter("fromDate") != null) {
				System.out.println("From Date:: " + request.getParameter("fromDate"));

//				ItemMasterForClaimMatrix claimItem = itemMasterRepository
//						.findById(Long.parseLong(request.getParameter("expenseItem"))).get();

				if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")) {
					claimApplicationExpenseDetailstemp.setFromDate(null);
				}
				else{
				
				claimApplicationExpenseDetailstemp.setFromDate(
						DateUtil.convertStringToDate(request.getParameter("fromDate"), DateUtil.IST_DATE_FORMATE));}
			}

			if (request.getParameter("toDate") != null) {
				String date = request.getParameter("toDate");

//				ItemMasterForClaimMatrix claimItem = itemMasterRepository
//						.findById(Long.parseLong(request.getParameter("expenseItem"))).get();

				if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")) {
					claimApplicationExpenseDetailstemp.setToDate(null);
				}else{
				claimApplicationExpenseDetailstemp.setToDate(DateUtil.convertStringToDate(date, DateUtil.IST_DATE_FORMATE));}

			}
			if(!claimItem.getItemName().equals("Over Time Allowance")) {
				if (!"".equals(request.getParameter("expenseIncurredAt"))) {
					claimApplicationExpenseDetailstemp.setExpenseIncurredAt(request.getParameter("expenseIncurredAt").trim());
	
				}
			}

			if (!"".equals(request.getParameter("billValue")) && !request.getParameter("billValue").equals("undefined") && !request.getParameter("billValue").equals("-")) {
				System.out.println("request.getParameter(\"billValue\") "+request.getParameter("billValue"));
				claimApplicationExpenseDetailstemp.setBillValue(Double.parseDouble(request.getParameter("billValue")));

			}
//			ItemMasterForClaimMatrix claimItem = itemMasterRepository
//					.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
			if(claimItem.getItemName().equals("Vehicle Running & Maintenance Expense Four Wheeler") || claimItem.getItemName().equals("Vehicle Running & Maintenance Expense Two Wheeler")) {
				if(request.getParameter("fromDate") != null || request.getParameter("toDate") != null) {
					Date fromDate = DateUtil.convertStringToDate(request.getParameter("fromDate"), DateUtil.IST_DATE_FORMATE);
					Date toDate = DateUtil.convertStringToDate(request.getParameter("toDate"), DateUtil.IST_DATE_FORMATE);
					Calendar calendarfrom = Calendar.getInstance();
		            calendarfrom.setTime(fromDate);
		            int monthfrom = calendarfrom.get(Calendar.MONTH) + 1;
		            String monthfromNumber = String.format("%02d", monthfrom);
		            Calendar calendarto = Calendar.getInstance();
		            calendarto.setTime(toDate);
		            int monthto = calendarto.get(Calendar.MONTH) + 1;
		            String monthtoNumber = String.format("%02d", monthto);
		            System.out.println("monthfrom "+monthfromNumber + "monthto " + monthtoNumber + request.getParameter("fuelType").trim().toUpperCase());
		            VehicleRunningMaintenanceExpense rate=  vehicleRepo.findAllBetweenStartMonthAndEndMonthAndIsDeleteFalse(monthfromNumber,monthtoNumber,request.getParameter("fuelType").trim().toUpperCase());
		            Double requestedvalue = Double.parseDouble(request.getParameter("claimedLtr")) * rate.getRate();
					Double requestedroundedValue = (double) Math.round(requestedvalue);
		            claimApplicationExpenseDetailstemp.setRequestedValue(requestedroundedValue);


					Double expenseAmt = Double.parseDouble(request.getParameter("incurredLtr"))* rate.getRate();
					Double expenseAmtroundedValue = (double) Math.round(expenseAmt);
		            System.out.println("expenseAmt is -----------------------"+expenseAmtroundedValue);
		            claimApplicationExpenseDetailstemp.setTotalBillValue(expenseAmtroundedValue);

				}
			}else {
			
			if (!"".equals(request.getParameter("requestedValue")) && !request.getParameter("requestedValue").equals("undefined") && !request.getParameter("requestedValue").equals("-")) {
				//System.out.println("request.getParameter(\"requestedValue\") "+request.getParameter("requestedValue"));
				
				claimApplicationExpenseDetailstemp
				.setRequestedValue(Double.parseDouble(request.getParameter("requestedValue")));
			}

			if (!"".equals(request.getParameter("totalBillValue"))
					&& !request.getParameter("totalBillValue").equals("-")) {
				
				
				claimApplicationExpenseDetailstemp
				.setTotalBillValue(Double.parseDouble(request.getParameter("totalBillValue")));
			}
			if (!"".equals(request.getParameter("requestedValue")) && !request.getParameter("requestedValue").equals("undefined") && !request.getParameter("requestedValue").equals("-")) {
				//System.out.println("request.getParameter(\"requestedValue\") "+request.getParameter("requestedValue"));
				claimApplicationExpenseDetails
						.setTotalBalancePending(Double.parseDouble(request.getParameter("requestedValue")));
				claimApplicationExpenseDetailstemp
				.setTotalBalancePending(Double.parseDouble(request.getParameter("requestedValue")));
			}
			}

			if (!"".equals(request.getParameter("currency"))) {
				claimApplicationExpenseDetailstemp.setCurrency(request.getParameter("currency").trim());

			}
			
//			String financialYear = request.getParameter("financialYear");
//			System.out.println("financial year is "+financialYear);
//			if (financialYear != null && !"".equals(financialYear)) {
//			    claimApplicationExpenseDetailstemp.setFinancialYear(financialYear.trim());
//			}
			String financialYear = request.getParameter("financialYear");
			System.out.println("financial year is "+financialYear);
			if (financialYear != null && !"".equals(financialYear) && !financialYear.equalsIgnoreCase("null")) {
			    String startYear = financialYear.split("-")[0];
			    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			    String dateString = "01/04/" + startYear;
			    Date fromDate = dateFormat.parse(dateString);
			    			    
			    claimApplicationExpenseDetailstemp.setFromDate(fromDate);
			    
			    String toDateString = "31/03/" + (Integer.parseInt(startYear) + 1);
			    Date toDate = dateFormat.parse(toDateString);
			   
			    claimApplicationExpenseDetailstemp.setToDate(toDate);
			    
			    claimApplicationExpenseDetailstemp.setFinancialYear(request.getParameter("financialYear").trim());
			}
			
			Optional<CompanyMaster> cm = companyMasterRepository.findById(companyId);
			Optional<CompanyBranchMaster> cbm = companyBranchMasterRepository.findById(companyBranchId);
			//System.out.println("request.getParameter(\"file\") : " + request.getParameter("file"));
			System.out.println("fileobjectis"+request.getParameter("fileIde"));
			System.out.println("file is +++++++++++ "+request.getParameter("file"));
          if(request.getParameter("file")==null) {
          	//MultipartFile file="undefined";
          }
			if (request.getParameter("uploadDocument") != null && !request.getParameter("uploadDocument").equals("") && request.getParameter("fileIde")==null  
					)  {
				
			  if(file!=null ) {
				  System.out.println(request.getParameter("uploadDocument").trim());
					FileMaster fileMaster = new FileMaster();
					if (file.getOriginalFilename() != "") {
						fileMaster.setFileName(file.getOriginalFilename());
						fileMaster.setContentType(file.getContentType());
						String fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'),
								file.getOriginalFilename().length());
						fileMaster.setFileType(fileType);
						fileMaster.setCompany(cm.get());
						fileMaster.setCompanyBranch(cbm.get());
						String name = file.getOriginalFilename();
						FileMaster fileMasterClaimApplication = commonUtility.saveFileObject(name, file,
								"/hrms/claimMatrix/ClaimApplication", companyId, companyBranchId);
						System.out.println("fileMasterClaimApplication "+fileMasterClaimApplication.getId());
						
						claimApplicationExpenseDetailstemp.setAttachDocument(fileMasterClaimApplication);

						
					}
					else {
						System.out.println("no action required");
					}

			  }
				

			}
			

			
			System.out.println("request.getParameter(\"exception\")" + request.getParameter("exception"));
			
				claimApplicationExpenseDetailstemp.setException("No");

			

			if (!"".equals(request.getParameter("comments"))) {
				claimApplicationExpenseDetailstemp.setClaimComments(request.getParameter("comments").trim());

			}
			if (!"".equals(request.getParameter("expenseAmount"))) {
				
				claimApplicationExpenseDetailstemp
				.setExpenseAmount(Double.parseDouble(request.getParameter("expenseAmount")));
			}

			if (!"".equals(request.getParameter("billPaymentRequired"))) {
				
				claimApplicationExpenseDetailstemp
				.setBillPaymentRequired(request.getParameter("billPaymentRequired").trim());
			}

			if (!"".equals(request.getParameter("quantityOrAmount"))) {
				
				
				claimApplicationExpenseDetailstemp
				.setQuantityOrAmount(Double.parseDouble(request.getParameter("quantityOrAmount")));
			}

			if (!"".equals(request.getParameter("pendingAmountForApproval"))) {
				
				
				claimApplicationExpenseDetailstemp.setPendingAmountForApproval(
						Double.parseDouble(request.getParameter("pendingAmountForApproval")));
			}

			if (request.getParameter("unitOfMeasurement") != null) {
				claimApplicationExpenseDetailstemp.setUnitOfMeasurement(request.getParameter("unitOfMeasurement").trim());

			}

			if (request.getParameter("natureOfTravel") != null) {
				claimApplicationExpenseDetailstemp.setNatureOfTravel(request.getParameter("natureOfTravel").trim());

			}

			if (!"".equals(request.getParameter("numberOfDays"))) {

				claimItem = itemMasterRepository
						.findById(Long.parseLong(request.getParameter("expenseItem"))).get();

				if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")) {
					claimApplicationExpenseDetailstemp.setNumberOfDays(null);
				}else{

				claimApplicationExpenseDetailstemp.setNumberOfDays(Long.parseLong(request.getParameter("numberOfDays")));
				}

			}

			if (request.getParameter("timeFrom") != null) {

				 claimItem = itemMasterRepository
						.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
				if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")) {
					claimApplicationExpenseDetailstemp.setTimeFrom(null);
				}else{


				claimApplicationExpenseDetailstemp.setTimeFrom(
						DateUtil.convertStringToDate(request.getParameter("timeFrom"), DateUtil.IST_DATE_FORMATE));}
			}

			if (request.getParameter("timeTo") != null) {

				claimItem = itemMasterRepository
						.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
				if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")) {
					claimApplicationExpenseDetailstemp.setTimeTo(null);
				}else{
				
				claimApplicationExpenseDetailstemp.setTimeTo(
						DateUtil.convertStringToDate(request.getParameter("timeTo"), DateUtil.IST_DATE_FORMATE));}
			}

			if (request.getParameter("fuelType") != null) {
				claimApplicationExpenseDetailstemp.setFuelType(request.getParameter("fuelType").trim());

			}

			if (!"".equals(request.getParameter("vehicleRegistrationNo"))) {
				
				claimApplicationExpenseDetailstemp
				.setVehicleRegistrationNo(request.getParameter("vehicleRegistrationNo"));
			}

			if (!"".equals(request.getParameter("advanceTaken")) && !request.getParameter("advanceTaken").equals("-")) {
				
				claimApplicationExpenseDetailstemp
				.setAdvanceTaken(Double.parseDouble(request.getParameter("advanceTaken")));
			}

			if (!"".equals(request.getParameter("netClaimedAmount"))
					&& !request.getParameter("netClaimedAmount").equals("-")) {
				
				claimApplicationExpenseDetailstemp
				.setNetClaimedAmount(Double.parseDouble(request.getParameter("netClaimedAmount")));
			}

			if (request.getParameter("purchasedFrom") != null && !request.getParameter("purchasedFrom").equals("-")) {
				
				claimApplicationExpenseDetailstemp.setPurchasedFrom(request.getParameter("purchasedFrom").trim());

			}

			if (request.getParameter("accountNo") != null && !request.getParameter("accountNo").equals("-")) {
				
				claimApplicationExpenseDetailstemp.setAccountNo(request.getParameter("accountNo").trim());

			}

			if (request.getParameter("vehicleType") != null && !request.getParameter("vehicleType").equals("-")) {
				
				claimApplicationExpenseDetailstemp.setVehicleType(request.getParameter("vehicleType").trim());

			}

			if (!"".equals(request.getParameter("incurredLtr")) && !request.getParameter("incurredLtr").equals("-")) {
				
				claimApplicationExpenseDetailstemp.setIncurredLtr(Double.parseDouble(request.getParameter("incurredLtr")));

			}

			if (!"".equals(request.getParameter("claimedLtr")) && !request.getParameter("claimedLtr").equals("-")) {
				
				claimApplicationExpenseDetailstemp.setClaimedLtr(Double.parseDouble(request.getParameter("claimedLtr")));

			}

			if (!"".equals(request.getParameter("billValueGst")) && !request.getParameter("billValueGst").equals("-")) {
				
				claimApplicationExpenseDetailstemp
				.setBillValueGst(Double.parseDouble(request.getParameter("billValueGst")));
			}

			// if (!"".equals(request.getParameter("totalBillValue"))
			// 		&& !request.getParameter("totalBillValue").equals("-")) {
				
				
			// 	claimApplicationExpenseDetailstemp
			// 	.setTotalBillValue(Double.parseDouble(request.getParameter("totalBillValue")));
			// }
			
			if (!"".equals(request.getParameter("hospitalId")) && !request.getParameter("hospitalId").equals("-")) {
				
				claimApplicationExpenseDetailstemp.setHospitalName(request.getParameter("hospitalId"));

			}
			
			if (!"".equals(request.getParameter("DoctorId")) && !request.getParameter("DoctorId").equals("-")) {
				
				claimApplicationExpenseDetailstemp.setDoctorName(request.getParameter("DoctorId"));

			}
			
			if (request.getParameter("furnitureItem") != null && !"".equals(request.getParameter("furnitureItem")) && !request.getParameter("furnitureItem").equals("-")) {
				claimApplicationExpenseDetailstemp.setFurnitureItem(request.getParameter("furnitureItem"));

			}

		    if (request.getParameter("furnitureSubItem") != null && !"".equals(request.getParameter("furnitureSubItem")) && !request.getParameter("furnitureSubItem").equals("-")) {
			  claimApplicationExpenseDetailstemp.setFurnitureSubItem(request.getParameter("furnitureSubItem"));
		    }

			if (request.getParameter("membershipType") != null && !"".equals(request.getParameter("membershipType")) && !request.getParameter("membershipType").equals("-")) {
			  claimApplicationExpenseDetailstemp.setMembershipType(request.getParameter("membershipType"));
		    }

			if (!"".equals(request.getParameter("overtimeHours")) && !request.getParameter("overtimeHours").equals("-")) {
				claimApplicationExpenseDetailstemp.setOvertimeHours(Double.parseDouble(request.getParameter("overtimeHours")));

			}
			if (!"".equals(request.getParameter("ratePerHour")) && !request.getParameter("ratePerHour").equals("-")) {
				claimApplicationExpenseDetailstemp.setRatePerHour(Double.parseDouble(request.getParameter("ratePerHour")));

			}
			if (!"".equals(request.getParameter("overtimeAmount")) && !request.getParameter("overtimeAmount").equals("-")) {
				claimApplicationExpenseDetailstemp.setOvertimeAmount(Double.parseDouble(request.getParameter("overtimeAmount")));
			}
			if (!"".equals(request.getParameter("overtimeRequestAmount")) && !request.getParameter("overtimeRequestAmount").equals("-")) {
				claimApplicationExpenseDetailstemp.setOvertimeRequestAmount(Double.parseDouble(request.getParameter("overtimeRequestAmount")));

			}
			
			if (request.getParameter("taxableformedical").equalsIgnoreCase("true")
				) {
				claimApplicationExpenseDetailstemp.setTaxableformedical(true);
			} else {
				claimApplicationExpenseDetailstemp.setTaxableformedical(false);
			}
			
			
			claimApplicationExpenseDetailstemp = claimApplicationExpenseDetailsTmpRepo.save(claimApplicationExpenseDetailstemp);

		  
	  }
	  //for edit conditon in main table update 
			  else {
				  String expidt=request.getParameter("Id");
		  System.out.println("check it======>>>>>>"+expidt);
				  Long expensedttmpidt=Long.parseLong(expidt);
		  System.out.println("check it again======>>>>>>"+expensedttmpidt);
				  claimApplicationExpenseDetails = 
						  claimApplicationExpenseDetailsRepo.findByexpneseApplicationId(expensedttmpidt);
				  
				  if (request.getParameter("expenseItem") != null) {
		
//						ItemMasterForClaimMatrix claimItem = itemMasterRepository
//								.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
						System.out.println(" result *****" + claimItem);
						claimApplicationExpenseDetails.setExpenseItem(claimItem);
					}
		
					if (request.getParameter("advanceId") != null) {
						ClaimApplication claimApplication = claimApplicationRepository
								.findByIdAndIsDeleteFalse(Long.parseLong(request.getParameter("expenseItem")));
						claimApplicationExpenseDetails.setAdvanceId(claimApplication);
					}
		
					if (!"".equals(request.getParameter("expenseBillPeriod"))) {
						if (!request.getParameter("expenseBillPeriod").equals("CalenderBased")) {
							HrmsCode month = hrmsCodeService.findByFieldNameAndCode("MONTHS",
									request.getParameter("expenseBillPeriod"));
							claimApplicationExpenseDetails.setExpenseBillPeriod(month.getDescription());
						} else {
							claimApplicationExpenseDetails.setExpenseBillPeriod("Caldendar Based");
						}
					}
		
					
					if (!"".equals(request.getParameter("incurredFor")) && !request.getParameter("relationship").equals("self")) {
		
		
						System.out.println(" incurredFor *****" + request.getParameter("incurredFor"));
						System.out.println(" relationship *****" + request.getParameter("relationship"));
						String str = request.getParameter("relationship");
						String result = str.split("\\(")[0].trim();
						
						EmpFamilyDtl empFamilyList = empFamilyRepository.findByEmployeeIdAndCompanyIdAndCompanyBranchId(
								Long.parseLong(request.getParameter("empClaim")), companyId, companyBranchId,
								result,request.getParameter("incurredFor").trim());

						if(claimItem.getItemName().equals("Over Time Allowance") || claimItem.getItemName().equals("Conveyance")){
							claimApplicationExpenseDetailstemp.setIncurredFor(null);

							claimApplicationExpenseDetailstemp
									.setRelationship(null);
						}
						else {
							claimApplicationExpenseDetails.setIncurredFor(empFamilyList);


							claimApplicationExpenseDetails
									.setRelationship(empFamilyList.getFamilyRelationId().getFamilyRelationName());
						}
						
					}
					else {
						claimApplicationExpenseDetails.setIncurredFor(null);
					}

					System.out.println("claimMonth if======>>>>>>"+request.getParameter("claimMonth")+"claimYear if=======>"+request.getParameter("claimYear"));


					if (request.getParameter("eventName") != null) {
						claimApplicationExpenseDetails.setEventName(request.getParameter("eventName").trim());
						claimApplicationExpenseDetailstemp.setEventName(request.getParameter("eventName").trim());
					}
					if (request.getParameter("claimMonth") != null && !request.getParameter("claimMonth").isBlank() && request.getParameter("claimMonth") !="" && request.getParameter("claimMonth") !="null") {
						claimApplicationExpenseDetails.setClaimMonth(request.getParameter("claimMonth").trim());
						claimApplicationExpenseDetailstemp.setClaimMonth(request.getParameter("claimMonth").trim());
					}
					if (request.getParameter("claimYear") != null && !request.getParameter("claimYear").isBlank() && request.getParameter("claimYear") !="" && request.getParameter("claimYear") !="null") {
						claimApplicationExpenseDetails.setClaimYear(Long.parseLong(request.getParameter("claimYear").trim()));
						claimApplicationExpenseDetailstemp.setClaimYear(Long.parseLong(request.getParameter("claimYear").trim()));
					}
					if (request.getParameter("relationship") == "" || request.getParameter("relationship").equalsIgnoreCase("self")) {
						claimApplicationExpenseDetails.setRelationship("self");
					}
		
					if (request.getParameter("billNumber") != null && !request.getParameter("billNumber").isBlank() && !request.getParameter("billNumber").equalsIgnoreCase("") && !request.getParameter("billNumber").equalsIgnoreCase("-")) {
						claimApplicationExpenseDetails.setBillNumber(request.getParameter("billNumber").trim());
		
					}
		
					if (request.getParameter("billDate") != null && !request.getParameter("billDate").isBlank() && !request.getParameter("billDate").equalsIgnoreCase("") && !request.getParameter("billDate").equalsIgnoreCase("-")) {
						
						if(claimItem.getItemName().equals("Over Time Allowance")){
							claimApplicationExpenseDetails.setBillDate(null);
							claimApplicationExpenseDetailstemp.setBillDate(null);
						}
						else {
							claimApplicationExpenseDetails.setBillDate(
									DateUtil.convertStringToDate(request.getParameter("billDate"), DateUtil.IST_DATE_FORMATE));
							claimApplicationExpenseDetailstemp.setBillDate(
									DateUtil.convertStringToDate(request.getParameter("billDate"), DateUtil.IST_DATE_FORMATE));
						}
						
					}else {
						if(claimItem.getItemName().equals("Over Time Allowance") || claimItem.getItemName().equals("Conveyance")){
							claimApplicationExpenseDetails.setBillDate(null);
							claimApplicationExpenseDetailstemp.setBillDate(null);
						}
						else {
							org.joda.time.LocalDate billdate= new org.joda.time.LocalDate();
							claimApplicationExpenseDetails.setBillDate(
									DateUtil.convertStringToDate(billdate.getDayOfMonth()+"/"+billdate.getMonthOfYear()+"/"+billdate.getYear(), DateUtil.IST_DATE_FORMATE));
							claimApplicationExpenseDetailstemp.setBillDate(
									DateUtil.convertStringToDate(billdate.getDayOfMonth()+"/"+billdate.getMonthOfYear()+"/"+billdate.getYear(), DateUtil.IST_DATE_FORMATE));
						}
						
					}
		
					if (request.getParameter("fromDate") != null) {
						System.out.println("From Date:: " + request.getParameter("fromDate"));

//						ItemMasterForClaimMatrix	claimItem = itemMasterRepository
//								.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
						if(claimItem.getItemName().equals("Entertainment Expenses")|| claimItem.getItemName().equals("Over Time Allowance")) {
							claimApplicationExpenseDetails.setFromDate(null);

						}else {

							claimApplicationExpenseDetails.setFromDate(
									DateUtil.convertStringToDate(request.getParameter("fromDate"), DateUtil.IST_DATE_FORMATE));
						}
					}
		
					if (request.getParameter("toDate") != null) {
						String date = request.getParameter("toDate");

//						ItemMasterForClaimMatrix	claimItem = itemMasterRepository
//								.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
						if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")) {
							claimApplicationExpenseDetails.setToDate(null);

						}else{
						
						claimApplicationExpenseDetails.setToDate(DateUtil.convertStringToDate(date, DateUtil.IST_DATE_FORMATE));}
		
					}
		
					if (!"".equals(request.getParameter("expenseIncurredAt"))) {
						claimApplicationExpenseDetails.setExpenseIncurredAt(request.getParameter("expenseIncurredAt").trim());
		
					}
		
					if (!"".equals(request.getParameter("billValue")) && !request.getParameter("billValue").equals("undefined") && !request.getParameter("billValue").equals("-")) {
						System.out.println("request.getParameter(\"billValue\") "+request.getParameter("billValue"));
						claimApplicationExpenseDetails.setBillValue(Double.parseDouble(request.getParameter("billValue")));
		
					}
					
					if (!"".equals(request.getParameter("overtimeHours"))) {
						claimApplicationExpenseDetails.setOvertimeHours(Double.parseDouble(request.getParameter("overtimeHours").trim()));
						claimApplicationExpenseDetailstemp.setOvertimeHours(Double.parseDouble(request.getParameter("overtimeHours").trim()));
					}
					if (!"".equals(request.getParameter("ratePerHour"))) {
						claimApplicationExpenseDetails.setRatePerHour(Double.parseDouble(request.getParameter("ratePerHour").trim()));
						claimApplicationExpenseDetailstemp.setRatePerHour(Double.parseDouble(request.getParameter("ratePerHour").trim()));
		
					}
					if (!"".equals(request.getParameter("overtimeAmount"))) {
						claimApplicationExpenseDetails.setOvertimeAmount(Double.parseDouble(request.getParameter("overtimeAmount").trim()));
						claimApplicationExpenseDetailstemp.setOvertimeAmount(Double.parseDouble(request.getParameter("overtimeAmount").trim()));
		
					}
					if (!"".equals(request.getParameter("overtimeRequestAmount"))) {
						claimApplicationExpenseDetails.setOvertimeRequestAmount(Double.parseDouble(request.getParameter("overtimeRequestAmount").trim()));
						claimApplicationExpenseDetailstemp.setOvertimeRequestAmount(Double.parseDouble(request.getParameter("overtimeRequestAmount").trim()));
					}
//					ItemMasterForClaimMatrix claimItem = itemMasterRepository
//							.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
					if(claimItem.getItemName().equals("Vehicle Running & Maintenance Expense Four Wheeler") || claimItem.getItemName().equals("Vehicle Running & Maintenance Expense Two Wheeler")) {
						if(request.getParameter("fromDate") != null || request.getParameter("toDate") != null) {
							Date fromDate = DateUtil.convertStringToDate(request.getParameter("fromDate"), DateUtil.IST_DATE_FORMATE);
							Date toDate = DateUtil.convertStringToDate(request.getParameter("toDate"), DateUtil.IST_DATE_FORMATE);
							Calendar calendarfrom = Calendar.getInstance();
				            calendarfrom.setTime(fromDate);
				            int monthfrom = calendarfrom.get(Calendar.MONTH) + 1;
				            String monthfromNumber = String.format("%02d", monthfrom);
				            Calendar calendarto = Calendar.getInstance();
				            calendarto.setTime(toDate);
				            int monthto = calendarto.get(Calendar.MONTH) + 1;
				            String monthtoNumber = String.format("%02d", monthto);
				            System.out.println("monthfrom "+monthfromNumber + "monthto " + monthtoNumber + request.getParameter("fuelType").trim().toUpperCase());
				            VehicleRunningMaintenanceExpense rate=  vehicleRepo.findAllBetweenStartMonthAndEndMonthAndIsDeleteFalse(monthfromNumber,monthtoNumber,request.getParameter("fuelType").trim().toUpperCase());
				            Double requestedvalue = Double.parseDouble(request.getParameter("claimedLtr")) * rate.getRate();
							Double requestedroundedValue = (double) Math.round(requestedvalue);
				            claimApplicationExpenseDetails.setRequestedValue(requestedroundedValue);
		
							Double expenseAmt = Double.parseDouble(request.getParameter("incurredLtr"))* rate.getRate();
							Double expenseAmtroundedValue = (double) Math.round(expenseAmt);
							System.out.println("expenseAmt is -----------------------"+expenseAmtroundedValue);
							claimApplicationExpenseDetails.setTotalBillValue(expenseAmtroundedValue);
		
						}
					}else {
					
					if (!"".equals(request.getParameter("requestedValue")) && !request.getParameter("requestedValue").equals("undefined") && !request.getParameter("requestedValue").equals("-")) {
						//System.out.println("request.getParameter(\"requestedValue\") "+request.getParameter("requestedValue"));
						
						claimApplicationExpenseDetails
						.setRequestedValue(Double.parseDouble(request.getParameter("requestedValue")));
					}

					if (!"".equals(request.getParameter("totalBillValue"))
					&& !request.getParameter("totalBillValue").equals("-")) {
				
				
				claimApplicationExpenseDetails
				.setTotalBillValue(Double.parseDouble(request.getParameter("totalBillValue")));
			}
					if (!"".equals(request.getParameter("requestedValue")) && !request.getParameter("requestedValue").equals("undefined") && !request.getParameter("requestedValue").equals("-")) {
						//System.out.println("request.getParameter(\"requestedValue\") "+request.getParameter("requestedValue"));
						claimApplicationExpenseDetails
								.setTotalBalancePending(Double.parseDouble(request.getParameter("requestedValue")));
						claimApplicationExpenseDetailstemp
						.setTotalBalancePending(Double.parseDouble(request.getParameter("requestedValue")));
					}
					}
					
//					String financialYear = request.getParameter("financialYear");
//					System.out.println("financial year is "+financialYear);
//					if (financialYear != null && !"".equals(financialYear)) {
//					    claimApplicationExpenseDetails.setFinancialYear(financialYear.trim());
//					}
					
					String financialYear = request.getParameter("financialYear");
					System.out.println("financial year is "+financialYear);
					if (financialYear != null && !"".equals(financialYear) && !financialYear.equalsIgnoreCase("null")) {
					    String startYear = financialYear.split("-")[0];
					    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
					    String dateString = "01/04/" + startYear;
					    Date fromDate = dateFormat.parse(dateString);
					    
					    claimApplicationExpenseDetails.setFromDate(fromDate);					    
					    
					    String toDateString = "31/03/" + (Integer.parseInt(startYear) + 1);
					    Date toDate = dateFormat.parse(toDateString);
					    
					    claimApplicationExpenseDetails.setToDate(toDate);
					    
					    claimApplicationExpenseDetails.setFinancialYear(request.getParameter("financialYear").trim());
					}
		
					if (!"".equals(request.getParameter("currency"))) {
						claimApplicationExpenseDetails.setCurrency(request.getParameter("currency").trim());
		
					}
					
					Optional<CompanyMaster> cm = companyMasterRepository.findById(companyId);
					Optional<CompanyBranchMaster> cbm = companyBranchMasterRepository.findById(companyBranchId);
					//System.out.println("request.getParameter(\"file\") : " + request.getParameter("file"));
					System.out.println("fileobjectis"+request.getParameter("fileIde"));
					System.out.println("file is +++++++++++ "+request.getParameter("file"));
		          if(request.getParameter("file")==null) {
		          	//MultipartFile file="undefined";
		          }
					if (request.getParameter("uploadDocument") != null && !request.getParameter("uploadDocument").equals("") && request.getParameter("fileIde")==null  
							)  {
						
					  if(file!=null ) {
						  System.out.println(request.getParameter("uploadDocument").trim());
							FileMaster fileMaster = new FileMaster();
							if (file.getOriginalFilename() != "") {
								fileMaster.setFileName(file.getOriginalFilename());
								fileMaster.setContentType(file.getContentType());
								String fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'),
										file.getOriginalFilename().length());
								fileMaster.setFileType(fileType);
								fileMaster.setCompany(cm.get());
								fileMaster.setCompanyBranch(cbm.get());
								String name = file.getOriginalFilename();
								FileMaster fileMasterClaimApplication = commonUtility.saveFileObject(name, file,
										"/hrms/claimMatrix/ClaimApplication", companyId, companyBranchId);
								System.out.println("fileMasterClaimApplication "+fileMasterClaimApplication.getId());
								
								claimApplicationExpenseDetails.setAttachDocument(fileMasterClaimApplication);
		
								
							}
							else {
								System.out.println("no action required");
							}
		
					  }
						
		
					}
					
		
					
					System.out.println("request.getParameter(\"exception\")" + request.getParameter("exception"));
					
					claimApplicationExpenseDetails.setException("No");
		
					
		
					if (!"".equals(request.getParameter("comments"))) {
						claimApplicationExpenseDetails.setClaimComments(request.getParameter("comments").trim());
		
					}
					if (!"".equals(request.getParameter("expenseAmount"))) {
						
						claimApplicationExpenseDetails
						.setExpenseAmount(Double.parseDouble(request.getParameter("expenseAmount")));
					}
		
					if (!"".equals(request.getParameter("billPaymentRequired"))) {
						
						claimApplicationExpenseDetails
						.setBillPaymentRequired(request.getParameter("billPaymentRequired").trim());
					}
		
					if (!"".equals(request.getParameter("quantityOrAmount"))) {
						
						
						claimApplicationExpenseDetails
						.setQuantityOrAmount(Double.parseDouble(request.getParameter("quantityOrAmount")));
					}
		
					if (!"".equals(request.getParameter("pendingAmountForApproval"))) {
						
						
						claimApplicationExpenseDetails.setPendingAmountForApproval(
								Double.parseDouble(request.getParameter("pendingAmountForApproval")));
					}
		
					if (request.getParameter("unitOfMeasurement") != null) {
						claimApplicationExpenseDetails.setUnitOfMeasurement(request.getParameter("unitOfMeasurement").trim());
		
					}
		
					if (request.getParameter("natureOfTravel") != null) {
						claimApplicationExpenseDetails.setNatureOfTravel(request.getParameter("natureOfTravel").trim());
		
					}
		
					if (!"".equals(request.getParameter("numberOfDays"))) {
							claimItem = itemMasterRepository
								.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
						if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")) {
							claimApplicationExpenseDetails.setNumberOfDays(null);
						}else{

						claimApplicationExpenseDetails.setNumberOfDays(Long.parseLong(request.getParameter("numberOfDays")));}
		
					}
		
					if (request.getParameter("timeFrom") != null) {

						claimItem = itemMasterRepository
								.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
						if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")) {
							claimApplicationExpenseDetails.setTimeFrom(null);
						}else{
						
						claimApplicationExpenseDetails.setTimeFrom(
								DateUtil.convertStringToDate(request.getParameter("timeFrom"), DateUtil.IST_DATE_FORMATE));}
					}
		
					if (request.getParameter("timeTo") != null) {
						claimItem = itemMasterRepository
								.findById(Long.parseLong(request.getParameter("expenseItem"))).get();
						if(claimItem.getItemName().equals("Entertainment Expenses") || claimItem.getItemName().equals("Over Time Allowance")) {
							claimApplicationExpenseDetails.setTimeTo(null);
						}else{

						
						claimApplicationExpenseDetails.setTimeTo(
								DateUtil.convertStringToDate(request.getParameter("timeTo"), DateUtil.IST_DATE_FORMATE));}
					}
		
					if (request.getParameter("fuelType") != null) {
						claimApplicationExpenseDetails.setFuelType(request.getParameter("fuelType").trim());
		
					}
		
					if (!"".equals(request.getParameter("vehicleRegistrationNo"))) {
						
						claimApplicationExpenseDetails
						.setVehicleRegistrationNo(request.getParameter("vehicleRegistrationNo"));
					}
		
					if (!"".equals(request.getParameter("advanceTaken")) && !request.getParameter("advanceTaken").equals("-")) {
						
						claimApplicationExpenseDetails
						.setAdvanceTaken(Double.parseDouble(request.getParameter("advanceTaken")));
					}
		
					if (!"".equals(request.getParameter("netClaimedAmount"))
							&& !request.getParameter("netClaimedAmount").equals("-")) {
						
						claimApplicationExpenseDetails
						.setNetClaimedAmount(Double.parseDouble(request.getParameter("netClaimedAmount")));
					}
		
					if (request.getParameter("purchasedFrom") != null && !request.getParameter("purchasedFrom").equals("-")) {
						
						claimApplicationExpenseDetails.setPurchasedFrom(request.getParameter("purchasedFrom").trim());
		
					}
		
					if (request.getParameter("accountNo") != null && !request.getParameter("accountNo").equals("-")) {
						
						claimApplicationExpenseDetails.setAccountNo(request.getParameter("accountNo").trim());
		
					}
		
					if (request.getParameter("vehicleType") != null && !request.getParameter("vehicleType").equals("-")) {
						
						claimApplicationExpenseDetails.setVehicleType(request.getParameter("vehicleType").trim());
		
					}
		
					if (!"".equals(request.getParameter("incurredLtr")) && !request.getParameter("incurredLtr").equals("-")) {
						
						claimApplicationExpenseDetails.setIncurredLtr(Double.parseDouble(request.getParameter("incurredLtr")));
		
					}
		
					if (!"".equals(request.getParameter("claimedLtr")) && !request.getParameter("claimedLtr").equals("-")) {
						
						claimApplicationExpenseDetails.setClaimedLtr(Double.parseDouble(request.getParameter("claimedLtr")));
		
					}
		
					if (!"".equals(request.getParameter("billValueGst")) && !request.getParameter("billValueGst").equals("-")) {
						
						claimApplicationExpenseDetails
						.setBillValueGst(Double.parseDouble(request.getParameter("billValueGst")));
					}
		
					// if (!"".equals(request.getParameter("totalBillValue"))
					// 		&& !request.getParameter("totalBillValue").equals("-")) {
						
						
					// 	claimApplicationExpenseDetails
					// 	.setTotalBillValue(Double.parseDouble(request.getParameter("totalBillValue")));
					// }
					
					if (!"".equals(request.getParameter("hospitalId")) && !request.getParameter("hospitalId").equals("-")) {
						
						claimApplicationExpenseDetails.setHospitalName(request.getParameter("hospitalId"));
		
					}
					
					if (!"".equals(request.getParameter("DoctorId")) && !request.getParameter("DoctorId").equals("-")) {
						
						claimApplicationExpenseDetails.setDoctorName(request.getParameter("DoctorId"));
		
					}
					
					if (request.getParameter("furnitureItem") != null && !"".equals(request.getParameter("furnitureItem")) && !request.getParameter("furnitureItem").equals("-")) {
						claimApplicationExpenseDetails.setFurnitureItem(request.getParameter("furnitureItem"));

					}
				    if (request.getParameter("furnitureSubItem") != null && !"".equals(request.getParameter("furnitureSubItem")) && !request.getParameter("furnitureSubItem").equals("-")) {
					    claimApplicationExpenseDetails.setFurnitureSubItem(request.getParameter("furnitureSubItem"));

				    } if (request.getParameter("membershipType") != null && !"".equals(request.getParameter("membershipType")) && !request.getParameter("membershipType").equals("-")) {
					    claimApplicationExpenseDetails.setMembershipType(request.getParameter("membershipType"));

				    }
					
					if (!"".equals(request.getParameter("overtimeHours")) && !request.getParameter("overtimeHours").equals("-")) {
						claimApplicationExpenseDetails.setOvertimeHours(Double.parseDouble(request.getParameter("overtimeHours")));

					}
					if (!"".equals(request.getParameter("ratePerHour")) && !request.getParameter("ratePerHour").equals("-")) {
						claimApplicationExpenseDetails.setRatePerHour(Double.parseDouble(request.getParameter("ratePerHour")));

					}
					if (!"".equals(request.getParameter("overtimeAmount")) && !request.getParameter("overtimeAmount").equals("-")) {
						claimApplicationExpenseDetails.setOvertimeAmount(Double.parseDouble(request.getParameter("overtimeAmount")));
					}
					if (!"".equals(request.getParameter("overtimeRequestAmount")) && !request.getParameter("overtimeRequestAmount").equals("-")) {
						claimApplicationExpenseDetails.setOvertimeRequestAmount(Double.parseDouble(request.getParameter("overtimeRequestAmount")));

					}
					
					if (request.getParameter("taxableformedical").equalsIgnoreCase("true")) {
						claimApplicationExpenseDetails.setTaxableformedical(true);
					} else {
						claimApplicationExpenseDetails.setTaxableformedical(false);
					}
					
					
					claimApplicationExpenseDetails = claimApplicationExpenseDetailsRepo.save(claimApplicationExpenseDetails);
				  
			  }
			  } catch (Exception e) {
					e.printStackTrace();
				}
				
				//System.out.println(claimApplicationExpenseDetails.getId());

		//System.out.println(claimApplicationExpenseDetails.getAttachDocument().getId()+" "+ claimApplicationExpenseDetails.getAttachDocument().getActualFileName());
		//return claimApplicationExpenseDetails;
		
		   if((request.getParameter("claimappidedit").equalsIgnoreCase("")) || (request.getParameter("claimappidedit").equalsIgnoreCase("-"))) {
			   return claimApplicationExpenseDetailstemp;
		   }
		   else {
			   return claimApplicationExpenseDetails;
		   }
	}

	private static void addValueToMap(Map<String, List<FileMaster>> map, String key, FileMaster value) {
		// If the key does not exist in the map, create a new list for it
		if (!map.containsKey(key)) {
			map.put(key, new ArrayList<>());
		}
		// Add the value to the list associated with the key
		map.get(key).add(value);
	}
	
	public static boolean hasAnyTrue(Map<Integer, List<Pair<Long, Boolean>>> forwardMap) {
        for (Map.Entry<Integer, List<Pair<Long, Boolean>>> entry : forwardMap.entrySet()) {
            for (Pair<Long, Boolean> pair : entry.getValue()) {
                if (pair.getSecond()) {
                    return true; // Return true if any pair's second value is true
                }
            }
        }
        return false; // Return false if none of the pairs have a second value of true
    }


	@PostMapping("/saveClaimApplication")
	@ResponseBody
	@Transactional
	public String saveClaimApplication(@RequestHeader Map<String, String> headers,

			@ModelAttribute("claimApplicationObj") ClaimApplication claimApplicationObj, HttpServletRequest request,
			@RequestParam(value = "file", required = false) MultipartFile file,
			HttpServletResponse response, BindingResult result, Model model, HttpSession session,
			RedirectAttributes redirectAttributes) throws IOException {
		Long id = (Long) session.getAttribute("id");
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		UserMaster um = (UserMaster) session.getAttribute("usermaster");
		// Long userId = (long) session.getAttribute("userId");
		JsonResponse res = new JsonResponse();
		try {
			if (companyId != null) {
				Optional<CompanyMaster> cm = companyMasterRepository.findById(companyId);
				if (cm.isPresent()) {
					claimApplicationObj.setCompany(cm.get());
				}
			}
			if (companyBranchId != null) {
				Optional<CompanyBranchMaster> cbm = companyBranchMasterRepository.findById(companyBranchId);
				if (cbm.isPresent()) {
					claimApplicationObj.setCompanyBranch(cbm.get());
				}
			}
		     if(request.getParameter("empId")!=null && !request.getParameter("empId").equals("")) {
		    	 System.out.println("emp--------> " + request.getParameter("empId"));
		    	 String employeeid=request.getParameter("empId");
		    	  Long empid=Long.parseLong(employeeid);
		    	  
		    	  Employee emp = employeeRepository.findByIdAndIsDeleteFalseAndCompanyIdAndCompanyBranchId(
		    			  empid, companyId, companyBranchId);
		    	  claimApplicationObj.setEmpClaim(emp);
		     }
		     else if (claimApplicationObj.getEmpClaim().getId() != null) {
				Employee emp = employeeRepository.findByIdAndIsDeleteFalseAndCompanyIdAndCompanyBranchId(
						claimApplicationObj.getEmpClaim().getId(), companyId, companyBranchId);
				System.out.println("emp--------> " + emp);
				claimApplicationObj.setEmpClaim(emp);
			}
		     
		     System.out.println("is true is" +request.getParameter("isEdit"));
				System.out.println("new ======>>>>>>>>"+ request.getParameter("eventName"));
		     System.out.println("is true is" +request.getParameter("claimappid"));
			if (claimApplicationObj.getId() == null &&  request.getParameter("claimappid")==null) {

				claimApplicationObj.setPaymentTo(claimApplicationObj.getPaymentTo());
				claimApplicationObj.setExpenseType(claimApplicationObj.getExpenseType());
				String requestDate1 = request.getParameter("requestDate");
				claimApplicationObj
						.setRequestDate(DateUtil.convertStringToDate(requestDate1, DateUtil.IST_DATE_FORMATE));
				Date date = new Date();
				claimApplicationObj.setRequestDate(date);
				claimApplicationObj
						.setExpenseCategoryOpeningBalance(claimApplicationObj.getExpenseCategoryOpeningBalance());

				//claimApplicationObj.setTotalBalance(claimApplicationObj.getTotalBalance());

				Object[] previousClaimDetails = claimApplicationService.
						getLastClaimData(claimApplicationObj.getEmpClaim().getId(),claimApplicationObj.getExpenseCategory().getId());

				// Log the array length and content
				/*System.out.println("Array Length: " + (previousClaimDetails != null ? previousClaimDetails.length : "null"));
				System.out.println("previousClaimDetails:: "+ Arrays.toString(previousClaimDetails));*/
				/*if(previousClaimDetails.length == 0) {
					claimApplicationObj.setTotalBalance(claimApplicationObj.getTotalBalance());
					System.out.println("-----------Before getting gst amount in claim application with if condition-----------");
					System.out.println("Total Balance:: "+claimApplicationObj.getTotalBalance());
				} else {
					System.out.println("-----------Before getting gst amount in claim application with else condition-----------");
					Double balance = previousClaimDetails[0] != null ? ((Number) previousClaimDetails[0]).doubleValue() : 0.0;
					System.out.println("Balance fetched from data is:: "+balance);
					claimApplicationObj.setTotalBalance(balance - claimApplicationObj.getTotalRequestedAmount());
					System.out.println("Total Balance:: "+claimApplicationObj.getTotalBalance());
				}*/
				claimApplicationObj.setTotalBalance(claimApplicationObj.getTotalBalance());
				claimApplicationObj.setExpensePending(claimApplicationObj.getExpensePending());
				claimApplicationObj.setTotalExpenseAmount(claimApplicationObj.getTotalExpenseAmount());
				claimApplicationObj.setTotalRequestedAmount(claimApplicationObj.getTotalRequestedAmount());
				claimApplicationObj.setAppId(1L);
				claimApplicationObj.setCreatedBy(um.getId());
				claimApplicationObj.setCreatedDate(new Date());
				claimApplicationObj.setIpAddress(request.getRemoteAddr());

				ClaimConfiguration claimConfiguration = claimConfigurationService
						.findById(Long.parseLong(request.getParameter("claimConfigId")));

				claimApplicationObj.setClaimConfigId(claimConfiguration);
				claimApplicationObj.setTotalBalancePending(claimApplicationObj.getTotalBalance());

				ClaimApplication claimApp = claimApplicationRepository.save(claimApplicationObj);
				int i = 0, j = 0;
				String customMap = headers.get("custom-header");
				if (!customMap.equals("{}")) {
					customMap = customMap.substring(1, customMap.length() - 1);
					String[] keyValuePairs = customMap.split(",");
					Map<String, String> map = new LinkedHashMap<>();

					for (String pair : keyValuePairs) {
						String[] entry = pair.split(":");
						map.put(entry[0].trim(), (entry[1].trim()));
					}
					Set<String> keys = map.keySet();

					for (String key : keys) {

						ClaimApplicationExpenseDetails claimItemConfiguration = new ClaimApplicationExpenseDetails();
						String valueAttr = map.get(key).substring(1, map.get(key).length() - 1);
						String[] sds = valueAttr.split("\\|");

						System.out.println("SDS:: " + sds.length);
						String expenseitemtmpid = sds[0];
					//	System.out.println("expenseitemtmpid::"+Long.parseLong(expenseitemtmpid));
						
						
						ClaimApplicationExpenseDetailstmp expenseitemdeailstmp=claimApplicationExpenseDetailsTmpRepo.
								findByClaimApplicationExpenseDetailstmpId(Long.parseLong(expenseitemtmpid));

	//					System.out.println("check 2========>>>>>>"+expenseitemdeailstmp.getEventName());
						claimItemConfiguration.setEventName(expenseitemdeailstmp.getEventName());
						claimItemConfiguration.setClaimMonth(expenseitemdeailstmp.getClaimMonth());
						claimItemConfiguration.setClaimYear(expenseitemdeailstmp.getClaimYear());
						claimItemConfiguration.setYear(expenseitemdeailstmp.getYear());
						claimItemConfiguration.setExpenseItem(expenseitemdeailstmp.getExpenseItem());
						claimItemConfiguration.setAdvanceId(expenseitemdeailstmp.getAdvanceId());
						claimItemConfiguration.setIncurredFor(expenseitemdeailstmp.getIncurredFor());
						claimItemConfiguration.setRelationship(expenseitemdeailstmp.getRelationship());
						claimItemConfiguration.setExpenseBillPeriod(expenseitemdeailstmp.getExpenseBillPeriod());
						claimItemConfiguration.setBillNumber(expenseitemdeailstmp.getBillNumber());
						claimItemConfiguration
									.setBillDate(expenseitemdeailstmp.getBillDate());
						claimItemConfiguration
									.setFromDate(expenseitemdeailstmp.getFromDate());
						claimItemConfiguration
									.setToDate(expenseitemdeailstmp.getToDate());
						claimItemConfiguration.setExpenseIncurredAt(expenseitemdeailstmp.getExpenseIncurredAt());
						claimItemConfiguration.setBillValue(expenseitemdeailstmp.getBillValue());
						claimItemConfiguration.setRequestedValue(expenseitemdeailstmp.getRequestedValue());
						claimItemConfiguration.setTotalBalancePending(expenseitemdeailstmp.getTotalBalancePending());
						claimItemConfiguration.setCurrency(expenseitemdeailstmp.getCurrency());
						claimItemConfiguration.setFinancialYear(expenseitemdeailstmp.getFinancialYear());
						claimItemConfiguration.setException(expenseitemdeailstmp.getException());
						claimItemConfiguration.setClaimComments(expenseitemdeailstmp.getClaimComments());
						claimItemConfiguration.setExpenseAmount(expenseitemdeailstmp.getExpenseAmount());
						claimItemConfiguration.setBillPaymentRequired(expenseitemdeailstmp.getBillPaymentRequired());
						claimItemConfiguration.setQuantityOrAmount(expenseitemdeailstmp.getQuantityOrAmount());
						claimItemConfiguration
									.setPendingAmountForApproval(expenseitemdeailstmp.getPendingAmountForApproval());
						claimItemConfiguration.setUnitOfMeasurement(expenseitemdeailstmp.getUnitOfMeasurement());
						claimItemConfiguration.setNatureOfTravel(expenseitemdeailstmp.getNatureOfTravel());
						claimItemConfiguration.setNumberOfDays(expenseitemdeailstmp.getNumberOfDays());
						claimItemConfiguration
									.setTimeFrom(expenseitemdeailstmp.getTimeFrom());
						claimItemConfiguration
									.setTimeTo(expenseitemdeailstmp.getTimeTo());
						claimItemConfiguration.setFuelType(expenseitemdeailstmp.getFuelType());

						claimItemConfiguration.setVehicleRegistrationNo(expenseitemdeailstmp.getVehicleRegistrationNo());
						claimItemConfiguration.setAdvanceTaken(expenseitemdeailstmp.getAdvanceTaken());
						claimItemConfiguration.setNetClaimedAmount(expenseitemdeailstmp.getNetClaimedAmount());
						claimItemConfiguration.setPurchasedFrom(expenseitemdeailstmp.getPurchasedFrom());
						claimItemConfiguration.setAccountNo(expenseitemdeailstmp.getAccountNo());
						claimItemConfiguration.setVehicleType(expenseitemdeailstmp.getVehicleType());
						claimItemConfiguration.setIncurredLtr(expenseitemdeailstmp.getIncurredLtr());
						claimItemConfiguration.setClaimedLtr(expenseitemdeailstmp.getClaimedLtr());
						claimItemConfiguration.setBillValueGst(expenseitemdeailstmp.getBillValueGst());
						claimItemConfiguration.setTotalBillValue(expenseitemdeailstmp.getTotalBillValue());
						claimItemConfiguration.setWorkingOverTimeHours(expenseitemdeailstmp.getWorkingOverTimeHours());
						claimItemConfiguration.setPerhouramount(expenseitemdeailstmp.getPerhouramount());
						claimItemConfiguration.setMonthlymaximumhour(expenseitemdeailstmp.getMonthlymaximumhour());
						claimItemConfiguration.setOvertimeHours(expenseitemdeailstmp.getOvertimeHours());
						claimItemConfiguration.setRatePerHour(expenseitemdeailstmp.getRatePerHour());
						claimItemConfiguration.setOvertimeAmount(expenseitemdeailstmp.getOvertimeAmount());
						claimItemConfiguration.setOvertimeRequestAmount(expenseitemdeailstmp.getOvertimeRequestAmount());

						claimItemConfiguration
								.setTotalworkinghouramount(expenseitemdeailstmp.getTotalworkinghouramount());
						System.out.println(claimApplicationObj.toString());
						if (claimApplicationObj.getId() != null) {
							claimItemConfiguration.setClaimApplication(claimApplicationObj);
						}
						
						claimItemConfiguration.setHospitalName(expenseitemdeailstmp.getHospitalName());
						claimItemConfiguration.setDoctorName(expenseitemdeailstmp.getDoctorName());
						claimItemConfiguration.setFurnitureItem(expenseitemdeailstmp.getFurnitureItem());
						claimItemConfiguration.setFurnitureSubItem(expenseitemdeailstmp.getFurnitureSubItem());
						claimItemConfiguration.setMembershipType(expenseitemdeailstmp.getMembershipType());

						
						claimItemConfiguration.setTaxableformedical(expenseitemdeailstmp.getTaxableformedical());
						
						//System.out.println(fileId);
						claimItemConfiguration.setAttachDocument(expenseitemdeailstmp.getAttachDocument());
						

						ClaimApplicationExpenseDetails c1 =  claimApplicationExpenseDetailsRepo.save(claimItemConfiguration);
						  claimApplicationExpenseDetailsTmpRepo.deleteById(Long.parseLong(expenseitemtmpid));

						/*System.out.println("-----------Before update total balance-----------");
						System.out.println("Total Balance:: "+claimApplicationObj.getTotalBalance());
						System.out.println("Requested amount:: "+claimItemConfiguration.getRequestedValue());
						System.out.println("GST amount:: "+claimItemConfiguration.getBillValueGst());
						System.out.println("Requested value - GST will be:: "+(claimItemConfiguration.getRequestedValue() - claimItemConfiguration.getBillValueGst()));
						System.out.println("Balance + GST will be:: "+(claimApplicationObj.getTotalBalance() + claimItemConfiguration.getBillValueGst()));*/

						/*if(previousClaimDetails.length == 0) {
							System.out.println("-----------No GST add if prevous claims are not avaialble-----------");
							System.out.println("Total Balance:: "+claimApplicationObj.getTotalBalance());
							claimApplicationObj.setTotalBalance(claimApplicationObj.getTotalBalance());
						} else {
							System.out.println("-----------GST will be added-----------");
							claimApplicationObj.setTotalBalance(claimApplicationObj.getTotalBalance() + claimItemConfiguration.getBillValueGst());
						}

						claimApplicationRepository.save(claimApplicationObj);*/
						
					}

					List<ClaimApplicationExpenseDetails> claimup = claimApplicationExpenseDetailsRepo
							.findAllByClaimApplicationOrderByIdDesc(claimApp);
					
					if(claimup.get(0).getExpenseItem().getItemName().equalsIgnoreCase("Furniture Purchase Claim")) {
//						List<ClaimItemConfiguration> byItemMasterIdAndIsDeleteFalse = claimItemConfigurationRepository.findByItemMasterIdAndIsDeleteFalse(claimApp.getClaimConfigId().getId(), c1.getExpenseItem().getId());
					List<ClaimApplication> claimApplications = claimApplicationService.fetchClaimsForPastFiveYearData(claimup.get(0).getBillDate(),
							claimApp.getExpenseCategory().getId(), claimup.get(0).getExpenseItem().getId(), claimApp.getEmpClaim().getId());
					if(claimApplications != null) {
						for (ClaimApplication c : claimApplications) {
							c.setTotalBalancePending(claimApp.getTotalBalance());
							claimApplicationRepository.save(c);
						}
					}
				}

				}

				


				return "SUCCESS";



			}
			else {
				
				String claimstrid=request.getParameter("claimappid");
				String clmdf=request.getParameter("Id");
				System.out.println("expenseeeeeeeeeeeee"+clmdf);
				Long claimlngid=Long.parseLong(claimstrid);
				Double newTotalBalance=claimApplicationObj.getTotalBalance();
				claimApplicationObj = claimApplicationRepository.findByApplicationId(claimlngid);
				Long wfid=claimApplicationObj.getClaimMatrixAppWorkFlowInstanceEntity();
				
//				WorkflowInstanceEntity wm=workflowinstancerepo.getById(wfid);
//				System.out.println("Workflowdatataaaa+++++++"+wm);
//				
//				Map<Integer, List<Pair<Long, Boolean>>> forwardMap = wm.getForwardMap();
//		        boolean isapprove = hasAnyTrue(forwardMap);
//		        System.out.println("extractedId id is------------"+isapprove);
//		        claimApplicationObj.setApproveornot(isapprove);
		       
				
				claimApplicationObj.setPaymentTo(claimApplicationObj.getPaymentTo());
				claimApplicationObj.setExpenseType(claimApplicationObj.getExpenseType());
				String requestDate1 = request.getParameter("requestDate");
				claimApplicationObj
						.setRequestDate(DateUtil.convertStringToDate(requestDate1, DateUtil.IST_DATE_FORMATE));
				Date date = new Date();
				claimApplicationObj.setRequestDate(date);
				claimApplicationObj
						.setExpenseCategoryOpeningBalance(claimApplicationObj.getExpenseCategoryOpeningBalance());
//				claimApplicationObj.setTotalBalance(claimApplicationObj.getTotalBalance());

				Object[] previousClaimDetails = claimApplicationService.
						getLastClaimData(claimApplicationObj.getEmpClaim().getId(),claimApplicationObj.getExpenseCategory().getId());

				// Log the array length and content
				//System.out.println("Array Length: " + (previousClaimDetails != null ? previousClaimDetails.length : "null"));
				//System.out.println("previousClaimDetails:: "+ Arrays.toString(previousClaimDetails));
				//System.out.println("Total Balance:: "+claimApplicationObj.getTotalBalance());
				/*if(previousClaimDetails.length == 0) {
					claimApplicationObj.setTotalBalance(claimApplicationObj.getTotalBalance());
					System.out.println("-----------Before getting gst amount in claim application with if condition-----------");
					System.out.println("Total Balance:: "+claimApplicationObj.getTotalBalance());
				} else {
					System.out.println("-----------Before getting gst amount in claim application with else condition-----------");
					Double balance = previousClaimDetails[0] != null ? ((Number) previousClaimDetails[0]).doubleValue() : 0.0;
					System.out.println("Balance fetched from data is:: "+balance);
					claimApplicationObj.setTotalBalance(balance - claimApplicationObj.getTotalRequestedAmount());
					System.out.println("Total Balance:: "+claimApplicationObj.getTotalBalance());
				}*/
				claimApplicationObj.setTotalBalance(newTotalBalance);
				claimApplicationObj.setExpensePending(claimApplicationObj.getExpensePending());
				
				String TotalExpenseAmount=request.getParameter("totalExpenseAmount");
				//System.out.println("totalExpenseAmount"+request.getParameter("totalExpenseAmount"));
				Double totexp=Double.parseDouble(TotalExpenseAmount.isBlank() ? "0" : TotalExpenseAmount);
				claimApplicationObj.setTotalExpenseAmount(totexp);
				
				String totalRequestedAmount=request.getParameter("totalRequestedAmount");
				//System.out.println("totalRequestedAmount"+request.getParameter("totalRequestedAmount"));
				Double totreq=Double.parseDouble(totalRequestedAmount.isBlank() ? "0" : totalRequestedAmount);
				claimApplicationObj.setTotalRequestedAmount(totreq);
				
				claimApplicationObj.setAppId(1L);
				claimApplicationObj.setCreatedBy(um.getId());
				claimApplicationObj.setCreatedDate(new Date());
				claimApplicationObj.setIpAddress(request.getRemoteAddr());
           if(!request.getParameter("claimConfigIdedit").equals("") ) {
        	   ClaimConfiguration claimConfiguration = claimConfigurationService
						.findById(Long.parseLong(request.getParameter("claimConfigIdedit")));
        	   claimApplicationObj.setClaimConfigId(claimConfiguration);
           } 
           else {
        	   ClaimConfiguration claimConfiguration = claimConfigurationService
						.findById(Long.parseLong(request.getParameter("claimConfigId")));
        	   claimApplicationObj.setClaimConfigId(claimConfiguration);
        	   
           }
//				ClaimConfiguration claimConfiguration = claimConfigurationService
//						.findById(Long.parseLong(request.getParameter("claimConfigId")));

//				claimApplicationObj.setClaimConfigId(claimConfiguration);
           claimApplicationObj.setTotalBalancePending(claimApplicationObj.getTotalBalance());
				ClaimApplication claimApp = claimApplicationRepository.save(claimApplicationObj);
				
				int i = 0, j = 0;
				String customMap = headers.get("custom-header");
				if (!customMap.equals("{}")) {
					customMap = customMap.substring(1, customMap.length() - 1);
					String[] keyValuePairs = customMap.split(",");
					Map<String, String> map = new LinkedHashMap<>();

					for (String pair : keyValuePairs) {
						String[] entry = pair.split(":");
						map.put(entry[0].trim(), (entry[1].trim()));
					}
					Set<String> keys = map.keySet();

					for (String key : keys) {

						ClaimApplicationExpenseDetails claimItemConfiguration = new ClaimApplicationExpenseDetails();
						String valueAttr = map.get(key).substring(1, map.get(key).length() - 1);
						String[] sds = valueAttr.split("\\|");

						System.out.println("SDS:: " + sds.length);
						String expenseitemtmpid = sds[0];
						System.out.println("expenseitemtmpid::"+Long.parseLong(expenseitemtmpid));
						
						
						Optional<ClaimApplicationExpenseDetails> expidexist = claimApplicationExpenseDetailsRepo.findById(Long.parseLong(expenseitemtmpid));
						Optional<ClaimApplicationExpenseDetailstmp> exptmpidexist =  claimApplicationExpenseDetailsTmpRepo.findByClaimApplicationExpenseDetailstmpIdOptional(Long.parseLong(expenseitemtmpid));
						
						if(exptmpidexist.isPresent()) {
							
						
						
						ClaimApplicationExpenseDetailstmp expenseitemdeailstmp=claimApplicationExpenseDetailsTmpRepo.
								findByClaimApplicationExpenseDetailstmpId(Long.parseLong(expenseitemtmpid));
						if(expenseitemdeailstmp!=null) {
							claimItemConfiguration.setEventName(expenseitemdeailstmp.getEventName());
							claimItemConfiguration.setClaimMonth(expenseitemdeailstmp.getClaimMonth());
							claimItemConfiguration.setClaimYear(expenseitemdeailstmp.getClaimYear());
							claimItemConfiguration.setYear(expenseitemdeailstmp.getYear());
							claimItemConfiguration.setExpenseItem(expenseitemdeailstmp.getExpenseItem());
							claimItemConfiguration.setAdvanceId(expenseitemdeailstmp.getAdvanceId());
							claimItemConfiguration.setIncurredFor(expenseitemdeailstmp.getIncurredFor());
							claimItemConfiguration.setRelationship(expenseitemdeailstmp.getRelationship());
							claimItemConfiguration.setExpenseBillPeriod(expenseitemdeailstmp.getExpenseBillPeriod());
							claimItemConfiguration.setBillNumber(expenseitemdeailstmp.getBillNumber());
							claimItemConfiguration
										.setBillDate(expenseitemdeailstmp.getBillDate());
							claimItemConfiguration
										.setFromDate(expenseitemdeailstmp.getFromDate());
							claimItemConfiguration
										.setToDate(expenseitemdeailstmp.getToDate());
							claimItemConfiguration.setExpenseIncurredAt(expenseitemdeailstmp.getExpenseIncurredAt());
							claimItemConfiguration.setBillValue(expenseitemdeailstmp.getBillValue());
							claimItemConfiguration.setRequestedValue(expenseitemdeailstmp.getRequestedValue());
							claimItemConfiguration.setTotalBalancePending(expenseitemdeailstmp.getTotalBalancePending());
							claimItemConfiguration.setCurrency(expenseitemdeailstmp.getCurrency());
							claimItemConfiguration.setFinancialYear(expenseitemdeailstmp.getFinancialYear());
							claimItemConfiguration.setException(expenseitemdeailstmp.getException());
							claimItemConfiguration.setClaimComments(expenseitemdeailstmp.getClaimComments());
							claimItemConfiguration.setExpenseAmount(expenseitemdeailstmp.getExpenseAmount());
							claimItemConfiguration.setBillPaymentRequired(expenseitemdeailstmp.getBillPaymentRequired());
							claimItemConfiguration.setQuantityOrAmount(expenseitemdeailstmp.getQuantityOrAmount());
							claimItemConfiguration
										.setPendingAmountForApproval(expenseitemdeailstmp.getPendingAmountForApproval());
							claimItemConfiguration.setUnitOfMeasurement(expenseitemdeailstmp.getUnitOfMeasurement());
							claimItemConfiguration.setNatureOfTravel(expenseitemdeailstmp.getNatureOfTravel());
							claimItemConfiguration.setNumberOfDays(expenseitemdeailstmp.getNumberOfDays());
							claimItemConfiguration
										.setTimeFrom(expenseitemdeailstmp.getTimeFrom());
							claimItemConfiguration
										.setTimeTo(expenseitemdeailstmp.getTimeTo());
							claimItemConfiguration.setFuelType(expenseitemdeailstmp.getFuelType());
	
							claimItemConfiguration.setVehicleRegistrationNo(expenseitemdeailstmp.getVehicleRegistrationNo());
							claimItemConfiguration.setAdvanceTaken(expenseitemdeailstmp.getAdvanceTaken());
							claimItemConfiguration.setNetClaimedAmount(expenseitemdeailstmp.getNetClaimedAmount());
							claimItemConfiguration.setPurchasedFrom(expenseitemdeailstmp.getPurchasedFrom());
							claimItemConfiguration.setAccountNo(expenseitemdeailstmp.getAccountNo());
							claimItemConfiguration.setVehicleType(expenseitemdeailstmp.getVehicleType());
							claimItemConfiguration.setIncurredLtr(expenseitemdeailstmp.getIncurredLtr());
							claimItemConfiguration.setClaimedLtr(expenseitemdeailstmp.getClaimedLtr());
							claimItemConfiguration.setBillValueGst(expenseitemdeailstmp.getBillValueGst());
							claimItemConfiguration.setTotalBillValue(expenseitemdeailstmp.getTotalBillValue());
							claimItemConfiguration.setWorkingOverTimeHours(expenseitemdeailstmp.getWorkingOverTimeHours());
							claimItemConfiguration.setPerhouramount(expenseitemdeailstmp.getPerhouramount());
							claimItemConfiguration.setMonthlymaximumhour(expenseitemdeailstmp.getMonthlymaximumhour());
	
							claimItemConfiguration
									.setTotalworkinghouramount(expenseitemdeailstmp.getTotalworkinghouramount());
							System.out.println(claimApplicationObj.toString());
							if (claimApplicationObj.getId() != null) {
								claimItemConfiguration.setClaimApplication(claimApplicationObj);
							}
							
							claimItemConfiguration.setHospitalName(expenseitemdeailstmp.getHospitalName());
							claimItemConfiguration.setDoctorName(expenseitemdeailstmp.getDoctorName());
							claimItemConfiguration.setFurnitureItem(expenseitemdeailstmp.getFurnitureItem());
							claimItemConfiguration.setFurnitureSubItem(expenseitemdeailstmp.getFurnitureSubItem());
							claimItemConfiguration.setMembershipType(expenseitemdeailstmp.getMembershipType());

							claimItemConfiguration.setOvertimeHours(expenseitemdeailstmp.getOvertimeHours());
							claimItemConfiguration.setRatePerHour(expenseitemdeailstmp.getRatePerHour());
							claimItemConfiguration.setOvertimeAmount(expenseitemdeailstmp.getOvertimeAmount());
							claimItemConfiguration.setOvertimeRequestAmount(expenseitemdeailstmp.getOvertimeRequestAmount());
							
							claimItemConfiguration.setTaxableformedical(expenseitemdeailstmp.getTaxableformedical());
							
							//System.out.println(fileId);
							claimItemConfiguration.setAttachDocument(expenseitemdeailstmp.getAttachDocument());
							
	
							ClaimApplicationExpenseDetails c1 =  claimApplicationExpenseDetailsRepo.save(claimItemConfiguration);
							claimApplicationExpenseDetailsTmpRepo.deleteById(Long.parseLong(expenseitemtmpid));
						}

							/*System.out.println("-----------Before update total balance in edit-----------");
							System.out.println("Total Balance:: "+claimApplicationObj.getTotalBalance());
							System.out.println("GST amount:: "+claimItemConfiguration.getBillValueGst());*/

							/*if(previousClaimDetails.length == 0) {
								System.out.println("-----------No GST add if prevous claims are not avaialble-----------");
								System.out.println("Total Balance:: "+claimApplicationObj.getTotalBalance());
							} else {
								System.out.println("-----------GST will be added-----------");
								claimApplicationObj.setTotalBalance(claimApplicationObj.getTotalBalance() + claimItemConfiguration.getBillValueGst());
							}

							claimApplicationRepository.save(claimApplicationObj);*/
						}
						
					}

					List<ClaimApplicationExpenseDetails> claimup = claimApplicationExpenseDetailsRepo
							.findAllByClaimApplicationOrderByIdDesc(claimApp);


					if(claimup.get(0).getExpenseItem().getItemName().equalsIgnoreCase("Furniture Purchase Claim")) {
//						List<ClaimItemConfiguration> byItemMasterIdAndIsDeleteFalse = claimItemConfigurationRepository.findByItemMasterIdAndIsDeleteFalse(claimApp.getClaimConfigId().getId(), c1.getExpenseItem().getId());
					List<ClaimApplication> claimApplications = claimApplicationService.fetchClaimsForPastFiveYearData(claimup.get(0).getBillDate(),
							claimApp.getExpenseCategory().getId(), claimup.get(0).getExpenseItem().getId(), claimApp.getEmpClaim().getId());
					if(claimApplications != null) {
						for (ClaimApplication c : claimApplications) {
							c.setTotalBalancePending(claimApp.getTotalBalance());
							claimApplicationRepository.save(c);
						}
					}
				}


			}
					

					
					


					

				}

				
				return "UPDATE";



			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "ERROR";
	}


//	@GetMapping(value = "/deleteClaimApplication/{id}")
//
//	@ResponseBody
//	public String deleteClaimConfiguration(@PathVariable("id") Long id, HttpSession session,
//			HttpServletResponse response, HttpServletRequest request, Model model) {
//		try {
//			securityChecker.checkMenuAccessPermit("/hrms/claimMatrix/claimApplicationList", request, response,
//					request.getSession(), CommonConstant.DELETE);
//			UserMaster um = (UserMaster) session.getAttribute("usermaster");
//			Long userId = (long) session.getAttribute("userId");
//			Long companyId = (Long) session.getAttribute("companyId");
//			Long companyBranchId = (Long) session.getAttribute("companyBranchId");
//
//			request = commonUtil.setMenuPermissionsInRequest(request, CommonConstant.CLAIM_APPLICATION);
//			model.addAttribute("isDelete", request.getAttribute("deletePermission"));
//
//			if (um == null || companyId == null || companyBranchId == null) {
//				return "redirect:/login";
//			}
//
//			applicationExpenseDetailsRepository.delete(id);
//			
//			auditTrailService.saveAuditTrailData("Employee", "Delete", "Admin", NotificationModule.CLAIM_APPLICATION,
//					NotificationAction.DELETE, "/deleteClaimApplication", userId);
//
//			return "sucess";
//		} catch (Exception e) {
//			e.printStackTrace();
//			return "fail";
//		}
//	}

	/*
	 * @GetMapping(value = "/getBillAmount") public @ResponseBody JsonResponse
	 * getBillAmount( @RequestParam("claimid") Long claimid, @RequestParam("itemid")
	 * Long itemid, HttpSession session) { JsonResponse res = new JsonResponse();
	 * Long companyId = (Long) session.getAttribute("companyId"); Long
	 * companyBranchId = (Long) session.getAttribute("companyBranchId");
	 * 
	 * Long configId = claimConfigurationRepo.findIdByBillItem(companyId,
	 * companyBranchId, claimid, itemid);
	 * 
	 * Object[] cfg = claimConfigurationRepo.
	 * findByCompanyIdAndCompanyBranchIdOrderByEffectiveDateDesc(companyId,
	 * companyBranchId,configId);
	 * 
	 * logger.info("Claim Configuration: " + configId); res.setObj((Object)
	 * configId); return res; }
	 */

	@GetMapping(value = "/getClaimConfiguration")
	public @ResponseBody JsonResponse getBillAmount(@RequestParam("claimid") Long claimid,
			@RequestParam("itemid") Long itemid, HttpSession session) {
		JsonResponse res = new JsonResponse();
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		try {
			res = claimApplicationService.getBillAmount(claimid, itemid, companyId, companyBranchId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	@RequestMapping("/getClaimApplication")
	public @ResponseBody ClaimApplication getClaimApplication(@RequestParam("applicationId") Long applicationId,
			HttpSession session) {
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		System.out.println("Company Id::" + companyId + "Company Branch Id::" + companyBranchId
				+ "Claim Application Id:" + applicationId);
		ClaimApplication claimApplication = null;

		try {
			claimApplication = claimApplicationService.getClaimApplication(applicationId, companyId, companyBranchId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return claimApplication;
	}

	@Async
	private void sendNotification(UserMaster um, Long companyId, Long companyBranchId,
			ClaimMatrixAppWorkFlowInstanceEntity claimMatrixAppWorkFlowInstanceEntity,
			ClaimApplication claimApplication) {

		boolean isParrallel = claimMatrixAppWorkFlowInstanceEntity.getRuleConfigurationMaster().getWorkflowProperties()
				.isParallelApproval();

		Long id = claimMatrixAppWorkFlowInstanceEntity.getId();

		List<Long> toUsers = new ArrayList<>();

		if (isParrallel) {

			/* if parrallel then need to send notification to every level reivewers */
			claimMatrixAppWorkFlowInstanceEntity.getReviewers().stream().map(x -> x.getSecond())
					.forEach(user -> toUsers.addAll(user));

		} else {
			/* if in serial then need to send to first level reivewers */
			claimMatrixAppWorkFlowInstanceEntity.getReviewers().stream().map(x -> x.getSecond()).findFirst()
					.filter(user -> toUsers.addAll(user));

		}

		notificationMasterService.sendNotification(CommonConstant.GENERAL_APPLICATION,
				"Review Claim Matrix Application", "Request For " + "Claim Matrix " + " Application", um,
				userMasterRepository.findAllById(toUsers), "hrms/workflow/widget/claimMatrix/viewClaimApplication/?id="+id, false, companyId,
				companyBranchId);

	}

	private List<Pair<Integer, Set<Long>>> getEmployeeReviwerListForWorkFlowDto(Employee employee,
			WorkflowRuleConfigurationMaster ruleConfigurationMaster) {

		EmpReportingOfficer officers = reportingOfficerRepository.findReviwerByEmpId(employee);

		/*
		 * WorkflowTypeEntity workflowTypeEntity =
		 * workflowTypeRepository.findByTypeId(WorkflowType.LEAVE_APPLICATION)
		 * .orElse(null);
		 */

		List<String> reviewersLevelFromWrofkFlow = ruleConfigurationMaster.getWorkflowProperties().getReviewers();

		List<Pair<Integer, Set<Long>>> orderAndReviwers = new ArrayList<>();

		// System.err.println("officers length" + officers);
		if (reviewersLevelFromWrofkFlow != null) {

			for (int i = 0; i < reviewersLevelFromWrofkFlow.size(); i++) {

				Pair<Integer, Set<Long>> pair = new Pair<>();

				pair.setFirst(i + 1);
				System.out.println("   ---> " + officers.getDh());

				switch (reviewersLevelFromWrofkFlow.get(i)) {
				case "DDO": {
					if (officers.getDdo() != null) {
						pair.setSecond(new HashSet<>(
								Arrays.asList(userMasterRepository.findByEmpId(officers.getDdo().getId()).getId())));
					}
					break;
				}
				case "DH": {
					pair.setSecond(new HashSet<>(

							Arrays.asList(userMasterRepository.findByEmpId(officers.getDh().getId()).getId())));
					break;
				}
				case "HO": {
					pair.setSecond(new HashSet<>(
							Arrays.asList(userMasterRepository.findByEmpId(officers.getHo().getId()).getId())));
					break;
				}
				case "HOD": {
					pair.setSecond(new HashSet<>(
							Arrays.asList(userMasterRepository.findByEmpId(officers.getHod().getId()).getId())));
					break;
				}
				default:

//					WorkflowRoleMaster workflowRoleMaster = workflowRolesMasterRepository
//							.findAllByRoleNameIgnoreCase(reviewersLevelFromWrofkFlow.get(i));
					WorkflowRoleMaster workflowRoleMaster = workflowRolesMasterRepository
							.findFirstByRoleNameIgnoreCaseAndCompanyAndCompanyBranchAndIsDeleteFalse(
									reviewersLevelFromWrofkFlow.get(i), ruleConfigurationMaster.getCompany(),
									ruleConfigurationMaster.getCompanyBranch());

					Set<Long> defaultReviewers = new HashSet<>();
					for (String id : workflowRoleMaster.getUserMasterId()) {
						defaultReviewers.add(Long.parseLong(id));
					}
					pair.setSecond(defaultReviewers);
				}

				orderAndReviwers.add(pair);

			}
		}

		return orderAndReviwers;
	}

	private String callEvent(Long leaveId, String event, String comment, HttpServletRequest request,
			HttpSession session) throws Exception {

		try {

			System.err.println("leaveId : " + leaveId + "\n" + "event : " + event + "\n comment : " + comment);

			UserMaster um = (UserMaster) session.getAttribute("usermaster");

			Long companyId = (Long) session.getAttribute("companyId");
			Long companyBranchId = (Long) session.getAttribute("companyBranchId");

			PassEventDto dto = new PassEventDto();

			dto.setActionBy(um.getId());
			dto.setEvent(event);
			dto.setWorkflowType(WorkflowType.CLAIM_MATRIX_APPLICATION);
			dto.setWorkflowInstanceId(leaveId);
			dto.setComment(comment);

			System.err.println("Call Event DTO : " + dto);
			List<EventResponseDto> responsedto = claimMatrixAppService.passEventToSM(dto);

			System.err.println("RESULT DTO : " + responsedto);

			if (responsedto.size() != 0) {

				return "SUCCESS";
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}

		throw new Exception();

	}

	@GetMapping(value = "/claimMatrix/forCheckingEmployeeAssignWorkflowOrNot")
	@ResponseBody
	public Boolean forCheckingEmployeeAssignWorkflowOrNot(HttpServletRequest request, Model model,
			HttpServletResponse response, HttpSession session, @RequestParam("employeeId") Long employeeId) {
		try {

			// UserMaster um = (UserMaster) session.getAttribute("usermaster");
			Long companyId = (Long) session.getAttribute("companyId");
			Long companyBranchId = (Long) session.getAttribute("companyBranchId");

			WorkflowRuleConfigurationMaster ruleConfigurationMaster = configuratiionMasterRepository
					.findByWorkflowTypeAndEmployeeIdToGetLatestEmployeeRule(companyId, companyBranchId,
							WorkflowType.CLAIM_MATRIX_APPLICATION.getTypeId(),
							userMasterRepository.findByEmpId(employeeId).getId().toString());
			if (ruleConfigurationMaster != null) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@RequestMapping("getEmpDetails")
	public @ResponseBody JsonResponse getEmpDetails(@RequestParam("empId") Long empId, HttpSession session) {
		logger.info("Employee Id:: " + empId);
		JsonResponse res = new JsonResponse();
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		try {
			res = claimApplicationService.getEmpDetails(empId, companyId, companyBranchId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	@ResponseBody
	@GetMapping(value = "/toCheckEmpApplicableOrNotForClaim")
	public JsonResponse toCheckEmpApplicableOrNotForClaim(HttpServletRequest request, HttpSession session) {

		JsonResponse response = new JsonResponse();
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		try {
			Long employeeId = 0L;
			String empId = request.getParameter("employeeId");
			if (StringUtil.isNotEmpty(empId)) {
				employeeId = Long.parseLong(empId);
			}
			response = claimApplicationService.toCheckEmpApplicableOrNotForClaim(companyId, companyBranchId,
					employeeId);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}

	@ResponseBody
	@GetMapping(value = "/getVehicleExpenseDetails")
	public VehicleRunningMaintenanceExpense getVehicleExpenseDetails(@RequestParam("month") String month,
			@RequestParam("year") Long year, @RequestParam("expenseItem") Long expenseItem,
			@RequestParam("expenseCategory") Long expenseCategory, HttpSession session) {
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		VehicleRunningMaintenanceExpense vehicleRunningMaintenanceExpense = null;
		try {
			vehicleRunningMaintenanceExpense = claimApplicationService.getVehicleExpenseDetails(month, year, companyId,
					companyBranchId, expenseItem, expenseCategory);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vehicleRunningMaintenanceExpense;
	}

	@ResponseBody
	@GetMapping(value = "/getOvertimeListClaim")
	public List<LeaveManualAttendanceMaster> getOvertimeListClaim(Model model, HttpServletRequest request,
			final RedirectAttributes redirectAttributes, HttpSession session) {
		// logger.info("OvertimeApplicationController.getOvertimeList()");
		List<LeaveManualAttendanceMaster> attendanceMasters = new ArrayList<>();
		try {
			UserMaster um = (UserMaster) session.getAttribute("usermaster");
			Long companyId = (Long) session.getAttribute("companyId");
			Long companyBranchId = (Long) session.getAttribute("companyBranchId");

			System.out.println("From Date:" + request.getParameter("fromDate"));
			System.out.println("To Date:" + request.getParameter("toDate"));

			String IST_DATE_FORMATE = "dd/MM/yyyy";
			String MY_DATE_FORMATE = "yyyy-MM-dd hh:mm:ss";

			String fromDate = DateUtil.changeStringDateFormat(request.getParameter("fromDate"), IST_DATE_FORMATE,
					MY_DATE_FORMATE);
			String toDate = DateUtil.changeStringDateFormat(request.getParameter("toDate"), IST_DATE_FORMATE,
					MY_DATE_FORMATE);
			String EmployeeName = request.getParameter("empClaim");
			// sys

			// System.err.println("==> "+request.getParameter("empClaim" +EmployeeName));

			Long employeeId = employeeRepository.findById(Long.parseLong(request.getParameter("empClaim"))).orElse(null)
					.getId();

			attendanceMasters = leaveManualAttendanceMasterRepository.findByOvertime(employeeId,
					DateUtil.convertStringToDate(fromDate, DateUtil.SDF1),
					DateUtil.convertStringToDate(toDate, DateUtil.SDF1), companyId, companyBranchId);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return attendanceMasters;
	}

	@GetMapping("/getEmpSalary")
	public @ResponseBody EmpSalaryDtl getEmpSalaryDetail(@RequestParam("empId") Long empId, HttpServletRequest request,
			HttpSession session) {

		logger.info("getEmpSalaryDetail called");
		@SuppressWarnings("deprecation")
		EmpSalaryDtl empSalary = empSalaryDtlRepository.findByEmp(employeeRepository.getById(empId));

		return empSalary;
	}

	@GetMapping("/getOverTimeFromClaim")
	public @ResponseBody ClaimApplicationExpenseDetails getOverTimeFromClaim(@RequestParam("empId") Long empId,
			@RequestParam("claimApplicationId") Long claimApplicationId, HttpServletRequest request,
			HttpSession session) {

		logger.info("getEmpSalaryDetail called");
		@SuppressWarnings("deprecation")
		ClaimApplicationExpenseDetails empovertime = claimApplicationExpenseDetailsRepo
				.findByClaimApplication(claimApplicationId);

		return empovertime;
	}

	@ResponseBody

	@GetMapping(value = "/getClaimType")
	public List<ClaimConfiguration> getClaimType(HttpServletRequest request, HttpSession session,
			@RequestParam("status") String status) {
		List<ClaimConfiguration> claimConfigurationList = new ArrayList<>();
		logger.info("ApplicationDetailsController.getAdvanceType()");
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		try {
			if (status.equals("ALL")) {
				claimConfigurationList = claimConfigurationRepository.findEmployeeForApplicationDetails(true, true,
						true, companyId, companyBranchId);
			} else if (status.equals("BOTHPERMDEP")) {
				claimConfigurationList = claimConfigurationRepository.findEmployeeForApplicationDetails(true, false,
						true, companyId, companyBranchId);
			} else if (status.equals("BOTHPERMPRB")) {
				claimConfigurationList = claimConfigurationRepository.findEmployeeForApplicationDetails(true, true,
						false, companyId, companyBranchId);
			} else if (status.equals("BOTH")) {
				claimConfigurationList = claimConfigurationRepository.findEmployeeForApplicationDetails(false, true,
						true, companyId, companyBranchId);
			} else if (status.equals("PRB")) {
				claimConfigurationList = claimConfigurationRepository.findEmployeeForApplicationDetails(false, true,
						false, companyId, companyBranchId);
			} else if (status.equals("DEP")) {
				claimConfigurationList = claimConfigurationRepository.findEmployeeForApplicationDetails(false, false,
						true, companyId, companyBranchId);
			} else if (status.equals("REG")) {
				claimConfigurationList = claimConfigurationRepository.findEmployeeForApplicationDetails(true, false,
						false, companyId, companyBranchId);
			} else if (status.equals("NODATA")) {

			} else {
				claimConfigurationList = claimConfigurationRepository.findEmployeeForApplicationDetails(false, false,
						false, companyId, companyBranchId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return claimConfigurationList;
	}

	@RequestMapping("/getClaimItemsFromConfig")
	public @ResponseBody List<ClaimItemConfiguration> getClaimItemsFromConfig(HttpServletRequest request,
			HttpSession session) {
		List<ClaimItemConfiguration> claimConfigurationList = new ArrayList<>();
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		// Long itemconfigId = request.getParameter("relationship");
		try {

			String status = request.getParameter("empStatus");
			String expenseCategory = request.getParameter("expenseCategory");

			ClaimConfiguration claimConfigurationEmpList = new ClaimConfiguration();
			// claimItemList =
			// claimConfigurationRepository.findAllByClaimCategoryFromConfigAndCompanyIdAndBranchIdAndIsDeleteFalse(Long.parseLong(claimTypeId),
			// companyId, companyBranchId);

			if (status.equals("ALL")) {
				claimConfigurationEmpList = claimConfigurationRepository.findEmployeeForApplicationDetailsWithId(
						Long.parseLong(expenseCategory), true, true, true, companyId, companyBranchId);
			} else if (status.equals("BOTHPERMDEP")) {
				claimConfigurationEmpList = claimConfigurationRepository.findEmployeeForApplicationDetailsWithId(
						Long.parseLong(expenseCategory), true, false, true, companyId, companyBranchId);
			} else if (status.equals("BOTHPERMPRB")) {
				claimConfigurationEmpList = claimConfigurationRepository.findEmployeeForApplicationDetailsWithId(
						Long.parseLong(expenseCategory), true, true, false, companyId, companyBranchId);
			} else if (status.equals("BOTH")) {
				claimConfigurationEmpList = claimConfigurationRepository.findEmployeeForApplicationDetailsWithId(
						Long.parseLong(expenseCategory), false, true, true, companyId, companyBranchId);
			} else if (status.equals("PRB")) {
				claimConfigurationEmpList = claimConfigurationRepository.findEmployeeForApplicationDetailsWithId(
						Long.parseLong(expenseCategory), false, true, false, companyId, companyBranchId);
			} else if (status.equals("DEP")) {
				claimConfigurationEmpList = claimConfigurationRepository.findEmployeeForApplicationDetailsWithId(
						Long.parseLong(expenseCategory), false, false, true, companyId, companyBranchId);
			} else if (status.equals("REG")) {
				claimConfigurationEmpList = claimConfigurationRepository.findEmployeeForApplicationDetailsWithId(
						Long.parseLong(expenseCategory), true, false, false, companyId, companyBranchId);
			} else if (status.equals("NODATA")) {
			} else {
				claimConfigurationEmpList = claimConfigurationRepository.findEmployeeForApplicationDetailsWithId(
						Long.parseLong(expenseCategory), false, false, false, companyId, companyBranchId);
			}

			claimConfigurationList = claimconfigurationrepo
					.findAllByClaimCategoryFromConfigAndCompanyIdAndBranchIdAndIsDeleteFalse(
							claimConfigurationEmpList.getId(), companyId, companyBranchId);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return claimConfigurationList;
	}

	@ResponseBody
	@GetMapping(value = "/getMonthlyCalculation")
	public List<ClaimApplicationExpenseDetails> getMonthlyCalculation(Model model, HttpServletRequest request,
			final RedirectAttributes redirectAttributes, HttpSession session) {
		// logger.info("OvertimeApplicationController.getOvertimeList()");
		// List<LeaveManualAttendanceMaster> attendanceMasters = new ArrayList<>();
		List<ClaimApplicationExpenseDetails> montlycalcutationforclaim = new ArrayList<>();
		try {
			UserMaster um = (UserMaster) session.getAttribute("usermaster");
			Long companyId = (Long) session.getAttribute("companyId");
			Long companyBranchId = (Long) session.getAttribute("companyBranchId");

			String IST_DATE_FORMATE = "dd/MM/yyyy";
			String MY_DATE_FORMATE = "yyyy-MM-dd hh:mm:ss";
			/*
			 * String expensebillPerioddate =
			 * DateUtil.changeStringDateFormat(request.getParameter("month"),
			 * IST_DATE_FORMATE, MY_DATE_FORMATE); String yearforbillperiod =
			 * DateUtil.changeStringDateFormat(request.getParameter("year"),
			 * IST_DATE_FORMATE, MY_DATE_FORMATE);
			 */

			String EmployeeName = request.getParameter("empClaim");
			String expenseItem = request.getParameter("expenseItem");
			String expenseCategory = request.getParameter("expenseCategory");
			String billPeriodMonth = request.getParameter("month");
			String billPeriodYear = request.getParameter("year");

			Long employeeId = employeeRepository.findById(Long.parseLong(request.getParameter("empClaim"))).orElse(null)
					.getId();

			System.out.println("======================================");
			System.out.println("expenseItem::" + Long.parseLong(expenseItem));
			System.out.println("Emp::" + employeeId);
			System.out.println("Expense Category::" + Long.parseLong(expenseCategory));
			System.out.println("Bill Period Month::" + billPeriodMonth);
			System.out.println("\n Bill Year::" + billPeriodYear);

			montlycalcutationforclaim = claimApplicationExpenseDetailsRepo.findByCompanyIdAndCompanyBranchId(
					Long.parseLong(expenseItem), Long.parseLong(request.getParameter("empClaim")),
					Long.parseLong(expenseCategory), billPeriodMonth, billPeriodYear);

			System.out.println("monthly check" + montlycalcutationforclaim);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return montlycalcutationforclaim;
	}

	@RequestMapping("/getBillDate")
	public @ResponseBody ClaimBean getBillDate(@RequestParam("expenseCategory") Long expenseCategory,
			@RequestParam("expenseItem") Long expenseItemId, @RequestParam("empId") Long empId, HttpSession session) {
		logger.info("Expense Category Id:: " + expenseCategory);
		logger.info("Expense Item Id:: " + expenseItemId);
		ClaimBean cb = new ClaimBean();

		try {
			Object[] claimApplication = claimApplicationService.getBillDate(expenseCategory, expenseItemId, empId);

			// Check if the array is not null and has at least one element
			if (claimApplication != null && claimApplication.length > 0) {
				cb.setBillDate(DateUtil.convertDateToString((Date) claimApplication[0], DateUtil.IST_DATE_FORMATE));
			} else {
				// Handle the case where the array is empty
				logger.error("Empty or null array returned from claimApplicationService.getBillDate");
				// You can set a default value or take appropriate action here
			}
		} catch (Exception e) {
			// Handle other exceptions
			e.printStackTrace();
			// You may want to log the exception or take appropriate action
		}
		return cb;
	}

	@GetMapping(value = "/findTotalBalanceForOverTimeByEmpIdAndExpanceId")
	public @ResponseBody JsonResponse findTotalBalanceForOverTimeByEmpIdAndExpanceId(
			@RequestParam("expenseBillPeriod") String expenseBillPeriod, @RequestParam("year") String year,
			@RequestParam("expenseItemId") Long expenseItemId, @RequestParam("empId") Long empId, Model model,
			HttpServletRequest request, final RedirectAttributes redirectAttributes, HttpSession session) {
		JsonResponse res = new JsonResponse();
		try {
			Double amount = claimApplicationExpenseDetailsRepo
					.findTotalBalanceForOverTimeByEmpIdAndExpanceId(expenseItemId, empId, expenseBillPeriod, year);

			res.setObj(amount);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	
	@RequestMapping("/getClaimItem")
	public @ResponseBody List<ItemMasterForClaimMatrix> getClaimItem(HttpServletRequest request,
			HttpSession session) {
		List<ItemMasterForClaimMatrix> claimItemList = new ArrayList<>();
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		try {
			if(request.getParameter("expenseCategory") != null && !request.getParameter("expenseCategory").equals("0")){
				claimItemList = claimApplicationService.getClaimItem(Long.parseLong(request.getParameter("expenseCategory")), companyId, companyBranchId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return claimItemList;
	}

	@PostMapping("/searchClaimApplication")
	public String searchClaimApplication(@RequestParam(value = "rowItems", defaultValue = "50") int rowItems,
			@RequestParam(value = "ipageId", defaultValue = "0") int ipageId,
			@RequestParam(value = "opageId", defaultValue = "0") int opageId,
			HttpServletRequest request, Model model,
			HttpServletResponse response, HttpSession session) {
		try {
			Long userId = (long) session.getAttribute("userId");
			UserMaster um = (UserMaster) session.getAttribute("usermaster");
			Long companyId = (Long) session.getAttribute("companyId");
			Long companyBranchId = (Long) session.getAttribute("companyBranchId");
			// securityChecker.checkMenuAccessPermit("/hrms/tds/ageGroupList", request,
			// response, request.getSession(),CommonConstant.VIEW);

			if (um == null || companyId == null || companyBranchId == null) {
				return "redirect:/signin";
			}

			int start = 0;
			int end = 0;
			Pageable pageable = PageRequest.of(ipageId, rowItems, Sort.by("id").descending());
			if (opageId == 0) {
				pageable = PageRequest.of(0, rowItems, Sort.by(Sort.Direction.DESC, "id"));
				start = 1;
				end = rowItems;
			} else {
				pageable = PageRequest.of(opageId - 1, rowItems, Sort.by(Sort.Direction.DESC, "id"));
				start = (rowItems * (opageId - 1) + 1);
				end = rowItems * opageId;
			}

			// request = commonUtil.setMenuPermissionsInRequest(request,
			// CommonConstant.AGE_GROUP_MASTER);

			// model.addAttribute("isAdd", request.getAttribute("addPermission"));
			// model.addAttribute("isEdit", request.getAttribute("editPermission"));
			// model.addAttribute("isView", request.getAttribute("viewPermission"));
			// model.addAttribute("isDelete", request.getAttribute("deletePermission"));

//			Page<ClaimApplication> claimapplicationsearch = claimApplicationRepository
//					.search(request.getParameter("name"), companyId, companyBranchId, pageable);
			
			Page<ClaimApplicationExpenseDetails> claimapplicationsearch = claimApplicationService
					.search(!request.getParameter("expenseCategory").equals("0")?Long.parseLong(request.getParameter("expenseCategory")):0, 
							request.getParameter("name"),
							request.getParameter("empCode"),
							!request.getParameter("expenseItem").equals("")?Long.parseLong(request.getParameter("expenseItem")):0,
							!request.getParameter("claimAppNo").equals("")?Long.parseLong(request.getParameter("claimAppNo")):0,
							request.getParameter("appStatus"), 
							DateUtil.convertStringToDate(request.getParameter("requestFromDate"),DateUtil.IST_DATE_FORMATE), 
							DateUtil.convertStringToDate(request.getParameter("requestToDate"),DateUtil.IST_DATE_FORMATE), 
							companyId, companyBranchId, um, pageable);
			if (end > claimapplicationsearch.getTotalElements()) {
				end = (int) claimapplicationsearch.getTotalElements();
			}
			
			List<ClaimApplicationExpenseDetails> claimapplicationsearch1 = claimApplicationService
					.search(!request.getParameter("expenseCategory").equals("0")?Long.parseLong(request.getParameter("expenseCategory")):0, 
							request.getParameter("name"),
							request.getParameter("empCode"),
							!request.getParameter("expenseItem").equals("")?Long.parseLong(request.getParameter("expenseItem")):0,
							!request.getParameter("claimAppNo").equals("")?Long.parseLong(request.getParameter("claimAppNo")):0,
							request.getParameter("appStatus"), 
							DateUtil.convertStringToDate(request.getParameter("requestFromDate"),DateUtil.IST_DATE_FORMATE), 
							DateUtil.convertStringToDate(request.getParameter("requestToDate"),DateUtil.IST_DATE_FORMATE), 
							companyId, companyBranchId, um); 
			for (ClaimApplicationExpenseDetails claimApplicationExpenseDetails : claimapplicationsearch) {
				if(claimApplicationExpenseDetails.getClaimApplication().getConfirmStatus() != null && claimApplicationExpenseDetails.getClaimApplication().getConfirmStatus() != "") {
					 if(claimApplicationExpenseDetails.getClaimApplication().getConfirmStatus().equals("UNDER_REVIEW")) {
						 List<WorkflowEventLogEntity> pendingWithUsername = workflowUtils.getLogsByApplicationId(claimApplicationExpenseDetails.
									getClaimApplication().getClaimMatrixAppWorkFlowInstanceEntity(), request, session);
						 List<WorkflowEventLogEntity> pendingWithUsername1=new ArrayList<WorkflowEventLogEntity>();
						 pendingWithUsername1.add(pendingWithUsername.get(0));
						 claimApplicationExpenseDetails.setPendingWithUser(String.join(", ", pendingWithUsername1.get(0).getDisplay_name().toString()));
					 }
				}
			}
			
			for (ClaimApplicationExpenseDetails claimApplicationExpenseDetails : claimapplicationsearch1) {
				if(claimApplicationExpenseDetails.getClaimApplication().getConfirmStatus() != null && claimApplicationExpenseDetails.getClaimApplication().getConfirmStatus() != "") {
					 if(claimApplicationExpenseDetails.getClaimApplication().getConfirmStatus().equals("UNDER_REVIEW")) {
						 List<WorkflowEventLogEntity> pendingWithUsername = workflowUtils.getLogsByApplicationId(claimApplicationExpenseDetails.
									getClaimApplication().getClaimMatrixAppWorkFlowInstanceEntity(), request, session);
						 List<WorkflowEventLogEntity> pendingWithUsername1=new ArrayList<WorkflowEventLogEntity>();
						 pendingWithUsername1.add(pendingWithUsername.get(0));
						 claimApplicationExpenseDetails.setPendingWithUser(String.join(", ", pendingWithUsername1.get(0).getDisplay_name().toString()));
					 }
				}
			}
			
			List<Long> uniqueApplicationIds = claimapplicationsearch1.stream()
				    .map(detail -> detail.getClaimApplication().getId()) 
				    .distinct() 
				    .collect(Collectors.toList());
			
			 List<Object[]> result = claimApplicationExpenseDetailsRepository.getSumApprovedAmount(uniqueApplicationIds);
			    List<ClaimApprovedSum> claimApprovedSums = new ArrayList<>();

			    for (Object[] row : result) {
			        Long claimApplicationId = ((Number) row[0]).longValue(); // Assuming the claim_application is Long
			        Double approvedAmountL2 = 0D;
			        if(row[1] != null)
			        	approvedAmountL2=((Number) row[1]).doubleValue(); // Assuming approved_amount_l2 is Double

			        ClaimApprovedSum claimApprovedSum = new ClaimApprovedSum(claimApplicationId, approvedAmountL2);
			        claimApprovedSums.add(claimApprovedSum);
			    }

			
			Map<Long,String> m=new HashMap<>();
			for(ClaimApprovedSum c:claimApprovedSums) {
				m.put(c.getClaimApplication(), c.getApprovedAmountL2() == 0D ?"-":c.getApprovedAmountL2()+"");
			}
			model.addAttribute("approvedamountl2map",m);
			
			System.out.println("approvedamountl2map is "+m);
			
			// Convert requestFromDate and requestToDate to the desired format for the model
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	        String formattedRequestFromDate = request.getParameter("requestFromDate").formatted(formatter);
	        String formattedRequestToDate = request.getParameter("requestToDate").formatted(formatter);

	        model.addAttribute("requestFromDate", formattedRequestFromDate);
	        model.addAttribute("requestToDate", formattedRequestToDate);

			model.addAttribute("selName", request.getParameter("name"));
			model.addAttribute("expenseCategory", request.getParameter("expenseCategory"));
			model.addAttribute("empCode", request.getParameter("empCode"));
			model.addAttribute("claimAppNo", request.getParameter("claimAppNo"));
			
			logger.info("claimapplicationsearch: {}", claimapplicationsearch);

			// model.addAttribute("claimApplicationList1", claimapplicationsearch);

			model.addAttribute("claimApplicationList1", claimapplicationsearch1);
			model.addAttribute("claimApplicationList", claimapplicationsearch.getContent());
			model.addAttribute("oTotalPages", claimapplicationsearch.getTotalPages());
			model.addAttribute("oTotalElements", claimapplicationsearch.getTotalElements());
			model.addAttribute("orowItems", rowItems);
			model.addAttribute("opageId", opageId);
			model.addAttribute("ostartPage", (opageId == 0) ? 1 : opageId);
			model.addAttribute("oStart", start);
			model.addAttribute("oEnd", end);
			model.addAttribute("listSizeDropDown", PaginationUtil.getShowPageList());
			model.addAttribute("rowItems", rowItems);

			// UNDER_REVIEW,APPROVED,REJECTED,CANCELED
			List<HrmsCode> applicationStatus = hrmsCodeService.findByFieldName("APPLICATION_STATUS");
			model.addAttribute("applicationStatus", applicationStatus);
			model.addAttribute("status", request.getParameter("appStatus") == null ? "status" : request.getParameter("appStatus"));
						
			//Added to load default claims ln list page
			List<ClaimCategoryModel> claims = claimCategoryMasterRepo
					.findAllByCompanyIdAndCompanyBranchIdAndIsDeleteFalse(companyId, companyBranchId);
			model.addAttribute("claims", claims);
			
			List<ItemMasterForClaimMatrix> expenseItemList = new ArrayList<>();
			if(!request.getParameter("expenseCategory").equals("0") && !request.getParameter("expenseCategory").equals("")) {
				expenseItemList = claimApplicationService.getClaimItems(Long.parseLong(request.getParameter("expenseCategory")), companyId,
						companyBranchId);
			}
			model.addAttribute("expenseItemSearch", expenseItemList);
			if(request.getParameter("expenseItem") != null && !request.getParameter("expenseCategory").equals("0")){
				model.addAttribute("expenseItem", Long.parseLong(request.getParameter("expenseItem")));
			}
			// auditTrailService.saveAuditTrailData("tds", "Search", "Admin",
			// NotificationModule.AGE_GROUP_MASTER,
			// NotificationAction.SEARCH, "/searchAgeGroup", userId);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "hrms/claimMatrix/claimApplicationList";
	}

//	@RequestMapping(value = "/deleteClaimExpenseDtl/{id}")
//	public @ResponseBody JsonResponse deleteClaimExpenseDtl(@PathVariable("id") Long id, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
//		
//		
//		JsonResponse res = new JsonResponse();
//		UserMaster um = (UserMaster) session.getAttribute("usermaster");
//		Long companyId = (Long) session.getAttribute("companyId");
//		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
//
//		if (um == null || companyId == null || companyBranchId == null) {
//			res.setStatus("FAIL");
//			return res;
//		}
//
//		ClaimApplicationExpenseDetails deleteClaimExpenseDtl = applicationExpenseDetailsRepository.findById(id).orElse(null);
//		if (deleteClaimExpenseDtl != null) {
//			deleteClaimExpenseDtl.setDelete(true);
//			applicationExpenseDetailsRepository.save(deleteClaimExpenseDtl);
//
//			res.setStatus("SUCCESS");
//		}
//
//		return res;
//	}
	@PostMapping(value = "/saveWorkFlow")
	@ResponseBody
	
	public String saveWorkFlow(HttpServletRequest request, Model model,
			
			
			HttpServletResponse response, HttpSession session) {
		String ret="";
		try {
			
			String applicationiidstr=request.getParameter("claimappid");
			Long applicationidpass=Long.parseLong(applicationiidstr);
			String totalBalanceStr = request.getParameter("totalBalance");

			// Check if totalBalanceStr is null or empty
			Double totalBalance = (totalBalanceStr != null && !totalBalanceStr.isEmpty()) ? 
			    Double.valueOf(totalBalanceStr) : 0.0;

			ClaimApplication claimApplicationObj = claimApplicationRepository.findByApplicationId(applicationidpass);
			UserMaster um = (UserMaster) session.getAttribute("usermaster");
			Long companyId = (Long) session.getAttribute("companyId");
			Long companyBranchId = (Long) session.getAttribute("companyBranchId");
			Long roleId = (Long) session.getAttribute("roleId");
			//Employee emp = employeeRepository.findById(employeeId).get();
			
			// Code for Workflow State machine
			BigDecimal column1Amount = BigDecimal.ZERO;
			BigDecimal column2Amount = BigDecimal.ZERO;
			
			WorkflowInstanceEntity workflowInstanceEntity = workflowInstanceRepository.findById(claimApplicationObj.getClaimMatrixAppWorkFlowInstanceEntity()).orElse(null);

			
			if(claimApplicationObj.getClaimMatrixAppWorkFlowInstanceEntity()==null) {
				 ClaimMatrixWFInstanceDto Wdto = new ClaimMatrixWFInstanceDto();

					Wdto.setTypeId(WorkflowType.CLAIM_MATRIX_APPLICATION);
					Wdto.setWorkflowVersion((short) 1);
					Wdto.setCreateDate(LocalDateTime.now());
					Wdto.setCompanyId(companyId);
					Wdto.setBranchId(Integer.parseInt(companyBranchId.toString()));
					Wdto.setCreatedByUserId(um.getId());

					Wdto.setClaimApplication(claimApplicationObj);

					UserMaster userMaster = userMasterRepository.findByEmpId(claimApplicationObj.getEmpClaim().getId());

					WorkflowRuleConfigurationMaster ruleConfigurationMaster = configuratiionMasterRepository
							.findByWorkflowTypeAndEmployeeIdToGetLatestEmployeeRule(companyId, companyBranchId,
									Wdto.getTypeId().getTypeId(), userMaster.getId().toString());
					/* if rule found then need to use that otherwise default rule applied */
					if (ruleConfigurationMaster != null) {
						Wdto.setRuleConfigurationMaster(ruleConfigurationMaster);
					} else {
						Wdto.setRuleConfigurationMaster(
								configuratiionMasterRepository.findAllByRuleNameIgnoreCase("DEFAULT_RULE").get(0));
					}

					/* for setting reviewers to application for defined workflow type */
					/* List<Pair<Integer, Set<Long>>> */
					Wdto.setReviewers(getEmployeeReviwerListForWorkFlowDto(claimApplicationObj.getEmpClaim(),
							Wdto.getRuleConfigurationMaster()));

					System.err.println("=========" + Wdto);

					// Call to create Application WorkFlow Service
					ClaimMatrixAppWorkFlowInstanceEntity claimMatrixAppWorkFlowInstanceEntity;
					
					

					
					

					// Call create Application WorkFlow Service
					RoleMaster role =  roleMasterRepo.findByIdAndIsDelete(roleId, false);
					
					if (role.getIsAdmin()) {
						claimMatrixAppWorkFlowInstanceEntity = claimMatrixAppService
								.createByAdmin(ClaimMatrixWFInstanceDto.toEntity(Wdto));
						
						List<ClaimApplicationExpenseDetails> expenseDetailsList = claimApplicationExpenseDetailsRepository.findByClaimApplicationId(Long.parseLong(applicationiidstr));
	                    
						for (ClaimApplicationExpenseDetails detail : expenseDetailsList) {
							
							Double requestamount = detail.getRequestedValue();

							detail.setApprovedAmount(requestamount);
							detail.setApprovedamountl2(requestamount);

							claimApplicationExpenseDetailsRepository.save(detail);
						    
						    
						}

						// auto approved when admin create application for employee.
						claimApplicationObj.setAprvId(um);
						claimApplicationObj.setConfirmStatus("APPROVED");
						claimApplicationObj.setApprvDate(new Date());
					} else {
						claimMatrixAppWorkFlowInstanceEntity = claimMatrixAppService
								.create(ClaimMatrixWFInstanceDto.toEntity(Wdto));
						claimApplicationObj.setConfirmStatus("UNDER_REVIEW");

					}

					/* Save workflow od instance entity into the od main table */


					claimApplicationObj
							.setClaimMatrixAppWorkFlowInstanceEntity(claimMatrixAppWorkFlowInstanceEntity.getId());
				 if(claimApplicationObj != null && claimApplicationObj.getExpenseCategory().getClaimName().equalsIgnoreCase("Furniture")) {
					 claimApplicationObj.setTotalBalance(totalBalance);
				 }
					/* Save after everything set into the object */
					claimApplicationRepository.save(claimApplicationObj);

					System.err.println("Section To Tour WorkFlow Entity created Successfully");

					/* For sending the notifications */
		if(claimMatrixAppWorkFlowInstanceEntity.getId()!=null) {
					 sendNotification(userMasterRepository.findByEmpId(claimApplicationObj.getEmpClaim().getId()), companyId,
							 companyBranchId, claimMatrixAppWorkFlowInstanceEntity, claimApplicationObj);
				 }
					
				 ret="SUCCESS";
				 //return "SUCCESS";
			 }
			 
			else if(workflowInstanceEntity.getCurrentState().equalsIgnoreCase("S_CREATED")) {
				callEvent(claimApplicationObj.getClaimMatrixAppWorkFlowInstanceEntity(), "E_SUBMIT", "Changes Done By User",
						request, session);
				
				ret="SUCCESS";
			}

			 else {
				 ret= "FAIL";
			 }

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	@RequestMapping("/editClaimApplication/{id}/{isview}")
	public String editClaimApplication(@PathVariable("id") Long id,@PathVariable("isview") Long isview, Model model, HttpSession session, 
	                                   HttpServletRequest request, HttpServletResponse response,
	                                   RedirectAttributes redirectAttributes) {
	    try {
	        // Uncomment and use the security checker if needed
	        // securityChecker.checkMenuAccessPermit("/hrms/loan/applicationList", request, response, request.getSession(), CommonConstant.VIEW);

	        UserMaster um = (UserMaster) session.getAttribute("usermaster");
	        Long companyId = (Long) session.getAttribute("companyId");
	        Long companyBranchId = (Long) session.getAttribute("companyBranchId");
	        Long roleId = (Long) session.getAttribute("roleId");
	        if (um == null || companyId == null || companyBranchId == null) {
	            return "redirect:/login";
	        }
	        System.out.println("isview "+isview+" (isview == 5) "+(isview == 5));
	        if(isview == 5)
	        	 model.addAttribute("isView", false);
	        else
	        	 model.addAttribute("isView", true);
	        
	        	ClaimApplication claimApplicationObj = claimApplicationRepository.findByApplicationId(id);
	        if (claimApplicationObj == null) {
	            // Handle the case where the claim application is not found
	            redirectAttributes.addFlashAttribute("error", "Claim application not found.");
	            return "redirect:/claimApplications"; // Assuming this is the list page
	        }
	       
			Long wfid=claimApplicationObj.getClaimMatrixAppWorkFlowInstanceEntity();
			
			WorkflowInstanceEntity wm=workflowinstancerepo.getById(wfid);
			System.out.println("Workflowdatataaaa+++++++"+wm);
			boolean isapprove=false;
		if(wfid!=null) {
			Map<Integer, List<Pair<Long, Boolean>>> forwardMap = wm.getForwardMap();
	         isapprove = hasAnyTrue(forwardMap);
	        System.out.println("extractedId id is------------"+isapprove);
	        claimApplicationObj.setApproveornot(isapprove);
		}
			
//			Map<Integer, List<Pair<Long, Boolean>>> forwardMap = wm.getForwardMap();
//	        boolean isapprove = hasAnyTrue(forwardMap);
//	        System.out.println("extractedId id is------------"+isapprove);
//	        claimApplicationObj.setApproveornot(isapprove);
			

	        List<ClaimApplicationExpenseDetails> claimApplicationExpenseDetails = claimApplicationExpenseDetailsRepo
	                .findAllByClaimApplication(claimApplicationObj); 
	        
			double suml1approval = 0.0;
		    double suml2approval= 0.0;
		    double reqvalue= 0.0;
		    double gstvalue= 0.0;
		    



		    for (ClaimApplicationExpenseDetails details : claimApplicationExpenseDetails) {
		     if(details.getApprovedAmount()!=null) {
		    	 suml1approval += details.getApprovedAmount();
		     }
		     if(details.getApprovedamountl2()!=null) {
		    	 suml2approval += details.getApprovedamountl2();
		     }
		     
		     if(details.getRequestedValue()!=null) {
		    	 reqvalue += details.getRequestedValue();
		     }
		     
		     if(details.getBillValueGst()!=null) {
		    	 gstvalue += details.getBillValueGst();
		     } 
		      
	        }
		    if(suml2approval== 0.0) {
		    	model.addAttribute("suml2approval", "-");
		    }
		    else {
		    	model.addAttribute("suml2approval", suml2approval);
		    }
		    if(suml1approval== 0.0) {
		    	model.addAttribute("suml1approval", "-");
		    }
		    else {
		    	model.addAttribute("suml1approval", suml1approval);
		    }
		    
		    if(reqvalue>gstvalue) {
		    	double presclaim=reqvalue-gstvalue;
		    	model.addAttribute("presclaim", presclaim);
		    }
		    else {
		    	double presclaim=gstvalue-reqvalue;
		    	model.addAttribute("presclaim", presclaim);
		    }
		    
		    



		    
		   
		    System.out.println("Sum of approved amounts by l1: " + suml1approval);
		     System.out.println("Sum of approved amounts by l2: " + suml2approval);
//	        List<ClaimApplicationExpenseDetails> claimApplicationExpenseDetailsgrid = claimApplicationExpenseDetailsRepo
//	                .findAllByClaimApplication(claimApplicationObj);
	        List<ClaimApplicationExpenseDetails> claimApplicationExpenseDetailsgrid = claimApplicationExpenseDetailsRepo
	                .findAllByClaimApplicationIsDeleteFalse(claimApplicationObj);
	        for(ClaimApplicationExpenseDetails ce : claimApplicationExpenseDetailsgrid) {
	        	//System.out.println("ce.getAttachDocument() "+ce.getAttachDocument());
	        	//System.out.println("claimApplicationExpenseDetailsgrid "+ce.getAttachDocument().getActualFileName());
	        	//This condition for furniture is added on 17-09-2024 to display total balance and total requested amount by ravi
	        	/*if(ce.getExpenseItem().getItemName().contains("Furniture")) {
	        		System.out.println("Claim Total Balance:: "+claimApplicationObj.getTotalBalance());
	        		claimApplicationObj.setTotalRequestedAmount(ce.getRequestedValue() - ce.getBillValueGst());
	        		//claimApplicationObj.setTotalBalance(claimApplicationObj.getTotalBalance() - claimApplicationObj.getTotalRequestedAmount());
	        		System.out.println("Requested Amount::"+claimApplicationObj.getTotalRequestedAmount()+"\nTotal Balance:"+claimApplicationObj.getTotalBalance());
	        	}*/
	        }
	        List<Employee> employeeList = null;
	        RoleMaster role =  roleMasterRepo.findByIdAndIsDelete(roleId, false);
	        if (role.getIsAdmin()) {
	            employeeList = employeeRepository.findAllByIsDeleteFalseAndCompanyIdAndCompanyBranchIdOrderByCreatedDateDesc(companyId, companyBranchId);
	            model.addAttribute("employeeList", employeeList);
	        } else {
	            model.addAttribute("Employee", um.getEmpId());
	        }

	        List<ClaimCategoryModel> claims = claimCategoryMasterRepo.findAllByCompanyIdAndCompanyBranchIdAndIsDeleteFalse(companyId, companyBranchId);

			List<ClaimConfiguration> claimConfigurations = claimConfigurationRepository.findByClaimCategoryId_(claimApplicationExpenseDetailsgrid.get(0).getExpenseItem().getClaimCategory().getId(),companyId, companyBranchId);
			if (claimConfigurations != null && !claimConfigurations.get(0).isOnActual())
			{
				if(claimApplicationObj.getTotalBalancePending() != null && claimApplicationObj.getExpenseCategoryOpeningBalance() != null){
					claimApplicationObj.setExpensePending(String.valueOf((claimApplicationObj.getExpenseCategoryOpeningBalance() - claimApplicationObj.getTotalBalancePending())));
					if(claimConfigurations.get(0).getClaimCategory().getClaimName().equalsIgnoreCase("Furniture and Fixture")) {
						claimApplicationObj.setTotalBalance(claimApplicationObj.getTotalBalancePending());
					}
					/*else if(claimConfigurations.get(0).getClaimCategory().getClaimName().equalsIgnoreCase("Entertainment")) {
						claimApplicationObj.setExpenseCategoryOpeningBalance(claimApplicationObj.getExpenseCategoryOpeningBalance()*12);
					}*/
				}else {
					claimApplicationObj.setTotalBalancePending((double) 0);
				}


			}
			ClaimApplication claimApplication = claimApplicationRepository.findByApplicationId(id);
			Long expensecatid=claimApplication.getExpenseCategory().getId();

			//Optional <ItemMasterForClaimMatrix> claimItem = itemMasterRepository.findById(expensecatid);
			Optional <ClaimCategoryModel> claimItem=claimCategoryMasterRepo.findById(expensecatid);
			System.out.println(claimApplication.getExpenseCategory().getId()+" getItemName "+claimItem.get().getClaimName());

			if(claimItem.get().getClaimName().equals("Entertainment"))
			{
				ClaimApplication claimApplicationforent = claimApplicationRepository.findByApplicationId(id);

				List<ClaimApplicationExpenseDetails> claimApplicationExpenseDetailsforent = claimApplicationExpenseDetailsRepo
						.findByClaimApplication1(claimApplication.getId());

				if (!claimApplicationExpenseDetailsforent.isEmpty()) {
					ClaimApplicationExpenseDetails firstExpenseDetail = claimApplicationExpenseDetailsforent.get(0);


					Date billDate = firstExpenseDetail.getBillDate();

// Ensure billDate is not null
					if (billDate == null) {
						//return ResponseEntity.badRequest().body("Bill date cannot be null.");
					}

// Use Calendar to calculate financial year start and end dates
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(billDate);

					int year = calendar.get(Calendar.YEAR);
					int month = calendar.get(Calendar.MONTH);

// Calculate financial year start and end dates
					String formattedStartDate;
					String formattedEndDate;

					Calendar financialYearCalendar = Calendar.getInstance();

					if (month < Calendar.APRIL) { // Before April
						// Start Date: April 1st of the previous year
						financialYearCalendar.set(year - 1, Calendar.APRIL, 1, 0, 0, 0);
						formattedStartDate = new SimpleDateFormat("yyyy-MM-dd").format(financialYearCalendar.getTime());

						// End Date: March 31st of the current year
						financialYearCalendar.set(year, Calendar.MARCH, 31, 23, 59, 59);
						formattedEndDate = new SimpleDateFormat("yyyy-MM-dd").format(financialYearCalendar.getTime());
					} else { // April or later
						// Start Date: April 1st of the current year
						financialYearCalendar.set(year, Calendar.APRIL, 1, 0, 0, 0);
						formattedStartDate = new SimpleDateFormat("yyyy-MM-dd").format(financialYearCalendar.getTime());

						// End Date: March 31st of the next year
						financialYearCalendar.set(year + 1, Calendar.MARCH, 31, 23, 59, 59);
						formattedEndDate = new SimpleDateFormat("yyyy-MM-dd").format(financialYearCalendar.getTime());
					}

// Convert formatted dates to java.sql.Date
					java.sql.Date sqlStartDate = java.sql.Date.valueOf(formattedStartDate);
					java.sql.Date sqlEndDate = java.sql.Date.valueOf(formattedEndDate);

					System.out.println("SQL Start Date: " + sqlStartDate);
					System.out.println("SQL End Date: " + sqlEndDate);


					// Extract the required data from the first item
					Long empId = claimApplicationforent.getEmpClaim().getId();
					Long expenseCategoryId = firstExpenseDetail.getClaimApplication().getExpenseCategory().getId();
					Long expenseItemId = firstExpenseDetail.getExpenseItem().getId();



					// Pass the extracted data to your query
					Double totalDifference = claimApplicationExpenseDetailsRepository.getEntertainmentSum(
							empId, expenseCategoryId, expenseItemId,
							sqlStartDate, sqlEndDate,
							companyId, companyBranchId);

					System.out.println("Sum Of The Claimed Balance Is" + totalDifference);
					model.addAttribute("totalSumEnt", totalDifference);
                    claimApplicationObj.setExpensePending(String.valueOf(totalDifference));
					claimApplicationObj.setTotalBalance((claimApplicationObj.getExpenseCategoryOpeningBalance()*12)-totalDifference);
					claimApplicationObj.setExpenseCategoryOpeningBalance(claimApplicationObj.getExpenseCategoryOpeningBalance()*12);
					// You can now use totalDifference as needed
				} else {
					// Handle case where the list is empty (optional)
					throw new NoSuchElementException("No expense details found for the given claim application.");
				}

			}
			
			
		////enddddd
			
			
			
			model.addAttribute("claims", claims);

	        List<HrmsCode> monthList = hrmsCodeService.findByFieldName("MONTHS");
	        model.addAttribute("months", monthList);
	        model.addAttribute("Approveornot",isapprove);

	        model.addAttribute("expenseType", ExpenseType.values());
	        model.addAttribute("fuelType", FuelType.values());

	        model.addAttribute("claimApplicationObj", claimApplicationObj);
	        model.addAttribute("claimApplicationExpenseDetails", new ClaimApplicationExpenseDetails());
	        model.addAttribute("claimApplicationExpenseDetailsgrid", claimApplicationExpenseDetailsgrid);


	        model.addAttribute("isEdit", true);
	        model.addAttribute("adminStatusEdit", "true");
	        model.addAttribute("claimappisAdd", false);
			model.addAttribute("claimappisEdit", false);
			model.addAttribute("claimappisView", true);
	        
	        model.addAttribute("id",id);
	    } catch (Exception e) {
	        e.printStackTrace();
	        logger.error("Error in editing Application Details: " + e.getMessage());
	    }
	    if(isview == 6) {
	    	try {

				logger.info("ClaimApplicationController:viewclaimApplication");

				UserMaster um = (UserMaster) session.getAttribute("usermaster");

				Long companyId = (Long) session.getAttribute("companyId");
				Long companyBranchId = (Long) session.getAttribute("companyBranchId");

				ClaimMatrixAppWorkFlowInstanceEntity claimMatrixAppWorkFlowInstanceEntity = claimMatrixAppWorkFlowInstanceRepository
						.findById(id).orElse(null);
				
				System.out.println("---------------------->>>>>>>>>>>>>>>>>>>>>");
				System.out.println(id);

				if (um == null || companyId == null || companyBranchId == null) {
					return "redirect:/signin";
				} else {

					model.addAttribute("claimMatrix", claimMatrixAppWorkFlowInstanceEntity);

					// ClaimApplication claimApplication
					// =claimApplicationRepository.findById(claimMatrixAppWorkFlowInstanceEntity.getClaimApplication().getId()).orElse(null);
					ClaimApplication claimApplication = claimApplicationRepository.findByApplicationId(id);

					List<ClaimApplicationExpenseDetails> claimApplicationExpenseDetails = claimApplicationExpenseDetailsRepo
							.findByClaimApplication1(claimApplication.getId());

					List<ClaimConfiguration> claimConfigurations = claimConfigurationRepository.findByClaimCategoryId_(claimApplicationExpenseDetails.get(0).getExpenseItem().getClaimCategory().getId(),companyId, companyBranchId);
					if (claimConfigurations != null && !claimConfigurations.get(0).isOnActual())
					{
						if(claimApplication.getTotalBalancePending() != null && claimApplication.getExpenseCategoryOpeningBalance() != null){
							claimApplication.setExpensePending(String.valueOf((claimApplication.getExpenseCategoryOpeningBalance() - claimApplication.getTotalBalancePending())));
							if(claimConfigurations.get(0).getClaimCategory().getClaimName().equalsIgnoreCase("Furniture and Fixture")) {
								claimApplication.setTotalBalance(claimApplication.getTotalBalancePending());
							}
							else if(claimConfigurations.get(0).getClaimCategory().getClaimName().equalsIgnoreCase("Entertainment")) {
								claimApplication.setExpenseCategoryOpeningBalance(claimApplication.getExpenseCategoryOpeningBalance()*12);
							}
						}else {
							claimApplication.setTotalBalancePending((double) 0);
						}


					}
					
					Long expensecatid=claimApplication.getExpenseCategory().getId();
					
					//Optional <ItemMasterForClaimMatrix> claimItem = itemMasterRepository.findById(expensecatid);
					Optional <ClaimCategoryModel> claimItem=claimCategoryMasterRepo.findById(expensecatid);
					System.out.println(claimApplication.getExpenseCategory().getId()+" getItemName "+claimItem.get().getClaimName());
					if(claimItem.get().getClaimName().equals("Vehicle Running a Maintenance Expense Four Wheeler") || claimItem.get().getClaimName().equals("Vehicle Running An Maintenance Expense Two Wheeler"))
					{
						
				  for (ClaimApplicationExpenseDetails details : claimApplicationExpenseDetails) {

					     
					     if(details.getClaimMonth()!=null) {
					    	 int monthNumber = 0;  // Default to 0 if no match is found
					    	    String claimMonth = details.getClaimMonth().trim().toLowerCase(); // Normalize to lower case

					    	    switch (claimMonth) {
					    	        case "january":
					    	            monthNumber = 1;
					    	            break;
					    	        case "february":
					    	            monthNumber = 2;
					    	            break;
					    	        case "march":
					    	            monthNumber = 3;
					    	            break;
					    	        case "april":
					    	            monthNumber = 4;
					    	            break;
					    	        case "may":
					    	            monthNumber = 5;
					    	            break;
					    	        case "june":
					    	            monthNumber = 6;
					    	            break;
					    	        case "july":
					    	            monthNumber = 7;
					    	            break;
					    	        case "august":
					    	            monthNumber = 8;
					    	            break;
					    	        case "september":
					    	            monthNumber = 9;
					    	            break;
					    	        case "october":
					    	            monthNumber = 10;
					    	            break;
					    	        case "november":
					    	            monthNumber = 11;
					    	            break;
					    	        case "december":
					    	            monthNumber = 12;
					    	            break;
					    	        default:
					    	            monthNumber = 0;  
					    	            break;
					    	         


					    	 
					     }
					    	    if (monthNumber != 0) {
					    	        System.out.println("Month Number: " + monthNumber);
					    	       // String monthtopass=String.valueOf(monthNumber);
					    	        String monthToPass = String.format("%02d", monthNumber);
					    	        String finmonth=monthToPass;
					  		      VehicleRunningMaintenanceExpense rate=  vehicleRepo.findAllBetweenStartMonthAndEndMonthAndIsDeleteFalseForView(finmonth,finmonth,details.getClaimYear());
					  		      
					  		    System.out.println("RATE IS++++++++++++++++++++: " + rate.getRate());
					  		    if(rate.getRate() !=null) {
					  		      details.setRate(rate.getRate());
					  		      model.addAttribute("rate", rate.getRate());
								//ClaimApplicationExpenseDetails c1 =  claimApplicationExpenseDetailsRepo.save(details);

					  		    }
					  		
					  		    
					    	    } else {
					    	        System.out.println("Invalid month name");
					    	    }
					    	    
					     }

					     System.out.println("rate is "+details.getRate());
				        }
					}
					if(claimItem.get().getClaimName().equals("Entertainment"))
					{
						ClaimApplication claimApplicationforent = claimApplicationRepository.findByApplicationId(id);

						List<ClaimApplicationExpenseDetails> claimApplicationExpenseDetailsforent = claimApplicationExpenseDetailsRepo
								.findByClaimApplication1(claimApplication.getId());

						if (!claimApplicationExpenseDetailsforent.isEmpty()) {
							ClaimApplicationExpenseDetails firstExpenseDetail = claimApplicationExpenseDetailsforent.get(0);


							Date billDate = firstExpenseDetail.getBillDate();

// Ensure billDate is not null
							if (billDate == null) {
								//return ResponseEntity.badRequest().body("Bill date cannot be null.");
							}

// Use Calendar to calculate financial year start and end dates
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(billDate);

							int year = calendar.get(Calendar.YEAR);
							int month = calendar.get(Calendar.MONTH);

// Calculate financial year start and end dates
							String formattedStartDate;
							String formattedEndDate;

							Calendar financialYearCalendar = Calendar.getInstance();

							if (month < Calendar.APRIL) { // Before April
								// Start Date: April 1st of the previous year
								financialYearCalendar.set(year - 1, Calendar.APRIL, 1, 0, 0, 0);
								formattedStartDate = new SimpleDateFormat("yyyy-MM-dd").format(financialYearCalendar.getTime());

								// End Date: March 31st of the current year
								financialYearCalendar.set(year, Calendar.MARCH, 31, 23, 59, 59);
								formattedEndDate = new SimpleDateFormat("yyyy-MM-dd").format(financialYearCalendar.getTime());
							} else { // April or later
								// Start Date: April 1st of the current year
								financialYearCalendar.set(year, Calendar.APRIL, 1, 0, 0, 0);
								formattedStartDate = new SimpleDateFormat("yyyy-MM-dd").format(financialYearCalendar.getTime());

								// End Date: March 31st of the next year
								financialYearCalendar.set(year + 1, Calendar.MARCH, 31, 23, 59, 59);
								formattedEndDate = new SimpleDateFormat("yyyy-MM-dd").format(financialYearCalendar.getTime());
							}

// Convert formatted dates to java.sql.Date
							java.sql.Date sqlStartDate = java.sql.Date.valueOf(formattedStartDate);
							java.sql.Date sqlEndDate = java.sql.Date.valueOf(formattedEndDate);

							System.out.println("SQL Start Date: " + sqlStartDate);
							System.out.println("SQL End Date: " + sqlEndDate);


							// Extract the required data from the first item
							Long empId = claimApplicationforent.getEmpClaim().getId();
							Long expenseCategoryId = firstExpenseDetail.getClaimApplication().getExpenseCategory().getId();
							Long expenseItemId = firstExpenseDetail.getExpenseItem().getId();



							// Pass the extracted data to your query
							Double totalDifference = claimApplicationExpenseDetailsRepository.getEntertainmentSum(
									empId, expenseCategoryId, expenseItemId,
									sqlStartDate, sqlEndDate,
									companyId, companyBranchId);

							System.out.println("Sum Of The Claimed Balance Is" + totalDifference);
							model.addAttribute("totalSumEnt", totalDifference);


							// You can now use totalDifference as needed
						} else {
							// Handle case where the list is empty (optional)
							throw new NoSuchElementException("No expense details found for the given claim application.");
						}

					}
					System.out.println("----<<<<>>>>"+claimItem.get().getClaimName().contains("Over Time Allowance"));
					if(claimItem.get().getClaimName().contains("Over Time Allowance")){
						ClaimApplication claimApplicationforent = claimApplicationRepository.findByApplicationId(id);

						List<ClaimApplicationExpenseDetails> claimApplicationExpenseDetailsforent = claimApplicationExpenseDetailsRepo
								.findByClaimApplication1(claimApplication.getId());

						Long empId = claimApplicationforent.getEmpClaim().getId();
						Integer year = claimApplicationExpenseDetailsforent.get(0).getYear();
						Integer month = Month.valueOf(claimApplicationExpenseDetailsforent.get(0).getExpenseBillPeriod().toUpperCase()).getValue();
						Long comId = claimApplicationforent.getCompany().getId();
						Long branchId = claimApplicationforent.getCompanyBranch().getId();

						List<Object[]> basicDaSum;
						basicDaSum=employeeProvidentFundDtlRepo.getBasicAndDAForEmployee(empId,year,month,comId,branchId);

						if(basicDaSum.isEmpty()) {

							basicDaSum = elementOfPaySystemMasterRepository.findBasicAndDAByStructureId(month,year,empId,comId,branchId);
						}
						Double daValue;
						Double basicValue;
						if(basicDaSum.get(0)[1].toString().equalsIgnoreCase("-17")){
							daValue = Double.parseDouble(basicDaSum.get(0)[2].toString());
							basicValue = Double.parseDouble(basicDaSum.get(1)[2].toString());
						}else{
							basicValue = Double.parseDouble(basicDaSum.get(0)[2].toString());
							daValue = Double.parseDouble(basicDaSum.get(1)[2].toString());
						}

						model.addAttribute("basicValue", Math.round(basicValue));
						model.addAttribute("daValue", Math.round(daValue));
					}

					System.out.println("id is=-------------->>>>>>>>>>> "+id);
					List<WorkflowEventLogEntity> logs = eventLogRepository.findWfdtlByclaimId(id);

					model.addAttribute("claimApplication", claimApplication);
					model.addAttribute("claimApplicationExpenseDetails1", claimApplicationExpenseDetails);
					

					model.addAttribute("logs",logs);
					model.addAttribute("expenseType", ExpenseType.values());
					model.addAttribute("id",id);
					

					System.out.println("claimapplicationmodel>>>>>>>>>>" + claimApplication);
					System.out.println("claimapplicationexpense>>>>>>>" + claimApplicationExpenseDetails);
					System.out.println("eventLogRepository>>>>>>>>>>" + logs);
					List<WorkflowEventLogEntity> entityLogs;
					for (WorkflowEventLogEntity log : logs) {
					    System.out.println("Log comment:---------------------- " + log.getComment());
					    entityLogs = workflowUtils.getLogsByApplicationId(log.getInstanceId(), request, session);
					    model.addAttribute("entityLogs", entityLogs);			
				    }

					System.err.println(um);

					model.addAttribute("user", um);
					//model.addAttribute("isEdit", false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error in View Claim Application");
			}
	    }
	    return "hrms/claimMatrix/claimApplication";
	}
	@ResponseBody
	@RequestMapping("/removeFile")
    public  ResponseEntity<String> removeFile( @RequestParam(value="fileId")Long fileId , HttpServletResponse response, HttpServletRequest request,
			HttpSession session) {
       System.out.println("fileId" + fileId);
        //String docid=request.getParameter("fileIde");
        //System.out.println("fileId" + docid);
        //Long fileId=Long.parseLong(docid);
        Object existsdocAfterDelete=null;
        boolean existsBeforeDelete = fileMasterRepository.existsById(fileId);
        FileMaster fileMaster = fileMasterRepository.findById(fileId).orElse(null);
        //System.out.println(fileMaster);
//        if(fileMaster!=null ) {
//        	objectMap.remove("fileMaster").remove(fileMaster);
//        	
//        }
//        else {
//        	System.out.println("object is empty");
//        }
        //objectMap.remove("fileMaster").remove(fileMaster);
        System.out.println(fileMaster);
        if (fileMaster != null) {
            // Get the list associated with the key "fileMaster" from the map
            List<FileMaster> fileMasterList = objectMap.get("fileMaster");
            
            // Check if the list is not null
            if (fileMasterList != null) {
                // Remove the fileMaster object from the list
                fileMasterList.remove(fileMaster);
                
                // If you want to remove the list from the map if it's empty after the removal
                if (fileMasterList.isEmpty()) {
                    objectMap.remove("fileMaster");
                }
            } else {
                System.out.println("No list found for key 'fileMaster'");
            }
        } else {
            System.out.println("object is empty");
        }
        if (existsBeforeDelete) {
            
            if(claimApplicationExpenseDetailsTmpRepo.findByClaimApplicationdocid(fileId)!=null) {
            	claimApplicationExpenseDetailsTmpRepo.deletebydocid(fileId);
            	fileMasterRepository.deleteById(fileId);
                 existsdocAfterDelete=claimApplicationExpenseDetailsTmpRepo.findByClaimApplicationdocid(fileId);

            }
            else {
                claimApplicationExpenseDetailsRepo.deletebydocid(fileId);
                fileMasterRepository.deleteById(fileId);
                 existsdocAfterDelete=claimApplicationExpenseDetailsRepo.existsById(fileId);

            }
            //claimApplicationExpenseDetailsRepo.deletebydocid(fileId);
             boolean existsAfterDelete = fileMasterRepository.existsById(fileId);
             existsdocAfterDelete=claimApplicationExpenseDetailsRepo.findByClaimApplicationdocid(fileId);
            
            if (!existsAfterDelete && existsdocAfterDelete ==null) {
                return ResponseEntity.ok("SUCCESS");
                
            } else {
                return ResponseEntity.status(500).body("FAIL");
            }
        } else {
            return ResponseEntity.status(404).body("FAIL");
        }
    }
	
	@ResponseBody
	@RequestMapping("/removeFiletemp")
    public  ResponseEntity<String> removeFiletemp( @RequestParam(value="fileId")Long fileId , HttpServletResponse response, HttpServletRequest request,
			HttpSession session) {
       System.out.println("fileId" + fileId);
        

        boolean existsBeforeDelete = fileMasterRepository.existsById(fileId);
        FileMaster fileMaster = fileMasterRepository.findById(fileId).orElse(null);
       
        System.out.println(fileMaster);
        if (fileMaster != null) {
            List<FileMaster> fileMasterList = objectMap.get("fileMaster");
            
            if (fileMasterList != null) {
                fileMasterList.remove(fileMaster);
                
                if (fileMasterList.isEmpty()) {
                    objectMap.remove("fileMaster");
                }
            } else {
                System.out.println("No list found for key 'fileMaster'");
            }
        } else {
            System.out.println("object is empty");
        }
        if (existsBeforeDelete) {
            fileMasterRepository.deleteById(fileId);
            claimApplicationExpenseDetailsTmpRepo.deletebydocid(fileId);
            boolean existsAfterDelete = fileMasterRepository.existsById(fileId);
            boolean existsdocAfterDelete=claimApplicationExpenseDetailsTmpRepo.existsById(fileId);
            
            if (!existsAfterDelete && !existsdocAfterDelete) {
                return ResponseEntity.ok("SUCCESS");
                
            } else {
                return ResponseEntity.status(500).body("FAIL");
            }
        } else {
            return ResponseEntity.status(404).body("FAIL");
        }
    }
	
	
	@ResponseBody
	@RequestMapping("/deletetemprec")
    public  ResponseEntity<String> deletetemprec( @RequestParam(value="exptepid")Long exptepid , HttpServletResponse response, HttpServletRequest request,
			HttpSession session) {
       System.out.println("exptepid" + exptepid);
        

        boolean existsBeforeDelete = claimApplicationExpenseDetailsTmpRepo.existsById(exptepid);
        
       
        
        if (existsBeforeDelete) {
            
            claimApplicationExpenseDetailsTmpRepo.deletebyexpid(exptepid);
           
            boolean existsdocAfterDelete=claimApplicationExpenseDetailsTmpRepo.existsById(exptepid);
            
            if (!existsdocAfterDelete) {
                return ResponseEntity.ok("SUCCESS");
                
            } else {
                return ResponseEntity.status(500).body("FAIL");
            }
        } else {
            return ResponseEntity.status(404).body("FAIL");
        }
    }
	
	
	
	
	@ResponseBody
	@RequestMapping("/deletepermrec")
    public  ResponseEntity<String> deletepermrec( @RequestParam(value="exptepid")Long exptepid , @RequestParam("totalBalance") Double totalBalance, HttpServletResponse response, HttpServletRequest request,
			HttpSession session) {
       System.out.println("exptepid" + exptepid);
        
       ClaimApplicationExpenseDetailstmp claimtep= claimApplicationExpenseDetailsTmpRepo.findByClaimApplicationExpenseDetailstmpId(exptepid);
       //boolean iftemp=claimtep.isTempis();
       
        boolean existsBeforeDeleteperm = claimApplicationExpenseDetailsRepo.existsById(exptepid);
       
        
        boolean existsBeforeDelete = claimApplicationExpenseDetailsTmpRepo.existsById(exptepid);
        
       
        
        if (existsBeforeDelete==true && claimtep.isTempis()==true) {
        	
           
             
            
            
            	
            
            
            claimApplicationExpenseDetailsTmpRepo.deletebyexpid(exptepid);
            
            
            boolean existsdocAfterDelete=claimApplicationExpenseDetailsTmpRepo.existsById(exptepid);
            
            
            if (!existsdocAfterDelete) {
                return ResponseEntity.ok("SUCCESS");
                
            } else {
                return ResponseEntity.status(500).body("FAIL");
            }
        }
        
         if ((existsBeforeDeleteperm==true) && (claimtep==null ) ) {
			  ClaimApplicationExpenseDetails claimApplicationExpenseDetails = claimApplicationExpenseDetailsRepo.findById(exptepid).get();
			  if(claimApplicationExpenseDetails.getExpenseItem().getItemName().equalsIgnoreCase("Furniture Purchase Claim")) {
				  Double requestedValue = claimApplicationExpenseDetails.getRequestedValue() - claimApplicationExpenseDetails.getBillValueGst();
				  ClaimApplication claimApplication = claimApplicationExpenseDetails.getClaimApplication();
				  claimApplication.setTotalBalance(requestedValue+totalBalance);
				  claimApplication.setTotalBalancePending(totalBalance+requestedValue);
				  List<ClaimApplication> claimApplications = claimApplicationService.fetchClaimsForPastFiveYearData(claimApplicationExpenseDetails.getBillDate(),
						  claimApplication.getExpenseCategory().getId(), claimApplicationExpenseDetails.getExpenseItem().getId(), claimApplication.getEmpClaim().getId());

				   if(claimApplications != null) {
					  for (ClaimApplication c : claimApplications) {
						  c.setTotalBalancePending(totalBalance + requestedValue);
						  claimApplicationRepository.save(c);
					  }
				  }

				  claimApplicationRepository.save(claimApplication);
			  }
			  claimApplicationExpenseDetailsRepo.deletebpermid(exptepid);

               //ClaimApplicationExpenseDetails existsdocpermAfterDelete=claimApplicationExpenseDetailsRepo.findByexpneseApplicationId(exptepid);
            
            
                return ResponseEntity.ok("SUCCESS");
                
            
        }
        
        
        
        else {
            return ResponseEntity.status(404).body("FAIL");
        }
    }




	@ResponseBody
	@RequestMapping("/deletepermrecTotalBalance")
	public  ResponseEntity<String> deletepermrecTotalBalance( @RequestParam(value="exptepid")Long exptepid , HttpServletResponse response, HttpServletRequest request,
												  HttpSession session) {
		System.out.println("exptepid" + exptepid);

		ClaimApplicationExpenseDetailstmp claimtep= claimApplicationExpenseDetailsTmpRepo.findByClaimApplicationExpenseDetailstmpId(exptepid);
		//boolean iftemp=claimtep.isTempis();

		boolean existsBeforeDeleteperm = claimApplicationExpenseDetailsRepo.existsById(exptepid);


		boolean existsBeforeDelete = claimApplicationExpenseDetailsTmpRepo.existsById(exptepid);



		if (existsBeforeDelete==true && claimtep.isTempis()==true) {



			claimApplicationExpenseDetailsTmpRepo.deletebyexpid(exptepid);


			boolean existsdocAfterDelete=claimApplicationExpenseDetailsTmpRepo.existsById(exptepid);


			if (!existsdocAfterDelete) {
				return ResponseEntity.ok("SUCCESS");

			} else {
				return ResponseEntity.status(500).body("FAIL");
			}
		}

		if ((existsBeforeDeleteperm==true) && (claimtep==null ) ) {
			ClaimApplicationExpenseDetails claimApplicationExpenseDetails = claimApplicationExpenseDetailsRepo.findById(exptepid).get();

			claimApplicationExpenseDetailsRepo.deletebpermid(exptepid);

			//ClaimApplicationExpenseDetails existsdocpermAfterDelete=claimApplicationExpenseDetailsRepo.findByexpneseApplicationId(exptepid);


			return ResponseEntity.ok("SUCCESS");


		}



		else {
			return ResponseEntity.status(404).body("FAIL");
		}
	}






	/*
		The conditions in the above controller are checked as follow:
	 * 1. User between the age of 40 to 50 can apply for two claims per year
	 * 2. User between the age of >50 can apply for only one claim per year
	 */
	@GetMapping("/checkWholeBodyCheckUpConditions")
	public @ResponseBody String checkWholeBodyCheckUpConditions( HttpServletRequest request,
																 HttpSession session,@RequestParam("employeeId") Long empId,@RequestParam("employeeName") String empName,
																 @RequestParam("headExpenseItemId") Long headExpenseItemId,
																 @RequestParam("headExpenseItemName") String headExpenseItemName,
																 @RequestParam("expenseItemTypeId") Long expenseItemTypeId,
																 @RequestParam("relation") String relation,
																 @RequestParam("relationId") Long relationId)
	{

		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");

		try{
			if(empId != null){
				EmpPersonalInfo currentEmpDdetail = empPersonalInfoRepository.findByEmpId(empId);
				Date birthDate = currentEmpDdetail.getDateOfBirth();


				int age = calculateAge(birthDate, new Date());
				System.out.println("**********************The age of the employee:"+age);
				if(age<=40){
					return "FAIL";
				}
				ClaimItemConfiguration claimItemConfigurationList=claimItemConfigurationRepository.findLatestByItemMasterId(expenseItemTypeId,companyId,companyBranchId);
				if(claimItemConfigurationList == null) {
					return "FAIL_NO_GRADE_AVAILABLE";
				}
				
				List<ClaimApplicationExpenseDetails> claimApplicationExpenseDetailsList;
				if(relation.trim().equalsIgnoreCase("self")) {
				 claimApplicationExpenseDetailsList =
						claimApplicationExpenseDetailsRepo.findAllClaimApplicationByEmpIdAndExpenseItemIdandBranchandComapny(empId,expenseItemTypeId,companyId,companyBranchId);
				}else{
					claimApplicationExpenseDetailsList =
						claimApplicationExpenseDetailsRepo.findAllClaimApplicationByEmpIdAndExpenseItemIdandBranchandComapnyandIncurredFor
								(empId,expenseItemTypeId,companyId,companyBranchId,relationId);
				}
				if(claimApplicationExpenseDetailsList == null || claimApplicationExpenseDetailsList.size()==0){
					return "SUCCESS";
				}

				if(age >40 && age<=50){

					Date lastClaimApplyDate = claimApplicationExpenseDetailsList.get(claimApplicationExpenseDetailsList.size()-1).getClaimApplication().getRequestDate();
					Calendar lastClaimCalendarDate = Calendar.getInstance();
					lastClaimCalendarDate.setTime(lastClaimApplyDate);
					Calendar currentDate = Calendar.getInstance();
					currentDate.setTime(new Date());
					long differenceInMillis = currentDate.getTimeInMillis() - lastClaimCalendarDate.getTimeInMillis();
					long differenceInDays = differenceInMillis / (1000 * 60 * 60 * 24);
					if(differenceInDays<=730){

							return "CAN'T APPLY FOR A ClAIM IN TWO YEARS";


					}else{
						return "SUCCESS";
					}


				}else if(age>50){

					Date lastClaimApplyDate = claimApplicationExpenseDetailsList.get(claimApplicationExpenseDetailsList.size()-1).getClaimApplication().getRequestDate();
					Calendar lastClaimCalendarDate = Calendar.getInstance();
					lastClaimCalendarDate.setTime(lastClaimApplyDate);
					Calendar currentDate = Calendar.getInstance();
					currentDate.setTime(new Date());
					if(lastClaimCalendarDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)){
						return "CAN'T APPLY FOR SECOND CLAIM IN SAME YEAR";
					}else{
						return "SUCCESS";
					}

				}

			
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return "ERROR";

	}

	public static int calculateAge(Date birthDate, Date currentDate) {
		// Create Calendar instances for the birth date and the current date
		Calendar birthCalendar = Calendar.getInstance();
		Calendar currentCalendar = Calendar.getInstance();

		birthCalendar.setTime(birthDate);
		currentCalendar.setTime(currentDate);

		int birthYear = birthCalendar.get(Calendar.YEAR);
		int birthMonth = birthCalendar.get(Calendar.MONTH);
		int birthDay = birthCalendar.get(Calendar.DAY_OF_MONTH);

		int currentYear = currentCalendar.get(Calendar.YEAR);
		int currentMonth = currentCalendar.get(Calendar.MONTH);
		int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);

		// Calculate the age
		int age = currentYear - birthYear;

		// Adjust age if the birthday has not occurred yet this year
		if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
			age--;
		}

		return age;
	}


	@RequestMapping("/fetchClaimsForPastNoOfYears")
	public @ResponseBody List<ClaimApplicationExpenseDetailsBean> fetchClaimsForPastNoOfYears(@RequestParam("billDate") String billDate,
																							  @RequestParam("noOfYears") Long noOfYears, @RequestParam("expenseCategoryId") Long expenseCategoryId,
																							  @RequestParam("expenseItemId") Long expenseItemId, @RequestParam("empId") Long empId, HttpSession session) {
		logger.info("billDate:: " + billDate);
		logger.info("noOfYears:: " + noOfYears);
		logger.info("Expense Category Id:: " + expenseCategoryId);
		logger.info("Expense Item Id:: " + expenseItemId);

		List<ClaimApplicationExpenseDetailsBean> cb = new ArrayList<ClaimApplicationExpenseDetailsBean>();
		try {
			cb = claimApplicationService.fetchClaimsForPastNoOfYears(billDate,noOfYears,expenseCategoryId, expenseItemId, empId);
			return cb;
		} catch (Exception e) {
			// Handle other exceptions
			e.printStackTrace();
			// You may want to log the exception or take appropriate action
		}
		return cb;
	}
	
	/**
	 * Added on  17-08-2024
	 * For Rotation Validation
	 * Get Past No Of Year Claim Detail For E0 To E9 in Furniture Claim
	 * @return ClaimApplicationExpenseDetailsBean
	 */
	@RequestMapping("/fetchClaimsForPastNoOfYearsForE0-E9")
	public @ResponseBody List<ClaimApplicationExpenseDetailsBean> fetchClaimsForPastNoOfYearsForE0E9( @RequestParam("expenseCategoryId") Long expenseCategoryId,
																							  @RequestParam("expenseItemId") Long expenseItemId, @RequestParam("empId") Long empId, HttpSession session) {

		logger.info("Expense Category Id:: " + expenseCategoryId);
		logger.info("Expense Item Id:: " + expenseItemId);

		List<ClaimApplicationExpenseDetailsBean> cb = new ArrayList<ClaimApplicationExpenseDetailsBean>();
		try {
			cb = claimApplicationService.fetchClaimsForPastNoOfYearsEoE9(expenseCategoryId, expenseItemId, empId);
			return cb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cb;
	}

	
	/**
	 * Added on  30-09-2024
	 * Get Restore Amount in Particular Date And return for Furniture Claim
	 * @return restoreAmount
	 */
	@RequestMapping("/fetchEmployeeRestoreAmount")
	public @ResponseBody double getRestoreAmount(@RequestParam("billDate") String billDate, @RequestParam("expenseCategoryId") Long expenseCategoryId,
												                                   @RequestParam("expenseItemId") Long expenseItemId,
																				   @RequestParam("empId") Long empId,
																				   @RequestParam("noOfYear") Long noOfYear,
																				   @RequestParam("remainingAmount") Long remainingAmount,
																				   @RequestParam("maximumAmount") Long maximumAmount ,HttpSession session) {
		try {
			 Double restoreAmount = claimApplicationService.getRestoreAmount(billDate, noOfYear, expenseCategoryId, expenseItemId, empId, maximumAmount, remainingAmount);
			return restoreAmount;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

	}
	
	
//	For Vehicle Expense Claim Type Fueltype Rate Not Configured then make Validation
	
	
	@ResponseBody
	@GetMapping("/fetchVehicleExpenseDetailMonth")
	public	boolean fetchMonth(@RequestParam("fromMonth")String fromMonth,
														  @RequestParam("toMonth")String toMonth,
														  @RequestParam("fuelType")String fuelType,
														  @RequestParam("expenseItem") Long expenseItem,
														  @RequestParam("expenseCategory") Long expenseCategory,
														  HttpSession session){


		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");


			VehicleRunningMaintenanceExpense details =
					vehicleRepo.
							findAllBetweenStartMonthAndEndMonthAndIsDeleteFalseAndCompanyIdAndBranchId(fromMonth, toMonth, fuelType,expenseItem,expenseCategory, companyId, companyBranchId);


		System.out.println("Here...=========>>>>>>>"+details);



		return details != null;




	}
	
	@ResponseBody
	@GetMapping("/getEmpAppointemntDateFromId")
	public Date getEmpAppointemntDateFromId(@RequestParam("id") Long empId){

		try
		{
			Employee emp = employeeRepository.findById(empId).get();
			return emp.getDateOfAppointment();
		}catch (Exception e){
			e.printStackTrace();
		}



		return null;
	}
	
	@GetMapping(value = "/findbyclaimyearandmonth")
	public @ResponseBody JsonResponse findbyclaimyearandmonth(
			 @RequestParam("year") Long year,
			@RequestParam("expenseItemId") Long expenseItemId, @RequestParam("empId") Long empId,@RequestParam("month") String month, Model model,
			HttpServletRequest request, final RedirectAttributes redirectAttributes, HttpSession session) {
		JsonResponse res = new JsonResponse();
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		try {
			Long Count = claimApplicationExpenseDetailsRepo
					.getMonthDataCount(month,year,empId,expenseItemId,companyId,companyBranchId );

			res.setObj(Count);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}



	@GetMapping("/eligibility")
	public ResponseEntity<EligibilityResponse> getEligibility(@RequestParam("empId") Long empId,@RequestParam("expenseCatId") Long expenseItemId,HttpServletRequest request, final RedirectAttributes redirectAttributes, HttpSession session) {
		EligibilityResponse response = new EligibilityResponse();
		System.out.println(empId+"scscsccccccc"+ expenseItemId);
		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		GradeMaster gradeMaster = gradeMasterRepository.findGradeByEmpId(companyId, companyBranchId, empId);
		Employee employee = employeeRepository.findByIdAndIsDeleteFalseAndCompanyIdAndCompanyBranchId(empId, companyId, companyBranchId);
		ClaimCategoryModel configration_master = claimCategoryMasterRepo.findByIdComIdAndBranchId(expenseItemId, companyId, companyBranchId);
		ClaimConfiguration claimConfiguration = claimConfigurationRepository.findByClaimCategoryMaster(configration_master.getId(), companyId, companyBranchId);

		response.setName(employee.getSalutation()+" "+employee.getFirstName()+" "+employee.getLastName());
		response.setGrade(gradeMaster.getGradeName());
		response.setCategoryName(configration_master.getClaimName());

		List<EligibilityResponse.EligibilityItem> generalEligibility = new ArrayList<>();
		List<EligibilityResponse.ItemEligibility> itemEligibility = new ArrayList<>();
		try {
		if(claimConfiguration != null && !claimConfiguration.isOnActual())
		{
			if(claimConfiguration.getAllowanceEligible().equalsIgnoreCase("grade") ) {
				ClaimGradeDetails claimGradeDetails = claimGradeDetailsRepository.findByGradeIdAndCategoryId(gradeMaster.getId(), claimConfiguration.getId());
				if (claimGradeDetails != null){

					List<ClaimItemConfiguration> byClaimConfigurationIdAndIsDeleteFalseAndGradeMasterId = claimItemConfigurationRepository.findByClaimConfigurationIdAndIsDeleteFalseAndGradeMasterId(claimConfiguration.getId(), gradeMaster.getId(), companyId, companyBranchId);
					String gapInYear = "";
					if (byClaimConfigurationIdAndIsDeleteFalseAndGradeMasterId.isEmpty()) {
						List<ClaimItemConfiguration> allByConfigurationIdIdAndIsDeleteFalse = claimItemConfigurationRepository.findAllByConfigurationIdIdAndIsDeleteFalse(claimConfiguration.getId());
						if (allByConfigurationIdIdAndIsDeleteFalse.get(0).getGradeMasterId() == null && allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear() != null) {
							if(!Objects.equals(allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear()+"", "0")) {
								gapInYear = "every " + allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear() + " years";
							}
						}
					}  else {
						if(!Objects.equals(byClaimConfigurationIdAndIsDeleteFalseAndGradeMasterId.get(0).getGapInYear()+"", "0")) {
							gapInYear = "every " + byClaimConfigurationIdAndIsDeleteFalseAndGradeMasterId.get(0).getGapInYear() + " years";
						}
					}

					if(claimGradeDetails.getIsPerMonth()){
						gapInYear = "per month";
					}

					String allowance="";
					if (!claimGradeDetails.getIsActual() && !claimGradeDetails.getIsLitre() && claimGradeDetails.getAmount() != null) {
						allowance="upto ₹ " + claimGradeDetails.getAmount();
					} else if (claimGradeDetails != null && !claimGradeDetails.getIsActual() && claimGradeDetails.getIsLitre()) {
						allowance=claimGradeDetails.getAmount() + " Litres";
					} else {
						allowance="As per bill.";
					}

					System.out.println("Gap in Year Show----------"+gapInYear);
					generalEligibility.add(new EligibilityResponse.EligibilityItem("Eligible", "Yes"));
					if (!allowance.equalsIgnoreCase("")){
						generalEligibility.add(new EligibilityResponse.EligibilityItem("Allowance", allowance));
					}
					if(claimConfiguration.getCalendar().equalsIgnoreCase("FinancialYear")){
						generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "April to March"));
					}else {
						generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "January to December"));

					}
					if (!Objects.equals(gapInYear, "")) {
						generalEligibility.add(new EligibilityResponse.EligibilityItem("Applicability", gapInYear));
					}

			    }
				else {
					generalEligibility.add(new EligibilityResponse.EligibilityItem("Eligible", "No"));
				}
			} else if (claimConfiguration.getAllowanceEligible().equalsIgnoreCase("item")) {

				if (!configration_master.getClaimName().contains("Medical Outdoor")){
					List<ClaimItemConfiguration> claimItemConfigurationList = claimItemConfigurationRepository.findByClaimConfigurationIdAndIsDeleteFalse(claimConfiguration.getId(), companyId, companyBranchId);
					List<ClaimItemDetailsForAllownce> claimItemDetailsForAllownces = claimItemDetailsForAllowanceRepository.findByConfigurationIdAndIsDeleteFalse(claimConfiguration.getId());

					if (!claimItemConfigurationList.isEmpty() && !claimItemDetailsForAllownces.isEmpty()) {
						for (ClaimItemConfiguration config : claimItemConfigurationList) {
							claimItemDetailsForAllownces.stream()
									.filter(allowance ->
											allowance.getItemMasterForClaimMatrix() != null &&
													allowance.getItemMasterForClaimMatrix().getId().equals(config.getItemMasterForClaimMatrixId().getId())
									)
									.findFirst() // Use findFirst to fetch the first matching allowance
									.ifPresent(matchingAllowance -> {
										String applyCondition = "";
										String amount="";

										if (config.getGapInYear() != null && !Objects.equals(config.getGapInYear()+"", "0")) {
											applyCondition = "every " + config.getGapInYear() + " years";
										}
										if (!matchingAllowance.getIsActual() && !Objects.equals(matchingAllowance.getAmount()+"", "0")){
											amount="upto ₹ " +matchingAllowance.getAmount();
										}
										String calculation="";
										if(matchingAllowance.getIsBasicDa()){
											calculation="1 Month Basic+Da";
										}
										itemEligibility.add(new EligibilityResponse.ItemEligibility(config.getItemMasterForClaimMatrixId().getItemName(), applyCondition, calculation, amount));
									});
						}

						generalEligibility.add(new EligibilityResponse.EligibilityItem("Eligible", "Yes"));
						generalEligibility.add(new EligibilityResponse.EligibilityItem("Allowance", "As per bill."));
						if(claimConfiguration.getCalendar().equalsIgnoreCase("FinancialYear")){
							generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "April to March"));
						}else {
							generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "January to December"));

						}



					}
				}
				else {
					if((configration_master.getClaimName().contains("Medical Outdoor  Executive") && (gradeMaster.getGradeName().matches("E[0-9]") || gradeMaster.getGradeName().contains("CMD") || gradeMaster.getGradeName().contains("DIRECTOR")))){
						List<ClaimItemConfiguration> claimItemConfigurationList = claimItemConfigurationRepository.findByClaimConfigurationIdAndIsDeleteFalse(claimConfiguration.getId(), companyId, companyBranchId);
						List<ClaimItemDetailsForAllownce> claimItemDetailsForAllownces = claimItemDetailsForAllowanceRepository.findByConfigurationIdAndIsDeleteFalse(claimConfiguration.getId());

						if (!claimItemConfigurationList.isEmpty() && !claimItemDetailsForAllownces.isEmpty()) {
							for (ClaimItemConfiguration config : claimItemConfigurationList) {
								claimItemDetailsForAllownces.stream()
										.filter(allowance ->
												allowance.getItemMasterForClaimMatrix() != null &&
														allowance.getItemMasterForClaimMatrix().getId().equals(config.getItemMasterForClaimMatrixId().getId())
										)
										.findFirst() // Use findFirst to fetch the first matching allowance
										.ifPresent(matchingAllowance -> {
											String applyCondition = "";
											String amount="";

											if (config.getGapInYear() != null && !Objects.equals(config.getGapInYear()+"", "0")) {
												applyCondition = "every " + config.getGapInYear() + " years";
											}
											if (!matchingAllowance.getIsActual() && !Objects.equals(matchingAllowance.getAmount()+"", "0")){
												amount="upto ₹ " +matchingAllowance.getAmount();
											}
											String calculation="";
											if(matchingAllowance.getIsBasicDa()){
												calculation="1 Month Basic+Da";
											}
											itemEligibility.add(new EligibilityResponse.ItemEligibility(config.getItemMasterForClaimMatrixId().getItemName(), applyCondition, calculation, amount));
										});
							}

							generalEligibility.add(new EligibilityResponse.EligibilityItem("Eligible", "Yes"));
							generalEligibility.add(new EligibilityResponse.EligibilityItem("Allowance", "As per bill."));
							if(claimConfiguration.getCalendar().equalsIgnoreCase("FinancialYear")){
								generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "April to March"));
							}else {
								generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "January to December"));

							}



						}
					} else if ((configration_master.getClaimName().contains("Medical Outdoor Non Executive") && (gradeMaster.getGradeName().matches("NE[0-9]")))) {
						List<ClaimItemConfiguration> claimItemConfigurationList = claimItemConfigurationRepository.findByClaimConfigurationIdAndIsDeleteFalse(claimConfiguration.getId(), companyId, companyBranchId);
						List<ClaimItemDetailsForAllownce> claimItemDetailsForAllownces = claimItemDetailsForAllowanceRepository.findByConfigurationIdAndIsDeleteFalse(claimConfiguration.getId());

						if (!claimItemConfigurationList.isEmpty() && !claimItemDetailsForAllownces.isEmpty()) {
							for (ClaimItemConfiguration config : claimItemConfigurationList) {
								claimItemDetailsForAllownces.stream()
										.filter(allowance ->
												allowance.getItemMasterForClaimMatrix() != null &&
														allowance.getItemMasterForClaimMatrix().getId().equals(config.getItemMasterForClaimMatrixId().getId())
										)
										.findFirst() // Use findFirst to fetch the first matching allowance
										.ifPresent(matchingAllowance -> {
											String applyCondition = "";
											String amount="";

											if (config.getGapInYear() != null && !Objects.equals(config.getGapInYear()+"", "0")) {
												applyCondition = "every " + config.getGapInYear() + " years";
											}
											if (!matchingAllowance.getIsActual() && !Objects.equals(matchingAllowance.getAmount()+"", "0")){
												amount="upto ₹ " +matchingAllowance.getAmount();
											}
											String calculation="";
											if(matchingAllowance.getIsBasicDa()){
												calculation="1 Month Basic+Da";
											}
											itemEligibility.add(new EligibilityResponse.ItemEligibility(config.getItemMasterForClaimMatrixId().getItemName(), applyCondition, calculation, amount));
										});
							}

							generalEligibility.add(new EligibilityResponse.EligibilityItem("Eligible", "Yes"));
							generalEligibility.add(new EligibilityResponse.EligibilityItem("Allowance", "As per bill."));
							if(claimConfiguration.getCalendar().equalsIgnoreCase("FinancialYear")){
								generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "April to March"));
							}else {
								generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "January to December"));

							}



						}
					}
					else {
						generalEligibility.add(new EligibilityResponse.EligibilityItem("Eligible", "No"));
					}
				}
				/*if((configration_master.getClaimName().contains("Medical Outdoor  Executive") && (gradeMaster.getGradeName().matches("E[0-9]") || gradeMaster.getGradeName().contains("CMD") || gradeMaster.getGradeName().contains("DIRECTOR")))){

				} else if (!(configration_master.getClaimName().contains("Medical Outdoor  Executive") && (gradeMaster.getGradeName().matches("NE[0-9]")))) {

				}*/

			 }
			else if (claimConfiguration.getAllowanceEligible().equalsIgnoreCase("basicDa")) {

				List<ClaimItemConfiguration> byClaimConfigurationIdAndIsDeleteFalseAndGradeMasterId = claimItemConfigurationRepository.findByClaimConfigurationIdAndIsDeleteFalseAndGradeMasterId(claimConfiguration.getId(), gradeMaster.getId(), companyId, companyBranchId);
				 ClaimAllowanceBasicDetails allByConfigurationIdAndIsDeleteFalse = claimAllowanceBasicDetailsRepository.findAllByConfigurationIdAndIsDeleteFalse_(claimConfiguration.getId());
				String gapInYear = "";
				if (byClaimConfigurationIdAndIsDeleteFalseAndGradeMasterId.isEmpty()) {
					List<ClaimItemConfiguration> allByConfigurationIdIdAndIsDeleteFalse = claimItemConfigurationRepository.findAllByConfigurationIdIdAndIsDeleteFalse(claimConfiguration.getId());
					if (allByConfigurationIdIdAndIsDeleteFalse.get(0).getGradeMasterId() == null && allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear() != null) {
						if(!Objects.equals(allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear()+"", "0")) {
							gapInYear = "every " + allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear() + " years";
						}
					}
				}  else {
					if(!Objects.equals(byClaimConfigurationIdAndIsDeleteFalseAndGradeMasterId.get(0).getGapInYear()+"", "0")) {
						gapInYear = "every " + byClaimConfigurationIdAndIsDeleteFalseAndGradeMasterId.get(0).getGapInYear() + " years";
					}
				}

				String allowance="";
				if(allByConfigurationIdAndIsDeleteFalse != null){
					if(allByConfigurationIdAndIsDeleteFalse.getBasicPercentage() != null){
						allowance=allByConfigurationIdAndIsDeleteFalse.getBasicPercentage()+"% of Basic+Da.";
					}
					else {
						allowance=allByConfigurationIdAndIsDeleteFalse.getBasicMonth()+" Month of Basic+Da.";
					}
					generalEligibility.add(new EligibilityResponse.EligibilityItem("Eligible", "Yes"));
					if(!allowance.equalsIgnoreCase("")) {
						generalEligibility.add(new EligibilityResponse.EligibilityItem("Allowance", allowance));
					}

					if(claimConfiguration.getCalendar().equalsIgnoreCase("FinancialYear")){
						generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "April to March"));
					}else {
						generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "January to December"));

					}

					if (!Objects.equals(gapInYear, "")) {
						generalEligibility.add(new EligibilityResponse.EligibilityItem("Applicability", gapInYear));
					}
				}


			  }
			}else {
			     if(claimConfiguration != null){

					 if (!configration_master.getClaimName().contains("Medical Indoor")) {
						 List<ClaimItemConfiguration> allByConfigurationIdIdAndIsDeleteFalse = claimItemConfigurationRepository.findAllByConfigurationIdIdAndIsDeleteFalse(claimConfiguration.getId());

						 generalEligibility.add(new EligibilityResponse.EligibilityItem("Eligible", "Yes"));
						 generalEligibility.add(new EligibilityResponse.EligibilityItem("Allowance", "As per bill."));
						 if(claimConfiguration.getCalendar().equalsIgnoreCase("FinancialYear")){
							 generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "April to March"));
						 }else {
							 generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "January to December"));
						 }
						 String gapInYear="";
						 if (allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear() != null) {
							 if (!Objects.equals(String.valueOf(allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear()), "0")) {
								 generalEligibility.add(new EligibilityResponse.EligibilityItem("Applicability", "every " + allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear() + " years"));

							 }
						 }
					 }else {
						 if((configration_master.getClaimName().contains("Medical Indoor  Executive") && (gradeMaster.getGradeName().matches("E[0-9]") || gradeMaster.getGradeName().contains("CMD") || gradeMaster.getGradeName().contains("DIRECTOR")))) {
							 List<ClaimItemConfiguration> allByConfigurationIdIdAndIsDeleteFalse = claimItemConfigurationRepository.findAllByConfigurationIdIdAndIsDeleteFalse(claimConfiguration.getId());

							 generalEligibility.add(new EligibilityResponse.EligibilityItem("Eligible", "Yes"));
							 generalEligibility.add(new EligibilityResponse.EligibilityItem("Allowance", "As per bill."));
							 if(claimConfiguration.getCalendar().equalsIgnoreCase("FinancialYear")){
								 generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "April to March"));
							 }else {
								 generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "January to December"));
							 }
							 String gapInYear="";
							 if (allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear() != null) {
								 if (!Objects.equals(String.valueOf(allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear()), "0")) {
									 generalEligibility.add(new EligibilityResponse.EligibilityItem("Applicability", "every " + allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear() + " years"));

								 }
							 }
						 }else if ((configration_master.getClaimName().contains("Medical Indoor Non Executive") && (gradeMaster.getGradeName().matches("NE[0-9]")))) {
							 List<ClaimItemConfiguration> allByConfigurationIdIdAndIsDeleteFalse = claimItemConfigurationRepository.findAllByConfigurationIdIdAndIsDeleteFalse(claimConfiguration.getId());

							 generalEligibility.add(new EligibilityResponse.EligibilityItem("Eligible", "Yes"));
							 generalEligibility.add(new EligibilityResponse.EligibilityItem("Allowance", "As per bill."));
							 if(claimConfiguration.getCalendar().equalsIgnoreCase("FinancialYear")){
								 generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "April to March"));
							 }else {
								 generalEligibility.add(new EligibilityResponse.EligibilityItem("Calendar", "January to December"));
							 }
							 String gapInYear="";
							 if (allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear() != null) {
								 if (!Objects.equals(String.valueOf(allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear()), "0")) {
									 generalEligibility.add(new EligibilityResponse.EligibilityItem("Applicability", "every " + allByConfigurationIdIdAndIsDeleteFalse.get(0).getGapInYear() + " years"));

								 }
							 }
						 }else {
							 generalEligibility.add(new EligibilityResponse.EligibilityItem("Eligible", "No"));
						 }
					 }

				 }
		     }

		response.setItemEligibility(itemEligibility);
		response.setGeneralEligibility(generalEligibility);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return ResponseEntity.ok(response);
	}


	@GetMapping("/history")
	public ResponseEntity<?> getHistory(@RequestParam("empId") Long empId, @RequestParam("expenseCatId") Long expenseCatId,
													   HttpServletRequest request, final RedirectAttributes redirectAttributes, HttpSession session) {

		List<ClaimApplicationExpenseDetails> claimApplicationExpenseDetails;
		logger.info("empId:: " + empId+ " expenseCatId:: " + expenseCatId);
		try {


			claimApplicationExpenseDetails = claimApplicationExpenseDetailsRepo.getAllExpenseByClaimIdLimit(empId, expenseCatId);

			// Group the claim application expense details by claim application
			Map<ClaimApplication, List<ClaimApplicationExpenseDetails>> collect = claimApplicationExpenseDetails.stream().collect(Collectors.groupingBy(ClaimApplicationExpenseDetails::getClaimApplication));

			// Calculate the total approved amount for each claim
			for(Map.Entry<ClaimApplication, List<ClaimApplicationExpenseDetails>> entry : collect.entrySet()){
				ClaimApplication claim = entry.getKey();
				List<ClaimApplicationExpenseDetails> claimApplicationExpenseDetailsList = entry.getValue();
				double approveAmount = claimApplicationExpenseDetailsList.stream()
						.filter(claimApplicationExpenseDetails1 -> claimApplicationExpenseDetails1.getApprovedamountl2() != null).mapToDouble(ClaimApplicationExpenseDetails::getApprovedamountl2).sum();
				claim.setApprovedamountl2(approveAmount);
			}

			List<Map.Entry<ClaimApplication, List<ClaimApplicationExpenseDetails>>> sortedList = new ArrayList<>(collect.entrySet());
			sortedList.sort((entry1, entry2) ->
					entry2.getKey().getRequestDate().compareTo(entry1.getKey().getRequestDate()));

// Create a new linked map or reassign the sorted entries
			Map<ClaimApplication, List<ClaimApplicationExpenseDetails>> sortedCollect = new LinkedHashMap<>();
			for (Map.Entry<ClaimApplication, List<ClaimApplicationExpenseDetails>> entry : sortedList) {
				sortedCollect.put(entry.getKey(), entry.getValue());
			}

			List<Map<String, Object>> result = new ArrayList<>();

			for (Map.Entry<ClaimApplication, List<ClaimApplicationExpenseDetails>> entry : sortedCollect.entrySet()) {
				Map<String, Object> map = new HashMap<>();
				map.put("claimApplication", entry.getKey()); // Serialize ClaimApplication as a value
				map.put("expenseDetails", entry.getValue());
				result.add(map);
			}

			return ResponseEntity.ok(result);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(400).body(null);
		}
	}


	   
	
	   @ResponseBody
	   @GetMapping(value = "/getFurnitureItems")
		public List<FurnitureItems> getFurnitureItems(@RequestParam("expenseItemId") Long expenseItemId
				,HttpServletRequest request, final RedirectAttributes redirectAttributes, HttpSession session) {
		    // Get the Furniture items based on the selected expense item
		    // You can add your logic to filter based on expense item
			Long companyId = (Long) session.getAttribute("companyId");
			Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		    List<FurnitureItems> furnitureItems = furnitureItemsrepository.findFurnitureData(expenseItemId,companyId, companyBranchId);
	
		    // Return the list as JSON
		    return furnitureItems;
		}

	@ResponseBody
	@GetMapping(value = "/getFurnitureSubItems")
	public List<FurnitureItemSubCategory> getFurnitureSubItems(
			@RequestParam("furnitureItemId") String furnitureItemId, // Adjusted to match AJAX parameter
			HttpServletRequest request,
			HttpSession session) {

		// Fetch sub-items for the given furnitureItemId
		List<FurnitureItemSubCategory> furnitureSubItems = furnitureItemSubCategoryRepository.findFurnitureSubData(furnitureItemId);

		// Log the result for debugging
		System.out.println("Furniture Sub-Items: " + furnitureSubItems);

		// Return the list of sub-items as JSON
		return furnitureSubItems;
	}



		@GetMapping("/getEntertainmentSum")
		public ResponseEntity<Double> getEntertainmentSum(
				@RequestParam Long empId,
				@RequestParam Long expenseCategoryId,
				@RequestParam Long expenseItemId,
				@RequestParam String billDate,
				HttpServletRequest request,
				HttpSession session) {

			Long companyId = (Long) session.getAttribute("companyId");
			Long companyBranchId = (Long) session.getAttribute("companyBranchId");

			// Parse billDate
			SimpleDateFormat inputFormatter = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd");
			Date parsedBillDate;
			try {
				parsedBillDate = inputFormatter.parse(billDate);
			} catch (ParseException e) {
				return ResponseEntity.badRequest().body(null);
			}

			// Extract year and month
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(parsedBillDate);

			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);

			// Calculate financial year start and end dates
			String formattedStartDate;
			String formattedEndDate;

			Calendar financialYearCalendar = Calendar.getInstance();

			if (month < Calendar.APRIL) { // Before April
				// Start Date: April 1st of the previous year
				financialYearCalendar.set(year - 1, Calendar.APRIL, 1, 0, 0, 0);
				formattedStartDate = outputFormatter.format(financialYearCalendar.getTime());

				// End Date: March 31st of the current year
				financialYearCalendar.set(year, Calendar.MARCH, 31, 23, 59, 59);
				formattedEndDate = outputFormatter.format(financialYearCalendar.getTime());
			} else { // April or later
				// Start Date: April 1st of the current year
				financialYearCalendar.set(year, Calendar.APRIL, 1, 0, 0, 0);
				formattedStartDate = outputFormatter.format(financialYearCalendar.getTime());

				// End Date: March 31st of the next year
				financialYearCalendar.set(year + 1, Calendar.MARCH, 31, 23, 59, 59);
				formattedEndDate = outputFormatter.format(financialYearCalendar.getTime());
			}

			System.out.println("Formatted Start Date: " + formattedStartDate);
			System.out.println("Formatted End Date: " + formattedEndDate);

			// Convert formatted dates to java.sql.Date
			java.sql.Date sqlStartDate = java.sql.Date.valueOf(formattedStartDate);
			java.sql.Date sqlEndDate = java.sql.Date.valueOf(formattedEndDate);

			// Pass formatted dates to repository method
			Double totalDifference = claimApplicationExpenseDetailsRepository.getEntertainmentSum(
					empId, expenseCategoryId, expenseItemId,
					sqlStartDate, sqlEndDate,
					companyId, companyBranchId);

			return ResponseEntity.ok(totalDifference);
		}

	@GetMapping("/findMembershipTake")
	public ResponseEntity<String> findMembershipTake(
			@RequestParam("finacialyear") String financialYear,
			@RequestParam("expenseItemId") Long expenseItemId,
			@RequestParam("empId") Long empId,
			@RequestParam("memberShipType") String membershipType,	HttpServletRequest request,
			HttpSession session) {

		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");
		String data = String.format("Financial Year: %s, Expense Item ID: %s, Employee ID: %s, Membership Type: %s",
				financialYear, expenseItemId, empId, membershipType);
		System.out.println(data);
		 Long yearDataCount = this.claimApplicationExpenseDetailsRepository.getYearDataCount(financialYear,empId, expenseItemId , companyId, companyBranchId, membershipType);
          String response="";
		 if (membershipType.equalsIgnoreCase("National")) {
			 if (yearDataCount >= 2) {
				 response="National";
			 }else {
				 response="NO";
			 }
		 } else if (membershipType.equalsIgnoreCase("Foreign")) {
			 if (yearDataCount >= 1) {
				 response="Foreign";
			 }else {
				 response="NO";
			 }
		 }
		return ResponseEntity.ok(response);
	}


	@GetMapping("/yearGapValidation")
	public ResponseEntity<Long> yearGapValidation(
			@RequestParam("billDate") String billDate,
			@RequestParam("expenseItemId") Long expenseItemId,
			@RequestParam("empId") Long empId,
			@RequestParam("incurredFor") String incurredFor,
			@RequestParam("noOfYear") Long noOfYear,HttpSession session) {

		Long companyId = (Long) session.getAttribute("companyId");
		Long companyBranchId = (Long) session.getAttribute("companyBranchId");

		String data = String.format("billDate : %s, Expense Item ID: %s, Employee ID: %s, noOfYear: %s,incurredFor: %s",
				billDate, expenseItemId, empId, noOfYear,incurredFor);
		System.out.println(data);
		try {
			 Long total = claimApplicationService.yearGapValidation(billDate, expenseItemId,empId,noOfYear, incurredFor, companyId, companyBranchId );
			return ResponseEntity.ok(total);
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity.ok(0L);
		}

	}

}
