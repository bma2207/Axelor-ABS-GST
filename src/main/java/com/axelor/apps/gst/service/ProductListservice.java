package com.axelor.apps.gst.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.gst.report.IReport;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductListservice {
  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @SuppressWarnings("unchecked")
  public void printProductCatalog(ActionRequest request, ActionResponse response)
      throws AxelorException {

    String productIds = "";

    List<Integer> lstSelectedProduct = (List<Integer>) request.getContext().get("_ids");

    if (lstSelectedProduct != null) {
      productIds = Joiner.on(",").join(lstSelectedProduct);
    }

    String name = I18n.get("Product Catalog");

    String fileLink =
        ReportFactory.createReport(IReport.PRODUCT_LIST, name + "-${date}")
            .addParam("ProductIds", productIds)
            .generate()
            .getFileLink();

    logger.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }
}
