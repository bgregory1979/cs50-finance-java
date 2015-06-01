package net.cs50.finance.controllers;

import net.cs50.finance.models.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris Bay on 5/17/15.
 */
@Controller
public class PortfolioController extends AbstractFinanceController {

    @RequestMapping(value = "/portfolio")
    public String portfolio(HttpServletRequest request, Model model){

        // TODO - Implement portfolio display
        User user = getUserFromSession(request);
        double cash = user.getCash();

        Map<String, StockHolding> holdings = user.getPortfolio();

        HashMap<String, HashMap<String, String>> outerHash = new HashMap<String, HashMap<String, String>>();
        for (Map.Entry<String, StockHolding> entry : holdings.entrySet()) {

            HashMap<String, String> innerHash = new HashMap<String, String>();
            Stock stock = null;
            try {
                stock = Stock.lookupStock(entry.getKey());
            } catch (StockLookupException e) {
                System.out.println(e.getMessage());
                displayError("The stock symbol you entered is invalid.", model);
            }
            StockHolding holding = entry.getValue();

            innerHash.put("name", stock.getName());

            int sharesOwned = holding.getSharesOwned();
            innerHash.put("shares", String.valueOf(sharesOwned));

            double currentPrice = stock.getPrice();
            innerHash.put("currentPrice", String.valueOf(String.format("%.2f", currentPrice)));

            double totalValue = currentPrice * sharesOwned;
            innerHash.put("totalValue", String.valueOf(String.format("%.2f", totalValue)));

            outerHash.put(entry.getKey(), innerHash);
        }

        model.addAttribute("cash", String.format("%.2f", cash));
        model.addAttribute("holdings", outerHash);
        model.addAttribute("title", "Portfolio");
        model.addAttribute("portfolioNavClass", "active");

        return "portfolio";
    }

}
