package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import java.util.List;

public interface invoiceService {
  public List<InvoiceLine> productList(Invoice invoice, List<Integer> pro);

  public List<InvoiceLine> invoiceCalculationOnPartner(Invoice invoice);

  public List<InvoiceLineTax> SetTaxLineOnChangePartner(Invoice invoice);

  public Invoice setTotal(Invoice invoice);
}
