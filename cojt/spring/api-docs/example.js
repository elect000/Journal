var json = {};
json.y = 0;

// XMLHttpRequestオブジェクトの生成
var xhr = new XMLHttpRequest();

// methodとurlを指定
xhr.open ("POST", "/api/tutorial/plus/3?x=1");

xhr.onreadystatechange = function() {
    if(xhr.readyState == XMLHttpRequest.DONE && xhr.status == 200) {
        alert( this.responseText );
    }
}
// Content-Typeを設定
xhr.setRequestHeader("Content-Type", "application/json");

// 送信
xhr.send(JSON.stringify(json));

//----------------------------------------------------

 // XMLHttpRequestオブジェクトの生成
var xhr = new XMLHttpRequest();

// methodとurlを指定
xhr.open ("GET", "/vchats-api/login?name=user&pass=password");

xhr.onload = function () {
        alert( this.responseText );
};
// 送信
xhr.send(null);

 fetch('/vchats-api/login?name=user&pass=password')
  .then(function(response) {
    return response.json();
  })
  .then(function(myJson) {
    alert(myJson);
  });
//---------------------------------
 var data = {y: 0};

fetch("/api/tutorial/plus/3?x=1", {
  method: 'POST', 
  body: JSON.stringify(data),
  headers: new Headers({
    'Content-Type': 'application/json'
  })
}).then(response => { return response.json(); })
.then (json => alert(JSON.stringify(json)));


// -----------------------------------------
fetch('/vchats-api/login?name=user&pass=password')
  .then(function(response) {
    return response.json();
  })
  .then(function(myJson) {
    alert(myJson["login?"]);
  });
