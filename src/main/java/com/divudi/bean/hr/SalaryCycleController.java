/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.hr;

import com.divudi.bean.common.SessionController;
import com.divudi.bean.common.UtilityController;
import com.divudi.data.hr.PaysheetComponentType;
import com.divudi.data.hr.ReportKeyWord;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Staff;
import com.divudi.entity.hr.PaysheetComponent;
import com.divudi.entity.hr.SalaryCycle;
import com.divudi.entity.hr.StaffPaysheetComponent;
import com.divudi.entity.hr.StaffSalary;
import com.divudi.entity.hr.StaffSalaryComponant;
import com.divudi.facade.PaysheetComponentFacade;
import com.divudi.facade.SalaryCycleFacade;
import com.divudi.facade.StaffFacade;
import com.divudi.facade.StaffPaysheetComponentFacade;
import com.divudi.facade.StaffSalaryComponantFacade;
import com.divudi.facade.StaffSalaryFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class SalaryCycleController implements Serializable {

    private SalaryCycle current;
    private List<SalaryCycle> salaryCycleList;
    @EJB
    private SalaryCycleFacade facade;
    @Inject
    private SessionController sessionController;
    List<SalaryCycle> salaryCycles;
    List<String> headersAdd;
    List<Double> footerAdd;
    List<Double> footerSub;
    Institution institution;
    Department department;
    double adjustmentToAllowances;

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<String> getHeadersAdd() {
        if (headersAdd == null) {
            headersAdd = new ArrayList<>();
        }
        return headersAdd;
    }

    public void setHeadersAdd(List<String> headersAdd) {
        this.headersAdd = headersAdd;
    }

    public List<SalaryCycle> completeSalaryCycle(String qry) {

        String sql = "";
        HashMap hm = new HashMap();
        sql = "select c from SalaryCycle c "
                + " where c.retired=false "
                + " and upper(c.name) like :q ";
        sql += " order by c.id desc";
        hm.put("q", "%" + qry.toUpperCase() + "%");
        salaryCycles = getFacade().findBySQL(sql, hm);

        return salaryCycles;
    }

    public void listAllSalaryCycles() {
        String sql;
        sql = "select c from SalaryCycle c "
                + " where c.retired=false "
                + " order by c.id desc";
        salaryCycles = getFacade().findBySQL(sql, 20);
    }

    double brVal = 0.0;
    double basicVal = 0.0;
    double mercAll = 0.0;
    double poyaAll = 0.0;
    double dayOffAll = 0.0;
    double slpAll = 0.0;
    double noPayBasic = 0.0;
    double adjstBasic = 0.0;
    double epfDeduct = 0.0;
    double tranGrossSal = 0.0;
    double adjustAll = 0.0;
    double noPayCount = 0.0;
    double transTotAll = 0.0;
    double epfStaffVal = 0.0;
    double transTotDeduct = 0.0;
    double transNetSal = 0.0;
    double epfComVal = 0.0;
    double etfComVal = 0.0;
    double noPayValAll = 0.0;

    public void SalaryTotalCalculation(List<StaffSalary> stfSalary) {
        brVal = 0.0;
        basicVal = 0.0;
        mercAll = 0.0;
        poyaAll = 0.0;
        dayOffAll = 0.0;
        slpAll = 0.0;
        noPayBasic = 0.0;
        adjstBasic = 0.0;
        epfDeduct = 0.0;
        tranGrossSal = 0.0;
        adjustAll = 0.0;
        noPayCount = 0.0;
        transTotAll = 0.0;
        epfStaffVal = 0.0;
        transTotDeduct = 0.0;
        transNetSal = 0.0;
        epfComVal = 0.0;
        etfComVal = 0.0;
        noPayValAll = 0.0;

        for (StaffSalary stfsal : stfSalary) {
            brVal += stfsal.getBrValue();
            basicVal += (stfsal.getBasicValue() - stfsal.getBrValue());
            mercAll += stfsal.getMerchantileAllowanceValue();
            poyaAll += stfsal.getPoyaAllowanceValue();
            dayOffAll += stfsal.getDayOffAllowance();
            slpAll += stfsal.getSleepingDayAllowance();
            noPayBasic += stfsal.getNoPayValueBasic();
            adjstBasic += stfsal.getAdjustmentToBasic();
            tranGrossSal += stfsal.getTransGrossSalary();
            epfDeduct += stfsal.getTransEpfEtfDiductableSalary();
            adjustAll += stfsal.getAdjustmentToAllowance();
            noPayCount += stfsal.getNoPayCount();
            transTotAll += stfsal.getTransTotalAllowance();
            epfStaffVal += stfsal.getEpfStaffValue();
            transTotDeduct += stfsal.getTransTotalDeduction();
            transNetSal += stfsal.getTransNetSalry();
            epfComVal += stfsal.getEpfCompanyValue();
            etfComVal += stfsal.getEtfCompanyValue();
            noPayValAll += stfsal.getNoPayValueAllowance();

        }

    }

    @EJB
    StaffSalaryFacade staffSalaryFacade;

    @EJB
    PaysheetComponentFacade paysheetComponentFacade;

    @EJB
    StaffPaysheetComponentFacade staffPaysheetComponentFacade;

    public String hr_report_all_staff_salary() {
        listAllSalaryCycles();
        return "/hr/hr_report_all_staff_salary";
    }

    public List<SalaryCycle> getSalaryCycles() {
        if (salaryCycles == null) {
            listAllSalaryCycles();
        }
        return salaryCycles;
    }

    public void setSalaryCycles(List<SalaryCycle> salaryCycles) {
        this.salaryCycles = salaryCycles;
    }

    private boolean errorCheck() {
        if (current == null) {
            return true;
        }

        if (current.getSalaryFromDate() == null) {
            return true;
        }

        if (current.getSalaryToDate() == null) {
            return true;
        }

        if (current.getWorkedFromDate() == null) {
            return true;
        }

        if (current.getWorkedToDate() == null) {
            return true;
        }

        //Check Salry Cycle
        String sql = "Select s from SalaryCycle s "
                + " where s.retired=false "
                + " and (s.salaryFromDate=:sfd "
                + " or s.salaryToDate=:std "
                + " or s.workedFromDate=:wfd "
                + " or s.workedToDate=:wtd ) ";
        HashMap hm = new HashMap();
        hm.put("sfd", getCurrent().getSalaryFromDate());
        hm.put("std", getCurrent().getSalaryToDate());
        hm.put("wfd", getCurrent().getWorkedFromDate());
        hm.put("wtd", getCurrent().getWorkedToDate());

        SalaryCycle salaryCycle = facade.findFirstBySQL(sql, hm);
        if (salaryCycle != null) {
            UtilityController.addErrorMessage("Salary Cycle Already Exist For This Date");
            return true;
        }

        //Check Salary Form Date
        sql = "Select s from SalaryCycle s "
                + " where s.retired=false "
                + " and s.salaryFromDate<=:sfd "
                + " and s.salaryToDate>=:sfd ";
        hm = new HashMap();
        hm.put("sfd", getCurrent().getSalaryFromDate());

        salaryCycle = facade.findFirstBySQL(sql, hm);
        if (salaryCycle != null) {
            UtilityController.addErrorMessage("Salary From Date Already Exist in Other Cycle");
            return true;
        }

        //Check Salary To Date
        sql = "Select s from SalaryCycle s "
                + " where s.retired=false "
                + " and s.salaryFromDate<=:sfd "
                + " and s.salaryToDate>=:sfd ";
        hm = new HashMap();
        hm.put("sfd", getCurrent().getSalaryToDate());

        salaryCycle = facade.findFirstBySQL(sql, hm);
        if (salaryCycle != null) {
            UtilityController.addErrorMessage("Salary To Date Already Exist in Other Cycle");
            return true;
        }

        //Check Worked Form Date
        sql = "Select s from SalaryCycle s "
                + " where s.retired=false "
                + " and s.workedFromDate<=:wfd "
                + " and s.workedToDate>=:wfd  ";
        hm = new HashMap();
        hm.put("wfd", getCurrent().getWorkedFromDate());

        salaryCycle = facade.findFirstBySQL(sql, hm);
        if (salaryCycle != null) {
            UtilityController.addErrorMessage("Worked From Date Already Exist in Other Cycle");
            return true;
        }

        //Check Worked TO Date
        sql = "Select s from SalaryCycle s "
                + " where s.retired=false "
                + " and s.workedFromDate<=:wfd "
                + " and s.workedToDate>=:wfd  ";
        hm = new HashMap();
        hm.put("wfd", getCurrent().getWorkedToDate());

        salaryCycle = facade.findFirstBySQL(sql, hm);
        if (salaryCycle != null) {
            UtilityController.addErrorMessage("Worked To Date Already Exist in Other Cycle");
            return true;
        }

        return false;
    }

    public void saveSelected() {
        if (errorCheck()) {
            return;
        }

        current.processName();
        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getFacade().edit(current);
            UtilityController.addSuccessMessage("savedOldSuccessfully");
        } else {
            current.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            current.setCreater(getSessionController().getLoggedUser());
            getFacade().create(current);
            UtilityController.addSuccessMessage("savedNewSuccessfully");
        }

        //     recreateModel();
//        createSalaryCycleList();
        listAllSalaryCycles();
        current = null;
    }

    public SalaryCycleController() {
    }

    public void prepareAdd() {
        current = null;
    }

    public void delete() {

        if (current != null) {
            // removeAll();
            current.setRetired(true);
            current.setRetiredAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            current.setRetirer(getSessionController().getLoggedUser());

            getFacade().edit(current);

//            getFacade().remove(current);
//            getCurrentRoster().getSalaryCycleList().remove(getCurrent());
//            getRosterFacade().edit(getCurrentRoster());
            UtilityController.addSuccessMessage("DeleteSuccessfull");
        } else {
            UtilityController.addSuccessMessage("NothingToDelete");
        }
        //   recreateModel();

        current = null;

    }

    List<StaffSalary> staffSalarys;

    public List<StaffSalary> getStaffSalarys() {
        return staffSalarys;
    }

    public void setStaffSalarys(List<StaffSalary> staffSalarys) {
        this.staffSalarys = staffSalarys;
    }

    public SalaryCycle getCurrent() {
        if (current == null) {
            current = new SalaryCycle();
        }
        return current;
    }

    public void setCurrent(SalaryCycle current) {
        this.current = current;

    }

    public SalaryCycleFacade getFacade() {
        return facade;
    }

    public void setFacade(SalaryCycleFacade facade) {
        this.facade = facade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public List<SalaryCycle> getSalaryCycleList() {
        return salaryCycleList;
    }

    public void setSalaryCycleList(List<SalaryCycle> salaryCycleList) {
        this.salaryCycleList = salaryCycleList;
    }

    List<StaffAndSalarySalaryComponent> staffAnsAndSalarySalaryComponents;

    public List<StaffAndSalarySalaryComponent> getStaffAnsAndSalarySalaryComponents() {
        return staffAnsAndSalarySalaryComponents;
    }

    public void setStaffAnsAndSalarySalaryComponents(List<StaffAndSalarySalaryComponent> staffAnsAndSalarySalaryComponents) {
        this.staffAnsAndSalarySalaryComponents = staffAnsAndSalarySalaryComponents;
    }

    @EJB
    StaffFacade staffFacade;
    List<StaffSalary> staffSalary;

    public StaffSalaryFacade getStaffSalaryFacade() {
        return staffSalaryFacade;
    }

    public void setStaffSalaryFacade(StaffSalaryFacade staffSalaryFacade) {
        this.staffSalaryFacade = staffSalaryFacade;
    }

    public PaysheetComponentFacade getPaysheetComponentFacade() {
        return paysheetComponentFacade;
    }

    public void setPaysheetComponentFacade(PaysheetComponentFacade paysheetComponentFacade) {
        this.paysheetComponentFacade = paysheetComponentFacade;
    }

    public StaffPaysheetComponentFacade getStaffPaysheetComponentFacade() {
        return staffPaysheetComponentFacade;
    }

    public void setStaffPaysheetComponentFacade(StaffPaysheetComponentFacade staffPaysheetComponentFacade) {
        this.staffPaysheetComponentFacade = staffPaysheetComponentFacade;
    }

    public StaffFacade getStaffFacade() {
        return staffFacade;
    }

    public void setStaffFacade(StaffFacade staffFacade) {
        this.staffFacade = staffFacade;
    }

    public List<StaffSalary> getStaffSalary() {
        return staffSalary;
    }

    public void setStaffSalary(List<StaffSalary> staffSalary) {
        this.staffSalary = staffSalary;
    }

    public StaffSalaryComponantFacade getStaffSalaryComponantFacade() {
        return staffSalaryComponantFacade;
    }

    public void setStaffSalaryComponantFacade(StaffSalaryComponantFacade staffSalaryComponantFacade) {
        this.staffSalaryComponantFacade = staffSalaryComponantFacade;
    }

    public double getBrVal() {
        return brVal;
    }

    public void setBrVal(double brVal) {
        this.brVal = brVal;
    }

    public double getBasicVal() {
        return basicVal;
    }

    public void setBasicVal(double basicVal) {
        this.basicVal = basicVal;
    }

    public double getMercAll() {
        return mercAll;
    }

    public void setMercAll(double mercAll) {
        this.mercAll = mercAll;
    }

    public double getPoyaAll() {
        return poyaAll;
    }

    public void setPoyaAll(double poyaAll) {
        this.poyaAll = poyaAll;
    }

    public double getDayOffAll() {
        return dayOffAll;
    }

    public void setDayOffAll(double dayOffAll) {
        this.dayOffAll = dayOffAll;
    }

    public double getSlpAll() {
        return slpAll;
    }

    public void setSlpAll(double slpAll) {
        this.slpAll = slpAll;
    }

    public double getNoPayBasic() {
        return noPayBasic;
    }

    public void setNoPayBasic(double noPayBasic) {
        this.noPayBasic = noPayBasic;
    }

    public double getAdjstBasic() {
        return adjstBasic;
    }

    public void setAdjstBasic(double adjstBasic) {
        this.adjstBasic = adjstBasic;
    }

    public double getEpfDeduct() {
        return epfDeduct;
    }

    public void setEpfDeduct(double epfDeduct) {
        this.epfDeduct = epfDeduct;
    }

    public double getTranGrossSal() {
        return tranGrossSal;
    }

    public void setTranGrossSal(double tranGrossSal) {
        this.tranGrossSal = tranGrossSal;
    }

    public double getAdjustAll() {
        return adjustAll;
    }

    public void setAdjustAll(double adjustAll) {
        this.adjustAll = adjustAll;
    }

    public double getNoPayCount() {
        return noPayCount;
    }

    public void setNoPayCount(double noPayCount) {
        this.noPayCount = noPayCount;
    }

    public double getTransTotAll() {
        return transTotAll;
    }

    public void setTransTotAll(double transTotAll) {
        this.transTotAll = transTotAll;
    }

    public double getEpfStaffVal() {
        return epfStaffVal;
    }

    public void setEpfStaffVal(double epfStaffVal) {
        this.epfStaffVal = epfStaffVal;
    }

    public double getTransTotDeduct() {
        return transTotDeduct;
    }

    public void setTransTotDeduct(double transTotDeduct) {
        this.transTotDeduct = transTotDeduct;
    }

    public double getTransNetSal() {
        return transNetSal;
    }

    public void setTransNetSal(double transNetSal) {
        this.transNetSal = transNetSal;
    }

    public double getEpfComVal() {
        return epfComVal;
    }

    public void setEpfComVal(double epfComVal) {
        this.epfComVal = epfComVal;
    }

    public double getEtfComVal() {
        return etfComVal;
    }

    public void setEtfComVal(double etfComVal) {
        this.etfComVal = etfComVal;
    }

    public double getNoPayValAll() {
        return noPayValAll;
    }

    public List<Double> getFooterAdd() {
        if (footerAdd == null) {
            footerAdd = new ArrayList<>();
        }
        return footerAdd;
    }

    public void setFooterAdd(List<Double> footerAdd) {
        this.footerAdd = footerAdd;
    }

    public List<Double> getFooterSub() {
        if (footerAdd == null) {
            footerAdd = new ArrayList<>();
        }
        return footerSub;
    }

    public void setFooterSub(List<Double> footerSub) {
        this.footerSub = footerSub;
    }

    public void setNoPayValAll(double noPayValAll) {
        this.noPayValAll = noPayValAll;
    }

//    public void fillStaffAndSalaryComponents2() {
//
//        List<PaysheetComponent> paysheetComponents;
//        List<Staff> staffes;
//        String jpql;
//        Map m;
//
//        headersAdd = new ArrayList<>();
//
//        m = new HashMap();
//        jpql = "select distinct(spc.staffPaysheetComponent.paysheetComponent) "
//                + " from StaffSalaryComponant spc"
//                + " where spc.salaryCycle=:sc"
//                + " and spc.retired=false"
//                + " order by spc.staffPaysheetComponent.paysheetComponent.orderNo";
//        m.put("sc", current);
//        paysheetComponents = paysheetComponentFacade.findBySQL(jpql, m);
//
//        headersAdd.add("Staff Name");
//        for (PaysheetComponent paysheetComponent : paysheetComponents) {
//            headersAdd.add(paysheetComponent.getName());
//        }
//
//        m = new HashMap();
//        jpql = "select distinct(spc.staffSalary.staff)"
//                + " from StaffSalaryComponant spc "
//                + " where spc.salaryCycle=:sc "
//                + " and spc.retired=false";
//        m.put("sc", current);
//        staffes = staffFacade.findBySQL(jpql, m);
//
//        staffAnsAndSalarySalaryComponents = new ArrayList<>();
//
//        for (Staff s : staffes) {
//            StaffAndSalarySalaryComponent sc = new StaffAndSalarySalaryComponent();
//            sc.setStaff(s);          
//            for (PaysheetComponent psc : paysheetComponents) {
//                jpql = "select spc from StaffSalaryComponant spc "
//                        + " where spc.staffSalary.staff=:st"
//                        + " and spc.retired=false"
//                        + " and spc.staffSalary.retired=false"
//                        + " and spc.staffPaysheetComponent.paysheetComponent=:pc "
//                        + " and spc.salaryCycle=:sc ";
//                m = new HashMap();
//                m.put("st", s);
//                m.put("pc", psc);
//                m.put("sc", current);
//                List<StaffSalaryComponant> c = staffSalaryComponantFacade.findBySQL(jpql, m);
//                sc.getStaffSalaryComponant().addAll(c);
//            }
//            staffAnsAndSalarySalaryComponents.add(sc);
//        }
//    }
    private List<PaysheetComponent> fetchPaysheetComponents(PaysheetComponentType paysheetComponentType) {
        HashMap m = new HashMap();
        String jpql = "select distinct(spc.staffPaysheetComponent.paysheetComponent) "
                + " from StaffSalaryComponant spc"
                + " where spc.salaryCycle=:sc"
                + " and spc.retired=false "
                + " and spc.staffPaysheetComponent.paysheetComponent.componentType in :tp"
                + " order by spc.staffPaysheetComponent.paysheetComponent.orderNo";
        m.put("sc", current);
        m.put("tp", Arrays.asList(paysheetComponentType.children()));
        return paysheetComponentFacade.findBySQL(jpql, m);
    }

    public List<PaysheetComponent> fetchPaysheetComponentsUserDefinded(List<PaysheetComponentType> list) {
        HashMap m = new HashMap();
        String jpql = "select distinct(spc.staffPaysheetComponent.paysheetComponent) "
                + " from StaffSalaryComponant spc"
                + " where spc.salaryCycle=:sc"
                + " and spc.retired=false "
                + " and spc.staffPaysheetComponent.paysheetComponent.componentType in :tp"
                + " order by spc.staffPaysheetComponent.paysheetComponent.orderNo";
        m.put("sc", current);
        m.put("tp", list);
        return paysheetComponentFacade.findBySQL(jpql, m);
    }

    List<String> headersSub;

    public List<String> getHeadersSub() {
        return headersSub;
    }

    public void setHeadersSub(List<String> headersSub) {
        this.headersSub = headersSub;
    }

    public void fillStaffAndSalaryComponents() {

        List<PaysheetComponent> paysheetComponentsAddition;
        List<PaysheetComponent> paysheetComponentsSubstraction;
        List<Staff> staffes;
        String jpql;
        Map m;

        headersAdd = new ArrayList<>();
        paysheetComponentsAddition = fetchPaysheetComponents(PaysheetComponentType.addition);
        for (PaysheetComponent paysheetComponent : paysheetComponentsAddition) {
            headersAdd.add(paysheetComponent.getName());
        }

        headersSub = new ArrayList<>();
        paysheetComponentsSubstraction = fetchPaysheetComponents(PaysheetComponentType.subtraction);
        for (PaysheetComponent paysheetComponent : paysheetComponentsSubstraction) {
            headersSub.add(paysheetComponent.getName());
        }

        m = new HashMap();
        jpql = "select distinct(spc.staffSalary.staff)"
                + " from StaffSalaryComponant spc "
                + " where spc.salaryCycle=:sc "
                + " and spc.retired=false";
        m.put("sc", current);
        staffes = staffFacade.findBySQL(jpql, m);

        staffAnsAndSalarySalaryComponents = new ArrayList<>();

        for (Staff s : staffes) {
            StaffAndSalarySalaryComponent sc = new StaffAndSalarySalaryComponent();
            sc.setStaff(s);
            for (PaysheetComponent psc : paysheetComponentsAddition) {
                List<StaffSalaryComponant> c = fetchSalaryComponents(s, psc);
                if (c != null) {
                    sc.getStaffSalaryComponantsAddition().addAll(c);
                }
            }
            for (PaysheetComponent psc : paysheetComponentsSubstraction) {
                List<StaffSalaryComponant> c = fetchSalaryComponents(s, psc);
                if (c != null) {
                    sc.getStaffSalaryComponantsSubstraction().addAll(c);
                }
            }
            sc.calValue();
            staffAnsAndSalarySalaryComponents.add(sc);
        }

        calTotalStaffSalary(staffAnsAndSalarySalaryComponents);
    }

    double staffSalaryComponentAdjustmentotal = 0.0;
    double valueAddingTotal = 0.0;
    double staffSalaryComponantsSubstractionTotal = 0.0;
    double epfTotal = 0.0;
    double etfTotal = 0.0;
    double netStaffSalaryTotal = 0.0;

    public void calTotalStaffSalary(List<StaffAndSalarySalaryComponent> stfSalCom) {

        staffSalaryComponentAdjustmentotal = 0.0;
        valueAddingTotal = 0.0;
        staffSalaryComponantsSubstractionTotal = 0.0;
        epfTotal = 0.0;
        etfTotal = 0.0;
        netStaffSalaryTotal = 0.0;

        for (StaffAndSalarySalaryComponent sassc : stfSalCom) {
//            staffSalaryComponentAdjustmentotal;
//            valueAddingTotal;
//            staffSalaryComponantsSubstractionTotal;
//            epfTotal;
//            etfTotal;
//            netStaffSalaryTotal;
        }
    }

    ReportKeyWord reportKeyWord;

    public ReportKeyWord getReportKeyWord() {
        if (reportKeyWord == null) {
            reportKeyWord = new ReportKeyWord();
        }
        return reportKeyWord;
    }

    public void setReportKeyWord(ReportKeyWord reportKeyWord) {
        this.reportKeyWord = reportKeyWord;
    }

    public void fillStaffPayRoll() {
        fillStaffPayRoll(false);
    }

    public void fillStaffPayRollBlocked() {
        fillStaffPayRoll(true);
    }

    public void fillStaffPayRoll(boolean blocked) {

        List<PaysheetComponent> paysheetComponentsAddition;
        List<PaysheetComponent> paysheetComponentsSubstraction;
        String jpql;
        Map m;

        headersAdd = new ArrayList<>();
        paysheetComponentsAddition = fetchPaysheetComponentsUserDefinded(PaysheetComponentType.addition.getUserDefinedComponentsAddidtions());
        for (PaysheetComponent paysheetComponent : paysheetComponentsAddition) {
            headersAdd.add(paysheetComponent.getName());
        }

        headersSub = new ArrayList<>();
        paysheetComponentsSubstraction = fetchPaysheetComponentsUserDefinded(PaysheetComponentType.subtraction.getUserDefinedComponentsDeductions());
        for (PaysheetComponent paysheetComponent : paysheetComponentsSubstraction) {
            headersSub.add(paysheetComponent.getName());
        }
        footerAdd = new ArrayList<>();
        footerSub = new ArrayList<>();

        m = new HashMap();
        jpql = "select spc"
                + " from StaffSalary spc "
                + " where spc.salaryCycle=:sc "
                + " and spc.retired=false "
                + " and spc.blocked=" + blocked;

        if (getReportKeyWord().getInstitution() != null) {
            jpql += " and spc.institution=:ins ";
            m.put("ins", getReportKeyWord().getInstitution());
        }

        if (getReportKeyWord().getDepartment() != null) {
            jpql += " and spc.department=:dep ";
            m.put("dep", getReportKeyWord().getDepartment());
        }

        if (getReportKeyWord().getStaff() != null) {
            jpql += " and spc.staff=:stf ";
            m.put("stf", getReportKeyWord().getStaff());
        }

        if (getReportKeyWord().getStaffCategory() != null) {
            jpql += " and spc.staff.staffCategory=:stfCat ";
            m.put("stfCat", getReportKeyWord().getStaffCategory());
        }

        if (getReportKeyWord().getDesignation() != null) {
            jpql += " and spc.staff.designation=:des ";
            m.put("des", getReportKeyWord().getDesignation());
        }

        if (getReportKeyWord().getRoster() != null) {
            jpql += " and spc.staff.roster=:rs ";
            m.put("rs", getReportKeyWord().getRoster());
        }

        jpql += " order by spc.staff.codeInterger ";
        m.put("sc", current);
        staffSalarys = staffSalaryFacade.findBySQL(jpql, m);

        if (staffSalarys == null) {
            return;
        }

        for (StaffSalary s : staffSalarys) {
            for (PaysheetComponent psc : paysheetComponentsAddition) {
                StaffSalaryComponant c = fetchSalaryComponents(s, psc, blocked);
                if (c != null) {
                    s.getTransStaffSalaryComponantsAddition().add(c);
                } else {
                    s.getTransStaffSalaryComponantsAddition().add(new StaffSalaryComponant(0, psc));

                }
            }
            for (PaysheetComponent psc : paysheetComponentsSubstraction) {
                StaffSalaryComponant c = fetchSalaryComponents(s, psc, blocked);
                if (c != null) {
                    s.getTransStaffSalaryComponantsSubtraction().add(c);
                } else {
                    s.getTransStaffSalaryComponantsSubtraction().add(new StaffSalaryComponant(0, psc));
                }
            }

        }

        for (PaysheetComponent psc : paysheetComponentsAddition) {
            double val = fetchSalaryComponents(psc, current, blocked);
            footerAdd.add(val);
            psc.setTransValue(val);
        }

        for (PaysheetComponent psc : paysheetComponentsSubstraction) {
            double val = fetchSalaryComponents(psc, current, blocked);
            footerSub.add(val);
            psc.setTransValue(val);
        }

        SalaryTotalCalculation(staffSalarys);

    }

    public List<StaffSalaryComponant> fetchSalaryComponents(Staff s, PaysheetComponent psc) {
        String jpql = "select spc from StaffSalaryComponant spc "
                + " where spc.staffSalary.staff=:st"
                + " and spc.retired=false"
                + " and spc.staffSalary.retired=false"
                + " and spc.staffPaysheetComponent.paysheetComponent=:pc "
                + " and spc.salaryCycle=:sc ";
        HashMap m = new HashMap();
        m.put("st", s);
        m.put("pc", psc);
        m.put("sc", current);
        return staffSalaryComponantFacade.findBySQL(jpql, m);

    }

    public Double fetchSalaryComponents(PaysheetComponent psc, SalaryCycle salaryCycle, boolean blocked) {
        String jpql = "select sum(spc.componantValue) "
                + " from StaffSalaryComponant spc "
                + " where spc.retired=false "
                + " and spc.staffSalary.blocked=" + blocked
                + " and spc.staffSalary.retired=false"
                + " and spc.staffPaysheetComponent.paysheetComponent=:pc "
                + " and spc.salaryCycle=:sc ";
        HashMap m = new HashMap();
        m.put("pc", psc);
        m.put("sc", salaryCycle);
        return staffSalaryComponantFacade.findDoubleByJpql(jpql, m);

    }

    public StaffSalaryComponant fetchSalaryComponents(StaffSalary s, PaysheetComponent psc, boolean blocked) {
        String jpql = "select spc from StaffSalaryComponant spc "
                + " where spc.staffSalary=:st"
                + " and spc.retired=false"
                + " and spc.staffSalary.retired=false "
                + " and spc.staffSalary.blocked=" + blocked
                + " and spc.staffPaysheetComponent.paysheetComponent=:pc "
                + " and spc.salaryCycle=:sc ";
        HashMap m = new HashMap();
        m.put("st", s);
        m.put("pc", psc);
        m.put("sc", current);
        return staffSalaryComponantFacade.findFirstBySQL(jpql, m);

    }

    public void fillStaffSalary() {

        String jpql = "select spc from StaffSalary spc "
                + " where spc.retired=false"
                + " and spc.salaryCycle=:sc"
                + " and ss.blocked=false "
                + " order by spc.staff.codeInterger ";
        HashMap m = new HashMap();
        m.put("sc", current);
        staffSalary = staffSalaryFacade.findBySQL(jpql, m);
        allStaffSalaryTotal(staffSalary);
    }

    double basicValueTotal;
    double overTimeValueTotal;
    double noPayValueTotal;
    double extraDutyValueTotal;
    double holyDayAllowancesTotal;
    double dayOffValueTotal;
    double additionalComponentTotal;
    double deductionalComponentTotal;
    double adjustmentToBasicTotal;
    double adjustmentToAllowancesTotal;
    double epfStaffValueTotal;
    double epfCompanyValueTotal;
    double etfStaffValueTotal;
    double etfCompanValueTotal;

    public void allStaffSalaryTotal(List<StaffSalary> stfSal) {

        basicValueTotal = 0.0;
        overTimeValueTotal = 0.0;
        noPayValueTotal = 0.0;
        extraDutyValueTotal = 0.0;
        holyDayAllowancesTotal = 0.0;
        dayOffValueTotal = 0.0;
        additionalComponentTotal = 0.0;
        deductionalComponentTotal = 0.0;
        adjustmentToBasicTotal = 0.0;
        adjustmentToAllowancesTotal = 0.0;
        epfStaffValueTotal = 0.0;
        epfCompanyValueTotal = 0.0;
        etfStaffValueTotal = 0.0;
        etfCompanValueTotal = 0.0;

        for (StaffSalary staffSalaryTotal : stfSal) {
            basicValueTotal += staffSalaryTotal.getBasicValue();
            overTimeValueTotal += staffSalaryTotal.getOverTimeValue();
            noPayValueTotal += staffSalaryTotal.getNoPayValueBasic() + staffSalaryTotal.getNoPayValueAllowance();
            extraDutyValueTotal += staffSalaryTotal.getTransExtraDutyValue();
            holyDayAllowancesTotal += staffSalaryTotal.getMerchantileAllowanceValue() + staffSalaryTotal.getPoyaAllowanceValue();
            dayOffValueTotal += staffSalaryTotal.getDayOffAllowance() + staffSalaryTotal.getSleepingDayAllowance();
            additionalComponentTotal += staffSalaryTotal.getComponentValueAddition();
            deductionalComponentTotal += staffSalaryTotal.getComponentValueSubstraction();
            adjustmentToBasicTotal += staffSalaryTotal.getAdjustmentToBasic();
            adjustmentToAllowancesTotal += staffSalaryTotal.getAdjustmentToAllowance();
            epfStaffValueTotal += staffSalaryTotal.getEpfStaffValue();
            epfCompanyValueTotal += staffSalaryTotal.getEpfCompanyValue();
            etfStaffValueTotal += staffSalaryTotal.getEtfSatffValue();
            etfCompanValueTotal += staffSalaryTotal.getEtfCompanyValue();
        }
    }
    @EJB
    StaffSalaryComponantFacade staffSalaryComponantFacade;

    public class StaffAndSalarySalaryComponent {

        Staff staff;
        double valueAdding;
        double epf;
        double etf;
        double valueSubstarction;
        List<StaffSalaryComponant> staffSalaryComponantsAddition;
        List<StaffSalaryComponant> staffSalaryComponantsSubstraction;

        public void calValue() {
            valueAdding = 0;
            valueSubstarction = 0;
            epf = 0;
            etf = 0;

            if (staffSalaryComponantsAddition != null) {
                for (StaffSalaryComponant componant : staffSalaryComponantsAddition) {
                    valueAdding += componant.getComponantValue();
                    epf += componant.getEpfValue();
                    etf += componant.getEtfValue();
                }
            }

            if (staffSalaryComponantsSubstraction != null) {
                for (StaffSalaryComponant componant : staffSalaryComponantsSubstraction) {
                    valueSubstarction += componant.getComponantValue();
                    epf += componant.getEpfValue();
                    etf += componant.getEtfValue();
                }
            }

        }

        public double getEpf() {
            return epf;
        }

        public void setEpf(double epf) {
            this.epf = epf;
        }

        public double getEtf() {
            return etf;
        }

        public void setEtf(double etf) {
            this.etf = etf;
        }

        public double getValueAdding() {
            return valueAdding;
        }

        public void setValueAdding(double valueAdding) {
            this.valueAdding = valueAdding;
        }

        public double getValueSubstarction() {
            return valueSubstarction;
        }

        public void setValueSubstarction(double valueSubstarction) {
            this.valueSubstarction = valueSubstarction;
        }

        public Staff getStaff() {
            return staff;
        }

        public void setStaff(Staff staff) {
            this.staff = staff;
        }

        public List<StaffSalaryComponant> getStaffSalaryComponantsAddition() {
            if (staffSalaryComponantsAddition == null) {
                staffSalaryComponantsAddition = new ArrayList<>();
            }
            return staffSalaryComponantsAddition;
        }

        public void setStaffSalaryComponantsAddition(List<StaffSalaryComponant> staffSalaryComponantsAddition) {
            this.staffSalaryComponantsAddition = staffSalaryComponantsAddition;
        }

        public List<StaffSalaryComponant> getStaffSalaryComponantsSubstraction() {
            if (staffSalaryComponantsSubstraction == null) {
                staffSalaryComponantsSubstraction = new ArrayList<>();
            }
            return staffSalaryComponantsSubstraction;
        }

        public void setStaffSalaryComponantsSubstraction(List<StaffSalaryComponant> staffSalaryComponantsSubstraction) {
            this.staffSalaryComponantsSubstraction = staffSalaryComponantsSubstraction;
        }

    }

    public double getStaffSalaryComponentAdjustmentotal() {
        return staffSalaryComponentAdjustmentotal;
    }

    public void setStaffSalaryComponentAdjustmentotal(double staffSalaryComponentAdjustmentotal) {
        this.staffSalaryComponentAdjustmentotal = staffSalaryComponentAdjustmentotal;
    }

    public double getValueAddingTotal() {
        return valueAddingTotal;
    }

    public void setValueAddingTotal(double valueAddingTotal) {
        this.valueAddingTotal = valueAddingTotal;
    }

    public double getStaffSalaryComponantsSubstractionTotal() {
        return staffSalaryComponantsSubstractionTotal;
    }

    public void setStaffSalaryComponantsSubstractionTotal(double staffSalaryComponantsSubstractionTotal) {
        this.staffSalaryComponantsSubstractionTotal = staffSalaryComponantsSubstractionTotal;
    }

    public double getEpfTotal() {
        return epfTotal;
    }

    public void setEpfTotal(double epfTotal) {
        this.epfTotal = epfTotal;
    }

    public double getEtfTotal() {
        return etfTotal;
    }

    public void setEtfTotal(double etfTotal) {
        this.etfTotal = etfTotal;
    }

    public double getNetStaffSalaryTotal() {
        return netStaffSalaryTotal;
    }

    public void setNetStaffSalaryTotal(double netStaffSalaryTotal) {
        this.netStaffSalaryTotal = netStaffSalaryTotal;
    }

    public double getBasicValueTotal() {
        return basicValueTotal;
    }

    public void setBasicValueTotal(double basicValueTotal) {
        this.basicValueTotal = basicValueTotal;
    }

    public double getOverTimeValueTotal() {
        return overTimeValueTotal;
    }

    public void setOverTimeValueTotal(double overTimeValueTotal) {
        this.overTimeValueTotal = overTimeValueTotal;
    }

    public double getNoPayValueTotal() {
        return noPayValueTotal;
    }

    public void setNoPayValueTotal(double noPayValueTotal) {
        this.noPayValueTotal = noPayValueTotal;
    }

    public double getExtraDutyValueTotal() {
        return extraDutyValueTotal;
    }

    public void setExtraDutyValueTotal(double extraDutyValueTotal) {
        this.extraDutyValueTotal = extraDutyValueTotal;
    }

    public double getHolyDayAllowancesTotal() {
        return holyDayAllowancesTotal;
    }

    public void setHolyDayAllowancesTotal(double holyDayAllowancesTotal) {
        this.holyDayAllowancesTotal = holyDayAllowancesTotal;
    }

    public double getDayOffValueTotal() {
        return dayOffValueTotal;
    }

    public void setDayOffValueTotal(double dayOffValueTotal) {
        this.dayOffValueTotal = dayOffValueTotal;
    }

    public double getAdditionalComponentTotal() {
        return additionalComponentTotal;
    }

    public void setAdditionalComponentTotal(double additionalComponentTotal) {
        this.additionalComponentTotal = additionalComponentTotal;
    }

    public double getDeductionalComponentTotal() {
        return deductionalComponentTotal;
    }

    public void setDeductionalComponentTotal(double deductionalComponentTotal) {
        this.deductionalComponentTotal = deductionalComponentTotal;
    }

    public double getAdjustmentToBasicTotal() {
        return adjustmentToBasicTotal;
    }

    public void setAdjustmentToBasicTotal(double adjustmentToBasicTotal) {
        this.adjustmentToBasicTotal = adjustmentToBasicTotal;
    }

    public double getAdjustmentToAllowances() {
        return adjustmentToAllowances;
    }

    public void setAdjustmentToAllowances(double adjustmentToAllowances) {
        this.adjustmentToAllowances = adjustmentToAllowances;
    }

    public double getEpfStaffValueTotal() {
        return epfStaffValueTotal;
    }

    public void setEpfStaffValueTotal(double epfStaffValueTotal) {
        this.epfStaffValueTotal = epfStaffValueTotal;
    }

    public double getEpfCompanyValueTotal() {
        return epfCompanyValueTotal;
    }

    public void setEpfCompanyValueTotal(double epfCompanyValueTotal) {
        this.epfCompanyValueTotal = epfCompanyValueTotal;
    }

    public double getEtfStaffValueTotal() {
        return etfStaffValueTotal;
    }

    public void setEtfStaffValueTotal(double etfStaffValueTotal) {
        this.etfStaffValueTotal = etfStaffValueTotal;
    }

    public double getEtfCompanValueTotal() {
        return etfCompanValueTotal;
    }

    public void setEtfCompanValueTotal(double etfCompanValueTotal) {
        this.etfCompanValueTotal = etfCompanValueTotal;
    }

    @FacesConverter(forClass = SalaryCycle.class)
    public static class SalaryCycleConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            SalaryCycleController controller = (SalaryCycleController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "salaryCycleController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            try {
                key = Long.valueOf(value);
            } catch (NumberFormatException exception) {
                key = 0l;
            }
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof SalaryCycle) {
                SalaryCycle o = (SalaryCycle) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + SalaryCycleController.class.getName());
            }
        }
    }

    @FacesConverter("salaryCycleConverter")
    public static class SalaryCycleControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            SalaryCycleController controller = (SalaryCycleController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "salaryCycleController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof SalaryCycle) {
                SalaryCycle o = (SalaryCycle) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + SalaryCycleController.class.getName());
            }
        }
    }

}
