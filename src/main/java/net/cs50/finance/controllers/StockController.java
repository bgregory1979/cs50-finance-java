package net.cs50.finance.controllers;

import net.cs50.finance.models.*;
import net.cs50.finance.models.dao.StockHoldingDao;
import net.cs50.finance.models.dao.StockTransactionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Chris Bay on 5/17/15.
 */
@Controller
public class StockController extends AbstractFinanceController {

    @Autowired
    StockHoldingDao stockHoldingDao;

    @RequestMapping(value = "/quote", method = RequestMethod.GET)
    public String quoteForm(Model model) {

        // pass data to template
        model.addAttribute("title", "Quote");
        model.addAttribute("quoteNavClass", "active");
        return "quote_form";
    }

    @RequestMapping(value = "/quote", method = RequestMethod.POST)
    public String quote(String symbol, Model model) {

        // TODO - Implement quote lookup
        Stock stockInfo = null;

        try {
            stockInfo = Stock.lookupStock(symbol);
        } catch (StockLookupException e) {
            System.out.println(e.getMessage());
            return displayError("The stock symbol you entered is invalid.", model);
        }

        // pass data to template
        model.addAttribute("stock_desc", stockInfo.getName());
        model.addAttribute("stock_price", stockInfo.getPrice());
        model.addAttribute("title", "Quote");
        model.addAttribute("quoteNavClass", "active");

        return "quote_display";
    }

    @RequestMapping(value = "/buy", method = RequestMethod.GET)
    public String buyForm(Model model) {

        model.addAttribute("title", "Buy");
        model.addAttribute("action", "/buy");
        model.addAttribute("buyNavClass", "active");
        return "transaction_form";
    }

    @RequestMapping(value = "/buy", method = RequestMethod.POST)
    public String buy(String symbol, int numberOfShares, HttpServletRequest request, Model model) throws StockLookupException {

        // TODO - Implement buy action
        User user = getUserFromSession(request);
        Stock stock = null;
        StockHolding holding = null;

        try {
            stock = Stock.lookupStock(symbol);
        } catch (StockLookupException e) {
            System.out.println(e.getMessage());
            return displayError("The stock symbol you entered is invalid.", model);
        }

        double transCost = numberOfShares * stock.getPrice();

        if (user.getCash() < transCost) {
            return displayError("Not enough funds to purchase the requested amount of stock.", model);
        }
        else {
            holding = StockHolding.buyShares(user, symbol, numberOfShares);
            user.setCash(user.getCash() - transCost);
        }

        stockHoldingDao.save(holding);
        userDao.save(user);

        model.addAttribute("title", "Buy");
        model.addAttribute("action", "/buy");
        model.addAttribute("buyNavClass", "active");

        return "transaction_confirm";
    }

    @RequestMapping(value = "/sell", method = RequestMethod.GET)
    public String sellForm(Model model) {
        model.addAttribute("title", "Sell");
        model.addAttribute("action", "/sell");
        model.addAttribute("sellNavClass", "active");
        return "transaction_form";
    }

    @RequestMapping(value = "/sell", method = RequestMethod.POST)
    public String sell(String symbol, int numberOfShares, HttpServletRequest request, Model model) {

        // TODO - Implement sell action
        User user = getUserFromSession(request);
        Stock stock = null;
        StockHolding holding = null;

        try {
            stock = Stock.lookupStock(symbol);
        } catch (StockLookupException e) {
            System.out.println(e.getMessage());
            return displayError("The stock symbol you entered is invalid.", model);
        }

        double transCost = numberOfShares * stock.getPrice();

        try {
            holding = StockHolding.sellShares(getUserFromSession(request), symbol, numberOfShares);
        } catch (StockLookupException e) {
            e.printStackTrace();
            return displayError("There was a problem with the Sell request.", model);
        }

        user.setCash(user.getCash() + transCost);
        stockHoldingDao.save(holding);
        userDao.save(user);

        model.addAttribute("title", "Sell");
        model.addAttribute("action", "/sell");
        model.addAttribute("sellNavClass", "active");

        return "transaction_confirm";
    }

}
