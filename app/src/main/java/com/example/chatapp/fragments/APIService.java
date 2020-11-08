package com.example.chatapp.fragments;

import com.example.chatapp.notifications.MyResponse;
import com.example.chatapp.notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAjZGttYQ:APA91bGX-DXirUZBzRe6cvIifUuFLArhFzYX92d6T-FsmHEY7imSjwIggiCgjIs-flihUuPy2kiRrLjtvKBrR6-JRT6YS76pF7ErjniVb7e1XTO6tfg9H_KNBzrZLXilJNoMw-SVyy94 "
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);


}
