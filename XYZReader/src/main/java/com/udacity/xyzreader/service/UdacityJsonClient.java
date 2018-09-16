package com.udacity.xyzreader.service;

import com.udacity.xyzreader.model.Article;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface UdacityJsonClient {
    @GET
    Call<List<Article>> listArticles(@Url String url);
}