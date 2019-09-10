package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.apps.supplychain.service.InvoiceLineSupplychainService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class invoiceLineServiceimpls extends InvoiceLineSupplychainService {

  @Inject
  public invoiceLineServiceimpls(
      CurrencyService currencyService,
      PriceListService priceListService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountManagementAccountService accountManagementAccountService,
      PurchaseProductService purchaseProductService) {
    super(
        currencyService,
        priceListService,
        appAccountService,
        analyticMoveLineService,
        accountManagementAccountService,
        purchaseProductService);
    // TODO Auto-generated constructor stub
  }

  @Inject TaxLineRepository TaxLineRepository;

  @Override
  public Map<String, Object> fillProductInformation(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {

    Map<String, Object> productInformation = super.fillProductInformation(invoice, invoiceLine);
    TaxLine taxLine = null;
    boolean isPurchase = InvoiceToolService.isPurchase(invoice);
    BigDecimal price = BigDecimal.ZERO;
    BigDecimal exTxtTotal = BigDecimal.ZERO;
    Address companyAddess = invoice.getCompany().getAddress();
    Address invoiceAddress = invoice.getAddress();
    BigDecimal sgst = BigDecimal.ZERO;
    BigDecimal cgst = BigDecimal.ZERO;
    BigDecimal igst = BigDecimal.ZERO;
    BigDecimal grossAmount = BigDecimal.ZERO;
    BigDecimal gstrate = invoiceLine.getTaxLine().getValue();

    /* set tax in invoice Line */
    TaxLine taxLines =
        TaxLineRepository.all()
            .filter("self.value =:value")
            .bind("value", invoiceLine.getProduct().getGstRate())
            .fetchOne();
    invoiceLine.setTaxLine(taxLines);
    /*
     * BigDecimal price = getExTaxUnitPrice(invoice, invoiceLine, taxLine,
     * isPurchase); BigDecimal priceContaint=invoiceLine.getPrice();
     */

    /*
     * if(price.equals(priceContaint)) { exTxtTotal =
     * invoiceLine.getQty().multiply(priceContaint); } else { exTxtTotal =
     * invoiceLine.getQty().multiply(price); }
     */
    if (invoiceLine.getPrice().compareTo(BigDecimal.ZERO) == 0) {
      price = getExTaxUnitPrice(invoice, invoiceLine, taxLine, isPurchase);
    } else {
      price = invoiceLine.getPrice();
      productInformation.put("price", price);
    }
    exTxtTotal = invoiceLine.getQty().multiply(price);
    BigDecimal values = exTxtTotal.multiply(gstrate);
    BigDecimal netAmount = exTxtTotal;

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
    invoiceLine.setGrossAmount(grossAmount);
    invoiceLine.setIgst(igst);
    invoiceLine.setSgst(sgst);
    invoiceLine.setCgst(cgst);
    productInformation.put("igst", igst);
    productInformation.put("sgst", sgst);
    productInformation.put("cgst", cgst);
    productInformation.put("grossAmount", grossAmount);

    return productInformation;
  }

  public Invoice invoiceCalculation(Invoice invoice) {
    List<InvoiceLine> invoiceLines = invoice.getInvoiceLineList();
    BigDecimal cgst = null, sgst = null, igst = null, netamount = null, grossamount = null;
    invoice.setNetAmount(null);
    invoice.setCgst(null);
    invoice.setSgst(null);
    invoice.setIgst(null);
    invoice.setGrossAmount(null);
    if (invoice.getInvoiceLineList() != null) {

      for (InvoiceLine invoiceLine : invoiceLines) {
        cgst = invoiceLine.getCgst().add(invoice.getCgst());
        sgst = invoiceLine.getSgst().add(invoice.getSgst());
        igst = invoiceLine.getIgst().add(invoice.getIgst());
        netamount = invoiceLine.getExTaxTotal().add(invoice.getNetAmount());
        invoice.setCgst(cgst);
        grossamount = invoiceLine.getGrossAmount().add(invoice.getGrossAmount());
        invoice.setSgst(sgst);
        invoice.setIgst(igst);
        invoice.setNetAmount(netamount);
        invoice.setGrossAmount(grossamount);
      }
    }

    return invoice;
  }
}
