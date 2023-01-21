
import express from "express";
import mqtt from "mqtt";

const PORT = process.env.PORT || 3001;

const app = express();
const client  = mqtt.connect("mqtt://127.0.0.1:1883",{clientId:"mqttjs01"});
var temp = 0;
var map = new Map();
var mapTestData = new Map();
mapTestData.set('livingroom\\t', '100');
mapTestData.set('livingroom\\h', '100');
mapTestData.set('livingroom\\b', '100');
mapTestData.set('kitchen\\t', '100');
mapTestData.set('kitchen\\h', '100');
mapTestData.set('kitchen\\b', '100');



function subscribe_livingroom(){
  
  client.subscribe('livingroom\\t', function (err) {
    if (!err) {
      console.log("Subscribed to livingroom\\t successfully");
    }
  })

  client.subscribe('livingroom\\h', function (err) {
    if (!err) {
      console.log("Subscribed to livingroom\\h successfully");
    }
  })

  client.subscribe('livingroom\\b', function (err) {
    if (!err) {
      console.log("Subscribed to livingroom\\b successfully");
    }
  })
  

}

function subscribe_kitchen(){
  
  client.subscribe('kitchen\\t', function (err) {
    if (!err) {
      console.log("Subscribed to kitchen\\t successfully");
    }
  })

  client.subscribe('kitchen\\h', function (err) {
    if (!err) {
      console.log("Subscribed to kitchen\\h successfully");
    }
  })

  client.subscribe('kitchen\\b', function (err) {
    if (!err) {
      console.log("Subscribed to kitchen\\b successfully");
    }
  })
  
}


function mapToObj(map){
  var obj = {}
  map.forEach(function(v, k){
    obj[k] = v
  })
  return obj
}



function connect(){
  var con = false;

  client.on("connect", function(){
    
    console.log("Connected successfully");
   
  })
  con = true;

  return con;
}



if(connect()){
  subscribe_livingroom();
  subscribe_kitchen();
}

client.on('message', function (topic, message) {
  // message is Buffer
  // 20:04:40.077 - hours:minutes:seconds.milliseconds
  var time = new Date();
  var time_string = time.getHours().toString() +  ":"
                  + time.getMinutes().toString() + ":"
                  + time.getSeconds().toString() + "."
                  + time.getMilliseconds().toString();
  console.log("\nReceived message at ", time_string);
  console.log("Topic is ",topic.toString())
  console.log("Payload is ", message.toString())
  //temp = message.toString();
  map.set(topic, message.toString());
  //client.end()
})

app.get("/api", (req, res) => {
    //res.json({ message: "Hello FIIIIC from server!" });
    //var jsonmap = mapToObj(map);
    //var ret = JSON.stringify(jsonmap);
    var jsonmap = Object.fromEntries(map);
    var ret = JSON.stringify(jsonmap);
    console.log(jsonmap);
    res.json({ message: ret });
});
  

app.listen(PORT, () => {
  console.log(`Server listening on ${PORT}`);
});