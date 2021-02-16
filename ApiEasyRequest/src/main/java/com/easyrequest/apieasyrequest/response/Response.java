package com.easyrequest.apieasyrequest.response;

public interface Response {
    void onError(String pMessage);
    void onResponse(String pResponse);
}