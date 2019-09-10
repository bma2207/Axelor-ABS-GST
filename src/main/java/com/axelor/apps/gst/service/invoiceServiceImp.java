package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.account.db.repo.TaxRepository;
import com.axelor.apps.account.service.invoice.print.InvoicePrintService;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.tax.AccountManagementServiceImpl;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.supplychain.service.InvoiceLineSupplychainService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class invoiceServiceImp implements invoiceService {

  @Inject private AccountManagementServiceImpl service;
  @Inject private TaxService taxservice;
  @Inject private InvoiceLineSupplychainService supplychainservice;
  @Inject private TaxLineRepository taxLineRepo;
  @Inject private InvoicePrintService invoicePrint;

  @Override
  public List<InvoiceLine> productList(Invoice invoice, List<Integer> pro) {
    // TODO Auto-generated method stub
    List<Product> product =
        Beans.get(ProductRepository.class).all().filter("self.id in (?1) ", pro).fetch();
    Company company = Beans.get(CompanyRepository.class).all().fetchOne();
    List<InvoiceLine> invoiceList = new ArrayList<InvoiceLine>();
    for (Product productObj : product) {
      InvoiceLine invoiceLine = new InvoiceLine();
      invoiceLine.setProduct(productObj);
      invoiceLine.setUnit(productObj.getUnit());
      invoiceLine.setProductCode(productObj.getCode());
      invoiceLine.setProductName(productObj.getFullName());
      invoiceLine.setPrice(productObj.getSalePrice());
      invoiceLine.setQty(BigDecimal.ONE);
      // invoiceLine.getProduct().getAccountManagementList();

      /*
       * TaxLine tax = taxLineRepo.all().filter("self.value =:value") .bind("value",
       * invoiceLine.getProduct().getGstRate()).fetchOne();
       * invoiceLine.setTaxLine(tax);
       */

      Tax tax = Beans.get(TaxRepository.class).all().filter("self.code = 'GST'").fetchOne();
      TaxLine taxLine = tax.getActiveTaxLine();
      taxLine.setValue(invoiceLine.getProduct().getGstRate());
      invoiceLine.setTaxLine(taxLine);
      /*
       * this service use for get Active tax line to set in invoice Line
       */

      /* AccountManagement accountManagementlist =
          service.getAccountManagement(
              invoiceLine.getProduct().getAccountManagementList(), company);

      try {
        TaxLine taxLines = taxservice.getTaxLine(tax, LocalDate.now());
        invoiceLine.setTaxLine(taxLines.getTax().getActiveTaxLine());

      } catch (AxelorException e) {
        e.printStackTrace();
      }*/

      invoiceList.add(invoiceLine);
    }
    return invoiceList;
  }

  public List<InvoiceLine> invoiceCalculationOnPartner(Invoice invoice) {
    List<InvoiceLine> invoiceLines = invoice.getInvoiceLineList();
    List<InvoiceLine> invoiceList = new ArrayList<InvoiceLine>();

    BigDecimal cgst = null, sgst = null, igst = null;

    if (invoice.getInvoiceLineList() != null) {

      for (InvoiceLine invoiceLine : invoiceLines) {
        BigDecimal grossAmount = BigDecimal.ZERO;
        BigDecimal price = invoiceLine.getPrice();
        BigDecimal qty = invoiceLine.getQty();
        BigDecimal gstrate = invoiceLine.getTaxLine().getValue();
        BigDecimal exTxtTotal = invoiceLine.getQty().multiply(price);
        BigDecimal values = exTxtTotal.multiply(gstrate);
        BigDecimal netAmount = exTxtTotal;

        Address companyAddess = invoice.getCompany().getAddress();
        Address invoiceAddress = invoice.getAddress();
        if (companyAddess.getState().equals(invoiceAddress.getState())) {
          BigDecimal dividevalue = values.divide(new BigDecimal(2));
          sgst = sgst.add(dividevalue);
          cgst = sgst;
          BigDecimal Amount = netAmount.add(cgst);
          grossAmount = sgst.add(Amount);
        } else {
          igst = values;
          grossAmount = netAmount.add(igst);
        }
        invoiceLine.setCgst(cgst);
        invoiceLine.setSgst(sgst);
        invoiceLine.setIgst(igst);
        invoiceLine.setGrossAmount(grossAmount);
        invoiceLine.setExTaxTotal(netAmount);

        invoiceList.add(invoiceLine);
      }
    }

    return invoiceList;
  }

  @Override
  public List<InvoiceLineTax> SetTaxLineOnChangePartner(Invoice invoice) {
    // TODO Auto-generated method stub
    List<InvoiceLineTax> invoicetaxLine = new ArrayList<InvoiceLineTax>();
    List<InvoiceLine> invoiceLines = invoice.getInvoiceLineList();

    if (invoice.getInvoiceLineList() != null) {

      for (InvoiceLine invoiceLine : invoiceLines) {
        InvoiceLineTax invoiceLineTaxs = new InvoiceLineTax();
        invoiceLineTaxs.setTaxLine(invoiceLine.getTaxLine());
        invoiceLineTaxs.setInTaxTotal(invoiceLine.getGrossAmount());
        invoiceLineTaxs.setTaxTotal(
            invoiceLine.getGrossAmount().subtract(invoiceLine.getExTaxTotal()));
        invoiceLineTaxs.setExTaxBase(invoiceLine.getExTaxTotal());
        invoicetaxLine.add(invoiceLineTaxs);
      }
    }
    return invoicetaxLine;
  }

  public Invoice setTotal(Invoice invoice) {
    List<InvoiceLine> invoiceLines = invoice.getInvoiceLineList();
    BigDecimal netAmount, grossAmount, tax;
    invoice.setTaxTotal(null);
    invoice.setExTaxTotal(null);
    invoice.setInTaxTotal(null);
    if (invoice.getInvoiceLineList() != null) {

      for (InvoiceLine invoiceLine : invoiceLines) {
        tax =
            invoice
                .getTaxTotal()
                .add(invoiceLine.getGrossAmount().subtract(invoiceLine.getExTaxTotal()));
        netAmount = invoice.getExTaxTotal().add(invoiceLine.getExTaxTotal());
        grossAmount = invoice.getInTaxTotal().add(invoiceLine.getGrossAmount());
        invoice.setTaxTotal(tax);
        invoice.setExTaxTotal(netAmount);
        invoice.setInTaxTotal(grossAmount);
      }
    }
    return invoice;
  }
}
