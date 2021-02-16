# ApiEasyRequest
![](https://jitpack.io/v/SergeyAvetisyan/TemperatureConverter.svg)

![Image of Request](https://www.datto.com/img/library/_800x420_crop_center-center_100_line/1782241/what_is_an_http_request_1600x400.jpg)

Add this in build.gradle

```
allprojects {
      repositories {
        maven { 
              url 'https://jitpack.io' 
              }
    }
}
```



In dependencies add this line

```
implementation 'com.github.SergeyAvetisyan:ApiEasyRequest:{Last Version}'
```


## Super simple to use
Request is designed to be the simplest way possible to make http or https calls.

In first step you must init RequestManager by passing context and BASE_URL
```
RequestManager.init(this,"https://example.com");
```
### Do request
Adding headers and params are optional. Params are HashMap<String,Object> ,headers are HashMap<String,String> , you just need add key and value and pass them to RequestManager

```
        RequestManager.getInstance().doGetRequest(
                "getNumbers/integers",
                paramsMap,
                headersMap,
                new Response() {
                    @Override
                    public void onError(String s) {

                    }

                    @Override
                    public void onResponse(String s) {

                    }
                }
        );
```


"getNumbers/integers" is a path of your url.
 
After success or error it will call onResponse or onError function. You will receive response in String format and can easily parse it to JSON or other object.

You can do Post,Get requests super easy and also doing post request can choose between FormBody and JSON 
