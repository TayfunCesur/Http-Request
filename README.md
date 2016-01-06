
Simple usage
=============
## HTTP GET
    HttpRequestFactory factory = new HttpRequestFactory(getApplicationContext()) {
            @Override
            public void Happens(RIO rio) {
                //you will have the response data in rio object
            }
        };

        factory.createRequest(RequestType.GET,"https://httpbin.org/get")
                .secure(true)
                .execute();
               
## HTTP POST
    HttpRequestFactory factory = new HttpRequestFactory(getApplicationContext()) {
                @Override
                public void Happens(RIO rio) {
                    
                }
            };
    
            factory.createRequest(RequestType.POST,"https://httpbin.org/post")
                    .secure(true)
                    .setBody("{\"test\":\"object\"}")
                    .execute();
                
RIO Details
===========
RIO is the response identifier object which carries the data related to response and exceptions.


- result , represents the request code; eg 200 HTTP_OK, 404 HTTP_NOT_FOUND etc
- response_json , JSON serialized version of response text, equals to null if there is an exception
- response_str, response text
- exceptions, the list of exceptions along the response comes through, null if there is no exception.
- responseUrl
- request, Request object that performs the http request



License
=======

    Copyright 2016 Ferhat Dündar

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
