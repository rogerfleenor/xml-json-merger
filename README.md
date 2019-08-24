# xml-json-merger

xml-json-merger reads from XML, reads `vin` and `trim` objects from JSON file and writes `override_trim` as new node in XML file for corresponding `vin`. 

XML is parsed via w3c dom https://docs.oracle.com/javase/7/docs/api/org/w3c/dom/package-summary.html, JSON is read and parsed via simple-json by json.org https://github.com/fangyidong/json-simple and xml is written via https://docs.oracle.com/javase/7/docs/api/index.html.


1) XML Read
```
...
<vin>3FAHP0JG3CR333166</vin>
<stockId>JGB97453A</stockId>
<year>2012</year>
<make>Ford</make>
<model>Fusion</model>
<trim>FXX</trim>
...
```
2) JSON Read
```
...
"3FAHP0JG3CR333166": {
"vin": "3FAHP0JG3CR333166",
"trim": "t123"
}
...
```
3) XML Write
```
...
<vin>3FAHP0JG3CR333166</vin>
<stockId>JGB97453A</stockId>
<year>2012</year>
<make>Ford</make>
<model>Fusion</model>
<trim>FXX</trim>
<override_trim>t123</override_trim>
...
```

Roger Fleenor
