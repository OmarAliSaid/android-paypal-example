var firebase = require('firebase-admin');
var bodyParser = require('body-parser');
var paypal = require('paypal-rest-sdk');
var express = require('express');
var app = express();

// Initialize the app with a service account, granting admin privileges
var serviceAccount = require("path/to/serviceAccountKey.json");

paypal.configure({
  'mode': 'sandbox', //sandbox or live
  'client_id': 'REPLACE WITH PAYPAL APP CLIENT_ID',
  'client_secret': 'REPLACE WITH PAYPAL APP SECRET'
});

firebase.initializeApp({
  credential: firebase.credential.cert(serviceAccount),
  databaseURL: "https://<DATABASE_NAME>.firebaseio.com"
});

// configure body parser
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

var port  = process.env.PORT || 5000; // set our port

// START THE SERVER
// =============================================================================
app.listen(port);
console.log('Magic happens on port ' + port);


// get an instance of the express Router
var router = express.Router();           


// REGISTER OUR ROUTES -------------------------------
// all of our routes will be prefixed with /api
app.use('/api', router);

//verify mobile payment (accessed at POST http://localhost:8080/api/verify_mobile_payment)
router.route('/verify_mobile_payment')
    .post(function(req, res) {
        //get POST parameters
        var payment_id = req.body.payment_id;
        var amout_client = req.body.amount;
        var currency_client = req.body.currency;
        var uid = req.body.uid;

        paypal.payment.get(payment_id, function (error, payment) {
            if (error) {
                res.json({"msg": error , "state": error.status});
                return ;
            }
                        
            var payment_state = payment.state;
            var transaction_server = payment.transactions[0];
            var amount_server = transaction_server.amount.total;
            var currency_server = transaction_server.amount.currency;
            var slale_state_server = transaction_server.related_resources[0].sale.state;
                
            if(payment_state !== "approved" ){
                res.json( { "msg" : "Payment has not been verified. " , "status" : 200 } );
                return ;
            }
            
            if(amount_server !== amout_client){
               res.json( { "msg" : "Payment amount doesn't matched. " , "state" : 200 } );
               return ; 
            }
            
            if(currency_server !== currency_client){
               res.json( { "msg" : "Payment currency doesn't matched. " , "state" : 200 } );
               return ; 
            }
            
            if(slale_state_server !== "completed"){
               res.json( { "msg" : "Sale not completed. " , "state" : 200 } );
               return ; 
            }
            
            // insert payment in db
            var new_payment_key = insertPayment( payment.id , uid , payment.create_time , payment.state , amount_server , currency_server );
            
            // insert sale in db
            insertItemSales(new_payment_key , payment.transactions[0] , payment.state);
            
            res.json( { "msg" : "Sale completed. " , "state" : 200 , "payment":payment } );
            return ; 
            
        });
        
    });


// get firebase products
router.route("/products").get(function(req,res){
    
    var jsonStr = '{"products":[]}';
    var obj = JSON.parse(jsonStr);
    
    // Attach an asynchronous callback to read the data at our products reference
    firebase.database().ref("/products").on("value", function(snapshot) {
        snapshot.forEach(function(data) {
            var response = data.val();
            response.sku = data.key;
            obj['products'].push(response);
        });
        
        obj.status = 200;
        jsonStr = JSON.stringify(obj);
        res.json(obj);
        
    }, function (errorObject) {
        res.json({"msg":errorObject.message , "status":errorObject.code});
    });
});


// insert payment in firebase.
function insertPayment(paymentID , uid , c_time , p_state , amount_server , currency_server) {
    
  var new_payment_key = firebase.database().ref("/").child("payments").push().key;
    
  firebase.database().ref('payments/'+new_payment_key).set({
    user_id : uid , 
    paypal_payment_id: paymentID ,
    create_time : c_time,
    payment_state : p_state,
    amount : amount_server,
    currency : currency_server
  });
    
  return new_payment_key;
}


// insert success sale in db
function insertItemSales(paymentID, transaction, state){
    var item_list = transaction.item_list;
    var items = item_list.items;
    
    items.forEach(function(item){
        var quantity = item.quantity;
        var price = item.price;
        var sku = item.sku;
       
        var new_sale_key = firebase.database().ref("/").child("sales").push().key;
        firebase.database().ref('sales/'+new_sale_key).set({ 
            firebase_payment_id : paymentID , 
            quantity: quantity ,
            price : price,
            payment_state : state,
            product_id : sku
        });
    });
    
}
