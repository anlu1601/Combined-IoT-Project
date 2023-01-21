import React from "react";
import logo from './logo.svg';
import './App.css';


//var map = new Map();

function Showdata(props){
  var jsonObject = JSON.parse(props.sss);
  var mapAgain = new Map(Object.entries(jsonObject));
  var retVal = mapAgain.get('livingroom\\t')
  console.log(retVal);
  //map = mapAgain;
  return retVal;
}

function ShowData2(props){
  var jsonObject = JSON.parse(props);
  const mapAgain = new Map(Object.entries(jsonObject));

}

function DataDiv(props){
  
  var room = props.name;
  var sensors = [room + '\\t', room + '\\h', room + '\\b', ];
  
  var jsonObject = JSON.parse(props.map);
  const mapAgain = new Map(Object.entries(jsonObject));
  
  
  return (
    <div className="dataprint">
      <h2>{room}</h2>
      <p>
          Temperature{" : " + mapAgain.get(sensors[0])}°C <br></br>
          Humidity{" : " + mapAgain.get(sensors[1])}% <br></br>
          Brightness{" : " + mapAgain.get(sensors[2])}lx <br></br>
      </p>
    </div>
  );
}

function Footer(props){
  
  
  
  var jsonObject = JSON.parse(props.map);
  const mapAgain = new Map(Object.entries(jsonObject));


  //mapAgain.forEach((val, key) => key, val);
  //{mapAgain.forEach((val, key) => key +": "+ val)} <br></br> 
  //myMap.forEach( (val, key) => console.log(key, val) );
  //{mapAgain.forEach( (val, key) => {<FooterRow k={key} v={val}/>} )};

  return (
    <div className="footer">
      
      <FooterRow k="testk" v="testv" />
      
    </div>
  );
}
//console.log(key + " : "+val)
//<FooterRow k={key} v={val}/>
function FooterRow(props){


  console.log(props.k + " : "+props.v);
  //{props.k + ": "+props.v}
  return (
    <p>
      TESTING
    </p>

  );
}

function App() {

  const [data, setData] = React.useState(null);

  const getData = async () =>{
    await fetch("/api")
    .then((res) => res.json())
    .then((data) => setData(data.message));

  };


  React.useEffect(() => {
    
    getData();
    const interval = setInterval(()=>{
      getData();
    }, 10000)

    return()=>clearInterval(interval);

  }, []);

  

  return ( // SUPER VIKTIGT MED {!data ? "Loading..." : data} // denna stoppar data från rendras null innan fetch!!!
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <h1>André's Home Sensors</h1>
      </header>
      <div className="content">
        {!data ? "Loading..." : <DataDiv name="livingroom" map={data}/>}
        {!data ? "Loading..." : <DataDiv name="kitchen" map={data}/>}
      </div>
      
      

    </div>
  );
}
//<Showdata sss={data} />
//<DataDiv name="livingroom"/>
//<DataDiv name="kitchen"/>
//{!<DataDiv name="livingroom" map={data}/> ? "Loading..." : <DataDiv name="livingroom" map={data}/>}
//{!<DataDiv name="kitchen" map={data}/> ? "Loading..." : <DataDiv name="kitchen" map={data}/>}
//{!data ? "Loading..." : <Footer map={data} />}
export default App;
