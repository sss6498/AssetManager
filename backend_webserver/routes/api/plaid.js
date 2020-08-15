const express = require("express");
const plaid = require("plaid");
const router = express.Router();
const passport = require("passport");
const moment = require("moment");
const mongoose = require("mongoose");
require('dotenv').config();

// Load Account and User models
const Account = require("../../models/Account");
const User = require("../../models/User");

const PLAID_CLIENT_ID = process.env.PLAID_CLIENT_ID;
const PLAID_SECRET = process.env.PLAID_SECRET;
const PLAID_PUBLIC_KEY = process.env.PLAID_PUBLIC_KEY;
const PLAID_ENV = process.env.PLAID_ENV;

var client = new plaid.Client(
    PLAID_CLIENT_ID,
    PLAID_SECRET,
    PLAID_PUBLIC_KEY,
    plaid.environments[PLAID_ENV],
    {version: '2019-05-29', clientApp: 'AssetManager'}
);

var PUBLIC_TOKEN = null;
var ACCESS_TOKEN = null;
var ITEM_ID = null;

// @route GET api/plaid/accounts
// @desc Get all accounts linked with plaid for a specific user
// @access Private
router.post("/accounts", (req, res) => {
    Account.find({ userId: req.body.userId })
      .then(accounts => res.json(accounts))
      .catch(err => console.log(err));
  }
);

//@route POST api/plaid/accounts/balance
// @desc Update balance for all accounts
// @access Private
router.post(
  "/accounts/balance",
  passport.authenticate("jwt", { session: false }),
  (req, res) => {
    const accounts = req.body;

    if (accounts) {
      res.accountBalance(accounts);
    }
  }
);

//helper function for account balance
function accountBalance(accounts) {
  accounts.forEach(function(account) {
    ACCESS_TOKEN = account.accessToken;
    client.getBalance(ACCESS_TOKEN, function(error, balanceResponse) {
      return account.balance = balanceResponse;
    })
  });
}

// @route POST api/plaid/accounts/add
// @desc Trades public token for access token and stores credentials in database
// @access Private
router.post("/accounts/add", (req, res) => {

    PUBLIC_TOKEN = req.body.public_token;
    const userId = req.body.userId;
    const institutionId = req.body.institutionId;
    const institutionName = req.body.institutionName;
    const accountName = req.body.accountName;
    const accountType = req.body.accountType;
    const accountSubtype = req.body.accountSubtype;

    if (PUBLIC_TOKEN) {
      client
        .exchangePublicToken(PUBLIC_TOKEN)
        .then(exchangeResponse => {
          ACCESS_TOKEN = exchangeResponse.access_token;
          ITEM_ID = exchangeResponse.item_id;
          // Check if account already exists for specific user
          Account.findOne({
            userId: userId,
            institutionId: institutionId
          })
            .then(account => {
              if (account) {
                console.log("Account already exists");
              } else {
                const newAccount = new Account({
                  userId: userId,
                  accessToken: ACCESS_TOKEN,
                  itemId: ITEM_ID,
                  institutionId: institutionId,
                  institutionName: institutionName,
                  accountName: accountName,
                  accountType: accountType,
                  accountSubtype: accountSubtype,
                });

                newAccount.save().then(account => res.json(account));
              }
            })
            .catch(err => console.log(err)); // Mongo Error
        })
        .catch(err => console.log(err)); // Plaid Error
    }
  }
);

// @route DELETE api/plaid/accounts/:id
// @desc Delete account with given id
// @access Private
router.delete("/accounts/:id", (req, res) => {
    Account.findById(req.params.id).then(account => {
      // Delete account
      account.remove().then(() => res.json({ success: true }));
    });
  }
);

// @route POST api/plaid/accounts/transactions
// @desc Fetch transactions from past 30 days from all linked accounts
// @access Private
router.post("/accounts/transactions", (req, res) => {
    const now = moment();
    const today = now.format("YYYY-MM-DD");
    const thirtyDaysAgo = now.subtract(30, "days").format("YYYY-MM-DD");

    let transactions = [];

    const accounts = req.body.accounts;

    if (accounts) {
      accounts.forEach(function(account) {
        ACCESS_TOKEN = account.accessToken;
        const institutionName = account.institutionName;
        client
          .getTransactions(ACCESS_TOKEN, thirtyDaysAgo, today)
          .then(response => {
            transactions.push({
              accountName: institutionName,
              transactions: response.transactions
            });

            if (transactions.length === accounts.length) {
              res.json(transactions);
            }
          })
          .catch(err => console.log(err));
      });
    }
  }
);



module.exports = router;
